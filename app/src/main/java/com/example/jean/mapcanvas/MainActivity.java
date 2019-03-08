package com.example.jean.mapcanvas;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.app.Service.START_STICKY;

public class MainActivity extends AppCompatActivity {
    static final float ALPHA = 0.8f; // if ALPHA = 1 OR 0, no filter applies.
    boolean flag = true;
    TextView countText, distanceText;
    ImageButton Btn, closeButton;
    EditText strideText;
    double stride_length=0, numOfStep=0, distance_result=0,radianConst=3.15192/180;
    String orientation_result;
    String filePath;
    int count = StepValue.Step;
    private long lastTime;
    int local_step,n=0;
    int received_row_id;
    private float deltaTotalAcc, x, y, z, azimuth, first_azimuth, last_azimuth, pitch, position_x,position_y,roll,totalAccDiff,lastDeltaTotalAcc,low_peak,high_peak,totalAbsAcc,lastTotalAbsAcc;
    private float customThresholdTotal=0,averageCustomThreshold=0;
    private static double AMPLITUDE_THRESHOLD = 2.1;
    public static boolean newBitmapAvailable=false;
    public static Bitmap newBitmap;
    public static int pathAvailableNumber;
    private int scrollPos=0;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor; //가속도 센서
    private Sensor magneticSensor; //지자기 센서
    private SensorEventListener accL;
    private SensorEventListener magN;
    float[] Gravity;
    float[] Magnetic;

    public static drawCanvas showCanvas;
    ImageView orientationImg;
    public static ImageView Img;
    ImageDBhelper IMGhelper; DBhelper helper;
    ArrayList<ImageInfo> path_imageList; ArrayList<PathInfo> pathList;
    SQLiteDatabase db, imgdb;
    public int imgid = 1;
    ScrollView scrollView; HorizontalScrollView horscrollView;
    AlertDialog.Builder begin_ad;
    EditText pathName_editText;
    long now = System.currentTimeMillis();
    Date currentDate = new Date(now);
    RadioButton man_stride, woman_stride;
    boolean man=false, woman=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("경로 기록");
        setContentView(R.layout.activity_main);

        showCanvas= (drawCanvas) findViewById(R.id.drawing);

        man_stride = (RadioButton)findViewById(R.id.man_stride);
        woman_stride = (RadioButton)findViewById(R.id.woman_stride);

        helper = new DBhelper(this);
        db = helper.getWritableDatabase();
        pathList = helper.getAllData();

        IMGhelper = new ImageDBhelper(this);
        imgdb = IMGhelper.getWritableDatabase();
        path_imageList=IMGhelper.getAllData();

        begin_ad = new AlertDialog.Builder(MainActivity.this);
        pathName_editText = new EditText(MainActivity.this);

        scrollView = (ScrollView)findViewById(R.id.scrollView);
        scrollView.setVerticalScrollBarEnabled(true);
        horscrollView = (HorizontalScrollView)findViewById(R.id.horscrollView);
        horscrollView.setHorizontalScrollBarEnabled(true);

        scrollView.removeCallbacks(verticalScrollDrag);
        scrollView.post(verticalScrollDrag);
        horscrollView.post(horizontalScrollDrag);

        Btn = (ImageButton) findViewById(R.id.start_button);
        closeButton = (ImageButton) findViewById(R.id.close_button);

        countText = (TextView) findViewById(R.id.stepText);
        distanceText = (TextView) findViewById(R.id.distanceText);
        strideText = (EditText) findViewById(R.id.stride_editText);

        orientationImg = (ImageView)findViewById(R.id.orientationImg);
        orientationImg.setRotation(0);
        Img = (ImageView)findViewById(R.id.Img);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accL = new accelerometerListener();
        magN = new magneticListener();

        callPathImageFromDataBase();

        Btn.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("RestrictedApi")
            @Override
            public void onClick(View v) {
                if (flag) {
                    Btn.setImageResource(R.drawable.stop);

                    try {
                        onStartCommand();

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    Btn.setImageResource(R.drawable.start);
                    showCanvas.finished();
                    try {
                        onStop();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                flag = !flag;
            }
        });
    }

    private Runnable verticalScrollDrag=new Runnable() {
        @Override
        public void run(){
                scrollView.smoothScrollBy(0, 1800);
        }
    };

    private Runnable horizontalScrollDrag=new Runnable() {
        @Override
        public void run() {
            horscrollView.smoothScrollBy(600,0);
        }
    };

    @Override
    protected void onStop() {
        super.onStop();
        if (sensorManager != null) {
            sensorManager.unregisterListener(accL);
            sensorManager.unregisterListener(magN);
        }
    }

    public int onStartCommand() {
        super.onStart();
        Log.i("onStartCommand", "IN");
        if (accelerometerSensor != null) {
            sensorManager.registerListener(accL, accelerometerSensor, sensorManager.SENSOR_DELAY_GAME);
        }
        if (magneticSensor != null) {
            sensorManager.registerListener(magN, magneticSensor, sensorManager.SENSOR_DELAY_GAME);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "IN");
        if (sensorManager != null) {
            sensorManager.unregisterListener(accL);
            sensorManager.unregisterListener(magN);
            StepValue.Step = 0; //다시 초기화
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
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
                ContentValues content= new ContentValues();
                content.put(helper.PATH_NAME,name);
                content.put(helper.PATH_DATE,date);
                //db.execSQL("INSERT INTO path VALUES(null,'"+name+"','"+date+"');");
                received_row_id=(int)db.insert(helper.DATABASE_TABLE,null,content);

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

    public void callPathImageFromDataBase() {
        Bundle extra= getIntent().getExtras();
        if(extra!=null){
            int value=extra.getInt("row_id");
            if(value > 0){
                Cursor res = IMGhelper.getData(value);
                imgid = value;
                Toast.makeText(getApplicationContext(),"Main_id:"+imgid,Toast.LENGTH_LONG).show();
                res.moveToFirst();

                int i=res.getInt(res.getColumnIndex(IMGhelper.PATH_AVAILABLE_NUM));
                Toast.makeText(getApplicationContext(),"pathavailable_id:"+i,Toast.LENGTH_LONG).show();

                if(i==1) {
                    Toast.makeText(getApplicationContext(),"삐삐",Toast.LENGTH_LONG).show();
                    pathAvailableNumber=1;
                    newBitmapAvailable=false;
                    byte[] image = res.getBlob(res.getColumnIndex(IMGhelper.PATH_IMAGE));

                    showCanvas.theta = res.getDouble(res.getColumnIndex(IMGhelper.PATH_FIRST_AZIMUTH));
                    showCanvas.endX = res.getFloat(res.getColumnIndex(IMGhelper.PATH_LAST_POSITION_X));
                    showCanvas.endY = res.getFloat(res.getColumnIndex(IMGhelper.PATH_LAST_POSITION_Y));

                    Bitmap tempBitmap=Bitmap.createBitmap(IMGhelper.retrieveImage(image));
                    Bitmap tempBitmap2=tempBitmap.copy(Bitmap.Config.ARGB_8888,true);
                    Img.setImageBitmap(tempBitmap2);
                    newBitmap=showCanvas.setBitmap(tempBitmap2);
                }

                if(!res.isClosed()){
                    res.close();
                }
            }
        }
    }

    private class accelerometerListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            //거리 계산
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Gravity = lowPass(event.values.clone(), Gravity);
                long currentTime = System.currentTimeMillis();
                long gapOfTime = (currentTime - lastTime);

                if (gapOfTime > 100) {
                    n++;

                    Log.i("onSensorChanged_IF", "FIRST_IF_IN");
                    lastTime = currentTime;

                    x = event.values[0];
                    y = event.values[1];
                    z = event.values[2];

                    totalAbsAcc=Math.abs(x)+Math.abs(y)+Math.abs(z);
                    totalAccDiff=(totalAbsAcc-(lastTotalAbsAcc));
                    deltaTotalAcc = totalAccDiff/gapOfTime;

                    if(n==1){
                        low_peak=totalAbsAcc;
                        high_peak=low_peak;
                    }
                    else {
                        if(totalAbsAcc<low_peak){
                            low_peak=totalAbsAcc;
                        }else if(totalAbsAcc>high_peak){
                            high_peak=totalAbsAcc;
                        }
                    }
                    if(high_peak-low_peak>AMPLITUDE_THRESHOLD){
                        if(count==0||(lastDeltaTotalAcc>0 && deltaTotalAcc<0)) {
                            if(StepValue.Step<=5){
                                customThresholdTotal+=totalAccDiff;
                                if(StepValue.Step==5) {
                                    averageCustomThreshold = customThresholdTotal / 5;
                                    if(averageCustomThreshold>AMPLITUDE_THRESHOLD)
                                        AMPLITUDE_THRESHOLD=averageCustomThreshold;
                                }
                            }
                            StepValue.Step = count++;
                            local_step = StepValue.Step;
                            low_peak=high_peak;
                            high_peak=0;
                            printResult();
                        }
                    }

                    lastTotalAbsAcc=totalAbsAcc;
                    lastDeltaTotalAcc=deltaTotalAcc;
                }
            }
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }

    private class magneticListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event){
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                Magnetic = lowPass(event.values.clone(), Magnetic);
            }

            if (Gravity != null && Magnetic != null) {
                float R[] = new float[9];
                float I[] = new float[9];
                boolean success = SensorManager.getRotationMatrix(R, I, Gravity, Magnetic);//?

                if (success) {
                    float orientation[] = new float[3];
                    SensorManager.getOrientation(R, orientation);
                    azimuth = orientation[0];
                    azimuth = (float)(azimuth * 1/radianConst);//범위가 -180~180 사이로 나옴 음수면 서쪽 양수면 동쪽이다
                    orientationImg.setRotation(-azimuth);
                    if(azimuth<0){
                        azimuth+=360;
                    }
                    pitch = (float)Math.toDegrees(orientation[1]);
                    roll = (float)Math.toDegrees(orientation[2]);
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }

    protected float[] lowPass( float[] input, float[] output ) {
        if ( output == null ) return input;

        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }

    public void printResult(){
        countText.setText(Integer.toString(local_step)); //step 수 출력
        numOfStep = Double.parseDouble(countText.getText().toString()); //step 수 변환
        stride_length = Double.parseDouble(strideText.getText().toString()); //보폭 변환
        distance_result = stride_length*numOfStep; //거리 계산
        distanceText.setText(Double.toString(distance_result)); //거리 출력

        if(local_step==1)
            first_azimuth=azimuth;
        moveScrollView();
        showCanvas.drawing(azimuth,local_step);
    }

    public void averageStrideSet(View view){
        switch (view.getId()){
            case R.id.man_stride:
                man = true;
                strideText.setText("0.76");
                break;

            case R.id.woman_stride:
                woman = true;
                strideText.setText("0.67");
                break;
        }
    }

    public void moveScrollView(){
        scrollPos = (int) (scrollView.getScrollY() - 10.0);
        scrollView.scrollTo(0,scrollPos);
        Log.e("moveScrollView","moveScrollView");
    }

    public void backTracking(View view){
        newBitmap = showCanvas.RotateBitmap(newBitmap);
        newBitmapAvailable=true;
        showCanvas.onSizeChanged(showCanvas.getWidth(),showCanvas.getHeight(),showCanvas.getWidth(),showCanvas.getHeight());
        StepValue.Step = 0;
        local_step = 0;
        count = 0;
    }

    @SuppressLint("RestrictedApi")
    public void saveCurrentPath(View v){
        AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
        final String TAG = "Test_Alert_Dialog";
        ad.setTitle("SAVE");

        ad.setNegativeButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.v(TAG,"Yes Btn Click");
                dialog.dismiss();
                byte[] image= IMGhelper.getBytes(showCanvas.drawBitmap);
                last_azimuth = azimuth;
                position_x = showCanvas.endX;
                position_y = showCanvas.endY;
                if(received_row_id!=0)
                    IMGhelper.updateDB(received_row_id,1,image,first_azimuth,last_azimuth,position_x,position_y);
                else
                    IMGhelper.updateDB(imgid,1,image,first_azimuth,last_azimuth,position_x,position_y);
                Toast.makeText(getApplicationContext(),"이미지id:"+imgid,Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(),"바이트:"+image,Toast.LENGTH_LONG).show();
            }
        });
        ad.show();

        filePath = showCanvas.saveBitmap(this.getApplicationContext(), showCanvas.drawBitmap, "NewBitmap");
        Toast.makeText(this.getApplicationContext(), "경로가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        makeNewBitmapFromPath(filePath);
    }

    public void goToList(View view){
        newBitmapAvailable=false;
        newBitmap=null;
        showCanvas.drawBitmap=null;
        pathAvailableNumber=0;

        Intent intent = new Intent(MainActivity.this, ListScreenActivity.class);
        startActivity(intent);
        finish();
    }

    public Bitmap makeNewBitmapFromPath(String filePath){
        BitmapFactory.Options newBitmapOption= new BitmapFactory.Options();
        newBitmap= BitmapFactory.decodeFile(filePath,newBitmapOption);
        newBitmap=Bitmap.createScaledBitmap(newBitmap,showCanvas.getWidth(),showCanvas.getHeight(),false);

        newBitmapAvailable=true;
        return newBitmap;
    }

    public void closeButton(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        alert.setMessage("강제 종료하시겠습니까?");

        alert.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alert.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                newBitmapAvailable=false;
                newBitmap=null;
                showCanvas.drawBitmap=null;
                pathAvailableNumber=0;

                Intent intent = new Intent(MainActivity.this, StartScreenActivity.class);
                startActivity(intent);
                finish();
            }
        });

        alert.show();
    }

    public void guidePath(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater factory = LayoutInflater.from(MainActivity.this);
        view = factory.inflate(R.layout.activity_guide, null);
        alert.setView(view);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }
}