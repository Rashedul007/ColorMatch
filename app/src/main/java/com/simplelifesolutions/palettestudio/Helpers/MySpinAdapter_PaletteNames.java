package com.simplelifesolutions.palettestudio.Helpers;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.simplelifesolutions.palettestudio.Beans.BeanMain;

import java.util.ArrayList;

public class MySpinAdapter_PaletteNames extends ArrayAdapter<BeanMain>
{
    // Your sent context
    private Context context;
    // Your custom values for the spinner (User)
    private ArrayList<BeanMain> countries;

    public MySpinAdapter_PaletteNames(Context context, int textViewResourceId, ArrayList<BeanMain> beanObj)
    {
        super(context, textViewResourceId, beanObj);
        this.context = context;
        this.countries = beanObj;
    }

    public int getCount(){
        return countries.size();
    }

    public BeanMain getItem(int position){
        return countries.get(position);
    }

    public long getItemId(int position){
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        TextView label = new TextView(context);
        label.setPadding(50,5,10,5);
        label.setTextColor(Color.BLACK);
        label.setText(countries.get(position).getPaletteName());

        return label;
    }

    @Override
    public View getDropDownView(int position, View convertView,
                                ViewGroup parent) {
        TextView label = new TextView(context);
        label.setPadding(50,5,10,5);
        label.setTextColor(Color.BLACK);
        label.setText(countries.get(position).getPaletteName());

        return label;
    }


}
