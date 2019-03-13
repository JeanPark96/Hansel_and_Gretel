package com.example.jean.mapcanvas;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by i on 2019-01-17.
 */

public class StartScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("");
        setContentView(R.layout.activity_startscreen);
    }

    public void onClick(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(StartScreenActivity.this);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                Intent intent = new Intent(StartScreenActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
        alert.setMessage("                              <  경고문  >\n\n시작 이후 디바이스를 휴대한 위치가 변경될 경우, 경로 기록이 정확하지 않을 수 있습니다.\n\n(ex) 디바이스를 손에 들고 걸었다가 주머니에 넣게 될 경우)");
        alert.show();
    }
}
