package com.example.recyclerviewdemo.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclerviewdemo.R;
import com.example.recyclerviewdemo.databinding.LayoutTuneitemBinding;
import com.example.recyclerviewdemo.model.Tune;

import java.util.List;

public class TuneAdapter extends RecyclerView.Adapter<TuneAdapter.TuneViewHolder> {
    List<Tune> adapterTuneList;
    int SelectedInd = -1;

    public List<Tune> getAdapterTuneList() {
        return adapterTuneList;
    }

    public void setAdapterTuneList(List<Tune> adapterTuneList) {
        this.adapterTuneList = adapterTuneList;
    }

    public int getSelectedInd() {
        return SelectedInd;
    }

    public void setSelectedInd(int selectedInd) {
        SelectedInd = selectedInd;
    }

    public TuneAdapter(List<Tune> adapterTuneList) {

        this.adapterTuneList = adapterTuneList;
    }

    @NonNull
    @Override
    public TuneViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //return null;
        //create binding object with external layout
        LayoutTuneitemBinding binding =
                LayoutTuneitemBinding
                        .inflate(LayoutInflater.from(parent.getContext()),
                                                        parent,false);
        //create tune view holder object
        TuneViewHolder holder = new TuneViewHolder(binding.getRoot(), binding);

        //return holder object
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull TuneViewHolder holder, int position) {
        holder.itemBinding
                .txtViewTune.setText(adapterTuneList.get(position).getTuneName());
        holder.itemBinding.imgViewTune
                .setImageResource(adapterTuneList.get(position).getTunePic());
        if (position == SelectedInd){
            holder.itemBinding.imgViewPlayPause
                    .setImageResource(R.drawable.pause);
        } else {
            holder.itemBinding.imgViewPlayPause
                    .setImageResource(R.drawable.play);
        }
    }

    @Override
    public int getItemCount() {
        return adapterTuneList.size(); //size of the adapter data
    }

    public class TuneViewHolder extends RecyclerView.ViewHolder {
        LayoutTuneitemBinding itemBinding;

        public TuneViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        public TuneViewHolder(@NonNull View itemView,
                              LayoutTuneitemBinding itemBinding) {
            super(itemView);
            this.itemBinding = itemBinding;
            //click listeners for the binding object
            this.itemBinding
                    .imgViewPlayPause
                    .setOnClickListener((View view) -> {
                        if (SelectedInd !=
                                getBindingAdapterPosition()){
                            SelectedInd = getBindingAdapterPosition();
                            notifyDataSetChanged();
                        } else {
                            SelectedInd = -1;
                            notifyDataSetChanged();
                        }
                    });
        }
    }
}
