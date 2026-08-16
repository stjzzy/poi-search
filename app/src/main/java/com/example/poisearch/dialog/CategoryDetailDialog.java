package com.example.poisearch.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.poisearch.R;
import com.example.poisearch.model.POICategory;

import java.util.List;

public class CategoryDetailDialog extends Dialog {

    private RecyclerView recyclerView;
    private TextView titleView;
    private OnSubCategorySelectedListener listener;
    private POICategory parentCategory;

    public interface OnSubCategorySelectedListener {
        void onSubCategorySelected(POICategory category);
    }

    public CategoryDetailDialog(@NonNull Context context, POICategory parentCategory) {
        super(context);
        this.parentCategory = parentCategory;
    }

    public void setOnSubCategorySelectedListener(OnSubCategorySelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_category_detail);

        titleView = findViewById(R.id.titleText);
        recyclerView = findViewById(R.id.recyclerView);
        
        titleView.setText(parentCategory.getName() + " - 选择子分类");
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        List<POICategory> subCategories = parentCategory.getSubCategories();
        // 添加"全部"选项
        POICategory allOption = new POICategory(parentCategory.getCode(), "全部", "🔍", R.drawable.ic_category_all);
        subCategories.add(0, allOption);
        
        SubCategoryAdapter adapter = new SubCategoryAdapter(subCategories);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.closeButton).setOnClickListener(v -> dismiss());
    }

    private class SubCategoryAdapter extends RecyclerView.Adapter<SubCategoryAdapter.ViewHolder> {
        private List<POICategory> categories;

        SubCategoryAdapter(List<POICategory> categories) {
            this.categories = categories;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_category_grid, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            POICategory category = categories.get(position);
            holder.bind(category);
        }

        @Override
        public int getItemCount() {
            return categories.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView iconView;
            private TextView nameView;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                iconView = itemView.findViewById(R.id.categoryIcon);
                nameView = itemView.findViewById(R.id.categoryName);
            }

            void bind(POICategory category) {
                iconView.setImageResource(category.getIconResId());
                nameView.setText(category.getName());

                itemView.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onSubCategorySelected(category);
                    }
                    dismiss();
                });
            }
        }
    }
}
