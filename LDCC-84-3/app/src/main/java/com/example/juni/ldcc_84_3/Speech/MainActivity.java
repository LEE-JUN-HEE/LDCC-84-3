/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.juni.ldcc_84_3.Speech;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.juni.ldcc_84_3.Nl.AccessTokenLoader;
import com.example.juni.ldcc_84_3.Nl.ApiFragment;
import com.example.juni.ldcc_84_3.Nl.model.EntityInfo;
import com.example.juni.ldcc_84_3.Nl.model.SentimentInfo;
import com.example.juni.ldcc_84_3.Nl.model.TokenInfo;
import com.example.juni.ldcc_84_3.R;
import com.example.juni.ldcc_84_3.ResultActivty;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.language.v1.CloudNaturalLanguage;
import com.google.api.services.language.v1.CloudNaturalLanguageRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class MainActivity extends AppCompatActivity implements MessageDialogFragment.Listener {
    public static MainActivity Instance;
    private static final String FRAGMENT_MESSAGE_DIALOG = "message_dialog";
    private static final String STATE_RESULTS = "results";
    private static final int REQUEST_RECORD_AUDIO_PERMISSION = 1;
    private SpeechService mSpeechService;
    private VoiceRecorder mVoiceRecorder;
    private int curSelectSevenId = 0;
    private CustomAnimationDialog customAnimationDialog; // Loading

    private final VoiceRecorder.Callback mVoiceCallback = new VoiceRecorder.Callback() {

        @Override
        public void onVoiceStart() {
            Log.e("Recorder", "레코더 스타트");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mStatus.setText("Now Start Say");
                }
            });

            if (mSpeechService != null) {
                mSpeechService.startRecognizing(mVoiceRecorder.getSampleRate());
            }
        }

        @Override
        public void onVoice(byte[] data, int size) {
            if (mSpeechService != null) {
                mSpeechService.recognize(data, size);
            }
        }

        @Override
        public void onVoiceEnd() {
            Log.e("Recorder", "레코더 끝!");
            if (mSpeechService != null) {
                mSpeechService.finishRecognizing();
                mVoiceRecorder.stop();
            }
        }
    };

    // View references
    private TextView mStatus;
    private TextView mText;
    private static ResultAdapter mAdapter;
    private static Context mContext;
    final FragmentManager fm = getSupportFragmentManager();

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            mSpeechService = SpeechService.from(binder);
            mSpeechService.addListener(mSpeechServiceListener);
            mStatus.setVisibility(View.VISIBLE);
            MainActivity.Instance.CheckAllReadyAndStart();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mSpeechService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Instance = this;
        setContentView(R.layout.activity_main_speech);
        curSelectSevenId = (int) getIntent().getLongExtra("sevenid", 0);

        customAnimationDialog = new CustomAnimationDialog(MainActivity.this); // Loading

        mStatus = (TextView) findViewById(R.id.status);
        mStatus.setText("Waiting...");
        mText = (TextView) findViewById(R.id.text);
        mContext = this.getApplicationContext();

        final ArrayList<String> results = savedInstanceState == null ? null :
                savedInstanceState.getStringArrayList(STATE_RESULTS);
        mAdapter = new ResultAdapter(results);

        if (getApiFragment() == null) {
            fm.beginTransaction().add(new ApiFragment(), FRAGMENT_API).commit();
        }
        prepareApi();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Prepare Cloud Speech API
        bindService(new Intent(this, SpeechService.class), mServiceConnection, BIND_AUTO_CREATE);

        // Start listening to voices
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
//                == PackageManager.PERMISSION_GRANTED) {
//            startVoiceRecorder();
//        } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
//                Manifest.permission.RECORD_AUDIO)) {
//            showPermissionMessageDialog();
//        } else {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
//                    REQUEST_RECORD_AUDIO_PERMISSION);
//        }
    }

    @Override
    protected void onStop() {
        // Stop listening to voice
        stopVoiceRecorder();

        // Stop Cloud Speech API
        if (mSpeechService != null)
            mSpeechService.removeListener(mSpeechServiceListener);

        unbindService(mServiceConnection);
        mSpeechService = null;

        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAdapter != null) {
            outState.putStringArrayList(STATE_RESULTS, mAdapter.getResults());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO_PERMISSION) {
            if (permissions.length == 1 && grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else {
                showPermissionMessageDialog();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_file:
                mSpeechService.recognizeInputStream(getResources().openRawResource(R.raw.audio));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void startVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
        }
        mVoiceRecorder = new VoiceRecorder(mVoiceCallback);
        mVoiceRecorder.start();
    }

    private void stopVoiceRecorder() {
        if (mVoiceRecorder != null) {
            mVoiceRecorder.stop();
            mVoiceRecorder = null;
        }
    }

    private void showPermissionMessageDialog() {
        MessageDialogFragment
                .newInstance(getString(R.string.permission_message))
                .show(getSupportFragmentManager(), FRAGMENT_MESSAGE_DIALOG);
    }

    @Override
    public void onMessageDialogDismissed() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                REQUEST_RECORD_AUDIO_PERMISSION);
    }

    private final SpeechService.Listener mSpeechServiceListener =
            new SpeechService.Listener() {
                @Override
                public void onSpeechRecognized(final String text, final boolean isFinal) {
                    if (isFinal) {
                        mVoiceRecorder.dismiss();
                    }
                    if (mText != null && !TextUtils.isEmpty(text)) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFinal) {
                                    mText.setText(null);
                                    Current = text;
                                    mStatus.setText("Processing....");

                                    findViewById(R.id.app_bar).setVisibility(View.INVISIBLE);
                                    startAnalyze(text);
                                    customAnimationDialog.show(); // Loading
                                } else {
                                    mText.setText(text);
                                    customAnimationDialog.dismiss(); // Unloading
                                }
                            }
                        });
                    }
                }
            };

    private static class ViewHolder extends RecyclerView.ViewHolder {

        TextView text;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.activity_main, parent, false));
            text = (TextView) itemView.findViewById(R.id.text);
        }
    }

    public void CheckAllReadyAndStart() {
        if (mSpeechService != null && mSpeechService.isReady == true) {
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.RECORD_AUDIO)
                    == PackageManager.PERMISSION_GRANTED) {
                startVoiceRecorder();
            } else if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.RECORD_AUDIO)) {
                showPermissionMessageDialog();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO},
                        REQUEST_RECORD_AUDIO_PERMISSION);
            }
        }
    }

    private static class ResultAdapter extends RecyclerView.Adapter<ViewHolder> {

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
            holder.text.setText(mResults.get(position));
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

////////////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////
    //NL

    private static final String FRAGMENT_API = "api";
    private static final int LOADER_ACCESS_TOKEN = 1;
    private static String Current = "";

    private GoogleCredential mCredential;

    private CloudNaturalLanguage mApi = new CloudNaturalLanguage.Builder(
            new NetHttpTransport(),
            JacksonFactory.getDefaultInstance(),
            new HttpRequestInitializer() {
                @Override
                public void initialize(HttpRequest request) throws IOException {
                    mCredential.initialize(request);
                }
            }).build();

    private final BlockingQueue<CloudNaturalLanguageRequest<? extends GenericJson>> mRequests
            = new ArrayBlockingQueue<>(3);

    public static ApiFragment.Callback mCallback = new ApiFragment.Callback() {
        boolean isComplete = false;
        String entityStr = null;
        String tokenStr = null;
        EntityInfo[] EI = null;
        TokenInfo[] TI = null;

        public String getEntitiesString(EntityInfo[] entities) {
            String Entitystr = "";
            for (EntityInfo i : entities) {
                Entitystr += "[Entity] name : " + i.name + "/ type : " + i.type + "\n";// + "/ salience : " + i.salience + "\n";
            }
            return Entitystr;
        }

        public String getTokensString(TokenInfo[] tokens) {
            String Tokenstr = "";

            for (TokenInfo i : tokens) {
                Tokenstr += "[Token] text : " + i.text + "/ label : " + i.label + "\n";//"/ lemma : " + i.lemma + "\n";
            }
            return Tokenstr;
        }

        @Override
        public void onEntitiesReady(EntityInfo[] entities) {
            entityStr = getEntitiesString(entities);
            EI = entities;
            CheckComplete();
        }

        @Override
        public void onSentimentReady(SentimentInfo sentiment) {

        }

        @Override
        public void onSyntaxReady(TokenInfo[] tokens) {
            tokenStr = getTokensString(tokens);
            TI = tokens;
            CheckComplete();
        }

        void Clear() {
            isComplete = false;
            EI = null;
            TI = null;
        }

        void CheckComplete() {
            if (EI != null && TI != null) {
                ArrayList<String> arrEINames = new ArrayList<>();
                ArrayList<String> arrEITypes = new ArrayList<>();
                for (EntityInfo E : EI) {
                    arrEINames.add(E.name);
                    arrEITypes.add(E.type);
                }

                Current = Current + "\n" + entityStr + tokenStr;

                mAdapter.addResult(Current);
                Intent i = new Intent(mContext, ResultActivty.class);
                i.putExtra("res", Current);
                i.putExtra("entitiynames", arrEINames);
                i.putExtra("entitiytypes", arrEITypes);
                i.putExtra("sevenid", MainActivity.Instance.curSelectSevenId);
                i.addFlags(FLAG_ACTIVITY_NEW_TASK);
                MainActivity.Instance.customAnimationDialog.dismiss();
                mContext.startActivity(i);

                Clear();
            }
        }
    };

    private void prepareApi() {
        // Initiate token refresh
        getSupportLoaderManager().initLoader(LOADER_ACCESS_TOKEN, null,
                new LoaderManager.LoaderCallbacks<String>() {
                    @Override
                    public Loader<String> onCreateLoader(int id, Bundle args) {
                        return new AccessTokenLoader(com.example.juni.ldcc_84_3.Speech.MainActivity.this);
                    }

                    @Override
                    public void onLoadFinished(Loader<String> loader, String token) {
                        getApiFragment().setAccessToken(token);
                    }

                    @Override
                    public void onLoaderReset(Loader<String> loader) {
                    }
                });
    }

    private void startAnalyze(String text) {
        // Call the API
        Log.e("NL", "분석시작");
        if (getApiFragment() != null) {
            getApiFragment().analyzeEntities(text); // 목표(제품) 분석
            //getApiFragment().analyzeSentiment(text); 감정(어감)분석은 추후 기대효과로
            getApiFragment().analyzeSyntax(text); // 행위(찾기, 추천 등) 분석
        }
    }

    private ApiFragment getApiFragment() {
        return (ApiFragment) getSupportFragmentManager().findFragmentByTag(FRAGMENT_API);
    }
}
