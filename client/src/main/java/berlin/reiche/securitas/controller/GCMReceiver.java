package berlin.reiche.securitas.controller;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

public class GCMReceiver extends GCMBroadcastReceiver {

	@Override
	protected String getGCMIntentServiceClassName(Context context) {
		return "berlin.reiche.securitas.controller.GCMIntentService";
	}

}
