package com.example.eventhandlingmethod1;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private ConstraintLayout ConstraintLayout;
    private String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        ConstraintLayout = findViewById(R.id.ConstraintLayout);

        Button btn1 = findViewById(R.id.btn1);
        btn1.setOnClickListener(this);
        Button btn2 = findViewById(R.id.btn2);
        btn2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        int id = v.getId();
        if (id == R.id.btn1) {
            ConstraintLayout.setBackgroundColor(Color.RED);
        } else if (id == R.id.btn2) {
            ConstraintLayout.setBackgroundColor(Color.GREEN);
        }
    }
}