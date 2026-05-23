package com.printxpress.app.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.printxpress.app.databinding.ItemOrderBinding;
import com.printxpress.app.models.Order;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.ViewHolder> {

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    private List<Order> orders;
    private OnOrderClickListener listener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    public OrderAdapter(List<Order> orders, OnOrderClickListener listener) {
        this.orders = orders;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOrderBinding binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Order order = orders.get(position);
        
        String category = order.getCategory() != null ? order.getCategory() : "Item";
        String displayTitle = category;
        
        if (category.equalsIgnoreCase("Cards") || category.equalsIgnoreCase("Business Cards")) {
            displayTitle = "Card";
        } else if (category.endsWith("s") && !category.equalsIgnoreCase("T-Shirts")) {
            displayTitle = category.substring(0, category.length() - 1);
        }
        
        holder.binding.textOrderID.setText(displayTitle + " Order");
        holder.binding.textOrderStatus.setText(order.getStatus());
        holder.binding.textOrderAmount.setText("Total: Rs. " + String.format("%.2f", order.getTotalAmount()));
        
        if (order.getCreatedAt() != null) {
            holder.binding.textOrderDate.setText(dateFormat.format(new java.util.Date(order.getCreatedAt())));
        }

        holder.itemView.setOnClickListener(v -> listener.onOrderClick(order));
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ItemOrderBinding binding;
        public ViewHolder(ItemOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
