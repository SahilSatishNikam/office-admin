package com.company.office_admin.config;

import com.company.office_admin.entity.*;
import com.company.office_admin.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SocialAccountRepository accountRepository;
    private final FollowerHistoryRepository historyRepository;
    private final SocialPostRepository postRepository;
    private final NotificationRepository notificationRepository;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Value("${app.admin.email:admin@company.com}")
    private String adminEmail;

    @Value("${app.designer.username:designer}")
    private String designerUsername;

    @Value("${app.designer.password:designer123}")
    private String designerPassword;

    @Value("${app.designer.email:designer@company.com}")
    private String designerEmail;

    @Value("${app.creator.username:creator}")
    private String creatorUsername;

    @Value("${app.creator.password:creator123}")
    private String creatorPassword;

    @Value("${app.creator.email:creator@company.com}")
    private String creatorEmail;

    public DataInitializer(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           SocialAccountRepository accountRepository,
                           FollowerHistoryRepository historyRepository,
                           SocialPostRepository postRepository,
                           NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountRepository = accountRepository;
        this.historyRepository = historyRepository;
        this.postRepository = postRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    public void run(String... args) {
        // 1. Admin User (Master Admin with Full Control)
        User admin = userRepository.findByUsername(adminUsername).orElseGet(() -> new User(
                adminUsername,
                passwordEncoder.encode(adminPassword),
                "Administrator",
                adminEmail,
                "ROLE_ADMIN",
                "AD"
        ));
        admin.setRole("ROLE_ADMIN");
        admin.setEmail("sahilnikam107@gmail.com");
        admin.setPhoneNumber("+917385192062");
        admin.setEmailNotificationsEnabled(true);
        admin.setSmsNotificationsEnabled(true);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setEnabled(true);
        userRepository.save(admin);

        // 2. Designer User (Designer Vault Workspace Only)
        User designer = userRepository.findByUsername(designerUsername).orElseGet(() -> new User(
                designerUsername,
                passwordEncoder.encode(designerPassword),
                "Creative Designer",
                designerEmail,
                "ROLE_DESIGNER",
                "DS"
        ));
        designer.setRole("ROLE_DESIGNER");
        designer.setPassword(passwordEncoder.encode(designerPassword));
        designer.setEnabled(true);
        userRepository.save(designer);

        // 3. Creator User (Creator Studio Workspace Only)
        User creator = userRepository.findByUsername(creatorUsername).orElseGet(() -> new User(
                creatorUsername,
                passwordEncoder.encode(creatorPassword),
                "Content Creator",
                creatorEmail,
                "ROLE_CREATOR",
                "CC"
        ));
        creator.setRole("ROLE_CREATOR");
        creator.setPassword(passwordEncoder.encode(creatorPassword));
        creator.setEnabled(true);
        userRepository.save(creator);

        // 4. Seed Connected Social Accounts if empty
        if (accountRepository.count() == 0) {
            SocialAccount insta = new SocialAccount("Instagram", "@codemetric.app", 42900L, 12.3, 5.8, 124, "instagram", "#e1306c", true);
            insta.setProfileUrl("https://instagram.com/codemetric.app");
            SocialAccount linkedin = new SocialAccount("LinkedIn", "@codemetric-io", 18400L, 8.5, 4.2, 86, "linkedin", "#0a66c2", true);
            linkedin.setProfileUrl("https://linkedin.com/company/codemetric-io");
            SocialAccount twitter = new SocialAccount("X (Twitter)", "@codemetric", 12200L, 4.7, 3.1, 310, "twitter", "#0f1419", true);
            twitter.setProfileUrl("https://x.com/codemetric");
            SocialAccount youtube = new SocialAccount("YouTube", "CodeMetric Tech", 8650L, 15.2, 6.4, 45, "youtube", "#cc0000", true);
            youtube.setProfileUrl("https://youtube.com/@codemetric");
            accountRepository.saveAll(List.of(insta, linkedin, twitter, youtube));
        }

        // 5. Seed Follower History for dynamic Chart.js periods if empty
        if (historyRepository.count() == 0) {
            LocalDate today = LocalDate.now();

            // 7D History
            historyRepository.saveAll(List.of(
                    new FollowerHistory(today.minusDays(6), "7d", "Day 1", 81200L, 80800L),
                    new FollowerHistory(today.minusDays(5), "7d", "Day 2", 81500L, 81000L),
                    new FollowerHistory(today.minusDays(4), "7d", "Day 3", 81850L, 81200L),
                    new FollowerHistory(today.minusDays(3), "7d", "Day 4", 82100L, 81500L),
                    new FollowerHistory(today.minusDays(2), "7d", "Day 5", 82350L, 81750L),
                    new FollowerHistory(today.minusDays(1), "7d", "Day 6", 82700L, 82000L),
                    new FollowerHistory(today, "7d", "Day 7", 83150L, 82250L)
            ));

            // 30D History
            historyRepository.saveAll(List.of(
                    new FollowerHistory(today.minusDays(28), "30d", "Week 1", 74200L, 71500L),
                    new FollowerHistory(today.minusDays(21), "30d", "Week 2", 76800L, 73800L),
                    new FollowerHistory(today.minusDays(14), "30d", "Week 3", 79400L, 76200L),
                    new FollowerHistory(today.minusDays(7), "30d", "Week 4", 81500L, 78400L),
                    new FollowerHistory(today, "30d", "Current", 83150L, 80100L)
            ));

            // 90D History
            historyRepository.saveAll(List.of(
                    new FollowerHistory(today.minusDays(90), "90d", "Month 1", 62000L, 58000L),
                    new FollowerHistory(today.minusDays(60), "90d", "Month 2", 69500L, 64200L),
                    new FollowerHistory(today.minusDays(30), "90d", "Month 3", 76800L, 70900L),
                    new FollowerHistory(today, "90d", "Current", 83150L, 77500L)
            ));

            // 1Y History
            historyRepository.saveAll(List.of(
                    new FollowerHistory(today.minusMonths(12), "1y", "Q1", 45000L, 38000L),
                    new FollowerHistory(today.minusMonths(9), "1y", "Q2", 56000L, 47000L),
                    new FollowerHistory(today.minusMonths(6), "1y", "Q3", 68500L, 57500L),
                    new FollowerHistory(today.minusMonths(3), "1y", "Q4", 76000L, 68000L),
                    new FollowerHistory(today, "1y", "Current", 83150L, 74000L)
            ));
        }

        // 6. Seed Social Posts if empty
        if (postRepository.count() == 0) {
            LocalDateTime now = LocalDateTime.now();

            SocialPost p1 = new SocialPost(
                    "LinkedIn", "linkedin", "@codemetric-io",
                    "Announcing our new cloud analytics dashboard release! 🚀",
                    "We're thrilled to unveil our real-time social metrics and post automation platform for modern teams.",
                    "Single Image", "PUBLISHED", null, now.minusDays(3),
                    1420L, 85L, 42L, 18500L, 4.8
            );

            SocialPost p2 = new SocialPost(
                    "Instagram", "instagram", "@codemetric.app",
                    "Top 5 UX Design Patterns for High-Converting SaaS Apps",
                    "Swipe to discover the design strategies that doubled engagement in our latest product release. #UX #Design",
                    "Carousel", "PUBLISHED", null, now.minusDays(1),
                    2850L, 164L, 98L, 34200L, 6.2
            );

            SocialPost p3 = new SocialPost(
                    "X (Twitter)", "twitter", "@codemetric",
                    "Speed matters: Reducing query latency by 4x using spring caching and connection pooling.",
                    "Quick thread on database performance optimizations for Spring Boot applications in production 🧵👇",
                    "Thread", "PUBLISHED", null, now.minusHours(6),
                    890L, 54L, 31L, 11200L, 3.9
            );

            SocialPost p4 = new SocialPost(
                    "LinkedIn", "linkedin", "@codemetric-io",
                    "Why developer velocity matters more than ever in 2026 💡",
                    "Building resilient systems requires empowering engineers with automated workflows and instant feedback loops.",
                    "Single Image", "SCHEDULED", now.plusDays(1), null,
                    0L, 0L, 0L, 0L, 0.0
            );
            p4.setRemindEmail(true);
            p4.setRemindSms(true);
            p4.setReminderMinutesBefore(15);
            p4.setRecipientEmail("sahilnikam107@gmail.com");
            p4.setRecipientPhone("+917385192062");
            p4.setConfirmationSent(true);

            postRepository.saveAll(List.of(p1, p2, p3, p4));
        }
    }
}

