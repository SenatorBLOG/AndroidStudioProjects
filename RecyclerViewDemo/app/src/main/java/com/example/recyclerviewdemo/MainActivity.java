package com.example.recyclerviewdemo;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recyclerviewdemo.adapters.TuneAdapter;
import com.example.recyclerviewdemo.databinding.ActivityMainBinding;
import com.example.recyclerviewdemo.model.Tune;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    List<String> TuneNames = new ArrayList<>
                                (Arrays.asList("Beauty and The Beast",
                                        "Game of Thrones", "Lion King",
                                        "Mary Poppins", "Ozark"));
    List<Integer> TunePics = new ArrayList<>
                            (Arrays.asList(R.drawable.beauty,
                                    R.drawable.gameofthrones, R.drawable.lionking,
                                    R.drawable.marypoppins,R.drawable.ozark));
    List<Tune> TuneList = new ArrayList<>(); //empty list


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        //setContentView(R.layout.activity_main);

        ActivityMainBinding binding = ActivityMainBinding
                                        .inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        LoadModel();
        Log.d("RECYCLERVIEWDEMO",TuneList.size() + " Items in the list");

        LinearLayoutManager lm =
                new LinearLayoutManager(MainActivity.this);
        binding.recyclerViewTunes.setLayoutManager(lm);

        TuneAdapter tuneAdapter = new TuneAdapter(TuneList);
        binding.recyclerViewTunes.setAdapter(tuneAdapter);

        //GridLayoutManager gm = new GridLayoutManager
        //                        (MainActivity.this,
        //                                2);
        binding.recyclerViewTunes.setLayoutManager(lm);

        ItemTouchHelper helper = getItemTouchHelper(tuneAdapter);
        helper.attachToRecyclerView(binding.recyclerViewTunes);


    }

    //create helper object and return it
    private ItemTouchHelper
            getItemTouchHelper(TuneAdapter tuneAdapter){

        //create simple call back object
        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                Log.d("RECYCLERVIEWDEMO","Drag detected "
                        + "Source Ind = " + viewHolder.getBindingAdapterPosition()
                        + "Target Ind = " + target.getBindingAdapterPosition());
                Collections.swap(TuneList,
                        viewHolder.getBindingAdapterPosition(),
                        target.getBindingAdapterPosition());
                tuneAdapter.notifyItemMoved(
                        viewHolder.getBindingAdapterPosition(),
                        target.getBindingAdapterPosition());

                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if (direction == ItemTouchHelper.START) {
                    Log.d("RECYCLERVIEWDEMO","Left Swiped at index "
                            + viewHolder.getBindingAdapterPosition());
                    TuneList.remove(viewHolder.getBindingAdapterPosition());
                    tuneAdapter
                            .notifyItemRemoved(
                                    viewHolder.getBindingAdapterPosition());
                } else if (direction == ItemTouchHelper.END){
                    String upperCaseTuneName = TuneList.get(viewHolder.getBindingAdapterPosition())
                                                                .getTuneName().toUpperCase();
                    TuneList.get(viewHolder.getBindingAdapterPosition()).setTuneName(upperCaseTuneName);
                    tuneAdapter.notifyItemChanged(viewHolder.getBindingAdapterPosition());
                }
            }
        };
        //create helper using call back object
        ItemTouchHelper helper = new ItemTouchHelper(callback);

        //return helper
        return helper;
    }

    private void LoadModel(){
        for (int i = 0; i < TuneNames.size();i++){
            Tune eachTune = new Tune(TuneNames.get(i), TunePics.get(i));
            TuneList.add(eachTune);
        }
    }
}