package com.example.android.quakereport;

public class Earthquake {




    private String mMagnitude;

    private String mLocation;

    private String mDate;

    private String mTime;

    private String mUrl;

    public Earthquake(String magnitude, String location, String date, String time, String url){

        mMagnitude = magnitude;

        mLocation = location;
        mDate = date;
        mTime = time;
        mUrl =  url;
    }

    public String getMagnitude(){
        return mMagnitude;
    }



    public String getLocation(){
        return mLocation;
    }

    public String getDate(){
        return mDate;
    }

    public String getTime(){
        return mTime;
    }

    public String getUrl(){
        return mUrl;
    }
}
