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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by i on 2019-01-17.
 */

public class ListScreenActivity extends AppCompatActivity {
    ListView listView;
    ImageButton shareButton, folderAddButton, beginButton, helpButton;
    EditText pathName_editText;
    long now = System.currentTimeMillis();
    Date currentDate = new Date(now);
    AlertDialog.Builder delete_ad, begin_ad, help_ad;

    private DBhelper helper; ImageDBhelper IMGhelper;
    SQLiteDatabase db, imgdb;
    CustomList myList;
    ArrayList<PathInfo> pathList;
    ArrayList<ImageInfo> path_imageList;
    Intent temp_intent;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listscreen);

        shareButton = findViewById(R.id.shareButton);
        folderAddButton = findViewById(R.id.folderAddButton);
        beginButton = findViewById(R.id.beginButton);
        helpButton = findViewById(R.id.helpButton);
        pathName_editText = new EditText(ListScreenActivity.this);
        delete_ad = new AlertDialog.Builder(ListScreenActivity.this);
        begin_ad = new AlertDialog.Builder(ListScreenActivity.this);
        help_ad = new AlertDialog.Builder(ListScreenActivity.this);

        helper = new DBhelper(this);
        db = helper.getWritableDatabase();
        IMGhelper = new ImageDBhelper(this);
        imgdb = IMGhelper.getWritableDatabase();

        path_imageList=IMGhelper.getAllData();
        pathList=helper.getAllData();

        myList=new CustomList(pathList,this);

        listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(myList);

        final Bundle extras=getIntent().getExtras();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                PathInfo info= (PathInfo) ((CustomList)adapterView.getAdapter()).getItem(position);
                final int id=info.getId();
                final Bundle data=new Bundle();
                data.putInt("row_id",id);

                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                //Toast.makeText(getApplicationContext(),"imgid:"+id,Toast.LENGTH_LONG).show();
                intent.putExtras(data);
                startActivity(intent);
            }
        });

        listView.setOnItemLongClickListener(new ListViewItemLongClickListener());
    }

    class ListViewItemLongClickListener implements AdapterView.OnItemLongClickListener{
        @Override
        public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long l){
            PathInfo info= (PathInfo) ((CustomList)adapterView.getAdapter()).getItem(position);
            final int id = info.getId();
            final Bundle pathInfoModificationBundle=new Bundle();
            pathInfoModificationBundle.putInt("row_id",id);

            final String TAG = "Modify_Alert_Dialog";
            delete_ad.setTitle("MODIFY");
            delete_ad.setMessage("수정 / 삭제 하시겠습니까?");

            delete_ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.v(TAG, "Yes Btn Click");
                    dialog.dismiss();
                    Intent intent = new Intent(getApplicationContext(),PathInfoModification.class);
                    intent.putExtras(pathInfoModificationBundle);
                    startActivity(intent);
                }
            });

            delete_ad.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.v(TAG,"No Btn Click");
                    dialog.dismiss();
                }
            });

            delete_ad.show();
            return true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        listView.setAdapter(null);
        pathList=helper.getAllData();
        path_imageList=IMGhelper.getAllData();
        myList=new CustomList(pathList,this);
        listView.setAdapter(myList);
    }

    public void helpUser(View view){
        final String TAG = "Help_Alert_Dialog";
        help_ad.setTitle("도움말");       // 제목 설정
        help_ad.setMessage("사용법");   // 내용 설정

        help_ad.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG, "Yes Btn Click");

                dialog.dismiss();
            }
        });

        help_ad.show();
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

                String name = pathName_editText.getText().toString();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String date = sdf.format(currentDate);
                db.execSQL("INSERT INTO path VALUES(null,'"+name+"','"+date+"');");

                byte[] image= null;
                int path_available=0;
                float first_azimuth=0;
                float last_azimuth=0;
                float position_x=0;
                float position_y=0;
                imgdb.execSQL("INSERT INTO path_image VALUES(null,'"+path_available+"','"+image+"','"+first_azimuth+"','"+last_azimuth+"','"+position_x+"','"+position_y+"');");
                onResume();
                dialog.dismiss();
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

    public void modifyPath(View view){
        startActivity(temp_intent);
    }

    public class CustomList extends BaseAdapter {
        private Context context;
        private ArrayList<PathInfo> pathInfoList;

        public CustomList(ArrayList<PathInfo> list, Context context){
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
