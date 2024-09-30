package com.example.clientv2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;

import androidx.annotation.NonNull;

public class RightJoystick extends SurfaceView implements SurfaceHolder.Callback,View.OnTouchListener{
    float x,y;

    JoystickListener joystickCallback;
    public RightJoystick(Context context){
        super(context);
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        setOnTouchListener(this);
        if(context instanceof JoystickListener){
            joystickCallback = (JoystickListener) context;
        }
    }
    public RightJoystick(Context context, AttributeSet a){
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener){
            joystickCallback = (JoystickListener) context;

        }
    }
    public RightJoystick(Context context, AttributeSet a,int source){
        super(context);
        getHolder().addCallback(this);
        setOnTouchListener(this);
        if(context instanceof JoystickListener){
            joystickCallback = (JoystickListener) context;
        }
    }
    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        setupDimensions();
        drawJoystick(centerX,centerY);
    }
    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
    }
    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
    }
    float centerX,centerY,baseRadius,hatRadius;
    void setupDimensions(){
        centerX = (float) getWidth() /2;
        centerY = (float)getHeight()/2;
        baseRadius = (float) Math.min(getWidth(), getHeight()) /4;
        hatRadius = (float) Math.min(getWidth(), getHeight()) /6;
    }
    private void drawJoystick(float newX, float newY){
        if(getHolder().getSurface().isValid()){
            Canvas myCanvas = this.getHolder().lockCanvas();
            Paint colors = new Paint();
            myCanvas.drawColor(Color.TRANSPARENT,PorterDuff.Mode.CLEAR);
            colors.setARGB(125,228,239,255);
            myCanvas.drawCircle(centerX,centerY,baseRadius,colors);
            colors.setARGB(255,255,254,254);
            myCanvas.drawCircle(newX,newY,hatRadius,colors);
            getHolder().unlockCanvasAndPost(myCanvas);
        }
    }
    @Override
    public boolean onTouch(View v, MotionEvent e) {
        if (v.equals(this)){
            float displacement = (float) Math.sqrt(Math.pow(e.getX()-centerX, 2)+ Math.pow((e.getY()-centerY),2));
            if (e.getAction() != MotionEvent.ACTION_UP){
                if (displacement < baseRadius) {
                    x = (e.getX()-centerX)/baseRadius;
                    y = (e.getY()-centerY)/baseRadius;
                    joystickCallback.onRightJoystickMoved((e.getX()-centerX)/baseRadius,(e.getY()-centerY)/baseRadius,getId());
                    drawJoystick(e.getX(),e.getY());
                }else{
                    float ratio = baseRadius/displacement;
                    float constrainedX = centerX + (e.getX()-centerX)*ratio;
                    float constrainedY = centerY + (e.getY()-centerY)*ratio;
                    x = (constrainedX-centerX)/baseRadius;
                    y = (constrainedY-centerY)/baseRadius;
                    joystickCallback.onRightJoystickMoved((constrainedX-centerX)/baseRadius,(constrainedY-centerY)/baseRadius,getId());
                    drawJoystick(constrainedX,constrainedY);
                }
            }else{
                joystickCallback.onRightJoystickMoved(0, 0, v.getId());
                drawJoystick(centerX,centerY);
            }
        }
        return true;
    }
    public interface JoystickListener{
        void onRightJoystickMoved(float x, float y, int source);
    }

}