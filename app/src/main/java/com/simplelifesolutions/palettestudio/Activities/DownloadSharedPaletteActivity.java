package com.simplelifesolutions.palettestudio.Activities;

import android.Manifest;
import android.app.DownloadManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.simplelifesolutions.palettestudio.Beans.BeanColor;
import com.simplelifesolutions.palettestudio.Beans.BeanDownloadedShareJson;
import com.simplelifesolutions.palettestudio.Beans.BeanDownloadedShare_details;
import com.simplelifesolutions.palettestudio.Beans.BeanImage;
import com.simplelifesolutions.palettestudio.Beans.BeanMain;
import com.simplelifesolutions.palettestudio.Beans.BeanObject;
import com.simplelifesolutions.palettestudio.Helpers.DatabaseHelper;
import com.simplelifesolutions.palettestudio.Helpers.MyImageHelper;
import com.simplelifesolutions.palettestudio.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.ArrayList;

public class DownloadSharedPaletteActivity extends AppCompatActivity
{
    TextView txtVwTest;
    private  String strJsonResponseForAppReQuest;

    private DatabaseHelper myDbHelper;
    private ProgressDialog pDialog = null;

    Uri uri ;
    String strUri ;
    String strShareIdFromUri;
    Context mContext = DownloadSharedPaletteActivity.this;

//..... for download manager
    private DownloadManager downloadManager;
    private long dlManagerRefId;
    ArrayList<Long> list_dlManager_RefIDs = new ArrayList<>();
    NotificationManager notifManager;

    private ArrayList<String> array_downloaded_imgNamesInSubDir;
    private ArrayList<BeanObject> array_anyObj;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download_shared_palette);

        if(!isStoragePermissionGranted())
            {       }


        initialize();

        if(getIntent().getData() != null)
            {    uri = getIntent().getData();       }
        else{     txtVwTest.setText("no intent for url");      }


       //  uri =  Uri.parse("https://simple-life-solutions.com/ceye/colorapp_share_response.php?arg_shareID=20180830192735");
        String path = uri.getPath();

         strUri = uri.toString();
         strShareIdFromUri = strUri.substring(strUri.lastIndexOf("=") + 1);

    }


    /**
     * Dispatch onPause() to fragments.
     */
    @Override
    protected void onPause() {
        super.onPause();

        funcCancelDownloads();

        if(pDialog != null && pDialog.isShowing())
            pDialog.cancel();

    }

    private void initialize() {
        txtVwTest = (TextView)findViewById(R.id.txtVwDl_test);
        txtVwTest.setMovementMethod(new ScrollingMovementMethod());

        myDbHelper = new DatabaseHelper(this);

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        strJsonResponseForAppReQuest="";

        array_downloaded_imgNamesInSubDir = new ArrayList<String>();

        array_anyObj = new ArrayList<BeanObject>();


        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Downloading files...");
        pDialog.setCancelable(true);
        pDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                funcCancelDownloads();
                dialog.dismiss();
            }
        });
    }





    public void onClick_download(View vw)
    {
        switch (vw.getId())
        {
            case R.id.btn_download_start:
                 pDialog.show();
            //    Log.d("Log_dlTest" , "bUTTON clICKED");

                list_dlManager_RefIDs.clear();
                array_anyObj.clear();
                registerReceiver(downloadManagerOnCompleteReciver,   new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                funcGetJsonResponseForApp(strShareIdFromUri);

                break;


            default:
                break;
        }
    }


    private void funcGetJsonResponseForApp(String argStrShareID)
    {
        String strUrlForAppRequest = "https://simple-life-solutions.com/ceye/colorapp_share_response_app.php?arg_shareID=" + argStrShareID  ;

        RequestQueue queue = Volley.newRequestQueue(this);


        StringRequest stringRequest = new StringRequest(Request.Method.GET, strUrlForAppRequest,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        txtVwTest.setText("Response is: "+ response);
                        strJsonResponseForAppReQuest = response;

                        //Log.d("testUri",  response + "\n");

                        funcParseJson(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                txtVwTest.setText("That didn't work!");
                strJsonResponseForAppReQuest = "Error";
              //  Log.e("testUri",  error.toString() + "\n");
            }
        });

        queue.add(stringRequest);
    }



    private void funcParseJson(String argStrJsonData)
    {
        Long  paletteID_pkDB = -1L  ;

        try {
            JSONObject jObject = new JSONObject(argStrJsonData);

            String strShare_PaletteName = jObject.getString("pltName");
            String strShare_PaletteCoverFlag = jObject.getString("pltCoverFlag");
            String strShare_PaletteCoverPathOrCode = jObject.getString("pltCoverPathOrCode");


//region... create a new palette

            if (myDbHelper.checkDuplicatePltName(strShare_PaletteName))
                {   strShare_PaletteName = strShare_PaletteName + String.valueOf(System.currentTimeMillis());          }


            BeanMain _PaletteObj = new BeanMain("NULL", strShare_PaletteName, "image", "0", "");

            paletteID_pkDB = myDbHelper.createNewPalette(_PaletteObj);

      //  Log.d("Log_dlTest" , "Palette Created name & ID:: " + strShare_PaletteName + "--" +paletteID_pkDB);


            if (paletteID_pkDB != -1)   //new palette created successfully
            {
                ArrayList<BeanDownloadedShare_details> array_BeanShareDetails = new ArrayList<BeanDownloadedShare_details>();

                String strShare_PaletteDetailArray = jObject.getString("pltDetailArray");

                JSONArray jSonDetailArray = new JSONArray(strShare_PaletteDetailArray);

                for (int i = 0; i < jSonDetailArray.length(); i++)
                {
                    JSONObject eachObjInJsonArray = jSonDetailArray.getJSONObject(i);

                    String resArr_flag = eachObjInJsonArray.getString("flagImgOrcolor");
                    String resArr_name = eachObjInJsonArray.getString("nameImgOrColor");
                    String resArr_pathOrCode = eachObjInJsonArray.getString("imgPath_or_colorCode");

                    BeanDownloadedShare_details  obj_each_BeanShareDetails = new BeanDownloadedShare_details(resArr_flag , resArr_name, resArr_pathOrCode);

                    array_BeanShareDetails.add(obj_each_BeanShareDetails);
                }


                BeanDownloadedShareJson obj_ShareJson = new BeanDownloadedShareJson(strShare_PaletteName, strShare_PaletteCoverFlag,
                        strShare_PaletteCoverPathOrCode, array_BeanShareDetails);

                funcDownloadImages(obj_ShareJson , String.valueOf(paletteID_pkDB));

            }
            else // problem createing new palette
             {  Toast.makeText(mContext, "There was a problem downloading palette. \n New Palette cannot be created in DB.", Toast.LENGTH_SHORT).show();                     }


//endregion

        } catch (Exception e) {
            e.printStackTrace();
        }


    }



//region.. funcDownloadImages() for downloading images from JSON response

    private void funcDownloadImages(BeanDownloadedShareJson beanObj_shareJson, String newPltID)
    {
        array_anyObj.clear();

        int str_totalDlNeeded = beanObj_shareJson.getArray_paletteDetail().size();

        String rootPth = this.getExternalFilesDir(null).toString();

        for(int i=0; i<str_totalDlNeeded ; i++)
        {
            BeanDownloadedShare_details obj_each_shareDetail =  beanObj_shareJson.getArray_paletteDetail().get(i);

            if(obj_each_shareDetail.getFlag_imgOrColor().equals("image"))
            {
                String  img_Time_Name = String.valueOf(System.currentTimeMillis());

                String str_appendImgUri = "https://simple-life-solutions.com/ceye/" +  obj_each_shareDetail.getImgPath_or_ColorCode().toString();

                Uri imgUri = Uri.parse(str_appendImgUri);

                int downladingNumber = i+1;
                String imgDirPath = "/ColorappImgs/colorappImg_" + img_Time_Name + "_shareDl.png"   ;

                 // Log.d("testUri",  imgUri.toString() + "\n");

                DownloadManager.Request request = new DownloadManager.Request(imgUri);
                request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                request.setAllowedOverRoaming(false);
                request.setTitle("Downloading file " + downladingNumber + " out of total " + str_totalDlNeeded);
                request.setDescription("Downloading file name: " + obj_each_shareDetail.getName_imgOrColor() );
                request.setVisibleInDownloadsUi(true);
                request.setDestinationInExternalFilesDir(mContext, null , imgDirPath);
                //request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/GadgetSaint/"  + "/" + "Sample_" + i + ".png");


                dlManagerRefId = downloadManager.enqueue(request);


               // Log.d("Log_dlTest", "" + dlManagerRefId);

//region----------save the image paths in global arrayList

                String str_img_pth = rootPth + imgDirPath;

                String xtra_cover_flag = "";


     //--- check if cover is image and same path ---------
                if(obj_each_shareDetail.getImgPath_or_ColorCode().equals(beanObj_shareJson.getPalette_CoverPathOrCode())
                     &&  beanObj_shareJson.getPaletteCover_flag().equals("image"))
                    {   xtra_cover_flag = "cover" ;}
                else xtra_cover_flag = "";


    //--- check in Db if image name exists , if exist then rename it.
                if (myDbHelper.checkDuplicateImgName(obj_each_shareDetail.getName_imgOrColor()))
                    {obj_each_shareDetail.setName_imgOrColor(obj_each_shareDetail.getName_imgOrColor() + img_Time_Name)  ;}



    //--- pass the cover flag in image_id ---------
                BeanImage _imgObj = new BeanImage(xtra_cover_flag , newPltID, str_img_pth, "", obj_each_shareDetail.getName_imgOrColor(), img_Time_Name);

                array_anyObj.add(new BeanObject(_imgObj, "image" , ""));
//endregion

                list_dlManager_RefIDs.add(dlManagerRefId);


                //obj_each_shareDetail.getImgPath_or_ColorCode();

               // Log.d("Log_dlTest" ,  obj_each_shareDetail.getImgPath_or_ColorCode() + "--RefIDs: " + String.valueOf(dlManagerRefId));
            }



            else if(obj_each_shareDetail.getFlag_imgOrColor().equals("color"))
            { String xtra_cover_flag = "";

                String  img_Time_Name = String.valueOf(System.currentTimeMillis());


                if(obj_each_shareDetail.getImgPath_or_ColorCode().equals(beanObj_shareJson.getPalette_CoverPathOrCode())
                        &&  beanObj_shareJson.getPaletteCover_flag().equals("color"))
                    { xtra_cover_flag = "cover" ;}
                else xtra_cover_flag = "";


         //--- check in Db if color name exists , if exist then rename it.
                if (myDbHelper.checkDuplicateColorName(obj_each_shareDetail.getName_imgOrColor()))
                {obj_each_shareDetail.setName_imgOrColor(obj_each_shareDetail.getName_imgOrColor() + img_Time_Name)  ;
                  //  Log.d("Log_dlTest", "" + obj_each_shareDetail.getName_imgOrColor());
                }


        //--- pass the cover flag in color_id ---------
                BeanColor _clrObj = new BeanColor(xtra_cover_flag , newPltID , obj_each_shareDetail.getImgPath_or_ColorCode() , obj_each_shareDetail.getName_imgOrColor() , "") ;
                array_anyObj.add(new BeanObject(_clrObj, "color" , ""));


            }


        }

    }

//endregion




//region..cancel downloads & unregister receiver

    private void funcCancelDownloads()
    {
        if (!list_dlManager_RefIDs.isEmpty())
        {
                for(Long each_dlManagerRefIDs : list_dlManager_RefIDs)
                {
                   // Log.d("Log_dlTest" , "Removing id:: " + String.valueOf(each_dlManagerRefIDs) );
                    downloadManager.remove(each_dlManagerRefIDs)    ;



                }

            if(downloadManagerOnCompleteReciver!=null)
                unregisterReceiver(downloadManagerOnCompleteReciver);
        }
    }

    //endregion



//region...BroadcastReceiver downloadManagerOnCompleteReciver

    BroadcastReceiver downloadManagerOnCompleteReciver = new BroadcastReceiver()
    {
        public void onReceive(Context ctxt, Intent intent)
        {
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

          //  Log.e("Log_dlTest", "" + referenceId);

            list_dlManager_RefIDs.remove(referenceId);


            if (list_dlManager_RefIDs.isEmpty())
            {
                //Log.e("Log_dlTest", "" + referenceId);

                txtVwTest.setText("All download done. Please wait for saving the files in database.");

             //   pDialog.dismiss();

                funcMyNotification("All Download completed.");

                saveImageNcolor_withThumbMake();


            }

        }
    };


//endregion


//region.. make thumbnail & save image, color info in dataBase

    private void saveImageNcolor_withThumbMake()
    {
       // Log.d("Log_dlTest" , "Inside saveImgeWithThumbMake");

        for(BeanObject eachAnyObj:array_anyObj)
        {
            if(eachAnyObj.getFlag_imgOrClr().equals("image"))
            {
                BeanImage obj_forImg = (BeanImage)eachAnyObj.getAnyObjLst();

                //-----thumbnail create-------------------

               Bitmap  mBitmapForThumb = MyImageHelper.getBitmapFromPath(obj_forImg.getimagePath());

                InputStream thumbInputStream_gallery = MyImageHelper.func_createThumbs_ReturnInStream(mBitmapForThumb);


                mBitmapForThumb = BitmapFactory.decodeStream(thumbInputStream_gallery);

               String strThumbPath = MyImageHelper.func_giveThumbBitmap_aFileName(mContext, mBitmapForThumb, obj_forImg.getUpdateTime()+"_shareDl" );


            //----- insert into DB

                BeanImage _imgObjFOrDbSave = new BeanImage("", obj_forImg.getPaletteID(), obj_forImg.getimagePath(), strThumbPath, obj_forImg.getimageName(), String.valueOf(System.currentTimeMillis()));
                Long dbImgInsertID_dlPlt = myDbHelper.insert_newImage(_imgObjFOrDbSave);

            //    Log.d("Log_dlTest", "Image Created With ID:: " + dbImgInsertID_dlPlt);

                if (dbImgInsertID_dlPlt == -1)
                    Toast.makeText(mContext, "Something went wrong when saving the image in DB!", Toast.LENGTH_SHORT).show();
                else {
//------update cover
                    if (obj_forImg.getimageId().equals("cover")) {
                        Long dbUpdateCover = myDbHelper.updateCoverInPalette( obj_forImg.getPaletteID(), "image", dbImgInsertID_dlPlt.toString() );

                     //   Log.d("Log_dlTest", "Cover image in DB:: " + dbUpdateCover);
                    }   }

            }


            else if(eachAnyObj.getFlag_imgOrClr().equals("color"))
            {
                BeanColor obj_forClr = (BeanColor)eachAnyObj.getAnyObjLst();

                BeanColor _clrObjFOrDbSave = new BeanColor("NULL", obj_forClr.getPaletteID(), obj_forClr.getColorCode(), obj_forClr.getColorName(), String.valueOf(System.currentTimeMillis()) );
                Long dbClrInsertID_dlPlt = myDbHelper.insert_newColor(_clrObjFOrDbSave);

               Log.d("Log_dlTest", "Color Created With ID:: " + dbClrInsertID_dlPlt);

                if (dbClrInsertID_dlPlt == -1)
                    Toast.makeText(mContext, "Something went wrong when saving the color in DB!", Toast.LENGTH_SHORT).show();
                else {
                    if (obj_forClr.getColorId().equals("cover")) {
                        Long dbUpdateCover = myDbHelper.updateCoverInPalette( obj_forClr.getPaletteID(), "color", dbClrInsertID_dlPlt.toString() );
                     //   Log.d("Log_dlTest", "cover Color Created With ID:: " + dbUpdateCover);
                    }   }
            }
        }

        pDialog.dismiss();

        txtVwTest.setText("Success! Downloaded palette successfuly saved.");

      //  Log.d("Log_dlTest" , "Inside saveImgeWithThumbMake ended...");


        Intent intnt_openMainActivity = new Intent(mContext , MainActivity.class);
        startActivity(intnt_openMainActivity);
        finish();

    }


//endregion



//region.. Notification manager channel for version>26 or Android-o

    public void funcMyNotification(String aMessage)
     {
        final int NOTIFY_ID = 0; // ID of notification
        String id = getString(R.string.default_notification_channel_id); // default channel id
        String title = getString(R.string.default_notification_channel_title); // default channel
        Intent intent;
        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        if (notifManager == null) {
            notifManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = notifManager.getNotificationChannel(id);
            if (mChannel == null) {
                mChannel = new NotificationChannel(id, title, importance);
                mChannel.enableVibration(true);
                mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                notifManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(this, id);
            //intent = new Intent(this, MainActivity.class);
            intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            builder.setContentTitle(aMessage)  // required
                    //.setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setSmallIcon(R.mipmap.ic_launcher) // required
                    .setContentText(this.getString(R.string.app_name))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
        } else {
            builder = new NotificationCompat.Builder(this, id);
            //intent = new Intent(this, MainActivity.class);
            intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
            builder.setContentTitle(aMessage)                           // required
                    .setSmallIcon(android.R.drawable.ic_popup_reminder) // required
                    .setContentText(this.getString(R.string.app_name))  // required
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setTicker(aMessage)
                    .setVibrate(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400})
                    .setPriority(Notification.PRIORITY_HIGH);
        }
        Notification notification = builder.build();
        notifManager.notify(NOTIFY_ID, notification);


     //   Log.d("Log_dlTest", "------" + "inside notification");
    }

//endregion


//region .... get storage permission
public  boolean isStoragePermissionGranted() {
    if (Build.VERSION.SDK_INT >= 23) {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            return false;
        }
    }
    else { //permission is automatically granted on sdk<23 upon installation
        return true;
    }
}




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            // permission granted
        }
    }

//endregion


}
