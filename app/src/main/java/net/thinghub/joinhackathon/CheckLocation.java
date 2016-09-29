package net.thinghub.joinhackathon;

import com.google.android.gms.location.Geofence;

import java.util.List;

/**
 * Created by diegoburgos on 29/09/2016.
 */

public class CheckLocation {
    List<Geofence> mGeofenceList;

    public CheckLocation(List<Geofence> mGeofenceList) {
        this.mGeofenceList = mGeofenceList;
    }

    public boolean checkIfLocationInside (Geofence location) {
        for (Geofence geof : mGeofenceList) {

        }
        return true;
    }
}
