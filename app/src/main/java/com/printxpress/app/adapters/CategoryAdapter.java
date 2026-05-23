package com.printxpress.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.printxpress.app.databinding.ItemCategoryBinding;
import com.printxpress.app.models.Category;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category);
    }

    private List<Category> categories;
    private OnCategoryClickListener listener;

    public CategoryAdapter(List<Category> categories, OnCategoryClickListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCategoryBinding binding = ItemCategoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.binding.textCategoryName.setText(category.getName());
        
        Object imageSource = category.getImageUrl();
        if (category.getImageUrl() != null && !category.getImageUrl().startsWith("http")) {
            int resId = holder.itemView.getContext().getResources().getIdentifier(
                    category.getImageUrl(), "drawable", holder.itemView.getContext().getPackageName());
            if (resId != 0) {
                imageSource = resId;
            }
        }

        Glide.with(holder.itemView.getContext())
                .load(imageSource)
                .placeholder(com.printxpress.app.R.drawable.ic_launcher_foreground)
                .error(com.printxpress.app.R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(holder.binding.imageCategory);
        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category));
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemCategoryBinding binding;
        public ViewHolder(ItemCategoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
