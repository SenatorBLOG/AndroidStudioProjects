package com.example.todolistdemo.Utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.todolistdemo.Model.ToDoModel;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private SQLiteDatabase db;
    private static final String DATABASE_NAME = "TODO_DATABASE";
    private static final String TABLE_NAME = "TODO_TABLE";
    private static final String COL1 = "ID";
    private static final String COL2 = "TASK";
    private static final String COL3 = "STATUS";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT, TASK TEXT, STATUS INTEGER)";
        db.execSQL(createTableStatement);

    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropTableStatement = "DROP TABLE IF EXISTS " + TABLE_NAME;
        db.execSQL(dropTableStatement);
        onCreate(db);
    }
    public void insertTask(ToDoModel model){
        db=this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL2,model.getTask());
        values.put(COL3,0);
        db.insert(TABLE_NAME,null,values);
    }
    public void updateTask(int id,String task){
        db=this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL2,task);
        db.update(TABLE_NAME,values,"ID=?",new String[]{String.valueOf(id)});
    }
    public void updateStatus(int id,int status){
        db=this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL3,status);
        db.update(TABLE_NAME,values,"ID=?",new String[]{String.valueOf(id)});
    }
    public void deleteTask(int id){
        db=this.getWritableDatabase();
        db.delete(TABLE_NAME,"ID=?",new String[]{String.valueOf(id)});
    }
    public List<ToDoModel> getAllTasks(){
        db=this.getReadableDatabase();
        Cursor cursor = null;
        List<ToDoModel> modelList = new ArrayList<>();
        try{
            cursor = db.query(TABLE_NAME,null,null,null,null,null,null);
            if(cursor != null){
                if(cursor.moveToFirst()){
                    do{
                        ToDoModel task = new ToDoModel();

                        task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL1)));
                        task.setTask(cursor.getString(cursor.getColumnIndexOrThrow(COL2)));
                        task.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COL3)));
                        modelList.add(task);
                    }while (cursor.moveToNext());
                }
            }
        }finally {

            if (cursor != null) {
                cursor.close();
            }

        }
        return modelList;
    }
}

