package com.example.poisearch.service;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.poisearch.model.POI;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 网格分片搜索服务 - 突破高德API 200条限制
 * 将大区域分割成多个小网格分别搜索，合并结果
 */
public class GridSearchService {
    private static final String TAG = "GridSearchService";
    
    private static final String AMAP_KEY = "d70ddce2dd11293a318d3a3fcde70612";
    private static final String AMAP_NEARBY_URL = "https://restapi.amap.com/v3/place/around";
    
    // 每个网格的搜索半径（米）- 建议 500-1000米，确保网格间有重叠避免遗漏
    private static final int GRID_RADIUS = 800;
    // 每个网格最多获取的POI数
    private static final int GRID_PAGE_SIZE = 25;
    // 每个网格最大页数
    private static final int GRID_MAX_PAGES = 8;
    
    private final RequestQueue requestQueue;
    private final Set<String> poiIdSet = new HashSet<>(); // 用于去重
    private final List<POI> allPois = new ArrayList<>();
    
    // 搜索中心坐标，用于重新计算POI到中心的真实距离
    private double searchCenterLat;
    private double searchCenterLng;
    
    // 搜索状态
    private int totalGrids = 0;
    private int completedGrids = 0;
    private boolean isCancelled = false;
    
    public interface GridSearchCallback {
        void onGridLoaded(int completedGrids, int totalGrids, int currentGridPoiCount, int totalPoiCount);
        void onAllGridsLoaded(List<POI> allPois, int totalCount);
        void onError(String error);
    }
    
    public GridSearchService(Context context) {
        this.requestQueue = Volley.newRequestQueue(context);
    }
    
    /**
     * 执行网格分片搜索
     * @param centerLat 中心纬度
     * @param centerLng 中心经度
     * @param totalRadius 总搜索半径（米）
     * @param keywords 关键词（可选）
     * @param category 分类代码（可选）
     * @param callback 回调
     */
    public void searchByGrid(double centerLat, double centerLng, int totalRadius,
                            String keywords, String category, GridSearchCallback callback) {
        
        // 重置状态
        poiIdSet.clear();
        allPois.clear();
        completedGrids = 0;
        isCancelled = false;
        
        // 保存搜索中心，用于重新计算每个POI到中心的真实距离
        this.searchCenterLat = centerLat;
        this.searchCenterLng = centerLng;
        
        // 生成网格中心点
        List<GridPoint> gridPoints = generateGridPoints(centerLat, centerLng, totalRadius, GRID_RADIUS);
        totalGrids = gridPoints.size();
        
        Log.d(TAG, "Grid search started: " + totalGrids + " grids, total radius: " + totalRadius + "m");
        
        // 开始搜索所有网格
        searchGridBatch(gridPoints, 0, keywords, category, callback);
    }
    
    /**
     * 生成网格中心点
     */
    private List<GridPoint> generateGridPoints(double centerLat, double centerLng, 
                                               int totalRadius, int gridRadius) {
        List<GridPoint> points = new ArrayList<>();
        
        // 计算网格数量（每个方向）
        int gridsPerDirection = (int) Math.ceil((double) totalRadius / gridRadius);
        
        // 经纬度偏移量（约每1000米）
        double latOffset = 0.009;  // 约1000米
        double lngOffset = 0.009 / Math.cos(Math.toRadians(centerLat)); // 根据纬度调整
        
        // 按比例调整偏移
        latOffset = latOffset * gridRadius / 1000.0;
        lngOffset = lngOffset * gridRadius / 1000.0;
        
        // 生成网格点（从中心向外扩散）
        for (int i = -gridsPerDirection; i <= gridsPerDirection; i++) {
            for (int j = -gridsPerDirection; j <= gridsPerDirection; j++) {
                double lat = centerLat + i * latOffset;
                double lng = centerLng + j * lngOffset;
                
                // 计算该网格中心到总中心的距离
                double distance = calculateDistance(centerLat, centerLng, lat, lng);
                
                // 只保留在总半径范围内的网格
                if (distance <= totalRadius) {
                    points.add(new GridPoint(lat, lng, i, j));
                }
            }
        }
        
        // 按距离中心远近排序，优先搜索中心区域
        points.sort((a, b) -> {
            double distA = calculateDistance(centerLat, centerLng, a.lat, a.lng);
            double distB = calculateDistance(centerLat, centerLng, b.lat, b.lng);
            return Double.compare(distA, distB);
        });
        
        return points;
    }
    
    /**
     * 批量搜索网格（控制并发数）
     */
    private void searchGridBatch(List<GridPoint> gridPoints, int startIndex,
                                String keywords, String category, GridSearchCallback callback) {
        if (isCancelled) return;
        
        if (startIndex >= gridPoints.size()) {
            // 所有网格搜索完成
            callback.onAllGridsLoaded(allPois, allPois.size());
            return;
        }
        
        GridPoint point = gridPoints.get(startIndex);
        
        searchSingleGrid(point, keywords, category, new GridSingleCallback() {
            @Override
            public void onComplete(int poiCount) {
                completedGrids++;
                callback.onGridLoaded(completedGrids, totalGrids, poiCount, allPois.size());
                
                // 继续搜索下一个网格
                searchGridBatch(gridPoints, startIndex + 1, keywords, category, callback);
            }
            
            @Override
            public void onError(String error) {
                // 单个网格错误不影响整体，继续下一个
                completedGrids++;
                searchGridBatch(gridPoints, startIndex + 1, keywords, category, callback);
            }
        });
    }
    
    /**
     * 搜索单个网格
     */
    private void searchSingleGrid(GridPoint point, String keywords, String category,
                                  GridSingleCallback callback) {
        searchGridPage(point, keywords, category, 1, new ArrayList<>(), callback);
    }
    
    private void searchGridPage(GridPoint point, String keywords, String category,
                               int page, List<POI> gridPois, GridSingleCallback callback) {
        if (isCancelled) return;
        
        StringBuilder urlBuilder = new StringBuilder(AMAP_NEARBY_URL);
        urlBuilder.append("?key=").append(AMAP_KEY);
        urlBuilder.append("&location=").append(point.lng).append(",").append(point.lat);
        urlBuilder.append("&radius=").append(GRID_RADIUS);
        urlBuilder.append("&offset=").append(GRID_PAGE_SIZE).append("&page=").append(page);
        urlBuilder.append("&extensions=all");
        urlBuilder.append("&sort=distance"); // 按距离排序
        
        // 关键词和分类二选一，避免交集导致结果过少
        // 优先使用关键词，如果没有关键词则使用分类
        if (keywords != null && !keywords.isEmpty()) {
            urlBuilder.append("&keywords=").append(URLEncoder.encode(keywords, StandardCharsets.UTF_8));
        } else if (category != null && !category.isEmpty()) {
            urlBuilder.append("&types=").append(category);
        }
        
        String url = urlBuilder.toString();
        
        JsonObjectRequest request = new JsonObjectRequest(
            Request.Method.GET, url, null,
            response -> {
                try {
                    GridResult result = parseGridResponse(response);
                    
                    // 添加到网格结果（去重）
                    for (POI poi : result.pois) {
                        if (!poiIdSet.contains(poi.getId())) {
                            poiIdSet.add(poi.getId());
                            gridPois.add(poi);
                            allPois.add(poi);
                        }
                    }
                    
                    // 判断是否继续加载该网格
                    int totalPages = (int) Math.ceil((double) result.totalCount / GRID_PAGE_SIZE);
                    if (result.pois.size() == GRID_PAGE_SIZE && page < totalPages 
                        && page < GRID_MAX_PAGES && gridPois.size() < 200) {
                        searchGridPage(point, keywords, category, page + 1, gridPois, callback);
                    } else {
                        callback.onComplete(gridPois.size());
                    }
                } catch (Exception e) {
                    callback.onError(e.getMessage());
                }
            },
            error -> callback.onError(error.getMessage())
        );
        
        requestQueue.add(request);
    }
    
    private GridResult parseGridResponse(JSONObject response) throws JSONException {
        List<POI> pois = new ArrayList<>();
        int totalCount = 0;
        
        String status = response.optString("status", "0");
        if (!"1".equals(status)) {
            return new GridResult(pois, 0);
        }
        
        String countStr = response.optString("count", "0");
        try {
            totalCount = Integer.parseInt(countStr);
        } catch (NumberFormatException e) {
            totalCount = 0;
        }
        
        if (!response.has("pois")) {
            return new GridResult(pois, totalCount);
        }
        
        org.json.JSONArray poisArray = response.getJSONArray("pois");
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
            
            // 重新计算POI到搜索中心的真实距离（覆盖网格中心的距离）
            if (poi.getLatitude() != 0 && poi.getLongitude() != 0) {
                double realDistance = calculateDistance(searchCenterLat, searchCenterLng,
                                                         poi.getLatitude(), poi.getLongitude());
                poi.setDistance(realDistance);
            }
            
            pois.add(poi);
        }
        
        return new GridResult(pois, totalCount);
    }
    
    /**
     * 计算两点间距离（米）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371000; // 地球半径（米）
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
    
    public void cancel() {
        isCancelled = true;
        requestQueue.cancelAll(request -> true);
    }
    
    // 内部类
    private static class GridPoint {
        double lat, lng;
        int gridX, gridY;
        
        GridPoint(double lat, double lng, int x, int y) {
            this.lat = lat;
            this.lng = lng;
            this.gridX = x;
            this.gridY = y;
        }
    }
    
    private static class GridResult {
        List<POI> pois;
        int totalCount;
        
        GridResult(List<POI> pois, int totalCount) {
            this.pois = pois;
            this.totalCount = totalCount;
        }
    }
    
    private interface GridSingleCallback {
        void onComplete(int poiCount);
        void onError(String error);
    }
}
