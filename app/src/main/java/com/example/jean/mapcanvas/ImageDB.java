package com.example.jean.mapcanvas;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

/**
 * Created by jean on 27/01/2019.
 */

public class ImageDB extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="pathImage.db";
    private static final int DATABASE_VERSION=2;
    public static final String IMAGE_NAME="img_name";

    public  ImageDB(Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS pathImage ( "+" row_id INTEGER PRIMARY KEY AUTOINCREMENT,"+" name TEXT, date TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS path");
        onCreate(db);
    }

    public static byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    // convert from byte array to bitmap
    public static Bitmap getPhoto(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}
