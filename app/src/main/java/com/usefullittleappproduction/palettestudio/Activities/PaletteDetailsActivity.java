package com.usefullittleappproduction.palettestudio.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.usefullittleappproduction.palettestudio.Beans.BeanColor;
import com.usefullittleappproduction.palettestudio.Beans.BeanImage;
import com.usefullittleappproduction.palettestudio.Beans.BeanMain;
import com.usefullittleappproduction.palettestudio.Beans.BeanObject;
import com.usefullittleappproduction.palettestudio.Helpers.DatabaseHelper;
import com.usefullittleappproduction.palettestudio.Helpers.MyColorHelper;
import com.usefullittleappproduction.palettestudio.Helpers.MyImageHelper;
import com.usefullittleappproduction.palettestudio.Helpers.MyRecycleAdapter_PaletteDetails;
import com.usefullittleappproduction.palettestudio.Helpers.MyVariables;
import com.usefullittleappproduction.palettestudio.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.widget.PopupWindow;
import android.view.ViewGroup.LayoutParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PaletteDetailsActivity extends AppCompatActivity
{
//region...... variables declaration
    private Context mContext = PaletteDetailsActivity.this;
    private Toolbar mToolbar;
    private final int COVERFLAG_NULL = 0;
    private final int COVERFLAG_IMAGE = 1;
    private final int COVERFLAG_COLOR = 2;

    Bundle extras;

    ImageView mImgViewCover;
    RecyclerView mRecycleVw;

    String intent_pltID, intent_pltName;

    private DatabaseHelper mDbHelper ;
    private MyRecycleAdapter_PaletteDetails mAdapter;
    private static final int SPAN_COUNT = 2;

    String intent_CoverFlag = "";
    String intent_CoverID = "";
    ArrayList<String> cover_arr;

    Boolean flag_isCoverImgPopUpOn = false;

    ArrayList<BeanObject> mEachObjInPaletteList;



//----- for sharing options

    private ProgressDialog dialog = null;
    private JSONObject jsonObject;

    String mFlag_cover_imgOrColor = "";
    String mCover_imgName_or_ColorCode = "";

    ArrayList<String> mArray_Flag_ImageOrColor;
    ArrayList<String> mArray_encodedImage_or_colorCode;
    ArrayList<String> mArray_ImageName_or_ColorName;

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

         extras = getIntent().getExtras();

        if (extras != null) {
            intent_pltID = extras.getString("xtra_pltID_fromListClk");

             intent_pltName = extras.getString("xtra_pltName_fromListClk");
            actionBar.setTitle(intent_pltName);

             cover_arr = mDbHelper.getCoverFromID(intent_pltID);

            // // cover_arr >>> ( CoverID, Flag, Name, Path/code ) ////

            if(cover_arr.get(1).equals("image"))
                {
                    if(cover_arr.get(0).equals("0"))
                    { mImgViewCover.setBackgroundResource(R.mipmap.icon_no_image);
                     mImgViewCover.setTag(COVERFLAG_NULL);

                     mFlag_cover_imgOrColor = "none";
                    }
                    else
                    {
                        //mImgViewCover.setBackground(Drawable.createFromPath(cover_arr.get(4)));
                        try {
                        Bitmap cameraBitmap = MyImageHelper.rotateImageFromURI(mContext, Uri.fromFile(new File(cover_arr.get(3))));
                        mImgViewCover.setImageBitmap(cameraBitmap);
                        }
                        catch(Exception exp)
                        {   Log.d("error_colorApp", exp.toString()); }

                        mImgViewCover.setTag(COVERFLAG_IMAGE);

                        mFlag_cover_imgOrColor = "image";
                        mCover_imgName_or_ColorCode = cover_arr.get(2);
                    }
                }
            else if(cover_arr.get(1).equals("color"))
                {
                    mImgViewCover.setBackgroundColor(Color.parseColor(cover_arr.get(3)));
                    mImgViewCover.setTag(COVERFLAG_COLOR);

                    mFlag_cover_imgOrColor = "color";
                    mCover_imgName_or_ColorCode = cover_arr.get(3);
                }




        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        func_getPaletteDetailFromDB();

        if(extras.containsKey("xtra_isShareFromPltLst"))
            if(extras.getString("xtra_isShareFromPltLst").equals("yes"))
                func_convertToBase64_forSharing();
    }

    private void  initialize()
    {
        mDbHelper = new DatabaseHelper(this);
        mImgViewCover = (ImageView) findViewById(R.id.imgVw_detailActivityCover);

        //get cover image from DB and fillup
       // mImgViewCover.setImageResource(R.mipmap.colorpicker);

        mRecycleVw = (RecyclerView)findViewById(R.id.rcView_paletteDetails);

//----- for palette sharing
        jsonObject = new JSONObject();
        mArray_Flag_ImageOrColor = new ArrayList<>();
        mArray_encodedImage_or_colorCode = new ArrayList<>();
        mArray_ImageName_or_ColorName = new ArrayList<>();

        dialog = new ProgressDialog(this);
        dialog.setMessage("Uploading Image...");
        dialog.setCancelable(false);
    }

    private void  func_getPaletteDetailFromDB()
    {
        BeanMain mDbPltObj = mDbHelper.getPaletteObjFromID(intent_pltID);
        ArrayList<BeanImage> mDbImgList = mDbHelper.getImageListFromPaletteID(intent_pltID, 0);
        ArrayList<BeanColor> mDbClrList = mDbHelper.getColorListFromPaletteID(intent_pltID, 0);

//++++++++++++++++++++++++++++++++++++

         mEachObjInPaletteList = new ArrayList<BeanObject>();

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

//region.... if instance of Button - for camera button
                    if(view instanceof Button) {
                        Intent intntCameraAct = new Intent(mContext, CallingCameraActivity.class);
                            intntCameraAct.putExtra("xtra_inside_plt_ID", intent_pltID);
                            intntCameraAct.putExtra("xtra_inside_plt_name", intent_pltName);
                            intntCameraAct.putExtra("xtra_img_path", mImgObj.getimagePath());
                            intntCameraAct.putExtra("xtra_flag_imgOrClr", "image");
                        startActivity(intntCameraAct);
//                        finish();
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

                            intnt.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
                            finish();

                        }
//region.... if instance of imageview
                        else if(view instanceof ImageView) {
                        Intent intent = new Intent(mContext, ColorMatchingActivity.class);
                        intent.putExtra("intnt_colorCode", mClrObj.getColorCode());
                        startActivity(intent);
                            finish();
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
           // Toast.makeText(mContext, "Share clicked", Toast.LENGTH_SHORT).show();
            func_convertToBase64_forSharing();
        }
        if(id == R.id.menu_item_customcamera){
           // Toast.makeText(mContext, mImgViewCover.getTag().toString(), Toast.LENGTH_SHORT).show();
            if(Integer.valueOf(mImgViewCover.getTag().toString()) == COVERFLAG_NULL)
                Toast.makeText(mContext,"Sorry, there isn't any cover item.", Toast.LENGTH_SHORT).show();
            else  if(Integer.valueOf(mImgViewCover.getTag().toString()) == COVERFLAG_IMAGE)
            {
                Intent intntCameraAct = new Intent(mContext, CallingCameraActivity.class);
                intntCameraAct.putExtra("xtra_inside_plt_ID", intent_pltID);
                intntCameraAct.putExtra("xtra_inside_plt_name", intent_pltName);
                intntCameraAct.putExtra("xtra_img_path", cover_arr.get(3));
                intntCameraAct.putExtra("xtra_flag_imgOrClr", "image");
                startActivity(intntCameraAct);
                finish();
                }
            else  if(Integer.valueOf(mImgViewCover.getTag().toString()) == COVERFLAG_COLOR)
            {Intent intntCameraAct = new Intent(mContext, CallingCameraActivity.class);
                intntCameraAct.putExtra("xtra_inside_plt_ID", intent_pltID);
                intntCameraAct.putExtra("xtra_inside_plt_name", intent_pltName);
                intntCameraAct.putExtra("xtra_colorCode", cover_arr.get(3) );
                intntCameraAct.putExtra("xtra_flag_imgOrClr", "color");
                startActivity(intntCameraAct);
                finish();
            }
        }
        return super.onOptionsItemSelected(item);
    }


    private void func_convertToBase64_forSharing()
    {
        mArray_Flag_ImageOrColor.clear();
        mArray_encodedImage_or_colorCode.clear();
        mArray_ImageName_or_ColorName.clear();


        for (BeanObject _beanObj: mEachObjInPaletteList)
        {
            String flag_imgOrClr = _beanObj.getFlag_imgOrClr();

            if(flag_imgOrClr.equals("image"))
            {
                BeanImage _imgObj = (BeanImage)_beanObj.getAnyObjLst();

                String share_str_imgPath =   _imgObj.getimagePath();
                Bitmap bitmap = BitmapFactory.decodeFile(share_str_imgPath);
                // Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                String encodedImage = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.DEFAULT);

                mArray_Flag_ImageOrColor.add("image");
                mArray_encodedImage_or_colorCode.add(encodedImage);
                mArray_ImageName_or_ColorName.add(_imgObj.getimageName());

            }

            else if(flag_imgOrClr.equals("color"))
            {
                BeanColor _clrObj = (BeanColor)_beanObj.getAnyObjLst();

                mArray_Flag_ImageOrColor.add("color");
                mArray_encodedImage_or_colorCode.add(_clrObj.getColorCode());
                mArray_ImageName_or_ColorName.add(_clrObj.getColorName());

            }

        }


        func_share_palette();
    }


//region ...share palette

    private void func_share_palette()
    {
        dialog.show();

        JSONArray jsonArray_encodedImg = new JSONArray();
        JSONArray jsonArray_ImgName = new JSONArray();
        JSONArray jsonArray_flag = new JSONArray();

        Log.d("msg_frm_server", "mArray_encodedImage_or_colorCode size :  " + mArray_encodedImage_or_colorCode.size());

        if (mArray_encodedImage_or_colorCode.isEmpty()){
            Toast.makeText(this, "Please select some images first.", Toast.LENGTH_SHORT).show();
            dialog.cancel();
            return;
        }

        for (String encoded: mArray_encodedImage_or_colorCode){
            jsonArray_encodedImg.put(encoded);    }

        for (String eachName: mArray_ImageName_or_ColorName){
            jsonArray_ImgName.put(eachName);  }

        for (String eachflag: mArray_Flag_ImageOrColor){
            jsonArray_flag.put(eachflag);  }

        try {
            jsonObject.put(MyVariables.paletteName, intent_pltName);
            jsonObject.put(MyVariables.coverFlag, mFlag_cover_imgOrColor);
            jsonObject.put(MyVariables.coverSrc, mCover_imgName_or_ColorCode);

            jsonObject.put(MyVariables.flagImgOrColor, jsonArray_flag);
            jsonObject.put(MyVariables.imageName, jsonArray_ImgName); //imageName goes here instead of imgCOlor
            jsonObject.put(MyVariables.imageOrColorCode, jsonArray_encodedImg);
        } catch (JSONException e) {
            Log.e("JSONObject Here", e.toString());
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, MyVariables.url_share_palette, jsonObject,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response_jsonObj) {
                        try {
                            // Log.e("msg_frm_server", response_jsonObj.toString());
                            String resMsg = response_jsonObj.getString("msg_img_upload");
                            Log.d("msg_frm_server","Respose html is:: " + resMsg.toString()) ;

                            String result_shareID = response_jsonObj.getString("msg_shareID").trim();

                            Log.d("msg_frm_server","shareID:: " + result_shareID) ;

                            String str_error_substr = result_shareID.substring(0, Math.min(result_shareID.length(), 5));

                            if(str_error_substr.equals("Error"))
                                {   Toast.makeText(getApplication(), "" + result_shareID , Toast.LENGTH_LONG).show(); }
                            else {
                                String str_sms = "http://simple-life-solutions.com/ceye/colorapp_share_response.php?arg_shareID="+result_shareID ;
                                //Log.d("msg_frm_server","URL should be:: " + str_sms) ;
                                // sendSMSwithOutSmsApp("+447826305563", str_sms); //rausan
                                sendSmsApp(str_sms);
                            }



                        }
                        catch(Exception exp){Log.d("msg_frm_server", exp.toString()) ;}


                        dialog.dismiss();

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.e("msg_frm_server", volleyError.toString());
                Toast.makeText(getApplication(), "Error Occurred", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy( 200*30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }


    public void sendSMSwithOutSmsApp(String phoneNo, String msg) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, msg, null, null);
            Toast.makeText(getApplicationContext(), "Message Sent", Toast.LENGTH_LONG).show();
        } catch (Exception ex) {
            Toast.makeText(getApplicationContext(),ex.getMessage().toString(),  Toast.LENGTH_LONG).show();
            ex.printStackTrace();
        }
    }

    public void sendSmsApp(String argMsg)
    {
        Intent smsIntent = new Intent(Intent.ACTION_VIEW);
        smsIntent.setType("vnd.android-dir/mms-sms");
        smsIntent.putExtra("sms_body",argMsg);
        startActivity(smsIntent);
    }

//endregion

}
