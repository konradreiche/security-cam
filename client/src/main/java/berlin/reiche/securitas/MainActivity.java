package berlin.reiche.securitas;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import berlin.reiche.securitas.util.Settings;

import com.google.android.gcm.GCMRegistrar;

public class MainActivity extends Activity {

	private static String TAG = MainActivity.class.getSimpleName();

	static final String GCM_SENDER_ID = "958926895848";

	public ImageView snapshot;

	public Button detectionToggle;

	public TextView errors;

	public ProgressBar progress;

	private SharedPreferences settings;

	/**
	 * Whether all components are initialized and can be referenced.
	 */
	private boolean initialized;

	/**
	 * Whether the motion detection on the endpoint is running.
	 */

	/**
	 * Called when the activity is first created.
	 * 
	 * @param savedInstanceState
	 *            If the activity is being re-initialized after previously being
	 *            shut down then this Bundle contains the data it most recently
	 *            supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it
	 *            is null.</b>
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		setContentView(R.layout.main);
		initialize();
		updateSettings();
		if (isConfigured()) {
			Client.retrieveServerStatus(this);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		updateSettings();
		GCMIntentService.resetMotionsDetected(this);
	}

	public void initialize() {
		if (!initialized) {
			snapshot = (ImageView) findViewById(R.id.snapshot);
			detectionToggle = (Button) findViewById(R.id.detection_toggle);
			errors = (TextView) findViewById(R.id.errors);
			progress = (ProgressBar) findViewById(R.id.progress_bar);
			initialized = true;
		}
	}

	private void updateSettings() {
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		String host = settings.getString(SettingsActivity.HOST, null);
		String port = settings.getString(SettingsActivity.PORT, null);
		String username = settings.getString(SettingsActivity.USER, null);
		String password = settings.getString(SettingsActivity.PASSWORD, null);

		if (!isConfigured()) {
			startSettingsActivity(true);
		} else {
			Client.endpoint = "http://" + host + ":" + port;
			Client.settings = new Settings(host, port, username, password);
			Log.i(TAG, "Updated endpoint to " + Client.endpoint);
			manageDeviceRegistration();
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startSettingsActivity(false);
			break;
		}
		return true;
	}

	private void startSettingsActivity(boolean forceConfiguration) {
		Intent intent = new Intent(this, SettingsActivity.class);
		intent.putExtra(SettingsActivity.DISPLAY_INSTRUCTION,
				forceConfiguration);
		startActivity(intent);
	}

	private boolean isConfigured() {
		settings = PreferenceManager.getDefaultSharedPreferences(this);
		String host = settings.getString(SettingsActivity.HOST, "");
		String port = settings.getString(SettingsActivity.PORT, "");
		String username = settings.getString(SettingsActivity.USER, "");
		String password = settings.getString(SettingsActivity.PASSWORD, "");

		boolean configured = !host.equals("") && !port.equals("")
				&& !username.equals("") && !password.equals("");
		return configured;
	}

	public void toggleMotionDetection(View view) {
		errors.setText("");
		lockUI();
		Client.toggleMotionDetection(this);
	}

	public void refreshSnapshot(View view) {
		lockUI();
		Client.downloadLatestSnapshot(this, snapshot);
	}

	public void lockUI() {
		detectionToggle.setEnabled(false);
		snapshot.setEnabled(false);
		snapshot.setVisibility(View.INVISIBLE);
		ProgressBar progress = (ProgressBar) findViewById(R.id.progress_bar);
		progress.setVisibility(View.VISIBLE);
	}

	public void unlockUI() {
		detectionToggle.setEnabled(true);
		snapshot.setEnabled(true);
		snapshot.setVisibility(View.VISIBLE);
		progress.setVisibility(View.INVISIBLE);
	}

	public void toggleButtonText() {
		String current = detectionToggle.getText().toString();
		if (current.equals(getString(R.string.button_start_detection))) {
			detectionToggle.setText(R.string.button_stop_detection);
		} else {
			detectionToggle.setText(R.string.button_start_detection);
		}
	}

	/**
	 * Registers the device on the GCM service. If the device is already
	 * registered the cached registration ID will be used.
	 */
	public void manageDeviceRegistration() {
		GCMRegistrar.checkDevice(this);
		GCMRegistrar.checkManifest(this);
		final String id = GCMRegistrar.getRegistrationId(this);
		if (id.equals("")) {
			Log.i(TAG, "No device id yet, issue registration indent.");
			GCMRegistrar.register(this, GCM_SENDER_ID);
		} else if (!GCMRegistrar.isRegisteredOnServer(this)) {
			Client.registerDevice(id, this);
		}
	}

	public boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

}
