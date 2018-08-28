package com.simplelifesolutions.palettestudio.Fragment;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.simplelifesolutions.palettestudio.Activities.PaletteDetailsActivity;
import com.simplelifesolutions.palettestudio.Beans.BeanColor;
import com.simplelifesolutions.palettestudio.Beans.BeanMain;
import com.simplelifesolutions.palettestudio.Beans.BeanSimilarColor;
import com.simplelifesolutions.palettestudio.Helpers.DatabaseHelper;
import com.simplelifesolutions.palettestudio.Helpers.MyRecycleAdapter_ColorMatching;
import com.simplelifesolutions.palettestudio.Helpers.MySpinAdapter_PaletteNames;
import com.simplelifesolutions.palettestudio.R;

//top convert html pages
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.RedirectException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FragmentComplementaryColor extends Fragment
{
//region...... variables declaration
    private final static int COUNT_NO_OF_COMPLEMENTS = 7;

    private static final String TAG = "RecyclerViewFragment";

    private static final int SPAN_COUNT = 3;

    private RecyclerView mRecyclerView;
    private MyRecycleAdapter_ColorMatching mAdapter;

    String  strIntentrecvdColor = ""; //////////delete this line


    //====================================

   ArrayList<BeanSimilarColor> list_beanObjects;

    String mColor_HexConverted;
    int mColor = 0;
    ArrayList<BeanMain> listPaletteDB ;
    private DatabaseHelper myDbHelper ;
    public String mpalettetNameFromSpinner ="";
    public String mpalettetIDFromSpinner ="";

    //====================================

    //-----for dialog view
    EditText mEdtVwPltName_new;
    Spinner mSpinnerPaletteName_exist;
    MySpinAdapter_PaletteNames adapter_Spinner;

    Long paletteID_pkDB = -1L;

//endregion

    public FragmentComplementaryColor() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        list_beanObjects = new ArrayList<BeanSimilarColor>();
        listPaletteDB = new ArrayList<BeanMain>();
        myDbHelper = new DatabaseHelper(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Bundle bundle_xtra = getActivity().getIntent().getExtras();
        if(bundle_xtra != null)
            strIntentrecvdColor = bundle_xtra.getString("intnt_colorCode");

        View rootView =  inflater.inflate(R.layout.fragment_complementarycolor, container, false);
        rootView.setTag(TAG);

        setHasOptionsMenu(true);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_complementarycolor);

        mAdapter = new MyRecycleAdapter_ColorMatching(getActivity(), list_beanObjects);

        RecyclerView.LayoutManager mLayoutManager = new GridLayoutManager(getActivity(),SPAN_COUNT);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new MyRecycleAdapter_ColorMatching.onRecyclerViewItemClickListener()
        {
            @Override
            public void onItemClickListener(View mView, String clrCode) {
                mColor_HexConverted = "#"+clrCode ;
                if (mView instanceof Button) {
                    mColor = Color.parseColor(mColor_HexConverted);
                    addColorToPlt(mColor);
                    //btnClkPickerExisting();
                 }
                else if (mView instanceof ImageView) {
                    Toast.makeText(getActivity(), "Color Code: " + mColor_HexConverted, Toast.LENGTH_SHORT).show();
                 }
            }
        });

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();

            if(list_beanObjects!=null)
                list_beanObjects.clear();

            if(!strIntentrecvdColor.equals("")) {
                int intClrHex = Color.parseColor(strIntentrecvdColor);

                ArrayList<Integer> returnIntColors =  new ArrayList<Integer>();

                if(returnIntColors.size() > 0)
                    returnIntColors.clear();

                returnIntColors =  getMultiComplementaryColor_new(intClrHex, COUNT_NO_OF_COMPLEMENTS);


                BeanSimilarColor _objSimilarColor ;

                for(int eachClr: returnIntColors)
                {
                    _objSimilarColor = new BeanSimilarColor();

                    _objSimilarColor.setColorHexCode(convertIntToHexColor(eachClr));
                    _objSimilarColor.setColorType(convertIntToHexColor(eachClr));

                    list_beanObjects.add(_objSimilarColor);

                    Log.d("Log_complement", "Returned Colors:: " + eachClr );
                    Log.d("Log_complement", "Returned Colors HEX:: " + convertIntToHexColor(eachClr));
                }

                mAdapter.notifyDataSetChanged();
            }

    }



    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

//--------------------------------------

    public static ArrayList<Integer> getMultiComplementaryColor_new(int colorToInvert, int howManyComplementNeeded) {
        float[] hsv = new float[3];
        Color.RGBToHSV(Color.red(colorToInvert), Color.green(colorToInvert),  Color.blue(colorToInvert), hsv);

        Log.d("Log_complement", "Colors:: Main multi:: " + hsv[0]);
        Log.d("Log_complement", "ColorsInt:: Main multi::" + Color.HSVToColor(hsv));

        ArrayList<Integer> returnColorLst = new ArrayList<>();

        float[] complement_hues = new float[howManyComplementNeeded];

        int needToadd = 360/(howManyComplementNeeded+1);

        Log.d("Log_complement", "Main:: " + hsv[0]);
        for(int i=0; i<howManyComplementNeeded ; i++)
        {
            if(i==0)
                complement_hues[0] = (hsv[0] + needToadd) % 360;
            else
                complement_hues[i] = (complement_hues[i-1] + needToadd) % 360;

            hsv[0] = complement_hues[i];

            returnColorLst.add(Color.HSVToColor(hsv));

            Log.d("Log_complement", "Colors:: " + complement_hues[i]);
            Log.d("Log_complement", "ColorsInt:: " + returnColorLst.get(i));
        }

        Log.d("Log_complement_count", "SIze in func::  " + returnColorLst.size() );

        return returnColorLst;
    }

    private String convertIntToHexColor(int intColor)
    {
        //String hexColor = String.format("#%06X", (0xFFFFFF & intColor));
        String hexColor = String.format("%06X", (0xFFFFFF & intColor));
        return hexColor;
    }

//------------- add to palette

    private void addColorToPlt(int clr)
    {
        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater li = LayoutInflater.from(getActivity());
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


        mRadioExist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });

        mRadioNew.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onRadioButtonClicked(v);
            }
        });
        //mRadioNew.setOnClickListener(radio_listener);

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
                        {

                            mAlertDialog.dismiss();

                            Intent intent_DetailsAct = new Intent(getActivity(), PaletteDetailsActivity.class);
                            intent_DetailsAct.putExtra("xtra_pltID_fromListClk", mpalettetIDFromSpinner);
                            intent_DetailsAct.putExtra("xtra_pltName_fromListClk", mpalettetNameFromSpinner);

                            intent_DetailsAct.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                            startActivity(intent_DetailsAct);

                            getActivity().finish();}
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
                Toast.makeText(getActivity(), "Palette Name already exists! Please try another name.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getActivity(), "Something went wrong when saving the color in the palette!", Toast.LENGTH_SHORT).show();
                    else {
                        if (mChkBx.isChecked()) {
                            Toast.makeText(getActivity(), "Image saved succssfuly!", Toast.LENGTH_SHORT).show();
                            Long dbUpdateCover = myDbHelper.updateCoverInPalette(paletteID_pkDB.toString(), "color", dbColorInsert.toString());
                        }
                        return 1;
                    }
                } else
                    Toast.makeText(getActivity(), "Something went wrong when creating a new palette!", Toast.LENGTH_SHORT).show();
            }
        } else    // either PaletteName or ImageName was not given
            Toast.makeText(getActivity(), "Please check the Palette & Color Name!", Toast.LENGTH_SHORT).show();

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
                Toast.makeText(getActivity(), "Something went wrong when saving the color in existing palette!", Toast.LENGTH_SHORT).show();
            else
            {
                if(mChkBx_addAsCover.isChecked()) {
//                    Toast.makeText(mContext, "Color saved succssfuly!", Toast.LENGTH_SHORT).show();
                    Long dbUpdateCover = myDbHelper.updateCoverInPalette(mpalettetIDFromSpinner.toString(), "color", dbColorInsert.toString());
                }
                return 1;
            }

        }
        else
            Toast.makeText(getActivity(), "Please insert color name!", Toast.LENGTH_SHORT).show();

        return 0;
    }


    private void setup_spinnerItems(Spinner mSpn)
    {
        ArrayList<BeanMain> listPaletteDB = myDbHelper.getPaletteList();

        adapter_Spinner = new MySpinAdapter_PaletteNames(getActivity(), android.R.layout.simple_spinner_item, listPaletteDB);
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