package com.simplelifesolutions.palettestudio.Helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.simplelifesolutions.palettestudio.Beans.BeanColor;
import com.simplelifesolutions.palettestudio.Beans.BeanImage;
import com.simplelifesolutions.palettestudio.Beans.BeanObject;
import com.simplelifesolutions.palettestudio.R;

import java.io.File;
import java.util.ArrayList;


public class MyRecycleAdapter_PaletteDetails extends RecyclerView.Adapter<MyRecycleAdapter_PaletteDetails.MyViewHolder>
{
    private Context mContext;
    private ArrayList<BeanObject> mbeanAllObj;

    private onRecyclerViewItemClickListener mItemClickListener;
    private onCoverUpdateListener mCoverUpdateListener;


    private DatabaseHelper myDbHelper;

    String flag_imgOrClr_clicked = "";
    String str_id_clicked = "";

   public MyRecycleAdapter_PaletteDetails(Context mContext, ArrayList<BeanObject> bnObj) {
        this.mContext = mContext;
        this.mbeanAllObj = bnObj;

        myDbHelper = new DatabaseHelper(mContext);
    }

//region interface 1
    public void setOnItemClickListener(onRecyclerViewItemClickListener mItemClickListener) {
        this.mItemClickListener = mItemClickListener;
    }

    public interface onRecyclerViewItemClickListener {
        //void onItemClickListener(View view, int position,String flag_clrImg, String clrOrImgID, String clrCd);
        void onItemClickListener(View view, int position,String flag_clrImg, BeanObject mBeanObj);
    }
//endregion

//region interface for updating cover in details activity
    public void setCoverUpdateListener(onCoverUpdateListener mUpdateListener) {
        this.mCoverUpdateListener = mUpdateListener;
    }

    public interface onCoverUpdateListener {
        void onCoverUpdate();
    }
//endregion


    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
         Button mDeleteButton, mCameraButton, mEyeButton;
         ImageView mImgVw;
         TextView mTxtVw;


        public MyViewHolder(final View view)
        {
            super(view);

           // mDeleteButton = (Button) view.findViewById(R.id.btnDeletePaletteItem);
            mCameraButton  = (Button) view.findViewById(R.id.btnCustomCamera);
            mImgVw = (ImageView) view.findViewById(R.id.imgVw_pltDetails);
            mTxtVw = (TextView) view.findViewById(R.id.title_pltDetails);
            mEyeButton = (Button)view.findViewById(R.id.btnViewPaletteItem);

            mImgVw.setOnClickListener(this);
            mCameraButton.setOnClickListener(this);


           /* Drawable drawable = mContext.getResources().getDrawable(R.drawable.ic_eye);
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, mContext.getResources().getColor(R.color.colorPrimary));

            mEyeButton.setCompoundDrawables(null, drawable, null, null);*/

        }

        @Override
        public void onClick(View v) {
            String clrcod = "0";

            if (mItemClickListener != null)
            {
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
            //holder.mDeleteButton.setTag(_imgObj.getimageId());
            //holder.mDeleteButton.setTag(_imgObj);
            holder.mEyeButton.setTag(_imgObj);

        }
//endregion

//region...... if object is of type "color" then set color as background of imgVw
        else if(flag_imgOrClr.equals("color"))
        {
            BeanColor _clrObj = (BeanColor)_beanObj.getAnyObjLst();

            String mCOlorCode = _clrObj.getColorCode().trim();
            holder.mImgVw.setBackgroundColor(Color.parseColor(mCOlorCode));

            holder.mTxtVw.setText(_clrObj.getColorName());
          //  holder.mDeleteButton.setTag(_clrObj.getColorId());
           // holder.mDeleteButton.setTag(_clrObj);
            holder.mEyeButton.setTag(_clrObj);
        }
//endregion

        holder.mEyeButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //region...............Delete button ................
                AlertDialog.Builder mAlertBuilder = new AlertDialog.Builder(mContext);

                LayoutInflater li = LayoutInflater.from(mContext);
                View promptsView = li.inflate(R.layout.dialog_eye_item, null);

                mAlertBuilder.setPositiveButton("ok", null);
                mAlertBuilder.setNegativeButton("cancel", null);
                mAlertBuilder.setView(promptsView);

                final TextView mEyeTxtVwTitle = (TextView) promptsView.findViewById(R.id.txtVw_eyeItemTitle);
              //  final ImageView mEyeImgVw = (ImageView) promptsView.findViewById(R.id.eye_imgVw);
                final ZoomableImageView mEyeImgVw = (ZoomableImageView) promptsView.findViewById(R.id.eye_imgVw);
                final CheckBox mChkBx_cover = (CheckBox) promptsView.findViewById(R.id.eye_chkBoxCover);
                final CheckBox mChkBx_delete = (CheckBox) promptsView.findViewById(R.id.eye_chkBoxDelete);




//region turn on only one checkbox at a time
                mChkBx_cover.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mChkBx_delete.isChecked())
                            mChkBx_delete.setChecked(false);      }   });

                mChkBx_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mChkBx_cover.isChecked())
                            mChkBx_cover.setChecked(false);      }   });
//endregion

//region set imageview background as image or color
                if(flag_imgOrClr.equals("image")) {
                    BeanImage clkImgObj = (BeanImage) holder.mEyeButton.getTag();

                    mEyeTxtVwTitle.setText(clkImgObj.getimageName());

                    String mainImgPath = clkImgObj.getimagePath() ;
                    try{
                        Bitmap cameraBitmap = MyImageHelper.rotateImageFromURI(mContext,  Uri.fromFile(new File(mainImgPath)));

                    mEyeImgVw.setImageBitmap(cameraBitmap);
                  //  mEyeImgVw.setScaleType(ImageView.ScaleType.FIT_XY);
                    }
                    catch(Exception ex)
                    { Log.d("ErrLog", "" + ex);}

                }
                else if(flag_imgOrClr.equals("color")){
                    BeanColor clkClrObj = (BeanColor) holder.mEyeButton.getTag();

                    mEyeTxtVwTitle.setText(clkClrObj.getColorName());

                    String mCOlorCode = clkClrObj.getColorCode().trim();
                    mEyeImgVw.setBackgroundColor(Color.parseColor(mCOlorCode));
                }


//endregion

                final AlertDialog mAlertDialog = mAlertBuilder.create();
                mAlertDialog.setOnShowListener(new DialogInterface.OnShowListener() {

                    @Override
                    public void onShow(DialogInterface dialog) {

                        Button btnDialog_positive = mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
                        btnDialog_positive.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View view) {
                                Long returnResult = -1L;

                                if(mChkBx_cover.isChecked())
//region update pallete cover in DB
                                {
                                    if(flag_imgOrClr.equals("image")) {
                                        BeanImage clkImgObj = (BeanImage) holder.mEyeButton.getTag();
                                         returnResult = myDbHelper.updateCoverInPalette(clkImgObj.getPaletteID().toString(), "image", clkImgObj.getimageId());
                                    }
                                    else if(flag_imgOrClr.equals("color")) {
                                        BeanColor clkClrObj = (BeanColor) holder.mEyeButton.getTag();
                                         returnResult = myDbHelper.updateCoverInPalette(clkClrObj.getPaletteID().toString(), "color", clkClrObj.getColorId());
                                    }

                                    if(returnResult > -1L)
                                         mAlertDialog.dismiss();
                                    else
                                    {Toast.makeText(mContext, "Sorry can't update cover image.", Toast.LENGTH_SHORT).show();
                                      mAlertDialog.dismiss();  }

                                      if (mCoverUpdateListener != null)
                                            mCoverUpdateListener.onCoverUpdate();
                                }
//endregion
                                else if(mChkBx_delete.isChecked())
//region delete all full & thumb images if flag=image or delete from DB if flg=color
                                {
                                    if(flag_imgOrClr.equals("image")) {
                                        BeanImage clkImgObj = (BeanImage) holder.mEyeButton.getTag();
                                        myDbHelper.deletePaletteItem(clkImgObj.getimageId().toString(), flag_imgOrClr);

                                        String mainImgPath = clkImgObj.getimagePath() ;
                                        String thumbPath = clkImgObj.getThumbPath();

                                        if(chkImageExist(mainImgPath))
                                            MyImageHelper.deleteRecursive(new File(mainImgPath));

                                        if(chkImageExist(thumbPath))
                                            MyImageHelper.deleteRecursive(new File(thumbPath));

                                    }
                                    else if(flag_imgOrClr.equals("color")){
                                        BeanColor clkClrObj = (BeanColor) holder.mEyeButton.getTag();
                                        myDbHelper.deletePaletteItem(clkClrObj.getColorId().toString(), flag_imgOrClr);
                                    }


                                    mbeanAllObj.remove(position);
                                    notifyItemRemoved(position);
                                    notifyItemRangeChanged(position, mbeanAllObj.size());
                                    notifyDataSetChanged();

                                    mAlertDialog.dismiss();
                                }
//endregion
                            }
                        });
                    }
                });
                mAlertDialog.show();





                //endregion
            }
        }); //end of EyeButton

//region oldcode for delete button
        /*holder.mDeleteButton.setOnClickListener(new View.OnClickListener(){
                    @Override
                    public void onClick(View v) {
                        //region...............Delete button ................
                        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:

                                        if(flag_imgOrClr.equals("image")) {
                                            BeanImage clkImgObj = (BeanImage) holder.mDeleteButton.getTag();
                                            myDbHelper.deletePaletteItem(clkImgObj.getimageId().toString(), flag_imgOrClr);

                                            String mainImgPath = clkImgObj.getimagePath() ;
                                            String thumbPath = clkImgObj.getThumbPath();

                                            if(chkImageExist(mainImgPath))
                                                MyImageHelper.deleteRecursive(new File(mainImgPath));

                                            if(chkImageExist(thumbPath))
                                                MyImageHelper.deleteRecursive(new File(thumbPath));

                                        }
                                        else if(flag_imgOrClr.equals("color")){
                                            BeanColor clkClrObj = (BeanColor) holder.mDeleteButton.getTag();
                                            myDbHelper.deletePaletteItem(clkClrObj.getColorId().toString(), flag_imgOrClr);
                                            }


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
        });*/ //end of deleteButton

//endregion

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
