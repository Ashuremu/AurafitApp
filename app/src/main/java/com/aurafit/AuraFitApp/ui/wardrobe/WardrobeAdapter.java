package com.aurafit.AuraFitApp.ui.wardrobe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.model.Outfit;
import com.aurafit.AuraFitApp.data.model.OutfitItem;
import com.bumptech.glide.Glide;

import java.util.List;

public class WardrobeAdapter extends RecyclerView.Adapter<WardrobeAdapter.OutfitViewHolder> {
    
    private List<Outfit> outfits;
    private OnOutfitClickListener listener;
    private OnOutfitDeleteListener deleteListener;
    
    public interface OnOutfitClickListener {
        void onOutfitClick(Outfit outfit);
    }
    
    public interface OnOutfitDeleteListener {
        void onOutfitDelete(Outfit outfit, int position);
    }
    
    public WardrobeAdapter(List<Outfit> outfits, OnOutfitClickListener listener) {
        this.outfits = outfits;
        this.listener = listener;
    }
    
    public WardrobeAdapter(List<Outfit> outfits, OnOutfitClickListener listener, OnOutfitDeleteListener deleteListener) {
        this.outfits = outfits;
        this.listener = listener;
        this.deleteListener = deleteListener;
    }

    public void setCustomNumbering(boolean enabled) {
        // This can be used to enable/disable custom numbering
        // For now, we'll always use position-based numbering
    }

    public void removeOutfit(int position) {
        if (position >= 0 && position < outfits.size()) {
            outfits.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, outfits.size());
        }
    }

    public void addOutfit(Outfit outfit) {
        outfits.add(outfit);
        notifyItemInserted(outfits.size() - 1);
    }

    public void updateOutfits(List<Outfit> newOutfits) {
        outfits.clear();
        outfits.addAll(newOutfits);
        notifyDataSetChanged();
    }

    public void refreshNumbering() {
        notifyDataSetChanged();
    }

    public void setDeleteListener(OnOutfitDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
    }
    
    @NonNull
    @Override
    public OutfitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_outfit_card, parent, false);
        return new OutfitViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull OutfitViewHolder holder, int position) {
        Outfit outfit = outfits.get(position);
        holder.bind(outfit);
    }
    
    @Override
    public int getItemCount() {
        return outfits.size();
    }
    
    class OutfitViewHolder extends RecyclerView.ViewHolder {
        private ImageView outfitImageView;
        private TextView outfitNameText;
        private TextView outfitDescriptionText;
        private LinearLayout itemsContainer;
        private ImageButton deleteOutfitButton;
        
        public OutfitViewHolder(@NonNull View itemView) {
            super(itemView);
            outfitImageView = itemView.findViewById(R.id.outfitImageView);
            outfitNameText = itemView.findViewById(R.id.outfitNameText);
            outfitDescriptionText = itemView.findViewById(R.id.outfitDescriptionText);
            itemsContainer = itemView.findViewById(R.id.itemsContainer);
            deleteOutfitButton = itemView.findViewById(R.id.deleteOutfitButton);
            
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOutfitClick(outfits.get(getAdapterPosition()));
                }
            });
            
            deleteOutfitButton.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Outfit outfit = outfits.get(position);
                    OutfitDeleteHelper.showDeleteConfirmation(
                        itemView.getContext(), 
                        outfit, 
                        deleteListener, 
                        position
                    );
                }
            });
        }
        
        public void bind(Outfit outfit) {
            // Set outfit name with counting only (position + 1 for 1-based numbering)
            int outfitNumber = getAdapterPosition() + 1;
            outfitNameText.setText("Option " + outfitNumber);
            
            outfitDescriptionText.setText(generateOutfitDescription(outfit));
            
            // Load outfit image from assets
            loadOutfitImage(outfit);
            
            // Display outfit items
            displayOutfitItems(outfit);
        }

        public void setOutfitNumber(int number) {
            outfitNameText.setText("Option " + number);
        }
        
        private void loadOutfitImage(Outfit outfit) {
            String imagePath = getOutfitPreviewImage(outfit);
            
            if (imagePath != null && !imagePath.isEmpty()) {
                // Use Glide to load the asset image
                Glide.with(itemView.getContext())
                    .load(imagePath)
                    .placeholder(R.drawable.ic_outfit_placeholder)
                    .error(R.drawable.ic_outfit_placeholder)
                    .centerCrop()
                    .listener(new com.bumptech.glide.request.RequestListener<android.graphics.drawable.Drawable>() {
                        @Override
                        public boolean onLoadFailed(com.bumptech.glide.load.engine.GlideException e, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, boolean isFirstResource) {
                            System.out.println("WardrobeAdapter - Failed to load image: " + imagePath + " Error: " + (e != null ? e.getMessage() : "Unknown error"));
                            return false;
                        }
                        
                        @Override
                        public boolean onResourceReady(android.graphics.drawable.Drawable resource, Object model, com.bumptech.glide.request.target.Target<android.graphics.drawable.Drawable> target, com.bumptech.glide.load.DataSource dataSource, boolean isFirstResource) {
                            System.out.println("WardrobeAdapter - Successfully loaded image: " + imagePath);
                            return false;
                        }
                    })
                    .into(outfitImageView);
            } else {
                System.out.println("WardrobeAdapter - No image path found for outfit");
                outfitImageView.setImageResource(R.drawable.ic_outfit_placeholder);
            }
        }
        
        private String getOutfitPreviewImage(Outfit outfit) {
            if (outfit.getItems() == null || outfit.getItems().isEmpty()) {
                System.out.println("WardrobeAdapter - No items in outfit");
                return null;
            }
            
            // Priority: Top > Bottom > Shoes > Accessories
            String[] priorities = {"top", "bottom", "shoes", "accessories"};
            
            for (String category : priorities) {
                for (OutfitItem item : outfit.getItems()) {
                    if (category.equalsIgnoreCase(item.getCategory())) {
                        // Use arModelUrl which contains the asset path
                        String assetPath = item.getArModelUrl();
                        System.out.println("WardrobeAdapter - Found item: " + item.getName() + " with arModelUrl: " + assetPath);
                        if (assetPath != null && !assetPath.isEmpty()) {
                            // Convert to proper asset path format - the path is already correct
                            String fullPath = "file:///android_asset/" + assetPath;
                            System.out.println("WardrobeAdapter - Loading image: " + fullPath);
                            return fullPath;
                        }
                    }
                }
            }
            
            System.out.println("WardrobeAdapter - No valid image path found for outfit");
            return null;
        }
        
        private String generateOutfitDescription(Outfit outfit) {
            StringBuilder description = new StringBuilder();
            for (OutfitItem item : outfit.getItems()) {
                if (description.length() > 0) {
                    description.append(" • ");
                }
                description.append(item.getName());
            }
            return description.toString();
        }
        
        private void displayOutfitItems(Outfit outfit) {
            itemsContainer.removeAllViews();
            
            for (OutfitItem item : outfit.getItems()) {
                TextView itemText = new TextView(itemView.getContext());
                itemText.setText("• " + item.getName());
                itemText.setTextSize(12);
                itemText.setTextColor(itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                itemText.setPadding(0, 2, 0, 2);
                
                itemsContainer.addView(itemText);
            }
        }
    }
}
