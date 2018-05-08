package com.simlelifesolution.colormatch.Activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
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

import java.util.ArrayList;

public class ColorListFromImageActivity extends AppCompatActivity
{
//region...... variables declaration
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

                    btnClkPickerExisting();
                } // make dialog and insertcolor to palette
                else if (mView instanceof ImageView) {
                    Intent intnt = new Intent(ColorListFromImageActivity.this, ColorMatchingActivity.class);
                    intnt.putExtra("intnt_colorCode", mColor_HexConverted);
                    startActivity(intnt);
                }
        }
        });
    }


    public void btnClkPickerExisting()
    {
        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(ColorListFromImageActivity.this);

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dailog_add_color_to_existing_plt, null);

        mAlertBuilder.setPositiveButton("ok", null);
        mAlertBuilder.setNegativeButton("cancel", null);
        mAlertBuilder.setView(promptsView);

        final Spinner mSpinnerPaletteName = (Spinner) promptsView.findViewById(R.id.spinner_existingPalette);
        mSpinner = mSpinnerPaletteName;
        final EditText mEdtVwColorName = (EditText) promptsView.findViewById(R.id.edTxtVwNewColorName);
        final View mVwColorBack = (View)promptsView.findViewById(R.id.vwColorBackGround);
        final TextView mTxtVwColorCode = (TextView) promptsView.findViewById(R.id.txtVwColorCode);

        mVwColorBack.setBackgroundColor(mColor);
        mTxtVwColorCode.setText(mColor_HexConverted);

        setupSpinner_paletteList(); //------------------ setUp the spinner for existing paletteList

        final AlertDialog mAlertDialog = mAlertBuilder.create();
        mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

            @Override
            public void onShow(DialogInterface dialog) {

                Button b = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                b.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        if ((mEdtVwColorName.getText().toString().trim().length() > 0) && !(mpalettetNameFromSpinner.equals("")))
                        {
                            BeanColor _PaletteObj = new BeanColor("NULL", mpalettetIDFromSpinner, mColor_HexConverted, mEdtVwColorName.getText().toString(), "");
                            Long dbColorInsert = myDbHelper.insert_newColor(_PaletteObj);
                            Toast.makeText(ColorListFromImageActivity.this, "Color inserted successfully with row no# : " + dbColorInsert, Toast.LENGTH_SHORT).show();

                            mAlertDialog.dismiss();
                        }
                        else
                            Toast.makeText(ColorListFromImageActivity.this, "Please insert color name.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mAlertDialog.show();
    }


    private void setupSpinner_paletteList()
    {
        if(listPaletteDB.size() >0)
            listPaletteDB.clear();

        listPaletteDB = myDbHelper.getPaletteList();
        mSpinnerAdapter = new MySpinAdapter_PaletteNames(ColorListFromImageActivity.this, android.R.layout.simple_spinner_item, listPaletteDB);
        mSpinner.setAdapter(mSpinnerAdapter);
        mSpinner.setSelection(listPaletteDB.size()-1);      // this is so that it auto selects the palette if any new palette created
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
                BeanMain _paletteObj = (BeanMain) mSpinnerAdapter.getItem(position);

                mpalettetNameFromSpinner = _paletteObj.getPaletteName().toString();
                mpalettetIDFromSpinner = _paletteObj.getPaletteID();
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapter) {  }
        });
    }

    public void onClickNewPlt(View v)
    {
        final AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(this);

        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.dailog_add_color_to_new_plt, null);

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
                                Toast.makeText(ColorListFromImageActivity.this, "Something went wrong when creating a new palette!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                            Toast.makeText(ColorListFromImageActivity.this, "Please insert palette name.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mAlertDialog.show();
    }


    public void clkCoverImage_details(View v) {
        // Toast.makeText(this, "clicked", Toast.LENGTH_SHORT).show();
        if (flag_isCoverImgPopUpOn==false )
        { flag_isCoverImgPopUpOn = true;

            LayoutInflater layoutInflater = (LayoutInflater) getBaseContext().getSystemService(LAYOUT_INFLATER_SERVICE);
            View popupView = layoutInflater.inflate(R.layout.popup_image, null);
            ImageView mImg = (ImageView) popupView.findViewById(R.id.popupimgvw);
            TextView mTxt = (TextView) popupView.findViewById(R.id.popuptxtVw);

                mImg.setImageBitmap(MyImageHelper.getBitmapFromPath(xtra_image_path));
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


}
