package com.example.planets;

import android.os.Bundle;
import android.util.Log;
import android.widget.GridLayout;
import android.widget.LinearLayout;

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

import com.example.planets.adapters.PlanetAdapter;
import com.example.planets.databinding.ActivityMainBinding;
import com.example.planets.model.Planet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<String> PlanetNames = new ArrayList<>(Arrays.asList("Earth","Jupiter","Mars",
            "Mercury","Saturn","Uranus"));
    List<Integer> PlanetPics = new ArrayList<>(Arrays.asList(R.drawable.earth,
            R.drawable.jupiter,R.drawable.mars,R.drawable.mercury,R.drawable.saturn,R.drawable.uranus));
    List<Planet> planetList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
//        setContentView(R.layout.activity_main);

        ActivityMainBinding binding = ActivityMainBinding
                .inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        LoadModel();
        LinearLayoutManager lm = new LinearLayoutManager(MainActivity.this);
        binding.recyclerViewPlanet.setLayoutManager(lm);
//        GridLayoutManager gm = new GridLayoutManager(MainActivity.this,2);
//        binding.recyclerViewPlanet.setLayoutManager(gm);


        PlanetAdapter planetAdapter = new PlanetAdapter(planetList);
        binding.recyclerViewPlanet.setAdapter(planetAdapter);

        ItemTouchHelper helper = getItemTouchHelper(planetAdapter);
        helper.attachToRecyclerView(binding.recyclerViewPlanet);

    }

    private ItemTouchHelper getItemTouchHelper(PlanetAdapter planetAdapter) {
        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(
                        ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.START | ItemTouchHelper.END
                ) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        Log.d("RECYCLERVIEWDEMO","Drag detected "
                                + "Source Ind = " + viewHolder.getAdapterPosition()
                                + "Target Ind = " + target.getAdapterPosition());
                        Collections.swap(planetList,viewHolder.getAdapterPosition(),
                                target.getAdapterPosition());
                        planetAdapter.notifyItemMoved(
                                viewHolder.getAdapterPosition(),
                                target.getAdapterPosition());
                        return true;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        if(direction == ItemTouchHelper.START) {
                            planetList.remove(viewHolder.getAdapterPosition());
                            planetAdapter.notifyItemRemoved(
                                    viewHolder.getAdapterPosition()
                            );
                        } else if (direction ==ItemTouchHelper.END){
                            planetList.get(position).setStatus("Explored");
                            planetAdapter.notifyItemChanged(position);
                        }
                    }
                };
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        return helper;
    }
    private void LoadModel(){
        for (int i = 0; i < PlanetNames.size();i++){
            Planet eachPlanet = new Planet(PlanetPics.get(i), PlanetNames.get(i));
            planetList.add(eachPlanet);
        }
    }
}