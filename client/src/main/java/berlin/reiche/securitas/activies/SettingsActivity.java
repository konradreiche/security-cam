package berlin.reiche.securitas.activies;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.R;
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.util.NotificationDialog;

import com.google.android.gcm.GCMRegistrar;

public class SettingsActivity extends PreferenceActivity {

	public static final String HOST = "pref_host";
	public static final String PORT = "pref_port";
	public static final String USER = "pref_user";
	public static final String PASSWORD = "pref_password";
	public static final String GCM_SENDER_ID = "pref_gcm_sender_id";

	public static final String DISPLAY_INSTRUCTION = "displayInstruction";
	private static final String TAG = SettingsActivity.class.getSimpleName();
	
	ClientController controller;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		controller = Client.getController();
		if (getIntent().getExtras().getBoolean(DISPLAY_INSTRUCTION)) {
			NotificationDialog.create(this, R.string.configuration_hint).show();
		}
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Client.updateSettings(this);
		manageDeviceRegistration();
	}
	
	/**
	 * Registers the device on the GCM service. If the device is already
	 * registered the cached registration ID will be used.
	 */
	public void manageDeviceRegistration() {
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		String id = GCMRegistrar.getRegistrationId(this);
		if (id.equals("")) {
			Log.i(TAG, "No device id yet, issue registration indent.");
			String senderId = Client.getSettings().getGcmSenderId();
			GCMRegistrar.register(this, senderId);
		} else if (!GCMRegistrar.isRegisteredOnServer(this)) {
			controller.registerDevice(id);
		}
	}
	

}