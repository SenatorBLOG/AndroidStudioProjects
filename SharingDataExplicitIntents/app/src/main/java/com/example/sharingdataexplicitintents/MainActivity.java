package com.example.sharingdataexplicitintents;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    private Button submitBtn;
    private EditText nameEditText;
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
        submitBtn = findViewById(R.id.submitBtn);
        nameEditText = findViewById(R.id.nameEditText);

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               /* Intent intent = new Intent( MainActivity.this,SecondActivity.class);


                Bundle b = new Bundle();
                b.putString("name",nameEditText.getText().toString());
                intent.putExtras(b);

                startActivity(intent);*/
                passDataFirstMethod();
                //passDataSecondMethod();

            }
        });

    }
    private void passDataFirstMethod(){
        Intent intent = new Intent( MainActivity.this,SecondActivity.class);


        Bundle b = new Bundle();
        b.putString("name",nameEditText.getText().toString());
        intent.putExtras(b);

        startActivity(intent);
    }
    private void passDataSecondMethod(){
        Intent intent = new Intent( MainActivity.this,SecondActivity.class);
        intent.putExtra("name",nameEditText.getText().toString());
        startActivity(intent);
    }
}