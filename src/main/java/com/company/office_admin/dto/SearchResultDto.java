package com.company.office_admin.dto;

import java.util.List;
import java.util.Map;

public class SearchResultDto {
    private String query;
    private int totalResults;
    private List<Map<String, Object>> pages;
    private List<Map<String, Object>> accounts;
    private List<Map<String, Object>> posts;

    public SearchResultDto() {}

    public SearchResultDto(String query, int totalResults, List<Map<String, Object>> pages, List<Map<String, Object>> accounts, List<Map<String, Object>> posts) {
        this.query = query;
        this.totalResults = totalResults;
        this.pages = pages;
        this.accounts = accounts;
        this.posts = posts;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public List<Map<String, Object>> getPages() {
        return pages;
    }

    public void setPages(List<Map<String, Object>> pages) {
        this.pages = pages;
    }

    public List<Map<String, Object>> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Map<String, Object>> accounts) {
        this.accounts = accounts;
    }

    public List<Map<String, Object>> getPosts() {
        return posts;
    }

    public void setPosts(List<Map<String, Object>> posts) {
        this.posts = posts;
    }
}

