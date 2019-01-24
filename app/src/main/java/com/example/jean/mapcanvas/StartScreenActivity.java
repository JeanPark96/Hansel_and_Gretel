package com.example.jean.mapcanvas;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();

                Intent intent = new Intent(StartScreenActivity.this, ListScreenActivity.class);
                startActivity(intent);
                finish();
            }
        });
        alert.setMessage("경고창");
        alert.show();
    }
}
