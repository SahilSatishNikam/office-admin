package com.company.office_admin.config;

import com.company.office_admin.entity.User;
import com.company.office_admin.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        admin.setEmail(adminEmail);
        admin.setPhoneNumber("");
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
    }
}
