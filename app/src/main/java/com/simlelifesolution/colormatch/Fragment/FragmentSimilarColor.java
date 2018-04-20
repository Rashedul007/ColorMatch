package com.simlelifesolution.colormatch.Fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.simlelifesolution.colormatch.Beans.BeanColor;
import com.simlelifesolution.colormatch.Beans.BeanMain;
import com.simlelifesolution.colormatch.Beans.BeanSimilarColor;
import com.simlelifesolution.colormatch.Helpers.DatabaseHelper;
import com.simlelifesolution.colormatch.Helpers.MyRecycleAdapter_ColorMatching;
import com.simlelifesolution.colormatch.Helpers.MySpinAdapter_PaletteNames;
import com.simlelifesolution.colormatch.R;

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
    private Spinner mSpinner;
    MySpinAdapter_PaletteNames mSpinnerAdapter;
    ArrayList<BeanMain> listPaletteDB ;
    private DatabaseHelper myDbHelper ;
    public String mpalettetNameFromSpinner ="";
    public String mpalettetIDFromSpinner ="";

    //====================================
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
                    btnClkPickerExisting();
                   // Toast.makeText(getActivity(), "Button clk", Toast.LENGTH_SHORT).show();

                } // make dialog and insertcolor to palette
                else if (mView instanceof ImageView) {
                    Toast.makeText(getActivity(), "Color Code: " + mColor_HexConverted, Toast.LENGTH_SHORT).show();
                   /* Intent intnt = new Intent(ColorListFromImageActivity.this, ColorMatchingActivity.class);
                    intnt.putExtra("intnt_colorCode", mColor_HexConverted);
                    startActivity(intnt);*/
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


    public void btnClkPickerExisting()
    {
        AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(getActivity());

        LayoutInflater li = LayoutInflater.from(getActivity());
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
                            Toast.makeText(getActivity(), "Color inserted successfully with row no# : " + dbColorInsert, Toast.LENGTH_SHORT).show();

                            mAlertDialog.dismiss();
                        }
                        else
                            Toast.makeText(getActivity(), "Please insert color name.", Toast.LENGTH_SHORT).show();
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
        mSpinnerAdapter = new MySpinAdapter_PaletteNames(getActivity(), android.R.layout.simple_spinner_item, listPaletteDB);
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

}