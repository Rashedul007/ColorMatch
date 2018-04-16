package com.simlelifesolution.colormatch.Activities;

import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.simlelifesolution.colormatch.Beans.BeanColor;
import com.simlelifesolution.colormatch.Beans.BeanMain;
import com.simlelifesolution.colormatch.Helpers.DatabaseHelper;
import com.simlelifesolution.colormatch.Helpers.MyGradientView;
import com.simlelifesolution.colormatch.Helpers.MySpinAdapter_PaletteNames;
import com.simlelifesolution.colormatch.R;

import java.util.ArrayList;

public class ColorPickerActivity extends AppCompatActivity
{
//region...... variables declaration
    private MyGradientView mTop;
    private MyGradientView mBottom;
    private TextView mTextView;
    private Drawable mIcon;
    private Button mBtnColorPicker;
    int mColor = 0;

    String colorPickerResult_hexColor ="";

    private DatabaseHelper myDbHelper;
    MySpinAdapter_PaletteNames mSpinnerAdapter;
    private Spinner mSpinner;
    ArrayList<BeanMain> listPaletteDB ;
    public String mpalettetNameFromSpinner ="";
    public String mpalettetIDFromSpinner ="";
//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(this, R.layout.activity_color_picker, null);
        setContentView(view);

        myDbHelper = new DatabaseHelper(this);
        listPaletteDB = new ArrayList<BeanMain>();

        mIcon = getResources().getDrawable(R.mipmap.colorchk);
        mTextView = (TextView)findViewById(R.id.txtVwColorCode);
        mBtnColorPicker = (Button)findViewById(R.id.btnColorPicker);


        mTextView.setCompoundDrawablesWithIntrinsicBounds(mIcon, null, null, null);
        mTop = (MyGradientView)findViewById(R.id.pickerTop);
        mBottom = (MyGradientView)findViewById(R.id.pickerBottom);
        mTop.setBrightnessGradientView(mBottom);
        mBottom.setOnColorChangedListener(new MyGradientView.OnColorChangedListener() {
            @Override
            public void onColorChanged(MyGradientView view, int color) {
                mTextView.setTextColor(color);
                mColor = color;
                mTextView.setText("#" + Integer.toHexString(color));
                 colorPickerResult_hexColor = String.format("#%06X", (0xFFFFFF & color));
                Log.d("colroCHk"," Color:: " +color +"\n rgbconverted:: " +colorPickerResult_hexColor );

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mIcon.setTint(color);
                }
            }
        });

        int color = 0xFF394572;
        mTop.setColor(color);
    }


    public void btnClkPickerExisting(View v)
    {
        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(ColorPickerActivity.this);

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
        mTxtVwColorCode.setText(colorPickerResult_hexColor);


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
                            BeanColor _PaletteObj = new BeanColor("NULL", mpalettetIDFromSpinner, colorPickerResult_hexColor, mEdtVwColorName.getText().toString(), "");
                            Long dbColorInsert = myDbHelper.insert_newColor(_PaletteObj);
                            //Log.d("dbResult", "DBresult:::" + paletteID_pkDB.toString());
                            Toast.makeText(ColorPickerActivity.this, "Color inserted successfully with row no# : " + dbColorInsert, Toast.LENGTH_SHORT).show();

                            if (dbColorInsert == -1)
                                Toast.makeText(ColorPickerActivity.this, "Something went wrong when saving the color in existing palette!", Toast.LENGTH_SHORT).show();
                            else
                            {
                                Toast.makeText(ColorPickerActivity.this, "Color saved succssfuly!", Toast.LENGTH_SHORT).show();
                                Long dbUpdateCover = myDbHelper.updateCoverInPalette(mpalettetIDFromSpinner.toString(), "color", dbColorInsert.toString());
                            }

                            mAlertDialog.dismiss();
                        }
                        else
                            Toast.makeText(ColorPickerActivity.this, "Please insert color name.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mAlertDialog.show();
    }

    private void setupSpinner_paletteList()
    {
        //**********************
        if(listPaletteDB.size() >0)
           listPaletteDB.clear();

        listPaletteDB = myDbHelper.getPaletteList();
        // BeanMain _paletteObj = new BeanMain();


        mSpinnerAdapter = new MySpinAdapter_PaletteNames(ColorPickerActivity.this, android.R.layout.simple_spinner_item, listPaletteDB);
        //mySpinner = (Spinner) findViewById(R.id.countrySpinner);
        mSpinner.setAdapter(mSpinnerAdapter);

        mSpinner.setSelection(listPaletteDB.size()-1);      // this is so that it auto selects the palette if any new palette created

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id)
            {
                BeanMain _paletteObj = (BeanMain) mSpinnerAdapter.getItem(position);
                // Toast.makeText(CityActivity.this, aCountry.getName(), 2000).show();

                mpalettetNameFromSpinner = _paletteObj.getPaletteName().toString();
                mpalettetIDFromSpinner = _paletteObj.getPaletteID();
                //Toast.makeText(ColorPickerActivity.this, ""+ _paletteObj.getPaletteID() + "---" + _paletteObj.getPaletteName(), Toast.LENGTH_SHORT).show();
                //Log.d("dbResult", mpalettetNameFromSpinner + mpalettetIDFromSpinner);

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
                            // Log.d("dbResult", "DBresult:::" + paletteID_pkDB.toString());

                            //---------------------------------------------------
                            if(paletteID_pkDB != -1)
                            {
                                setupSpinner_paletteList();
                                mAlertDialog.dismiss();

                            }
                            else{
                                Toast.makeText(ColorPickerActivity.this, "Something went wrong when creating a new palette!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                            Toast.makeText(ColorPickerActivity.this, "Please insert palette name.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        mAlertDialog.show();
    }

}
