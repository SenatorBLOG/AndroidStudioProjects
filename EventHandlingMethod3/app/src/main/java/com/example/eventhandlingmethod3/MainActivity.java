package com.example.eventhandlingmethod3;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private ConstraintLayout constraintLayout;
    private String TAG = MainActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ConstraintLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        constraintLayout = findViewById(R.id.ConstraintLayout);
    }

    public void ChangeToCyan(View view) {
        constraintLayout.setBackgroundColor(Color.CYAN);
        Log.e(TAG, "ChangeToCyan: ");
    }
    public void ChangeToYellow(View view) {
        constraintLayout.setBackgroundColor(Color.YELLOW);
        Log.e(TAG, "ChangeToYellow: ");
    }
}