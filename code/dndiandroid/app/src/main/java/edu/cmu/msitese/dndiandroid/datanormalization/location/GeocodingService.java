package edu.cmu.msitese.dndiandroid.datanormalization.location;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.IntDef;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;

import com.bezirk.middleware.Bezirk;
import com.bezirk.middleware.addressing.ZirkEndPoint;
import com.bezirk.middleware.android.BezirkMiddleware;
import com.bezirk.middleware.messages.Event;
import com.bezirk.middleware.messages.EventSet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.cmu.msitese.dndiandroid.R;
import edu.cmu.msitese.dndiandroid.Utils;
import edu.cmu.msitese.dndiandroid.datagathering.gps.LocationDataService;
import edu.cmu.msitese.dndiandroid.event.CommandEvent;
import edu.cmu.msitese.dndiandroid.event.RawData;
import edu.cmu.msitese.dndiandroid.event.RawDataEvent;

import static edu.cmu.msitese.dndiandroid.Utils.getLocationStringFromJSONRaw;
//TODO: Add Check for NetworkInfo and ConnectionManager

public class GeocodingService extends Service {

    private static final String TAG = "GeocodingZirk";
    private final IBinder mBinder = new GeocodingServiceBinder();
    private String errorMessage = "";

    private Location mLocation;

    private Bezirk bezirk;
    final EventSet eventSet = new EventSet(RawDataEvent.class);


    public GeocodingService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class GeocodingServiceBinder extends Binder {
        public GeocodingService getService() {
            return GeocodingService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        bezirk= BezirkMiddleware.registerZirk("GeocodingZirk");
        bezirkListener(eventSet);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    private void getGeoCodeAddress() {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        // Address found using the Geocoder.
        List<Address> addresses = null;

        if(mLocation != null){
            try {
                addresses = geocoder.getFromLocation(
                        mLocation.getLatitude(),
                        mLocation.getLongitude(),
                        1);
            } catch (IOException ioException) {
                // Catch network or other I/O problems.
                errorMessage = getString(R.string.service_not_available);
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                // Catch invalid latitude or longitude values.
                errorMessage = getString(R.string.invalid_lat_long_used);
                Log.e(TAG, errorMessage + ". " + "Latitude = " + mLocation.getLatitude() + ", Longitude = " + mLocation.getLongitude(), illegalArgumentException);
            }
        }


        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = getString(R.string.no_address_found);
                Log.e(TAG, errorMessage);
            }
        } else {
            Address address = addresses.get(0);
            Log.i(TAG, "(THEZIRKS) Address: " + address.getAddressLine(0) + ", " + address.getAddressLine(1) + ", " +
                    address.getAdminArea());
            ArrayList<String> addressFragments = new ArrayList<>();

            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
//            Log.i(TAG, getString(R.string.address_found));
        }
    }

    //Listen to bezirk event: RawDataEvent
    private void bezirkListener(EventSet eventSet) {
        eventSet.setEventReceiver(new EventSet.EventReceiver() {

            @Override
            public void receiveEvent(Event event, ZirkEndPoint zirkEndPoint) {
//                Log.i(TAG, this.getClass().getName() + ":: received " );
                final RawDataEvent rawDataEvent = (RawDataEvent) event;

                RawData data = rawDataEvent.getRawDataArray().get(0);
                if(data.getLocation() != null){
                    mLocation = getLocationStringFromJSONRaw(data.getLocation());
                    if(mLocation != null){
                        Log.i(TAG, "Received Location:"+mLocation.getLatitude()+ "," + mLocation.getLongitude());
                        checkNetworkCallGeoCoder();
                    }
                }
            }
        });
        bezirk.subscribe(eventSet);
    }

    private void checkNetworkCallGeoCoder() {
        ConnectivityManager connManager = (ConnectivityManager)
                getBaseContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nwInfo = connManager.getActiveNetworkInfo();
        if (nwInfo != null) {
            if (nwInfo.getType() == (ConnectivityManager.TYPE_WIFI ) ||
                    nwInfo.getType() == ( ConnectivityManager.TYPE_MOBILE)) {
                getGeoCodeAddress();
            }
        }
        else {
            Log.i(TAG, "No Network Available");
        }
    }

    @VisibleForTesting(otherwise = VisibleForTesting.NONE)
    public String getLatLong(){
        return mLocation.getLongitude() + ":" + mLocation.getLatitude();
    }
}
