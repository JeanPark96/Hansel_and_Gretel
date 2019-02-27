package com.example.jean.mapcanvas;

/**
 * Created by i on 2019-02-11.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class ImageDBhelper extends SQLiteOpenHelper {
    //Database Name
    private static final String DATABASE_NAME="path_image.db";
    private static final int DATABASE_VERSION=2;
    //Table Name
    private static final String DATABASE_TABLE="image_table";
    //Column Name
    public static final String PATH_IMAGE="image";
    public static final String PATH_AVAILABLE_NUM="path_available";
    public static final String PATH_FIRST_AZIMUTH="first_azimuth";
    public static final String PATH_LAST_AZIMUTH="last_azimuth";
    public static final String PATH_LAST_POSITION_X="path_position_x";
    public static final String PATH_LAST_POSITION_Y="path_position_y";

    public  ImageDBhelper(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS path_image ( "+" row_id INTEGER PRIMARY KEY AUTOINCREMENT,"+"path_available INTEGER,"+" image BLOB,"+"first_azimuth REAL,"+"last_azimuth REAL,"+"path_position_x REAL,"+"path_position_y REAL);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS path_image");
        onCreate(db);
    }

    public boolean updateDB(int id,int path_available,byte[] image,float fisrt_azimuth,float last_azimuth, float path_position_x, float path_position_y){
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues content=new ContentValues();
        content.put(PATH_IMAGE,image);
        content.put(PATH_AVAILABLE_NUM,path_available);
        content.put(PATH_FIRST_AZIMUTH, fisrt_azimuth);
        content.put(PATH_LAST_AZIMUTH, last_azimuth);
        content.put(PATH_LAST_POSITION_X, path_position_x);
        content.put(PATH_LAST_POSITION_Y, path_position_y);
        db.update("path_image",content,"row_id=?",new String[]{Integer.toString(id)});
        return true;
    }

    public Integer deleteDB(int id){
        SQLiteDatabase db=this.getWritableDatabase();
        return db.delete("path_image","row_id=?",new String[]{Integer.toString(id)});
    }

    public ArrayList<ImageInfo> getAllData(){
        ArrayList<ImageInfo> image_list=new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor res=db.rawQuery("select * from path_image",null);
        res.moveToFirst();
        while(res.isAfterLast()==false){
            int id=res.getInt(0);
            int path_available=res.getInt(1);
            byte[] image=res.getBlob(2);
            float first_azimuth=res.getFloat(3);
            float last_azimuth=res.getFloat(4);
            float path_position_x=res.getFloat(5);
            float path_position_y=res.getFloat(6);
            ImageInfo newImage=new ImageInfo(id,path_available,image,first_azimuth,last_azimuth,path_position_x,path_position_y);
            image_list.add(newImage);
            res.moveToNext();
        }
        return image_list;
    }

    public Cursor getData(int row_id){
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor res=db.rawQuery("SELECT * FROM path_image WHERE row_id="+row_id+"",null);
        return res;
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        return byteArray;
    }

    public static Bitmap retrieveImage(byte[] image) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        return bitmap;
    }
}



