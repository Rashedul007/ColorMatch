package com.simlelifesolution.colormatch.Activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.simlelifesolution.colormatch.Beans.BeanColor;
import com.simlelifesolution.colormatch.Beans.BeanImage;
import com.simlelifesolution.colormatch.Beans.BeanMain;
import com.simlelifesolution.colormatch.Beans.BeanObject;
import com.simlelifesolution.colormatch.Helpers.DatabaseHelper;
import com.simlelifesolution.colormatch.Helpers.MyColorHelper;
import com.simlelifesolution.colormatch.Helpers.MyImageHelper;
import com.simlelifesolution.colormatch.Helpers.MyRecycleAdapter_PaletteDetails;
import com.simlelifesolution.colormatch.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.widget.PopupWindow;
import android.view.ViewGroup.LayoutParams;

public class PaletteDetailsActivity extends AppCompatActivity
{
//region...... variables declaration
    private Context mContext = PaletteDetailsActivity.this;
    private Toolbar mToolbar;

    ImageView mImgViewCover;
    RecyclerView mRecycleVw;

    String intent_pltID, intent_pltName;

    private DatabaseHelper mDbHelper ;
    private MyRecycleAdapter_PaletteDetails mAdapter;
    private static final int SPAN_COUNT = 2;

    String intent_CoverFlag = "";
    String intent_CoverID = "";

    Boolean flag_isCoverImgPopUpOn = false;
//endregion

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

             intent_pltName = extras.getString("xtra_pltName_fromListClk");
            actionBar.setTitle(intent_pltName);

            ArrayList<String> cover_arr = mDbHelper.getCoverFromID(intent_pltID);

            // //( CoverID, Flag, Name, Path/code ) ////

            if(cover_arr.get(1).equals("image"))
                {
                    if(cover_arr.get(0).equals("0"))
                        mImgViewCover.setBackgroundResource(R.mipmap.icon_no_image);
                    else
                        mImgViewCover.setBackground(Drawable.createFromPath(cover_arr.get(3)));
                }
            else if(cover_arr.get(1).equals("color"))
                {
                    mImgViewCover.setBackgroundColor(Color.parseColor(cover_arr.get(3)));
                }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        func_getPaletteDetailFromDB();
    }

    private void  initialize()
    {
        mDbHelper = new DatabaseHelper(this);
        mImgViewCover = (ImageView) findViewById(R.id.imgVw_detailActivityCover);

        //get cover image from DB and fillup
       // mImgViewCover.setImageResource(R.mipmap.colorpicker);

        mRecycleVw = (RecyclerView)findViewById(R.id.rcView_paletteDetails);
    }

    private void  func_getPaletteDetailFromDB()
    {
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
            //public void onItemClickListener(View view, int position, String flagClrImg, String clrOrImgID, String colorCd)
            @Override
            public void onItemClickListener(View view, int position, String flagClrImg, BeanObject mBeanObj)
            {
//                Toast.makeText(mContext,"Palette clicked at position:: " + position +"---flag::" + flagClrImg +"---id::" + clrOrImgID, Toast.LENGTH_SHORT).show();
//region.... if image
                if(flagClrImg.equals("image"))
                {// Toast.makeText(mContext,"This is a image....Need to work on it", Toast.LENGTH_SHORT).show();
                    BeanImage mImgObj = (BeanImage) mBeanObj.getAnyObjLst();

//region.... if instance of Button - for maera button
                    if(view instanceof Button) {
                        Intent intntCameraAct = new Intent(mContext, CallingCameraActivity.class);
                            intntCameraAct.putExtra("xtra_inside_plt_ID", intent_pltID);
                            intntCameraAct.putExtra("xtra_inside_plt_name", intent_pltName);
                            intntCameraAct.putExtra("xtra_img_path", mImgObj.getimagePath());
                            intntCameraAct.putExtra("xtra_flag_imgOrClr", "image");
                        startActivity(intntCameraAct);

                    }
//endregion

//region.... if instance of imageview
                    else if(view instanceof ImageView) {
                        try {
                            Bitmap mImgBitmap = MyImageHelper.getBitmapFromPath(mImgObj.getimagePath());
                            ArrayList<String> mColorsInImgArr = new MyColorHelper().getDominantColorFromImage(mImgBitmap);

                            Intent intnt = new Intent(mContext, ColorListFromImageActivity.class);
                            intnt.putStringArrayListExtra("xtra_colorList", mColorsInImgArr);
                            intnt.putExtra("xtra_inside_plt_ID", intent_pltID);
                            intnt.putExtra("xtra_inside_plt_name", intent_pltName);
                            intnt.putExtra("xtra_img_path", mImgObj.getimagePath());
                            intnt.putExtra("xtra_img_name", mImgObj.getimageName());
                            startActivity(intnt);
                        } catch (Exception ex) {
                            Log.e(getResources().getString(R.string.common_log), "errs:: " + ex.toString());
                            Toast.makeText(mContext, "There is an error, please contact the support team!", Toast.LENGTH_SHORT);
                        }
                    }
//endregion

                    //new MyColorHelper().getDominantColorFromImage()
                }
//endregion

//region.... if color
                else if(flagClrImg.equals("color")) {
                    {
                        BeanColor mClrObj = (BeanColor) mBeanObj.getAnyObjLst();

                        if(view instanceof Button) {
                            Intent intntCameraAct = new Intent(mContext, CallingCameraActivity.class);
                                intntCameraAct.putExtra("xtra_inside_plt_ID", intent_pltID);
                                intntCameraAct.putExtra("xtra_inside_plt_name", intent_pltName);
                                intntCameraAct.putExtra("xtra_colorCode", mClrObj.getColorCode());
                                intntCameraAct.putExtra("xtra_flag_imgOrClr", "color");
                            startActivity(intntCameraAct);
                        }
//region.... if instance of imageview
                        else if(view instanceof ImageView) {
                        Intent intent = new Intent(mContext, ColorMatchingActivity.class);
                        intent.putExtra("intnt_colorCode", mClrObj.getColorCode());
                        startActivity(intent);
                    }
//endregion
                    }
                }
//endregion
            }
        });


        mAdapter.setCoverUpdateListener(new MyRecycleAdapter_PaletteDetails.onCoverUpdateListener() {
            @Override
            public void onCoverUpdate() {
                finish();
                startActivity(getIntent());
            }
        });
    }


    public void clkCoverImage_details(View v) {
        // Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
        if (intent_CoverFlag.equals("image") && flag_isCoverImgPopUpOn==false )
        { flag_isCoverImgPopUpOn = true;

        LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = layoutInflater.inflate(R.layout.popup_image, null);
        ImageView mImg = (ImageView) popupView.findViewById(R.id.popupimgvw);
        TextView mTxt = (TextView) popupView.findViewById(R.id.popuptxtVw);

//region.........set the image & text
          if (!intent_CoverID.equals("0")) {
                BeanImage _imgObj = mDbHelper.getImageFromImageID(intent_CoverID);
              Uri outputImgUri = Uri.fromFile(new File(_imgObj.getimagePath()));
              try {
                  Bitmap cameraBitmap = MyImageHelper.rotateImageFromURI(this, outputImgUri);
                   mImg.setImageBitmap(cameraBitmap);
              }catch(Exception ex){}

            //   mImg.setBackground(Drawable.createFromPath(_imgObj.getimagePath()));
                mTxt.setText(_imgObj.getimageName().toString());
            } else {
                mImg.setBackgroundResource(R.mipmap.icon_no_image);
                mTxt.setText("There isn't any cover selected for this item.");
            }
//endregion

        final PopupWindow popupWindow = new PopupWindow(popupView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);

        btnDismiss.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v) {  popupWindow.dismiss();  flag_isCoverImgPopUpOn = false;    }});

        popupWindow.showAsDropDown(mImgViewCover, 200, 0);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
          return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.menu_item_share){
            Toast.makeText(mContext, "Share clicked", Toast.LENGTH_SHORT).show();
        }
        if(id == R.id.menu_item_customcamera){
            Toast.makeText(mContext, "menu camera clicked", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }
}
