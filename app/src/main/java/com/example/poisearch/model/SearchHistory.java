package com.example.poisearch.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SearchHistory implements Serializable {
    private long id;
    private String keywords;
    private String categoryCode;
    private String categoryName;
    private int radius;
    private double lat;
    private double lng;
    private long searchTime;
    private int resultCount;

    public SearchHistory() {
    }

    // Getters and Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getKeywords() { return keywords; }
    public void setKeywords(String keywords) { this.keywords = keywords; }

    public String getCategoryCode() { return categoryCode; }
    public void setCategoryCode(String categoryCode) { this.categoryCode = categoryCode; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public int getRadius() { return radius; }
    public void setRadius(int radius) { this.radius = radius; }

    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }

    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }

    public long getSearchTime() { return searchTime; }
    public void setSearchTime(long searchTime) { this.searchTime = searchTime; }

    public int getResultCount() { return resultCount; }
    public void setResultCount(int resultCount) { this.resultCount = resultCount; }

    // 格式化时间
    public String getFormattedTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());
        return sdf.format(new Date(searchTime));
    }

    // 格式化范围
    public String getFormattedRadius() {
        if (radius < 1000) {
            return radius + "米";
        } else {
            return String.format("%.1f公里", radius / 1000f);
        }
    }

    // 获取搜索描述
    public String getSearchDescription() {
        StringBuilder sb = new StringBuilder();
        if (keywords != null && !keywords.isEmpty()) {
            sb.append("关键词: ").append(keywords);
        }
        if (categoryName != null && !categoryName.isEmpty()) {
            if (sb.length() > 0) sb.append(" | ");
            sb.append("分类: ").append(categoryName);
        }
        if (sb.length() == 0) {
            sb.append("全部POI");
        }
        return sb.toString();
    }
}
