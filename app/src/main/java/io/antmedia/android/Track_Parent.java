package io.antmedia.android;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import io.antmedia.android.liveVideoBroadcaster.R;

import static io.antmedia.android.liveVideoBroadcaster.R.id.location;

/**
 * Created by Vasu Verma on 26/11/17.
 */

public class Track_Parent extends FragmentActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private FirebaseAuth mAuth;
    String bgcode;
    TextView loc;
    GoogleMap googleMap;
    GoogleApiClient mGoogleApiClient;
    public LatLng latLng = null;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(io.antmedia.android.liveVideoBroadcaster.R.layout.parent_track);

        loc=(TextView) findViewById(location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.googleMap);
        mapFragment.getMapAsync(this);
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
                        latLng = new LatLng(Double.parseDouble(la),Double.parseDouble(lo));
                        if(googleMap!=null) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                            googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
                            googleMap.addMarker(new MarkerOptions()
                                    .position(latLng)
                                    .title("Your child's location"));
                        }


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


    @Override
    public void onLocationChanged(Location location) {
        if(latLng!=null){
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(latLng!=null){
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onMapReady(GoogleMap googlemap) {
        googleMap = googlemap;
        buildGoogleApiClient();
        if(latLng!=null){
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            googleMap.animateCamera(CameraUpdateFactory.zoomTo(11));
        }

    }

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }
}
