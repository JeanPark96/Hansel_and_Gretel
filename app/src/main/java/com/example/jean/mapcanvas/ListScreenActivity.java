package com.example.jean.mapcanvas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

/**
 * Created by i on 2019-01-17.
 */

public class ListScreenActivity extends AppCompatActivity{
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
        Intent intent = new Intent(ListScreenActivity.this, MainActivity.class);
        startActivity(intent);
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
