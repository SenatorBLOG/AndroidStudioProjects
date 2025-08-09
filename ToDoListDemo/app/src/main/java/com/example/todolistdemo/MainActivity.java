package com.example.todolistdemo;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.todolistdemo.Adapter.ToDoAdapter;
import com.example.todolistdemo.Model.ToDoModel;
import com.example.todolistdemo.Utils.DatabaseHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity implements onDialogCloseListener {

    private RecyclerView mRecyclerView;
    private FloatingActionButton fab;
    private DatabaseHelper myDb;
    private ToDoAdapter adapter;
    private List<ToDoModel> mList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.recyclerView);
        fab = findViewById(R.id.fab);
        myDb = new DatabaseHelper(this);
        mList = new ArrayList<>();
        adapter = new ToDoAdapter(myDb,this);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setAdapter(adapter);
        mList = myDb.getAllTasks();
        Collections.reverse(mList);
        adapter.setTasks(mList);


        fab.setOnClickListener(view -> {
            AddNewTask.newInstance().show(getSupportFragmentManager(),AddNewTask.TAG);
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new RecyclerViewTouchHelper(adapter));
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void onDialogClose(DialogInterface dialogInterface) {
        mList = myDb.getAllTasks();
        Collections.reverse(mList);
        adapter.setTasks(mList);
        adapter.notifyDataSetChanged();

    }
}