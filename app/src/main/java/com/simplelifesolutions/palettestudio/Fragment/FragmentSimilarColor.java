package com.simplelifesolutions.palettestudio.Fragment;


import android.app.ProgressDialog;
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
import android.widget.RadioButton;
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//----- for converting html pages
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class FragmentSimilarColor extends Fragment
{
//region...... variables declaration
    private ProgressDialog pDialog;

    private static final String TAG = "RecyclerViewFragment";

    private static final int SPAN_COUNT = 3;

    private RecyclerView mRecyclerView;
    private MyRecycleAdapter_ColorMatching mAdapter;

    String  strIntentrecvdColor = ""; //////////delete this line

    //====================================

    String urlSimilar = "http://simple-life-solutions.com/ceye/colour.php";
    ArrayList<BeanSimilarColor> list_beanObjects;

    String mColor_HexConverted;
    int mColor = 0;
    ArrayList<BeanMain> listPaletteDB ;
    private DatabaseHelper myDbHelper ;
    public String mpalettetNameFromSpinner ="";
    public String mpalettetIDFromSpinner ="";

 //-----for dialog view
    EditText mEdtVwPltName_new;
    Spinner mSpinnerPaletteName_exist;
    MySpinAdapter_PaletteNames adapter_Spinner;

    Long paletteID_pkDB = -1L;

//endregion

    public FragmentSimilarColor() {
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

        View rootView =  inflater.inflate(R.layout.fragment_similarcolor, container, false);
        rootView.setTag(TAG);

        setHasOptionsMenu(true);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_similarcolor);

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
                } // make dialog and insertcolor to palette
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

    /*    if(isNetworkAvailable(getActivity()))
            {*/
            if(list_beanObjects!=null)
                list_beanObjects.clear();

            if(!strIntentrecvdColor.equals(""))
                new AsyncConvertHtmlTags().execute();
    //}
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }


  /*  public boolean isNetworkAvailable(Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }*/

//--------------------------------------

    class AsyncConvertHtmlTags extends AsyncTask<String, Integer, String>
    {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... params)
        { String responseStr = "";

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            //String  strIntentrecvdColor = "ff5050"; //////////delete this line
            nameValuePairs.add(new BasicNameValuePair("c", strIntentrecvdColor));
            String paramsString = URLEncodedUtils.format(nameValuePairs, "UTF-8");

            HttpClient httpclient = new DefaultHttpClient();
            HttpGet httpget = new HttpGet(urlSimilar + "?" + paramsString);
            HttpResponse response;

            try {
                response = httpclient.execute(httpget);
                HttpEntity entity = response.getEntity();

                if (entity != null)
                {
                    InputStream instream = entity.getContent();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
                    StringBuilder str = new StringBuilder();
                    String line = null;
                    while((line = reader.readLine()) != null)
                    {
                        str.append(line);
                    }

                    responseStr = str.toString();

                    Log.d("similarColor", responseStr);

                    instream.close();

                    return responseStr;
                }
                else
                    return null;

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


            return responseStr;
        }



        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            list_beanObjects.clear();


            BeanSimilarColor _objSimilarColor ;

            Document doc = Jsoup.parse(s);
            Elements divs = doc.select("div");

            //regex matcher to get the background values pattern to look for all characters between "background:#" and "'"
            Pattern p = Pattern.compile("(?<=background:#)(.*)(?=\")");

            for(Element elem : divs)
            {
                _objSimilarColor = new BeanSimilarColor();

                // String ss =  elem.attr("style");// for all the style properties

                Matcher m = p.matcher(elem.attributes().toString());

                while(m.find()){
                    String str_backgroundDivColor = m.group();
                    Log.d("similarColor", "\n jSoup Div background : " + str_backgroundDivColor ); //background value
                    _objSimilarColor.setColorHexCode(str_backgroundDivColor);
                }

                // text after the div which is the next sibling of the div
                // Log.d("similarColor", "\n after Div : " +  elem.nextSibling().toString().trim() );

                String str_aftrDiv = elem.nextSibling().toString().trim();
                Log.d("similarColor", " after Div split: " + str_aftrDiv);
                _objSimilarColor.setColorType(str_aftrDiv);

                Log.d("similarColor","-----------------------");

                list_beanObjects.add(_objSimilarColor);

            }


           mAdapter.notifyDataSetChanged();

            /*grdVw.setOnItemClickListener(new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View v, int position, long id)
                {
                    String str_colorCodeSimilar = ((TextView) v.findViewById(R.id.listrow_similar_hexcode_hiddden)).getText().toString();
                    Toast.makeText(getApplicationContext(), "ID:: "+ str_colorCodeSimilar , Toast.LENGTH_SHORT).show();

                    Intent retrnIntnt = new Intent();
                    retrnIntnt.putExtra("intnt_similarColor", str_colorCodeSimilar);
                    setResult(RESULT_OK, retrnIntnt);
                    finish();

                }
            });*/

        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }
    }



//------------- add Color to palette

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