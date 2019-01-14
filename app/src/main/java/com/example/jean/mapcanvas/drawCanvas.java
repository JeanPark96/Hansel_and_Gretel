package com.example.jean.mapcanvas;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class drawCanvas extends View {
    public static File tempFile;
    public static Bitmap drawBitmap, firstBitmap,newBitmap;
    public Bitmap bitmapForbackTracking;
    private Paint paintCanvas,paintMark,canvasPaint, intermediatePaint;
    private Canvas canvas;

    private double radianConst=3.15192/180,theta=0,angleDiff,one_fourth_rad=90*radianConst,half_rad=180*radianConst,three_fourth=270*radianConst;
    private float angleDiffRadian;
    private float startX, startY,endX,endY;
    private int r_msg,flag=0;
    private float r_azimuth;
    private int width,height;
    private Path path=new Path();

    public drawCanvas(Context context, AttributeSet attrs) {
        super(context,attrs);
        paintCanvas = new Paint();
        paintMark = new Paint();
        intermediatePaint = new Paint();

        paintCanvas.setStyle(Paint.Style.STROKE);
        paintCanvas.setColor(Color.BLUE);
        paintCanvas.setStrokeWidth(10f);
        paintCanvas.setAntiAlias(true);
        paintCanvas.setStrokeJoin(Paint.Join.ROUND);

        paintMark.setStyle(Paint.Style.STROKE);
        paintMark.setColor(Color.GREEN);
        paintMark.setStrokeWidth(5f);
        paintMark.setAntiAlias(true);
        paintMark.setStrokeJoin(Paint.Join.ROUND);
        canvasPaint=new Paint(Paint.DITHER_FLAG);

        intermediatePaint.setStyle(Paint.Style.STROKE);
        intermediatePaint.setColor(Color.BLACK);
        intermediatePaint.setStrokeWidth(5f);
        intermediatePaint.setAntiAlias(true);
        intermediatePaint.setStrokeJoin(Paint.Join.ROUND);
        // canvasMap=(ImageView)findViewById(R.id.grid_img);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        width = getWidth();
        height = getHeight();
        if(isBackTrackActivated()){
           // canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
           // firstBitmap=Bitmap.createScaledBitmap(bitmapForbackTracking,900,900,false);
           // canvas.drawColor(0,PorterDuff.Mode.CLEAR);
            firstBitmap=bitmapForbackTracking;
            firstBitmap=Bitmap.createBitmap(getWidth(),getHeight(), Bitmap.Config.ALPHA_8);
            flag=1;
        }else{
            drawBitmap = Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
            canvas = new Canvas(drawBitmap);
            firstBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.grid),width,height,false);
        }
  }

    public boolean isBackTrackActivated(){
        if(MainActivity.newBitmapAvailable==true) {
            bitmapForbackTracking=MainActivity.newBitmap;
            return true;
        }
        else
            return false;
    }
    public void drawing(float azimuth,int msg) {
        r_msg=msg;
        r_azimuth=azimuth;
        Log.d("drawing method working",r_msg+"drawing");
        if (r_msg <= 1) {
            startX = (width/ 2);
            startY = (height / 2) + 300;
            path.moveTo(startX,startY);
            invalidate();
            endX = startX;
            endY = startY - 3;
            theta = r_azimuth;
            path.lineTo(endX,endY);
            canvas.drawOval(startX-8,startY-8,startX+8,endY+8,paintMark);//스타트 마크, 여기에 있어야 사라지지 않음
            canvas.drawPath(path,paintCanvas);
        } else {
            angleDiff=r_azimuth - theta;
            angleDiffRadian =modifyDirection(angleDiff);
            updateLocation(startX,startY,angleDiffRadian);
            path.lineTo(endX,endY);
            canvas.drawPath(path,paintCanvas);

            if(r_msg % 80 == 0) {
                canvas.drawOval(startX-8, startY-8, startX+8, endY+8, intermediatePaint);
            }
        }

        invalidate();

        startX=endX;
        startY=endY;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(firstBitmap,0,0,canvasPaint);
        canvas.drawBitmap(drawBitmap,0,0,canvasPaint);

    }

    public void finished(){
        paintMark.setColor(Color.RED);
        canvas.drawOval(startX-8,startY-8,startX+8,endY+8,paintMark);
        invalidate();
    }

    /*public Bitmap RotateBitmap(Bitmap bitmap){
        Matrix matrix = new Matrix();
        matrix.setScale(1, -1); //상하반전
        matrix.setScale(-1, 1); //좌우반전
        //matrix.postRotate(180);

        drawBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);

        return drawBitmap;
    }
    public drawCanvas RotateBitmap(drawCanvas canvas){
        canvas.setRotation(180);
        return  canvas;
    }*/

    public static String saveBitmap(Context context, Bitmap bitmap, String name){
        File storage = context.getCacheDir(); // 이 부분이 임시파일 저장 경로
        String fileName = name + ".jpg";  // 파일이름은 마음대로!
        tempFile = new File(storage, fileName);

        /*if(tempFile.exists()){
            drawBitmap = BitmapFactory.decodeFile(tempFile.getAbsolutePath());
            TestActivity.imageView.setImageBitmap(drawBitmap);
        }*/
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

    private void updateLocation(float startX, float startY, float angleDiffRadian){
        if (this.angleDiffRadian >0 && this.angleDiffRadian <= one_fourth_rad) {
            endX = startX + 3 * (float) Math.cos(this.angleDiffRadian);
            endY = startY - 3 * (float) Math.sin(this.angleDiffRadian);
        } else if (this.angleDiffRadian <= half_rad) {
            endX = startX + 3 * (float) Math.cos(this.angleDiffRadian - one_fourth_rad);
            endY = startY + 3 * (float) Math.sin(this.angleDiffRadian - one_fourth_rad);
        } else if (this.angleDiffRadian <= three_fourth) {
            endX = startX - 3 * (float) Math.sin(this.angleDiffRadian - half_rad);
            endY = startY + 3 * (float) Math.cos(this.angleDiffRadian - half_rad);
        } else if(this.angleDiffRadian <=(2*half_rad) || this.angleDiffRadian ==0){
            endX = startX - 3 * (float) Math.cos(this.angleDiffRadian - three_fourth);
            endY = startY - 3 * (float) Math.sin(this.angleDiffRadian - three_fourth);
        }

    }
}