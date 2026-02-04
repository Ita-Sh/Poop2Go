package com.example.poop2go;

public class User {
    private String uId;
    private String name;
    private int dateOfBirth;    //format: YYYYMMDD

    public User (){
        this.uId = "";
        this.name = "";
        this.dateOfBirth = 0;
    }    //default User
    public User(String name, int dateOfBirth){
        this.uId = ""; //set automatically
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }

    public String getUId(){
        return this.uId;
    }
    public String getName(){
        return this.name;
    }
    public int getDateOfBirth(){
        return this.dateOfBirth;
    }

    public void setUId(String uid){
        this.uId = uid;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setDateOfBirth(int dateOfBirth){
        this.dateOfBirth = dateOfBirth;
    }

}
