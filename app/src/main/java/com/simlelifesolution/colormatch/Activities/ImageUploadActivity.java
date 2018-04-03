package com.simlelifesolution.colormatch.Activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.simlelifesolution.colormatch.Beans.BeanImage;
import com.simlelifesolution.colormatch.Beans.BeanMain;
import com.simlelifesolution.colormatch.Helpers.DatabaseHelper;
import com.simlelifesolution.colormatch.Helpers.MySpinAdapter_PaletteNames;
import com.simlelifesolution.colormatch.R;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;

public class ImageUploadActivity extends AppCompatActivity {

    //region...... variables Initialization
    ImageView imgVw_upload;
    Button uploadButton, corpButton, addToNewPalleteButton;

    private final int TAG_CAMERA = 1;
    private final int TAG_GALLERY = 2;

    String strImgPath = null;
    Cursor mCursor;

    private ProgressDialog pDialog;
    HttpURLConnection conn = null;
    BufferedInputStream responseStream;
    int serverResponseCode = 0;
    String serverResponseMessage;

    private DatabaseHelper myDbHelper = new DatabaseHelper(this);
    Long paletteID_pkDB = -1L;

    MySpinAdapter_PaletteNames adapter;
    public String pltName = "";
    public String pltID = "";

    //endregion


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        initialize();
        getImage();
    }

    private void initialize() {
        imgVw_upload = (ImageView) findViewById(R.id.imgVw);
        uploadButton = (Button) findViewById(R.id.btnAddToNewPallete);
        corpButton = (Button) findViewById(R.id.btnCrop);
        addToNewPalleteButton = (Button) findViewById(R.id.btnAddToExistingPalette);

    }

    private void getImage() {
        int extra_btnPressed = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            extra_btnPressed = extras.getInt("btnPressed");
            Log.d("uploadFile_3", String.valueOf(extra_btnPressed));
        }

        if (extra_btnPressed == 1)
            startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAG_CAMERA);
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
                onCaptureImageResult(data);
        } else
            this.finish();
    }

    private void onCaptureImageResult(Intent data) {
        Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        try {
            imgVw_upload.setImageBitmap(thumbnail);

            Uri tempUri = getCameraImageUri(getApplicationContext(), thumbnail);
            strImgPath = new File(getRealPathFromURI(tempUri)).toString(); //here new file creates & gets saved
            Log.v("resultCode", "=" + strImgPath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void onSelectFromGalleryResult(Intent data) {
        Bitmap thumbnail = null;


        if (data != null) {
            try {
                thumbnail = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), data.getData());

                imgVw_upload.setImageBitmap(thumbnail);

                Uri selectedImageUri = data.getData();
                strImgPath = getRealPathFromURI(selectedImageUri).toString();
                Log.v("resultCode", "=" + strImgPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public Uri getCameraImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public String getRealPathFromURI(Uri uri) {
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();
        int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        return cursor.getString(idx);
    }


    public void buttonOnClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddToNewPallete:
                if (strImgPath == null)
                    Toast.makeText(this, "Please select an image first!", Toast.LENGTH_SHORT).show();
                else {
                    //createNewPaletteInDB();
                    addImageToNewPlt();
                }
                break;

            case R.id.btnCrop:
                Toast.makeText(this, "Button corp Pressed!", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btnAddToExistingPalette:
                //Toast.makeText(this, "Button AddToNEwpellete pressed!", Toast.LENGTH_SHORT).show();
                addImageToExistingPlt();
                break;

            default:
                break;
        }

    }


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
                                Toast.makeText(ImageUploadActivity.this, "Palette Name already exists! Please try another name.", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                if (myDbHelper.checkDuplicateImgName(_imgName)) {
                                    Toast.makeText(ImageUploadActivity.this, "Image Name already exists! Please try another name.", Toast.LENGTH_SHORT).show();
                                } else {
                                    BeanMain _PaletteObj = new BeanMain("NULL", _pltName, "image", "0", "");
                                    paletteID_pkDB = myDbHelper.createNewPalette(_PaletteObj);

                                    if (paletteID_pkDB != -1)   //new palette created successfully
                                    {
                                        Toast.makeText(ImageUploadActivity.this, "New Palette created succssfuly!\n Please wait for storing the image. ", Toast.LENGTH_SHORT).show();

                                        pltID = String.valueOf(paletteID_pkDB);

                                        BeanImage _imgObj = new BeanImage("", pltID, strImgPath, _imgName, "");
                                        Long dbImgInsert = myDbHelper.insert_newImage(_imgObj);

                                        Log.d("dbResult_explt", "Image Created DBresult:::" + dbImgInsert.toString() + " pltID: " + _imgObj.getPaletteID().toString());

                                        if (dbImgInsert == -1)
                                            Toast.makeText(ImageUploadActivity.this, "Something went wrong when saving the image in existing palette!", Toast.LENGTH_SHORT).show();
                                        else
                                            Toast.makeText(ImageUploadActivity.this, "Image saved succssfuly!", Toast.LENGTH_SHORT).show();

                                        mAlertDialog.dismiss();
                                    } else
                                        Toast.makeText(ImageUploadActivity.this, "Something went wrong when creating a new palette!", Toast.LENGTH_SHORT).show();

                                }
                            }
                        }
                        else    // either PaletteName or ImageName was not given
                            Toast.makeText(ImageUploadActivity.this, "Please check the Palette & Image Name!", Toast.LENGTH_SHORT).show();
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

        setup_spinnerItems(mSpinnerPaletteName); //------------------ setUp the spinner for existing paletteList


        final AlertDialog mAlertDialog = mAlertBuilder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if ((mEdtVwImageName.getText().toString().trim().length() > 0)  && !(pltName.equals("")))
                        {
                           String _imgName = mEdtVwImageName.getText().toString();

                            if (myDbHelper.checkDuplicateImgName(_imgName)) {
                                Toast.makeText(ImageUploadActivity.this, "Image Name already exists! Please try another name.", Toast.LENGTH_SHORT).show();
                            } else {
                                BeanImage _imgObj = new BeanImage("", pltID, strImgPath, _imgName, "");
                                Long dbImgInsert = myDbHelper.insert_newImage(_imgObj);

                                Log.d("dbResult_explt", "Image Created DBresult:::" + dbImgInsert.toString() + " pltID: " + _imgObj.getPaletteID().toString());

                                if (dbImgInsert == -1)
                                    Toast.makeText(ImageUploadActivity.this, "Something went wrong when saving the image in existing palette!", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(ImageUploadActivity.this, "Image saved succssfuly!", Toast.LENGTH_SHORT).show();

                                mAlertDialog.dismiss();
                            }

                        }
                        else
                            Toast.makeText(ImageUploadActivity.this, "Please insert image name!", Toast.LENGTH_SHORT).show();
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


        adapter = new MySpinAdapter_PaletteNames(ImageUploadActivity.this, android.R.layout.simple_spinner_item, listPaletteDB);
        mSpn.setAdapter(adapter);
        mSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                BeanMain _paletteObj = (BeanMain) adapter.getItem(position);

                pltName = _paletteObj.getPaletteName().toString();
                pltID = _paletteObj.getPaletteID();

                //Log.d("dbResult", mpalettetNameFromSpinner + mpalettetIDFromSpinner);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
    }


}
