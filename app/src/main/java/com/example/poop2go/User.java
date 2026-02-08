package com.example.poop2go;

public class User {
    private String uId;
    private String name;
    private String dateOfBirth;    //format: YYYYMMDD

    public User (){
        this.uId = "";
        this.name = "";
        this.dateOfBirth = "";
    }    //default User
    public User(String uId, String name, String dateOfBirth){
        this.uId = uId; //set automatically
        this.name = name;
        this.dateOfBirth = dateOfBirth;
    }

    public String getUId(){
        return this.uId;
    }
    public String getName(){
        return this.name;
    }
    public String getDateOfBirth(){
        return this.dateOfBirth;
    }

    public void setUId(String uid){
        this.uId = uid;
    }
    public void setName(String name){
        this.name = name;
    }
    public void setDateOfBirth(String dateOfBirth){
        this.dateOfBirth = dateOfBirth;
    }

}
