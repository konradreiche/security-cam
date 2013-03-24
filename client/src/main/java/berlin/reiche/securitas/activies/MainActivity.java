package berlin.reiche.securitas.activies;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.R;
import berlin.reiche.securitas.Settings;
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.Protocol;
import berlin.reiche.securitas.model.ClientModel.State;
import berlin.reiche.securitas.util.NotificationDialog;

import com.google.android.gcm.GCMRegistrar;

/**
 * Activity used to remote control the server and display snapshots. This class
 * implements the {@link Callback} interface in order to handle requests sent
 * from the controller for updating the interface.
 * 
 * @author Konrad Reiche
 * 
 */
public class MainActivity extends Activity implements Callback {

	/**
	 * Tag for logging.
	 */
	private static String TAG = MainActivity.class.getSimpleName();

	/**
	 * This {@link ImageView} will display the latest snapshot or the snapshot
	 * that triggered an motion detection.
	 */
	public ImageView snapshot;

	/**
	 * The same button is used for starting and stopping the motion detection on
	 * the backend.
	 */
	public Button detectionToggle;

	/**
	 * The layout for this activity.
	 */
	private RelativeLayout layout;

	/**
	 * Separate layout for the layout.
	 */
	public RelativeLayout snapshotArea;

	/**
	 * Progress bar for displaying progress.
	 */
	public ProgressBar progress;

	/**
	 * Simply a headline.
	 */
	public TextView headline;

	/**
	 * Simply a subtitle to the headline.
	 */
	public TextView subtitle;

	/**
	 * The inbox handler takes requests and delegates them to the current
	 * controller state object.
	 */
	private Handler handler;

	/**
	 * Whether all components are initialized and can be referenced.
	 */
	private boolean initialized;

	/**
	 * This is an activity state variable used for saving and restoring the
	 * state without accessing the model.
	 */
	private boolean detecting;

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

		Client.setModel(new ClientModel());
		Client.setController(new ClientController(Client.getModel()));

		Client.getController().addOutboxHandler(new Handler(this));
		handler = Client.getController().getInboxHandler();

		initializeReferences();
		updateSettings();

		// restore image due to orientation change
		if (getLastNonConfigurationInstance() != null) {
			Bitmap bitmap = (Bitmap) getLastNonConfigurationInstance();
			snapshot.setImageBitmap(bitmap);
		}

		if (savedInstanceState != null) {
			// activity was put to the background, restore
			Log.i(TAG, "Restore saved instance state");
			String detectingKey = getString(R.string.is_detection_active_key);
			String snapshotKey = getString(R.string.snapshot_key);
			detecting = savedInstanceState.getBoolean(detectingKey);
			Bitmap bitmap = savedInstanceState.getParcelable(snapshotKey);
			snapshot.setImageBitmap(bitmap);
		} else if (getIntent().getExtras() == null) {
			// activity was destroyed, restore based on server state
			lockInterface();
			handler.sendEmptyMessage(Protocol.RESTORE_CLIENT_STATE.code);
			handler.sendEmptyMessage(Protocol.DOWNLOAD_LATEST_SNAPSHOT.code);
		} else {
			// activity was destroyed, activity started through notification
			lockInterface();
			handler.sendEmptyMessage(Protocol.RESTORE_CLIENT_STATE.code);
			int what = Protocol.DOWNLOAD_MOTION_SNAPSHOT.code;
			String filename = getIntent().getExtras().getString("filename");
			Message message = Message.obtain(handler, what, filename);
			handler.sendMessage(message);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		updateSettings();

		int orientation = getResources().getConfiguration().orientation;
		if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
			getWindow().setFlags(FLAG_FULLSCREEN, FLAG_FULLSCREEN);
			LayoutParams params = new LayoutParams(MATCH_PARENT, MATCH_PARENT);

			layout.setBackgroundDrawable(null);
			snapshotArea.setLayoutParams(params);
			headline.setVisibility(View.GONE);
			subtitle.setVisibility(View.GONE);
		}

		boolean notification = getIntent().getExtras() != null;
		if (detecting) {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			detectionToggle.setText(R.string.button_stop_detection);
			detecting = true;
			if (!notification) {
				handler.sendEmptyMessage(Protocol.DOWNLOAD_LATEST_SNAPSHOT.code);
			}
		}

		if (notification) {
			String filename = getIntent().getExtras().getString("filename");
			int what = Protocol.DOWNLOAD_MOTION_SNAPSHOT.code;
			Message message = Message.obtain(handler, what, filename);
			handler.sendMessage(message);
		}
	}

	/**
	 * Disposes the controller so the handler looper is shut down. Makes sure
	 * that that this method does not fail and delegates to the super method.
	 * 
	 * @see android.app.Activity#onDestroy()
	 */
	@Override
	public void onDestroy() {
		try {
			Client.getController().dispose();
		} catch (Throwable t) {
			Log.e(TAG, "Failed to destroy the controller", t);
		} finally {
			super.onDestroy();
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

	/**
	 * Saves all necessary information to restore the activity state.
	 * 
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		String detectingKey = getString(R.string.is_detection_active_key);
		String snapshotKey = getString(R.string.snapshot_key);
		savedInstanceState.putBoolean(detectingKey, detecting);

		if (snapshot.getDrawable() != null) {
			savedInstanceState.putParcelable(snapshotKey,
					((BitmapDrawable) snapshot.getDrawable()).getBitmap());
		}
	}

	/**
	 * Called when the screen orientation changes and stores the image so it
	 * does not need to be retrieved once more.
	 * 
	 * @see android.app.Activity#onRetainNonConfigurationInstance()
	 */
	@Override
	public Object onRetainNonConfigurationInstance() {
		super.onRetainNonConfigurationInstance();
		return ((BitmapDrawable) snapshot.getDrawable()).getBitmap();
	}

	/**
	 * For a fast access of the components this method initializes all the
	 * references by finding their view elements through their IDs.
	 */
	public void initializeReferences() {
		if (!initialized) {
			layout = (RelativeLayout) findViewById(R.id.layout);
			snapshot = (ImageView) findViewById(R.id.snapshot);
			detectionToggle = (Button) findViewById(R.id.detection_toggle);
			progress = (ProgressBar) findViewById(R.id.progress_bar);
			snapshotArea = (RelativeLayout) findViewById(R.id.snapshot_area);
			headline = (TextView) findViewById(R.id.headline);
			subtitle = (TextView) findViewById(R.id.subtitle);
			initialized = true;
		}
	}

	private void updateSettings() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		String host = settings.getString(SettingsActivity.HOST, null);
		String port = settings.getString(SettingsActivity.PORT, null);
		String username = settings.getString(SettingsActivity.USER, null);
		String password = settings.getString(SettingsActivity.PASSWORD, null);
		String id = settings.getString(SettingsActivity.GCM_SENDER_ID, null);

		if (!isConfigured()) {
			startSettingsActivity(true);
		} else {
			Client.setSettings(new Settings(host, port, username, password, id));
			Log.i(TAG, "Updated endpoint to " + Client.getEndpoint());
			manageDeviceRegistration();

		}
	}

	private void startSettingsActivity(boolean forceConfiguration) {
		Intent intent = new Intent(this, SettingsActivity.class);
		intent.putExtra(SettingsActivity.DISPLAY_INSTRUCTION,
				forceConfiguration);
		startActivity(intent);
	}

	private boolean isConfigured() {
		SharedPreferences settings = PreferenceManager
				.getDefaultSharedPreferences(this);
		String host = settings.getString(SettingsActivity.HOST, "");
		String port = settings.getString(SettingsActivity.PORT, "");
		String username = settings.getString(SettingsActivity.USER, "");
		String password = settings.getString(SettingsActivity.PASSWORD, "");
		String gcmSenderId = settings.getString(SettingsActivity.GCM_SENDER_ID,
				"");

		boolean configured = !host.equals("") && !port.equals("")
				&& !username.equals("") && !password.equals("")
				&& !gcmSenderId.equals("");

		return configured;
	}

	/**
	 * Toggles the motion detection based on the current state. This method must
	 * only be invoked when the state is {@link State#IDLE} or
	 * {@link State#DETECTING}.
	 * 
	 * @param view
	 *            the view that was clicked.
	 */
	public void toggleMotionDetection(View view) {
		lockInterface();
		if (detecting) {
			handler.sendEmptyMessage(Protocol.STOP_DETECTION.code);
		} else {
			handler.sendEmptyMessage(Protocol.START_DETECTION.code);
		}
	}

	public void lockInterface() {
		detectionToggle.setEnabled(false);
		snapshot.setEnabled(false);
		ProgressBar progress = (ProgressBar) findViewById(R.id.progress_bar);
		progress.setVisibility(View.VISIBLE);
		snapshot.setVisibility(View.INVISIBLE);
	}

	/**
	 * Unlocks the interface because a request was carried out, independent
	 * whether it was successful or not.
	 * 
	 * Only make {@link ImageView} for snapshot visible again, if the motion
	 * detection is currently active.
	 */
	public void unlockInterface(boolean detecting) {
		detectionToggle.setEnabled(true);
		snapshot.setEnabled(true);
		progress.setVisibility(View.INVISIBLE);

		if (detecting) {
			snapshot.setVisibility(View.VISIBLE);
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
			String senderId = Client.getSettings().getGcmSenderId();
			GCMRegistrar.register(this, senderId);
		} else if (!GCMRegistrar.isRegisteredOnServer(this)) {
			handler.sendMessage(Message.obtain(handler,
					Protocol.REGISTER_DEVICE.code, id));
		}
	}

	// TODO: make use of it
	public boolean isNetworkAvailable() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = cm.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		}
		return false;
	}

	public void refreshSnapshot(View view) {
		lockInterface();
		handler.sendEmptyMessage(Protocol.DOWNLOAD_LATEST_SNAPSHOT.code);
	}

	@Override
	public boolean handleMessage(Message message) {

		Action action = Action.valueOf(message.what);
		switch (action) {
		case LOCK_INTERFACE:
			lockInterface();
			break;
		case UNLOCK_INTERFACE:
			unlockInterface((Boolean) message.obj);
			break;
		case SET_DETECTION_MODE:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
			detectionToggle.setText(R.string.button_stop_detection);
			detecting = true;
			break;
		case SET_IDLE_MODE:
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
			detectionToggle.setText(R.string.button_start_detection);
			detecting = false;
			snapshot.setVisibility(ImageView.INVISIBLE);
			unlockInterface(false);
			break;
		case SET_REGISTERED_ON_SERVER:
			GCMRegistrar.setRegisteredOnServer(this, (Boolean) message.obj);
			break;
		case SET_SNAPSHOT:
			snapshot.setImageBitmap((Bitmap) message.obj);
			break;
		case ALERT_PROBLEM:
			NotificationDialog.create(this, message.obj.toString()).show();
			break;
		default:
			Log.e(TAG, "Retrieved illegal action: " + action);
			throw new IllegalStateException();
		}

		return true;
	}
}
