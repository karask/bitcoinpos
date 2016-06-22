package com.cryptocurrencies.bitcoinpos;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
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

    private SurfaceView mCameraView;
    private CameraSource mCameraSource;
    private BarcodeDetector mBarcodeDetector;
    private final int REQUEST_PERMISSION_CAMERA=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scanner);

        // init preferences
        // TODO get package name dynamically!!
        sharedPreferences = getSharedPreferences("com.cryptocurrencies.bitcoinpos_preferences", Context.MODE_PRIVATE);

        // Display toolbar and back arrow -- title and parent is found in activity tag in manifest
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mCameraView = (SurfaceView)findViewById(R.id.camera_view);
        mBarcodeDetector =
                new BarcodeDetector.Builder(this)
                        .setBarcodeFormats(Barcode.QR_CODE)
                        .build();

        mCameraSource = new CameraSource
                .Builder(getApplicationContext(), mBarcodeDetector)
                .setAutoFocusEnabled(true)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedFps(15.0f)
                .build();

        Log.d("CAMERA SOURCE 0", "Is operational: " + mBarcodeDetector.isOperational());

        mCameraView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.CAMERA);
                    if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
                        Log.d("CAMERA SOURCE", "Is operational: " + mBarcodeDetector.isOperational());
                        mCameraSource.start(mCameraView.getHolder());
                    } else {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
                        }
                    }
                } catch (IOException ie) {
                    Log.e("CAMERA SOURCE", ie.getMessage());
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
                if (barcodes.size() != 0) {

                    // set appropriate setting/preference
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString(getString(R.string.payment_address_key), barcodes.valueAt(0).displayValue);
                    editor.commit();

                    // go to settings activity
                    Intent goToSettings = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(goToSettings);

                }
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    Intent goToSelf = new Intent(ScannerActivity.this, ScannerActivity.class);
                    startActivity(goToSelf);
                    finish();
                } else {
                    // permission denied
                    Intent goToSettings = new Intent(getApplicationContext(), SettingsActivity.class);
                    startActivity(goToSettings);
                }
                return;
            }

        }
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
