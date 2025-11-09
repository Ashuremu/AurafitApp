package com.aurafit.AuraFitApp.ui.reservation.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.ui.reservation.model.ReservationItem;
import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReservedItemAdapter extends RecyclerView.Adapter<ReservedItemAdapter.ReservedItemViewHolder> {

    private List<ReservationItem> reservedItems;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(ReservationItem reservationItem);
    }

    public ReservedItemAdapter(List<ReservationItem> reservedItems) {
        this.reservedItems = reservedItems;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void updateData(List<ReservationItem> newReservedItems) {
        this.reservedItems = newReservedItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ReservedItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_reserved, parent, false);
        return new ReservedItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReservedItemViewHolder holder, int position) {
        ReservationItem item = reservedItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return reservedItems != null ? reservedItems.size() : 0;
    }

    class ReservedItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemImage;
        private TextView itemName;
        private TextView itemSize;
        private TextView itemPrice;
        private TextView itemStatus;
        private TextView reservedDate;

        public ReservedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemSize = itemView.findViewById(R.id.itemSize);
            itemPrice = itemView.findViewById(R.id.itemPrice);
            itemStatus = itemView.findViewById(R.id.itemStatus);
            reservedDate = itemView.findViewById(R.id.reservedDate);

            itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(reservedItems.get(position));
                    }
                }
            });
        }

        public void bind(ReservationItem item) {
            // Set item image
            if (item.getItemImageUrl() != null && !item.getItemImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getItemImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(itemImage);
            } else {
                itemImage.setImageResource(R.drawable.placeholder_image);
            }

            // Set item name
            itemName.setText(item.getItemName() != null ? item.getItemName() : "Untitled");

            // Set size
            itemSize.setText("Size: " + (item.getSelectedSize() != null ? item.getSelectedSize() : "N/A"));

            // Set price
            itemPrice.setText(String.format("₱%.2f", item.getItemPrice()));

            // Set status with color coding
            String status = item.getStatus() != null ? item.getStatus() : "reserved";
            itemStatus.setText(status);
            
            // Apply status-specific styling
            switch (status.toLowerCase()) {
                case "reserved":
                    itemStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_blue_dark));
                    itemStatus.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_blue_light));
                    break;
                case "confirmed":
                    itemStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                    itemStatus.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_green_light));
                    break;
                case "cancelled":
                    itemStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                    itemStatus.setBackgroundColor(itemView.getContext().getColor(android.R.color.holo_red_light));
                    break;
                default:
                    itemStatus.setTextColor(itemView.getContext().getColor(android.R.color.darker_gray));
                    itemStatus.setBackgroundColor(itemView.getContext().getColor(android.R.color.darker_gray));
            }

            // Set reserved date
            if (item.getReservedAt() != null) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                String dateString = dateFormat.format(item.getReservedAt());
                reservedDate.setText("Reserved on: " + dateString);
            } else {
                reservedDate.setText("Reserved on: Unknown");
            }
        }
    }
}

