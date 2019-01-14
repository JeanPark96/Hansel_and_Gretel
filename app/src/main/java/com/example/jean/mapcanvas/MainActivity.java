package com.example.jean.mapcanvas;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import static android.app.Service.START_STICKY;

public class MainActivity extends AppCompatActivity {
    static final float ALPHA = 0.8f; // if ALPHA = 1 OR 0, no filter applies.
    boolean flag = true;
    TextView countText, distanceText, orientationText;

    Button Btn;
    EditText walkText;
    double a=0, b=0, result=0,radianConst=3.15192/180;
    String result2;

    int count = StepValue.Step;
    private long lastTime;
    int msg,n=0;
    private float deltaTotalAcc, lastX, lastY, lastZ, x, y, z, azimuth, pitch, roll,totalAccDiff,lastDeltaTotalAcc,low_peak,high_peak,totalAbsAcc,lastTotalAbsAcc;
    private float customThresholdTotal=0,averageCustomThreshold=0;
    private static double AMPLITUDE_THRESHOLD = 4.3;
    public static boolean newBitmapAvailable=false;
    public static Bitmap newBitmap;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor; //가속도 센서
    private Sensor magneticSensor; //지자기 센서
    private SensorEventListener accL;
    private SensorEventListener magN;
    float[] Gravity;
    float[] Magnetic;
    private drawCanvas showCanvas,backTrackingCanvas;
    ImageView orientationImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        showCanvas= (drawCanvas) findViewById(R.id.drawing);

        countText = (TextView) findViewById(R.id.stepText);
        distanceText = (TextView) findViewById(R.id.distanceText);
        orientationText = (TextView) findViewById(R.id.orientationText);
        Btn = (Button) findViewById(R.id.button);
        walkText = (EditText) findViewById(R.id.walkText);

        orientationImg = (ImageView)findViewById(R.id.orientationImg);
        orientationImg.setRotation(0);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accL = new accelerometerListener();
        magN = new magneticListener();

        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag) {
                    Btn.setText("STOP");

                    try {
                        onStartCommand();

                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                else {
                    Btn.setText("START");
                    showCanvas.finished();
                    try {
                       // onDestroy();
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
            StepValue.Step = 0; //다시 초기화
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
                            msg = StepValue.Step;
                            low_peak=high_peak;
                            high_peak=0;
                            printResult();
                        }
                    }
                    lastX=x;
                    lastY=y;
                    lastZ=z;
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
        countText.setText(Integer.toString(msg)); //step 수 출력

        a = Double.parseDouble(walkText.getText().toString()); //보폭 변환
        b = Double.parseDouble(countText.getText().toString()); //step 수 변환
        result = a*b; //거리 계산
        distanceText.setText(Double.toString(result)); //거리 출력

        result2 = "Azimut:"+azimuth+"\n"+"Pitch:"+pitch+"\n"+"Roll:"+roll;
        orientationText.setText(result2); //방향 출력

        showCanvas.drawing(azimuth,msg);

    }

    public void backTracking(View view){
        String filePath = showCanvas.saveBitmap(this.getApplicationContext(), showCanvas.drawBitmap, "NewBitmap");
        Toast.makeText(this.getApplicationContext(), "경로가 저장되었습니다.", Toast.LENGTH_LONG).show();
        makeNewBitmapFromPath(filePath);
        showCanvas.onSizeChanged(showCanvas.getWidth(),showCanvas.getHeight(),showCanvas.getWidth(),showCanvas.getHeight());
        //showCanvas.RotateBitmap(showCanvas);
        //showCanvas.firstBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), (int)path),900,900,false);
        //orientationImg.setImageBitmap(showCanvas.drawBitmap);
    }

    public Bitmap makeNewBitmapFromPath(String filePath){
        BitmapFactory.Options newBitmapOption= new BitmapFactory.Options();
        //newBitmapOption.inPreferredConfig=Bitmap.Config.ARGB_4444;
        //newBitmapOption.outHeight=showCanvas.getHeight();
        //newBitmapOption.outWidth=showCanvas.getWidth();
        newBitmap= BitmapFactory.decodeFile(filePath,newBitmapOption);
        newBitmap=Bitmap.createScaledBitmap(newBitmap,showCanvas.getWidth(),showCanvas.getHeight(),false);

        newBitmapAvailable=true;
        //newBitmap=showCanvas.RotateBitmap(newBitmap);
        return showCanvas.RotateBitmap(newBitmap);
    }


}