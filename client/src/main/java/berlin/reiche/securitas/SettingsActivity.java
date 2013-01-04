package berlin.reiche.securitas;

import berlin.reiche.securitas.util.NotificationDialog;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;

public class SettingsActivity extends PreferenceActivity {

	public static final String HOST = "pref_host";
	public static final String PORT = "pref_port";
	public static final String USER = "pref_user";
	public static final String PASSWORD = "pref_password";

	public static final String DISPLAY_INSTRUCTION = "displayInstruction";
	private static final String TAG = SettingsActivity.class.getSimpleName();

	// private static final String TAG = SettingsActivity.class.getSimpleName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		if (getIntent().getExtras().getBoolean(DISPLAY_INSTRUCTION)) {
			NotificationDialog.create(this, R.string.configuration_hint).show();
		}
	}

}