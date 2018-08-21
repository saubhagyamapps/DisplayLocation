package com.example.android.displaylocation;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "MainActivity";
    private static final int GOOGLE_API_CLIENT_ID = 0;
    private static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(23.63936, 68.14712), new LatLng(28.20453, 97.34466));
    int FLAG = 1;
    ImageView imageViewLocation;
    private Button btnSearch;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private PlaceArrayAdapterFrom mPlaceArrayAdapterFrom;
    private AutoCompleteTextView mAutocompleteTextViewTo, mAutocompleteTextViewFrom;
    private double mStartlatitude, mStartlongitude;
    private double mEndlatitude, mEndlongitude;
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallbackfrom
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("Location", "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            mEndlatitude = place.getLatLng().latitude;
            mEndlongitude = place.getLatLng().longitude;

            Log.i("name", place.getName().toString());
            Log.i("coordinates", place.getLatLng().toString());
        }
    };
    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallbackto
            = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("Location", "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            if (FLAG == 1) {
                mStartlatitude = place.getLatLng().latitude;
                mStartlongitude = place.getLatLng().longitude;
            }
            Log.i("name", place.getName().toString());
            Log.i("coordinates", place.getLatLng().toString());
        }
    };
    private AdapterView.OnItemClickListener mAutocompleteClickListenerTo
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("Location", "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallbackto);
            Log.i("Location", "Fetching details for ID: " + item.placeId);
        }
    };
    private AdapterView.OnItemClickListener mAutocompleteClickListenerFrom
            = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapterFrom.PlaceAutocomplete item = mPlaceArrayAdapterFrom.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("Location", "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallbackfrom);
            Log.i("Location", "Fetching details for ID: " + item.placeId);
        }
    };

    public void getAddress(Context context, double LATITUDE, double LONGITUDE) {

        //Set Address
        try {
            Geocoder geocoder = new Geocoder(context, Locale.ENGLISH);
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null && addresses.size() > 0) {


                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                String city = addresses.get(0).getLocality();
                String state = addresses.get(0).getAdminArea();
                String country = addresses.get(0).getCountryName();
                String postalCode = addresses.get(0).getPostalCode();
                String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

                Log.d(TAG, "getAddress:  address" + address);
                Log.d(TAG, "getAddress:  city" + city);
                Log.d(TAG, "getAddress:  state" + state);
                Log.d(TAG, "getAddress:  postalCode" + postalCode);
                Log.d(TAG, "getAddress:  knownName" + knownName);
                mAutocompleteTextViewTo.setText(address);

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, TrackerService.class));
        initialization();
        requestLocationUpdates();

    }

    private void initialization() {

        mGoogleApiClient = new GoogleApiClient.Builder(MainActivity.this)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, GOOGLE_API_CLIENT_ID, this)
                .addConnectionCallbacks(this)
                .build();
        mAutocompleteTextViewTo = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewTO);
        mAutocompleteTextViewFrom = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextViewFrom);
        btnSearch = (Button) findViewById(R.id.btnSearch);
        imageViewLocation = (ImageView) findViewById(R.id.imgLocation);
        mAutocompleteTextViewTo.setThreshold(0);
        mAutocompleteTextViewTo.setOnItemClickListener(mAutocompleteClickListenerTo);
        mPlaceArrayAdapter = new PlaceArrayAdapter(this, R.layout.list_view, BOUNDS_MOUNTAIN_VIEW, null, R.drawable.marker_a);
        mAutocompleteTextViewTo.setAdapter(mPlaceArrayAdapter);

        mAutocompleteTextViewFrom.setThreshold(0);
        mAutocompleteTextViewFrom.setOnItemClickListener(mAutocompleteClickListenerFrom);
        mPlaceArrayAdapterFrom = new PlaceArrayAdapterFrom(this, android.R.layout.simple_list_item_1, BOUNDS_MOUNTAIN_VIEW, null);
        mAutocompleteTextViewFrom.setAdapter(mPlaceArrayAdapterFrom);
        searchBtnClick();
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(100);
        request.setFastestInterval(100);
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {

            client.requestLocationUpdates(request, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    final Location location = locationResult.getLastLocation();
                    if (location != null) {
                        Log.e(TAG, "location update " + location.getLongitude());
                        imageViewLocation.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FLAG = 0;
                                mStartlatitude = location.getLatitude();
                                mStartlongitude = location.getLongitude();
                                getAddress(getApplicationContext(), mStartlatitude, mStartlongitude);
                            }
                        });
                    }
                }
            }, null);
        }
    }

    private void searchBtnClick() {
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
                intent.putExtra("startlat", mStartlatitude);
                intent.putExtra("startlong", mStartlongitude);
                intent.putExtra("endlat", mEndlatitude);
                intent.putExtra("endlong", mEndlongitude);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
        mPlaceArrayAdapterFrom.setGoogleApiClient(mGoogleApiClient);
        Log.i("Location", "Google Places API connected.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("Location", "Google Places API connection failed with error code: "
                + connectionResult.getErrorCode());

        Toast.makeText(this,
                "Google Places API connection failed with error code:" +
                        connectionResult.getErrorCode(),
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onConnectionSuspended(int i) {
        mPlaceArrayAdapter.setGoogleApiClient(null);
        mPlaceArrayAdapterFrom.setGoogleApiClient(null);
        Log.e("Location", "Google Places API connection suspended.");
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }


}

