package com.example.sharedpreferencesapplicationlevel;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;


import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class MainActivity extends AppCompatActivity {
    private EditText etName, etMajor, etId;
    private TextView txvName, txvMajor, txvID;
    private Switch pageColorSwitch;
    private LinearLayout pageLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        etName = findViewById(R.id.etName);
        etMajor = findViewById(R.id.etMajor);
        etId = findViewById(R.id.etId);
        txvName = findViewById(R.id.txvName);
        txvMajor = findViewById(R.id.txvMajor);
        txvID = findViewById(R.id.txvID);

        pageLayout = findViewById(R.id.pageLayout);
        pageColorSwitch = findViewById(R.id.PageColorSwitch);
        pageColorSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            setPageColor(isChecked);
        });

        SharedPreferences sharedPreferences = getPreferences( Context.MODE_PRIVATE);
        boolean isChecked = sharedPreferences.getBoolean("yellow", false);
        pageColorSwitch.setChecked(isChecked);


    }

    private void setPageColor(boolean isChecked) {
        SharedPreferences sharedPreferences = getPreferences( Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("yellow", isChecked);
        editor.apply();

        pageLayout.setBackgroundColor(isChecked ? Color.YELLOW : Color.WHITE);
    }


    public void saveData(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("name", etName.getText().toString());
        editor.putString("major", etMajor.getText().toString());
        editor.putString("id", etId.getText().toString());
        editor.apply(); // editot.commit();
    }


    public void loadData(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        String name = sharedPreferences.getString("name", "Name is not available");
        String major = sharedPreferences.getString("major", "Major is not available");
        String id = sharedPreferences.getString("id", "ID is not available");
        txvName.setText(name);
        txvMajor.setText(major);
        txvID.setText(id);
    }
    public void openSecondActivity(View view) {
        Intent intent = new Intent(this, SecondActivity.class);
        startActivity(intent);
    }
}
