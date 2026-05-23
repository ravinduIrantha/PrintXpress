package com.printxpress.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.printxpress.app.databinding.ItemProductBinding;
import com.printxpress.app.models.Product;
import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    private final List<Product> products;
    private final OnProductClickListener listener;

    public ProductAdapter(List<Product> products, OnProductClickListener listener) {
        this.products = products;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductBinding binding = ItemProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = products.get(position);
        holder.binding.textProductName.setText(product.getName());
        holder.binding.textProductPrice.setText(String.format(Locale.getDefault(), "Rs. %.2f", product.getPrice()));

        Object imageSource = product.getImageUrl();
        if (product.getImageUrl() != null && !product.getImageUrl().startsWith("http")) {
            int resId = holder.itemView.getContext().getResources().getIdentifier(
                    product.getImageUrl(), "drawable", holder.itemView.getContext().getPackageName());
            if (resId != 0) {
                imageSource = resId;
            }
        }

        Glide.with(holder.itemView.getContext())
                .load(imageSource)
                .placeholder(com.printxpress.app.R.drawable.ic_launcher_foreground)
                .error(com.printxpress.app.R.drawable.ic_launcher_foreground)
                .centerCrop()
                .into(holder.binding.imageProduct);
        holder.itemView.setOnClickListener(v -> listener.onProductClick(product));
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void updateList(List<Product> newList) {
        this.products.clear();
        this.products.addAll(newList);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemProductBinding binding;
        public ViewHolder(ItemProductBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
