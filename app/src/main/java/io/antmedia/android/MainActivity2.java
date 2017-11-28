package io.antmedia.android;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.microsoft.bing.speech.SpeechClientStatus;
import com.microsoft.cognitiveservices.speechrecognition.Confidence;
import com.microsoft.cognitiveservices.speechrecognition.DataRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionResult;
import com.microsoft.cognitiveservices.speechrecognition.RecognitionStatus;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionMode;
import com.microsoft.cognitiveservices.speechrecognition.SpeechRecognitionServiceFactory;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.microsoft.cognitiveservices.speechrecognition.DataRecognitionClient;
import com.microsoft.cognitiveservices.speechrecognition.ISpeechRecognitionServerEvents;
import com.microsoft.cognitiveservices.speechrecognition.MicrophoneRecognitionClient;

import io.antmedia.android.liveVideoBroadcaster.*;
import io.antmedia.android.liveVideoPlayer.LiveVideoPlayerActivity;

public class MainActivity2 extends AppCompatActivity implements ISpeechRecognitionServerEvents {

    public static final String RTMP_BASE_URL = "rtmp://192.168.58.121/vod/";

    private FirebaseAuth mAuth;
    String bgcode;
    String value;
    String flag;
    TextView tv;
    int m_waitSeconds = 0;
    DataRecognitionClient dataClient = null;
    MicrophoneRecognitionClient micClient = null;
    FinalResponseStatus isReceivedResponse = FinalResponseStatus.NotReceived;
    EditText _logText;
    RadioGroup _radioGroup;
    Button _buttonSelectMode;
    Button stsr;
    Button _startButton,track,partra;

    public enum FinalResponseStatus { NotReceived, OK, Timeout }

    class Task extends AsyncTask<String,Object,String> {

        @Override
        protected String doInBackground(String... strings) {
            while(true) {
                if (_startButton.isEnabled() == true) {

                }
            }
//            return null;
        }
    }
    public String getPrimaryKey() {
        return this.getString(io.antmedia.android.liveVideoBroadcaster.R.string.primaryKey);
    }

    /**
     * Gets the LUIS application identifier.
     * @return The LUIS application identifier.
     */
    private String getLuisAppId() {
        return this.getString(io.antmedia.android.liveVideoBroadcaster.R.string.luisAppID);
    }

    /**
     * Gets the LUIS subscription identifier.
     * @return The LUIS subscription identifier.
     */
    private String getLuisSubscriptionID() {
        return this.getString(io.antmedia.android.liveVideoBroadcaster.R.string.luisSubscriptionID);
    }

    /**
     * Gets a value indicating whether or not to use the microphone.
     * @return true if [use microphone]; otherwise, false.
     */
    private Boolean getUseMicrophone() {
        return true;
    }

    /**
     * Gets a value indicating whether LUIS results are desired.
     * @return true if LUIS results are to be returned otherwise, false.
     */
    private Boolean getWantIntent() {
        return false;
    }

    /**
     * Gets the current speech recognition mode.
     * @return The speech recognition mode.
     */
    private SpeechRecognitionMode getMode() {
        return SpeechRecognitionMode.ShortPhrase;
    }

    /**
     * Gets the default locale.
     * @return The default locale.
     */
    private String getDefaultLocale() {
        return "en-us";
    }

    /**
     * Gets the short wave file path.
     * @return The short wave file.
     */
    private String getShortWaveFile() {
        return "whatstheweatherlike.wav";
    }

    /**
     * Gets the long wave file path.
     * @return The long wave file.
     */
    private String getLongWaveFile() {
        return "batman.wav";
    }

    /**
     * Gets the Cognitive Service Authentication Uri.
     * @return The Cognitive Service Authentication Uri.  Empty if the global default is to be used.
     */
    private String getAuthenticationUri() {
        return this.getString(io.antmedia.android.liveVideoBroadcaster.R.string.authenticationUri);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.antmedia.android.liveVideoBroadcaster.R.layout.activity_main2);
        this._logText = (EditText) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.editText1);
        this._startButton = (Button) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.button1);
        this.track = (Button) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.track);
       this.partra = (Button) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.parent);
        this.stsr = (Button) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.stopsr);

        mAuth = FirebaseAuth.getInstance();

        if (getIntent() != null) {
            Intent in = getIntent();
            flag = in.getStringExtra("flag");

        }
        tv= (TextView) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.lvb);

        if(flag.equals("B")) {
            final String uid1 = mAuth.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference myRef = database.getReference();
            myRef.child("Users/Bag").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(uid1).child("mvalue").getValue().equals("NO")) {

                    } else if (dataSnapshot.child(uid1).child("mvalue").getValue().equals("YES")) {
                        track.performClick();


                    }

                }


                @Override
                public void onCancelled(DatabaseError firebaseError) {
       /*
        * You may print the error message.
               **/
                }

            });
            myRef.child("Users/Bag").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.child(uid1).child("value").getValue().equals("NO")) {

                    } else if (dataSnapshot.child(uid1).child("value").getValue().equals("YES")) {
                        stsr.performClick();

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                tv.performClick();
                                myRef.child("Users/Bag/" + uid1 + "/").child("value").setValue("NO");
                            }
                        },2000);

                    }

                }


                @Override
                public void onCancelled(DatabaseError firebaseError) {
       /*
        * You may print the error message.
               **/
                }

            });

            if (getString(io.antmedia.android.liveVideoBroadcaster.R.string.primaryKey).startsWith("Please")) {
                new AlertDialog.Builder(this)
                        .setTitle(getString(io.antmedia.android.liveVideoBroadcaster.R.string.add_subscription_key_tip_title))
                        .setMessage(getString(io.antmedia.android.liveVideoBroadcaster.R.string.add_subscription_key_tip))
                        .setCancelable(false)
                        .show();
            }

            // setup the buttons
            this._startButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    MainActivity2.this.StartButton_Click(arg0);
                }
            });

            this._startButton.performClick();
            this._startButton.setVisibility(View.INVISIBLE);
            this.partra.setVisibility(View.INVISIBLE);
            this.ShowMenu(true);
        }
        else if(flag.equals("P"))
        {
            this._startButton.setVisibility(View.INVISIBLE);
            this._logText.setVisibility(View.INVISIBLE);
            this.track.setVisibility(View.INVISIBLE);
            mAuth = FirebaseAuth.getInstance();
            final String user_id = mAuth.getCurrentUser().getUid();
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference();
            myRef.child("Users/Customers").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    bgcode=dataSnapshot.child(user_id).child("bag code").getValue().toString();
                }
                @Override
                public void onCancelled(DatabaseError firebaseError) {
       /*
        * You may print the error message.
               **/
                }

            });
        }



    }

    public void stsr(View v)
    {

        if (this.micClient != null) {
            this.micClient.endMicAndRecognition();
            try {
                this.micClient.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            this.micClient = null;
        }

        if (this.dataClient != null) {
            try {
                this.dataClient.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            this.dataClient = null;
        }

        this.ShowMenu(false);
        this._startButton.setEnabled(true);

    }


    public void openVideoBroadcaster(View view) {



        Intent i = new Intent(MainActivity2.this, LiveVideoBroadcasterActivity.class);
        startActivity(i);
        finish();


    }

    public void logout(View v)
    {
        FirebaseAuth.getInstance().signOut();
        Intent i = new Intent(this, FirstActivity.class);
        startActivity(i);
    }
    public void openVideoPlayer(View view) {

        Intent i = new Intent(MainActivity2.this, LiveVideoPlayerActivity.class);
        startActivity(i);

    }

    public void trac(View v)
    {
        Intent intent = new Intent(MainActivity2.this, DriverMapActivity.class);
        startActivity(intent);
        finish();
        return;
    }
    public void parenttrack(View v)
    {

        Intent intent = new Intent(MainActivity2.this, Track_Parent.class);
        startActivity(intent);


    }

    private void ShowMenu(boolean show) {
        if (show) {
//            this._radioGroup.setVisibility(View.VISIBLE);
            this._logText.setVisibility(View.INVISIBLE);
        } else {
//            this._radioGroup.setVisibility(View.INVISIBLE);
//            this._logText.setText("");
            this._logText.setVisibility(View.VISIBLE);
        }
    }
    /**
     * Handles the Click event of the _startButton control.
     */
    private void StartButton_Click(View arg0) {
        this._startButton.setEnabled(false);
//        this._radioGroup.setEnabled(false);

        this.m_waitSeconds = this.getMode() == SpeechRecognitionMode.ShortPhrase ? 1 : 1;

        this.ShowMenu(false);

        this.LogRecognitionStart();

        if (this.getUseMicrophone()) {
            if (this.micClient == null) {
                if (this.getWantIntent()) {
//                    this.WriteLine("--- Start microphone dictation with Intent detection ----");

                    this.micClient =
                            SpeechRecognitionServiceFactory.createMicrophoneClientWithIntent(
                                    this,
                                    this.getDefaultLocale(),
                                    this,
                                    this.getPrimaryKey(),
                                    this.getLuisAppId(),
                                    this.getLuisSubscriptionID());
                }
                else
                {
                    this.micClient = SpeechRecognitionServiceFactory.createMicrophoneClient(
                            this,
                            this.getMode(),
                            this.getDefaultLocale(),
                            this,
                            this.getPrimaryKey());
                }

                this.micClient.setAuthenticationUri(this.getAuthenticationUri());
            }

            this.micClient.startMicAndRecognition();
        }
        else
        {
            if (null == this.dataClient) {
                if (this.getWantIntent()) {
                    this.dataClient =
                            SpeechRecognitionServiceFactory.createDataClientWithIntent(
                                    this,
                                    this.getDefaultLocale(),
                                    this,
                                    this.getPrimaryKey(),
                                    this.getLuisAppId(),
                                    this.getLuisSubscriptionID());
                }
                else {
                    this.dataClient = SpeechRecognitionServiceFactory.createDataClient(
                            this,
                            this.getMode(),
                            this.getDefaultLocale(),
                            this,
                            this.getPrimaryKey());
                }

                this.dataClient.setAuthenticationUri(this.getAuthenticationUri());
            }

            this.SendAudioHelper((this.getMode() == SpeechRecognitionMode.ShortPhrase) ? this.getShortWaveFile() : this.getLongWaveFile());
        }

    }

    private void LogRecognitionStart() {
        String recoSource;
        if (this.getUseMicrophone()) {
            recoSource = "microphone";
        } else if (this.getMode() == SpeechRecognitionMode.ShortPhrase) {
            recoSource = "short wav file";
        } else {
            recoSource = "long wav file";
        }

//        this.WriteLine("\n--- Start speech recognition using " + recoSource + " with " + this.getMode() + " mode in " + this.getDefaultLocale() + " language ----\n\n");
    }

    private void SendAudioHelper(String filename) {
        RecognitionTask doDataReco = new RecognitionTask(this.dataClient, this.getMode(), filename);
        try
        {
            doDataReco.execute().get(m_waitSeconds, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            doDataReco.cancel(true);
            isReceivedResponse = FinalResponseStatus.Timeout;
        }
    }

    public void onFinalResponseReceived(final RecognitionResult response) {
        boolean isFinalDicationMessage = this.getMode() == SpeechRecognitionMode.LongDictation &&
                (response.RecognitionStatus == RecognitionStatus.EndOfDictation ||
                        response.RecognitionStatus == RecognitionStatus.DictationEndSilenceTimeout);
        if (null != this.micClient && this.getUseMicrophone() && ((this.getMode() == SpeechRecognitionMode.ShortPhrase) || isFinalDicationMessage)) {
            // we got the final result, so it we can end the mic reco.  No need to do this
            // for dataReco, since we already called endAudio() on it as soon as we were done
            // sending all the data.
            this.micClient.endMicAndRecognition();
        }

        if (isFinalDicationMessage) {
            this._startButton.setEnabled(true);
            this.isReceivedResponse = FinalResponseStatus.OK;
        }

        if (!isFinalDicationMessage) {
//            this.WriteLine("********* Final n-BEST Results *********");
            for (int i = 0; i < response.Results.length; i++) {
                String[] temp = response.Results[i].DisplayText.split(" ");
                if(response.Results[i].Confidence == Confidence.High || response.Results[i].Confidence == Confidence.Normal)
                    for(int j=0;j<temp.length;j++){
                        if(temp[j].toLowerCase().equals("help")) {
                            this.WriteLine("Found Help Cry");
                            j = temp.length;
                            i = response.Results.length;
                            break;
                        }
                    }

            }
            _startButton.performClick();
//            this.WriteLine();
        }
    }

    /**
     * Called when a final response is received and its intent is parsed
     */
    public void onIntentReceived(final String payload) {
//        this.WriteLine(payload);
//        this.WriteLine();
    }

    public void onPartialResponseReceived(final String response) {
//        this.WriteLine(response);
//        this.WriteLine();
    }

    public void onError(final int errorCode, final String response) {
        this._startButton.setEnabled(true);
        this.WriteLine("--- Error received by onError() ---");
        this.WriteLine("Error code: " + SpeechClientStatus.fromInt(errorCode) + " " + errorCode);
        this.WriteLine("Error text: " + response);
//        this.WriteLine();
    }

    /**
     * Called when the microphone status has changed.
     * @param recording The current recording state
     */
    public void onAudioEvent(boolean recording) {
        if (recording) {
//            this.WriteLine("Please start speaking.");
        }

//        WriteLine();
        if (!recording) {
            this.micClient.endMicAndRecognition();
            this._startButton.setEnabled(true);
        }
    }

    /**
     * Writes the line.
     */
    private void WriteLine() {
        this.WriteLine("");
    }

    /**
     * Writes the line.
     * @param text The line to write.
     */
    private void WriteLine(String text) {
        this._logText.append(text + "\n");
    }

    /**
     * Handles the Click event of the RadioButton control.
     * @param rGroup The radio grouping.
     * @param checkedId The checkedId.
     */
    private void RadioButton_Click(RadioGroup rGroup, int checkedId) {
        // Reset everything
        if (this.micClient != null) {
            this.micClient.endMicAndRecognition();
            try {
                this.micClient.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            this.micClient = null;
        }

        if (this.dataClient != null) {
            try {
                this.dataClient.finalize();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            this.dataClient = null;
        }

        this.ShowMenu(false);
        this._startButton.setEnabled(true);
    }

    /*
     * Speech recognition with data (for example from a file or audio source).
     * The data is broken up into buffers and each buffer is sent to the Speech Recognition Service.
     * No modification is done to the buffers, so the user can apply their
     * own VAD (Voice Activation Detection) or Silence Detection
     *
     * @param dataClient
     * @param recoMode
     * @param filename
     */
    private class RecognitionTask extends AsyncTask<Void, Void, Void> {
        DataRecognitionClient dataClient;
        SpeechRecognitionMode recoMode;
        String filename;

        RecognitionTask(DataRecognitionClient dataClient, SpeechRecognitionMode recoMode, String filename) {
            this.dataClient = dataClient;
            this.recoMode = recoMode;
            this.filename = filename;
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                // Note for wave files, we can just send data from the file right to the server.
                // In the case you are not an audio file in wave format, and instead you have just
                // raw data (for example audio coming over bluetooth), then before sending up any
                // audio data, you must first send up an SpeechAudioFormat descriptor to describe
                // the layout and format of your raw audio data via DataRecognitionClient's sendAudioFormat() method.
                // String filename = recoMode == SpeechRecognitionMode.ShortPhrase ? "whatstheweatherlike.wav" : "batman.wav";
                InputStream fileStream = getAssets().open(filename);
                int bytesRead = 0;
                byte[] buffer = new byte[1024];

                do {
                    // Get  Audio data to send into byte buffer.
                    bytesRead = fileStream.read(buffer);

                    if (bytesRead > -1) {
                        // Send of audio data to service.
                        dataClient.sendAudio(buffer, bytesRead);
                    }
                } while (bytesRead > 0);

            } catch (Throwable throwable) {
                throwable.printStackTrace();
            } finally {
                dataClient.endAudio();
                _startButton.setEnabled(true);
            }

            return null;
        }
    }


}
