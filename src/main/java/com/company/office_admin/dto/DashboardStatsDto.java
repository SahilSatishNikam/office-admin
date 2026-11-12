package com.company.office_admin.dto;

public class DashboardStatsDto {
    private String totalFollowers;
    private String followersGrowth;
    private String totalEngagement;
    private String engagementGrowth;
    private long publishedPosts;
    private String publishedGrowth;
    private long scheduledPosts;
    private String scheduledGrowth;
    private String totalReach;
    private String avgEngagementRate;

    // Real dynamic interaction counts
    private String totalLikes;
    private String totalComments;
    private String totalShares;

    // Real follower growth attributes
    private String netGrowth;
    private String organicGrowth;
    private String paidGrowth;
    private String peakDay;

    // Workspace capacity
    private int connectedAccountsCount;
    private int maxAccounts = 20;
    private int planUsagePercentage;

    public DashboardStatsDto() {}

    public String getTotalFollowers() { return totalFollowers; }
    public void setTotalFollowers(String totalFollowers) { this.totalFollowers = totalFollowers; }

    public String getFollowersGrowth() { return followersGrowth; }
    public void setFollowersGrowth(String followersGrowth) { this.followersGrowth = followersGrowth; }

    public String getTotalEngagement() { return totalEngagement; }
    public void setTotalEngagement(String totalEngagement) { this.totalEngagement = totalEngagement; }

    public String getEngagementGrowth() { return engagementGrowth; }
    public void setEngagementGrowth(String engagementGrowth) { this.engagementGrowth = engagementGrowth; }

    public long getPublishedPosts() { return publishedPosts; }
    public void setPublishedPosts(long publishedPosts) { this.publishedPosts = publishedPosts; }

    public String getPublishedGrowth() { return publishedGrowth; }
    public void setPublishedGrowth(String publishedGrowth) { this.publishedGrowth = publishedGrowth; }

    public long getScheduledPosts() { return scheduledPosts; }
    public void setScheduledPosts(long scheduledPosts) { this.scheduledPosts = scheduledPosts; }

    public String getScheduledGrowth() { return scheduledGrowth; }
    public void setScheduledGrowth(String scheduledGrowth) { this.scheduledGrowth = scheduledGrowth; }

    public String getTotalReach() { return totalReach; }
    public void setTotalReach(String totalReach) { this.totalReach = totalReach; }

    public String getAvgEngagementRate() { return avgEngagementRate; }
    public void setAvgEngagementRate(String avgEngagementRate) { this.avgEngagementRate = avgEngagementRate; }

    public String getTotalLikes() { return totalLikes; }
    public void setTotalLikes(String totalLikes) { this.totalLikes = totalLikes; }

    public String getTotalComments() { return totalComments; }
    public void setTotalComments(String totalComments) { this.totalComments = totalComments; }

    public String getTotalShares() { return totalShares; }
    public void setTotalShares(String totalShares) { this.totalShares = totalShares; }

    public String getNetGrowth() { return netGrowth; }
    public void setNetGrowth(String netGrowth) { this.netGrowth = netGrowth; }

    public String getOrganicGrowth() { return organicGrowth; }
    public void setOrganicGrowth(String organicGrowth) { this.organicGrowth = organicGrowth; }

    public String getPaidGrowth() { return paidGrowth; }
    public void setPaidGrowth(String paidGrowth) { this.paidGrowth = paidGrowth; }

    public String getPeakDay() { return peakDay; }
    public void setPeakDay(String peakDay) { this.peakDay = peakDay; }

    public int getConnectedAccountsCount() { return connectedAccountsCount; }
    public void setConnectedAccountsCount(int connectedAccountsCount) { this.connectedAccountsCount = connectedAccountsCount; }

    public int getMaxAccounts() { return maxAccounts; }
    public void setMaxAccounts(int maxAccounts) { this.maxAccounts = maxAccounts; }

    public int getPlanUsagePercentage() { return planUsagePercentage; }
    public void setPlanUsagePercentage(int planUsagePercentage) { this.planUsagePercentage = planUsagePercentage; }
}
