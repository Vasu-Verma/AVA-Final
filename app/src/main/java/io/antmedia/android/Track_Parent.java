package io.antmedia.android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by AnamBhatia on 26/11/17.
 */

public class Track_Parent extends AppCompatActivity{

    private FirebaseAuth mAuth;
    String bgcode;
    TextView loc;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.antmedia.android.liveVideoBroadcaster.R.layout.parent_track);

        loc=(TextView) findViewById(io.antmedia.android.liveVideoBroadcaster.R.id.location);
        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        final String uid = mAuth.getCurrentUser().getUid();
        myRef.child("Users/Customers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                bgcode=dataSnapshot.child(uid).child("bag code").getValue().toString();
                System.out.println("vv: "+bgcode);
                myRef.child("Users/Bag/"+bgcode+"/").child("mvalue").setValue("YES");
            }


            @Override
            public void onCancelled(DatabaseError firebaseError) {
       /*
        * You may print the error message.
               **/
            }

        });



            myRef.child("bags").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(dataSnapshot.child(bgcode).child("l").exists()) {

                        String la = dataSnapshot.child(bgcode).child("l").child("0").getValue().toString();
                        String lo = dataSnapshot.child(bgcode).child("l").child("1").getValue().toString();
                        loc.setText(la + ", " + lo);

                    }


                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
       /*
        * You may print the error message.
               **/
                }

            });






    }

    public void stop(View v)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference();
        final String uid = mAuth.getCurrentUser().getUid();
        myRef.child("Users/Customers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {


                bgcode=dataSnapshot.child(uid).child("bag code").getValue().toString();
                System.out.println("vv: "+bgcode);
                myRef.child("Users/Bag/"+bgcode+"/").child("mvalue").setValue("NO");
            }


            @Override
            public void onCancelled(DatabaseError firebaseError) {
       /*
        * You may print the error message.
               **/
            }

        });

        Intent intent = new Intent(Track_Parent.this, MainActivity2.class);
        intent.putExtra("flag","P");
        startActivity(intent);
        finish();
        return;
    }









}
