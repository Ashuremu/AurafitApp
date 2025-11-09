package com.aurafit.AuraFitApp.ui.generate;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.model.OutfitItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying individual clothing items in the selector panel
 */
public class ClothingItemAdapter extends RecyclerView.Adapter<ClothingItemAdapter.ItemViewHolder> {
    
    private Context context;
    private List<OutfitItem> items;
    private OutfitItem selectedItem;
    private OnItemSelectedListener listener;
    
    public interface OnItemSelectedListener {
        void onItemSelected(OutfitItem item);
    }
    
    public ClothingItemAdapter(Context context, OnItemSelectedListener listener) {
        this.context = context;
        this.items = new ArrayList<>();
        this.listener = listener;
    }
    
    public void setItems(List<OutfitItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }
    
    public void setSelectedItem(OutfitItem item) {
        this.selectedItem = item;
        notifyDataSetChanged();
    }
    
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_clothing_selector, parent, false);
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
        LinearLayout itemContainer;
        ImageView itemIcon;
        TextView itemName;
        View selectedIndicator;
        
        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemContainer = itemView.findViewById(R.id.itemContainer);
            itemIcon = itemView.findViewById(R.id.itemIcon);
            itemName = itemView.findViewById(R.id.itemName);
            selectedIndicator = itemView.findViewById(R.id.selectedIndicator);
        }
        
        void bind(OutfitItem item) {
            // Set item name (truncate if too long)
            String name = item.getName();
            if (name.length() > 20) {
                name = name.substring(0, 17) + "...";
            }
            itemName.setText(name);
            
            // Set icon based on category
            setIconForCategory(item.getCategory());
            
            // Show selection indicator if this item is selected
            boolean isSelected = selectedItem != null && 
                                selectedItem.getItemId().equals(item.getItemId());
            selectedIndicator.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            
            // Highlight selected item
            if (isSelected) {
                itemContainer.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0xFF4ECDC4)
                );
            } else {
                itemContainer.setBackgroundTintList(
                    android.content.res.ColorStateList.valueOf(0x80000000)
                );
            }
            
            // Handle item click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onItemSelected(item);
                }
            });
        }
        
        private void setIconForCategory(String category) {
            int iconResId;
            switch (category) {
                case "Top":
                    iconResId = R.drawable.ic_shirt;
                    break;
                case "Bottom":
                    iconResId = R.drawable.ic_pants;
                    break;
                case "Shoes":
                    iconResId = R.drawable.ic_shoes;
                    break;
                case "Accessories":
                    iconResId = R.drawable.ic_accessories;
                    break;
                default:
                    iconResId = R.drawable.ic_shirt;
                    break;
            }
            itemIcon.setImageResource(iconResId);
        }
    }
}

