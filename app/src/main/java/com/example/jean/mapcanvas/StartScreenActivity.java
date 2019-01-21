package com.example.jean.mapcanvas;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

/**
 * Created by i on 2019-01-17.
 */

public class StartScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startscreen);
    }

    public void onClick(View view){

        AlertDialog.Builder alert = new AlertDialog.Builder(StartScreenActivity.this);
        alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                Intent intent = new Intent(StartScreenActivity.this, ListScreenActivity.class);
                startActivity(intent);
            }
        });
        alert.setMessage("dnpajdkfbkxjch lzsdkhfksd");
        alert.show();
    }
}
