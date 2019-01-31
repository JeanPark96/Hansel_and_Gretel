package com.example.jean.mapcanvas;

/**
 * Created by i on 2019-01-24.
 */

public class PathInfo {
    private Integer id;
    private String name;
    private String date;
    private String pathName;

    public PathInfo(int id,String name,String date,String pathName){
        this.id=id;
        this.name=name;
        this.date=date;
        this.pathName=pathName;
    }

    public Integer getId(){
        return this.id;
    }
    public String getName(){ return  this.name; }
    public String getDate(){
        return  this.date;
    }
    public String getPathName(){ return this.pathName; }
}
