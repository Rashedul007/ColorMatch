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
import android.widget.TextView;

import com.simlelifesolution.colormatch.Beans.BeanColor;
import com.simlelifesolution.colormatch.Beans.BeanImage;
import com.simlelifesolution.colormatch.Beans.BeanObject;
import com.simlelifesolution.colormatch.R;

import java.io.File;
import java.util.ArrayList;


public class MyRecycleAdapter_PaletteDetails extends RecyclerView.Adapter<MyRecycleAdapter_PaletteDetails.MyViewHolder>
{
    private Context mContext;
    private ArrayList<BeanObject> mbeanAllObj;

    private onRecyclerViewItemClickListener mItemClickListener;

    private DatabaseHelper myDbHelper;

    String flag_imgOrClr_clicked = "";
    String str_id_clicked = "";

   public MyRecycleAdapter_PaletteDetails(Context mContext, ArrayList<BeanObject> bnObj) {
        this.mContext = mContext;
        this.mbeanAllObj = bnObj;

        myDbHelper = new DatabaseHelper(mContext);
    }

    public void setOnItemClickListener(onRecyclerViewItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onRecyclerViewItemClickListener {
        void onItemClickListener(View view, int position,String flag_clrImg, String clrOrImgID);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
         Button mDeleteButton;
         ImageView mImgVw;
         TextView mTxtVw;


        public MyViewHolder(final View view)
        {
            super(view);

            mDeleteButton = (Button) view.findViewById(R.id.btnDeletePaletteItem);
            mImgVw = (ImageView) view.findViewById(R.id.imgVw_pltDetails);
            mTxtVw = (TextView) view.findViewById(R.id.title_pltDetails);

            mImgVw.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            if (mItemClickListener != null) {
                str_id_clicked="";

                BeanObject  _beanObj = mbeanAllObj.get(getAdapterPosition());
                String flag_imgOrClr = mbeanAllObj.get(getAdapterPosition()).getFlag_imgOrClr();

                if(flag_imgOrClr.equals("image"))
                {
                    BeanImage _imgObj = (BeanImage)_beanObj.getAnyObjLst();
                    flag_imgOrClr_clicked = "image";
                    str_id_clicked = _imgObj.getimageId();
                }

                else if(flag_imgOrClr.equals("color"))
                {
                    BeanColor _clrObj = (BeanColor)_beanObj.getAnyObjLst();
                    flag_imgOrClr_clicked = "color";
                    str_id_clicked = _clrObj.getColorId();
                }

               mItemClickListener.onItemClickListener(v, getAdapterPosition(), flag_imgOrClr_clicked, str_id_clicked);
            }
        }
    }


    @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_card_palettedetails, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position)
    {
      final   BeanObject  _beanObj = mbeanAllObj.get(position);
      final String flag_imgOrClr = _beanObj.getFlag_imgOrClr();

       if(flag_imgOrClr.equals("image"))
        {
            BeanImage _imgObj = (BeanImage)_beanObj.getAnyObjLst();

            Drawable d = Drawable.createFromPath(_imgObj.getimagePath());
            holder.mImgVw.setBackground(d);

            holder.mTxtVw.setText(_imgObj.getimageName() );
            holder.mDeleteButton.setTag(_imgObj.getimageId());
        }
        else if(flag_imgOrClr.equals("color"))
        {
            BeanColor _clrObj = (BeanColor)_beanObj.getAnyObjLst();

            String mCOlorCode = _clrObj.getColorCode().trim();
            holder.mImgVw.setBackgroundColor(Color.parseColor(mCOlorCode));

            holder.mTxtVw.setText(_clrObj.getColorName());
            holder.mDeleteButton.setTag(_clrObj.getColorId());
        }


        holder.mDeleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
               // Log.e("DBDel",  "AdapterPos:: "+ position + " ID::"+ holder.mDeleteButton.getTag() + "\n");
                myDbHelper.deletePaletteItem(holder.mDeleteButton.getTag().toString(), flag_imgOrClr);

//----------to rearrange the list
                mbeanAllObj.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, mbeanAllObj.size());
                notifyDataSetChanged();
            }
        });

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
        return mbeanAllObj.size();
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
