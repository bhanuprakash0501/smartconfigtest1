package com.bhanuprakash0501.smartconfigtest1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.espressif.iot.esptouch.EsptouchTask;
import com.espressif.iot.esptouch.IEsptouchResult;
import com.espressif.iot.esptouch.util.ByteUtil;
import com.espressif.iot.esptouch.util.TouchNetUtil;

import java.lang.ref.WeakReference;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 0x01;
    String ssid;
    String bssid;
    String password;
    String wifi_details[];
    private MActivityAsyncTask4 mTask;
    Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            requestPermissions(permissions, REQUEST_PERMISSION);
        }
        button = findViewById(R.id.button);

        ssid = "Bhanu";
        password = "prakash0501";
        Log.i("SSID", ssid);
        wifi_details = getWifiDetails(this);
        ssid = wifi_details[0];
        bssid = wifi_details[1];
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                buttonMethod();
            }
        });
    }



    public void buttonMethod() {
            Log.i("SSID", ssid);
            Log.i("BSSID", bssid);
            mTask = new MActivityAsyncTask4(this);
            byte[] bytesbssid = TouchNetUtil.parseBssid2bytes(bssid);
            byte[] bytesssid = ByteUtil.getBytesByString(ssid);
            byte[] bytespassword = ByteUtil.getBytesByString(password);
            mTask.execute(bytesssid, bytesbssid, bytespassword);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getWifiDetails(this);
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Warning!!")
                        .setMessage("Android M or higher version. Unable to get Location permission!!")
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.this.finish();
                            }
                        })
                        .show();
            }

            return;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    public String[] getWifiDetails(Context context) {
        String ssid = "test_none";
        String bssid = "test_none";
        String wifi_details[];
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        //Log.i("Wifi Get detailed state", String.valueOf(WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState() )));
        if (WifiInfo.getDetailedStateOf(wifiInfo.getSupplicantState()) == NetworkInfo.DetailedState.CONNECTED) {
            Log.i("wifi state good", "Good");
            bssid = wifiInfo.getBSSID();
            ssid = wifiInfo.getSSID();
        }
        ssid = wifiInfo.getSSID();
        bssid = wifiInfo.getBSSID();
        wifi_details = new String[] {ssid, bssid};
        //Log.i("SSID", ssid);
        //Log.i("BSSID", bssid);
        return wifi_details;
    }



    private static class MActivityAsyncTask4 extends AsyncTask<byte[], IEsptouchResult, List<IEsptouchResult>> {

        private WeakReference<MainActivity> mActivity;
        private final Object mLock = new Object();
        private ProgressDialog mProgressDialog;
        byte[] apSsid = {}; // Set AP's SSID
        byte[] apBssid = {}; // Set AP's BSSID
        byte[] apPassword = {}; // Set AP's password
        EsptouchTask task;

        MActivityAsyncTask4(MainActivity activity) {
            mActivity = new WeakReference<>(activity);
        }


        @Override
        protected void onProgressUpdate(IEsptouchResult... values) {
            Context context = mActivity.get();
            if (context != null) {
                IEsptouchResult result = values[0];
                Log.i("EspTouchResult", "EspTouchResult: " + result);
                String text = result.getBssid() + " is connected to the wifi";
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        protected List<IEsptouchResult> doInBackground(byte[]... bytes) {
            Log.i("BackgroundTask", "yes");
            int taskResultCount= 1;
            MainActivity activity = mActivity.get();
            synchronized (mLock) {
                Context context = activity.getApplicationContext();
                apSsid = bytes[0];
                apBssid = bytes[1];
                apPassword = bytes[2];
                task = new EsptouchTask(apSsid, apBssid, apPassword, context);
                task.setPackageBroadcast(true);
                return task.executeForResults(taskResultCount);
            }
        }
    }

}


