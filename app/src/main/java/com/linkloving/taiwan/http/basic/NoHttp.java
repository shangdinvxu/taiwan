package com.linkloving.taiwan.http.basic;

/**
 * Created by YaPi on 2017/5/6.
 */

public class NoHttp  {
    public static MyJsonRequest  createStringRequestPost(String url){
       return new MyJsonRequest(url);
    }
}
