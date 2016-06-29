package com.cryptocurrencies.bitcoinpos;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // get caller params
        Intent caller = getIntent();
        boolean showAddressInvalidMessage = caller.getBooleanExtra(BitcoinUtils.showAddressInvalidMessage, false);

        // Display toolbar and back arrow -- title and parent is found in activity tag in manifest
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsFragment())
                .commit();

        if(showAddressInvalidMessage) {
            Toast.makeText(getBaseContext(), R.string.invalid_bitcoin_address_message, Toast.LENGTH_SHORT).show();
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





    public static class SettingsFragment extends PreferenceFragmentCompat  implements SharedPreferences.OnSharedPreferenceChangeListener {

        SharedPreferences sharedPreferences;
        private final int REQUEST_PERMISSION_CAMERA=1;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            // Set preferences' values to summaries by reusing onSharedPreferenceChanged
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.merchant_name_key));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.payment_address_key));
            onSharedPreferenceChanged(sharedPreferences, getString(R.string.local_currency_key));
        }

        @Override
        public void onResume() {
            super.onResume();
            //unregister the preferenceChange listener
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference(key);
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }
            } else if (preference instanceof EditTextPreference) {
                preference.setSummary(sharedPreferences.getString(key, ""));
            } else {
                preference.setSummary(sharedPreferences.getString(key, ""));
            }
        }

        @Override
        public boolean onPreferenceTreeClick(final Preference preference) {
            if(preference.getKey().equals(getString(R.string.payment_address_key))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.add_payment_address_title);
                builder.setMessage(getString(R.string.add_payment_address_message));

                // get camera's permission
                int permissionCheck = ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                        android.Manifest.permission.CAMERA);

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
                } else {
                    // SCAN button
                    builder.setNegativeButton(getString(R.string.scan), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Intent goToScanner = new Intent(getContext(), ScannerActivity.class);
                            startActivity(goToScanner);
                        }
                    });

                    // PASTE button
                    builder.setPositiveButton(getString(R.string.paste), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            requestPastePaymentAddress();
                        }
                    });

                    builder.show();
                }

            }
            return super.onPreferenceTreeClick(preference);
        }


        @Override
        public void onPause() {
            super.onPause();
            //unregister the preference change listener
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

//        @Override
//        public void onDestroy() {
//            super.onDestroy();
//            //unregister event bus.
//            EventBus.unregister(this);
//        }


        public void requestPastePaymentAddress() {
            AlertDialog.Builder getAddressDialog = new AlertDialog.Builder(SettingsFragment.this.getContext());
            getAddressDialog.setTitle(getString(R.string.payment_address));
            getAddressDialog.setMessage(getString(R.string.paste_bitcoin_address_message));

            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            getAddressDialog.setView(input);

            getAddressDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // get value and validate
                    String address = input.getText().toString();
                    boolean isAddressValid = BitcoinUtils.validateAddress(address);
                    if(isAddressValid) {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString(getString(R.string.payment_address_key), input.getText().toString());
                        editor.commit();
                        findPreference(getString(R.string.payment_address_key)).setSummary(input.getText().toString());
                    } else {
                        Toast.makeText(getContext(), R.string.invalid_bitcoin_address_message, Toast.LENGTH_LONG).show();
                    }                            }
            });
            getAddressDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            // InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            // imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            getAddressDialog.show();
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
                        Intent goToScanning = new Intent(getContext(), ScannerActivity.class);
                        startActivity(goToScanning);
                        getActivity().finish();
                    } else {
                        // permission denied (whether never ask is checked or not
                        requestPastePaymentAddress();
                    }
                    return;
                }

            }
        }

    }




    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {


        // call fragment's onRequestPermissionsResult !!
        getSupportFragmentManager().findFragmentById(R.id.content_frame).onRequestPermissionsResult(requestCode, permissions, grantResults);
    }



}
