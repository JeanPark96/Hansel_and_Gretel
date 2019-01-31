package com.example.jean.mapcanvas;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by i on 2019-01-17.
 */

public class ListScreenActivity extends AppCompatActivity {
    ListView listView;
    ImageButton shareButton, folderAddButton, beginButton;
    EditText pathName_editText;
    long now = System.currentTimeMillis();
    Date currentDate = new Date(now);
    AlertDialog.Builder delete_ad, begin_ad;
    String listPathName;
    private DBhelper helper;
    SQLiteDatabase db;
    CustomList myList;
    ArrayList<PathInfo> pathList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listscreen);

        shareButton = findViewById(R.id.shareButton);
        folderAddButton = findViewById(R.id.folderAddButton);
        beginButton = findViewById(R.id.beginButton);
        pathName_editText = new EditText(ListScreenActivity.this);
        delete_ad = new AlertDialog.Builder(ListScreenActivity.this);
        begin_ad = new AlertDialog.Builder(ListScreenActivity.this);

        helper = new DBhelper(this);
        db = helper.getWritableDatabase();

        pathList=helper.getAllData();
        myList=new CustomList(pathList,this);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(myList);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                PathInfo info= (PathInfo) ((CustomList)adapterView.getAdapter()).getItem(position);
                final int id=info.getId();
                final String name=info.getName(),date=info.getName(),pathName=info.getPathName();

                Toast.makeText(getApplicationContext(),"id:"+id,Toast.LENGTH_SHORT).show();
                final Bundle data=new Bundle();
                data.putInt("row_id",id);
                data.putString("name",name);
                data.putString("date",date);
                data.putString("pathName",pathName);
                //data.putString("pathName", listPathName);

                final String TAG = "Modify_Alert_Dialog";
                delete_ad.setTitle("MODIFY");
                delete_ad.setMessage("수정 / 삭제 하시겠습니까?");

                delete_ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG, "Yes Btn Click");
                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(),PathInfoModification.class);
                        intent.putExtras(data);
                        startActivity(intent);
                    }
                });

                delete_ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG,"No Btn Click");
                        dialog.dismiss();
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        Bundle data2=new Bundle();
                        data2.putInt("row_id",id);
                        data2.putString("name",name);
                        data2.putString("date",date);
                        data2.putString("pathName",pathName);
                        int pathImgAvailable=1;
                        data2.putInt("pathImgAvailable",pathImgAvailable);
                        intent.putExtras(data2);
                        startActivity(intent);
                    }
                });

                delete_ad.show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        listView.setAdapter(null);
        pathList=helper.getAllData();
        myList=new CustomList(pathList,this);
        listView.setAdapter(myList);
    }

    @SuppressLint("RestrictedApi")
    public void beginPath(View view){
        final String TAG = "Begin_Alert_Dialog";
        begin_ad.setTitle("NEW");       // 제목 설정
        begin_ad.setMessage("경로명");   // 내용 설정
        begin_ad.setView(pathName_editText, 50, 0, 50, 0);

        begin_ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG, "Yes Btn Click");
                String value = pathName_editText.getText().toString();
                Log.v(TAG, value);
                dialog.dismiss();

                String name=pathName_editText.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String date = sdf.format(currentDate);

                String tempPathName=null;
                db.execSQL("INSERT INTO path VALUES(null,'"+name+"','"+date+"','"+tempPathName+"');");

                Intent intent = new Intent(ListScreenActivity.this, MainActivity.class);
                Bundle data=new Bundle();
                data.putInt("pathImgAvailable",0);
                startActivity(intent);
            }
        });

        begin_ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG,"No Btn Click");
                dialog.dismiss();
            }
        });

        begin_ad.show();
    }

    public class CustomList extends BaseAdapter {
        private Context context;
        private ArrayList<PathInfo> pathInfoList;

        public CustomList(ArrayList<PathInfo> list,Context context){
            this.pathInfoList=list;
            this.context=context;
        }

        @Override
        public int getCount() {
            return this.pathInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return this.pathInfoList.get(position);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if(convertView==null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list, null, true);
                TextView names = (TextView) convertView.findViewById(R.id.path_name);
                TextView dates = (TextView) convertView.findViewById(R.id.path_date);
                TextView filepath =(TextView) convertView.findViewById(R.id.path_file);
                PathInfo pathInfo = pathInfoList.get(position);
                names.setText(pathInfo.getName());
                dates.setText(pathInfo.getDate());
                filepath.setText(pathInfo.getPathName());
            }
            return convertView;
        }
    }
}
