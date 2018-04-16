package com.simlelifesolution.colormatch.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.simlelifesolution.colormatch.R;

public class MainActivity extends AppCompatActivity
{
//region...... variables declaration
    Button mCameraBtn, mGalleryBtn, mPalette, mColorPickerBtn;
    TextView mTxtVwColorCode;
    EditText mEtVwAutoCompleteColor;

    private static final int PERMISSION_CALLBACK_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 103;

    private static int btnPressType = 0 ;

    String[] permissionsRequired = new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

    private static final int REQUEST_COLOR_PICKER_ACTIVITY = 9;
//endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);
//        getActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#33B5E5")));

        initialize();
    }

    private void initialize() {
        mCameraBtn = (Button)findViewById(R.id.btnCamera);
        mGalleryBtn = (Button)findViewById(R.id.btnGallery);
        mPalette = (Button)findViewById(R.id.btnMyPalette);
        mColorPickerBtn = (Button)findViewById(R.id.btnColorPicker);
        mTxtVwColorCode = (TextView)findViewById(R.id.txtVwColorCode);
      //  mEtVwAutoCompleteColor = (EditText)findViewById(R.id.etAutoColor);
    }


    public void buttonOnClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btnCamera:
                btnPressType = 1;
                func_getPermission();
              //  proceedAfterPermission();
                break;

            case R.id.btnGallery:
                btnPressType = 2;
                func_getPermission();
                break;

            case R.id.btnColorPicker:
                Intent colorPkrIntent = new Intent(MainActivity.this, ColorPickerActivity.class);
                startActivityForResult(colorPkrIntent, 9);
                break;

            case R.id.btnMyPalette:
                Intent intentPaletteActivity = new Intent(MainActivity.this, PaletteListActivity.class);
                startActivity(intentPaletteActivity);
                break;

            default:
                break;
        }

    }

    public void func_getPermission()
    {

        /* for single permission
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CAMERA))*/
        if(ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(MainActivity.this, permissionsRequired[2]) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[2]))
            {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Retional: Need multiple Permission");
                builder.setMessage("This app needs camera & storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                       /* for single permission
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
                         */
                        ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            }

            else if(permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again'- which results in shouldShowRequestPermissionRationale=false and requestPermissions method will do just nothing. So we need to remember whether we ave priorly requested for permission or not, even after the app restarts
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("sharedPref: Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(MainActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }

            // the below codes will run everytime until we get permission
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0],true);
            editor.commit();
            Log.d("Log_pref", String.valueOf(permissionStatus.getBoolean(Manifest.permission.CAMERA,false)));


        } else {
            //You already have the permission, just go ahead.
            proceedAfterPermission();
        }
    }

    private void proceedAfterPermission()
    {
        //We've got the permission, now we can proceed further
      //  Toast.makeText(getBaseContext(), "We got the Storage Permission", Toast.LENGTH_LONG).show();

        Intent intnt = new Intent(this, ImageUploadActivity.class);
        intnt.putExtra("btnPressed",btnPressType);
        startActivity(intnt);
            // startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAG_CAMERA);
            //startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), TAG_GALLERY);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CALLBACK_CONSTANT){
            //check if all permissions are granted
            boolean allgranted = false;
            for(int i=0;i<grantResults.length;i++){
                if(grantResults[i]==PackageManager.PERMISSION_GRANTED){
                    allgranted = true;
                } else {
                    allgranted = false;
                    break;
                }
            }

            if(allgranted){
                proceedAfterPermission();
            }
            else if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[1])
                    || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,permissionsRequired[2])){
               // txtPermissions.setText("Permissions Required");
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Location permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this,permissionsRequired,PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                Toast.makeText(getBaseContext(),"Unable to get Permission",Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
     if(requestCode == REQUEST_COLOR_PICKER_ACTIVITY)
        {  /*if(resultCode == Activity.RESULT_OK)
            { String resultColorPkr = data.getStringExtra("resultPkr");
            //    Toast.makeText(this, resultColorPkr, Toast.LENGTH_SHORT).show();
               mTxtVwColorCode.setText(resultColorPkr); } */
        }
    }


}
