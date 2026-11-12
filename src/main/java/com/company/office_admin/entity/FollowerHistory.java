package com.company.office_admin.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "follower_history")
public class FollowerHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recorded_date", nullable = false)
    private LocalDate recordedDate;

    @Column(name = "period_tag", nullable = false)
    private String periodTag; // 7d, 30d, 90d, 1y

    @Column(name = "label_text", nullable = false)
    private String labelText;

    @Column(name = "current_followers", nullable = false)
    private Long currentFollowers;

    @Column(name = "previous_followers", nullable = false)
    private Long previousFollowers;

    public FollowerHistory() {}

    public FollowerHistory(LocalDate recordedDate, String periodTag, String labelText,
                           Long currentFollowers, Long previousFollowers) {
        this.recordedDate = recordedDate;
        this.periodTag = periodTag;
        this.labelText = labelText;
        this.currentFollowers = currentFollowers;
        this.previousFollowers = previousFollowers;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getRecordedDate() { return recordedDate; }
    public void setRecordedDate(LocalDate recordedDate) { this.recordedDate = recordedDate; }

    public String getPeriodTag() { return periodTag; }
    public void setPeriodTag(String periodTag) { this.periodTag = periodTag; }

    public String getLabelText() { return labelText; }
    public void setLabelText(String labelText) { this.labelText = labelText; }

    public Long getCurrentFollowers() { return currentFollowers; }
    public void setCurrentFollowers(Long currentFollowers) { this.currentFollowers = currentFollowers; }

    public Long getPreviousFollowers() { return previousFollowers; }
    public void setPreviousFollowers(Long previousFollowers) { this.previousFollowers = previousFollowers; }
}

