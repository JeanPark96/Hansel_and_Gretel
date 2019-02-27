package com.example.jean.mapcanvas;

/**
 * Created by i on 2019-02-11.
 */

public class ImageInfo {
    private Integer id;
    private int availablePath;
    private byte[] image;
    private float first_azimuth;
    private float last_azimuth;
    private float position_x;
    private float position_y;

    public ImageInfo(int id,int availablePath,byte[] image,float first_azimuth,float last_azimuth,float position_x,float position_y){
        this.id=id;
        this.image=image;
        this.availablePath=availablePath;
        this.first_azimuth=first_azimuth;
        this.last_azimuth=last_azimuth;
        this.position_x=position_x;
        this.position_y=position_y;
    }

    public Integer getId(){
        return this.id;
    }
    public byte[] getImage(){ return this.image; }
    public int getAvailablePath(){ return this.availablePath;}
    public float getFirst_azimuth(){return  this.first_azimuth;}
    public float getLast_azimuth(){return this.last_azimuth;}
    public float getPosition_X(){return  this.position_x;}
    public float getPosition_Y(){return this.position_y;}
}
