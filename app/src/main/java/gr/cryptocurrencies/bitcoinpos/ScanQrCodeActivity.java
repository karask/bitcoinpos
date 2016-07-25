package gr.cryptocurrencies.bitcoinpos;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;

import com.journeyapps.barcodescanner.CaptureManager;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class ScanQrCodeActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private DecoratedBarcodeView mDecoratedBarcodeView;
    private CaptureManager capture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr_code);

        // Display toolbar and back arrow -- title and parent is found in activity tag in manifest
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // scanner view
        mDecoratedBarcodeView = (DecoratedBarcodeView) findViewById(R.id.zxing_barcode_scanner);

        capture = new CaptureManager(this, mDecoratedBarcodeView);
        capture.initializeFromIntent(getIntent(), savedInstanceState);
        capture.decode();
    }


    @Override
    protected void onResume() {
        super.onResume();

        // If API 23 permissions will be asked and onResume it will ask to allow/deny
        // After choice onResume is run again, however scanner get stuck if run immediately.
        // Thus for API 23+ we run onResume() with a small delay to bypass this glitch with
        // a small tradeoff in efficiency
        // For API <23 permissions are checked on installation thus no dialog/no getting stuck
        // TODO Revisit for cleaner solution
        if(Build.VERSION.SDK_INT >= 23) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    capture.onResume();
                }
            }, 10);
        } else {
            capture.onResume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        capture.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        capture.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        capture.onSaveInstanceState(outState);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mDecoratedBarcodeView.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
    }



}
