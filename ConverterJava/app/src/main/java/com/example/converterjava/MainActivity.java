package com.example.converterjava;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    public void Convert(View view) {
        EditText editText = findViewById(R.id.editTextNumberDecimal);
        TextView textView = findViewById(R.id.textView);

        String input = editText.getText().toString();
        if (input.isEmpty()) {
            textView.setText("Please enter a number");
            return;
        } else {
            Float euro = Float.parseFloat(input) * 0.85f;
            textView.setText(euro.toString());
        }
    }
}