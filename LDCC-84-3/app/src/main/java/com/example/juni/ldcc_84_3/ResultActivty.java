package com.example.juni.ldcc_84_3;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.juni.ldcc_84_3.Nl.model.EntityInfo;
import com.example.juni.ldcc_84_3.Speech.MainActivity;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ResultActivty extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_activty);
        Intent i = getIntent();
        String result = i.getStringExtra("res");
        ArrayList<String> EInames = i.getStringArrayListExtra("entitiynames");
        ArrayList<String> EItypes = i.getStringArrayListExtra("entitiynames");

        if(MainActivity.Instance != null) MainActivity.Instance.finish();

        //Entity로 서버 통신 쓰레드 오픈
        InsertData task = new InsertData();
        String product = "product";
        String pos = "100,100";
        String postParameters = "pos=" + pos + "want=" + product + "&method=find";
        task.execute(postParameters);

        TextView t = (TextView) findViewById(R.id.nlresult);
        t.setText("[Debug]" + result + "\nProcessing...");
    }

    class InsertData extends AsyncTask<String, Void, String> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(ResultActivty.this,
                    "Please Wait", null, true, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.e("Server", "완료");
//            mTextViewResult.setText(result);
//            Log.d(TAG, "POST response  - " + result);
        }


        @Override
        protected String doInBackground(String... params) {

            String postParameters = (String)params[0];

            //String serverURL = "http://13.124.31.50/testando.php"; //원래 insert.php로 되어있음.
            String serverURL = "http://13.124.31.50/testjson.php"; //원래 insert.php로 되어있음.
            //String postParameters = "name=" + name  + "&value=" + "LDCCandHIT";

            try {
                URL url = new URL(serverURL);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();


                httpURLConnection.setReadTimeout(5000);
                httpURLConnection.setConnectTimeout(5000);
                httpURLConnection.setRequestMethod("POST");
                //httpURLConnection.setRequestProperty("content-type", "application/json");
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();


                OutputStream outputStream = httpURLConnection.getOutputStream();
                outputStream.write(postParameters.getBytes("UTF-8"));
                outputStream.flush();
                outputStream.close();


                int responseStatusCode = httpURLConnection.getResponseCode();

                InputStream inputStream;
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {
                    inputStream = httpURLConnection.getInputStream();
                }
                else{
                    inputStream = httpURLConnection.getErrorStream();
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "UTF-8");
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                StringBuilder sb = new StringBuilder();
                String line = null;

                while((line = bufferedReader.readLine()) != null){
                    sb.append(line);
                }


                bufferedReader.close();


                return sb.toString();


            } catch (Exception e) {
                return new String("Error: " + e.getMessage());
            }

        }
    }
}
