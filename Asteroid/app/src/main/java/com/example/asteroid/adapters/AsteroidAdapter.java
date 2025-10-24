package com.example.asteroid.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asteroid.databinding.LayoutAsteroidItemBinding;
import com.example.asteroid.model.Asteroid;

import java.util.List;

public class AsteroidAdapter extends RecyclerView.Adapter<AsteroidAdapter.AsteroidViewHolder> {
    List<Asteroid> adapterAsteroidList;
    public AsteroidAdapter(List<Asteroid> adapterAsteroidList){
        this.adapterAsteroidList = adapterAsteroidList;
    }

    @NonNull
    @Override
    public AsteroidAdapter.AsteroidViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        return null;
        LayoutAsteroidItemBinding binding = LayoutAsteroidItemBinding
                .inflate(LayoutInflater.from(parent.getContext()),
                        parent,false);
        AsteroidViewHolder holder = new AsteroidViewHolder(binding.getRoot(),binding);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull AsteroidAdapter.AsteroidViewHolder holder, int position) {
        Asteroid currentAsteroid = adapterAsteroidList.get(position);

        holder.itemBinding.txtViewDesignation.setText(adapterAsteroidList.get(position).getDesignation());
        holder.itemBinding.imgViewIcon.setImageResource(adapterAsteroidList.get(position).getIconResource());

        if(currentAsteroid.getTrackingStatus() != null && !currentAsteroid.getTrackingStatus().isEmpty()){
            holder.itemBinding.txtViewStatus.setText(currentAsteroid.getTrackingStatus());
        } else {
            holder.itemBinding.txtViewStatus.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return adapterAsteroidList.size();
    }

    public class AsteroidViewHolder extends RecyclerView.ViewHolder{
        LayoutAsteroidItemBinding itemBinding;
        public AsteroidViewHolder(@NonNull View itemView,LayoutAsteroidItemBinding itemBinding) {
            super(itemView);
            this.itemBinding = itemBinding;
        }
    }
}
