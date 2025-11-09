package com.aurafit.AuraFitApp.ui.wardrobe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.model.OutfitItem;
import com.bumptech.glide.Glide;

import java.util.List;

public class OutfitItemsAdapter extends RecyclerView.Adapter<OutfitItemsAdapter.ItemViewHolder> {
    
    private List<OutfitItem> items;
    
    public OutfitItemsAdapter(List<OutfitItem> items) {
        this.items = items;
    }
    
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit_item, parent, false);
        return new ItemViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        OutfitItem item = items.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return items.size();
    }
    
    class ItemViewHolder extends RecyclerView.ViewHolder {
        private ImageView itemImageView;
        private TextView itemNameText;
        private TextView itemCategoryText;
        
        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImageView = itemView.findViewById(R.id.itemImageView);
            itemNameText = itemView.findViewById(R.id.itemNameText);
            itemCategoryText = itemView.findViewById(R.id.itemCategoryText);
        }
        
        public void bind(OutfitItem item) {
            itemNameText.setText(item.getName());
            itemCategoryText.setText(item.getCategory());
            
            // Load item image from assets
            String imagePath = getItemImagePath(item);
            if (imagePath != null && !imagePath.isEmpty()) {
                Glide.with(itemView.getContext())
                    .load(imagePath)
                    .placeholder(R.drawable.ic_outfit_placeholder)
                    .error(R.drawable.ic_outfit_placeholder)
                    .centerCrop()
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            System.out.println("OutfitItemsAdapter - Failed to load image: " + imagePath + " Error: " + (e != null ? e.getMessage() : "Unknown error"));
                            return false;
                        }
                        
                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            System.out.println("OutfitItemsAdapter - Successfully loaded image: " + imagePath);
                            return false;
                        }
                    })
                    .into(itemImageView);
            } else {
                System.out.println("OutfitItemsAdapter - No image path found for item");
                itemImageView.setImageResource(R.drawable.ic_outfit_placeholder);
            }
        }
        
        private String getItemImagePath(OutfitItem item) {
            String assetPath = item.getArModelUrl();
            System.out.println("OutfitItemsAdapter - Item: " + item.getName() + " with arModelUrl: " + assetPath);
            if (assetPath != null && !assetPath.isEmpty()) {
                // Convert to proper asset path format - the path is already correct
                String fullPath = "file:///android_asset/" + assetPath;
                System.out.println("OutfitItemsAdapter - Loading image: " + fullPath);
                return fullPath;
            }
            System.out.println("OutfitItemsAdapter - No valid asset path for item: " + item.getName());
            return null;
        }
    }
}
