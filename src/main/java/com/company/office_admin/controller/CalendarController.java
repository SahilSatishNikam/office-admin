package com.company.office_admin.controller;

import com.company.office_admin.entity.SocialPost;
import com.company.office_admin.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Controller
@RequestMapping("/calendar")
public class CalendarController {

    private final DashboardService dashboardService;

    public CalendarController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public String showCalendarPage(@RequestParam(required = false) Integer year,
                                   @RequestParam(required = false) Integer month,
                                   Model model) {
        LocalDate today = LocalDate.now();
        int targetYear = (year != null && year >= 2020 && year <= 2040) ? year : today.getYear();
        int targetMonth = (month != null && month >= 1 && month <= 12) ? month : today.getMonthValue();

        YearMonth ym = YearMonth.of(targetYear, targetMonth);
        int daysInMonth = ym.lengthOfMonth();
        int firstDayOfWeek = ym.atDay(1).getDayOfWeek().getValue(); // 1 = Mon ... 7 = Sun
        int leadEmptyDays = firstDayOfWeek - 1; // 0 for Mon ... 6 for Sun

        String monthTitle = ym.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + targetYear;

        // Fetch real scheduled posts from database
        List<SocialPost> scheduledPosts = dashboardService.getUpcomingScheduledPosts();
        Map<Integer, List<SocialPost>> postsByDay = new HashMap<>();

        for (SocialPost post : scheduledPosts) {
            if (post.getScheduledAt() != null) {
                if (post.getScheduledAt().getYear() == targetYear && post.getScheduledAt().getMonthValue() == targetMonth) {
                    int day = post.getScheduledAt().getDayOfMonth();
                    postsByDay.computeIfAbsent(day, k -> new ArrayList<>()).add(post);
                }
            }
        }

        // Previous and Next month calculations
        YearMonth prevYm = ym.minusMonths(1);
        YearMonth nextYm = ym.plusMonths(1);

        model.addAttribute("pageTitle", "Content Calendar");
        model.addAttribute("activeMenu", "calendar");
        model.addAttribute("monthTitle", monthTitle);
        model.addAttribute("currentYear", targetYear);
        model.addAttribute("currentMonth", targetMonth);
        model.addAttribute("daysInMonth", daysInMonth);
        model.addAttribute("leadEmptyDays", leadEmptyDays);
        model.addAttribute("todayDay", (today.getYear() == targetYear && today.getMonthValue() == targetMonth) ? today.getDayOfMonth() : -1);
        model.addAttribute("postsByDay", postsByDay);
        model.addAttribute("totalScheduled", scheduledPosts.size());
        model.addAttribute("prevYear", prevYm.getYear());
        model.addAttribute("prevMonth", prevYm.getMonthValue());
        model.addAttribute("nextYear", nextYm.getYear());
        model.addAttribute("nextMonth", nextYm.getMonthValue());

        return "calendar";
    }
}

