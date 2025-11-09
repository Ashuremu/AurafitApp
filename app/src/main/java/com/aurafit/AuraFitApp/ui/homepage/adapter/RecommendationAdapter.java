package com.aurafit.AuraFitApp.ui.homepage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.aurafit.AuraFitApp.R;
import com.aurafit.AuraFitApp.ui.homepage.model.Recommendation;

import java.util.List;

public class RecommendationAdapter extends RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder> {

    private List<Recommendation> recommendations;

    public RecommendationAdapter(List<Recommendation> recommendations) {
        this.recommendations = recommendations;
    }

    @NonNull
    @Override
    public RecommendationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recommendation, parent, false);
        return new RecommendationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecommendationViewHolder holder, int position) {
        Recommendation recommendation = recommendations.get(position);
        holder.recommendationTitle.setText(recommendation.getTitle());
        holder.recommendationDescription.setText(recommendation.getDescription());
        // Using placeholder image for now
        holder.recommendationImage.setImageResource(R.drawable.circle);
    }

    @Override
    public int getItemCount() {
        return recommendations.size();
    }

    static class RecommendationViewHolder extends RecyclerView.ViewHolder {
        ImageView recommendationImage;
        TextView recommendationTitle;
        TextView recommendationDescription;

        public RecommendationViewHolder(@NonNull View itemView) {
            super(itemView);
            recommendationImage = itemView.findViewById(R.id.recommendationImage);
            recommendationTitle = itemView.findViewById(R.id.recommendationTitle);
            recommendationDescription = itemView.findViewById(R.id.recommendationDescription);
        }
    }
}
