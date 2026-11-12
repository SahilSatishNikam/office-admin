package com.company.office_admin.dto;

import java.util.List;

public class ChartSeriesDto {
    private List<String> labels;
    private List<Long> currentPeriod;
    private List<Long> previousPeriod;

    public ChartSeriesDto() {}

    public ChartSeriesDto(List<String> labels, List<Long> currentPeriod, List<Long> previousPeriod) {
        this.labels = labels;
        this.currentPeriod = currentPeriod;
        this.previousPeriod = previousPeriod;
    }

    public List<String> getLabels() { return labels; }
    public void setLabels(List<String> labels) { this.labels = labels; }

    public List<Long> getCurrentPeriod() { return currentPeriod; }
    public void setCurrentPeriod(List<Long> currentPeriod) { this.currentPeriod = currentPeriod; }

    public List<Long> getPreviousPeriod() { return previousPeriod; }
    public void setPreviousPeriod(List<Long> previousPeriod) { this.previousPeriod = previousPeriod; }
}

