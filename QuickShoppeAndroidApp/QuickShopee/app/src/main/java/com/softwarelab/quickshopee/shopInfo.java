package com.softwarelab.quickshopee;

/**
 * Created by rehan on 31/10/17.
 */

public class shopInfo {
    private double latitude;
    private double longitude;
    private String name;
    private String id;
    private String city;
    shopInfo(double latitude,double longitude,String name,String id,String city){
        this.latitude=latitude;
        this.longitude=longitude;
        this.name=name;
        this.id=id;
        this.city=city;
    }
    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }
    public String getName(){
        return name;
    }
    public String getId(){
        return id;
    }
    public String getCity(){return city; }
}
