package com.example.poisearch;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.poisearch.database.FavoriteManager;
import com.example.poisearch.model.POI;

public class POIDetailActivity extends AppCompatActivity {

    private static final String TAG = "POIDetailActivity";
    private static final int CALL_PHONE_REQUEST_CODE = 2001;

    public static final String EXTRA_POI = "poi";

    private POI poi;
    private FavoriteManager favoriteManager;
    private Button btnFavorite;
    private boolean isFavorite = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poi_detail);

        favoriteManager = new FavoriteManager(this);

        if (getIntent() != null && getIntent().hasExtra(EXTRA_POI)) {
            poi = (POI) getIntent().getSerializableExtra(EXTRA_POI);
        }

        if (poi == null) {
            Toast.makeText(this, "POI数据错误", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        updateFavoriteStatus();
    }

    private void initViews() {
        TextView tvName = findViewById(R.id.tv_poi_name);
        tvName.setText(poi.getName());

        TextView tvAddress = findViewById(R.id.tv_poi_address);
        tvAddress.setText("地址: " + (poi.getAddress() != null ? poi.getAddress() : "未知"));

        TextView tvPhone = findViewById(R.id.tv_poi_phone);
        if (poi.getPhone() != null && !poi.getPhone().isEmpty()) {
            tvPhone.setText("电话: " + poi.getPhone());
        } else {
            tvPhone.setText("电话: 暂无");
        }

        RatingBar ratingBar = findViewById(R.id.rating_bar);
        TextView tvRating = findViewById(R.id.tv_rating);
        if (poi.getRating() > 0) {
            ratingBar.setRating((float) poi.getRating());
            tvRating.setText(String.format("%.1f分", poi.getRating()));
        } else {
            ratingBar.setRating(0);
            tvRating.setText("暂无评分");
        }

        TextView tvType = findViewById(R.id.tv_poi_type);
        tvType.setText("类型: " + (poi.getCategory() != null ? poi.getCategory() : "未知"));

        TextView tvDistance = findViewById(R.id.tv_poi_distance);
        if (poi.getDistance() > 0) {
            tvDistance.setText(String.format("距离: %.1f公里", poi.getDistance() / 1000.0));
        } else {
            tvDistance.setText("距离: 未知");
        }

        TextView tvLocation = findViewById(R.id.tv_poi_location);
        tvLocation.setText(String.format("坐标: %.6f, %.6f", poi.getLatitude(), poi.getLongitude()));

        Button btnNavigate = findViewById(R.id.btn_navigate);
        btnNavigate.setOnClickListener(v -> openRoutePlanning());

        Button btnCall = findViewById(R.id.btn_call);
        btnCall.setOnClickListener(v -> callPOI());

        Button btnShare = findViewById(R.id.btn_share);
        btnShare.setOnClickListener(v -> sharePOI());

        btnFavorite = findViewById(R.id.btn_favorite);
        btnFavorite.setOnClickListener(v -> toggleFavorite());
    }

    /**
     * 拨打电话 - 直接拨号
     */
    private void callPOI() {
        if (poi.getPhone() == null || poi.getPhone().isEmpty()) {
            Toast.makeText(this, "电话号码为空", Toast.LENGTH_SHORT).show();
            return;
        }

        // 检查拨号权限
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE},
                    CALL_PHONE_REQUEST_CODE);
            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse("tel:" + poi.getPhone()));
        try {
            startActivity(intent);
        } catch (Exception e) {
            // 如果直接拨号失败，退回到拨号界面
            Intent dialIntent = new Intent(Intent.ACTION_DIAL);
            dialIntent.setData(Uri.parse("tel:" + poi.getPhone()));
            startActivity(dialIntent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALL_PHONE_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                callPOI();
            } else {
                Toast.makeText(this, "需要拨号权限才能拨打电话", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 打开路线规划
     */
    private void openRoutePlanning() {
        Intent intent = new Intent(this, RouteActivity.class);
        intent.putExtra("target_lat", poi.getLatitude());
        intent.putExtra("target_lng", poi.getLongitude());
        intent.putExtra("target_name", poi.getName());
        startActivity(intent);
    }

    /**
     * 分享POI
     */
    private void sharePOI() {
        String shareText = String.format("【%s】\n地址: %s\n电话: %s\n坐标: %.6f, %.6f",
                poi.getName(),
                poi.getAddress() != null ? poi.getAddress() : "未知",
                poi.getPhone() != null ? poi.getPhone() : "暂无",
                poi.getLatitude(),
                poi.getLongitude());

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, shareText);
        startActivity(Intent.createChooser(intent, "分享POI信息"));
    }

    /**
     * 更新收藏按钮状态
     */
    private void updateFavoriteStatus() {
        isFavorite = favoriteManager.isFavorite(poi.getId());
        btnFavorite.setText(isFavorite ? "取消收藏" : "收藏");
    }

    /**
     * 切换收藏状态
     */
    private void toggleFavorite() {
        if (isFavorite) {
            favoriteManager.removeFavorite(poi.getId());
            Toast.makeText(this, "已取消收藏", Toast.LENGTH_SHORT).show();
        } else {
            favoriteManager.addFavorite(poi);
            Toast.makeText(this, "已收藏", Toast.LENGTH_SHORT).show();
        }
        updateFavoriteStatus();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
