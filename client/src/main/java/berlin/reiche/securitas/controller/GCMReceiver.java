package berlin.reiche.securitas.controller;

import android.content.Context;

import com.google.android.gcm.GCMBroadcastReceiver;

/**
 * Custom GCM Receiver. This class is necessary so that the GCM Intent Service
 * and other related GCM classes can be stored in another package than the
 * default application package.
 * 
 * @author Konrad Reiche
 * 
 */
public class GCMReceiver extends GCMBroadcastReceiver {

	/**
	 * This methods needs to be overridden so the context can find the intent
	 * service. Class.getName() is used in order to harden this implementation
	 * against refactoring.
	 */
	@Override
	protected String getGCMIntentServiceClassName(Context context) {
		return GCMIntentService.class.getName();
	}

}
