package io.antmedia.android;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FirstActivity extends AppCompatActivity {
    private Button mDriver, mCustomer;
    public static char flag;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.antmedia.android.liveVideoBroadcaster.R.layout.activity_first);

        mDriver = (Button) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.driver);
        mCustomer = (Button) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.customer);

        mDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstActivity.this, DriverLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });

        mCustomer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FirstActivity.this, CustomerLoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        });


    }
}