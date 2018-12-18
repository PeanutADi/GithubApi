package com.example.peanut.webapi2;

public class Issues {
    public String title;
    public String state;
    public String body;
    public String created_at;

    public Issues(String title,String state,String body,String created_at){
        this.body=body;
        this.title=title;
        this.state=state;
        this.created_at=created_at;
    }
    //public String message;
}
