package com.example.jean.mapcanvas;

/**
 * Created by i on 2019-01-24.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;

public class DBhelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="path.db";
    private static final int DATABASE_VERSION=2;
    public static final String PATH_NAME="name";
    public static final String PATH_DATE="date";

    public  DBhelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS path ( "+" row_id INTEGER PRIMARY KEY AUTOINCREMENT,"+" name TEXT, date TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS path");
        onCreate(db);
    }

    public boolean updateDB(int id,String name,String date){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues content=new ContentValues();
        content.put("name",name);
        content.put("date",date);
        db.update("path",content,"row_id=?",new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteDB(int id){
        SQLiteDatabase db=this.getWritableDatabase();
        return db.delete("path","row_id=?",new String[]{Integer.toString(id)});
    }

    public ArrayList<PathInfo> getAllData(){
        ArrayList<PathInfo> path_list=new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor res=db.rawQuery("select * from path",null);
        res.moveToFirst();
        while(res.isAfterLast()==false){
            int id=res.getInt(0);
            String name=res.getString(1);
            String date=res.getString(2);
            PathInfo newPath=new PathInfo(id,name,date);
            path_list.add(newPath);
            res.moveToNext();
        }
        return path_list;
    }

    public Cursor getData(int row_id){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor res=db.rawQuery("SELECT * FROM path WHERE row_id="+row_id+"",null);
        return res;
    }
}


