package com.example.poisearch.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poisearch.R;

import java.util.List;

public class SimpleCityAdapter extends RecyclerView.Adapter<SimpleCityAdapter.CityViewHolder> {

    private List<String> cityList;
    private List<String> downloadedCities;
    private OnCityClickListener listener;

    public interface OnCityClickListener {
        void onCityClick(String cityName);
    }

    public SimpleCityAdapter(List<String> cityList, List<String> downloadedCities) {
        this.cityList = cityList;
        this.downloadedCities = downloadedCities;
    }

    public void setOnCityClickListener(OnCityClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public CityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_offline_city, parent, false);
        return new CityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CityViewHolder holder, int position) {
        String cityName = cityList.get(position);
        boolean isDownloaded = downloadedCities.contains(cityName);
        holder.bind(cityName, isDownloaded);
    }

    @Override
    public int getItemCount() {
        return cityList.size();
    }

    class CityViewHolder extends RecyclerView.ViewHolder {
        TextView cityName;
        TextView citySize;
        Button btnDownload;

        CityViewHolder(@NonNull View itemView) {
            super(itemView);
            cityName = itemView.findViewById(R.id.tv_city_name);
            citySize = itemView.findViewById(R.id.tv_city_size);
            btnDownload = itemView.findViewById(R.id.btn_download_city);

            btnDownload.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onCityClick(cityList.get(pos));
                }
            });
        }

        void bind(String name, boolean isDownloaded) {
            cityName.setText(name);
            if (isDownloaded) {
                citySize.setText("已下载");
                btnDownload.setText("已下载");
                btnDownload.setEnabled(false);
            } else {
                citySize.setText("未下载");
                btnDownload.setText("下载");
                btnDownload.setEnabled(true);
            }
        }
    }
}
