package gr.cryptocurrencies.bitcoinpos;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import gr.cryptocurrencies.bitcoinpos.network.RestBitcoinHelper;
import gr.cryptocurrencies.bitcoinpos.network.Utilities;
import gr.cryptocurrencies.bitcoinpos.utilities.AddressValidator;

import gr.cryptocurrencies.bitcoinpos.utilities.CurrencyUtils;

public class SettingsActivity extends AppCompatActivity {

    private Toolbar mToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // get caller params
        Intent caller = getIntent();
        boolean showAddressInvalidMessage = caller.getBooleanExtra(AddressValidator.showAddressInvalidMessage, false);//BitcoinUtils.showAddressInvalidMessage was used before

        // Display toolbar and back arrow -- title and parent is found in activity tag in manifest
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content_frame, new SettingsFragment())
                .commit();

        if(showAddressInvalidMessage) {
            Toast.makeText(getBaseContext(), R.string.invalid_address_message, Toast.LENGTH_SHORT).show();
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

    //when address was changed and back was pressed from phone's button, it was not used instantly in MainActivity and fragments,
    // added on back pressed to work as in 'onOptionsItemSelected' when back is pressed from toolbar
    @Override
    public void onBackPressed()
    {
        NavUtils.navigateUpFromSameTask(this);

        super.onBackPressed();
    }



    public static class SettingsFragment extends PreferenceFragmentCompat  implements SharedPreferences.OnSharedPreferenceChangeListener {

        SharedPreferences mSharedPreferences;
        Fragment currentFragment;
        boolean testnetStatus=false;
        int cameraScanCrypto = 0;
        String mSelectedCryptocurrency;

        @Override
        public void onCreatePreferences(Bundle bundle, String s) {
            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
            ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

            // Set preferences' values to summaries by reusing onSharedPreferenceChanged
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            onSharedPreferenceChanged(mSharedPreferences, getString(R.string.merchant_name_key));
            onSharedPreferenceChanged(mSharedPreferences, getString(R.string.btc_payment_address_key));
            onSharedPreferenceChanged(mSharedPreferences, getString(R.string.bch_payment_address_key));
            onSharedPreferenceChanged(mSharedPreferences, getString(R.string.ltc_payment_address_key));
            onSharedPreferenceChanged(mSharedPreferences, getString(R.string.local_currency_key));
            onSharedPreferenceChanged(mSharedPreferences, getString(R.string.accept_testnet_key));

            testnetStatus = mSharedPreferences.getBoolean(getString(R.string.accept_testnet_key), false);

            if(testnetStatus){
                onSharedPreferenceChanged(mSharedPreferences, getString(R.string.btc_test_payment_address_key));
            }
            else {
                PreferenceScreen screen = getPreferenceScreen();
                Preference pref = getPreferenceManager().findPreference(getString(R.string.btc_test_payment_address_key));
                if(pref!=null){
                screen.removePreference(pref);
                }
            }

        }

        @Override
        public void onResume() {
            super.onResume();
            //unregister the preferenceChange listener
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
            testnetStatus = mSharedPreferences.getBoolean(getString(R.string.accept_testnet_key),false);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference(key);
            PreferenceScreen screen = getPreferenceScreen();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(key, ""));
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                }
            } else if (preference instanceof EditTextPreference) {
                preference.setSummary(sharedPreferences.getString(key, ""));
            } else if (preference instanceof CheckBoxPreference) {
                //checkbox changed
                testnetStatus = sharedPreferences.getBoolean(key, false);
                if(testnetStatus){
                    Preference pref = findPreference(getString(R.string.btc_test_payment_address_key));
                    if(pref==null){
                        Preference prefTest = new Preference(getActivity());
                        prefTest.setKey(getString(R.string.btc_test_payment_address_key));
                        prefTest.setTitle(getString(R.string.payment_address_btc_test));
                        prefTest.setSummary(mSharedPreferences.getString(getString(R.string.btc_test_payment_address_key),""));
                        prefTest.setLayoutResource(R.layout.custom_preference_layout);
                        screen.addPreference(prefTest);
                    }
                }
                else {
                    Preference pref = findPreference(getString(R.string.btc_test_payment_address_key));
                    if(pref!=null) {
                        screen.removePreference(pref);

                    }
                }
            }
            else {
                if(preference!=null) {
                    preference.setSummary(sharedPreferences.getString(key, ""));
                }
            }
        }

        @Override
        public boolean onPreferenceTreeClick(final Preference preference) {
            if(preference.getKey().equals(getString(R.string.btc_payment_address_key))) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.add_payment_address_title);
                builder.setMessage(getString(R.string.add_payment_address_message));
                currentFragment = this;//setTargetFragment(this, 0);

                cameraScanCrypto = 0; //BTC
                // SCAN button
                mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BTC);
                builder.setNegativeButton(getString(R.string.scan), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(currentFragment);//getTargetFragment());
                        integrator.setBeepEnabled(true);
                        integrator.setOrientationLocked(true);
                        integrator.setCaptureActivity(ScanQrCodeActivity.class);
                        integrator.setPrompt(getString(R.string.scan_qr_code_prompt));
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
//                        integrator.addExtra("SCAN_WIDTH", 200);
//                        integrator.addExtra("SCAN_HEIGHT", 200);
                        integrator.initiateScan();
                    }
                });

                // PASTE button
                builder.setPositiveButton(getString(R.string.paste), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        requestPastePaymentAddress(mSelectedCryptocurrency);
                    }
                });

                builder.show();

            }
            else if(preference.getKey().equals(getString(R.string.btc_test_payment_address_key))) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.add_payment_address_title);
                builder.setMessage(getString(R.string.add_payment_address_message));
                currentFragment = this;//setTargetFragment(this, 0);

                cameraScanCrypto = 3; //BTC TEST
                // SCAN button
                mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BTCTEST);
                builder.setNegativeButton(getString(R.string.scan), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(currentFragment);//getTargetFragment());
                        integrator.setBeepEnabled(true);
                        integrator.setOrientationLocked(true);
                        integrator.setCaptureActivity(ScanQrCodeActivity.class);
                        integrator.setPrompt(getString(R.string.scan_qr_code_prompt));
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
//                        integrator.addExtra("SCAN_WIDTH", 200);
//                        integrator.addExtra("SCAN_HEIGHT", 200);
                        integrator.initiateScan();
                    }
                });

                // PASTE button
                builder.setPositiveButton(getString(R.string.paste), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPastePaymentAddress(mSelectedCryptocurrency);
                    }
                });

                builder.show();

            }
            else if(preference.getKey().equals(getString(R.string.bch_payment_address_key))) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.add_payment_address_title);
                builder.setMessage(getString(R.string.add_payment_address_message));
                currentFragment = this;//setTargetFragment(this, 0);

                cameraScanCrypto = 1; //BCH
                mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BCH);
                // SCAN button
                builder.setNegativeButton(getString(R.string.scan), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(currentFragment);//getTargetFragment());
                        integrator.setBeepEnabled(true);
                        integrator.setOrientationLocked(true);
                        integrator.setCaptureActivity(ScanQrCodeActivity.class);
                        integrator.setPrompt(getString(R.string.scan_qr_code_prompt));
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
//                        integrator.addExtra("SCAN_WIDTH", 200);
//                        integrator.addExtra("SCAN_HEIGHT", 200);
                        integrator.initiateScan();
                    }
                });

                // PASTE button
                builder.setPositiveButton(getString(R.string.paste), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPastePaymentAddress(mSelectedCryptocurrency);
                    }
                });

                builder.show();

            }
            else if(preference.getKey().equals(getString(R.string.ltc_payment_address_key))) {

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.add_payment_address_title);
                builder.setMessage(getString(R.string.add_payment_address_message));
                currentFragment = this;//setTargetFragment(this, 0);

                cameraScanCrypto = 2; //LTC
                mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.LTC);
                // SCAN button
                builder.setNegativeButton(getString(R.string.scan), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(currentFragment);//getTargetFragment());
                        integrator.setBeepEnabled(true);
                        integrator.setOrientationLocked(true);
                        integrator.setCaptureActivity(ScanQrCodeActivity.class);
                        integrator.setPrompt(getString(R.string.scan_qr_code_prompt));
                        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
//                        integrator.addExtra("SCAN_WIDTH", 200);
//                        integrator.addExtra("SCAN_HEIGHT", 200);
                        integrator.initiateScan();
                    }
                });

                // PASTE button
                builder.setPositiveButton(getString(R.string.paste), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        requestPastePaymentAddress(mSelectedCryptocurrency);
                    }
                });

                builder.show();

            }
            else if(preference.getKey().equals(getString(R.string.accept_testnet_key))){
                if(!testnetStatus){
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putInt(getString(R.string.navigation_crypto), 0); // default bitcoin radio button checked
                editor.apply();
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


        public void requestPastePaymentAddress(final String cryptocurrency) {
            AlertDialog.Builder getAddressDialog = new AlertDialog.Builder(SettingsFragment.this.getContext());
            getAddressDialog.setTitle(getString(R.string.payment_address));
            getAddressDialog.setMessage(getString(R.string.paste_address_message));

            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_NORMAL);
            getAddressDialog.setView(input);

            getAddressDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // get value and validate
                    String address = String.valueOf(input.getText().toString().replaceAll("\\s","")); // remove whitespaces
                    //boolean isAddressValid = BitcoinUtils.validateAddress(address);
                    mSelectedCryptocurrency = cryptocurrency;
                    boolean isAddressValid = false;
                    if(!mSelectedCryptocurrency.equals(String.valueOf(CurrencyUtils.CurrencyType.BCH))) {
                        isAddressValid = AddressValidator.validate(address, mSelectedCryptocurrency); //-->>.validate(address, type of currency)

                        if(isAddressValid) {
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            if(mSelectedCryptocurrency.equals(String.valueOf(CurrencyUtils.CurrencyType.BTC))){
                                editor.putString(getString(R.string.btc_payment_address_key), address);
                                //findPreference(getString(R.string.btc_payment_address_key)).setSummary(input.getText().toString()); //not needed
                            }
//                        else if(mSelectedCryptocurrency.equals(String.valueOf(CurrencyUtils.CurrencyType.BCH))){
//                            editor.putString(getString(R.string.bch_payment_address_key), input.getText().toString());
//                        }
                            else if(mSelectedCryptocurrency.equals(String.valueOf(CurrencyUtils.CurrencyType.LTC))){
                                editor.putString(getString(R.string.ltc_payment_address_key), address);
                            }
                            else if(mSelectedCryptocurrency.equals(String.valueOf(CurrencyUtils.CurrencyType.BTCTEST))){
                                editor.putString(getString(R.string.btc_test_payment_address_key), address);
                            }
                            //editor.putString(getString(R.string.payment_address_key), input.getText().toString());
                            editor.apply();
                            //findPreference(getString(R.string.payment_address_key)).setSummary(input.getText().toString());
                        } else {
                            Toast.makeText(getContext(), R.string.invalid_address_message, Toast.LENGTH_LONG).show();
                        }

                    }
                    else{
                        if(Utilities.isNetworkConnectionAvailable(getActivity())) {
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            RestBitcoinHelper.isAddressValidViaRest(address, editor, getString(R.string.bch_payment_address_key));
                        }
                        else {
                            Toast.makeText(getActivity(), R.string.network_connection_not_available_message, Toast.LENGTH_SHORT).show();
                        }
                    }
                                                }
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

        // Capture the scanner results
        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if(result != null) {
                boolean isAddressValid = false;

                if(result.getContents() != null) {
                    // get detected value and validate
                    String addressString = result.getContents();
                    String addressNoWhiteSpaces = String.valueOf(addressString.replaceAll("\\s",""));
                    String address;
                    Log.d("SCANNER ADDRESS", addressString);

                    // if BIP 21 is used get the address
                    if(AddressValidator.isAddressUsingBIP21(addressNoWhiteSpaces)) {
                        address = AddressValidator.getAddressFromBip21String(addressNoWhiteSpaces);
                    } else {
                        address = String.valueOf(addressNoWhiteSpaces);
                    }
                    //boolean isTestnet=false;
                    switch (cameraScanCrypto){
                        case 0:
                            mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BTC);
                            break;
                        case 1:
                            mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BCH);
                            break;
                        case 2:
                            mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.LTC);
                            break;
                        case 3:
                            mSelectedCryptocurrency = String.valueOf(CurrencyUtils.CurrencyType.BTCTEST);
                            break;
                    }
                    //if(cameraScanCrypto==3){isTestnet=true;}

                    if(!mSelectedCryptocurrency.equals(String.valueOf(CurrencyUtils.CurrencyType.BCH))) {
                        //isAddressValid = BitcoinUtils.validateAddress(address);//previous check
                        isAddressValid = AddressValidator.validate(address, mSelectedCryptocurrency);
                        //Toast.makeText(getActivity(), String.valueOf(isAddressValid), Toast.LENGTH_SHORT).show();

                        if (isAddressValid) {
                            // set appropriate setting/preference
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            if (cameraScanCrypto == 0) {
                                editor.putString(getString(R.string.btc_payment_address_key), address);
                                findPreference(getString(R.string.btc_payment_address_key)).setSummary(address);
                            }
//                            else if (cameraScanCrypto == 1) { // BCH
//                                editor.putString(getString(R.string.bch_payment_address_key), address);
//                                //findPreference(getString(R.string.bch_payment_address_key)).setSummary(address);
//                            }
                            else if (cameraScanCrypto == 2) {
                                editor.putString(getString(R.string.ltc_payment_address_key), address);
                                findPreference(getString(R.string.ltc_payment_address_key)).setSummary(address);
                            } else if (cameraScanCrypto == 3) {
                                editor.putString(getString(R.string.btc_test_payment_address_key), address);
                                findPreference(getString(R.string.btc_test_payment_address_key)).setSummary(address);
                            }
                            editor.apply();
                        }
                        else {
                            // go to settings activity
                            Intent goToSettings = new Intent(getContext(), SettingsActivity.class);
                            goToSettings.putExtra(AddressValidator.showAddressInvalidMessage, !isAddressValid); //BitcoinUtils.showAddressInvalidMessage was used before
                            goToSettings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(goToSettings);
                        }
                    }
                    else{
                        if(Utilities.isNetworkConnectionAvailable(getActivity())) {
                            SharedPreferences.Editor editor = mSharedPreferences.edit();
                            RestBitcoinHelper.isAddressValidViaRest(address, editor, getString(R.string.bch_payment_address_key));
                        }
                        else {
                            Toast.makeText(getActivity(), R.string.network_connection_not_available_message, Toast.LENGTH_SHORT).show();
                        }

                        //not needed to put Extra, address will be resistered by RestBitcoinHelper
                        Intent goToSettings = new Intent(getContext(), SettingsActivity.class);
                        //goToSettings.putExtra(AddressValidator.showAddressInvalidMessage, !isAddressValid); //BitcoinUtils.showAddressInvalidMessage was used before
                        goToSettings.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(goToSettings);

                    }

                }


            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }




    }




}
