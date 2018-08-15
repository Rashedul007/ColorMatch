package com.usefullittleappproduction.palettestudio.Helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.ActivityChooserView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.usefullittleappproduction.palettestudio.R;
import java.util.ArrayList;


public class MyRecycleAdapter_ColorListFromImage extends RecyclerView.Adapter<MyRecycleAdapter_ColorListFromImage.MyViewHolder>
{
    private Context mContext;
    private ArrayList<String> mColorArrarLst;

    private onRecyclerViewItemClickListener mItemClickListener;

    private DatabaseHelper myDbHelper;

    String flag_imgOrClr_clicked = "";
    String str_id_clicked = "";

   public MyRecycleAdapter_ColorListFromImage(Context mContext, ArrayList<String> strLst) {
        this.mContext = mContext;
        this.mColorArrarLst = strLst;

        myDbHelper = new DatabaseHelper(mContext);
    }

    public void setOnItemClickListener(onRecyclerViewItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onRecyclerViewItemClickListener {
        void onItemClickListener(View view, String _eachColor);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
         Button mBtn_AddColrToPlt;
         ImageView mImgVw;
         TextView mTxtVw;


        public MyViewHolder(final View view)
        {
            super(view);

            mBtn_AddColrToPlt = (Button) view.findViewById(R.id.btnAddColor_ToExistingPalette);
            mImgVw = (ImageView) view.findViewById(R.id.imgVw_colorfromimg);
            mTxtVw = (TextView) view.findViewById(R.id.title_colorCodeFromImage);

            mImgVw.setOnClickListener(this);
            mBtn_AddColrToPlt.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        { if (mItemClickListener != null)
            {  String  _eachStr = mColorArrarLst.get(getAdapterPosition());
                mItemClickListener.onItemClickListener(v, _eachStr);
            }
        }
    }


    @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_card_colorsfromimage, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position)
    {
      final   String  _eachColorCode = mColorArrarLst.get(position);
        //int intColor = Integer.valueOf("10a010");
        //  String colrCode = String.format("#%06X", (0xFFFFFF & intColor));
      String colrCode =  _eachColorCode;



      holder.mTxtVw.setText(colrCode);
       holder.mImgVw.setBackgroundColor(Color.parseColor(colrCode));
    }


    @Override
    public int getItemCount() {
        return mColorArrarLst.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }


}

