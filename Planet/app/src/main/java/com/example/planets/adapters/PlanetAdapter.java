package com.example.planets.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.example.planets.databinding.LayoutPlanetItemBinding;
import com.example.planets.model.Planet;

import java.util.List;

public class PlanetAdapter extends RecyclerView.Adapter<PlanetAdapter.PlanetViewHolder> {

    List<Planet> adapterPlanetList;

    public PlanetAdapter(List<Planet> adapterPlanetList) {
        this.adapterPlanetList = adapterPlanetList;
    }

    @NonNull
    @Override
    public PlanetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        return null;
        LayoutPlanetItemBinding binding = LayoutPlanetItemBinding
                .inflate(LayoutInflater.from(parent.getContext()),
                        parent,false);
        PlanetViewHolder holder = new PlanetViewHolder(binding.getRoot(),binding);
        return holder;

    }

    @Override
    public void onBindViewHolder(@NonNull PlanetViewHolder holder, int position) {
        Planet currentPlanet = adapterPlanetList.get(position);

        holder.itemBiding.txtViewName.setText(adapterPlanetList.get(position).getPlanetName());
        holder.itemBiding.imageViewPlanet.setImageResource(adapterPlanetList.get(position).getImageResource());

        if(currentPlanet.getStatus() != null && !currentPlanet.getStatus().isEmpty()){
            holder.itemBiding.txtViewStatus.setText(currentPlanet.getStatus());
        } else {
            holder.itemBiding.txtViewStatus.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return adapterPlanetList.size();
    }

    public class PlanetViewHolder extends RecyclerView.ViewHolder {
        LayoutPlanetItemBinding itemBiding;
        public PlanetViewHolder(@NonNull View itemView, LayoutPlanetItemBinding itemBiding) {
            super(itemView);
            this.itemBiding = itemBiding;
        }
    }
}
