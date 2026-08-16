package com.example.poisearch.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poisearch.R;
import com.example.poisearch.model.SearchHistory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<SearchHistory> historyList = new ArrayList<>();
    private OnHistoryClickListener listener;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm", Locale.getDefault());

    public interface OnHistoryClickListener {
        void onHistoryClick(SearchHistory history);
        void onHistoryDelete(SearchHistory history);
    }

    public void setOnHistoryClickListener(OnHistoryClickListener listener) {
        this.listener = listener;
    }

    public void setHistoryList(List<SearchHistory> list) {
        this.historyList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        SearchHistory history = historyList.get(position);
        holder.bind(history);
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvKeyword;
        TextView tvDetail;
        TextView tvTime;
        TextView tvResultCount;
        TextView btnDelete;

        HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvKeyword = itemView.findViewById(R.id.tv_history_keyword);
            tvDetail = itemView.findViewById(R.id.tv_history_detail);
            tvTime = itemView.findViewById(R.id.tv_history_time);
            tvResultCount = itemView.findViewById(R.id.tv_history_count);
            btnDelete = itemView.findViewById(R.id.btn_delete_history);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onHistoryClick(historyList.get(pos));
                }
            });

            btnDelete.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onHistoryDelete(historyList.get(pos));
                }
            });
        }

        void bind(SearchHistory history) {
            String keyword = history.getKeywords();
            if (keyword == null || keyword.isEmpty()) {
                keyword = history.getCategoryName() != null ? history.getCategoryName() : "分类搜索";
            }
            tvKeyword.setText(keyword);

            String detail = String.format("范围: %d米 | 分类: %s",
                    history.getRadius(),
                    history.getCategoryName() != null ? history.getCategoryName() : "全部");
            tvDetail.setText(detail);

            tvTime.setText(sdf.format(new Date(history.getSearchTime())));
            tvResultCount.setText(history.getResultCount() + "个结果");
        }
    }
}
