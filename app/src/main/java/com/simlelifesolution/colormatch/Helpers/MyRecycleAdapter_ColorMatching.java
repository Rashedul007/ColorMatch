package com.simlelifesolution.colormatch.Helpers;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.simlelifesolution.colormatch.Beans.BeanSimilarColor;
import com.simlelifesolution.colormatch.R;

import java.io.File;
import java.util.List;


public class MyRecycleAdapter_ColorMatching extends RecyclerView.Adapter<MyRecycleAdapter_ColorMatching.MyViewHolder>
{
    private Context mContext;
    private List<BeanSimilarColor> beanClassList_s;

    private onRecyclerViewItemClickListener mItemClickListener;

    private DatabaseHelper myDbHelper;

   public MyRecycleAdapter_ColorMatching(Context mContext, List<BeanSimilarColor> beanClassList) {
        this.mContext = mContext;
        this.beanClassList_s = beanClassList;

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
         ImageView mImgVw;
         TextView mTxtVw;
        Button mBtn_AddColrToPlt;

        public MyViewHolder(final View view)
        {
            super(view);

            mImgVw = (ImageView) view.findViewById(R.id.colorImageView);
            mTxtVw = (TextView) view.findViewById(R.id.colorTitle);
            mBtn_AddColrToPlt = (Button) view.findViewById(R.id.btnAddColor_ToExistingPalette_frmMatch);

            mImgVw.setOnClickListener(this);
            mBtn_AddColrToPlt.setOnClickListener(this);
        }

        @Override
        public void onClick(View v)
        { if (mItemClickListener != null)
        {  String  _eachStr = beanClassList_s.get(getAdapterPosition()).getColorHexCode();
            mItemClickListener.onItemClickListener(v, _eachStr);
        }
        }
    }


    @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_card_colormatch, parent, false);

        return new MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position)
    {
      final   BeanSimilarColor  beanClass = beanClassList_s.get(position);
        String colrCode = "#"+ beanClass.getColorHexCode();

        holder.mTxtVw.setText(beanClass.getColorType() );

        holder.mImgVw.setBackgroundColor(Color.parseColor(colrCode));
    }

    private Boolean chkImageExist(String pth)
    {
       File f = new File(pth);
        if(f.exists())
            return true;
        else
            return false;

    }


    @Override
    public int getItemCount() {
        return beanClassList_s.size();
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
