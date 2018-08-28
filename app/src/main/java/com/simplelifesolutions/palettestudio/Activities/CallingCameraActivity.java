package com.simplelifesolutions.palettestudio.Activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.Toast;

import com.simplelifesolutions.palettestudio.Beans.BeanImage;
import com.simplelifesolutions.palettestudio.Beans.BeanMain;
import com.simplelifesolutions.palettestudio.Helpers.App;
import com.simplelifesolutions.palettestudio.Helpers.DatabaseHelper;
import com.simplelifesolutions.palettestudio.Helpers.MyImageHelper;
import com.simplelifesolutions.palettestudio.Helpers.MySpinAdapter_PaletteNames;
import com.simplelifesolutions.palettestudio.R;

public class CallingCameraActivity extends AppCompatActivity
{
//region.....variables & ClickListeners
    Context  mContext = CallingCameraActivity.this;
    String getIntent_flag_ImgOrClr, getIntent_pltID, getIntent_pltName, getIntent_imgPath_OR_clrCode;

    private static final int TAG_CAMERA_OLD = 100;
    private static final int TAG_CAMERA_NEW = 12;

    private ImageView mCameraImageView;
    private Bitmap mCameraBitmap;
    private Button mSaveImageButton_new;

    static String img_Time_Name, strImgPath, strThumbPath, strTempImgPath;
    private Uri outputImgUri;

    private DatabaseHelper myDbHelper;
    Long paletteID_pkDB = -1L;

    Long dbImgInsertID_exstPlt = -1L;

    MySpinAdapter_PaletteNames adapter_Spinner;
    public String pltName_from_Spinner = "";
    public String pltID_from_Spinner = "";

    private ProgressDialog pDialog;

    // for dialog view
    EditText mEdtVwPltName_new;
    Spinner mSpinnerPaletteName_exist;
    //endregion

    private static String flag_which_camera = "old" ;


    public void btnOnClick(View view){
        switch (view.getId())
        {
            case R.id.capture_image_button:
                    startImageCapture();
                break;

            case R.id.save_image_new_button:
                    if(flag_which_camera.equals("old"))
                        call_asyncForImageSave();
                    else if(flag_which_camera.equals("new"))
                        addImageToPlt();
                break;
            default:
                break;
        }
    }

//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling_camera);


        mCameraImageView = (ImageView) findViewById(R.id.camera_image_view);
        mSaveImageButton_new = (Button) findViewById(R.id.save_image_new_button);

        mSaveImageButton_new.setEnabled(false);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            getIntent_flag_ImgOrClr = extras.getString("xtra_flag_imgOrClr");

            getIntent_pltID = extras.getString("xtra_inside_plt_ID");
            getIntent_pltName = extras.getString("xtra_inside_plt_name");

            if(getIntent_flag_ImgOrClr.equals("image"))
                getIntent_imgPath_OR_clrCode =  extras.getString("xtra_img_path");
            else if(getIntent_flag_ImgOrClr.equals("color"))
                getIntent_imgPath_OR_clrCode =   extras.getString("xtra_colorCode");

            startImageCapture();

        }

        myDbHelper = new DatabaseHelper(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mCameraBitmap != null && !mCameraBitmap.isRecycled()) {
    mCameraImageView.setImageBitmap(null);
    mCameraBitmap.recycle();
    mCameraBitmap = null;
}
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK)
            {
                if (requestCode == TAG_CAMERA_OLD)
                {   flag_which_camera = "old";

                    byte[] cameraData = App.getInstance().getCapturedPhotoData();
                    App.getInstance().setCapturedPhotoData(null);

                    if (cameraData != null) {
                        mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.length);

                        mCameraImageView.setImageBitmap(mCameraBitmap);
                        mSaveImageButton_new.setEnabled(true);

                        //--------------------------create thumbnail

                        InputStream thumbInputStream = MyImageHelper.func_createThumbs_ReturnInStream(mCameraBitmap);  /////////////////makes the smallest thumnail
                        Bitmap thumbBitmap = BitmapFactory.decodeStream(thumbInputStream);

                        strThumbPath = MyImageHelper.func_giveThumbBitmap_aFileName(mContext, thumbBitmap, img_Time_Name);
                    }
                }
                else if(requestCode == TAG_CAMERA_NEW) {
                    flag_which_camera = "new";
                    mSaveImageButton_new.setEnabled(true);

                    func_onCaptureImageResult_new(data);
                }

            } else {
                mCameraBitmap = null;
                mSaveImageButton_new.setEnabled(false);
                finish();
            }

    }


    private void startImageCapture()
    {    Intent custIntent = new Intent();
            custIntent.putExtra("xtra_inside_plt_ID", getIntent_pltID);
            custIntent.putExtra("xtra_inside_plt_name", getIntent_pltName);
            custIntent.putExtra("xtra_imgPth_or_colorCode", getIntent_imgPath_OR_clrCode);
            custIntent.putExtra("xtra_flag_imgOrClr", getIntent_flag_ImgOrClr);

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
            {  custIntent.setClass(mContext,CustomCameraActivity.class);
                startActivityForResult(custIntent, TAG_CAMERA_OLD);  }
        else
            {   custIntent.setClass(mContext,Camera2Activity.class);
                startActivityForResult(custIntent, TAG_CAMERA_NEW);  }
    }



    private void func_onCaptureImageResult_new(Intent intntData)
    {
        try {
            strImgPath = intntData.getStringExtra("strImgpathFromCam2");
            img_Time_Name = intntData.getStringExtra("strImgTimeNameCam2");
            outputImgUri =  Uri.parse(intntData.getStringExtra("strImgUriCam2"));
            Log.d("log_came2", "from intent:  "+strImgPath + "\t" + img_Time_Name);

            mCameraBitmap = MyImageHelper.rotateImageFromURI(this, outputImgUri);

//            Bitmap cameraBitmap = MyImageHelper.rotateImageFromURI(this, outputImgUri);

            mCameraImageView.setImageBitmap(mCameraBitmap);

                //--------------------------create thumbnail

                InputStream thumbInputStream = MyImageHelper.func_createThumbs_ReturnInStream(mCameraBitmap);  /////////////////makes the smallest thumnail
                Bitmap thumbBitmap = BitmapFactory.decodeStream(thumbInputStream);

                strThumbPath = MyImageHelper.func_giveThumbBitmap_aFileName(mContext, thumbBitmap, img_Time_Name);


            strImgPath =    MyImageHelper.func_rotateResave_delOld(mContext , outputImgUri, strImgPath , img_Time_Name);
           // }
        }catch(Exception ex){
            String errmsg = "inside onActivityResult:: \t"+ex;
            Log.e("TAG_LOG_TAKE_PICTURE", errmsg);
            Toast.makeText(mContext, errmsg, Toast.LENGTH_LONG).show();
        }
    }

//region*****************************save images in DB

    private void addImageToPlt()
    {
        dbImgInsertID_exstPlt = -1L;

        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this);

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_saveimage_topalette, null);

        mAlertBuilder.setPositiveButton("ok", null);
        mAlertBuilder.setNegativeButton("cancel", null);
        mAlertBuilder.setView(promptsView);

        mEdtVwPltName_new = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        final EditText mEdtVwImageName = (EditText) promptsView.findViewById(R.id.etDialogImgName);
        final CheckBox mChkBx = (CheckBox) promptsView.findViewById(R.id.chkBoxCover);
        final RadioButton mRadioExist = (RadioButton) promptsView.findViewById(R.id.rdBtn_Existing);
        final RadioButton mRadioNew = (RadioButton) promptsView.findViewById(R.id.rdBtn_New);

        mSpinnerPaletteName_exist = (Spinner) promptsView.findViewById(R.id.spinner_existingPalette);

        setup_spinnerItems(mSpinnerPaletteName_exist); //------------------ setUp the spinner for existing paletteList

        final AlertDialog mAlertDialog = mAlertBuilder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button btnDialog_positive = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnDialog_positive.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        int returnResult = 0;
                        if (mRadioExist.isChecked())
                            returnResult = func_addImageToExistingPlt(mEdtVwImageName,  mChkBx );

                        else if (mRadioNew.isChecked())
                            returnResult = func_addImageToNewPlt(mEdtVwImageName,  mChkBx );

                        if(returnResult == 1)
                        {  mAlertDialog.dismiss();
                            finish();}
                    }
                });
            }
        });
        mAlertDialog.show();

           // return dbImgInsertID_exstPlt;
    }


    private int func_addImageToNewPlt(EditText mEdtVwImageName,  CheckBox mChkBx )
    {
        if ((mEdtVwPltName_new.getText().toString().trim().length() > 0) && (mEdtVwImageName.getText().toString().trim().length() > 0)) {
            String _pltName = mEdtVwPltName_new.getText().toString();
            String _imgName = mEdtVwImageName.getText().toString();

            if (myDbHelper.checkDuplicatePltName(_pltName)) {
                Toast.makeText(mContext, "Palette Name already exists! Please try another name.", Toast.LENGTH_SHORT).show();
            } else {
                if (myDbHelper.checkDuplicateImgName(_imgName)) {
                    Toast.makeText(mContext, "Image Name already exists! Please try another name.", Toast.LENGTH_SHORT).show();
                } else {
                    BeanMain _PaletteObj = new BeanMain("NULL", _pltName, "image", "0", "");
                    paletteID_pkDB = myDbHelper.createNewPalette(_PaletteObj);

                    if (paletteID_pkDB != -1)   //new palette created successfully
                    {
                        Toast.makeText(mContext, "New Palette created succssfuly!\n Please wait for storing the image. ", Toast.LENGTH_SHORT).show();

                        pltID_from_Spinner = String.valueOf(paletteID_pkDB);

                        BeanImage _imgObj = new BeanImage("", pltID_from_Spinner, strImgPath, strThumbPath, _imgName, "");
                        Long dbImgInsertID = myDbHelper.insert_newImage(_imgObj);

                        Log.d("dbResult_explt", "Image Created DBresult:::" + dbImgInsertID.toString() + " pltID_from_Spinner: " + _imgObj.getPaletteID().toString());

                        if (dbImgInsertID == -1)
                            Toast.makeText(mContext, "Something went wrong when saving the image in existing palette!", Toast.LENGTH_SHORT).show();
                        else {
                            if (mChkBx.isChecked()) {
                                Toast.makeText(mContext, "Image saved succssfuly!", Toast.LENGTH_SHORT).show();
                                Long dbUpdateCover = myDbHelper.updateCoverInPalette(paletteID_pkDB.toString(), "image", dbImgInsertID.toString());
                            }
                            return 1;
                        }
                    } else
                        Toast.makeText(mContext, "Something went wrong when creating a new palette!", Toast.LENGTH_SHORT).show();
                }
            }
        } else    // either PaletteName or ImageName was not given
            Toast.makeText(mContext, "Please check the Palette & Image Name!", Toast.LENGTH_SHORT).show();

        return 0;
    }


    private int func_addImageToExistingPlt(EditText mEdtVwImageName,  CheckBox mChkBx_addAsCover )
    {
        if ((mEdtVwImageName.getText().toString().trim().length() > 0)  && !(pltName_from_Spinner.equals("")))
        {
            String _imgName = mEdtVwImageName.getText().toString();

            if (myDbHelper.checkDuplicateImgName(_imgName)) {
                Toast.makeText(mContext, "Image Name already exists! Please try another name.", Toast.LENGTH_SHORT).show();
            } else {
                BeanImage _imgObj = new BeanImage("", pltID_from_Spinner, strImgPath, strThumbPath, _imgName, "");
                Long dbImgInsertID_exstPlt = myDbHelper.insert_newImage(_imgObj);

                Log.d("dbResult_explt", "Image Created DBresult:::" + dbImgInsertID_exstPlt.toString() + " pltID_from_Spinner: " + _imgObj.getPaletteID().toString());

                if (dbImgInsertID_exstPlt == -1)
                    Toast.makeText(mContext, "Something went wrong when saving the image in existing palette!", Toast.LENGTH_SHORT).show();
                else
                {
                    if(mChkBx_addAsCover.isChecked()) {
                        Toast.makeText(mContext, "Image saved succssfuly!", Toast.LENGTH_SHORT).show();
                        Long dbUpdateCover = myDbHelper.updateCoverInPalette(pltID_from_Spinner.toString(), "image", dbImgInsertID_exstPlt.toString());
                    }
                    return 1;
                }
            }
        }
        else
            Toast.makeText(mContext, "Please insert image name!", Toast.LENGTH_SHORT).show();

        return 0;
    }


    public String onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        switch(view.getId()) {
            case R.id.rdBtn_Existing:
                if (checked)
                    mEdtVwPltName_new.setVisibility(View.GONE);
                mEdtVwPltName_new.setText(null);
                mSpinnerPaletteName_exist.setVisibility(View.VISIBLE);
                break;
            case R.id.rdBtn_New:
                if (checked)
                    mEdtVwPltName_new.setVisibility(View.VISIBLE);
                mSpinnerPaletteName_exist.setVisibility(View.GONE);
                break;

        }return null;
    }


    private void setup_spinnerItems(Spinner mSpn)
    {
        ArrayList<BeanMain> listPaletteDB = myDbHelper.getPaletteList();
        // BeanMain _paletteObj = new BeanMain();


        adapter_Spinner = new MySpinAdapter_PaletteNames(mContext, android.R.layout.simple_spinner_item, listPaletteDB);
        mSpn.setAdapter(adapter_Spinner);
        mSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                BeanMain _paletteObj = (BeanMain) adapter_Spinner.getItem(position);

                pltName_from_Spinner = _paletteObj.getPaletteName().toString();
                pltID_from_Spinner = _paletteObj.getPaletteID();

                //Log.d("dbResult", mpalettetNameFromSpinner + mpalettetIDFromSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }

//endregion


//region *********************************** asyntask for saving in palette --- for OLD camera
    private void call_asyncForImageSave()
    {
        img_Time_Name = String.valueOf(System.currentTimeMillis());
        File outputImageFile = MyImageHelper.func_makeFolderForImage(mContext, "ColorappImgs", "colorappImg_", img_Time_Name, ".png");

        strImgPath = outputImageFile.getAbsolutePath();

        if (outputImageFile != null)
            new AsyncSaveImage().execute(outputImageFile);
        else
            Toast.makeText(CallingCameraActivity.this, "Unable to open file for saving image.", Toast.LENGTH_LONG).show();
    }

    private class AsyncSaveImage extends AsyncTask<File, Void, Boolean>
    {
        Boolean flag_success = false;
        String flag_ExistingOrNew = "";
       // Long flag_saveToDB = -1L;


        @Override
        protected Boolean doInBackground(File... paramsFile)
        {
            try {
                if (mCameraBitmap != null) {
                    FileOutputStream outStream = null;
                    try {
                        outStream = new FileOutputStream(paramsFile[0]);
                        if (!mCameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
                            Toast.makeText(mContext, "Unable to save image to file.", Toast.LENGTH_LONG).show();
                            flag_success = false;
                        } else {
 //region...create thumbs
                            InputStream thumbInputStream = MyImageHelper.func_createThumbs_ReturnInStream(mCameraBitmap);  /////////////////makes the smallest thumbnail
                            Bitmap thumbBitmap = BitmapFactory.decodeStream(thumbInputStream);
                            strThumbPath = MyImageHelper.func_giveThumbBitmap_aFileName(mContext, thumbBitmap, img_Time_Name);
//endregion
                            if(strThumbPath != null)
                                flag_success = true;
                            // Toast.makeText(CallingCameraActivity.this, "Saved image to: " + file.getPath(), Toast.LENGTH_LONG).show();
                        }
                        outStream.close();
                    } catch (Exception e) {
                        flag_success = false;
                        }
                }
            } catch (Exception e) {   e.printStackTrace();   }

            return flag_success;
        }


        @Override
        protected void onPostExecute(Boolean resultFlag) {
            // execution of result of Long time consuming operation
            Log.d("Log_Async", "resultFlag:: "+ resultFlag);

            if(resultFlag)
                addImageToPlt();

            pDialog.dismiss();
        }


        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            pDialog = new ProgressDialog(mContext);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(true);
          //  pDialog.setOnDismissListener(this);
            pDialog.show();
        }
    }//asyncTask End
//endregion


}
