package berlin.reiche.securitas.activities;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.R;
import berlin.reiche.securitas.util.NotificationDialog;

/**
 * Settings activity for configuring the server endpoint.
 * 
 * @author Konrad Reiche
 * 
 */
/**
 * @author Konrad
 *
 */
/**
 * @author Konrad
 * 
 */
public class SettingsActivity extends PreferenceActivity {

	/**
	 * Key for the host preference.
	 */
	public static final String HOST = "pref_host";

	/**
	 * Key for the port preference.
	 */
	public static final String PORT = "pref_port";

	/**
	 * Key for the user preference.
	 */
	public static final String USER = "pref_user";

	/**
	 * Key for the password preference.
	 */
	public static final String PASSWORD = "pref_password";

	/**
	 * Key for the GCM Sender ID preference.
	 */
	public static final String GCM_SENDER_ID = "pref_gcm_sender_id";

	/**
	 * Key for displaying instruction boolean.
	 */
	public static final String DISPLAY_INSTRUCTION = "displayInstruction";

	/**
	 * Tag for logging.
	 */
	private static final String TAG = SettingsActivity.class.getSimpleName();

	/**
	 * Creates the preference screen and issues a notification dialog based on
	 * the intent that started this activity.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		if (getIntent().getExtras().getBoolean(DISPLAY_INSTRUCTION)) {
			NotificationDialog.create(this, R.string.configuration_hint).show();
		}
	}

	/**
	 * When the user hits the back button the settings need to be updated and
	 * propagated.
	 */
	@Override
	public void onPause() {
		Log.i(TAG, "onPause");
		super.onPause();
		Client.updateSettings(this);
		Client.getController().restoreClientState();
	}

}