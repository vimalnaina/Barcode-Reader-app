package com.example.rajkotjamatapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/*
 * Author: Vimal Naina
 * */

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CAMERA_PERMISSION = 100;
    private SharedPreferences permissionStatus;

    // Progress Dialog
    private ProgressDialog pDialog;
    Button BtnScan, BtnSubmit;
    public static TextView ResultText;
    JSONParser jsonParser = new JSONParser();
    JSONObject json;

    private static String file_url = "http://main.rajkotjamaat.com/daily_thali_scan.php";

    private static final String TAG_SUCCESS = "code";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BtnScan = (Button) findViewById(R.id.btnScan);
        BtnSubmit = (Button) findViewById(R.id.btnSubmit);
        ResultText = (TextView) findViewById(R.id.resultText);

        permissionStatus = getSharedPreferences("permissionStatus",MODE_PRIVATE);

        BtnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                permissionStatus.getBoolean(Manifest.permission.CAMERA,false);
                if(permissionStatus.getBoolean(Manifest.permission.CAMERA,false)) {
                    startActivity(new Intent(getApplicationContext(), BarcodeScannerActivity.class));
                }else{
                    Toast.makeText(MainActivity.this,"Give Camera Permission From Settings!",Toast.LENGTH_LONG).show();
                }
                SharedPreferences.Editor editor = permissionStatus.edit();
                editor.putBoolean(Manifest.permission.CAMERA,true);
                editor.commit();
            }
        });

        BtnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new MyTask().execute();
            }
        });
    }

    /**
     * Background Async Task to Create new product
     * */
    class MyTask extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Uploading...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        @SuppressLint("LongLogTag")
        protected String doInBackground(String... args) {
            String tokenno = ResultText.getText().toString();
            try {
                // Building Parameters
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("tokenno", tokenno));

                // getting JSON Object
                // Note that create product url accepts POST method
                json = jsonParser.makeHttpRequest(file_url,
                        "POST", params);

                // check log cat fro response
                Log.d("Create Response", json.toString());
            }
            catch (Exception e){
                Log.d("error in doInBackground: ", e.getMessage());
            }


            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * Show the output on tost
         * **/
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    Toast.makeText(MainActivity.this, "Token Added Successfully!", Toast.LENGTH_LONG).show();
                }
                else if(success == 2){
                    Toast.makeText(MainActivity.this, "Token Already Available \n Failed To Insert Token!", Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(MainActivity.this, "Invalid Token. \n Failed To Insert Token!", Toast.LENGTH_LONG).show();
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}