package com.example.sharedpreferencesapplicationlevel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class SecondActivity extends AppCompatActivity {
    private TextView txvName, txvMajor, txvID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        txvName = findViewById(R.id.txvName);
        txvMajor = findViewById(R.id.txvMajor);
        txvID = findViewById(R.id.txvID);

    }

    public void removeStudentMajor(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("major");
        editor.apply();
    }
    public void clearData(View view) {
        SharedPreferences sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

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
}
