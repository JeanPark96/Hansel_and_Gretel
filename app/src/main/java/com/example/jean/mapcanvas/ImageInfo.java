package com.example.jean.mapcanvas;

/**
 * Created by i on 2019-02-11.
 */

public class ImageInfo {
    private Integer id;
    private int availablePath;
    private byte[] image;
    private float azimuth;
    private float position_x;
    private float position_y;

    public ImageInfo(int id,int availablePath,byte[] image,float azimuth,float position_x,float position_y){
        this.id=id;
        this.image=image;
        this.availablePath=availablePath;
        this.azimuth=azimuth;
        this.position_x=position_x;
        this.position_y=position_y;
    }

    public Integer getId(){
        return this.id;
    }
    public byte[] getImage(){ return this.image; }
    public int getAvailablePath(){ return this.availablePath;}
    public float getAzimuth(){return  this.azimuth;}
    public float getPosition_X(){return  this.position_x;}
    public float getPosition_Y(){return this.position_y;}
}
