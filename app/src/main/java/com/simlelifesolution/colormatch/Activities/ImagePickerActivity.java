package com.simlelifesolution.colormatch.Activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import com.simlelifesolution.colormatch.Beans.BeanImage;
import com.simlelifesolution.colormatch.Beans.BeanMain;
import com.simlelifesolution.colormatch.Helpers.DatabaseHelper;
import com.simlelifesolution.colormatch.Helpers.MyImageHelper;
import com.simlelifesolution.colormatch.Helpers.MySpinAdapter_PaletteNames;
import com.simlelifesolution.colormatch.R;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;

import static android.media.ThumbnailUtils.extractThumbnail;

public class ImagePickerActivity extends AppCompatActivity {

//region...... variables declaration
    Context mContext = ImagePickerActivity.this;
    ImageView imgVw_upload;
    Button uploadButton, corpButton, addToNewPalleteButton;

    private final int TAG_CAMERA = 1;
    private final int TAG_CAMERA_NEW = 12;
    private final int TAG_GALLERY = 2;

    String strImgPath = null;
    String strThumbPath = null;
    Cursor mCursor;

    private DatabaseHelper myDbHelper;
    Long paletteID_pkDB = -1L;

    MySpinAdapter_PaletteNames adapter_Spinner;
    public String pltName_from_Spinner = "";
    public String pltID_from_Spinner = "";

    private Uri outputImgUri;
    static String img_Time_Name = "" ;

    // for dialog view
     EditText mEdtVwPltName_new;
     Spinner mSpinnerPaletteName_exist;
    //endregion

 //------------- for new camera2
    private Bitmap mCameraBitmap;
 //--------


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_picker);

        myDbHelper = new DatabaseHelper(this);

        initialize();
        getImage();

    }

    private void initialize() {
        imgVw_upload = (ImageView) findViewById(R.id.imgVw);
        uploadButton = (Button) findViewById(R.id.btnAddToNewPallete);
        //corpButton = (Button) findViewById(R.id.btnCrop);
        //addToNewPalleteButton = (Button) findViewById(R.id.btnAddToExistingPalette);

    }

    private void getImage() {
        int extra_btnPressed = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            extra_btnPressed = extras.getInt("btnPressed");
            Log.d("uploadFile_3", String.valueOf(extra_btnPressed));
        }

        if (extra_btnPressed == 1) {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP)
                func_myCamera();
            else {
                    Intent cam2Intent = new Intent();
                   // cam2Intent.setClass(mContext, Camera2Activity.class);
                    cam2Intent.setClass(mContext, Camera2Activity.class);
                    startActivityForResult(cam2Intent, TAG_CAMERA_NEW);
            }
        }
        else if (extra_btnPressed == 2)
                   startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), TAG_GALLERY);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.v("resultCode", "=" + resultCode);

       if (resultCode == Activity.RESULT_OK) {
            mCursor = null;
            if (requestCode == TAG_GALLERY)
                onSelectFromGalleryResult(data);
            else if (requestCode == TAG_CAMERA)
                func_onCaptureImageResult();
            else if (requestCode == TAG_CAMERA_NEW) {
                //func_onCaptureImageResult_new(data);
                func_onCaptureImageResult_new(data);
            }

        } else if(resultCode == Activity.RESULT_CANCELED){
           if (requestCode == TAG_CAMERA){

           func_delFile(outputImgUri);
            }
           this.finish();
        }
    }


    private void onSelectFromGalleryResult(Intent data) {
        Bitmap bitmap_gallery = null;


        if (data != null) {
            try {
                bitmap_gallery = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());

                imgVw_upload.setImageBitmap(bitmap_gallery);

                Uri selectedImageUri = data.getData();

               String galleryImgPth = MyImageHelper.getRealPathFromURI(mContext, selectedImageUri).toString();
                Log.d("Log_ColorMatch", "Selected imgpath from gallery= " + galleryImgPth); //

 //--------------------------copy the image from galery to app's image folder and create thumbnail
    //----- copy-----------------------------
                File source_file = new File(galleryImgPth) ; //
                img_Time_Name = String.valueOf(System.currentTimeMillis());
                File dest_file = MyImageHelper.func_makeFolderForImage(mContext, "ColorappImgs", "colorappImg_", img_Time_Name, ".png");

                strImgPath = dest_file.getAbsolutePath();

                MyImageHelper.copyFile(source_file, dest_file);

    //-----thumbnail create-------------------
                InputStream thumbInputStream_gallery = MyImageHelper.func_createThumbs_ReturnInStream(bitmap_gallery);  /////////////////makes the smallest thumnail
                Bitmap thumbBitmap_galry = BitmapFactory.decodeStream(thumbInputStream_gallery);
                // int size_afterDesample3 = BitmapCompat.getAllocationByteCount(mBtmp3);

                strThumbPath = MyImageHelper.func_giveBitmap_aFileName(mContext, thumbBitmap_galry, img_Time_Name);

            } catch(Exception ex){Log.d("Log_ColorMatch", "Exception for copying file: " + ex );        }



        }

    }

    public void buttonOnClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddToNewPallete:
                if ((strImgPath == null) || (strThumbPath == null))
                    Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show();
                else {
                    addImageToPlt();
                }
                break;

            default:
                break;
        }

    }

    private void addImageToPlt()
    {
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

                               Intent intent_DetailsAct = new Intent(mContext, PaletteDetailsActivity.class);
                               intent_DetailsAct.putExtra("xtra_pltID_fromListClk", pltID_from_Spinner);
                               intent_DetailsAct.putExtra("xtra_pltName_fromListClk", pltName_from_Spinner);

                               intent_DetailsAct.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                               startActivity(intent_DetailsAct);

                              finish();}
                    }
                });
            }
        });
        mAlertDialog.show();
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

                        BeanMain _mainObj = myDbHelper.getPaletteObjFromID(paletteID_pkDB.toString());
                        pltName_from_Spinner = _mainObj.getPaletteName();



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


  //region************************************** Code for camera

    private void func_myCamera(){
        try {
            // File outputImageFile = func_makeFolderForImage();

            img_Time_Name = String.valueOf(System.currentTimeMillis());
            File outputImageFile = MyImageHelper.func_makeFolderForImage(mContext, "ColorappImgs", "colorappImg_", img_Time_Name, ".png");

            strImgPath = outputImageFile.getAbsolutePath();

            if(outputImageFile!=null) {

                outputImgUri = MyImageHelper.getImageFileUriByOsVersion(mContext, outputImageFile);
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                cameraIntent.putExtra("android.intent.extra.quickCapture",true);

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputImgUri);
                startActivityForResult(cameraIntent, TAG_CAMERA);
            }
            else{
                Toast.makeText(mContext, "Problem creating file",Toast.LENGTH_LONG).show();}

        }catch(Exception ex)
        {
            String errmsg = "Insideoncreate()BtnClk:: \t"+ex;
            Log.e("TAG_LOG_TAKE_PICTURE", errmsg);
            Toast.makeText(mContext, errmsg, Toast.LENGTH_LONG).show();
        }


    }




    private void func_delFile(Uri uri)
    {
        File fdelete = new File(uri.getPath());
        if (fdelete.exists())
        {if(fdelete.length()==0){
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + uri.getPath());
            } else {
                System.out.println("file not Deleted :" + uri.getPath());
            }
        }}
    }

    private void func_onCaptureImageResult()
    {
        try {
            ContentResolver contentResolver = getContentResolver();

            Bitmap cameraBitmap = MyImageHelper.rotateImageFromURI(this, outputImgUri);
            imgVw_upload.setImageBitmap(cameraBitmap);

 /*            InputStream inputStream = contentResolver.openInputStream(outputImgUri);
            Bitmap cameraBitmap = BitmapFactory.decodeStream(inputStream);
            imgVw_upload.setImageBitmap(cameraBitmap);*/

    //--------------------------create thumbnail

            InputStream thumbInputStream = MyImageHelper.func_createThumbs_ReturnInStream(cameraBitmap);  /////////////////makes the smallest thumnail
            Bitmap thumbBitmap = BitmapFactory.decodeStream(thumbInputStream);
           // int size_afterDesample3 = BitmapCompat.getAllocationByteCount(mBtmp3);

            strThumbPath = MyImageHelper.func_giveBitmap_aFileName(mContext, thumbBitmap, img_Time_Name);

          }catch(Exception ex){
            String errmsg = "inside onActivityResult:: \t"+ex;
            Log.e("TAG_LOG_TAKE_PICTURE", errmsg);
            Toast.makeText(mContext, errmsg, Toast.LENGTH_LONG).show();
        }


        try{
            ExifInterface ei = new ExifInterface(strImgPath);

            String orientString = ei.getAttribute(ExifInterface.TAG_ORIENTATION);
            Log.d("TEST", "After orientString: " + orientString);

            int orientation = orientString != null ? Integer.parseInt(orientString) : ExifInterface.ORIENTATION_NORMAL;
            Log.d("TEST", "After orientation: " + orientation);

            int rotationAngle = 0;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) rotationAngle = 90;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_180) rotationAngle = 180;
            if (orientation == ExifInterface.ORIENTATION_ROTATE_270) rotationAngle = 270;
            Log.d("TEST", "After rotationAngle: " + rotationAngle);
        }
        catch(Exception ex){}
    }



    private void func_onCaptureImageResult_new(Intent intntData)
    {
        try {
            strImgPath = intntData.getStringExtra("strImgpathFromCam2");
            img_Time_Name = intntData.getStringExtra("strImgTimeNameCam2");
            outputImgUri =  Uri.parse(intntData.getStringExtra("strImgUriCam2"));

            Log.d("log_came2", "from intent:  "+strImgPath + "\t" + img_Time_Name);

            mCameraBitmap = MyImageHelper.rotateImageFromURI(this, outputImgUri);

          //  mCameraBitmap = BitmapFactory.decodeFile(strImgPath);

            imgVw_upload.setImageBitmap(mCameraBitmap);



            InputStream thumbInputStream = MyImageHelper.func_createThumbs_ReturnInStream(mCameraBitmap);  /////////////////makes the smallest thumnail
            Bitmap thumbBitmap = BitmapFactory.decodeStream(thumbInputStream);

            strThumbPath = MyImageHelper.func_giveBitmap_aFileName(mContext, thumbBitmap, img_Time_Name);

        }catch(Exception ex){
            String errmsg = "inside onActivityResult:: \t"+ex;
            Log.e("TAG_LOG_TAKE_PICTURE", errmsg);
            Toast.makeText(mContext, errmsg, Toast.LENGTH_LONG).show();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();

        if (mCameraBitmap != null && !mCameraBitmap.isRecycled()) {
            imgVw_upload.setImageBitmap(null);
            mCameraBitmap.recycle();
            mCameraBitmap = null;
        }
    }

}
