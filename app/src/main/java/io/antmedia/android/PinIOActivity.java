package io.antmedia.android;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import io.antmedia.android.liveVideoBroadcaster.LiveVideoBroadcasterActivity;
import io.antmedia.android.liveVideoBroadcaster.R;
import io.antmedia.android.ble.BleManager;
import io.antmedia.android.ble.BleUtils;
import io.antmedia.android.liveVideoPlayer.LiveVideoPlayerActivity;
import io.antmedia.android.settings.ConnectedSettingsActivity;
import io.antmedia.android.ui.utils.ExpandableHeightExpandableListView;

public class PinIOActivity extends UartInterfaceActivity implements ISpeechRecognitionServerEvents{

    private final static String TAG = UartActivity.class.getSimpleName();
    private static final int kActivityRequestCode_ConnectedSettingsActivity = 0;
    private static final long CAPABILITY_QUERY_TIMEOUT = 15000;      // in milliseconds
    private static final byte SYSEX_START = (byte) 0xF0;
    private static final byte SYSEX_END = (byte) 0xF7;

    private static final int DEFAULT_PINS_COUNT = 20;
    private static final int FIRST_DIGITAL_PIN = 3;
    private static final int LAST_DIGITAL_PIN = 8;
    private static final int FIRST_ANALOG_PIN = 14;
    private static final int LAST_ANALOG_PIN = 19;

    private static final int kUartStatus_InputOutput = 0;       // Default mode (sending and receiving pin data)
    private static final int kUartStatus_QueryCapabilities = 1;
    private static final int kUartStatus_QueryAnalogMapping = 2;

    public static final String RTMP_BASE_URL = "rtmp://192.168.58.121/vod/";

    private FirebaseAuth mAuth;
    String bgcode;
    String value;
    String flag;
    TextView tv;
    int m_waitSeconds = 0;
    DataRecognitionClient dataClient = null;
    MicrophoneRecognitionClient micClient = null;
    MainActivity2.FinalResponseStatus isReceivedResponse = MainActivity2.FinalResponseStatus.NotReceived;
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
    private class PinData {
        private static final int kMode_Unknown = 255;
        private static final int kMode_Input = 0;
        private static final int kMode_Output = 1;
        private static final int kMode_Analog = 2;
        private static final int kMode_PWM = 3;
        private static final int kMode_Servo = 4;
        private static final int kMode_InputPullup = 0xb;

        private static final int kDigitalValue_Low = 0;
        private static final int kDigitalValue_High = 1;

        int digitalPinId = -1;
        int analogPinId = -1;
        boolean isInput;
        boolean isOutput;
        boolean isAnalog;
        boolean isPwm;
        boolean isInputPullup;

        int mode = kMode_Input;
        int digitalValue = kDigitalValue_Low;
        int analogValue = 0;

        PinData(int digitalPinId, boolean isInput, boolean isOutput, boolean isAnalog, boolean isPwm, boolean isInputPullup) {
            this.digitalPinId = digitalPinId;
            this.isInput = isInput;
            this.isOutput = isOutput;
            this.isAnalog = isAnalog;
            this.isPwm = isPwm;
            this.isInputPullup = isInputPullup;
        }
    }

    // UI
    private ExpandableHeightExpandableListView mPinListView;
    private ExpandableListAdapter mPinListAdapter;
    private ScrollView mPinScrollView;
    private AlertDialog mQueryCapabilitiesDialog;

    // Data
    private boolean mIsActivityFirstRun;
    private ArrayList<PinData> mPins = new ArrayList<>();
    private int mUartStatus = kUartStatus_InputOutput;
    private Handler mQueryCapabilitiesTimerHandler;
    private Runnable mQueryCapabilitiesTimerRunnable = new Runnable() {
        @Override
        public void run() {
            cancelQueryCapabilities();
        }
    };


    private DataFragment mRetainedDataFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pin_io);
        mBleManager = BleManager.getInstance(this);
        restoreRetainedDataFragment();

        mPinListView = (ExpandableHeightExpandableListView) findViewById(R.id.pinListView);
        mPinListAdapter = new ExpandableListAdapter();
        mPinListView.setAdapter(mPinListAdapter);
        mPinListView.setExpanded(true);

        mPinScrollView = (ScrollView) findViewById(R.id.pinScrollView);

        mIsActivityFirstRun = savedInstanceState == null;
        this._logText = (EditText) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.editText1);
        this._startButton = (Button) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.button1);
        this.track = (Button) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.track);
        this.partra = (Button) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.parent);
        this.stsr = (Button) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.stopsr);

        mAuth = FirebaseAuth.getInstance();

        if (getIntent() != null) {
            Intent in = getIntent();
            flag = "B";

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
                    PinIOActivity.this.StartButton_Click(arg0);
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
        onServicesDiscovered();
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



        Intent i = new Intent(PinIOActivity.this, LiveVideoBroadcasterActivity.class);
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

        Intent i = new Intent(PinIOActivity.this, LiveVideoPlayerActivity.class);
        startActivity(i);

    }

    public void trac(View v)
    {
        Intent intent = new Intent(PinIOActivity.this, DriverMapActivity.class);
        startActivity(intent);
        finish();
        return;
    }
    public void parenttrack(View v)
    {

        Intent intent = new Intent(PinIOActivity.this, Track_Parent.class);
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
        PinIOActivity.RecognitionTask doDataReco = new PinIOActivity.RecognitionTask(this.dataClient, this.getMode(), filename);
        try
        {
            doDataReco.execute().get(m_waitSeconds, TimeUnit.SECONDS);
        }
        catch (Exception e)
        {
            doDataReco.cancel(true);
            isReceivedResponse = MainActivity2.FinalResponseStatus.Timeout;
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
            this.isReceivedResponse = MainActivity2.FinalResponseStatus.OK;
        }

        if (!isFinalDicationMessage) {
//            this.WriteLine("********* Final n-BEST Results *********");
            for (int i = 0; i < response.Results.length; i++) {
                String[] temp = response.Results[i].DisplayText.split(" ");
                if(response.Results[i].Confidence == Confidence.High || response.Results[i].Confidence == Confidence.Normal)
                    for(int j=0;j<temp.length;j++){
                        if(temp[j].toLowerCase().equals("help")) {
                            this.WriteLine("Found Help Cry");
                            PinData pinData = mPins.get(0);
                            pinData.isOutput = true;
                            pinData.isInput = false;
                            int newState = PinData.kDigitalValue_High;
                            setDigitalValue(pinData, newState);
                            mPinListAdapter.notifyDataSetChanged();
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
    @Override
    public void onResume() {
        super.onResume();

        // Setup listeners
        mBleManager.setBleListener(this);
    }

    @Override
    public void onDestroy() {
        cancelQueryCapabilitiesTimer();

        // Retain data
        saveRetainedDataFragment();

        super.onDestroy();
    }

    // region Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pin_io, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_help) {
            startHelp();
            return true;
        } else if (id == R.id.action_connected_settings) {
            startConnectedSettings();
            return true;
        } else if (id == R.id.action_refreshcache) {
            if (mBleManager != null) {
                mBleManager.refreshDeviceCache();
            }
        } else if (id == R.id.action_query) {
            reset();
        }

        return super.onOptionsItemSelected(item);
    }

    private void startConnectedSettings() {
        // Launch connected settings activity
        Intent intent = new Intent(this, ConnectedSettingsActivity.class);
        startActivityForResult(intent, kActivityRequestCode_ConnectedSettingsActivity);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
        if (resultCode == RESULT_OK && requestCode == kActivityRequestCode_ConnectedSettingsActivity) {
            finish();
        }
    }

    private void startHelp() {
        // Launch app help activity

    }
    // endregion

    private boolean isQueryingCapabilities() {
        return mUartStatus != kUartStatus_InputOutput;
    }

    @Override
    public void onDataAvailable(BluetoothGattCharacteristic characteristic) {
        byte[] data = characteristic.getValue();
        Log.d(TAG, "received: " + BleUtils.bytesToHexWithSpaces(data));

        switch (mUartStatus) {
            case kUartStatus_QueryCapabilities:
                receivedQueryCapabilities(data);
                break;
            case kUartStatus_QueryAnalogMapping:
                receivedAnalogMapping(data);
                break;
            default:
                receivedPinState(data);
                break;
        }
    }

    // region Query Capabilities

    private void reset() {
        mUartStatus = kUartStatus_InputOutput;
        mPins.clear();

        mPinListAdapter.notifyDataSetChanged();

        // Reset Firmata
        byte bytes[] = new byte[]{(byte) 0xff};
        sendHexData(bytes);

        startQueryCapabilitiesProcess();
    }

    private ArrayList<Byte> queryCapabilitiesDataBuffer = new ArrayList<>();

    private void queryCapabilities() {
        Log.d(TAG, "queryCapabilities");

        // Set status
        mPins.clear();
        mUartStatus = kUartStatus_QueryCapabilities;
        queryCapabilitiesDataBuffer.clear();

        // Query Capabilities
        byte bytes[] = new byte[]{SYSEX_START, (byte) 0x6B, SYSEX_END};
        sendHexData(bytes);


        mQueryCapabilitiesTimerHandler = new Handler();
        mQueryCapabilitiesTimerHandler.postDelayed(mQueryCapabilitiesTimerRunnable, CAPABILITY_QUERY_TIMEOUT);
    }

    private void receivedQueryCapabilities(byte[] data) {
        // Read received packet
        for (final byte dataByte : data) {
            queryCapabilitiesDataBuffer.add(dataByte);
            if (dataByte == SYSEX_END) {
                Log.d(TAG, "Finished receiving capabilities");
                queryAnalogMapping();
                break;
            }
        }
    }

    private void cancelQueryCapabilitiesTimer() {
        if (mQueryCapabilitiesTimerHandler != null) {
            mQueryCapabilitiesTimerHandler.removeCallbacks(mQueryCapabilitiesTimerRunnable);
            mQueryCapabilitiesTimerHandler = null;
        }
    }
    // endregion

    // region Query AnalogMapping

    private ArrayList<Byte> queryAnalogMappingDataBuffer = new ArrayList<>();

    private void queryAnalogMapping() {
        Log.d(TAG, "queryAnalogMapping");

        // Set status
        mUartStatus = kUartStatus_QueryAnalogMapping;
        queryAnalogMappingDataBuffer.clear();

        // Query Analog Mapping
        byte bytes[] = new byte[]{SYSEX_START, (byte) 0x69, SYSEX_END};
        sendHexData(bytes);
    }

    private void receivedAnalogMapping(byte[] data) {
        cancelQueryCapabilitiesTimer();

        // Read received packet
        for (final byte dataByte : data) {
            queryAnalogMappingDataBuffer.add(dataByte);
            if (dataByte == SYSEX_END) {
                Log.d(TAG, "Finished receiving Analog Mapping");
                endPinQuery(false);
                break;
            }
        }
    }

    public void cancelQueryCapabilities() {
        Log.d(TAG, "timeout: cancelQueryCapabilities");
        endPinQuery(true);
    }

    // endregion

    // region Process Capabilities
    private void endPinQuery(boolean abortQuery) {
        cancelQueryCapabilitiesTimer();
        mUartStatus = kUartStatus_InputOutput;

        boolean capabilitiesParsed = false;
        boolean mappingDataParsed = false;
        if (!abortQuery && queryCapabilitiesDataBuffer.size() > 0 && queryAnalogMappingDataBuffer.size() > 0) {
            capabilitiesParsed = parseCapabilities(queryCapabilitiesDataBuffer);
            mappingDataParsed = parseAnalogMappingData(queryAnalogMappingDataBuffer);
        }

        final boolean isDefaultConfigurationAssumed = abortQuery || !capabilitiesParsed || !mappingDataParsed;
        if (isDefaultConfigurationAssumed) {
            initializeDefaultPins();
        }
        enableReadReports();

        // Clean received data
        queryCapabilitiesDataBuffer.clear();
        queryAnalogMappingDataBuffer.clear();

        // Refresh
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mQueryCapabilitiesDialog != null) {
                    mQueryCapabilitiesDialog.dismiss();
                }
                mPinListAdapter.notifyDataSetChanged();

                if (isDefaultConfigurationAssumed) {
                    defaultCapabilitiesAssumedDialog();
                }
            }
        });
    }

    private boolean parseCapabilities(ArrayList<Byte> capabilitiesData) {
        int endIndex = capabilitiesData.indexOf(SYSEX_END);
        if (capabilitiesData.size() > 2 && capabilitiesData.get(0) == SYSEX_START && capabilitiesData.get(1) == 0x6C && endIndex >= 0) {
            // Separate pin data
            ArrayList<ArrayList<Byte>> pinsBytes = new ArrayList<>();
            ArrayList<Byte> currentPin = new ArrayList<>();
            for (int i = 2; i < endIndex; i++) {        // Skip 2 header bytes and end byte
                byte dataByte = capabilitiesData.get(i);
                if (dataByte != 0x7f) {
                    currentPin.add(dataByte);
                } else {      // Finished current pin
                    pinsBytes.add(currentPin);
                    currentPin = new ArrayList<>();
                }
            }

            // Extract pin info
            mPins.clear();
            int pinNumber = 0;

                boolean isInput = false, isOutput = false, isAnalog = false, isPWM = false, isInputPullup = false;

                    PinData pinData = new PinData(6, false, true, isAnalog, isPWM, isInputPullup);
                    mPins.add(pinData);
                    int newMode  = PinData.kMode_Output;
                    setControlMode(pinData, newMode);
                    pinData = new PinData(18, false, false, true, isPWM, isInputPullup);
                    newMode  = PinData.kMode_Analog;
                    setControlMode(pinData, newMode);
                    mPins.add(pinData);
                    Log.d(TAG, "pin id: " + pinNumber + " isInput: " + (pinData.isInput ? "yes" : "no") + " isOutput: " + (pinData.isOutput ? "yes" : "no") + " analog: " + (pinData.isAnalog ? "yes" : "no") + " isInputPullup: " + (pinData.isInputPullup ? "yes" : "no"));
                RunLights();
                return true;

        } else {
            Log.d(TAG, "invalid capabilities received");
            if (capabilitiesData.size() <= 2) {
                Log.d(TAG, "capabilitiesData size <= 2");
            }
            if (capabilitiesData.get(0) != SYSEX_START) {
                Log.d(TAG, "SYSEX_START not present");
            }
            if (endIndex < 0) {
                Log.d(TAG, "SYSEX_END not present");
            }

            return false;
        }
    }

    private void RunLights() {
        Log.d(TAG, "RunLights: ");
        PinData pinData = mPins.get(1);
//        pinData.isOutput = true;
//        pinData.isInput = false;
//        int newState = PinData.kDigitalValue_High;
//        setDigitalValue(pinData, newState);
//        int newMode  = PinData.kMode_Analog;
//        setControlMode(pinData, newMode);
//        mPinListAdapter.notifyDataSetChanged();

    }

    private boolean parseAnalogMappingData(ArrayList<Byte> analogData) {
        int endIndex = analogData.indexOf(SYSEX_END);
        if (analogData.size() > 2 && analogData.get(0) == SYSEX_START && analogData.get(1) == 0x6A && endIndex >= 0) {
            int pinNumber = 0;

            for (int i = 2; i < endIndex; i++) {        // Skip 2 header bytes and end byte
                byte dataByte = analogData.get(i);
                if (dataByte != 0x7f) {
                    int indexOfPinNumber = indexOfPinWithDigitalId(pinNumber);
                    if (indexOfPinNumber >= 0) {
                        mPins.get(indexOfPinNumber).analogPinId = dataByte & 0xff;
                        Log.d(TAG, "pin id: " + pinNumber + " analog id: " + dataByte);
                    } else {
                        Log.d(TAG, "warning: trying to set analog id: " + dataByte + " for pin id: " + pinNumber);
                    }

                }
                pinNumber++;
            }
            return true;
        } else {
            Log.d(TAG, "invalid analog mapping received");
            return false;
        }
    }

    private int indexOfPinWithDigitalId(int digitalPinId) {
        int i = 0;
        while (i < mPins.size() && mPins.get(i).digitalPinId != digitalPinId) {
            i++;
        }
        return i < mPins.size() ? i : -1;
    }

    private int indexOfPinWithAnalogId(int analogPinId) {
        int i = 0;
        while (i < mPins.size() && mPins.get(i).analogPinId != analogPinId) {
            i++;
        }
        return i < mPins.size() ? i : -1;
    }
    // endregion

    // region Pin Management
    private void initializeDefaultPins() {
        mPins.clear();

        for (int i = 0; i < DEFAULT_PINS_COUNT; i++) {
            PinData pin = null;
            if (i == 3 || i == 5 || i == 6) {
                pin = new PinData(i, true, true, false, false, false);
            } else if (i >= FIRST_DIGITAL_PIN && i <= LAST_DIGITAL_PIN) {
                pin = new PinData(i, true, true, false, false, false);
            } else if (i >= FIRST_ANALOG_PIN && i <= LAST_ANALOG_PIN) {
                pin = new PinData(i, true, true, true, false, false);
                pin.analogPinId = i - FIRST_ANALOG_PIN;
            }

            if (pin != null) {
                mPins.add(pin);
            }
        }
    }

    private void enableReadReports() {

        // Enable read reports by port
        for (int i = 0; i <= 2; i++) {
            byte data0 = (byte) (0xd0 + i);     // start port 0 digital reporting (0xD0 + port#)
            byte data1 = 1;                     // enable
            byte bytes[] = new byte[]{data0, data1};
            sendHexData(bytes);
        }

        // Set all pin modes active
        for (int i = 0; i < mPins.size(); i++) {
            // Write pin mode
            PinData pin = mPins.get(i);
            setControlMode(pin, pin.mode);
        }
    }

    private void setControlMode(PinData pin, int mode) {
        int previousMode = pin.mode;

        // Store
        pin.mode = mode;
        pin.digitalValue = PinData.kDigitalValue_Low;       // Reset dialog value when changing mode
        pin.analogValue = 0;                                // Reset analog value when changing mode

        // Write pin mode
        byte bytes[] = new byte[]{(byte) 0xf4, (byte) pin.digitalPinId, (byte) mode};
        sendHexData(bytes);

        // Update reporting for Analog pins
        if (mode == PinData.kMode_Analog) {
            setAnalogValueReporting(pin, true);
        } else if (previousMode == PinData.kMode_Analog) {
            setAnalogValueReporting(pin, false);
        }
    }

    private void setAnalogValueReporting(PinData pin, boolean enabled) {
        // Write pin mode
        byte data0 = (byte) (0xc0 + pin.analogPinId);       // start analog reporting for pin (192 + pin#)
        byte data1 = (byte) (enabled ? 1 : 0);              // enable

        // send data
        byte bytes[] = {data0, data1};
        sendHexData(bytes);
    }

    private void setDigitalValue(PinData pin, int value) {
        // Store
        pin.digitalValue = value;
        Log.d(TAG, "setDigitalValue: " + value + " for pin id: " + pin.digitalPinId);

        // Write value
        int port = pin.digitalPinId / 8;
        byte data0 = (byte) (0x90 + port);

        int offset = 8 * port;
        int state = 0;
        for (int i = 0; i <= 7; i++) {
            int pinIndex = indexOfPinWithDigitalId(offset + i);
            if (pinIndex >= 0) {
                int pinValue = mPins.get(pinIndex).digitalValue & 0x1;
                int pinMask = pinValue << i;
                state |= pinMask;
            }
        }

        byte data1 = (byte) (state & 0x7f);      // only 7 bottom bits
        byte data2 = (byte) (state >> 7);        // top bit in second byte

        // send data
        byte bytes[] = new byte[]{data0, data1, data2};
        sendHexData(bytes);
    }

    private long lastSentAnalogValueTime = 0;

    private boolean setPMWValue(PinData pin, int value) {

        // Limit the amount of messages sent over Uart
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSentAnalogValueTime >= 100) {
            Log.d(TAG, "pwm elapsed: " + (currentTime - lastSentAnalogValueTime));
            lastSentAnalogValueTime = currentTime;

            // Store
            pin.analogValue = value;

            // Send
            byte bytes[];
            if (pin.digitalPinId > 15) {
                // Extended analog
                byte data0 = (byte) (pin.digitalPinId);
                byte data1 = (byte) (value & 0x7f);      // only 7 bottom bits
                byte data2 = (byte) (value >> 7);        // top bit in second byte

                bytes = new byte[]{SYSEX_START, 0x6f, data0, data1, data2, SYSEX_END};

            } else {
                byte data0 = (byte) (0xe0 + pin.digitalPinId);
                byte data1 = (byte) (value & 0x7f);      // only 7 bottom bits
                byte data2 = (byte) (value >> 7);        // top bit in second byte

                bytes = new byte[]{data0, data1, data2};
            }
            sendHexData(bytes);


            return true;
        } else {
            Log.d(TAG, "Won't send: Too many slider messages");
            return false;
        }
    }

    private ArrayList<Byte> receivedPinStateDataBuffer2 = new ArrayList<>();

    private int getUnsignedReceivedPinState(int index) {
        return receivedPinStateDataBuffer2.get(index) & 0xff;
    }

    private void receivedPinState(byte[] data) {

        // Append received bytes to buffer
        for (final byte dataByte : data) {
            receivedPinStateDataBuffer2.add(dataByte);
        }

        // Check if we received a pin state response
        int endIndex = receivedPinStateDataBuffer2.indexOf(SYSEX_END);
        if (receivedPinStateDataBuffer2.size() >= 5 && getUnsignedReceivedPinState(0) == SYSEX_START && getUnsignedReceivedPinState(1) == 0x6e && endIndex >= 0) {


            int pinDigitalId = getUnsignedReceivedPinState(2);
            int pinMode = getUnsignedReceivedPinState(3);
            int pinState = getUnsignedReceivedPinState(4);

            int index = indexOfPinWithDigitalId(pinDigitalId);
            if (index >= 0) {
                PinData pin = mPins.get(index);
                pin.mode = pinMode;
                if (pinMode == PinData.kMode_Analog || pinMode == PinData.kMode_PWM || pinMode == PinData.kMode_Servo) {
                    if (receivedPinStateDataBuffer2.size() >= 6) {
                        pin.analogValue = pinState + (getUnsignedReceivedPinState(5) << 7);
                    } else {
                        Log.d(TAG, "Warning: received pinstate for analog pin without analogValue");
                    }
                } else {
                    if (pinState == PinData.kDigitalValue_Low || pinState == PinData.kDigitalValue_High) {
                        pin.digitalValue = pinState;
                    } else {
                        Log.d(TAG, "Warning: received pinstate with unknown digital value. Valid (0,1). Received: " + pinState);
                    }
                }

            } else {
                Log.d(TAG, "Warning: received pinstate for unknown digital pin id: " + pinDigitalId);
            }

            //  Remove from the buffer the bytes parsed
            for (int i = 0; i < endIndex; i++) {
                receivedPinStateDataBuffer2.remove(0);
            }
        } else {
            // Each pin message is 3 bytes long
            int data0 = getUnsignedReceivedPinState(0);
            boolean isDigitalReportingMessage = data0 >= 0x90 && data0 <= 0x9F;
            boolean isAnalogReportingMessage = data0 >= 0xe0 && data0 <= 0xef;
//            Log.d(TAG, "data0: "+data0);

            Log.d(TAG, "receivedPinStateDataBuffer size: " + receivedPinStateDataBuffer2.size());
            //          Log.d(TAG, "data[0]="+BleUtils.byteToHex(receivedPinStateDataBuffer.get(0))+ "data[1]="+BleUtils.byteToHex(receivedPinStateDataBuffer.get(1)));

            while (receivedPinStateDataBuffer2.size() >= 3 && (isDigitalReportingMessage || isAnalogReportingMessage)) {     // Check that current message length is at least 3 bytes
                if (isDigitalReportingMessage) {            // Digital Reporting (per port)
                     /* two byte digital data format, second nibble of byte 0 gives the port number (e.g. 0x92 is the third port, port 2)
                    * 0  digital data, 0x90-0x9F, (MIDI NoteOn, but different data format)
                    * 1  digital pins 0-6 bitmask
                    * 2  digital pin 7 bitmask
                    */

                    int port = getUnsignedReceivedPinState(0) - 0x90;
                    int pinStates = getUnsignedReceivedPinState(1);
                    pinStates |= getUnsignedReceivedPinState(2) << 7;        // PORT 0: use LSB of third byte for pin7, PORT 1: pins 14 & 15
                    updatePinsForReceivedStates(pinStates, port);
                } else if (isAnalogReportingMessage) {        // Analog Reporting (per pin)
                    /* analog 14-bit data format
                    * 0  analog pin, 0xE0-0xEF, (MIDI Pitch Wheel)
                    * 1  analog least significant 7 bits
                    * 2  analog most significant 7 bits
                    */

                    int analogPinId = getUnsignedReceivedPinState(0) - 0xe0;
                    int value = getUnsignedReceivedPinState(1) + (getUnsignedReceivedPinState(2) << 7);

                    int index = indexOfPinWithAnalogId(analogPinId);
                    if (index >= 0) {
                        PinData pin = mPins.get(index);
                        pin.analogValue = value;
                        if(value>20) {
                            PinData pinData = mPins.get(0);
                            pinData.isOutput = true;
                            pinData.isInput = false;
                            int newState = PinData.kDigitalValue_High;
                            setDigitalValue(pinData, newState);
                            mPinListAdapter.notifyDataSetChanged();
                        }
                        Log.d(TAG, "received analog value: " + value + " pin analog id: " + analogPinId + " digital Id: " + index);
                    } else {
                        Log.d(TAG, "Warning: received pinstate for unknown analog pin id: " + index);
                    }
                }

                //  Remove from the buffer the bytes parsed
                for (int i = 0; i < 3; i++) {
                    receivedPinStateDataBuffer2.remove(0);
                }

                // Setup vars for next message
                if (receivedPinStateDataBuffer2.size() >= 3) {
                    data0 = getUnsignedReceivedPinState(0);
                    isDigitalReportingMessage = data0 >= 0x90 && data0 <= 0x9F;
                    isAnalogReportingMessage = data0 >= 0xe0 && data0 <= 0xef;

                } else {
                    isDigitalReportingMessage = false;
                    isAnalogReportingMessage = false;
                }
            }
        }

        // Refresh
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPinListAdapter.notifyDataSetChanged();
            }
        });
    }

    private void updatePinsForReceivedStates(int pinStates, int port) {
        int offset = 8 * port;

        // Iterate through all pins
        for (int i = 0; i <= 7; i++) {
            int mask = 1 << i;
            int state = (pinStates & mask) >> i;

            int digitalId = offset + i;

            int index = indexOfPinWithDigitalId(digitalId);
            if (index >= 0) {
                PinData pin = mPins.get(index);
                pin.digitalValue = state;
                //Log.d(TAG, "update pinid: " + digitalId + " digitalValue: " + state);
            }
        }
    }

    // endregion

    public void onClickPinIOTitle(final View view) {
        final int groupPosition = (Integer) view.getTag();
        if (mPinListView.isGroupExpanded(groupPosition)) {
            mPinListView.collapseGroup(groupPosition);
        } else {
            // Expand this, Collapse the rest
            int len = mPinListAdapter.getGroupCount();
            for (int i = 0; i < len; i++) {
                if (i != groupPosition) {
                    mPinListView.collapseGroup(i);
                }
            }

            mPinListView.expandGroup(groupPosition, true);

            // Force scrolling to view the children
            mPinScrollView.post(new Runnable() {
                @Override
                public void run() {
                    mPinListView.scrollToGroup(groupPosition, view, mPinScrollView);
                }
            });
        }
    }


    public void onClickMode(View view) {
        PinData pinData = (PinData) view.getTag();

        int newMode = PinData.kMode_Unknown;
        switch (view.getId()) {
            case R.id.inputRadioButton:
                newMode = PinData.kMode_Input;      // Reset mode to input
                break;
            case R.id.outputRadioButton:
                newMode = PinData.kMode_Output;
                break;
            case R.id.pwmRadioButton:
                newMode = PinData.kMode_PWM;
                break;
            case R.id.analogRadioButton:
                newMode = PinData.kMode_Analog;
                break;
        }

        if (newMode != PinData.kMode_Unknown) {
            setControlMode(pinData, newMode);

            mPinListAdapter.notifyDataSetChanged();
        }
    }

    public void onClickInputType(View view) {
        PinData pinData = (PinData) view.getTag();

        int newMode = PinData.kMode_Input;
        switch (view.getId()) {
            case R.id.floatingRadioButton:
                newMode = PinData.kMode_Input;
                break;
            case R.id.pullupRadioButton:
                newMode = PinData.kMode_InputPullup;
                break;
        }

        setControlMode(pinData, newMode);
        mPinListAdapter.notifyDataSetChanged();
    }

    public void onClickOutputType(View view) {
        Log.d(TAG, "onClickOutputType: ");
        PinData pinData = mPins.get(0);
        int newState = PinData.kDigitalValue_High;
        setDigitalValue(pinData, newState);
        mPinListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onDisconnected() {
        super.onDisconnected();
        Log.d(TAG, "Disconnected. Back to previous activity");
        setResult(-1);      // Unexpected Disconnect
        finish();
    }

    @Override
    public void onServicesDiscovered() {
        super.onServicesDiscovered();
        enableRxNotifications();

        // PinIo init
        if (mIsActivityFirstRun) {
            reset();
        }
        Log.d(TAG, "onServicesDiscovered: ");
    }


    private void sendHexData(byte[] data) {
        if (BuildConfig.DEBUG) {
            String hexRepresentation = BleUtils.bytesToHexWithSpaces(data);
            Log.d(TAG, "sendHex: " + hexRepresentation);
        }
        sendData(data);
    }

    // region UI

    private void startQueryCapabilitiesProcess() {
        if (!isQueryingCapabilities()) {

            // Show dialog
            mQueryCapabilitiesDialog = new AlertDialog.Builder(this)
                    .setMessage(R.string.pinio_capabilityquery_querying_title)
                    .setCancelable(true)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            endPinQuery(true);
                        }
                    })
                    .create();

            mQueryCapabilitiesDialog.show();

            // Start process
            queryCapabilities();
        } else {
            Log.d(TAG, "error: queryCapabilities called while querying capabilities");
        }
    }

    private void defaultCapabilitiesAssumedDialog() {
        Log.d(TAG, "QueryCapabilities not found");

        // Show dialog
        new AlertDialog.Builder(this)
                .setTitle(R.string.pinio_capabilityquery_expired_title)
                .setMessage(R.string.pinio_capabilityquery_expired_message)
                .create()
                .show();
    }

    // endregion

    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        @Override
        public int getGroupCount() {
            return mPins.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return 1;
        }

        @Override
        public Object getGroup(int groupPosition) {
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.layout_pinio_item_title, parent, false);
            }

            convertView.setTag(groupPosition);

            PinData pin = mPins.get(groupPosition);

            TextView nameTextView = (TextView) convertView.findViewById(R.id.nameTextView);

            String name;
            if (pin.isAnalog) {
                name = String.format(getString(R.string.pinio_pinname_analog_format), pin.digitalPinId, pin.analogPinId);
            } else {
                name = String.format(getString(R.string.pinio_pinname_digital_format), pin.digitalPinId);
            }
            nameTextView.setText(name);

            // UI: Mode
            TextView modeTextView = (TextView) convertView.findViewById(R.id.modeTextView);
            modeTextView.setText(stringForPinMode(pin.mode));

            // UI: State
            TextView valueTextView = (TextView) convertView.findViewById(R.id.stateTextView);
            String valueString;
            switch (pin.mode) {
                case PinData.kMode_Input:
                case PinData.kMode_InputPullup:
                    valueString = stringForPinDigitalValue(pin.digitalValue);
                    break;
                case PinData.kMode_Output:
                    valueString = stringForPinDigitalValue(pin.digitalValue);
                    break;
                case PinData.kMode_Analog:
                    valueString = String.valueOf(pin.analogValue);
                    break;
                case PinData.kMode_PWM:
                    valueString = String.valueOf(pin.analogValue);
                    break;
                default:
                    valueString = "";
                    break;
            }
            valueTextView.setText(valueString);


            return convertView;
        }

        private String stringForPinMode(int mode) {
            int modeStringResourceId;
            switch (mode) {
                case PinData.kMode_Input:
                    modeStringResourceId = R.string.pinio_pintype_inputfloating_long;
                    break;
                case PinData.kMode_Output:
                    modeStringResourceId = R.string.pinio_pintype_output;
                    break;
                case PinData.kMode_Analog:
                    modeStringResourceId = R.string.pinio_pintype_analog;
                    break;
                case PinData.kMode_PWM:
                    modeStringResourceId = R.string.pinio_pintype_pwm;
                    break;
                case PinData.kMode_Servo:
                    modeStringResourceId = R.string.pinio_pintype_servo;
                    break;
                case PinData.kMode_InputPullup:
                    modeStringResourceId = R.string.pinio_pintype_inputpullup_long;
                    break;

                default:
                    modeStringResourceId = R.string.pinio_pintype_unknown;
                    break;
            }

            return getString(modeStringResourceId);
        }

        private String stringForPinDigitalValue(int digitalValue) {
            int stateStringResourceId;
            switch (digitalValue) {
                case PinData.kDigitalValue_Low:
                    stateStringResourceId = R.string.pinio_pintype_low;
                    break;
                case PinData.kDigitalValue_High:
                    stateStringResourceId = R.string.pinio_pintype_high;
                    break;
                default:
                    stateStringResourceId = R.string.pinio_pintype_unknown;
                    break;
            }

            return getString(stateStringResourceId);
        }

        @Override
        public View getChildView(final int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.layout_pinio_item_child, parent, false);
            }
            // set tags
            final PinData pin = mPins.get(groupPosition);

            // Setup mode
            RadioButton inputRadioButton = (RadioButton) convertView.findViewById(R.id.inputRadioButton);
            inputRadioButton.setTag(pin);
            inputRadioButton.setChecked(pin.mode == PinData.kMode_Input || pin.mode == PinData.kMode_InputPullup);
            inputRadioButton.setVisibility(pin.isInput || pin.isInputPullup ? View.VISIBLE : View.GONE);

            RadioButton outputRadioButton = (RadioButton) convertView.findViewById(R.id.outputRadioButton);
            outputRadioButton.setTag(pin);
            outputRadioButton.setChecked(pin.mode == PinData.kMode_Output);
            outputRadioButton.setVisibility(pin.isOutput ? View.VISIBLE : View.GONE);

            RadioButton pwmRadioButton = (RadioButton) convertView.findViewById(R.id.pwmRadioButton);
            pwmRadioButton.setTag(pin);
            pwmRadioButton.setChecked(pin.mode == PinData.kMode_PWM);
            pwmRadioButton.setVisibility(pin.isPwm ? View.VISIBLE : View.GONE);

            RadioButton analogRadioButton = (RadioButton) convertView.findViewById(R.id.analogRadioButton);
            analogRadioButton.setTag(pin);
            analogRadioButton.setChecked(pin.mode == PinData.kMode_Analog);
            analogRadioButton.setVisibility(pin.isAnalog ? View.VISIBLE : View.GONE);

            // Setup input mode
            boolean isInputModeVisible = pin.mode == PinData.kMode_Input || pin.mode == PinData.kMode_InputPullup;
            RadioGroup inputRadioGroup = (RadioGroup) convertView.findViewById(R.id.inputRadioGroup);
            inputRadioGroup.setVisibility(isInputModeVisible ? View.VISIBLE : View.GONE);
            if (isInputModeVisible) {
                RadioButton floatingRadioButton = (RadioButton) convertView.findViewById(R.id.floatingRadioButton);
                floatingRadioButton.setTag(pin);
                floatingRadioButton.setChecked(pin.mode == PinData.kMode_Input);

                RadioButton pullupRadioButton = (RadioButton) convertView.findViewById(R.id.pullupRadioButton);
                pullupRadioButton.setTag(pin);
                pullupRadioButton.setChecked(pin.mode == PinData.kMode_InputPullup);
            }

            // Setup output state
            boolean isStateVisible = pin.mode == PinData.kMode_Output;
            RadioGroup stateRadioGroup = (RadioGroup) convertView.findViewById(R.id.stateRadioGroup);
            stateRadioGroup.setVisibility(isStateVisible ? View.VISIBLE : View.GONE);
            if (isStateVisible) {
                RadioButton lowRadioButton = (RadioButton) convertView.findViewById(R.id.lowRadioButton);
                lowRadioButton.setTag(pin);
                lowRadioButton.setChecked(pin.digitalValue == PinData.kDigitalValue_Low);

                RadioButton highRadioButton = (RadioButton) convertView.findViewById(R.id.highRadioButton);
                highRadioButton.setTag(pin);
                highRadioButton.setChecked(pin.digitalValue == PinData.kDigitalValue_High);
            }

            // pwm slider bar
            boolean isPwmBarVisible = pin.mode == PinData.kMode_PWM;
            SeekBar pmwSeekBar = (SeekBar) convertView.findViewById(R.id.pmwSeekBar);
            pmwSeekBar.setVisibility(isPwmBarVisible ? View.VISIBLE : View.GONE);
            pmwSeekBar.setProgress(pin.analogValue);
            pmwSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser) {
//                        pin.analogValue = progress;
                        setPMWValue(pin, progress);

                        // Update only the value in the parent group
                        long parentPacketPosition = ExpandableListView.getPackedPositionForGroup(groupPosition);
                        long parentFlatPosition = mPinListView.getFlatListPosition(parentPacketPosition);
                        if (parentFlatPosition >= mPinListView.getFirstVisiblePosition() && parentFlatPosition <= mPinListView.getLastVisiblePosition()) {
                            View view = mPinListView.getChildAt((int) parentFlatPosition);
                            TextView valueTextView = (TextView) view.findViewById(R.id.stateTextView);
                            valueTextView.setText(String.valueOf(progress));
                        }
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    setPMWValue(pin, pin.analogValue);
                    mPinListAdapter.notifyDataSetChanged();
                }
            });

            // spacer visibility (spacers are shown if pwm or analog are visible)
            final boolean isSpacer2Visible = pin.isPwm || pin.isAnalog;
            View spacer2View = convertView.findViewById(R.id.spacer2View);
            spacer2View.setVisibility(isSpacer2Visible ? View.VISIBLE : View.GONE);

            final boolean isSpacer3Visible = pin.isPwm && pin.isAnalog;
            View spacer3View = convertView.findViewById(R.id.spacer3View);
            spacer3View.setVisibility(isSpacer3Visible ? View.VISIBLE : View.GONE);

            return convertView;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }
    }

    // region DataFragment
    public static class DataFragment extends Fragment {
        private ArrayList<PinData> mPins;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }

    private void restoreRetainedDataFragment() {
        // find the retained fragment
        FragmentManager fm = getFragmentManager();
        mRetainedDataFragment = (DataFragment) fm.findFragmentByTag(TAG);

        if (mRetainedDataFragment == null) {
            // Create
            mRetainedDataFragment = new DataFragment();
            fm.beginTransaction().add(mRetainedDataFragment, TAG).commit();

            // Init variables
            mPins = new ArrayList<>();

        } else {
            // Restore status
            mPins = mRetainedDataFragment.mPins;
        }
    }

    private void saveRetainedDataFragment() {
        mRetainedDataFragment.mPins = mPins;
    }
    // endregion
}
