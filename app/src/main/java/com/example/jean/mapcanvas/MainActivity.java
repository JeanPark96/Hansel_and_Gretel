package com.example.jean.mapcanvas;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
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

    Boolean isGPSEnabled;//gps 사용가능 여부
    Boolean isNetworkEnabled;//네트워크 사용가능여부
    int countGPSCall=0; //거리와 방위각을 계산하기 위해서는 위도와 경도가 최소한 1번 바껴야 하므로 필요함
    double lastKnownlng=0;
    double lastKnownlat=0;
    double distance;
    double radian_distance;
    double radian_bearing;
    double true_bearing;
    double last_true_bearing;
    float marker_dp;
    float curr_posX;
    float curr_posY;
    float marker_diffX;
    float marker_diffY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //android.app.ActionBar actionBar = getActionBar();
        //actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#F0F8FF")));
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

    //gps버튼 작동
    public void GetLocations(){

        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    0 );
        }//안드로이드가 23 버전 이상으로 업그레이드 되면서 사용자에게 퍼미션을 요청하는 소스코드를 manifest 파일 외에도 삽입해야함


        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);//시스템 위치 서비스에 접근 가능하게 함. 이 서비스는 어플이 기기의 지리학적 위치를 주기적으로 업데이트 받아오게끔 함

        // GPS 프로바이더 사용가능여부
        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        // 네트워크 프로바이더 사용가능여부
        isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        Log.d("Main", "isGPSEnabled="+ isGPSEnabled);
        Log.d("Main", "isNetworkEnabled="+ isNetworkEnabled);
        LocationListener locationListener = new LocationListener() {//location manager로부터 location이 바뀌었을 때 알림을 받을 수 있도록 함

            public void onLocationChanged(Location location) {//위치가 변할때마다 호출


                double lat = location.getLatitude();//위도 받아오기
                double lng = location.getLongitude();//경도 받아오

                if (++count >= 2) {//위도 경도를 각각 2번 이상 받아왔을 때 거리와 방위각 계산이 가능해짐
                    // DrawMap();
                    Location location1 = new Location("point1");
                    location1.setLatitude(lastKnownlat);//location1의 위도는 이전 위치의 위도
                    location1.setLongitude(lastKnownlng);//location1의 경도는 이전 위치의 경도
                    Location location2 = new Location("point1");
                    location2.setLatitude(lat);//location2의 위도는 현재 위도
                    location2.setLongitude(lng);//location2 경도는 현재 경도
                    double temp_distance = location1.distanceTo(location2);//location1으로부터 location2까지의 거리를 구하는 메소드
                    distance = temp_distance;
                    true_bearing = bearingTo(lastKnownlat, lastKnownlng,lat,lng);//bearingTo 함수 실행해서 방위각 구함 이전 위도경도와 현재 위도경도로 구함
                    last_true_bearing = true_bearing;//이전 방위각을 저장해놓음->나중에 지도 그릴때 사용하기 위해
                }
                lastKnownlng = lng;//현재 경도는 곧 이전 경도가 됨
                lastKnownlat = lat;//현재 위도는 곧 이전 위도가 됨

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {//provider의 상태가 바뀔
            }

            public void onProviderEnabled(String provider) {//사용자에 의해 provider가 설정될 때 호출
            }

            public void onProviderDisabled(String provider) {//사용자에 의해 provider가 해제될 때 호출
            }
        };

        //Register the listener with the Location Manager to receive location updates
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);//위치갱신 요청
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);//위치갱신 요청gps
        //gps 리퀘스트가 제대로 요청하는건지 확인해보기 반응을 더 빠르게 할 수 있는지 확인해보기

        // 수동으로 위치 구하기
        String locationProvider = LocationManager.GPS_PROVIDER;

        Location lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);//마지막으로 저장된 위치 찾기->gps가 처음부터 위치측위를 하는데에 시간을 덜 걸리게 해줌
        if (lastKnownLocation != null) {//마지막 위치 정보를 저장하고 있다면
            double lng = lastKnownLocation.getLongitude();//마지막 경도를 현재 경도로 저장
            double lat = lastKnownLocation.getLatitude();//마지막 위도를 현재 위도로 저장

            Log.d("Main", "longtitude=" + lng + ", latitude=" + lat);

        }
    }


    //방위각 구하기
    public short bearingTo(double latitude1,double longitude1,double latitude2,double longitude2) {
        double currLatRadian = latitude1 * radianConst;//이전 위도를 라디안으로 변환
        double currLngRadian = longitude1 * radianConst;//이전 경도를 라디안으로 변환
        double destLatRadian = latitude2* radianConst;//현재 위도를 라디안으로 변환
        double destLngRadian = longitude2 * radianConst;//현재 경도를 라디안으로 변환
        radian_distance = Math.acos(Math.sin(currLatRadian) * Math.sin(destLatRadian) + Math.cos(currLatRadian) * Math.cos(destLatRadian) * Math.cos(currLngRadian - destLngRadian));
        radian_bearing = Math.acos((Math.sin(destLatRadian) - Math.sin(currLatRadian) * Math.cos(radian_distance)) / (Math.cos(currLatRadian) * Math.sin(radian_distance)));
        true_bearing=0;
        if (Math.sin(destLngRadian - currLngRadian) < 0) {//경도는 서쪽 180, 동쪽 180으로만 구분되므로 총 360도인 방위각으로 표현하기 위해서는 현재 경도보다 이전 경도가 크면 라디안 방위각을 방위각으로 변환시켜 그대로 출력하지 않고 360에서 빼줘야 한다
            true_bearing = radian_bearing * (1/radianConst);//라디안 각을 degree로 표시
            true_bearing = (360 - true_bearing);//현재경도보다 이전경도가 크면 방위각으로 표ㅛ현할때 보와해줘야 함
        } else {
            true_bearing = (radian_bearing * (1/radianConst));// 현재 경도가 이전 경도보다 크면 방위각 그대로 계산 라디안 각을 degree로 변환
        }
        return (short)true_bearing;//short형으로 방위각 리턴
    }
}