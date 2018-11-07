package com.example.jean.fmp;

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
import android.widget.TextView;
import android.widget.Toast;

import static android.app.Service.START_STICKY;

public class MainActivity extends AppCompatActivity {

    boolean flag = true;
    TextView countText, distanceText, minText,maxText;
    Button Btn;
    EditText walkText;
    double a=0, b=0, result=0;
    String result2,minResult, maxResult;

    int count = StepValue.Step;
    private long lastTime;
    private float deltaTotalAcc, lastX, lastY, lastZ, x, y, z, azimuth, pitch, roll,totalAccDiff,lastDeltaTotalAcc,low_peak,high_peak,totalAbsAcc,lastTotalAbsAcc;
    int msg,n=0;
    private static final double SHAKE_THRESHOLD = 15;
    private static final double AMPLITUDE_THRESHOLD = 0;
    private SensorManager sensorManager;
    private Sensor accelerometerSensor; //가속도 센서
    private Sensor magneticSensor; //지자기 센서
    private SensorEventListener accL;
    private SensorEventListener magN;

    float[] Gravity;
    float[] Magnetic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        countText = (TextView) findViewById(R.id.stepText);
        distanceText = (TextView) findViewById(R.id.distanceText);
        Btn = (Button) findViewById(R.id.button);
        walkText = (EditText) findViewById(R.id.walkText);
        minText=(TextView)findViewById(R.id.minText);
        maxText=(TextView)findViewById(R.id.maxText);
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        accL = new accelerometerListener();


        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag) { //버튼 처음 눌렀을 때
                    Btn.setText("STOP");

                    try {
                        onStartCommand();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }

                else { //버튼 다시 눌렀을 때
                    Btn.setText("START");

                    try {
                        onDestroy();
                    } catch (Exception e) {
                        Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                }
                flag = !flag;
            }
        });
    }

    public int onStartCommand() {
        super.onStart();
        Log.i("onStartCommand", "IN");
        if (accelerometerSensor != null) {
            sensorManager.registerListener(accL, accelerometerSensor, sensorManager.SENSOR_DELAY_GAME);
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("onDestroy", "IN");
        if (sensorManager != null) {
            sensorManager.unregisterListener(accL);
            StepValue.Step = 0; //다시 초기화
        }
    }

    private class accelerometerListener implements SensorEventListener {
        public void onSensorChanged(SensorEvent event) {
            //거리 계산
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                Gravity = event.values.clone(); //지자기 센서를 작동하기 위한 배열 한개라도 값이 채워져야 함
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
                    n++;

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
                           StepValue.Step = count++;
                           msg = StepValue.Step;
                           printResult();
                           low_peak=high_peak;
                           high_peak=0;
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



    public void printResult(){
        countText.setText(Integer.toString(msg)); //step 수 출력

        a = Double.parseDouble(walkText.getText().toString()); //보폭 변환
        b = Double.parseDouble(countText.getText().toString()); //step 수 변환
        result = a*b; //거리 계산
        distanceText.setText(Double.toString(result)); //거리 출력

        minResult = minText.getText()+", "+Float.toString(low_peak);
        minText.setText(minResult);
        maxResult = maxText.getText()+", "+Float.toString(high_peak);
        maxText.setText(maxResult);

    }
}