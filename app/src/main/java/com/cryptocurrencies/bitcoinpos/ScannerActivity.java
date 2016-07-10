package com.cryptocurrencies.bitcoinpos;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

public class ScannerActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private SharedPreferences sharedPreferences;
    private int cameraWidth;
    private MediaPlayer mediaPlayer;

    private SurfaceView mCameraView;
    private CameraSource mCameraSource;
    private BarcodeDetector mBarcodeDetector;

    // flag to make sure that it detects only once
    private boolean firstDetection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // only allow one detection (detects more than once because it is fast)
        firstDetection = true;

        // init preferences
        // TODO get package name dynamically!!
        sharedPreferences = getSharedPreferences("com.cryptocurrencies.bitcoinpos_preferences", Context.MODE_PRIVATE);

        // get display width and initialize cameraWidth
//        DisplayMetrics metrics = new DisplayMetrics();
//        getWindowManager().getDefaultDisplay().getMetrics(metrics);
//        int screenWidth = metrics.widthPixels;
//        cameraWidth = screenWidth * 2 / 3;

        // Display toolbar and back arrow -- title and parent is found in activity tag in manifest
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // init media player with scanner sound
        mediaPlayer = MediaPlayer.create(this, R.raw.scanner);


        mCameraView = (SurfaceView)findViewById(R.id.camera_view);
//        ViewGroup.LayoutParams params = mCameraView.getLayoutParams();
//        params.width = cameraWidth;
//        params.height = cameraWidth;

        mBarcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

        mCameraSource = new CameraSource
                .Builder(getApplicationContext(), mBarcodeDetector)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                //.setRequestedPreviewSize(cameraWidth, cameraWidth)
                .build();

        //Log.d("CAMERA SOURCE 0", "Is operational: " + mBarcodeDetector.isOperational());

        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.CAMERA);
                    if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        //Log.d("CAMERA SOURCE 1", "Is operational: " + mBarcodeDetector.isOperational());
                        mCameraSource.start(mCameraView.getHolder());
                    }
                } catch (IOException ie) {
                    //Log.e("CAMERA SOURCE", ie.getMessage());
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCameraSource.stop();
            }
        });

        mBarcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
            @Override
            public void release() {
            }

            @Override
            public void receiveDetections(Detector.Detections<Barcode> detections) {
                final SparseArray<Barcode> barcodes = detections.getDetectedItems();
                if (barcodes.size() != 0 && firstDetection) {

                    // first detection entered, do not allow second detection ignore sub-sequenct/fast detections
                    firstDetection = false;

                    mediaPlayer.start();

                    // get detected value and validate
                    String addressString = barcodes.valueAt(0).displayValue;
                    String address;
                    Log.d("SCANNER ADDRESS", addressString);

                    // if BIP 21 is used get the address
                    if(BitcoinUtils.isAddressUsingBIP21(addressString)) {
                        address = BitcoinUtils.getAddressFromBip21String(addressString);
                    } else {
                        address = addressString;
                    }

                    boolean isAddressValid = BitcoinUtils.validateAddress(address);
                    if(isAddressValid) {
                        // set appropriate setting/preference
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(getString(R.string.payment_address_key), address);
                        editor.commit();
                    }

                    // go to settings activity
                    Intent goToSettings = new Intent(getApplicationContext(), SettingsActivity.class);
                    goToSettings.putExtra(BitcoinUtils.showAddressInvalidMessage, !isAddressValid);
                    goToSettings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(goToSettings);
                    finish();
                }
            }
        });

    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
