package com.simlelifesolution.colormatch.Activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.simlelifesolution.colormatch.Beans.BeanColor;
import com.simlelifesolution.colormatch.Beans.BeanImage;
import com.simlelifesolution.colormatch.Beans.BeanMain;
import com.simlelifesolution.colormatch.Beans.BeanObject;
import com.simlelifesolution.colormatch.Helpers.DatabaseHelper;
import com.simlelifesolution.colormatch.Helpers.MyRecycleAdapter_PaletteDetails;
import com.simlelifesolution.colormatch.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;


public class PaletteDetailsActivity extends AppCompatActivity
{
    private Toolbar mToolbar;

    ImageView mImgViewCover;
    RecyclerView mRecycleVw;

    String intent_pltID;

    private DatabaseHelper mDbHelper = new DatabaseHelper(this);
    private MyRecycleAdapter_PaletteDetails mAdapter;
    private static final int SPAN_COUNT = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        initialize();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            intent_pltID = extras.getString("xtra_pltID_fromListClk");
           String intent_pltName = extras.getString("xtra_pltName_fromListClk");
            actionBar.setTitle(intent_pltName);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        func_getPaletteDetailFromDB();
    }

    private void  initialize()
    {
        mImgViewCover = (ImageView) findViewById(R.id.imgVw_detailActivityCover);

        //get cover image from DB and fillup
        mImgViewCover.setImageResource(R.mipmap.colorpicker);

        mRecycleVw = (RecyclerView)findViewById(R.id.rcView_paletteDetails);


    }

    private void  func_getPaletteDetailFromDB()
    {
        //ArrayList<BeanObjectList> mAllPaletteInfoObj_s_sorted = new ArrayList<BeanObjectList>();

        BeanMain mDbPltObj = mDbHelper.getPaletteObjFromID(intent_pltID);
        ArrayList<BeanImage> mDbImgList = mDbHelper.getImageListFromPaletteID(intent_pltID, 0);
        ArrayList<BeanColor> mDbClrList = mDbHelper.getColorListFromPaletteID(intent_pltID, 0);


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


//+++++++++++++++++++++++++++++++++++++++++++++
        mAdapter = new MyRecycleAdapter_PaletteDetails(this, mEachObjInPaletteList);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(),SPAN_COUNT);
        mRecycleVw.setLayoutManager(mLayoutManager);
        mRecycleVw.setItemAnimator(new DefaultItemAnimator());
        mRecycleVw.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();

        mAdapter.setOnItemClickListener(new MyRecycleAdapter_PaletteDetails.onRecyclerViewItemClickListener()
        {
            @Override
            public void onItemClickListener(View view, int position, String flagClrImg, String clrOrImgID)
            {
                Toast.makeText(PaletteDetailsActivity.this,"Palette clicked at position:: " + position +"---flag::" + flagClrImg +"---id::" + clrOrImgID, Toast.LENGTH_SHORT).show();

            }
        });

    }


    @Override
    protected void onStop() {
        super.onStop();

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

}
