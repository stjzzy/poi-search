package com.example.poisearch.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.example.poisearch.R;

import java.util.ArrayList;
import java.util.List;

public class OfflineCityAdapter extends RecyclerView.Adapter<OfflineCityAdapter.CityViewHolder> {

    private List<OfflineMapCity> cityList = new ArrayList<>();
    private OnCityClickListener listener;

    public interface OnCityClickListener {
        void onCityClick(OfflineMapCity city);
    }

    public void setOnCityClickListener(OnCityClickListener listener) {
        this.listener = listener;
    }

    public void setCityList(List<OfflineMapCity> list) {
        this.cityList = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
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
        OfflineMapCity city = cityList.get(position);
        holder.bind(city);
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

        void bind(OfflineMapCity city) {
            cityName.setText(city.getCity());
            long size = city.getSize();
            String sizeText;
            if (size > 1024 * 1024) {
                sizeText = String.format("%.1f MB", size / (1024.0 * 1024.0));
            } else if (size > 1024) {
                sizeText = String.format("%.1f KB", size / 1024.0);
            } else {
                sizeText = size + " B";
            }
            citySize.setText(sizeText);
        }
    }
}
