package com.simlelifesolution.colormatch.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.simlelifesolution.colormatch.Beans.BeanColor;
import com.simlelifesolution.colormatch.Beans.BeanImage;
import com.simlelifesolution.colormatch.Beans.BeanMain;
import com.simlelifesolution.colormatch.Beans.BeanObject;
import com.simlelifesolution.colormatch.Beans.BeanObjectList;
import com.simlelifesolution.colormatch.Helpers.DatabaseHelper;
import com.simlelifesolution.colormatch.Helpers.MyRecycleAdapter_PaletteList;
import com.simlelifesolution.colormatch.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class PaletteListActivity extends AppCompatActivity
{
//region...... variables declaration
    private Toolbar mToolbar;

    private DatabaseHelper mDbHelper;

    private RecyclerView mRecyclerView;
    private MyRecycleAdapter_PaletteList mAdapter;
    private static final int SPAN_COUNT = 2;
//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_color_palette_list);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("My Palette List");

        mDbHelper = new DatabaseHelper(this);
        mRecyclerView= (RecyclerView)findViewById(R.id.rcView_paletteList);

       // func_getAllPalettesFromDb();
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
                   // Toast.makeText(PaletteListActivity.this,"Palette clicked at position:: " + position +"---paletteid::" + pltID_from_Spinner, Toast.LENGTH_SHORT).show();

                Intent intent_DetailsAct = new Intent(PaletteListActivity.this, PaletteDetailsActivity.class);
                    intent_DetailsAct.putExtra("xtra_pltID_fromListClk", pltID);
                    intent_DetailsAct.putExtra("xtra_pltName_fromListClk", pltNm);
                    intent_DetailsAct.putExtra("xtra_pltCoverFlag_fromListClk", cvr_flag);
                    intent_DetailsAct.putExtra("xtra_pltCoverID_fromListClk", cvr_ID);
                startActivity(intent_DetailsAct);
            }
        });

    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }



}
