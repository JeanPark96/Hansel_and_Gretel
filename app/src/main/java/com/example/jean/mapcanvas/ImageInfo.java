package com.example.jean.mapcanvas;

/**
 * Created by i on 2019-02-11.
 */

public class ImageInfo {
    private Integer id;
    private int availablePath;
    private byte[] image;

    public ImageInfo(int id,int availablePath,byte[] image){
        this.id=id;
        this.image=image;
        this.availablePath=availablePath;
    }

    public Integer getId(){
        return this.id;
    }
    public byte[] getImage(){ return this.image; }
    public int getAvailablePath(){ return this.availablePath;}
}
