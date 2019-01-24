package com.example.jean.mapcanvas;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by i on 2019-01-17.
 */

public class ListScreenActivity extends AppCompatActivity {
    ListView listView;
    ImageButton shareButton, folderAddButton, beginButton;

    String[] path_name = {"한국", "미국", "일본"};
    String[] path_date = {"2019.01.13", "2018.11.21", "2017.03.19"};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listscreen);

        shareButton = findViewById(R.id.shareButton);
        folderAddButton = findViewById(R.id.folderAddButton);
        beginButton = findViewById(R.id.beginButton);

        CustomList adapter = new CustomList(ListScreenActivity.this);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(adapter);
    }

    public void beginPath(View view){

        //팝업 창 띄우기
        AlertDialog.Builder ad = new AlertDialog.Builder(ListScreenActivity.this);
        final String TAG = "Test_Alert_Dialog";

        ad.setTitle("NEW");       // 제목 설정
        ad.setMessage("경로명");   // 내용 설정
        // EditText 삽입하기
        final EditText et = new EditText(ListScreenActivity.this);
        ad.setView(et, 50, 0, 50, 0);

        // 확인 버튼 설정
        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG, "Yes Btn Click");

                // Text 값 받아서 로그 남기기
                String value = et.getText().toString();
                Log.v(TAG, value);

                dialog.dismiss();     //닫기

                // Event
                Intent intent = new Intent(ListScreenActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        // 취소 버튼 설정
        ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG,"No Btn Click");
                dialog.dismiss();     //닫기
                // Event
            }
        });

        // 창 띄우기
        ad.show();
    }

    public class CustomList extends ArrayAdapter<String> {
        private final Activity context;

        public CustomList(Activity context){
            super(context, R.layout.list, path_name);
            this.context = context;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent){
            LayoutInflater inflater = context.getLayoutInflater();
            View rowView = inflater.inflate(R.layout.list, null, true);
            ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
            TextView name = (TextView) rowView.findViewById(R.id.path_name);
            TextView  date = (TextView) rowView.findViewById(R.id.path_date);

            name.setText(path_name[position]);
            date.setText(path_date[position]);

            return rowView;
        }
    }
}
