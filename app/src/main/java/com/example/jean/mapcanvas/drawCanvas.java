package com.example.jean.mapcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class drawCanvas extends View {
    public static File tempFile;
    public static Bitmap drawBitmap, firstBitmap,tempBitmap;
    public Bitmap bitmapForbackTracking;
    private Paint pathColorPaint, startAndFinishMarkColorPaint,canvasPaint, intermediatePaint,GPSPaint;
    private Canvas canvas;

    private double radianConst=3.15192/180,angleDiff,GPS_angleDiff,one_fourth_rad=90*radianConst,half_rad=180*radianConst,three_fourth=270*radianConst;
    public static double theta=0;
    public static short fixed_bearing=0;
    private float angleDiffRadian,bearingDiffRadian;
    public static float startX, startY, endX, endY;
    public static float GPS_startX, GPS_startY, GPS_endX, GPS_endY;
    private int width,height;

    private int r_local_step;
    private float r_azimuth;
    private float r_stride;

    private short r_bearing;
    private double r_distance;
    private int r_GPSCall;
    private double GPS_totalDistance=0;

    private Path path = new Path();
    private Path path2=new Path();

    public drawCanvas(Context context, AttributeSet attrs) {
        super(context,attrs);
        pathColorPaint = new Paint();
        startAndFinishMarkColorPaint = new Paint();
        intermediatePaint = new Paint();
        GPSPaint=new Paint();

        pathColorPaint=settingPaint(R.color.blue,15f);
        startAndFinishMarkColorPaint=settingPaint(R.color.green,8f);
        intermediatePaint=settingPaint(R.color.black,8f);
        GPSPaint=settingPaint(R.color.red,15f);
        canvasPaint=new Paint(Paint.DITHER_FLAG);
    }

    public Paint settingPaint(int colorId,float strokeWidth){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(ContextCompat.getColor(getContext(),colorId));
        paint.setStrokeWidth(strokeWidth);
        paint.setAntiAlias(true);
        paint.setStrokeJoin(Paint.Join.ROUND);

        return paint;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = getWidth();
        height = getHeight();

        if(isBackTrackActivated()){
            path.reset();

            canvas = new Canvas(bitmapForbackTracking);
            startX = width-endX;
            startY = height-endY;

            pathColorPaint=settingPaint(R.color.gray,15f);
            path.moveTo(startX, startY);

            theta += 180; //backTracking 시 길 방향 보정
            theta %= 360;
        }else{
            if(MainActivity.pathAvailableNumber==0) {
                drawBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
            }else{
                drawBitmap = Bitmap.createBitmap(MainActivity.newBitmap);
            }
            canvas = new Canvas(drawBitmap);
            firstBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.campus),width,height,false);
        }
  }

    public boolean isBackTrackActivated(){
        if(MainActivity.newBitmapAvailable==true) {
            bitmapForbackTracking = MainActivity.newBitmap;

            return true;
        }
        else
            return false;
    }

    public void drawing(float azimuth,int local_step,double stride,double distanceFromMain) {
        r_local_step = local_step;
        r_azimuth = azimuth;
        r_stride = 8*(float) stride;

        Log.d("drawing method working",r_local_step+"drawing");
        if (r_local_step <= 1 && !isBackTrackActivated()) {
            if(MainActivity.pathAvailableNumber==0) {
                startX = (width / 2)+185;
                startY = (height / 2) + 550;
                path.moveTo(startX, startY);
                invalidate();
                endX = startX;
                endY = startY - (float)(r_stride);
                theta = r_azimuth;
                path.lineTo(endX, endY);
                canvas.drawOval(startX - 12, startY - 12, startX + 12, endY + 12, startAndFinishMarkColorPaint);//스타트 마크, 여기에 있어야 사라지지 않음
                canvas.drawPath(path, pathColorPaint);
            }
            else{
                startX=endX;
                startY=endY;
                path.moveTo(startX, startY);
                invalidate();
                endX = startX;
                endY = startY - 3;
                path.lineTo(endX, endY);
                canvas.drawPath(path, pathColorPaint);
            }
        } else {
            angleDiff=r_azimuth - theta;
            angleDiffRadian = modifyDirection(angleDiff);
            updateLocation(startX,startY,angleDiffRadian,r_stride);
            path.lineTo(endX,endY);
            canvas.drawPath(path, pathColorPaint);

            if(0<=distanceFromMain%50 && distanceFromMain%50<1 && r_local_step!=0) {
                canvas.drawOval(startX-12, startY-12, startX+12, endY+12, intermediatePaint);
            }
        }

        invalidate();

        startX=endX;
        startY=endY;
    }
    public void GPSdrawing(int countGPSCall, double distance, short bearing){
        r_GPSCall=countGPSCall;
        r_distance = 8*distance;
        r_bearing = bearing;
        GPS_totalDistance+=distance;

        Log.d("drawing method working",r_local_step+"drawing");
        if (r_GPSCall <= 2 && !isBackTrackActivated()) {
            if(MainActivity.pathAvailableNumber==0) {
                GPS_startX = (width / 2)+220;
                GPS_startY = (height / 2) + 550;
                path2.moveTo(GPS_startX, GPS_startY);
                invalidate();
                GPS_endX = GPS_startX;
                GPS_endY = GPS_startY - (float)(r_distance);
                fixed_bearing = (short)r_bearing;
                path2.lineTo(GPS_endX, GPS_endY);
                canvas.drawOval(GPS_startX - 12, GPS_startY - 12, GPS_startX + 12, GPS_startY + 12, startAndFinishMarkColorPaint);//스타트 마크, 여기에 있어야 사라지지 않음
                canvas.drawPath(path2, GPSPaint);
            }
            else{
                GPS_startX=GPS_endX;
                GPS_startY=GPS_endY;
                path2.moveTo(GPS_startX, GPS_startY);
                invalidate();
                GPS_endX = GPS_startX;
                GPS_endY = GPS_startY - 3;
                path2.lineTo(GPS_endX, GPS_endY);
                canvas.drawPath(path2, GPSPaint);
            }
        } else {
            GPS_angleDiff=(short)(r_bearing- fixed_bearing);
            bearingDiffRadian = modifyGPSDirection(GPS_angleDiff);
            updateGPSLocation(GPS_startX,GPS_startY,bearingDiffRadian,(float)r_distance);
            path2.lineTo(GPS_endX,GPS_endY);
            canvas.drawPath(path2, GPSPaint);
            fixed_bearing=r_bearing;
            if( 0<=GPS_totalDistance%50 && GPS_totalDistance%50<1 && r_local_step!=0) {
                canvas.drawOval(GPS_startX-12, GPS_startY-12, GPS_startX+12, GPS_endY+12, intermediatePaint);
            }
        }

        invalidate();

        GPS_startX=GPS_endX;
        GPS_startY=GPS_endY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(firstBitmap,0,0,canvasPaint);
        canvas.drawBitmap(drawBitmap,0,0,canvasPaint);
    }

    public void finished(){
        startAndFinishMarkColorPaint=settingPaint(R.color.red,8f);
        canvas.drawOval(startX-12,startY-12,startX+12,endY+12, startAndFinishMarkColorPaint);
        canvas.drawOval(GPS_startX-12,GPS_startY-12,GPS_startX+12,GPS_endY+12, startAndFinishMarkColorPaint);
        invalidate();
    }

    public Bitmap RotateBitmap(Bitmap bitmap){
        Matrix matrix = new Matrix();
        matrix.setScale(-1, -1); //상하좌우반전

        drawBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        return drawBitmap;
    }

    public Bitmap setBitmap(Bitmap bitmap){
        Matrix matrix = new Matrix();
        matrix.setScale(1, 1);
        drawBitmap=Bitmap.createBitmap(bitmap,0,0,bitmap.getWidth(),bitmap.getHeight(),matrix,false);

        return drawBitmap;
    }

    public static String saveBitmap(Context context, Bitmap bitmap, String name){
        File storage = context.getCacheDir(); // 이 부분이 임시파일 저장 경로
        String fileName = name + ".png";  // 파일이름은 마음대로!
        tempFile = new File(storage, fileName);

        try{
            tempFile.createNewFile();  // 파일을 생성해주고
            FileOutputStream out = new FileOutputStream(tempFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90 , out);  // 넘거 받은 bitmap을 jpeg(손실압축)으로 저장해줌
            out.flush();
            out.close(); // 마무리로 닫아줍니다.
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile.getAbsolutePath();   // 임시파일 저장경로를 리턴해주면 끝!
    }

    private float modifyDirection(double angleDiff){
        if (angleDiff < 0)//angleDiff 값은 무조건 양수가 나오도록 보정
            angleDiff += 360;

        if(angleDiff>345 || angleDiff<=15){//방위각 보정
            angleDiff=0;
        }else if(angleDiff>15&&angleDiff<=45){
            angleDiff=30;
        }else if(angleDiff>45&&angleDiff<=75){
            angleDiff=60;
        }else if(angleDiff>75&&angleDiff<=105){
            angleDiff=90;
        }else if(angleDiff>105&&angleDiff<=135){
            angleDiff=120;
        }else if(angleDiff>135&&angleDiff<=165){
            angleDiff=150;
        }else if(angleDiff>165&&angleDiff<=195){
            angleDiff=180;
        }else if(angleDiff>195&&angleDiff<=225){
            angleDiff=210;
        }else if(angleDiff>225&&angleDiff<=255){
            angleDiff=240;
        }else if(angleDiff>255&&angleDiff<=285){
            angleDiff=270;
        }else if(angleDiff>285&&angleDiff<=315){
            angleDiff=300;
        }else if(angleDiff>315&&angleDiff<=345){
            angleDiff=330;
        }

        angleDiffRadian = (float) (angleDiff* radianConst);//346~15는 직선 +16~45는 30도 오른쪽, 46~75는 60도 오른쪽, 76~105는 90도 오른쪽, 106~135는 120도, 136~165는 150도, 166~195는 180도,196~225는 210도, 226~255는 240도, 256~285는 270도 , 286~315는 300도, 316~345는 330도,
        return angleDiffRadian;
    }
    private float modifyGPSDirection(double GPS_angleDiff){
        if (GPS_angleDiff < 0)//angleDiff 값은 무조건 양수가 나오도록 보정
            GPS_angleDiff += 360;
        if(GPS_angleDiff>345 || GPS_angleDiff<=15){//방위각 보정
            GPS_angleDiff=0;
        }else if(GPS_angleDiff>15&&GPS_angleDiff<=45){
            GPS_angleDiff=30;
        }else if(GPS_angleDiff>45&&GPS_angleDiff<=75){
            GPS_angleDiff=60;
        }else if(GPS_angleDiff>75&&GPS_angleDiff<=105){
            GPS_angleDiff=90;
        }else if(GPS_angleDiff>105&&GPS_angleDiff<=135){
            GPS_angleDiff=120;
        }else if(GPS_angleDiff>135&&GPS_angleDiff<=165){
            GPS_angleDiff=150;
        }else if(GPS_angleDiff>165&&GPS_angleDiff<=195){
            GPS_angleDiff=180;
        }else if(GPS_angleDiff>195&&GPS_angleDiff<=225){
            GPS_angleDiff=210;
        }else if(GPS_angleDiff>225&&GPS_angleDiff<=255){
            GPS_angleDiff=240;
        }else if(GPS_angleDiff>255&&GPS_angleDiff<=285){
            GPS_angleDiff=270;
        }else if(GPS_angleDiff>285&&GPS_angleDiff<=315){
            GPS_angleDiff=300;
        }else if(GPS_angleDiff>315&&GPS_angleDiff<=345){
            GPS_angleDiff=330;
        }


        bearingDiffRadian = (float) (GPS_angleDiff* radianConst);//346~15는 직선 +16~45는 30도 오른쪽, 46~75는 60도 오른쪽, 76~105는 90도 오른쪽, 106~135는 120도, 136~165는 150도, 166~195는 180도,196~225는 210도, 226~255는 240도, 256~285는 270도 , 286~315는 300도, 316~345는 330도,
        return bearingDiffRadian;
    }

    private void updateLocation(float startX, float startY, float angleDiffRadian,float stride){
        if (this.angleDiffRadian >0 && this.angleDiffRadian <= one_fourth_rad) {
            endX = startX + (float) Math.cos(this.angleDiffRadian)* stride;
            endY = startY - (float) Math.sin(this.angleDiffRadian)* stride;
        } else if (this.angleDiffRadian <= half_rad) {
            endX = startX + (float) Math.cos(this.angleDiffRadian - one_fourth_rad)* stride;
            endY = startY + (float) Math.sin(this.angleDiffRadian - one_fourth_rad)* stride;
        } else if (this.angleDiffRadian <= three_fourth) {
            endX = startX - (float) Math.sin(this.angleDiffRadian - half_rad)* stride;
            endY = startY + (float) Math.cos(this.angleDiffRadian - half_rad)* stride;
        } else if(this.angleDiffRadian <=(2*half_rad) || this.angleDiffRadian ==0){
            endX = startX - (float) Math.cos(this.angleDiffRadian - three_fourth)* stride;
            endY = startY - (float) Math.sin(this.angleDiffRadian - three_fourth)* stride;
        }
    }
    private void updateGPSLocation(float GPS_startX, float GPS_startY, float bearingDiffRadian,float r_distance){
        if (this.bearingDiffRadian >0 && this.bearingDiffRadian <= one_fourth_rad) {
            GPS_endX = GPS_startX + r_distance * (float) Math.cos(this.bearingDiffRadian);
            GPS_endY = GPS_startY - r_distance * (float) Math.sin(this.bearingDiffRadian);
        } else if (this.bearingDiffRadian <= half_rad) {
            GPS_endX = GPS_startX + r_distance * (float) Math.cos(this.bearingDiffRadian - one_fourth_rad);
            GPS_endY = GPS_startY + r_distance * (float) Math.sin(this.bearingDiffRadian - one_fourth_rad);
        } else if (this.bearingDiffRadian <= three_fourth) {
            GPS_endX = GPS_startX - r_distance * (float) Math.sin(this.bearingDiffRadian - half_rad);
            GPS_endY = GPS_startY + r_distance * (float) Math.cos(this.bearingDiffRadian - half_rad);
        } else if(this.bearingDiffRadian <=(2*half_rad) || this.bearingDiffRadian ==0){
            GPS_endX = GPS_startX - r_distance * (float) Math.cos(this.bearingDiffRadian - three_fourth);
            GPS_endY = GPS_startY - r_distance * (float) Math.sin(this.bearingDiffRadian - three_fourth);
        }
    }
}