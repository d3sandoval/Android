package net.desandoval.apps.imhere.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import net.desandoval.apps.imhere.locations.MovementLocationListener;

/**
 * Starts MovementLocationListener onStart
 */
public class StartAtBootReceiver extends BroadcastReceiver{
    @Override
    public void onReceive(Context context, Intent intent) {

        new MovementLocationListener(context);

    }
}
