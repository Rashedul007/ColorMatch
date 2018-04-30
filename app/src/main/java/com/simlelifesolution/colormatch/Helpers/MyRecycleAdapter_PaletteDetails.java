package com.simlelifesolution.colormatch.Helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.simlelifesolution.colormatch.Activities.CallingCameraActivity;
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
        //void onItemClickListener(View view, int position,String flag_clrImg, String clrOrImgID, String clrCd);
        void onItemClickListener(View view, int position,String flag_clrImg, BeanObject mBeanObj);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
         Button mDeleteButton, mCameraButton;
         ImageView mImgVw;
         TextView mTxtVw;


        public MyViewHolder(final View view)
        {
            super(view);

            mDeleteButton = (Button) view.findViewById(R.id.btnDeletePaletteItem);
            mCameraButton  = (Button) view.findViewById(R.id.btnCustomCamera);
            mImgVw = (ImageView) view.findViewById(R.id.imgVw_pltDetails);
            mTxtVw = (TextView) view.findViewById(R.id.title_pltDetails);

            mImgVw.setOnClickListener(this);
            mCameraButton.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            String clrcod="0";

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
                    clrcod = _clrObj.getColorCode();
                }
                mItemClickListener.onItemClickListener(v, getAdapterPosition(), flag_imgOrClr_clicked, _beanObj);
             //  mItemClickListener.onItemClickListener(v, getAdapterPosition(), flag_imgOrClr_clicked, str_id_clicked, clrcod);
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

//region...... if object is of type "image" then set image for imgVw
       if(flag_imgOrClr.equals("image"))
        {
            BeanImage _imgObj = (BeanImage)_beanObj.getAnyObjLst();

            Drawable d = Drawable.createFromPath(_imgObj.getThumbPath());
            holder.mImgVw.setBackground(d);

            holder.mTxtVw.setText(_imgObj.getimageName() );
            holder.mDeleteButton.setTag(_imgObj.getimageId());
        }
//endregion

//region...... if object is of type "color" then set color as background of imgVw
        else if(flag_imgOrClr.equals("color"))
        {
            BeanColor _clrObj = (BeanColor)_beanObj.getAnyObjLst();

            String mCOlorCode = _clrObj.getColorCode().trim();
            holder.mImgVw.setBackgroundColor(Color.parseColor(mCOlorCode));

            holder.mTxtVw.setText(_clrObj.getColorName());
            holder.mDeleteButton.setTag(_clrObj.getColorId());
        }
//endregion

        holder.mDeleteButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //region...............Delete button ................
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:

                                        myDbHelper.deletePaletteItem(holder.mDeleteButton.getTag().toString(), flag_imgOrClr);

                                        mbeanAllObj.remove(position);
                                        notifyItemRemoved(position);
                                        notifyItemRangeChanged(position, mbeanAllObj.size());
                                        notifyDataSetChanged();


                                        break;

                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //
                                        break;
                                }
                            }
                        };



                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("Are you sure you want to delete the item?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();

 //endregion
            }
        }); //end of deleteButton


      /*  holder.mCameraButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //region...............Delete button ................
                    // Toast.makeText(mContext, "Camera", Toast.LENGTH_SHORT).show();
                    Intent intntCameraAct = new Intent(mContext, CallingCameraActivity.class);
                    mContext.startActivity(intntCameraAct);
                //endregion
            }
        });*/ //end of cameraButton


    } //end of onBindViewHolder


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
