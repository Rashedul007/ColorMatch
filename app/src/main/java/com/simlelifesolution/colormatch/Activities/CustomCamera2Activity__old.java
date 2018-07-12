package com.simlelifesolution.colormatch.Activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.simlelifesolution.colormatch.Helpers.App;
import com.simlelifesolution.colormatch.Helpers.MyImageHelper;
import com.simlelifesolution.colormatch.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class CustomCamera2Activity__old extends AppCompatActivity {

    private Context mContext = CustomCamera2Activity__old.this;
    private Activity mActvity = CustomCamera2Activity__old.this;
    private static final String TAG = "AndroidCameraApi";
    private FloatingActionButton takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imgReader;
    private File outputImageFile;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;

//-------
    static String img_Time_Name = "" ;
    String strImgPath = null;

    String getIntent_flag_ImgOrClr, getIntent_pltID, getIntent_pltName, getIntent_imgPath_OR_clrCode;

    private Toolbar mToolbar;
    ImageView mImgViewCover;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_camera2);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Custom Camera");

        mImgViewCover = (ImageView) findViewById(R.id.imgVw_customCameraCover);

        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);

        takePictureButton = (FloatingActionButton) findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

        getIntentPassedData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
       closeCamera();
        stopBackgroundThread();
        super.onPause();
    }


    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };


    private void openCamera()
    {  //region ... call cameraManager, get characterstics, get sizes & store in imageDimension, call manager.openCamera(cameraId, stateCallback, null);
        CameraManager manager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP)
        {
            manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mActvity, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        }
        Log.e(TAG, "openCamera X");

//endregion
    }


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback()
    {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();     }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;    }
    };


    protected void createCameraPreview() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            try {
                SurfaceTexture texture = textureView.getSurfaceTexture();
                assert texture != null;
                texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
                Surface surface = new Surface(texture);

                captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                captureRequestBuilder.addTarget(surface);

                cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        //The camera is already closed
                        if (null == cameraDevice) {
                            return;
                        }
                        // When the session is ready, we start displaying the preview.
                        cameraCaptureSessions = cameraCaptureSession;
                        updatePreview();
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                        Toast.makeText(mContext, "Configuration change", Toast.LENGTH_SHORT).show();
                    }
                }, null);


            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }


    //region ...CameraCaptureSession not used
 /*   final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback()
    {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(mContext, "Saved:" + outputImageFile, Toast.LENGTH_SHORT).show();
            createCameraPreview();
        }
    };*/
//endregion

    protected void startBackgroundThread()
    {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }


    protected void stopBackgroundThread()
    {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void takePicture()
    {
        // ImageReader size, add surfaces, capture_request with surface_add & modeSet

        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

            try {
//region ...imageReader size
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
                Size[] jpegSizes = null;
                if (characteristics != null) {
                    jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                }
                int width = 640;
                int height = 480;
                if (jpegSizes != null && 0 < jpegSizes.length) {
                    width = jpegSizes[0].getWidth();
                    height = jpegSizes[0].getHeight();
                }
                // ImageReader imgReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2);

                 imgReader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 2);
//endregion

//region ...add surface in surfaceList
                List<Surface> outputSurfaces = new ArrayList<Surface>(2);
                outputSurfaces.add(imgReader.getSurface());
                outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
//endregion

//region ...captureRequest, add surface and set ControlMode in request
                final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(imgReader.getSurface());
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                // Orientation
                int rotation = getWindowManager().getDefaultDisplay().getRotation();
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
//endregion



                /* final File*/
                //outputImageFile = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
                img_Time_Name = String.valueOf(System.currentTimeMillis());
                outputImageFile = MyImageHelper.func_makeFolderForImage(mContext, "ColorappImgs", "colorappImg_", img_Time_Name, ".png");

                strImgPath = outputImageFile.getAbsolutePath();

                ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener()
                {
                    //region ...when ImageReader.OnImageAvailableListener then save a outputImageFile pic.jpg
                    @Override
                    public void onImageAvailable(ImageReader reader) {
                        Image image = null;
                        try {
                            image = reader.acquireLatestImage();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);

                            if (bytes != null) {
                                App.getInstance().setCapturedPhotoData(bytes);
                                Intent returnIntent = new Intent();
                                returnIntent.putExtra("strImgpathFromCam2",strImgPath);
                                returnIntent.putExtra("strImgTimeNameCam2",img_Time_Name);
                                setResult(RESULT_OK, returnIntent);

                            } else {
                                setResult(RESULT_CANCELED);     }


                            save(bytes);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (image != null) {
                                image.close();


                                finish();
                            }
                        }
                    }

                    private void save(byte[] bytes) throws IOException {
                        OutputStream output = null;
                        try {
                            output = new FileOutputStream(outputImageFile);
                            output.write(bytes);
                        } finally {
                            if (null != output) {
                                output.close();
                            }
                        }
                    }
//endregion
                };

                imgReader.setOnImageAvailableListener(readerListener, mBackgroundHandler);


//region ...CameraCaptureSession >> Get the Capture Results. The capture result will get back to you asynchronously.
                final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                      //  Toast.makeText(mContext, "Saved:" + outputImageFile, Toast.LENGTH_SHORT).show();
                       // createCameraPreview(); //stopped for finish()
                    }
                };
//endregion


//region ...createCaptureSession >> CaptureRequestSession from the CameraDevice. Consider a CaptureRequestSession as a context in which CaptureRequest will be submitted.
                cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(CameraCaptureSession session) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            try {
                                session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onConfigureFailed(CameraCaptureSession session) {
                    }
                }, mBackgroundHandler);
//endregion

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

    }




    protected void updatePreview() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            if (null == cameraDevice) {
                Log.e(TAG, "updatePreview error, return");
            }
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            try {
                cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }


        if (null != imgReader) {
            imgReader.close();
            imgReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(mContext, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }


//----------------- for custom activity
private void getIntentPassedData()
{
    Bundle extras = getIntent().getExtras();

    if (extras != null) {
        getIntent_pltID = extras.getString("xtra_inside_plt_ID");

        getIntent_pltName = extras.getString("xtra_inside_plt_name");
        getIntent_imgPath_OR_clrCode = extras.getString("xtra_imgPth_or_colorCode");
        getIntent_flag_ImgOrClr = extras.getString("xtra_flag_imgOrClr");

        if(getIntent_flag_ImgOrClr.equals("image"))
        {
            Drawable d = Drawable.createFromPath(getIntent_imgPath_OR_clrCode);
            mImgViewCover.setBackground(d);
        }
        else if(getIntent_flag_ImgOrClr.equals("color"))
        {
            String mCOlorCode = getIntent_imgPath_OR_clrCode.trim();
            mImgViewCover.setBackgroundColor(Color.parseColor(mCOlorCode));
        }


    }
}

}
