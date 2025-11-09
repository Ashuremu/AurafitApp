package com.aurafit.AuraFitApp.ui.homepage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.ui.homepage.model.WardrobeItem;
import com.aurafit.AuraFitApp.ui.reservation.ItemReservationActivity;
import com.bumptech.glide.Glide;

import java.util.List;

public class WardrobeItemAdapter extends RecyclerView.Adapter<WardrobeItemAdapter.WardrobeItemViewHolder> {
    
    private List<WardrobeItem> wardrobeItems;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(WardrobeItem wardrobeItem);
    }

    public WardrobeItemAdapter(List<WardrobeItem> wardrobeItems) {
        this.wardrobeItems = wardrobeItems;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void updateItems(List<WardrobeItem> newItems) {
        this.wardrobeItems = newItems;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WardrobeItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_wardrobe, parent, false);
        return new WardrobeItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WardrobeItemViewHolder holder, int position) {
        WardrobeItem item = wardrobeItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return wardrobeItems != null ? wardrobeItems.size() : 0;
    }

    class WardrobeItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemImage;
        private TextView itemName;
        private TextView itemDescription;
        private TextView itemGender;
        private TextView itemWeather;

        public WardrobeItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemDescription = itemView.findViewById(R.id.itemDescription);
            itemGender = itemView.findViewById(R.id.itemGender);
            itemWeather = itemView.findViewById(R.id.itemWeather);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    WardrobeItem item = wardrobeItems.get(position);
                    
                    // Navigate to reservation activity
                    Context context = itemView.getContext();
                    Intent intent = new Intent(context, ItemReservationActivity.class);
                    intent.putExtra("wardrobeItem", item);
                    context.startActivity(intent);
                }
            });
        }

        public void bind(WardrobeItem item) {
            // Set basic information
            itemName.setText(item.getName() != null ? item.getName() : "Untitled");
            itemDescription.setText(item.getDescription() != null ? item.getDescription() : "");
            
            // Set gender with light colored background
            if (itemGender != null) {
                String gender = item.getGender() != null ? item.getGender() : "Unknown";
                itemGender.setText(gender);
                
                // Apply gender-specific light background colors
                if (gender.equalsIgnoreCase("Male")) {
                    itemGender.setBackgroundColor(0xFFE3F2FD); // Light blue
                } else if (gender.equalsIgnoreCase("Female")) {
                    itemGender.setBackgroundColor(0xFFF3E5F5); // Light purple
                } else if (gender.equalsIgnoreCase("Unisex")) {
                    itemGender.setBackgroundColor(0xFFE8F5E8); // Light green
                } else {
                    itemGender.setBackgroundColor(0xFFF5F5F5); // Light gray
                }
            }
            
            // Set weather with light colored background
            if (itemWeather != null) {
                String weather = item.getWeather() != null ? item.getWeather() : "Unknown";
                itemWeather.setText(weather);
                
                // Apply weather-specific light background colors
                if (weather.equalsIgnoreCase("Sunny")) {
                    itemWeather.setBackgroundColor(0xFFFFF3E0); // Light orange
                } else if (weather.equalsIgnoreCase("Rainy")) {
                    itemWeather.setBackgroundColor(0xFFE3F2FD); // Light blue
                } else if (weather.equalsIgnoreCase("Sunny/Rainy")) {
                    itemWeather.setBackgroundColor(0xFFE8F5E8); // Light green
                } else {
                    itemWeather.setBackgroundColor(0xFFF5F5F5); // Light gray
                }
            }
            

            // Load image using Glide
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .error(R.drawable.placeholder_image)
                        .centerCrop()
                        .into(itemImage);
            } else {
                itemImage.setImageResource(R.drawable.placeholder_image);
            }
        }
    }
}
