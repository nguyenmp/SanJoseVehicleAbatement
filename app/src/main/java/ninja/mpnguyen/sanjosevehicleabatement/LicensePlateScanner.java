package ninja.mpnguyen.sanjosevehicleabatement;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import okhttp3.Request;


public class LicensePlateScanner extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private static final int PERMISSIONS_REQUEST_CODE = 3123;
    GoogleApiClient mGoogleApiClient = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_license_plate_scanner);
        initDate(this);
        setAdapter(this, R.id.state_spinner, R.array.states);
        setAdapter(this, R.id.color_spinner, R.array.color);
        setAdapter(this, R.id.make_spinner, R.array.make);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
    }

    private static void setAdapter(Activity activity, int view, int values) {
        Spinner statesSpinner = (Spinner) activity.findViewById(view);
        ArrayAdapter<CharSequence> statesAdapter =
                ArrayAdapter.createFromResource(
                        activity,
                        values,
                        android.R.layout.simple_spinner_item
                );
        statesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statesSpinner.setAdapter(statesAdapter);
    }

    private static void initDate(Activity activity) {
        TextView dateTextView = (TextView) activity.findViewById(R.id.date_text_view);
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshDate((TextView) v);
            }
        });
        dateTextView.callOnClick();
    }

    private static void refreshDate(TextView dateTextView) {
        String format = "M/d/yyyy h:mm:ss aa";
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        String date = formatter.format(new Date());
        dateTextView.setText(date);
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
        super.onStart();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // TODO: Update location
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST_CODE);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            System.out.println("No Permissions");
        } else {
            Location mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateLocation(mLastKnownLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int resultCode, String[] permissions, int[] grantResults) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location mLastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        updateLocation(mLastKnownLocation);
    }

    private void updateLocation(Location mLastKnownLocation) {
        if (mLastKnownLocation != null) {
            new AddressResolver(this).execute(mLastKnownLocation);
        } else {
            System.out.println("No known last location");
        }
    }

    private static class AddressResolver extends AsyncTask<Location, Void, String> {
        private final WeakReference<Activity> activityWeakReference;
        public AddressResolver(Activity activity) {
            super();
            activityWeakReference = new WeakReference<Activity>(activity);
        }

        @Override
        protected void onPostExecute(String address) {
            super.onPostExecute(address);
            Activity activity = activityWeakReference.get();
            if (activity != null) {
                TextView addressTextView = (TextView) activity.findViewById(R.id.location);
                assert addressTextView != null;
                addressTextView.setText(address);
            }
        }

        @Override
        protected String doInBackground(Location... params) {
            Location location = params[0];
            try {
                OkHttpClient client = new OkHttpClient();
                double lat = location.getLatitude();
                double lon = location.getLongitude();
                String API_KEY = "";
                String url = String.format(
                        "https://maps.googleapis.com/maps/api/geocode/json?latlng=%f,%f&key=%s",
                        lat,
                        lon,
                        API_KEY
                );
                Request request = new Request
                        .Builder()
                        .url(url)
                        .build();
                String address_json = client.newCall(request).execute().body().string();
                return new JSONObject(address_json)
                        .getJSONArray("results")
                        .getJSONObject(0)
                        .getString("formatted_address");
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // TODO: Why?
        System.out.println("Connection suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // TODO: Make a snack bar to show that we failed to get location information
        System.out.println("Connection failed");
    }
}
