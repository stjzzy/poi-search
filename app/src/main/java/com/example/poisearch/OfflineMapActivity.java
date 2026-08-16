package com.example.poisearch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.example.poisearch.adapter.SimpleCityAdapter;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OfflineMapActivity extends AppCompatActivity implements OfflineMapManager.OfflineMapDownloadListener {

    private static final String TAG = "OfflineMapActivity";

    private TextView tvStatus;
    private TextView tvProgress;
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private SimpleCityAdapter cityAdapter;
    private List<String> cityList = new ArrayList<>();
    private Set<String> downloadedCities = new HashSet<>();
    private Set<String> downloadingCities = new HashSet<>();

    private OfflineMapManager offlineMapManager;

    // 离线地图下载状态常量
    private static final int STATUS_READY = 0;
    private static final int STATUS_WAITING = 1;
    private static final int STATUS_LOADING = 2;
    private static final int STATUS_UNZIP = 3;
    private static final int STATUS_SUCCESS = 4;
    private static final int STATUS_STOP = 5;
    private static final int STATUS_PAUSE = 6;
    private static final int STATUS_ERROR = 7;

    private static final String[] POPULAR_CITIES = {
        "北京", "上海", "广州", "深圳", "杭州", "南京", "成都", "武汉",
        "重庆", "西安", "天津", "苏州", "长沙", "郑州", "青岛", "沈阳",
        "大连", "厦门", "福州", "济南", "哈尔滨", "昆明", "南宁", "贵阳"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offline_map);

        initViews();
        initOfflineManager();
        loadCityList();
    }

    private void initViews() {
        MaterialButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        tvStatus = findViewById(R.id.tv_offline_status);
        tvProgress = findViewById(R.id.tv_offline_progress);
        progressBar = findViewById(R.id.offline_progress);
        recyclerView = findViewById(R.id.offline_cities_recycler);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        cityAdapter = new SimpleCityAdapter(cityList, new ArrayList<>(downloadedCities));
        cityAdapter.setOnCityClickListener(cityName -> downloadCity(cityName));
        recyclerView.setAdapter(cityAdapter);

        MaterialButton btnRefresh = findViewById(R.id.btn_refresh);
        btnRefresh.setOnClickListener(v -> {
            loadDownloadedCities();
            loadCityList();
        });
    }

    private void initOfflineManager() {
        try {
            offlineMapManager = new OfflineMapManager(this, this);
            Log.d(TAG, "OfflineMapManager initialized");
            loadDownloadedCities();
        } catch (Exception e) {
            Log.e(TAG, "Failed to init OfflineMapManager: " + e.getMessage(), e);
            tvStatus.setText("离线地图功能初始化失败: " + e.getMessage());
        }
    }

    private void loadDownloadedCities() {
        if (offlineMapManager == null) return;
        try {
            downloadedCities.clear();
            List<OfflineMapCity> downloadedList = offlineMapManager.getDownloadOfflineMapCityList();
            if (downloadedList != null) {
                for (OfflineMapCity city : downloadedList) {
                    downloadedCities.add(city.getCity());
                    Log.d(TAG, "Downloaded: " + city.getCity());
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to get downloaded cities: " + e.getMessage());
        }
    }

    private void loadCityList() {
        cityList.clear();
        for (String city : POPULAR_CITIES) {
            cityList.add(city);
        }
        cityAdapter = new SimpleCityAdapter(cityList, new ArrayList<>(downloadedCities));
        cityAdapter.setOnCityClickListener(cityName -> downloadCity(cityName));
        recyclerView.setAdapter(cityAdapter);

        int downloaded = downloadedCities.size();
        tvStatus.setText(downloaded > 0
                ? "已下载 " + downloaded + " 个城市离线地图，点击下载更多"
                : "点击城市下载离线地图包（在APP内直接下载）");
    }

    /**
     * 通过高德SDK的OfflineMapManager下载离线地图
     */
    private void downloadCity(String cityName) {
        if (offlineMapManager == null) {
            Toast.makeText(this, "离线地图功能未初始化", Toast.LENGTH_SHORT).show();
            return;
        }

        if (downloadedCities.contains(cityName)) {
            Toast.makeText(this, cityName + "离线地图已下载", Toast.LENGTH_SHORT).show();
            return;
        }

        if (downloadingCities.contains(cityName)) {
            Toast.makeText(this, cityName + "正在下载中...", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            downloadingCities.add(cityName);
            progressBar.setVisibility(View.VISIBLE);
            tvProgress.setVisibility(View.VISIBLE);
            tvProgress.setText("正在下载 " + cityName + " 离线地图...");
            tvStatus.setText("正在下载: " + cityName);

            offlineMapManager.downloadByCityName(cityName);
            Log.d(TAG, "Started download for: " + cityName);
        } catch (Exception e) {
            Log.e(TAG, "Download failed for " + cityName + ": " + e.getMessage(), e);
            downloadingCities.remove(cityName);
            progressBar.setVisibility(View.GONE);
            tvProgress.setVisibility(View.GONE);
            Toast.makeText(this, "下载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    // ===== OfflineMapManager.OfflineMapDownloadListener =====

    @Override
    public void onDownload(int status, int completeCode, String downName) {
        Log.d(TAG, "onDownload: status=" + status + ", completeCode=" + completeCode + ", name=" + downName);

        runOnUiThread(() -> {
            if (status == STATUS_SUCCESS) {
                // 下载成功
                downloadingCities.remove(downName);
                downloadedCities.add(downName);
                progressBar.setVisibility(View.GONE);
                tvProgress.setVisibility(View.GONE);
                tvStatus.setText(downName + " 离线地图下载完成！");
                Toast.makeText(this, downName + " 离线地图下载完成", Toast.LENGTH_SHORT).show();

                // 刷新列表
                cityAdapter = new SimpleCityAdapter(cityList, new ArrayList<>(downloadedCities));
                cityAdapter.setOnCityClickListener(cityName -> downloadCity(cityName));
                recyclerView.setAdapter(cityAdapter);
            } else if (status == STATUS_LOADING) {
                // 下载中
                tvProgress.setText("正在下载 " + downName + " ... " + completeCode + "%");
                progressBar.setProgress(completeCode);
            } else if (status == STATUS_UNZIP) {
                tvProgress.setText("正在解压 " + downName + " ...");
            } else if (status == STATUS_READY) {
                tvProgress.setText(downName + " 准备下载...");
            } else if (status == STATUS_WAITING) {
                tvProgress.setText(downName + " 等待下载...");
            } else if (status == STATUS_PAUSE) {
                tvProgress.setText(downName + " 下载已暂停");
            } else if (status == STATUS_STOP) {
                tvProgress.setText(downName + " 下载已停止");
            } else if (status == STATUS_ERROR) {
                downloadingCities.remove(downName);
                progressBar.setVisibility(View.GONE);
                tvProgress.setVisibility(View.GONE);
                tvStatus.setText(downName + " 下载失败");
                Toast.makeText(this, downName + " 下载失败，请检查网络", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onCheckUpdate(boolean hasNew, String name) {
        Log.d(TAG, "onCheckUpdate: hasNew=" + hasNew + ", name=" + name);
    }

    @Override
    public void onRemove(boolean success, String name, String removeName) {
        Log.d(TAG, "onRemove: success=" + success + ", name=" + name + ", removeName=" + removeName);
        if (success) {
            runOnUiThread(() -> {
                downloadedCities.remove(removeName);
                cityAdapter = new SimpleCityAdapter(cityList, new ArrayList<>(downloadedCities));
                cityAdapter.setOnCityClickListener(cityName -> downloadCity(cityName));
                recyclerView.setAdapter(cityAdapter);
                Toast.makeText(this, removeName + " 离线地图已删除", Toast.LENGTH_SHORT).show();
            });
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (offlineMapManager != null) {
            try {
                offlineMapManager = null;
            } catch (Exception e) {
                Log.e(TAG, "Error releasing OfflineMapManager: " + e.getMessage());
            }
        }
    }
}
