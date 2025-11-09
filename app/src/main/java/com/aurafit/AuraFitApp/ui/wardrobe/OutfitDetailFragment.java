package com.aurafit.AuraFitApp.ui.wardrobe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.data.model.Outfit;
import com.aurafit.AuraFitApp.data.model.OutfitItem;
import com.bumptech.glide.Glide;

import java.util.List;

public class OutfitDetailFragment extends Fragment {
    
    private static final String ARG_OUTFIT = "outfit";
    
    private Outfit outfit;
    private RecyclerView itemsRecyclerView;
    private OutfitItemsAdapter itemsAdapter;
    private TextView outfitNameText;
    private TextView outfitDescriptionText;
    private ImageView backButton;
    
    public static OutfitDetailFragment newInstance(Outfit outfit) {
        OutfitDetailFragment fragment = new OutfitDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_OUTFIT, outfit);
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            outfit = (Outfit) getArguments().getSerializable(ARG_OUTFIT);
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_outfit_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupClickListeners();
        displayOutfitDetails();
    }
    
    private void initializeViews(View view) {
        outfitNameText = view.findViewById(R.id.outfitNameText);
        outfitDescriptionText = view.findViewById(R.id.outfitDescriptionText);
        itemsRecyclerView = view.findViewById(R.id.itemsRecyclerView);
        backButton = view.findViewById(R.id.backButton);
    }
    
    private void setupClickListeners() {
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }
    
    private void displayOutfitDetails() {
        if (outfit == null) return;
        
        outfitNameText.setText(outfit.getName());
        outfitDescriptionText.setText(generateOutfitDescription(outfit));
        
        // Setup items grid
        itemsAdapter = new OutfitItemsAdapter(outfit.getItems());
        itemsRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        itemsRecyclerView.setAdapter(itemsAdapter);
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
}
