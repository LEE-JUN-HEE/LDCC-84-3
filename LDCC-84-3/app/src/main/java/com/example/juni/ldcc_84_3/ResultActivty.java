package com.example.juni.ldcc_84_3;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.juni.ldcc_84_3.Nl.model.EntityInfo;
import com.example.juni.ldcc_84_3.Speech.MainActivity;
import com.fasterxml.jackson.core.JsonGenerator;
import com.google.api.client.json.JsonObjectParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class ResultActivty extends AppCompatActivity {
    RecyclerView mRV;
    ArrayList<String> EInames;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result_activty);
        Intent intent = getIntent();

        String result = intent.getStringExtra("res");
        EInames = intent.getStringArrayListExtra("entitiynames");
        ArrayList<String> EItypes = intent.getStringArrayListExtra("entitiytypes");
        int sevenid = intent.getIntExtra("sevenid", 0);

        if(MainActivity.Instance != null)
            MainActivity.Instance.finish();

        //Entity로 서버 통신 쓰레드 오픈
        InsertData task = new InsertData();
        String postParameters = String.format("seven_id=%d", sevenid);
        String names = "&name=";
        String values = "&value=";
        for(int i = 0; i < EInames.size(); i++){
            names += EInames.get(i);
            values += EItypes.get(i);
            if(i != EInames.size() - 1){
                names += "|";
                values += "|";
            }
        }

        postParameters = String.format("%s%s%s",postParameters, names, values);
        Log.e("post", postParameters);
        task.execute(postParameters);

        TextView t = (TextView) findViewById(R.id.resulttext);
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

        //successtype : int, sevenid : int, pointx : double, pointy : double, count : int, recommend : string
        String data = "{'successtype':'1', 'sevenid' : '0','pointx' : '',, 'count' : '1', recommend : '0'}";
        String testJSONString = "{[" + data + "," + data + "," + data + "]}";
        @Override
        protected void onPostExecute(String result) {
            //result가 JSONArray으로 올것이다.
            //String data = "{\"successtype\":\"1\", \"sevenid\" : \"0\", \"count\" : \"1\", \"recommend\" : \"0\"}";
            //String testJSONString = "[" + data + "," + data + "," + data + "]";
            //Log.e("hh", result);
            try {
                JSONArray ar = new JSONArray(testJSONString);
                for(int i =0; i < ar.length(); i++){
                    JSONObject jo = ar.getJSONObject(i);
                    int id = 0;
                    switch(i){
                        case 0:
                            id = R.id.item1;
                            break;
                        case 1:
                            id = R.id.item2;
                            break;
                        case 2:
                            id = R.id.item3;
                            break;
                    }

                    View itemView = findViewById(id);
                    ImageView imageView = (ImageView) itemView.findViewById(R.id.item_image);
                    TextView title = (TextView) itemView.findViewById(R.id.item_title);
                    TextView seven = (TextView) itemView.findViewById(R.id.item_seven);
                    Button mapBt = (Button) itemView.findViewById(R.id.item_map);

                    //이름 Lowercase로 아이콘 가져오기
                    //아이콘 등록
                    title.setText(EInames.get(i));
                    switch (jo.getInt("successtype")){
                        case :
                    }
                    //전달받은 세븐일레븐 id 따와서 이름 찍기
                    //전달받은 세븐일레븐 좌표 저장해두기
                    //전달 세븐일레븐 없으면 버튼, 세븐 이름 지우기
                    Log.e(String.valueOf(i), String.valueOf(jo.getInt("successtype")));
                    Log.e(String.valueOf(i), String.valueOf(jo.getInt("sevenid")));
                    Log.e(String.valueOf(i), String.valueOf(jo.getInt("count")));
                    Log.e(String.valueOf(i), String.valueOf(jo.getInt("recommend")));
                }
            }
            catch (Exception e){
                Log.e("Error!!", e.getMessage());
            }
            super.onPostExecute(result);

            progressDialog.dismiss();
            Log.e("Server", "완료");
        }

        @Override
        protected String doInBackground(String... params) {
            String postParameters = (String)params[0];
            String serverURL = "http://13.124.31.50/project_findproduct.php";

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

    private static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imageView;
        TextView title;
        TextView seven;
        Button mapBt;


        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.final_result, parent, false));

            imageView = (ImageView) itemView.findViewById(R.id.item_image);
            title = (TextView) itemView.findViewById(R.id.item_title);
            seven = (TextView) itemView.findViewById(R.id.item_seven);
            mapBt = (Button) itemView.findViewById(R.id.item_map);
        }
    }

    class ResultAdapter extends RecyclerView.Adapter<ViewHolder>{
        private final ArrayList<String> mResults = new ArrayList<>();

        ResultAdapter(ArrayList<String> results) {
            if (results != null) {
                mResults.addAll(results);
            }
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            //holder.text.setText(mResults.get(position));
        }

        @Override
        public int getItemCount() {
            return mResults.size();
        }

        void addResult(String result) {
            mResults.add(0, result);
            notifyItemInserted(0);
        }

        public ArrayList<String> getResults() {
            return mResults;
        }

    }

}