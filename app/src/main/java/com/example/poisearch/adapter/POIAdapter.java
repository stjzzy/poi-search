package com.example.poisearch.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poisearch.R;
import com.example.poisearch.model.POI;

import java.util.ArrayList;
import java.util.List;

public class POIAdapter extends RecyclerView.Adapter<POIAdapter.POIViewHolder> {

    private List<POI> poiList = new ArrayList<>();
    private OnPOIClickListener listener;

    public interface OnPOIClickListener {
        void onPOIClick(POI poi);
        void onPhoneClick(String phone);
        void onNavigateClick(POI poi);
    }

    public void setOnPOIClickListener(OnPOIClickListener listener) {
        this.listener = listener;
    }

    public void setPOIList(List<POI> pois) {
        this.poiList = pois != null ? pois : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addPOIs(List<POI> pois) {
        if (pois != null) {
            int startPosition = poiList.size();
            poiList.addAll(pois);
            notifyItemRangeInserted(startPosition, pois.size());
        }
    }

    public void clear() {
        poiList.clear();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public POIViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_poi, parent, false);
        return new POIViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull POIViewHolder holder, int position) {
        POI poi = poiList.get(position);
        holder.bind(poi);
    }

    @Override
    public int getItemCount() {
        return poiList.size();
    }

    class POIViewHolder extends RecyclerView.ViewHolder {
        private final ImageView poiIcon;
        private final TextView poiName;
        private final TextView poiCategory;
        private final TextView poiDistance;
        private final TextView poiAddress;
        private final TextView poiPhone;
        private final TextView poiRating;

        POIViewHolder(@NonNull View itemView) {
            super(itemView);
            poiIcon = itemView.findViewById(R.id.poiIcon);
            poiName = itemView.findViewById(R.id.poiName);
            poiCategory = itemView.findViewById(R.id.poiCategory);
            poiDistance = itemView.findViewById(R.id.poiDistance);
            poiAddress = itemView.findViewById(R.id.poiAddress);
            poiPhone = itemView.findViewById(R.id.poiPhone);
            poiRating = itemView.findViewById(R.id.poiRating);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPOIClick(poiList.get(position));
                }
            });
        }

        void bind(POI poi) {
            poiName.setText(poi.getName());
            poiCategory.setText(poi.getCategory());
            
            // 设置距离
            if (poi.getDistance() > 0) {
                String distanceText;
                if (poi.getDistance() < 1000) {
                    distanceText = String.format("%.0f米", poi.getDistance());
                } else {
                    distanceText = String.format("%.1f公里", poi.getDistance() / 1000);
                }
                poiDistance.setText(distanceText);
            } else {
                poiDistance.setText("");
            }

            // 设置地址
            if (poi.getAddress() != null && !poi.getAddress().isEmpty()) {
                poiAddress.setText(poi.getAddress());
                poiAddress.setVisibility(View.VISIBLE);
            } else {
                poiAddress.setVisibility(View.GONE);
            }

            // 设置电话
            if (poi.getPhone() != null && !poi.getPhone().isEmpty()) {
                poiPhone.setText(poi.getPhone());
                poiPhone.setVisibility(View.VISIBLE);
                poiPhone.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onPhoneClick(poi.getPhone());
                    }
                });
            } else {
                poiPhone.setVisibility(View.GONE);
            }

            // 设置评分
            if (poi.getRating() > 0) {
                poiRating.setText(String.format("%.1f", poi.getRating()));
                poiRating.setVisibility(View.VISIBLE);
            } else {
                poiRating.setVisibility(View.GONE);
            }

            // 设置图标
            setCategoryIcon(poi.getCategory());
        }

        private void setCategoryIcon(String category) {
            int iconRes = com.example.poisearch.R.drawable.ic_category_default;
            
            if (category == null) {
                iconRes = com.example.poisearch.R.drawable.ic_category_default;
            } else if (category.contains("餐") || category.contains("饮") || category.contains("快餐") || 
                      category.contains("火锅") || category.contains("咖啡") || category.contains("厅")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_food;
            } else if (category.contains("酒店") || category.contains("宾馆") || category.contains("旅")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_hotel;
            } else if (category.contains("购物") || category.contains("超市") || category.contains("便利") ||
                      category.contains("商场") || category.contains("店")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_shopping;
            } else if (category.contains("交通") || category.contains("地铁") || category.contains("公交") ||
                      category.contains("加油") || category.contains("停车")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_transport;
            } else if (category.contains("银行") || category.contains("ATM") || category.contains("证券") ||
                      category.contains("保险")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_finance;
            } else if (category.contains("医院") || category.contains("医疗") || category.contains("药") ||
                      category.contains("诊所")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_medical;
            } else if (category.contains("健身") || category.contains("运动") || category.contains("电影") ||
                      category.contains("KTV") || category.contains("娱乐") || category.contains("休闲") ||
                      category.contains("公园") || category.contains("游泳") || category.contains("羽毛") ||
                      category.contains("酒吧")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_sports;
            } else if (category.contains("景点") || category.contains("公园") || category.contains("故宫") ||
                      category.contains("天坛") || category.contains("寺庙") || category.contains("世遗")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_scenic;
            } else if (category.contains("教育") || category.contains("大学") || category.contains("中学") ||
                      category.contains("培训") || category.contains("图书") || category.contains("学校")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_education;
            } else if (category.contains("政府") || category.contains("机关") || category.contains("行政")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_government;
            } else if (category.contains("企业") || category.contains("公司") || category.contains("商务")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_company;
            } else if (category.contains("服务") || category.contains("快递") || category.contains("家政") ||
                      category.contains("中介") || category.contains("洗衣")) {
                iconRes = com.example.poisearch.R.drawable.ic_category_service;
            }
            
            poiIcon.setImageResource(iconRes);
        }
    }
}
