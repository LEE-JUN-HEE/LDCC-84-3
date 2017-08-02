package com.example.juni.ldcc_84_3;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.VideoView;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    ArrayList<Integer> vName;
    VideoPlayer vPlayer;
    int stopPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btspeech = (Button)findViewById(R.id.gospeechbt);
        VideoView mVideoView2 = (VideoView)findViewById(R.id.videoView1);

        String[] list= getResources().getStringArray(R.array.seven_name);
        //세븐일레븐 스피너 셋
        //String[] list = {"가산 하이엔드", "LDCC", "가산디지털역", "가산우림", "가리봉중앙", "남구로역", "남구로행운", "마리오아울렛3관", "가산롯데IT캐슬", "가산푸르지오시티", "구로본점", "가산월드", "가산STXV타워", "마리오아울렛1관"};
        final Spinner spinner = (Spinner)findViewById(R.id.spinner);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.support_simple_spinner_dropdown_item,
                list);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);
        spinner.getSelectedItemId();

        //광고 영상 이름, 원래는 광고서버가 있어야 하나 구현 편의상 로컬 등록
        vName = new ArrayList<>();
        vName.add(R.raw.sero);
        //vName.add(R.raw.parken);
        //vName.add(R.raw.signal);
        //vName.add(R.raw.wonder);

        //광고 끝 콜백 등록x
        vPlayer = new VideoPlayer(mVideoView2, vName);
        mVideoView2.setOnCompletionListener(vPlayer);

        btspeech.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                Intent intent=new Intent(MainActivity.this, com.example.juni.ldcc_84_3.Speech.MainActivity.class);
                intent.putExtra("sevenid", spinner.getSelectedItemId() + 1);
                startActivity(intent);
            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        VideoView mVideoView2 = (VideoView)findViewById(R.id.videoView1);
        mVideoView2.seekTo(stopPosition);
        mVideoView2.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        VideoView mVideoView2 = (VideoView)findViewById(R.id.videoView1);
        stopPosition = mVideoView2.getCurrentPosition();
        mVideoView2.pause();
    }

    class VideoPlayer implements MediaPlayer.OnCompletionListener{

        VideoView mVideoView;
        ArrayList<Integer> arr;
        MediaPlayer mp;
        int curIdx;

        @Override
        public void onCompletion(MediaPlayer mp) {
            this.mp = mp;
            setAdPlay(mVideoView, new Random().nextInt(arr.size()));
        }

        public VideoPlayer(){}

        public VideoPlayer(VideoView mVideoView, ArrayList<Integer> arr){
            this.mVideoView = mVideoView;
            this.arr = arr;
            setAdPlay(mVideoView, new Random().nextInt(arr.size()));
        }

        void setAdPlay(VideoView vv, int _id){
            if(curIdx == _id)
                _id = (_id + 1) % arr.size();

            if(vv != null){
                String uriPath2 = "android.resource://com.example.juni.ldcc_84_3/"+ arr.get(_id);
                Uri uri2 = Uri.parse(uriPath2);
                curIdx = _id;
                vv.setVideoURI(uri2);
                vv.requestFocus();
                vv.start();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
