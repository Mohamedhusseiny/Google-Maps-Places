package com.example.husseiiny.googlemapactivity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    // Place Picker
    private final static int PLACE_PICKER_REQUEST = 1;
    PlacePicker.IntentBuilder builder;

    // is used to set auto complete adapter
    GeoDataClient mGeoDataClient;

    // is used to hold the information of the searched place.
    PlaceInfo placeInfo;
    // Google map object to hold the map
    private GoogleMap mMap;
    // Client Location
    private FusedLocationProviderClient mFusedLocationProviderClient;
    AutoCompleteTextView searchEdit;
    // The adapter that is helps to view all possible results.
    PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;

    ImageView imgPicker;
    private Marker mMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Auto complete text view to display all possible results for user search.
        searchEdit = findViewById(R.id.input_search);
        imgPicker = findViewById(R.id.img_picker);

        builder = new PlacePicker.IntentBuilder();

    }

    //------------------------------------LOCATE POSITIONS------------------------------------------
    private void locateMyPosition(LatLng coordinates) {
        // Clear the map markers
        mMap.clear();

        // Geocoder object is used to return a list of all addresses by getFromLocation method
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses = new ArrayList<>();

        try {
            addresses = geocoder.getFromLocation(coordinates.latitude, coordinates.longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error getting adresses", Toast.LENGTH_SHORT).show();
        }

        if (addresses.size() > 0) {
            Address address = addresses.get(0);
            if (address != null) {
                // Set the camera to that address.
                mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));
            }
        }
        hideKeyboard();
    }

    private void geoLocate(LatLng coordinates, PlaceInfo info) {

        mMap.clear();
        String snippest = "Address: " + info.getAddress() + "\n"
                + "Phone Number: " + info.getPhoneNumber() + "\n"
                + "Website: " + info.getWebsite() + "\n"
                + "Rating: " + info.getRating() + "\n";
        // Set Info Window Adapter in custom view
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(this));

        // Set market with the details of the address.
        if (info != null) {
            try {
                mMarker = mMap.addMarker(new MarkerOptions().position(coordinates)
                        .title(info.getPlaceName())
                        .snippet(snippest));
            } catch (NullPointerException e) {
                e.getMessage();
            }
        } else {
            mMarker = mMap.addMarker(new MarkerOptions().position(coordinates));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLng(coordinates));

        hideKeyboard();
    }

    //==============================================================================================

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        mMap = googleMap;
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        googleMap.setMyLocationEnabled(true);
        // getting my  location
        final Task location = mFusedLocationProviderClient.getLastLocation();
        // When the task is complete, we can hold the result of the location
        location.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Location currentLocation = (Location) location.getResult();
                locateMyPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
            }
        });

        //---Set click listener for the image to view all the place picker
        imgPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placePickerRequest();
            }
        });

        //------------------------------------------------ PLACES ---------------------------------------------

        LatLngBounds latLngBounds = new LatLngBounds(new LatLng(-48, -48), new LatLng(120, 120));

        mGeoDataClient = Places.getGeoDataClient(this, null);
        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(this,
                mGeoDataClient, latLngBounds, null);

        // Set Adapter to the autoCompleteTextView for auto suggestions results entered by user.
        searchEdit.setAdapter(mPlaceAutocompleteAdapter);

        // SetOnItemClickListener is used to make action and go to the selected place by the user.
        searchEdit.setOnItemClickListener(mAutoCompleteClickListener);

                    // The following code is to hide the set location button
                    //mMap.getUiSettings().setMyLocationButtonEnabled(false);
    }

    //------------------------------------------------ PLACES ---------------------------------------------
    private AdapterView.OnItemClickListener mAutoCompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();

            Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(placeId);
            placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);

        }
    };

    /*
     * To prevent memory leak, we use OnCompleteListener to make sure that
     * the task is ended with a result.
     *
     * Functionality: Get all the information about the place we select that its request is submitted.
     */
    private OnCompleteListener<PlaceBufferResponse> mUpdatePlaceDetailsCallback = new OnCompleteListener<PlaceBufferResponse>() {
        @Override
        public void onComplete(@NonNull Task<PlaceBufferResponse> task) {
            if (task.isSuccessful()) {
                // Here to get the result of the task and assigned to Place Buffer.
                PlaceBufferResponse places = task.getResult();


                // Now, We are sure that we receive a place back.
                // Place Object contains address, phone number, website URL, LatLng..etc
                final Place place = places.get(0);

                try {
                    placeInfo = new PlaceInfo(place.getName().toString(), place.getAddress().toString(),
                            place.getPhoneNumber().toString()
                            , place.getWebsiteUri(), place.getRating());
                } catch (NullPointerException e) {
                    e.getMessage();
                }

                geoLocate(place.getLatLng(), placeInfo);
                /*
                 * we can release the place data to avoid memory leak after we geoLocate it.
                 */
                places.release();
            }
        }
    };

    //----------------------------------------PLACE PICKER -----------------------------------------
    private void placePickerRequest() {
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                Task<PlaceBufferResponse> placeResult = mGeoDataClient.getPlaceById(place.getId());
                placeResult.addOnCompleteListener(mUpdatePlaceDetailsCallback);
            }
        }
    }

    //----------------------------------------EXTERNAL METHODS--------------------------------------
    private void hideKeyboard() {
        // Hide the keyboard after searching.
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
    }
}
