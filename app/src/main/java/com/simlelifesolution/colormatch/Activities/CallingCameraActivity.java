package com.simlelifesolution.colormatch.Activities;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.Manifest;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.simlelifesolution.colormatch.Beans.BeanImage;
import com.simlelifesolution.colormatch.Beans.BeanMain;
import com.simlelifesolution.colormatch.Helpers.DatabaseHelper;
import com.simlelifesolution.colormatch.Helpers.MyImageHelper;
import com.simlelifesolution.colormatch.Helpers.MySpinAdapter_PaletteNames;
import com.simlelifesolution.colormatch.R;

public class CallingCameraActivity extends AppCompatActivity
{
    //region.....variables & ClickListeners
    Context mContext;
    String getIntent_flag_ImgOrClr, getIntent_pltID, getIntent_pltName, getIntent_imgPath_OR_clrCode;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 103;

    private static int btnPressType = 0 ;

    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

    private static final int TAKE_PICTURE_REQUEST_B = 100;

    private ImageView mCameraImageView;
    private Bitmap mCameraBitmap;
    private Button mSaveImageButton_new, mSaveImageButton_existing;

    static String img_Time_Name, strImgPath, strThumbPath;

    private DatabaseHelper myDbHelper;
    Long paletteID_pkDB = -1L;

    MySpinAdapter_PaletteNames adapter_Spinner;
    public String pltName_from_Spinner = "";
    public String pltID_from_Spinner = "";


    private OnClickListener mCaptureImageButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            startImageCapture();
        }
    };

    private OnClickListener mSaveImage_newPlt_ButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
           // File saveFile = openFileForImage();
            img_Time_Name = String.valueOf(System.currentTimeMillis());
            File outputImageFile = MyImageHelper.func_makeFolderForImage(CallingCameraActivity.this, "ColorappImgs", "colorappImg_", img_Time_Name, ".png");

            strImgPath = outputImageFile.getAbsolutePath();

            if (outputImageFile != null)
                { saveImageToFile(outputImageFile);
                  func_saveThumb();
                    addImageToNewPlt();
                }
            else
                Toast.makeText(CallingCameraActivity.this, "Unable to open file for saving image.", Toast.LENGTH_LONG).show();


        }
    };

    private OnClickListener mSaveImage_existPlt_ButtonClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // File saveFile = openFileForImage();
            img_Time_Name = String.valueOf(System.currentTimeMillis());
            File outputImageFile = MyImageHelper.func_makeFolderForImage(CallingCameraActivity.this, "ColorappImgs", "colorappImg_", img_Time_Name, ".png");

            strImgPath = outputImageFile.getAbsolutePath();

            if (outputImageFile != null)
            { saveImageToFile(outputImageFile);
                func_saveThumb();
                addImageToExistingPlt();}
            else
                Toast.makeText(CallingCameraActivity.this, "Unable to open file for saving image.", Toast.LENGTH_LONG).show();


        }
    };


    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling_camera);
        mContext = CallingCameraActivity.this;

        mCameraImageView = (ImageView) findViewById(R.id.camera_image_view);

        findViewById(R.id.capture_image_button).setOnClickListener(mCaptureImageButtonClickListener);

        mSaveImageButton_new = (Button) findViewById(R.id.save_image_new_button);
        mSaveImageButton_new.setOnClickListener(mSaveImage_newPlt_ButtonClickListener);
        mSaveImageButton_new.setEnabled(false);

        mSaveImageButton_existing = (Button) findViewById(R.id.save_image_existing_button);
        mSaveImageButton_existing.setOnClickListener(mSaveImage_existPlt_ButtonClickListener);
        mSaveImageButton_existing.setEnabled(false);


        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);

        // getIntent_flag_ImgOrClr, getIntent_pltID, getIntent_pltName, getIntent_imgPath_OR_clrCode;
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            getIntent_flag_ImgOrClr = extras.getString("xtra_flag_imgOrClr");

            getIntent_pltID = extras.getString("xtra_inside_plt_ID");
            getIntent_pltName = extras.getString("xtra_inside_plt_name");

            if(getIntent_flag_ImgOrClr.equals("image"))
                getIntent_imgPath_OR_clrCode =  extras.getString("xtra_img_path");
            else if(getIntent_flag_ImgOrClr.equals("color"))
                getIntent_imgPath_OR_clrCode =   extras.getString("xtra_colorCode");


        }

        myDbHelper = new DatabaseHelper(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_PICTURE_REQUEST_B) {
            if (resultCode == RESULT_OK) {
                // Recycle the previous bitmap.
                if (mCameraBitmap != null) {
                    mCameraBitmap.recycle();
                    mCameraBitmap = null;
                }
                Bundle extras = data.getExtras();
                //  mCameraBitmap = (Bitmap) extras.get("data");
                byte[] cameraData = extras.getByteArray(CustomCameraActivity.EXTRA_CAMERA_DATA);
                if (cameraData != null) {
                    mCameraBitmap = BitmapFactory.decodeByteArray(cameraData, 0, cameraData.length);
                    mCameraImageView.setImageBitmap(mCameraBitmap);
                    mSaveImageButton_new.setEnabled(true);
                    mSaveImageButton_existing.setEnabled(true);

                }
            } else {
                mCameraBitmap = null;
                mSaveImageButton_new.setEnabled(false);
                mSaveImageButton_existing.setEnabled(false);
            }
        }
    }

    private void startImageCapture() {
        //startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PICTURE_REQUEST_B);
        Intent custIntent = new Intent(CallingCameraActivity.this, CustomCameraActivity.class);
            custIntent.putExtra("xtra_inside_plt_ID", getIntent_pltID);
            custIntent.putExtra("xtra_inside_plt_name", getIntent_pltName);
            custIntent.putExtra("xtra_imgPth_or_colorCode", getIntent_imgPath_OR_clrCode);
            custIntent.putExtra("xtra_flag_imgOrClr", getIntent_flag_ImgOrClr);

        //startActivityForResult(new Intent(CallingCameraActivity.this, CustomCameraActivity.class), TAKE_PICTURE_REQUEST_B);
        startActivityForResult(custIntent, TAKE_PICTURE_REQUEST_B);
    }

    private void saveImageToFile(File file) {
        if (mCameraBitmap != null) {
            FileOutputStream outStream = null;
            try {
                outStream = new FileOutputStream(file);
                if (!mCameraBitmap.compress(Bitmap.CompressFormat.PNG, 100, outStream)) {
                    Toast.makeText(CallingCameraActivity.this, "Unable to save image to file.", Toast.LENGTH_LONG).show();
                } else {
                   // Toast.makeText(CallingCameraActivity.this, "Saved image to: " + file.getPath(), Toast.LENGTH_LONG).show();
                }
                outStream.close();
            } catch (Exception e) {
                Toast.makeText(CallingCameraActivity.this, "Unable to save image to file.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }


    //region********************************** save full image & thumb to  folder

    private void func_saveThumb()
    {
        if (mCameraBitmap != null) {
            try {
                InputStream thumbInputStream = MyImageHelper.func_createThumbs_ReturnInStream(mCameraBitmap);  /////////////////makes the smallest thumnail
                Bitmap thumbBitmap = BitmapFactory.decodeStream(thumbInputStream);
                // int size_afterDesample3 = BitmapCompat.getAllocationByteCount(mBtmp3);

                strThumbPath = MyImageHelper.func_giveBitmap_aFileName(CallingCameraActivity.this, thumbBitmap, img_Time_Name);

            } catch (Exception ex) {
                String errmsg = "inside func_saveThumb:: \t" + ex;
                Log.e("TAG_LOG_TAKE_PICTURE", errmsg);
                Toast.makeText(mContext, errmsg, Toast.LENGTH_LONG).show();
            }
        }
    }
    //endregion


    //region*****************************save images in DB


    private void addImageToNewPlt()
    {
        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this);

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dailog_new_palette, null);

        mAlertBuilder.setPositiveButton("ok", null);
        mAlertBuilder.setNegativeButton("cancel", null);
        mAlertBuilder.setView(promptsView);

        final EditText mEdtVwPltName = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        final EditText mEdtVwImageName = (EditText) promptsView.findViewById(R.id.etDialogImgName);
        final CheckBox mChkBx = (CheckBox) promptsView.findViewById(R.id.chkBoxCover);

        final AlertDialog mAlertDialog = mAlertBuilder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if ((mEdtVwPltName.getText().toString().trim().length() > 0) && (mEdtVwImageName.getText().toString().trim().length() > 0))
                        {
                            String _pltName = mEdtVwPltName.getText().toString();
                            String _imgName = mEdtVwImageName.getText().toString();

                            if(myDbHelper.checkDuplicatePltName(_pltName))
                            {
                                Toast.makeText(mContext, "Palette Name already exists! Please try another name.", Toast.LENGTH_SHORT).show();
                            }
                            else {
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
                                            if(mChkBx.isChecked()) {
                                                Toast.makeText(mContext, "Image saved succssfuly!", Toast.LENGTH_SHORT).show();
                                                Long dbUpdateCover = myDbHelper.updateCoverInPalette(paletteID_pkDB.toString(), "image", dbImgInsertID.toString());
                                            }

                                        }

                                        mAlertDialog.dismiss();
                                    } else
                                        Toast.makeText(mContext, "Something went wrong when creating a new palette!", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }
                        else    // either PaletteName or ImageName was not given
                            Toast.makeText(mContext, "Please check the Palette & Image Name!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    private void addImageToExistingPlt()
    {
        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this);

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_existingpalette, null);

        mAlertBuilder.setPositiveButton("ok", null);
        mAlertBuilder.setNegativeButton("cancel", null);
        mAlertBuilder.setView(promptsView);

        final Spinner mSpinnerPaletteName = (Spinner) promptsView.findViewById(R.id.spinner_existingPalette);
        final EditText mEdtVwImageName = (EditText) promptsView.findViewById(R.id.etDialogImgName);
        final CheckBox mChkBx_existing = (CheckBox) promptsView.findViewById(R.id.chkBoxCover_existing);

        setup_spinnerItems(mSpinnerPaletteName); //------------------ setUp the spinner for existing paletteList


        final AlertDialog mAlertDialog = mAlertBuilder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
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
                                    if(mChkBx_existing.isChecked()) {
                                        Toast.makeText(mContext, "Image saved succssfuly!", Toast.LENGTH_SHORT).show();
                                        Long dbUpdateCover = myDbHelper.updateCoverInPalette(pltID_from_Spinner.toString(), "image", dbImgInsertID_exstPlt.toString());
                                    }
                                }

                                mAlertDialog.dismiss();
                            }

                        }
                        else
                            Toast.makeText(mContext, "Please insert image name!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mAlertDialog.show();
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
}
