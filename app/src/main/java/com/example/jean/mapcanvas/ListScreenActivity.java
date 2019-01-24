package com.example.jean.mapcanvas;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.health.PackageHealthStats;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.nio.file.Path;
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

    private DBhelper helper;
    SQLiteDatabase db;
    CustomList myList;
    ArrayList<PathInfo> pathList;
    int id=1;

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

        helper=new DBhelper(this);
        try{
            db=helper.getWritableDatabase();
        }catch (SQLException ex){
            db=helper.getReadableDatabase();
        }

        pathList=helper.getAllData();
        myList=new CustomList(pathList,this);
        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(myList);

        /*Bundle extras=getIntent().getExtras();
        if(extras!=null){
            int value=extras.getInt("row_id");
            if(value>0){
                Cursor rs=helper.getData(value);
                id=value;
                rs.moveToFirst();
                String n=rs.getString(rs.getColumnIndex(DBhelper.PATH_NAME));
                String d=rs.getString(rs.getColumnIndex(DBhelper.PATH_DATE));
                if(!rs.isClosed()){
                    rs.close();
                }

                pathName_editText.setText((CharSequence)n);
            }
        }*/

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                PathInfo info= (PathInfo) ((CustomList)adapterView.getAdapter()).getItem(position);
                id=info.getId();

                Toast.makeText(getApplicationContext(),"id:"+id,Toast.LENGTH_SHORT).show();
                Bundle data=new Bundle();
                data.putInt("row_id",id);
                /*Intent intent=new Intent(getApplicationContext(),PathInfoModification.class);
                intent.putExtras(data);
                startActivity(intent);*/

                final String TAG = "Delete_Alert_Dialog";
                delete_ad.setTitle("DELETE");       // 제목 설정
                delete_ad.setMessage("삭제하시겠습니까?");   // 내용 설정

                delete_ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG, "Yes Btn Click");
                        /*Bundle extras=getIntent().getExtras();
                        if(extras!=null){
                            int value=extras.getInt("row_id");
                            if(value>0){
                                helper.deleteDB(id);
                                finish();
                            }
                        }*/
                        pathList.remove(id);
                        onResume();
                        dialog.dismiss();
                        Toast.makeText(getApplicationContext(),"삭제되었습니다.",Toast.LENGTH_SHORT).show();
                    }
                });

                delete_ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Log.v(TAG,"No Btn Click");
                        dialog.dismiss();     //닫기
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

    public void beginPath(View view){
        final String TAG = "New_Alert_Dialog";
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
                db.execSQL("INSERT INTO path VALUES(null,'"+name+"','"+date+"');");

                Intent intent = new Intent(ListScreenActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        begin_ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG,"No Btn Click");
                dialog.dismiss();     //닫기
            }
        });

        // 창 띄우기
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

                PathInfo pathInfo = pathInfoList.get(position);
                names.setText(pathInfo.getName());
                dates.setText(pathInfo.getDate());
            }
            return convertView;
        }
    }
}
