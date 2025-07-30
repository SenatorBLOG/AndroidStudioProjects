package com.example.optionsmenu;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {
    TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textView = findViewById(R.id.textView1);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_setting) {
        textView.setText("Settings");
        Toast.makeText(this, "Settings", Toast.LENGTH_LONG).show();
    } else if (item.getItemId() == R.id.one) {
        textView.setText("One");
        Toast.makeText(this, "One", Toast.LENGTH_LONG).show();
    } else if (item.getItemId() == R.id.two) {
        textView.setText("Two");
        Toast.makeText(this, "Two", Toast.LENGTH_LONG).show();
    } else if (item.getItemId() == R.id.discard) {
        textView.setText("Discard");
        Toast.makeText(this, "Discard", Toast.LENGTH_LONG).show();
    }
    else if (item.getItemId() == R.id.search) {
        textView.setText("Search");
        Toast.makeText(this, "Search", Toast.LENGTH_LONG).show();
    }
    else if (item.getItemId() == R.id.activity_two) {
        Intent intent = new Intent(this, Activity2.class);
        startActivity(intent);
    }

    else if (item.getItemId() == R.id.activity_one) {
        Intent intent = new Intent(this, Activity1.class);
        startActivity(intent);
    }

    else {
        return super.onOptionsItemSelected(item);
    }
    return true;

}
}