package com.example.viyer;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.viyer.models.Product;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;
import org.parceler.Parcels;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.RuntimePermissions;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

@RuntimePermissions
public class SuggestLocationActivity extends AppCompatActivity {

    public static final String TAG = "SuggestLocationActivity";
    private SupportMapFragment mapFragment;
    private GoogleMap map;
    private Location mCurrentLocation;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 60000;
    private long FASTEST_INTERVAL = 5000;
    private AutocompleteSupportFragment autocompleteFragment;
    private FirebaseUser user;
    private LatLng latLng;
    private Product product;
    private ImageButton ivSuggest;
    private TextView tvPlace;
    private TextInputLayout etDate;
    private TextInputLayout etOffer;
    private TextInputEditText etDateSelect;
    private TextInputEditText etOfferSelect;
    private Date date;
    private String address;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggest_location);

        mToolbar = findViewById(R.id.suggestToolbar);
        this.setSupportActionBar(mToolbar);
//        mToolbar.setTitle("Suggest Meetup Location");

        ivSuggest = findViewById(R.id.ivSuggest);
        tvPlace = findViewById(R.id.tvPlace);
        etDate = findViewById(R.id.etDate);
        etOffer = findViewById(R.id.etOffer);
        etDateSelect = findViewById(R.id.etDateSelect);
        etOfferSelect = findViewById(R.id.etOfferSelect);


        user = FirebaseAuth.getInstance().getCurrentUser();
        Places.initialize(getApplicationContext(), "AIzaSyAOJDiBTvLbWl4kX80Dzs-eRE3YcFVlXlw");

        mapFragment = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map));
        if (mapFragment != null) {
            mapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap map) {
                    loadMap(map);
                }
            });
        }

        product = (Product) Parcels.unwrap(getIntent().getParcelableExtra(Product.class.getSimpleName()));

        etOfferSelect.setText("" + product.getPrice());

        etDateSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDatePickerDialog(view);
            }
        });

        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS));
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NotNull Place place) {
                LatLng placeLatLng = place.getLatLng();
                tvPlace.setText(place.getAddress());
                address = place.getAddress();
                focusLocation(placeLatLng.latitude, placeLatLng.longitude);
            }

            @Override
            public void onError(@NotNull Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        ivSuggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (tvPlace.getText().equals("Please select a meetup location")) {
                    Toast.makeText(SuggestLocationActivity.this, "Please select an address", Toast.LENGTH_SHORT).show();
                    return;
                } else if (getTextInputString(etOffer).equals("")) {
                    Toast.makeText(SuggestLocationActivity.this, "Please set an offer", Toast.LENGTH_SHORT).show();
                    return;
                } else if (getTextInputString(etDate).equals("")) {
                    Toast.makeText(SuggestLocationActivity.this, "Please select a date", Toast.LENGTH_SHORT).show();
                    return;
                }
                findOffer();
            }
        });
    }

    public static String getTextInputString(TextInputLayout id) {
        return id.getEditText().getText().toString().trim();
    }

    public void openDatePickerDialog(final View v) {
        Calendar cal = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, year, monthOfYear, dayOfMonth) -> {
                    String selectedDate = dayOfMonth + "/" + (monthOfYear + 1) + "/" + year;
                    switch (v.getId()) {
                        case R.id.etDateSelect:
                            Calendar temp = Calendar.getInstance();
                            temp.set(year, monthOfYear, dayOfMonth);
                            date = temp.getTime();
                            ((TextInputEditText)v).setText(selectedDate);
                            break;
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(cal.getTimeInMillis());
        datePickerDialog.show();
    }

    protected void loadMap(GoogleMap googleMap) {
        map = googleMap;
        if (map != null) {
            SuggestLocationActivityPermissionsDispatcher.getMyLocationWithPermissionCheck(this);
            SuggestLocationActivityPermissionsDispatcher.startLocationUpdatesWithPermissionCheck(this);
            getMyLocation();
        }
    }

    @SuppressWarnings({"MissingPermission"})
    @NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    void getMyLocation() {
        map.setMyLocationEnabled(true);
        map.getUiSettings().setMyLocationButtonEnabled(true);

        FusedLocationProviderClient locationClient = getFusedLocationProviderClient(this);
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    @SuppressLint("MissingPermission")
    @NeedsPermission({android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION})
    protected void startLocationUpdates() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        getFusedLocationProviderClient(this).requestLocationUpdates(mLocationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        onLocationChanged(locationResult.getLastLocation());
                    }
                },
                Looper.myLooper());
    }

    protected void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        mCurrentLocation = location;

//        autocompleteFragment.setLocationBias(RectangularBounds.newInstance(
//                new LatLng(-33.880490, 151.184363),
//                new LatLng(-33.858754, 151.229596)));

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + ", " +
                Double.toString(location.getLongitude());
        focusLocation(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    protected void focusLocation(double lat, double lon) {
        latLng = new LatLng(lat, lon);

        map.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        map.addMarker(markerOptions);

//        map.addCircle(new CircleOptions()
//                .center(latLng)
//                .radius(25)
//                .strokeColor(Color.parseColor("#039be5"))
//                .strokeWidth(3)
//                .fillColor(0x550000FF));

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 18);
        map.animateCamera(cameraUpdate);
    }

    private void updateLocationOffer(String offerId, double lat, double lon) {
        LoginActivity.db().collection("offers")
                .document(offerId)
                .update("location", new GeoPoint(lat, lon))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
        LoginActivity.db().collection("offers")
                .document(offerId)
                .update("offer", Integer.parseInt(getTextInputString(etOffer)))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
        LoginActivity.db().collection("offers")
                .document(offerId)
                .update("date", date)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
        LoginActivity.db().collection("offers")
                .document(offerId)
                .update("address", address)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error updating document", e);
                    }
                });
    }

    private void findOffer() {
        LoginActivity.db().collection("offers")
                .whereEqualTo("productId", product.getDocumentId())
                .whereEqualTo("buyerUid", user.getUid())
                .limit(1)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                updateLocationOffer(document.getId(), latLng.latitude, latLng.longitude);
                                break;
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}