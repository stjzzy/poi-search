package com.example.poisearch;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.CircleOptions;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.example.poisearch.model.POI;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class AMapActivity extends AppCompatActivity {
    private static final String TAG = "AMapActivity";
    private static final int BATCH_SIZE = 200; // 每批显示200个标记

    public static final String EXTRA_POI_LIST = "poi_list";
    public static final String EXTRA_CENTER_LAT = "center_lat";
    public static final String EXTRA_CENTER_LNG = "center_lng";
    public static final String EXTRA_RADIUS = "radius";

    private MapView mapView;
    private AMap aMap;
    private List<POI> poiList;
    private double centerLat;
    private double centerLng;
    private int radius;

    // 与图例完全一致的颜色
    private static final int COLOR_GREEN = 0xFF4CAF50;   // < 500米
    private static final int COLOR_YELLOW = 0xFFFFEB3B;   // 500-1500米
    private static final int COLOR_RED = 0xFFF44336;      // > 1500米
    private static final int COLOR_CENTER = 0xFF2196F3;   // 搜索中心（蓝色）

    // 分批加载相关
    private int displayedCount = 0; // 已显示的标记数
    private View batchLoadBar;
    private TextView tvBatchInfo;
    private MaterialButton btnLoadMore;
    private Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amap);

        // 获取传递的数据 - 从静态数据持有者读取，避免Intent大数据崩溃
        poiList = POIDataHolder.getPoiList();
        centerLat = getIntent().getDoubleExtra(EXTRA_CENTER_LAT, POIDataHolder.getCenterLat());
        centerLng = getIntent().getDoubleExtra(EXTRA_CENTER_LNG, POIDataHolder.getCenterLng());
        radius = getIntent().getIntExtra(EXTRA_RADIUS, POIDataHolder.getRadius());

        Log.d(TAG, "=== AMapActivity onCreate ===");
        Log.d(TAG, "POI List: " + (poiList != null ? poiList.size() : "null"));
        Log.d(TAG, "Center: " + centerLat + ", " + centerLng);
        Log.d(TAG, "Radius: " + radius);

        if (poiList == null || poiList.isEmpty()) {
            Toast.makeText(this, "没有POI数据可显示", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // 初始化地图
        mapView = findViewById(R.id.map);
        if (mapView == null) {
            Log.e(TAG, "MapView is null! Check layout file.");
            Toast.makeText(this, "地图视图未找到", Toast.LENGTH_LONG).show();
            return;
        }

        mapView.onCreate(savedInstanceState);

        // 初始化分批加载控件（必须在initMap之前，因为initMap会调用loadNextBatch→updateBatchBar）
        batchLoadBar = findViewById(R.id.batchLoadBar);
        tvBatchInfo = findViewById(R.id.tvBatchInfo);
        btnLoadMore = findViewById(R.id.btnLoadMore);
        btnLoadMore.setOnClickListener(v -> loadNextBatch());

        initMap();

        // 返回按钮
        findViewById(R.id.backButton).setOnClickListener(v -> finish());

        // 定位按钮
        findViewById(R.id.locationButton).setOnClickListener(v -> moveToCenter());

        // 切换地图类型
        MaterialButton mapTypeButton = findViewById(R.id.mapTypeButton);
        mapTypeButton.setOnClickListener(v -> toggleMapType());
    }

    private void initMap() {
        if (aMap == null) {
            aMap = mapView.getMap();
        }

        if (aMap == null) {
            Toast.makeText(this, "地图加载失败", Toast.LENGTH_LONG).show();
            return;
        }

        // UI设置
        try {
            aMap.getUiSettings().setZoomControlsEnabled(true);
        } catch (Exception e) {
            Log.e(TAG, "Error setting UI: " + e.getMessage());
        }

        // 设置定位样式 - 仅显示定位点，不跟随移动
        try {
            MyLocationStyle myLocationStyle = new MyLocationStyle();
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
            myLocationStyle.interval(2000);
            aMap.setMyLocationStyle(myLocationStyle);
            aMap.getUiSettings().setMyLocationButtonEnabled(false);
            aMap.getUiSettings().setScrollGesturesEnabled(true);
        } catch (Exception e) {
            Log.e(TAG, "Error setting location style: " + e.getMessage());
        }

        // 检查权限并启用定位
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                aMap.setMyLocationEnabled(true);
            } catch (Exception e) {
                Log.e(TAG, "Error enabling my location: " + e.getMessage());
            }
        }

        // 显示搜索中心和范围
        showSearchArea();

        // 分批显示POI标记（第一批）
        displayedCount = 0;
        loadNextBatch();

        // 设置标记点击事件
        aMap.setOnMarkerClickListener(marker -> {
            String title = marker.getTitle();
            Toast.makeText(this, title, Toast.LENGTH_SHORT).show();
            return true;
        });

        Log.d(TAG, "Map initialized successfully");
    }

    private void showSearchArea() {
        if (aMap == null) return;

        try {
            LatLng center = new LatLng(centerLat, centerLng);

            // 添加中心点标记
            aMap.addMarker(new MarkerOptions()
                    .position(center)
                    .title("搜索中心")
                    .icon(createCenterMarker())
                    .anchor(0.5f, 0.5f));

            // 绘制搜索范围圈
            aMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radius)
                    .strokeColor(0xFF2196F3)
                    .strokeWidth(5)
                    .fillColor(0x202196F3));

            // 移动相机到中心位置
            float zoomLevel = getZoomLevel(radius);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, zoomLevel));

        } catch (Exception e) {
            Log.e(TAG, "Error showing search area: " + e.getMessage(), e);
        }
    }

    /**
     * 分批加载POI标记，每次加载 BATCH_SIZE 个
     */
    private void loadNextBatch() {
        if (aMap == null || poiList == null || poiList.isEmpty()) return;

        int total = poiList.size();
        int startIndex = displayedCount;
        int endIndex = Math.min(startIndex + BATCH_SIZE, total);

        Log.d(TAG, "Loading batch: " + startIndex + " - " + endIndex + " / " + total);

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        boolean hasValidPoint = false;
        int addedCount = 0;

        for (int i = startIndex; i < endIndex; i++) {
            POI poi = poiList.get(i);
            if (poi == null) continue;

            double lat = poi.getLatitude();
            double lng = poi.getLongitude();

            if (lat == 0 && lng == 0) continue;
            if (lat < -90 || lat > 90 || lng < -180 || lng > 180) continue;

            try {
                LatLng position = new LatLng(lat, lng);

                // 第一批时构建视野范围
                if (displayedCount == 0) {
                    boundsBuilder.include(position);
                    hasValidPoint = true;
                }

                // 确保距离正确：如果distance为0或异常，重新计算
                double poiDistance = poi.getDistance();
                if (poiDistance <= 0 && centerLat != 0 && centerLng != 0) {
                    poiDistance = calculateDistance(centerLat, centerLng, lat, lng);
                }

                MarkerOptions markerOptions = new MarkerOptions()
                        .position(position)
                        .title(poi.getName() != null ? poi.getName() : "未知地点")
                        .snippet((poi.getAddress() != null ? poi.getAddress() : "") +
                                "\n距离: " + formatDistance(poiDistance));

                int markerColor = getColorByDistance(poiDistance);
                markerOptions.icon(createCircleMarker(markerColor));
                markerOptions.anchor(0.5f, 0.5f);

                aMap.addMarker(markerOptions);
                addedCount++;
            } catch (Exception e) {
                Log.e(TAG, "Error adding marker for POI " + i + ": " + e.getMessage());
            }
        }

        displayedCount = endIndex;
        Log.d(TAG, "Added " + addedCount + " markers, total displayed: " + displayedCount);

        // 第一批加载后调整视野
        if (startIndex == 0 && hasValidPoint) {
            try {
                LatLngBounds bounds = boundsBuilder.build();
                aMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
            } catch (Exception e) {
                Log.e(TAG, "Error adjusting camera: " + e.getMessage());
                LatLng center = new LatLng(centerLat, centerLng);
                aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center, getZoomLevel(radius)));
            }
        }

        // 更新分批加载提示栏
        updateBatchBar(total);
    }

    /**
     * 更新分批加载提示栏
     */
    private void updateBatchBar(int total) {
        if (batchLoadBar == null || tvBatchInfo == null || btnLoadMore == null) return;

        if (displayedCount >= total) {
            // 全部加载完毕
            if (total > BATCH_SIZE) {
                batchLoadBar.setVisibility(View.VISIBLE);
                tvBatchInfo.setText("已显示全部 " + total + " 个POI");
                btnLoadMore.setVisibility(View.GONE);
            } else {
                batchLoadBar.setVisibility(View.GONE);
            }
        } else {
            // 还有更多
            batchLoadBar.setVisibility(View.VISIBLE);
            tvBatchInfo.setText("显示 1-" + displayedCount + " / 共 " + total + " 个");
            btnLoadMore.setVisibility(View.VISIBLE);
            btnLoadMore.setText("加载更多 (" + (total - displayedCount) + ")");
        }
    }

    /**
     * 根据距离获取对应颜色值（与图例一致）
     */
    private int getColorByDistance(double distance) {
        if (distance < 500) {
            return COLOR_GREEN;
        } else if (distance < 1500) {
            return COLOR_YELLOW;
        } else {
            return COLOR_RED;
        }
    }

    /**
     * 创建圆形自定义标记图标，颜色与图例完全一致
     */
    private BitmapDescriptor createCircleMarker(int color) {
        int size = 56; // 直径（像素）
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float cx = size / 2f;
        float cy = size / 2f;
        float radius = size / 2f - 4;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 白色外边框
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, radius, paint);

        // 内部彩色圆
        paint.setColor(color);
        canvas.drawCircle(cx, cy, radius - 3, paint);

        // 中心小白点
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, radius * 0.3f, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    /**
     * 创建搜索中心标记图标（蓝色，带十字准星）
     */
    private BitmapDescriptor createCenterMarker() {
        int size = 64;
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float cx = size / 2f;
        float cy = size / 2f;
        float radius = size / 2f - 6;

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // 白色外边框
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(cx, cy, radius, paint);

        // 蓝色内圆
        paint.setColor(COLOR_CENTER);
        canvas.drawCircle(cx, cy, radius - 3, paint);

        // 十字准星
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(3);
        paint.setStyle(Paint.Style.STROKE);
        canvas.drawLine(cx - radius * 0.5f, cy, cx + radius * 0.5f, cy, paint);
        canvas.drawLine(cx, cy - radius * 0.5f, cx, cy + radius * 0.5f, paint);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private String formatDistance(double distance) {
        if (distance < 1000) {
            return String.format("%.0f米", distance);
        } else {
            return String.format("%.1f公里", distance / 1000);
        }
    }

    /**
     * Haversine公式计算两点间距离（米）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private float getZoomLevel(int radius) {
        if (radius <= 500) return 16;
        if (radius <= 1000) return 15;
        if (radius <= 2000) return 14;
        if (radius <= 5000) return 13;
        if (radius <= 10000) return 12;
        return 11;
    }

    private void moveToCenter() {
        LatLng center = new LatLng(centerLat, centerLng);
        aMap.animateCamera(CameraUpdateFactory.newLatLngZoom(center, getZoomLevel(radius)));
    }

    private boolean isSatellite = false;
    private void toggleMapType() {
        isSatellite = !isSatellite;
        if (isSatellite) {
            aMap.setMapType(AMap.MAP_TYPE_SATELLITE);
            Toast.makeText(this, "卫星地图", Toast.LENGTH_SHORT).show();
        } else {
            aMap.setMapType(AMap.MAP_TYPE_NORMAL);
            Toast.makeText(this, "标准地图", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }
}
