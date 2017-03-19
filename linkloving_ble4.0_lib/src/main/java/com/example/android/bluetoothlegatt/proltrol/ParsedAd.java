package com.example.android.bluetoothlegatt.proltrol;

import java.util.ArrayList;

/**
 * Created by Daniel.Xu on 2017/2/13.
 */

public class ParsedAd {
    public ArrayList<String> uuidStrings;
    public byte flags ;
    public Short manufacturer ;
    public ParsedAd() {
         this.uuidStrings = new ArrayList<>();
    }



}
