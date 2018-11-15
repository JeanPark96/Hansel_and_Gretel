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
import android.widget.ImageView;

public class drawCanvas extends View {

    private Bitmap drawBitmap,firstBitmap,bitmap;
    private Paint paintCanvas,paintMark,canvasPaint;
    private Canvas canvas;
    ImageView canvasMap;

    private double radianConst=3.15192/180,theta=0,angleDiff,one_fourth_rad=90*radianConst,half_rad=180*radianConst,three_fourth=270*radianConst;
    private float angleDiffRadian;
    private float startX, startY,endX,endY;
    private int r_msg;
    private float r_azimuth;
    private int width,height;
    private Path path=new Path();


    public drawCanvas(Context context, AttributeSet attrs) {
        super(context,attrs);
        paintCanvas=new Paint();
        paintMark=new Paint();

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
        // canvasMap=(ImageView)findViewById(R.id.grid_img);

    }
  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
      drawBitmap=Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);//CanvasBitmap를 그대로 가져오면서 흑백처리 해버림.
      //drawBitmap=Bitmap.createBitmap(getWidth(),getHeight(),Bitmap.Config.ARGB_8888);
      canvas=new Canvas(drawBitmap);//이거 없으면 start 버튼 누르면 grid 이미지 사라짐
      width=getWidth();
      height=getHeight();
      firstBitmap= Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(),R.drawable.grid),900,900,false);

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

        } else {

            angleDiff=r_azimuth - theta;
            angleDiffRadian =modifyDirection(angleDiff);
            updateLocation(startX,startY,angleDiffRadian);
            path.lineTo(endX,endY);
        }

        invalidate();

        startX=endX;
        startY=endY;
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //firstBitmap= BitmapFactory.decodeResource(getResources(),R.drawable.grid);//res 폴더에 저장된 이미지를 Bitmap으로 만들때 사용함
        //canvasMap.setImageBitmap(firstBitmap);


        canvas.drawBitmap(firstBitmap,0,200,canvasPaint);

        canvas.drawBitmap(drawBitmap,0,0,canvasPaint);
        canvas.drawPath(path,paintCanvas);
        //canvasMap.setImageDrawable(new BitmapDrawable(getResources(),drawBitmap));//이거 없으면 안그려짐 애초에 그려질 수 있도록 drawBitmap이 없는것처럼 인식됨

        //canvas.drawLine(startx,starty,endx,endy,paintCanvas);
    //canvas.save();

    }

   public void finished(){
           paintMark.setColor(Color.RED);
           canvas.drawOval(startX-8,startY-8,startX+8,endY+8,paintMark);
           //pathMark.addArc(startx-5,starty-5,startx+5,endy+5,0,360);
           //  canvas.drawPath(pathMark,paintMark);
        invalidate();
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
