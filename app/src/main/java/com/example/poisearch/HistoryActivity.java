package com.example.poisearch;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poisearch.adapter.HistoryAdapter;
import com.example.poisearch.database.HistoryManager;
import com.example.poisearch.model.SearchHistory;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.OnHistoryClickListener {

    private HistoryManager historyManager;
    private HistoryAdapter historyAdapter;
    private List<SearchHistory> historyList = new ArrayList<>();
    private TextView emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyManager = new HistoryManager(this);

        initViews();
        loadHistory();
    }

    private void initViews() {
        MaterialButton btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(v -> finish());

        emptyView = findViewById(R.id.empty_view);

        RecyclerView recyclerView = findViewById(R.id.history_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        historyAdapter = new HistoryAdapter();
        historyAdapter.setOnHistoryClickListener(this);
        recyclerView.setAdapter(historyAdapter);

        MaterialButton btnClear = findViewById(R.id.btn_clear_history);
        btnClear.setOnClickListener(v -> confirmClearAll());
    }

    private void loadHistory() {
        historyList = historyManager.getAllHistory();
        historyAdapter.setHistoryList(historyList);

        if (historyList.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("暂无搜索历史");
        } else {
            emptyView.setVisibility(View.GONE);
        }
    }

    private void confirmClearAll() {
        if (historyList.isEmpty()) {
            Toast.makeText(this, "没有历史可清空", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("清空历史")
                .setMessage("确定要清空所有搜索历史吗？此操作不可恢复。")
                .setPositiveButton("清空", (dialog, which) -> {
                    historyManager.clearAllHistory();
                    loadHistory();
                    Toast.makeText(this, "已清空所有历史", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    @Override
    public void onHistoryClick(SearchHistory history) {
        // 返回到MainActivity并传递搜索参数
        android.content.Intent intent = new android.content.Intent();
        intent.putExtra("keywords", history.getKeywords() != null ? history.getKeywords() : "");
        intent.putExtra("category_code", history.getCategoryCode() != null ? history.getCategoryCode() : "");
        intent.putExtra("radius", history.getRadius());
        intent.putExtra("lat", history.getLat());
        intent.putExtra("lng", history.getLng());
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onHistoryDelete(SearchHistory history) {
        historyManager.deleteHistory(history.getId());
        loadHistory();
        Toast.makeText(this, "已删除", Toast.LENGTH_SHORT).show();
    }
}
