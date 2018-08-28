package com.simplelifesolutions.palettestudio.Activities;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.simplelifesolutions.palettestudio.Fragment.Camera2Fragment;
import com.simplelifesolutions.palettestudio.R;

public class Camera2Activity extends AppCompatActivity {


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera2);

       if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, Camera2Fragment.newInstance())
                    .commit();
        }
    }

    /*@Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }*/


}
