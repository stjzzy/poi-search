package com.example.poisearch.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.poisearch.model.POI;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class POISearchService {
    private static final String TAG = "POISearchService";
    
    // 使用高德地图API - 需要替换为实际的API Key
    private static final String AMAP_KEY = "d70ddce2dd11293a318d3a3fcde70612";
    private static final String AMAP_NEARBY_URL = "https://restapi.amap.com/v3/place/around";
    private static final String AMAP_TEXT_URL = "https://restapi.amap.com/v3/place/text";
    
    // 每页数量
    private static final int PAGE_SIZE = 25;
    // 最大页数（高德API限制最多返回1000条数据）
    private static final int MAX_PAGES = 40;
    
    private final RequestQueue requestQueue;
    private final Gson gson;
    
    public POISearchService(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
        this.gson = new Gson();
    }
    
    public interface POISearchCallback {
        void onSuccess(List<POI> pois, int totalCount);
        void onError(String error);
    }
    
    public interface POIPageCallback {
        void onPageLoaded(List<POI> pois, int currentPage, int totalPages, int totalCount);
        void onAllPagesLoaded(List<POI> allPois, int totalCount);
        void onError(String error);
    }
    
    /**
     * 搜索附近POI - 加载所有分页数据
     */
    public void searchNearbyAll(double latitude, double longitude, String keywords, 
                                String category, int radius, POIPageCallback callback) {
        List<POI> allPois = new ArrayList<>();
        searchNearbyPage(latitude, longitude, keywords, category, radius, 1, allPois, callback);
    }
    
    /**
     * 分页搜索附近POI
     */
    private void searchNearbyPage(double latitude, double longitude, String keywords, 
                                  String category, int radius, int page, 
                                  List<POI> allPois, POIPageCallback callback) {
        
        StringBuilder urlBuilder = new StringBuilder(AMAP_NEARBY_URL);
        urlBuilder.append("?key=").append(AMAP_KEY);
        urlBuilder.append("&location=").append(longitude).append(",").append(latitude);
        urlBuilder.append("&radius=").append(radius);
        urlBuilder.append("&offset=").append(PAGE_SIZE).append("&page=").append(page);
        urlBuilder.append("&extensions=all");
        urlBuilder.append("&sort=distance"); // 按距离排序
        
        if (keywords != null && !keywords.isEmpty()) {
            urlBuilder.append("&keywords=").append(URLEncoder.encode(keywords, StandardCharsets.UTF_8));
        }
        
        if (category != null && !category.isEmpty()) {
            urlBuilder.append("&types=").append(category);
        }
        
        String url = urlBuilder.toString();
        Log.d(TAG, "Request URL (page " + page + "): " + url);
        
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET, url, null,
            response -> {
                try {
                    PageResult result = parsePOIResponseWithPage(response);
                    allPois.addAll(result.pois);
                    
                    int totalPages = (int) Math.ceil((double) result.totalCount / PAGE_SIZE);
                    Log.d(TAG, "Page " + page + ": loaded " + result.pois.size() + 
                          " POIs, total: " + result.totalCount + ", totalPages: " + totalPages);
                    
                    callback.onPageLoaded(result.pois, page, totalPages, result.totalCount);
                    
                    if (result.pois.size() == PAGE_SIZE && page < totalPages && page < MAX_PAGES) {
                        searchNearbyPage(latitude, longitude, keywords, category, radius, 
                                        page + 1, allPois, callback);
                    } else {
                        Log.d(TAG, "All pages loaded. Total POIs: " + allPois.size());
                        callback.onAllPagesLoaded(allPois, result.totalCount);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Parse error", e);
                    callback.onError("数据解析错误: " + e.getMessage());
                }
            },
            error -> {
                Log.e(TAG, "Request error", error);
                callback.onError("网络请求错误: " + (error.getMessage() != null ? error.getMessage() : "未知错误"));
            }
        );
        
        requestQueue.add(request);
    }
    
    /**
     * 搜索附近POI - 只加载第一页（用于快速预览）
     */
    public void searchNearby(double latitude, double longitude, String keywords, 
                            String category, int radius, final POISearchCallback callback) {
        
        StringBuilder urlBuilder = new StringBuilder(AMAP_NEARBY_URL);
        urlBuilder.append("?key=").append(AMAP_KEY);
        urlBuilder.append("&location=").append(longitude).append(",").append(latitude);
        urlBuilder.append("&radius=").append(radius);
        urlBuilder.append("&offset=").append(PAGE_SIZE).append("&page=1");
        urlBuilder.append("&extensions=all");
        urlBuilder.append("&sort=distance"); // 按距离排序
        
        if (keywords != null && !keywords.isEmpty()) {
            urlBuilder.append("&keywords=").append(URLEncoder.encode(keywords, StandardCharsets.UTF_8));
        }
        
        if (category != null && !category.isEmpty()) {
            urlBuilder.append("&types=").append(category);
        }
        
        String url = urlBuilder.toString();
        Log.d(TAG, "Request URL: " + url);
        
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET, url, null,
            response -> {
                try {
                    PageResult result = parsePOIResponseWithPage(response);
                    callback.onSuccess(result.pois, result.totalCount);
                } catch (Exception e) {
                    Log.e(TAG, "Parse error", e);
                    callback.onError("数据解析错误: " + e.getMessage());
                }
            },
            error -> {
                Log.e(TAG, "Request error", error);
                callback.onError("网络请求错误: " + (error.getMessage() != null ? error.getMessage() : "未知错误"));
            }
        );
        
        requestQueue.add(request);
    }
    
    public void searchByText(String city, String keywords, POISearchCallback callback) {
        StringBuilder urlBuilder = new StringBuilder(AMAP_TEXT_URL);
        urlBuilder.append("?key=").append(AMAP_KEY);
        urlBuilder.append("&keywords=").append(URLEncoder.encode(keywords, StandardCharsets.UTF_8));
        urlBuilder.append("&city=").append(URLEncoder.encode(city, StandardCharsets.UTF_8));
        urlBuilder.append("&offset=").append(PAGE_SIZE).append("&page=1");
        urlBuilder.append("&extensions=all");
        
        String url = urlBuilder.toString();
        
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET, url, null,
            response -> {
                try {
                    PageResult result = parsePOIResponseWithPage(response);
                    callback.onSuccess(result.pois, result.totalCount);
                } catch (Exception e) {
                    callback.onError("数据解析错误: " + e.getMessage());
                }
            },
            error -> {
                callback.onError("网络请求错误: " + (error.getMessage() != null ? error.getMessage() : "未知错误"));
            }
        );
        
        requestQueue.add(request);
    }
    
    private static class PageResult {
        List<POI> pois;
        int totalCount;
        
        PageResult(List<POI> pois, int totalCount) {
            this.pois = pois;
            this.totalCount = totalCount;
        }
    }
    
    private PageResult parsePOIResponseWithPage(JSONObject response) throws JSONException {
        List<POI> pois = new ArrayList<>();
        int totalCount = 0;
        
        Log.d(TAG, "API Response: " + response.toString());
        
        // 检查状态码
        String status = response.optString("status", "0");
        String info = response.optString("info", "");
        String infocode = response.optString("infocode", "");
        
        Log.d(TAG, "Status: " + status + ", Info: " + info + ", Code: " + infocode);
        
        if (!"1".equals(status)) {
            Log.e(TAG, "API Error: " + info + " (Code: " + infocode + ")");
            return new PageResult(pois, 0);
        }
        
        // 获取总数
        String countStr = response.optString("count", "0");
        try {
            totalCount = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            totalCount = 0;
        }
        
        // 检查是否有pois字段
        if (!response.has("pois")) {
            Log.w(TAG, "No 'pois' field in response");
            return new PageResult(pois, totalCount);
        }
        
        org.json.JSONArray poisArray = response.getJSONArray("pois");
        Log.d(TAG, "Found " + poisArray.length() + " POIs in this page");
        
        for (int i = 0; i < poisArray.length(); i++) {
            JSONObject poiObj = poisArray.getJSONObject(i);
            POI poi = new POI();
            poi.setId(poiObj.optString("id"));
            poi.setName(poiObj.optString("name"));
            poi.setAddress(poiObj.optString("address"));
            poi.setPhone(poiObj.optString("tel"));
            poi.setCategory(poiObj.optString("type"));
            
            String location = poiObj.optString("location");
            if (location != null && location.contains(",")) {
                String[] coords = location.split(",");
                poi.setLongitude(Double.parseDouble(coords[0]));
                poi.setLatitude(Double.parseDouble(coords[1]));
            }
            
            String distance = poiObj.optString("distance");
            if (!distance.isEmpty()) {
                poi.setDistance(Double.parseDouble(distance));
            }
            
            pois.add(poi);
        }
        
        return new PageResult(pois, totalCount);
    }
    
    public String getCategoryCode(String categoryName) {
        switch (categoryName) {
            case "餐饮":
                return "050000";
            case "酒店":
                return "100000";
            case "购物":
                return "060000";
            case "交通":
                return "150000";
            default:
                return "";
        }
    }
}
