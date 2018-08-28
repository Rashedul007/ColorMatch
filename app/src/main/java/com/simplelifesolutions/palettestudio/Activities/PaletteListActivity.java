package com.simplelifesolutions.palettestudio.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.simplelifesolutions.palettestudio.Beans.BeanColor;
import com.simplelifesolutions.palettestudio.Beans.BeanImage;
import com.simplelifesolutions.palettestudio.Beans.BeanMain;
import com.simplelifesolutions.palettestudio.Beans.BeanObject;
import com.simplelifesolutions.palettestudio.Beans.BeanObjectList;
import com.simplelifesolutions.palettestudio.Helpers.DatabaseHelper;
import com.simplelifesolutions.palettestudio.Helpers.MyRecycleAdapter_PaletteList;
import com.simplelifesolutions.palettestudio.Helpers.MyVariables;
import com.simplelifesolutions.palettestudio.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PaletteListActivity extends AppCompatActivity implements View.OnClickListener
{
//region...... variables declaration
private Context mContext = PaletteListActivity.this;
    private Toolbar mToolbar;

    private DatabaseHelper mDbHelper;

    private RecyclerView mRecyclerView;
    private MyRecycleAdapter_PaletteList mAdapter;
    private static final int SPAN_COUNT = 2;

    //... for animated fab_main icon
    private Boolean isFabOpen = false;
    private FloatingActionButton fab_main, fab_camera, fab_gallery, fab_colorpicker;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;


//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_palette_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Palette List");

        initialize();
    }

    private void initialize()
    {
        mDbHelper = new DatabaseHelper(this);
        mRecyclerView= (RecyclerView)findViewById(R.id.rcView_paletteList);

        initialise_fab();



    }


    @Override
    protected void onStop() {
        super.onStop();

        resetFabs();
    }

    @Override
    protected void onResume() {
        super.onResume();

        func_getAllPalettesFromDb();
    }

    public void func_getAllPalettesFromDb()
    {
        ArrayList<BeanObjectList> mAllPaletteInfoObj_s_sorted = new ArrayList<BeanObjectList>();

        ArrayList<BeanMain> mDbPltList = mDbHelper.getPaletteList();

        for(BeanMain eachPltObj :mDbPltList )
        {
            //Log.d("LogDbPLt", "PaletteList:: " + eachPltObj.getPaletteID() + "--\t--" + eachPltObj.getPaletteName() + "\n");

            ArrayList<BeanImage> mDbImgList = mDbHelper.getImageListFromPaletteID(eachPltObj.getPaletteID(), 5);
            ArrayList<BeanColor> mDbClrList = mDbHelper.getColorListFromPaletteID(eachPltObj.getPaletteID(), 5);


//++++++++++++++++++++++++++++++++++++

             ArrayList<BeanObject> mEachObjInPaletteList = new ArrayList<BeanObject>();

            for(BeanImage eachImgObjj: mDbImgList)
                {   mEachObjInPaletteList.add( new BeanObject(eachImgObjj, "image", eachImgObjj.getUpdateTime()));    }

            for(BeanColor eachClrObjj: mDbClrList)
                {   mEachObjInPaletteList.add( new BeanObject(eachClrObjj, "color" , eachClrObjj.getUpdateTime()));    }


    // ... sort by date of BeanObject arraylist-mEachObjInPaletteList
            Collections.sort(mEachObjInPaletteList, new Comparator<BeanObject>() {
                public int compare(BeanObject o1, BeanObject o2) {
                    return o1.getDtime().compareTo(o2.getDtime());
                }
            });

    // ... take the top 5 data from arrayList- in a new arrayList
            ArrayList<BeanObject> sorted5list = new ArrayList<BeanObject>();

            if(mEachObjInPaletteList.size()<5)
                sorted5list.addAll(mEachObjInPaletteList.subList(0,mEachObjInPaletteList.size()));
            else
                sorted5list.addAll(mEachObjInPaletteList.subList(0, 5));


            BeanObjectList mObjLst = new BeanObjectList(eachPltObj, sorted5list);
            mAllPaletteInfoObj_s_sorted.add(mObjLst);

        }

        mAdapter = new MyRecycleAdapter_PaletteList(this, mAllPaletteInfoObj_s_sorted);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(),SPAN_COUNT);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();


        mAdapter.setOnItemClickListener(new MyRecycleAdapter_PaletteList.onRecyclerViewItemClickListener()
        {
            @Override
            public void onItemClickListener(View view, int position, String pltID, String pltNm, String cvr_flag, String cvr_ID)
            {
                // Toast.makeText(mContext,"Palette clicked at position:: " + position +"---paletteid::" + pltID_from_Spinner, Toast.LENGTH_SHORT).show();

                Intent intent_DetailsAct = new Intent(mContext, PaletteDetailsActivity.class);
                intent_DetailsAct.putExtra("xtra_pltID_fromListClk", pltID);
                intent_DetailsAct.putExtra("xtra_pltName_fromListClk", pltNm);
                /*    intent_DetailsAct.putExtra("xtra_pltCoverFlag_fromListClk", cvr_flag);
                    intent_DetailsAct.putExtra("xtra_pltCoverID_fromListClk", cvr_ID);*/

                if(view instanceof Button) // share button in actionbar/optionMenu
                    intent_DetailsAct.putExtra("xtra_isShareFromPltLst", "yes");

                intent_DetailsAct.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent_DetailsAct);
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


//region........For fab_main icons

    private void initialise_fab(){
        fab_main = (FloatingActionButton)findViewById(R.id.fabMain);
        fab_camera = (FloatingActionButton)findViewById(R.id.fabCamera);
        fab_gallery = (FloatingActionButton)findViewById(R.id.fabGallery);
        fab_colorpicker = (FloatingActionButton)findViewById(R.id.fabColorPkr);

        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);

        fab_main.setOnClickListener(this);
        fab_camera.setOnClickListener(this);
        fab_gallery.setOnClickListener(this);
        fab_colorpicker.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id){
            case R.id.fabMain:
                animateFAB();

                break;
            case R.id.fabCamera:
                Intent intnt_camera= new Intent(this, ImagePickerActivity.class);
                intnt_camera.putExtra("btnPressed", 1); //1 for camera
                startActivity(intnt_camera);

                break;
            case R.id.fabGallery:
                Intent intnt_galary = new Intent(this, ImagePickerActivity.class);
                intnt_galary.putExtra("btnPressed",2); //2 for gallery
                startActivity(intnt_galary);

                break;

            case R.id.fabColorPkr:
                Intent colorPkrIntent = new Intent(this, ColorPickerActivity.class);
                startActivity(colorPkrIntent);

                break;
        }
    }

    public void animateFAB(){

        if(isFabOpen){

            fab_main.startAnimation(rotate_backward);
            fab_camera.startAnimation(fab_close);
            fab_gallery.startAnimation(fab_close);
            fab_colorpicker.startAnimation(fab_close);
            fab_camera.setClickable(false);
            fab_gallery.setClickable(false);
            fab_colorpicker.setClickable(false);
            isFabOpen = false;
            Log.d("Raj", "close");

        } else {

            fab_main.startAnimation(rotate_forward);
            fab_camera.startAnimation(fab_open);
            fab_gallery.startAnimation(fab_open);
            fab_colorpicker.startAnimation(fab_open);
            fab_camera.setClickable(true);
            fab_gallery.setClickable(true);
            fab_colorpicker.setClickable(true);
            isFabOpen = true;
            Log.d("Raj","open");

        }
    }

    private void resetFabs()
    {
        isFabOpen = false;

        fab_main.startAnimation(rotate_backward);
        fab_camera.startAnimation(fab_close);
        fab_gallery.startAnimation(fab_close);
        fab_colorpicker.startAnimation(fab_close);
        fab_camera.setClickable(false);
        fab_gallery.setClickable(false);
        fab_colorpicker.setClickable(false);
    }

//endregions



}
