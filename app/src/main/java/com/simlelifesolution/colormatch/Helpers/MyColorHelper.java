package com.simlelifesolution.colormatch.Helpers;


import android.graphics.Bitmap;
import android.support.v7.graphics.Palette;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MyColorHelper
{
    public MyColorHelper(){}

    public  ArrayList<String> getDominantColorFromImage(Bitmap bitmap)
    {
        ArrayList<String> mArr = new ArrayList<>();

        List<Palette.Swatch> swatchesTemp = Palette.from(bitmap).generate().getSwatches();
        List<Palette.Swatch> swatches = new ArrayList<Palette.Swatch>(swatchesTemp);

        Collections.sort(swatches, new Comparator<Palette.Swatch>() {
            @Override
            public int compare(Palette.Swatch swatch1, Palette.Swatch swatch2) {
                return swatch2.getPopulation() - swatch1.getPopulation();
            }
        });


        int x=0;

        do{
            int[] rgb1 = getRGBArr(swatches.get(x).getRgb());

            Log.d("LogImgcolor", "" + rgb1);

            String xx1 = String.format("#%02X%02X%02X", rgb1[0], rgb1[1],rgb1[2]);
           // String xx2 = String.format("#%06X", (0xFFFFFF & swatches.get(x).getRgb()));

            mArr.add(xx1);
            x++;
        }while( x < swatches.size());

        return mArr;
    }

    public static int[] getRGBArr(int pixel) {

        int red = (pixel >> 16) & 0xff;
        int green = (pixel >> 8) & 0xff;
        int blue = (pixel) & 0xff;

        return new int[]{red, green, blue};
    }


}
