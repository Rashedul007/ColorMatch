package com.simlelifesolution.colormatch.Activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.simlelifesolution.colormatch.Beans.BeanColor;
import com.simlelifesolution.colormatch.Beans.BeanImage;
import com.simlelifesolution.colormatch.Beans.BeanMain;
import com.simlelifesolution.colormatch.Helpers.DatabaseHelper;
import com.simlelifesolution.colormatch.Helpers.MyImageHelper;
import com.simlelifesolution.colormatch.Helpers.MyRecycleAdapter_ColorListFromImage;
import com.simlelifesolution.colormatch.Helpers.MySpinAdapter_PaletteNames;
import com.simlelifesolution.colormatch.R;

import java.io.File;
import java.util.ArrayList;

public class ColorListFromImageActivity extends AppCompatActivity
{
//region...... variables declaration
    private Context mContext = ColorListFromImageActivity.this;
    private Toolbar mToolbar;
    private ImageView mImageView;
    private RecyclerView mRecyclerView;
    private MyRecycleAdapter_ColorListFromImage mAdapter;
    private static final int SPAN_COUNT = 4;

//--- for intent extra data fetch
    ArrayList<String> arr_ColorsFromIntent;
    String inside_which_plt_ID, inside_which_plt_Name, xtra_image_path, xtra_image_name;

    String mColor_HexConverted;
    int mColor = 0;
    private DatabaseHelper myDbHelper;
    MySpinAdapter_PaletteNames mSpinnerAdapter;
    private Spinner mSpinner;
    ArrayList<BeanMain> listPaletteDB ;
    public String mpalettetNameFromSpinner ="";
    public String mpalettetIDFromSpinner ="";

    Boolean flag_isCoverImgPopUpOn = false;

//-----for dialog view
    EditText mEdtVwPltName_new;
    Spinner mSpinnerPaletteName_exist;
    MySpinAdapter_PaletteNames adapter_Spinner;

    Long paletteID_pkDB = -1L;

//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colorlist_fromimage);



        mImageView = (ImageView)findViewById(R.id.imgVw_colorLstFrmImg);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);


        myDbHelper = new DatabaseHelper(this);
        mRecyclerView= (RecyclerView)findViewById(R.id.rcView_colorListFromImg);
        listPaletteDB = new ArrayList<BeanMain>();

        arr_ColorsFromIntent = new ArrayList<String>();

        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            arr_ColorsFromIntent = extras.getStringArrayList("xtra_colorList");
            inside_which_plt_ID = extras.getString("xtra_inside_plt_ID");
            inside_which_plt_Name = extras.getString("xtra_inside_plt_name");
            xtra_image_path = extras.getString("xtra_img_path"); //xtra_img_name
            xtra_image_name = extras.getString("xtra_img_name");

            actionBar.setTitle(inside_which_plt_Name);
            mImageView.setImageBitmap(MyImageHelper.getBitmapFromPath(xtra_image_path));

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        func_getColorListFromIntent();
    }

    public void func_getColorListFromIntent()
    {
        mAdapter = new MyRecycleAdapter_ColorListFromImage(this, arr_ColorsFromIntent);
        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getApplicationContext(),SPAN_COUNT);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.notifyDataSetChanged();


        mAdapter.setOnItemClickListener(new MyRecycleAdapter_ColorListFromImage.onRecyclerViewItemClickListener()
        {
            @Override
            public void onItemClickListener(View mView, String clrCode) {
                 //mColor_HexConverted = "#"+clrCode ;
                mColor_HexConverted = clrCode ;
                if (mView instanceof Button) {
                    mColor = Color.parseColor(mColor_HexConverted);
                addColorToPlt(mColor);
                } // make dialog and insertcolor to palette
                else if (mView instanceof ImageView) {
                    Intent intnt = new Intent(mContext, ColorMatchingActivity.class);
                    intnt.putExtra("intnt_colorCode", mColor_HexConverted);
                    startActivity(intnt);
                }
        }
        });
    }




//region ... for add button inside dialog
   /* public void onClickNewPlt(View v)
    {
        final AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this);

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_add_color_to_new_plt, null);

        mAlertBuilder.setPositiveButton("ok", null);
        mAlertBuilder.setNegativeButton("cancel", null);
        mAlertBuilder.setView(promptsView);

        final EditText mEdtVwNewPaletteName = (EditText) promptsView.findViewById(R.id.edtTxtVwNewPltName);

        final AlertDialog mAlertDialog = mAlertBuilder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if (mEdtVwNewPaletteName.getText().toString().trim().length() > 0)
                        {
                            String _pltName = (mEdtVwNewPaletteName.getText().toString());

                            BeanMain _PaletteObj =  new BeanMain("NULL", _pltName, "color", "0", "");
                            long  paletteID_pkDB = myDbHelper.createNewPalette(_PaletteObj);

                            if(paletteID_pkDB != -1)
                            {
                                setupSpinner_paletteList();
                                mAlertDialog.dismiss();
                            }
                            else{
                                Toast.makeText(mContext, "Something went wrong when creating a new palette!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                            Toast.makeText(mContext, "Please insert palette name.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mAlertDialog.show();
    }*/
//endregion



    public void clkCoverImage_details(View v) {
        // Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
        if (flag_isCoverImgPopUpOn==false )
        { flag_isCoverImgPopUpOn = true;

            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = layoutInflater.inflate(R.layout.popup_image, null);
            ImageView mImg = (ImageView) popupView.findViewById(R.id.popupimgvw);
            TextView mTxt = (TextView) popupView.findViewById(R.id.popuptxtVw);

            Uri outputImgUri = Uri.fromFile(new File(xtra_image_path));
            try {
                Bitmap cameraBitmap = MyImageHelper.rotateImageFromURI(this, outputImgUri);
                mImg.setImageBitmap(cameraBitmap);
            }catch(Exception ex){}
              //  mImg.setImageBitmap(MyImageHelper.getBitmapFromPath(c));
                mTxt.setText(xtra_image_name.toString());

            final PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

            Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);

            btnDismiss.setOnClickListener(new Button.OnClickListener(){
                @Override
                public void onClick(View v) {  popupWindow.dismiss();  flag_isCoverImgPopUpOn = false;    }});

            popupWindow.showAsDropDown(mImageView, 200, 0);
        }
    }


    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }


    private void addColorToPlt(int clr)
    {
        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this);

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dialog_savecolor_topalette, null);

        mAlertBuilder.setPositiveButton("ok", null);
        mAlertBuilder.setNegativeButton("cancel", null);
        mAlertBuilder.setView(promptsView);

        mEdtVwPltName_new = (EditText) promptsView.findViewById(R.id.editTextDialogUserInput);
        final View mVwColorBg = (View) promptsView.findViewById(R.id.vwColorBackGround);
        final TextView mTxtVwColorCode = (TextView) promptsView.findViewById(R.id.txtVwColorCode);
        final EditText mEdtVwColorName = (EditText) promptsView.findViewById(R.id.etDialogImgName);
        final CheckBox mChkBx = (CheckBox) promptsView.findViewById(R.id.chkBoxCover);
        final RadioButton mRadioExist = (RadioButton) promptsView.findViewById(R.id.rdBtn_Existing);
        final RadioButton mRadioNew = (RadioButton) promptsView.findViewById(R.id.rdBtn_New);

        mSpinnerPaletteName_exist = (Spinner) promptsView.findViewById(R.id.spinner_existingPalette);

        setup_spinnerItems(mSpinnerPaletteName_exist); //------------------ setUp the spinner for existing paletteList

        mVwColorBg.setBackgroundColor(clr);
        mTxtVwColorCode.setText(mColor_HexConverted);

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
                            returnResult = func_addImageToExistingPlt(mEdtVwColorName,  mChkBx );

                        else if (mRadioNew.isChecked())
                            returnResult = func_addImageToNewPlt(mEdtVwColorName,  mChkBx );

                        if(returnResult == 1)
                        {  mAlertDialog.dismiss();

                            Intent intent_DetailsAct = new Intent(mContext, PaletteDetailsActivity.class);
                                intent_DetailsAct.putExtra("xtra_pltID_fromListClk", mpalettetIDFromSpinner);
                                intent_DetailsAct.putExtra("xtra_pltName_fromListClk", mpalettetNameFromSpinner);
                            intent_DetailsAct.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent_DetailsAct);

                            finish();}
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    private int func_addImageToNewPlt(EditText mEdtVwColorName,  CheckBox mChkBx )
    {
        if ((mEdtVwPltName_new.getText().toString().trim().length() > 0) && (mEdtVwColorName.getText().toString().trim().length() > 0)) {
            String _pltName = mEdtVwPltName_new.getText().toString();
            String _clrName = mEdtVwColorName.getText().toString();

            if (myDbHelper.checkDuplicatePltName(_pltName)) {
                Toast.makeText(mContext, "Palette Name already exists! Please try another name.", Toast.LENGTH_SHORT).show();
            }
            else {
                     BeanMain _PaletteObj = new BeanMain("NULL", _pltName, "image", "0", "");
                    paletteID_pkDB = myDbHelper.createNewPalette(_PaletteObj);


                    if (paletteID_pkDB != -1)   //new palette created successfully
                    {
                       // Toast.makeText(mContext, "New Palette created succssfuly!\n Please wait for storing the image. ", Toast.LENGTH_SHORT).show();

                        mpalettetIDFromSpinner = String.valueOf(paletteID_pkDB);

                        BeanMain _mainObj = myDbHelper.getPaletteObjFromID(paletteID_pkDB.toString());
                        mpalettetNameFromSpinner = _mainObj.getPaletteName();


                        BeanColor _ColorObj = new BeanColor("NULL", mpalettetIDFromSpinner, mColor_HexConverted, _clrName, "");
                        Long dbColorInsert = myDbHelper.insert_newColor(_ColorObj);


                        Log.d("dbResult_explt", "Color Created DBresult:::" + dbColorInsert.toString() + " pltID_from_Spinner: " + _ColorObj.getPaletteID().toString());

                        if (dbColorInsert == -1)
                            Toast.makeText(mContext, "Something went wrong when saving the color in the palette!", Toast.LENGTH_SHORT).show();
                        else {
                            if (mChkBx.isChecked()) {
                                Toast.makeText(mContext, "Image saved succssfuly!", Toast.LENGTH_SHORT).show();
                                Long dbUpdateCover = myDbHelper.updateCoverInPalette(paletteID_pkDB.toString(), "color", dbColorInsert.toString());
                            }
                            return 1;
                        }
                    } else
                        Toast.makeText(mContext, "Something went wrong when creating a new palette!", Toast.LENGTH_SHORT).show();
            }
        } else    // either PaletteName or ImageName was not given
            Toast.makeText(mContext, "Please check the Palette & Color Name!", Toast.LENGTH_SHORT).show();

        return 0;
    }


    private int func_addImageToExistingPlt(EditText mEdtVwColorName,  CheckBox mChkBx_addAsCover )
    {
        if ((mEdtVwColorName.getText().toString().trim().length() > 0)  && !(mpalettetNameFromSpinner.equals("")))
        {
            String _clrName = mEdtVwColorName.getText().toString();

            BeanColor _ColorObj = new BeanColor("NULL", mpalettetIDFromSpinner, mColor_HexConverted, _clrName, "");
            Long dbColorInsert = myDbHelper.insert_newColor(_ColorObj);

            Log.d("dbResult_explt", "Image Created DBresult:::" + dbColorInsert.toString() + " pltID_from_Spinner: " + _ColorObj.getPaletteID().toString());

            if (dbColorInsert == -1)
                Toast.makeText(mContext, "Something went wrong when saving the color in existing palette!", Toast.LENGTH_SHORT).show();
            else
            {
                if(mChkBx_addAsCover.isChecked()) {
                    Toast.makeText(mContext, "Color saved succssfuly!", Toast.LENGTH_SHORT).show();
                    Long dbUpdateCover = myDbHelper.updateCoverInPalette(mpalettetIDFromSpinner.toString(), "color", dbColorInsert.toString());
                }
                return 1;
            }

        }
        else
            Toast.makeText(mContext, "Please insert color name!", Toast.LENGTH_SHORT).show();

        return 0;
    }


    private void setup_spinnerItems(Spinner mSpn)
    {
        ArrayList<BeanMain> listPaletteDB = myDbHelper.getPaletteList();

        adapter_Spinner = new MySpinAdapter_PaletteNames(mContext, android.R.layout.simple_spinner_item, listPaletteDB);
        mSpn.setAdapter(adapter_Spinner);
        mSpn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                BeanMain _paletteObj = (BeanMain) adapter_Spinner.getItem(position);

                mpalettetNameFromSpinner = _paletteObj.getPaletteName().toString();
                mpalettetIDFromSpinner = _paletteObj.getPaletteID();

               // Toast.makeText(mContext, ""+ mpalettetIDFromSpinner + "\t" + mpalettetNameFromSpinner, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapter) {
            }
        });
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


}
