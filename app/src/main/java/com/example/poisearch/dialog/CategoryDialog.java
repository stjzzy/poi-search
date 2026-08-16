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

public class CategoryDialog extends Dialog {

    private RecyclerView recyclerView;
    private OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onCategorySelected(POICategory category);
    }

    public CategoryDialog(@NonNull Context context) {
        super(context);
    }

    public CategoryDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public void setOnCategorySelectedListener(OnCategorySelectedListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_category);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        List<POICategory> categories = POICategory.getAllCategories();
        CategoryAdapter adapter = new CategoryAdapter(categories);
        recyclerView.setAdapter(adapter);

        findViewById(R.id.closeButton).setOnClickListener(v -> dismiss());
    }

    private class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        private List<POICategory> categories;

        CategoryAdapter(List<POICategory> categories) {
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
                        listener.onCategorySelected(category);
                    }
                    dismiss();
                });
            }
        }
    }
}
