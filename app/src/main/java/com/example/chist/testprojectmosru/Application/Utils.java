package com.example.chist.testprojectmosru.Application;

import android.content.Context;

import com.example.chist.testprojectmosru.R;

/**
 * Created by 1 on 28.02.2017.
 */
public class Utils {
    public static int getBackGroundColorFromMarker(Context ctx, int marker) {
        switch(marker){
            case 0:
                return ctx.getResources().getColor(R.color.greenalpha);
            case 1:
                return ctx.getResources().getColor(R.color.yellowalpha);
            case 2:
                return ctx.getResources().getColor(R.color.redalpha);
            default:
                return ctx.getResources().getColor(R.color.yellowalpha);
        }
    }
}
