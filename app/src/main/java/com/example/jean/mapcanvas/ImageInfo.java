package com.example.jean.mapcanvas;

/**
 * Created by i on 2019-02-11.
 */

public class ImageInfo {
    private Integer id;
    private byte[] image;

    public ImageInfo(int id,byte[] image){
        this.id=id;
        this.image=image;
    }

    public Integer getId(){
        return this.id;
    }
    public byte[] getImage(){ return this.image; }
}
