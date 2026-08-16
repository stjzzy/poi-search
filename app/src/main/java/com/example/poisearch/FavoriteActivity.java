package com.example.poisearch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poisearch.adapter.POIAdapter;
import com.example.poisearch.database.FavoriteManager;
import com.example.poisearch.model.POI;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class FavoriteActivity extends AppCompatActivity implements POIAdapter.OnPOIClickListener {

    private FavoriteManager favoriteManager;
    private POIAdapter poiAdapter;
    private List<POI> favoriteList = new ArrayList<>();
    private TextView emptyView;
    private TextView tvCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        favoriteManager = new FavoriteManager(this);

        initViews();
        loadFavorites();
    }

    private void initViews() {
        MaterialButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        tvCount = findViewById(R.id.tv_favorite_count);
        emptyView = findViewById(R.id.empty_view);

        RecyclerView recyclerView = findViewById(R.id.favorites_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        poiAdapter = new POIAdapter();
        poiAdapter.setOnPOIClickListener(this);
        recyclerView.setAdapter(poiAdapter);

        MaterialButton btnClear = findViewById(R.id.btn_clear_favorites);
        btnClear.setOnClickListener(v -> confirmClearAll());
    }

    private void loadFavorites() {
        favoriteList = favoriteManager.getAllFavorites();
        poiAdapter.setPOIList(favoriteList);
        tvCount.setText("共 " + favoriteList.size() + " 个收藏");

        if (favoriteList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("暂无收藏");
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void confirmClearAll() {
        if (favoriteList.isEmpty()) {
            Toast.makeText(this, "没有收藏可清空", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("清空收藏")
                .setMessage("确定要清空所有收藏吗？此操作不可恢复。")
                .setPositiveButton("清空", (dialog, which) -> {
                    favoriteManager.clearAllFavorites();
                    loadFavorites();
                    Toast.makeText(this, "已清空所有收藏", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onPOIClick(POI poi) {
        Intent intent = new Intent(this, POIDetailActivity.class);
        intent.putExtra(POIDetailActivity.EXTRA_POI, poi);
        startActivity(intent);
    }

    @Override
    public void onPhoneClick(String phone) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(android.net.Uri.parse("tel:" + phone));
        startActivity(intent);
    }

    @Override
    public void onNavigateClick(POI poi) {
        Intent intent = new Intent(this, RouteActivity.class);
        intent.putExtra("target_lat", poi.getLatitude());
        intent.putExtra("target_lng", poi.getLongitude());
        intent.putExtra("target_name", poi.getName());
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavorites();
    }
}
