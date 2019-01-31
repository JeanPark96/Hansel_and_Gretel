package com.example.jean.mapcanvas;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

/**
 * Created by i on 2019-01-25.
 */

public class PathInfoModification extends AppCompatActivity{
    private DBhelper dbHelper;
    int id = 0;
    EditText name_editText, date_editText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pathinfomodification);

        name_editText = (EditText)findViewById(R.id.name_delete);
        date_editText = (EditText)findViewById(R.id.date_delete);
        dbHelper = new DBhelper(this);
        Bundle extras=getIntent().getExtras();

        if(extras!=null){
            int value=extras.getInt("row_id");
            if(value > 0){
                Cursor rs = dbHelper.getData(value);
                id = value;
                rs.moveToFirst();
                String n = rs.getString(rs.getColumnIndex(DBhelper.PATH_NAME));
                String d = rs.getString(rs.getColumnIndex(DBhelper.PATH_DATE));
                if(!rs.isClosed()){
                    rs.close();
                }

                name_editText.setText((CharSequence)n);
                date_editText.setText((CharSequence)d);
            }
        }
    }

    public void modify(View view){
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            int value=extras.getInt("row_id");
            String pathName=extras.getString("pathName");
            if(value>0){
                if(dbHelper.updateDB(id,name_editText.getText().toString(),date_editText.getText().toString(),pathName)){
                    Toast.makeText(getApplicationContext(),"수정되었습니다.",Toast.LENGTH_SHORT).show();
                    Intent intent=new Intent(getApplicationContext(), ListScreenActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        }
    }

    public void delete(View view){
        Bundle extras=getIntent().getExtras();
        if(extras!=null){
            int value=extras.getInt("row_id");
            if(value>0){
                dbHelper.deleteDB(id);
                Toast.makeText(getApplicationContext(),"삭제되었습니다.",Toast.LENGTH_SHORT).show();
                Intent intent=new Intent(getApplicationContext(), ListScreenActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }
}
