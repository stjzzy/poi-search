package com.example.poisearch;



import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.location.AMapLocationClient;
import com.example.poisearch.adapter.POIAdapter;
import com.example.poisearch.database.HistoryManager;
import com.example.poisearch.dialog.CategoryDetailDialog;
import com.example.poisearch.dialog.CategoryDialog;
import com.example.poisearch.dialog.ExportSettingsDialog;
import com.example.poisearch.dialog.LocationDialog;
import com.example.poisearch.dialog.RadiusDialog;
import com.example.poisearch.model.POI;
import com.example.poisearch.model.POICategory;
import com.example.poisearch.service.POISearchService;
import com.example.poisearch.service.GridSearchService;
import com.example.poisearch.utils.ExcelExporter;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 1002;
    private static final int DEFAULT_SEARCH_RADIUS = 5000; // 默认5公里

    private TextInputEditText searchInput;
    private MaterialButton searchButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView emptyView;
    private TextView selectedCategoryText;
    private MaterialButton categoryButton;
    private TextView radiusText;
    private MaterialButton radiusButton;
    private TextView locationText;
    private MaterialButton locationButton;
    private MaterialButton mapButton;
    private MaterialButton exportButton;
    private MaterialButton favoriteButton;
    private MaterialButton historyButton;
    private MaterialButton offlineButton;
    private TextView tvLocationInfo; // 用于显示经纬度的文本框
    private com.amap.api.location.AMapLocationClient mLocationClient; // 高德定位客户端

    private POIAdapter poiAdapter;
    private POISearchService poiSearchService;
    private GridSearchService gridSearchService;
    private HistoryManager historyManager;

    private double currentLatitude = 39.9042; // 默认北京
    private double currentLongitude = 116.4074;
    private boolean hasLocation = false;
    private String currentCategoryCode = ""; // 当前选中的分类代码
    private int currentRadius = DEFAULT_SEARCH_RADIUS; // 当前搜索范围
    private String currentLocationName = "当前位置"; // 当前位置名称
    private List<POI> currentPOIList = new ArrayList<>(); // 当前POI列表
    private List<POI> allPOIList = new ArrayList<>(); // 所有分页加载的POI列表（用于导出）
    private int totalPOICount = 0; // POI总数
    private int currentPage = 0; // 当前加载页数
    private int totalPages = 0; // 总页数
    private boolean isLoadingAllPages = false; // 是否正在加载所有分页
    private boolean isGridSearchMode = false; // 是否使用网格搜索模式

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 必须在 AMapLocationClient 实例化之前调用
        AMapLocationClient.updatePrivacyShow(this, true, true);
        AMapLocationClient.updatePrivacyAgree(this, true);
        initViews();
        initServices();
        setupRecyclerView();
        setupListeners();

        // 检查位置权限
        checkLocationPermission();
    }

    private void initViews() {
        searchInput = findViewById(R.id.searchInput);
        searchButton = findViewById(R.id.searchButton);
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyView = findViewById(R.id.emptyView);
        selectedCategoryText = findViewById(R.id.selectedCategoryText);
        categoryButton = findViewById(R.id.categoryButton);
        radiusText = findViewById(R.id.radiusText);
        radiusButton = findViewById(R.id.radiusButton);
        locationText = findViewById(R.id.locationText);
        locationButton = findViewById(R.id.locationButton);
        mapButton = findViewById(R.id.mapButton);
        exportButton = findViewById(R.id.exportButton);
        favoriteButton = findViewById(R.id.favoriteButton);
        historyButton = findViewById(R.id.historyButton);
        offlineButton = findViewById(R.id.offlineButton);
        tvLocationInfo = findViewById(R.id.tv_location_info);
// 注意：确保你的 activity_main.xml 布局里有一个 id 为 tv_location_info 的 TextView
        
        // 初始化搜索模式选择
        ChipGroup searchModeChipGroup = findViewById(R.id.searchModeChipGroup);
        if (searchModeChipGroup != null) {
            searchModeChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
                isGridSearchMode = (checkedId == R.id.chipGridMode);
                if (isGridSearchMode) {
                    Toast.makeText(this, "网格搜索模式：将搜索全部POI数据", Toast.LENGTH_SHORT).show();
                }
            });
        }
        
        // 初始化分页信息视图
        TextView pageInfoText = findViewById(R.id.pageInfoText);
        if (pageInfoText != null) {
            pageInfoText.setText("");
        }

        // 同步范围文本显示
        updateRadiusText();
    }
    // 定义定位回调监听器
    private com.amap.api.location.AMapLocationListener mLocationListener = new com.amap.api.location.AMapLocationListener() {
        @Override
        public void onLocationChanged(com.amap.api.location.AMapLocation amapLocation) {
            if (amapLocation != null) {
                if (amapLocation.getErrorCode() == 0) {
                    // 获取经纬度并保存到全局变量
                    currentLatitude = amapLocation.getLatitude();
                    currentLongitude = amapLocation.getLongitude();
                    hasLocation = true;

                    // 保存到POIDataHolder供路线规划使用
                    POIDataHolder.getInstance().setStartLat(currentLatitude);
                    POIDataHolder.getInstance().setStartLng(currentLongitude);

                    // 更新界面上的经纬度显示
                    if (tvLocationInfo != null) {
                        String info = String.format("当前经度：%.6f\n当前纬度：%.6f\n地址：%s",
                                currentLongitude, currentLatitude, amapLocation.getAddress());
                        tvLocationInfo.setText(info);
                    }

                    Log.d(TAG, "高德定位成功: " + currentLatitude + "," + currentLongitude);
                } else {
                    // 定位失败
                    if (tvLocationInfo != null) {
                        tvLocationInfo.setText("定位失败");
                    }
                    Log.e(TAG, "定位失败, 错误码:" + amapLocation.getErrorCode()
                            + " 信息:" + amapLocation.getErrorInfo());
                }
            }
        }
    };


    private void initServices() {
        poiSearchService = new POISearchService(this);
        gridSearchService = new GridSearchService(this);
        historyManager = new HistoryManager(this);

        // 初始化高德定位
        try {
            mLocationClient = new com.amap.api.location.AMapLocationClient(getApplicationContext());
            mLocationClient.setLocationListener(mLocationListener);

            com.amap.api.location.AMapLocationClientOption option = new com.amap.api.location.AMapLocationClientOption();
            option.setLocationMode(com.amap.api.location.AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
            option.setInterval(2000); // 2秒刷新一次
            option.setOnceLocation(false); // 连续定位

            mLocationClient.setLocationOption(option);
            mLocationClient.startLocation(); // 启动定位
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupRecyclerView() {
        poiAdapter = new POIAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(poiAdapter);

        poiAdapter.setOnPOIClickListener(new POIAdapter.OnPOIClickListener() {
            @Override
            public void onPOIClick(POI poi) {
                openPOIDetail(poi);
            }

            @Override
            public void onPhoneClick(String phone) {
                dialPhone(phone);
            }

            @Override
            public void onNavigateClick(POI poi) {
                navigateToPOI(poi);
            }
        });
    }

    private void setupListeners() {
        // 搜索按钮点击
        searchButton.setOnClickListener(v -> performSearch());

        // 搜索框回车键
        searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        // 分类选择按钮
        categoryButton.setOnClickListener(v -> showCategoryDialog());

        // 范围选择按钮
        radiusButton.setOnClickListener(v -> showRadiusDialog());

        // 位置选择按钮
        locationButton.setOnClickListener(v -> showLocationDialog());

        // 地图按钮
        mapButton.setOnClickListener(v -> openMap());

        // 导出按钮
        exportButton.setOnClickListener(v -> exportToExcel());

        // 收藏按钮
        favoriteButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, FavoriteActivity.class);
            startActivity(intent);
        });

        // 历史按钮
        historyButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivityForResult(intent, 2001);
        });

        // 离线地图按钮
        offlineButton.setOnClickListener(v -> {
            Intent intent = new Intent(this, OfflineMapActivity.class);
            startActivity(intent);
        });
    }

    private void showCategoryDialog() {
        CategoryDialog dialog = new CategoryDialog(this);
        dialog.setOnCategorySelectedListener(category -> {
            // 如果选中的分类有子分类，显示子分类对话框
            if (!category.getSubCategories().isEmpty()) {
                showCategoryDetailDialog(category);
            } else {
                // 直接选中该分类
                selectCategory(category);
            }
        });
        dialog.show();
    }

    private void showCategoryDetailDialog(POICategory parentCategory) {
        CategoryDetailDialog dialog = new CategoryDetailDialog(this, parentCategory);
        dialog.setOnSubCategorySelectedListener(this::selectCategory);
        dialog.show();
    }

    private void selectCategory(POICategory category) {
        currentCategoryCode = category.getCode();
        selectedCategoryText.setText(category.getName());
        performSearch();
    }

    private void showRadiusDialog() {
        RadiusDialog dialog = new RadiusDialog(this, currentRadius);
        dialog.setOnRadiusSelectedListener(radius -> {
            currentRadius = radius;
            updateRadiusText();
            performSearch();
        });
        dialog.show();
    }

    private void updateRadiusText() {
        if (currentRadius < 1000) {
            radiusText.setText(currentRadius + "米");
        } else {
            radiusText.setText(String.format("%.1f公里", currentRadius / 1000f));
        }
    }

    private void showLocationDialog() {
        LocationDialog dialog = new LocationDialog(this, currentLatitude, currentLongitude);
        dialog.setOnLocationSelectedListener(new LocationDialog.OnLocationSelectedListener() {
            @Override
            public void onLocationSelected(double lat, double lng, String address) {
                currentLatitude = lat;
                currentLongitude = lng;
                currentLocationName = address.isEmpty() ? String.format("%.4f, %.4f", lat, lng) : address;
                locationText.setText(currentLocationName);
                hasLocation = true;
                performSearch();
            }

            @Override
            public void onUseCurrentLocation() {
                // 高德定位已在后台连续运行，无需手动获取
                Toast.makeText(MainActivity.this, "正在获取当前位置...", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.show();
    }


    private void openPOIDetail(POI poi) {
        Intent intent = new Intent(this, POIDetailActivity.class);
        intent.putExtra(POIDetailActivity.EXTRA_POI, poi);
        startActivity(intent);
    }

    private void exportToExcel() {
        // 优先使用allPOIList（包含所有分页数据），如果为空则使用currentPOIList
        final List<POI> exportList = allPOIList.isEmpty() ? currentPOIList : allPOIList;
        
        if (exportList.isEmpty()) {
            Toast.makeText(this, "没有数据可导出", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查存储权限 (Android 9及以下需要)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        STORAGE_PERMISSION_REQUEST_CODE);
                return;
            }
        }

        // 显示导出确认对话框
        showExportConfirmDialog(exportList);
    }
    
    private void showExportConfirmDialog(final List<POI> exportList) {
        String message = String.format("确定要导出 %d 个POI数据到Excel文件吗？", exportList.size());
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("导出确认")
                .setMessage(message)
                .setPositiveButton("导出", (dialog, which) -> {
                    // 显示导出设置对话框
                    showExportSettingsDialog(exportList);
                })
                .setNegativeButton("取消", null)
                .show();
    }
    
    private void showExportSettingsDialog(final List<POI> exportList) {
        ExportSettingsDialog dialog = new ExportSettingsDialog();
        dialog.setCallback(customPath -> {
            // 执行导出
            performExport(exportList, customPath);
        });
        dialog.show(getSupportFragmentManager(), "export_settings");
    }
    
    private void performExport(List<POI> exportList, String customPath) {
        showLoading(true);
        ExcelExporter.exportToExcel(this, exportList, customPath, new ExcelExporter.ExportCallback() {
            @Override
            public void onSuccess(String filePath) {
                runOnUiThread(() -> {
                    showLoading(false);
                    // 显示用户友好的路径
                    String displayPath = ExcelExporter.getDisplayPath(filePath);
                    Toast.makeText(MainActivity.this, "导出成功:\n" + displayPath, Toast.LENGTH_LONG).show();
                    // 询问是否分享或打开位置
                    showShareDialog(filePath);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }



    private void showShareDialog(String filePath) {
        String displayPath = ExcelExporter.getDisplayPath(filePath);
        
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("导出成功")
                .setMessage("文件位置: " + displayPath)
                .setPositiveButton("分享", (dialog, which) -> {
                    ExcelExporter.shareExcelFile(this, filePath);
                })
                .setNeutralButton("打开位置", (dialog, which) -> {
                    ExcelExporter.openFileLocation(this, filePath);
                })
                .setNegativeButton("关闭", null)
                .show();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // 高德定位已在initServices()中启动，无需手动获取位置
            Log.d(TAG, "定位权限已获取，高德定位正在运行");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限已授予，高德定位已在initServices()中启动
                Toast.makeText(this, "定位权限已获取，正在定位...", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.permission_denied, Toast.LENGTH_LONG).show();
                // 使用默认位置搜索
                performSearch();
            }
        } else if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                exportToExcel();
            } else {
                Toast.makeText(this, "需要存储权限才能导出文件", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void performSearch() {
        String keywords = searchInput.getText() != null ? searchInput.getText().toString().trim() : "";
        String category = getSelectedCategory();

        // 重置状态
        allPOIList.clear();
        currentPage = 0;
        totalPages = 0;
        totalPOICount = 0;
        isLoadingAllPages = true;

        // 使用final变量存储搜索参数
        final String searchKeywords = keywords;
        final String searchCategory = category;

        // 根据搜索模式选择不同的搜索方式
        if (isGridSearchMode) {
            // 网格搜索模式 - 突破API限制获取全部数据
            performGridSearch(searchKeywords, searchCategory);
        } else {
            // 普通搜索模式 - 快速搜索，最多200-1000条
            performNormalSearch(searchKeywords, searchCategory);
        }
    }

    /**
     * 普通搜索模式
     */
    private void performNormalSearch(String keywords, String category) {
        updatePageInfo("正在加载第1页...");

        poiSearchService.searchNearbyAll(currentLatitude, currentLongitude, keywords, category,
                currentRadius, new POISearchService.POIPageCallback() {
                    @Override
                    public void onPageLoaded(List<POI> pois, int page, int totalPagesResult, int totalCount) {
                        runOnUiThread(() -> {
                            MainActivity.this.currentPage = page;
                            int displayTotalPages = totalPagesResult > MAX_PAGES ? MAX_PAGES : totalPagesResult;
                            MainActivity.this.totalPages = displayTotalPages;
                            MainActivity.this.totalPOICount = totalCount;

                            allPOIList.addAll(pois);

                            String progressText = String.format("正在加载: 第%d/%d页 (共%d个POI)",
                                    page, displayTotalPages, totalCount);
                            updatePageInfo(progressText);

                            currentPOIList = new ArrayList<>(allPOIList);
                            poiAdapter.setPOIList(currentPOIList);
                            showEmptyView(false);
                        });
                    }

                    @Override
                    public void onAllPagesLoaded(List<POI> allPois, int totalCount) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            isLoadingAllPages = false;

                            historyManager.addHistory(keywords, category, currentRadius,
                                    currentLatitude, currentLongitude, allPois.size());

                            currentPOIList = allPois;
                            allPOIList = new ArrayList<>(allPois);

                            if (allPois.isEmpty()) {
                                showEmptyView(true);
                                updatePageInfo("");
                            } else {
                                showEmptyView(false);
                                poiAdapter.setPOIList(allPois);
                                String infoText = String.format("✅ 搜索完成！共找到 %d 个POI", allPois.size());
                                updatePageInfo(infoText);
                            }

                            Toast.makeText(MainActivity.this,
                                    "搜索完成，共找到 " + allPois.size() + " 个POI",
                                    Toast.LENGTH_SHORT).show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            isLoadingAllPages = false;
                            // 显示详细错误信息
                            String errorMsg = "搜索错误: " + error;
                            Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                            Log.e(TAG, "Search error: " + error);
                            
                            if (allPOIList.isEmpty()) {
                                showEmptyView(true);
                                updatePageInfo("");
                            } else {
                                currentPOIList = allPOIList;
                                poiAdapter.setPOIList(allPOIList);
                                String infoText = String.format("部分加载: %d 个POI (加载中断)",
                                        allPOIList.size());
                                updatePageInfo(infoText);
                            }
                        });
                    }
                });
    }

    /**
     * 网格搜索模式 - 突破API限制
     */
    private void performGridSearch(String keywords, String category) {
        updatePageInfo("正在启动网格搜索...");

        // 显示提示对话框
        new MaterialAlertDialogBuilder(this)
                .setTitle("网格搜索模式")
                .setMessage("此模式会将搜索区域分割成多个小网格分别搜索，可以获取全部POI数据，但耗时较长。\n\n提示：\n• 关键词和分类二选一使用，避免结果过少\n• 搜索范围越大，网格数量越多\n• 请耐心等待搜索完成")
                .setPositiveButton("开始搜索", (dialog, which) -> {
                    startGridSearch(keywords, category);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    showLoading(false);
                    updatePageInfo("");
                })
                .show();
    }

    private void startGridSearch(String keywords, String category) {
        gridSearchService.searchByGrid(currentLatitude, currentLongitude, currentRadius,
                keywords, category, new GridSearchService.GridSearchCallback() {
                    @Override
                    public void onGridLoaded(int completedGrids, int totalGrids, int currentGridPoiCount, int totalPoiCount) {
                        runOnUiThread(() -> {
                            String progressText = String.format("网格搜索: %d/%d 区域 | 本区域%d个 | 总计%d个",
                                    completedGrids, totalGrids, currentGridPoiCount, totalPoiCount);
                            updatePageInfo(progressText);

                            // 实时更新列表显示
                            allPOIList.clear();
                            // gridSearchService内部维护列表，这里需要从回调获取
                            // 实际更新在onAllGridsLoaded中处理
                        });
                    }

                    @Override
                    public void onAllGridsLoaded(List<POI> allPois, int totalCount) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            isLoadingAllPages = false;

                            historyManager.addHistory(keywords, category, currentRadius,
                                    currentLatitude, currentLongitude, allPois.size());

                            currentPOIList = allPois;
                            allPOIList = new ArrayList<>(allPois);

                            if (allPois.isEmpty()) {
                                showEmptyView(true);
                                updatePageInfo("");
                            } else {
                                showEmptyView(false);
                                poiAdapter.setPOIList(allPois);
                                String infoText = String.format("✅ 网格搜索完成！共找到 %d 个POI", allPois.size());
                                updatePageInfo(infoText);
                            }

                            Toast.makeText(MainActivity.this,
                                    "网格搜索完成！共找到 " + allPois.size() + " 个POI",
                                    Toast.LENGTH_LONG).show();
                        });
                    }

                    @Override
                    public void onError(String error) {
                        runOnUiThread(() -> {
                            showLoading(false);
                            isLoadingAllPages = false;
                            Toast.makeText(MainActivity.this, "搜索错误: " + error, Toast.LENGTH_LONG).show();

                            if (allPOIList.isEmpty()) {
                                showEmptyView(true);
                                updatePageInfo("");
                            }
                        });
                    }
                });
    }
    
    private static final int MAX_PAGES = 40; // 最大页数限制
    
    private void updatePageInfo(String text) {
        TextView pageInfoText = findViewById(R.id.pageInfoText);
        if (pageInfoText != null) {
            pageInfoText.setText(text);
            pageInfoText.setVisibility(text.isEmpty() ? View.GONE : View.VISIBLE);
        }
    }

    private String getSelectedCategory() {
        // 返回当前选中的分类代码
        return currentCategoryCode;
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            recyclerView.setVisibility(View.GONE);
            emptyView.setVisibility(View.GONE);
        }
    }

    private void showEmptyView(boolean show) {
        emptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showPOIDetails(POI poi) {
        // 可以在这里打开详情页面
        Toast.makeText(this, poi.getName(), Toast.LENGTH_SHORT).show();
    }

    private void dialPhone(String phone) {
        if (phone == null || phone.isEmpty()) {
            Toast.makeText(this, "电话号码为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 优先直接拨号，无权限则退回到拨号界面
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + phone));
            try {
                startActivity(intent);
                return;
            } catch (Exception e) {
                Log.e(TAG, "Direct call failed", e);
            }
        }

        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + phone));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void navigateToPOI(POI poi) {
        Intent intent = new Intent(this, RouteActivity.class);
        intent.putExtra("target_lat", poi.getLatitude());
        intent.putExtra("target_lng", poi.getLongitude());
        intent.putExtra("target_name", poi.getName());
        startActivity(intent);
    }

    private void openMap() {
        if (currentPOIList == null || currentPOIList.isEmpty()) {
            Toast.makeText(this, "请先搜索POI数据", Toast.LENGTH_SHORT).show();
            return;
        }

        // 使用静态数据持有者传递POI列表，避免Intent传递大数据导致崩溃
        POIDataHolder.setData(currentPOIList, currentLatitude, currentLongitude, currentRadius);
        
        Intent intent = new Intent(this, AMapActivity.class);
        intent.putExtra(AMapActivity.EXTRA_CENTER_LAT, currentLatitude);
        intent.putExtra(AMapActivity.EXTRA_CENTER_LNG, currentLongitude);
        intent.putExtra(AMapActivity.EXTRA_RADIUS, currentRadius);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLocationClient != null) {
            mLocationClient.onDestroy();
        }
    }
}
