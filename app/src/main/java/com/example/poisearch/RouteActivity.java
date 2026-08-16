package com.example.poisearch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.poisearch.adapter.RouteStepAdapter;
import com.example.poisearch.model.RouteStep;
import com.google.android.material.button.MaterialButton;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RouteActivity extends AppCompatActivity {

    private static final String TAG = "RouteActivity";
    private static final String AMAP_KEY = "d70ddce2dd11293a318d3a3fcde70612";
    private static final String ROUTE_URL = "https://restapi.amap.com/v3/direction";

    private MapView mapView;
    private AMap aMap;
    private RequestQueue requestQueue;

    private double startLat, startLng;
    private double targetLat, targetLng;
    private String targetName;

    private String currentMode = "driving"; // driving, walking, riding

    private TextView tvRouteInfo;
    private TextView tvDistance;
    private TextView tvDuration;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private RouteStepAdapter stepAdapter;
    private List<RouteStep> stepList = new ArrayList<>();

    private MaterialButton btnDriving;
    private MaterialButton btnWalking;
    private MaterialButton btnRiding;
    private Button btnOpenAmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_route);

        // 获取数据
        targetLat = getIntent().getDoubleExtra("target_lat", 0);
        targetLng = getIntent().getDoubleExtra("target_lng", 0);
        targetName = getIntent().getStringExtra("target_name");

        // 从全局获取起点（使用POIDataHolder中保存的当前位置）
        startLat = POIDataHolder.getInstance().getStartLat();
        startLng = POIDataHolder.getInstance().getStartLng();

        if (startLat == 0 || startLng == 0) {
            startLat = 39.9042;
            startLng = 116.4074;
        }

        requestQueue = Volley.newRequestQueue(this);

        initViews();
        initMap(savedInstanceState);

        // 默认查询驾车路线
        searchRoute();
    }

    private void initViews() {
        TextView tvTitle = findViewById(R.id.tv_route_title);
        tvTitle.setText("路线规划: " + (targetName != null ? targetName : "目标位置"));

        tvRouteInfo = findViewById(R.id.tv_route_info);
        tvDistance = findViewById(R.id.tv_route_distance);
        tvDuration = findViewById(R.id.tv_route_duration);
        progressBar = findViewById(R.id.route_progress);
        recyclerView = findViewById(R.id.route_steps_recycler);
        btnOpenAmap = findViewById(R.id.btn_open_amap);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        stepAdapter = new RouteStepAdapter(stepList);
        recyclerView.setAdapter(stepAdapter);

        btnDriving = findViewById(R.id.btn_driving);
        btnWalking = findViewById(R.id.btn_walking);
        btnRiding = findViewById(R.id.btn_riding);

        btnDriving.setOnClickListener(v -> {
            currentMode = "driving";
            updateModeButtons();
            searchRoute();
        });

        btnWalking.setOnClickListener(v -> {
            currentMode = "walking";
            updateModeButtons();
            searchRoute();
        });

        btnRiding.setOnClickListener(v -> {
            currentMode = "riding";
            updateModeButtons();
            searchRoute();
        });

        btnOpenAmap.setOnClickListener(v -> openInAmap());

        MaterialButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        updateModeButtons();
    }

    private void updateModeButtons() {
        btnDriving.setChecked("driving".equals(currentMode));
        btnWalking.setChecked("walking".equals(currentMode));
        btnRiding.setChecked("riding".equals(currentMode));
    }

    private void initMap(Bundle savedInstanceState) {
        mapView = findViewById(R.id.route_map_view);
        mapView.onCreate(savedInstanceState);
        aMap = mapView.getMap();

        // 标记起点和终点
        LatLng startLatLng = new LatLng(startLat, startLng);
        LatLng targetLatLng = new LatLng(targetLat, targetLng);

        aMap.addMarker(new MarkerOptions()
                .position(startLatLng)
                .title("起点")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

        aMap.addMarker(new MarkerOptions()
                .position(targetLatLng)
                .title(targetName != null ? targetName : "终点")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // 调整视野包含起点和终点
        try {
            List<LatLng> points = new ArrayList<>();
            points.add(startLatLng);
            points.add(targetLatLng);
            aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng((startLat + targetLat) / 2, (startLng + targetLng) / 2), 12));
        } catch (Exception e) {
            Log.e(TAG, "Map error", e);
        }
    }

    /**
     * 搜索路线
     */
    private void searchRoute() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvRouteInfo.setText("正在规划路线...");
        tvDistance.setText("");
        tvDuration.setText("");

        String origin = startLng + "," + startLat;
        String destination = targetLng + "," + targetLat;

        StringBuilder urlBuilder = new StringBuilder();
        if ("driving".equals(currentMode)) {
            urlBuilder.append(ROUTE_URL).append("/driving");
        } else if ("walking".equals(currentMode)) {
            urlBuilder.append(ROUTE_URL).append("/walking");
        } else {
            urlBuilder.append(ROUTE_URL).append("/riding");
        }

        urlBuilder.append("?key=").append(AMAP_KEY);
        urlBuilder.append("&origin=").append(origin);
        urlBuilder.append("&destination=").append(destination);

        // 驾车需要更多参数
        if ("driving".equals(currentMode)) {
            urlBuilder.append("&strategy=0"); // 速度优先
        }

        String url = urlBuilder.toString();
        Log.d(TAG, "Route URL: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    parseRouteResponse(response);
                },
                error -> {
                    progressBar.setVisibility(View.GONE);
                    tvRouteInfo.setText("路线规划失败");
                    Log.e(TAG, "Route error", error);
                }
        );

        requestQueue.add(request);
    }

    /**
     * 解析路线响应
     */
    private void parseRouteResponse(JSONObject response) {
        try {
            String status = response.optString("status", "0");
            if (!"1".equals(status)) {
                tvRouteInfo.setText("路线规划失败: " + response.optString("info", "未知错误"));
                return;
            }

            JSONObject route = response.optJSONObject("route");
            if (route == null) {
                tvRouteInfo.setText("未找到路线");
                return;
            }

            JSONArray paths = route.optJSONArray("paths");
            if (paths == null || paths.length() == 0) {
                tvRouteInfo.setText("未找到可用路线");
                return;
            }

            // 取第一条路线
            JSONObject path = paths.getJSONObject(0);
            String distanceStr = path.optString("distance", "0");
            String durationStr = path.optString("duration", "0");

            int distance = Integer.parseInt(distanceStr);
            int duration = Integer.parseInt(durationStr);

            // 显示距离和时间
            String distanceText;
            if (distance < 1000) {
                distanceText = distance + "米";
            } else {
                distanceText = String.format("%.1f公里", distance / 1000.0);
            }

            String durationText;
            if (duration < 60) {
                durationText = duration + "秒";
            } else if (duration < 3600) {
                durationText = (duration / 60) + "分钟";
            } else {
                durationText = (duration / 3600) + "小时" + ((duration % 3600) / 60) + "分钟";
            }

            tvDistance.setText("距离: " + distanceText);
            tvDuration.setText("预计: " + durationText);

            String modeText = "driving".equals(currentMode) ? "驾车" :
                    "walking".equals(currentMode) ? "步行" : "骑行";
            tvRouteInfo.setText(modeText + "路线规划成功");

            // 解析步骤
            stepList.clear();
            JSONArray steps = path.optJSONArray("steps");
            if (steps != null) {
                List<LatLng> routePoints = new ArrayList<>();
                for (int i = 0; i < steps.length(); i++) {
                    JSONObject step = steps.getJSONObject(i);
                    String instruction = step.optString("instruction", "");
                    String stepDistance = step.optString("distance", "0");
                    String polyline = step.optString("polyline", "");

                    RouteStep routeStep = new RouteStep();
                    routeStep.setInstruction(instruction);
                    routeStep.setDistance(Integer.parseInt(stepDistance));
                    stepList.add(routeStep);

                    // 解析路线坐标点用于在地图上绘制
                    if (!polyline.isEmpty()) {
                        String[] coords = polyline.split(";");
                        for (String coord : coords) {
                            String[] lngLat = coord.split(",");
                            if (lngLat.length == 2) {
                                double lng = Double.parseDouble(lngLat[0]);
                                double lat = Double.parseDouble(lngLat[1]);
                                routePoints.add(new LatLng(lat, lng));
                            }
                        }
                    }
                }

                // 在地图上绘制路线
                if (!routePoints.isEmpty()) {
                    aMap.addPolyline(new PolylineOptions()
                            .addAll(routePoints)
                            .width(10)
                            .color(0xFF2196F3));
                }
            }

            stepAdapter.notifyDataSetChanged();
            recyclerView.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            Log.e(TAG, "Parse route error", e);
            tvRouteInfo.setText("解析路线数据失败");
        }
    }

    /**
     * 打开高德地图APP导航
     */
    private void openInAmap() {
        try {
            String mode = "driving".equals(currentMode) ? "0" :
                    "walking".equals(currentMode) ? "1" : "3";
            String uri = String.format("amapuri://route/plan/?slat=%f&slon=%f&sname=我的位置&dlat=%f&dlon=%f&dname=%s&dev=0&t=%s",
                    startLat, startLng, targetLat, targetLng,
                    URLEncoder.encode(targetName != null ? targetName : "目标位置", StandardCharsets.UTF_8),
                    mode);

            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setPackage("com.autonavi.minimap");
            startActivity(intent);
        } catch (Exception e) {
            // 如果没有安装高德地图APP，用浏览器打开网页版
            String webUrl = String.format("https://uri.amap.com/navigation?to=%f,%f,%s&mode=%s",
                    targetLat, targetLng,
                    URLEncoder.encode(targetName != null ? targetName : "目标", StandardCharsets.UTF_8),
                    "driving".equals(currentMode) ? "car" : "walking".equals(currentMode) ? "walk" : "ride");
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webUrl));
            startActivity(webIntent);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mapView != null) mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) mapView.onDestroy();
    }
}
