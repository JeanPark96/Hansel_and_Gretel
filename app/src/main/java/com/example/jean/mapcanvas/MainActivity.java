package com.example.jean.mapcanvas;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import static android.app.Service.START_STICKY;

public class MainActivity extends AppCompatActivity {
    static final float ALPHA = 0.8f; // if ALPHA = 1 OR 0, no filter applies.
    boolean flag = true;
    TextView countText, distanceText, orientationText;
    ImageButton Btn, closeButton;
    EditText strideText;
    double stride_length=0, numOfStep=0, distance_result=0,radianConst=3.15192/180;
    String orientation_result;
    String filePath;
    int count = StepValue.Step;
    private long lastTime;
    int local_step,n=0;
    private float deltaTotalAcc, x, y, z, azimuth, pitch, roll,totalAccDiff,lastDeltaTotalAcc,low_peak,high_peak,totalAbsAcc,lastTotalAbsAcc;
    private float customThresholdTotal=0,averageCustomThreshold=0;
    private static double AMPLITUDE_THRESHOLD = 2.1;
    public static boolean newBitmapAvailable=false;
    public static Bitmap newBitmap;
    public static int pathAvailableNumber;
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
    //public static byte[] image;
    ImageDBhelper IMGhelper;
    ArrayList<ImageInfo> path_imageList;
    DBhelper dbHelper;
    SQLiteDatabase db, imgdb;
    public int imgid;
    SQLiteStatement p;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showCanvas= (drawCanvas) findViewById(R.id.drawing);

        Btn = (ImageButton) findViewById(R.id.start_button);
        closeButton = (ImageButton) findViewById(R.id.close_button);

        countText = (TextView) findViewById(R.id.stepText);
        distanceText = (TextView) findViewById(R.id.distanceText);
        orientationText = (TextView) findViewById(R.id.orientationText);
        strideText = (EditText) findViewById(R.id.stride_editText);

        orientationImg = (ImageView)findViewById(R.id.orientationImg);
        orientationImg.setRotation(0);
        Img = (ImageView)findViewById(R.id.Img);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accL = new accelerometerListener();
        magN = new magneticListener();

        IMGhelper = new ImageDBhelper(this);
        imgdb = IMGhelper.getWritableDatabase();
        path_imageList=IMGhelper.getAllData();

        Bundle extra= getIntent().getExtras();
        if(extra!=null){
            int value=extra.getInt("row_id");
            if(value > 0){
                Cursor res = IMGhelper.getData(value);
                imgid = value;
                //Toast.makeText(getApplicationContext(),"imgid:"+imgid,Toast.LENGTH_LONG).show();
                res.moveToFirst();

                int i=res.getInt(res.getColumnIndex(IMGhelper.PATH_AVAILABLE_NUM));
                Toast.makeText(getApplicationContext(),"pathavailable_id:"+i,Toast.LENGTH_LONG).show();


                if(i==1) {
                    Toast.makeText(getApplicationContext(),"삐삐",Toast.LENGTH_LONG).show();


                    pathAvailableNumber=1;
                    newBitmapAvailable=false;
                    byte[] image = res.getBlob(res.getColumnIndex(IMGhelper.PATH_IMAGE));

                   // BitmapFactory.Options option=new BitmapFactory.Options();
                   // option.inMutable=true;
                    //Bitmap tempBitmap=BitmapFactory.decodeResource(getResources(),option);
                    Bitmap tempBitmap=Bitmap.createBitmap(IMGhelper.retrieveImage(image));
                    Bitmap tempBitmap2=tempBitmap.copy(Bitmap.Config.ARGB_8888,true);
                    Img.setImageBitmap(tempBitmap2);
                    newBitmap=showCanvas.setBitmap(tempBitmap2);
                    //showCanvas.drawBitmap=IMGhelper.retrieveImage(image);
                    //newBitmap=IMGhelper.retrieveImage(image);
                   // showCanvas.setBitmap(IMGhelper.retrieveImage(image));

                }


                if(!res.isClosed()){
                    res.close();
                }
            }
        }

        Btn.setOnClickListener(new View.OnClickListener() {
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
        //path_imageList=IMGhelper.getAllData();
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

        stride_length = Double.parseDouble(strideText.getText().toString()); //보폭 변환
        numOfStep = Double.parseDouble(countText.getText().toString()); //step 수 변환
        distance_result = stride_length*numOfStep; //거리 계산
        distanceText.setText(Double.toString(distance_result)); //거리 출력

        orientation_result = "Azimuth:"+azimuth+"\n"+"Pitch:"+pitch+"\n"+"Roll:"+roll;
        orientationText.setText(orientation_result); //방향 출력

        showCanvas.drawing(azimuth,local_step);
    }

    public void backTracking(View view){
        newBitmap = showCanvas.RotateBitmap(newBitmap);
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
                IMGhelper.updateDB(imgid,1,image);
                Toast.makeText(getApplicationContext(),"이미지id:"+imgid,Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(),"바이트:"+image,Toast.LENGTH_LONG).show();
            }
        });
        ad.show();

        filePath = showCanvas.saveBitmap(this.getApplicationContext(), showCanvas.drawBitmap, "NewBitmap");
        Toast.makeText(this.getApplicationContext(), "경로가 저장되었습니다.", Toast.LENGTH_SHORT).show();
        makeNewBitmapFromPath(filePath);
        //pathAvailableNumber = 1;
    }

    public Bitmap makeNewBitmapFromPath(String filePath){
        BitmapFactory.Options newBitmapOption= new BitmapFactory.Options();
        newBitmap= BitmapFactory.decodeFile(filePath,newBitmapOption);
        newBitmap=Bitmap.createScaledBitmap(newBitmap,showCanvas.getWidth(),showCanvas.getHeight(),false);

        newBitmapAvailable=true;
       // newBitmap = showCanvas.RotateBitmap(newBitmap);
        return newBitmap;
    }

    public void closeButton(View view){
        AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
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
                Intent intent = new Intent(MainActivity.this, ListScreenActivity.class);
                startActivity(intent);
                finish();
            }
        });

        alert.setMessage("강제 종료하시겠습니까?");
        alert.show();
    }
}