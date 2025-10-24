package com.example.asteroid;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.asteroid.adapters.AsteroidAdapter;
import com.example.asteroid.databinding.ActivityMainBinding;
import com.example.asteroid.model.Asteroid;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    List<String> AsteroidNames = new ArrayList<>(Arrays.asList("Ceres","Abtoliy","George","Ivan",
            "Palas","Vesta"));
    List<Integer> AsteroidPics = new ArrayList<>(Arrays.asList(R.drawable.ceres,R.drawable.abtoliy,R.drawable.george,
            R.drawable.ivan,R.drawable.palas,R.drawable.vesta));
    List<Asteroid> asteroidList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

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
        binding.recyclerViewAsteroid.setLayoutManager(lm);

        AsteroidAdapter asteroidAdapter = new AsteroidAdapter(asteroidList);
        binding.recyclerViewAsteroid.setAdapter(asteroidAdapter);

        ItemTouchHelper helper = getItemTouchHelper(asteroidAdapter);
        helper.attachToRecyclerView(binding.recyclerViewAsteroid);
    }

    private ItemTouchHelper getItemTouchHelper(AsteroidAdapter asteroidAdapter) {
        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN,
                        ItemTouchHelper.START | ItemTouchHelper.END) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        Collections.swap(asteroidList,viewHolder.getAdapterPosition(),
                                target.getAdapterPosition());
                        asteroidAdapter.notifyItemMoved(
                                viewHolder.getAdapterPosition(),
                                target.getAdapterPosition()
                        );
                        return true;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        if (direction == ItemTouchHelper.START){
                            asteroidList.remove(viewHolder.getAdapterPosition());
                            asteroidAdapter.notifyItemRemoved(viewHolder.getAdapterPosition());
                        } else if(direction == ItemTouchHelper.END) {
                            asteroidList.get(position).setTrackingStatus("Good");
                            asteroidAdapter.notifyItemChanged(position);
                        }


                    }
                };
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        return helper;
    }

    private void LoadModel() {

        for(int i = 0; i < AsteroidNames.size(); i++){
            Asteroid eachAsteroid = new Asteroid(AsteroidPics.get(i),AsteroidNames.get(i));
            asteroidList.add(eachAsteroid);
        }
    }
}