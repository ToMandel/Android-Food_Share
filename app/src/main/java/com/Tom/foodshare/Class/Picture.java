package com.Tom.foodshare.Class;

public class Picture {
    private String imageEncoded;
    private String date;
    private  User user;

    private int id;


    public Picture(){}

    public Picture(String imageEncoded, String date, User user, int id){
        this.imageEncoded = imageEncoded;
        this.date = date;
        this.user = user;
        this.id = id;
    }

    public String getDate(){return date;}

    public void setDate(String date){this.date = date;}

    public User getUser(){return user;}

    public void setUser(User user){this.user = user;}

    public String getImageEncoded(){ return imageEncoded;}

    public void setImageEncoded(String imageEncoded){this.imageEncoded = imageEncoded;}

    public int getId(){return this.id;}

    public void setId(int id){this.id = id;}
}
