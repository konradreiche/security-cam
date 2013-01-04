package berlin.reiche.securitas.tasks;

import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.MainActivity;
import berlin.reiche.securitas.R;
import berlin.reiche.securitas.util.HttpUtilities;

import com.google.android.gcm.GCMRegistrar;

public class DetectionRequest extends AsyncTask<String, Void, HttpResponse> {

	IOException exception;

	Activity activity;

	private static String TAG = DetectionRequest.class.getSimpleName();

	public DetectionRequest(Activity activity) {
		this.activity = activity;
	}

	@Override
	protected HttpResponse doInBackground(String... uri) {

		HttpClient client = HttpUtilities.newHttpClient();
		HttpGet get = new HttpGet(uri[0]);
		HttpUtilities.setAuthorization(get, Client.getSettings());

		try {
			return client.execute(get);
		} catch (IOException e) {
			exception = e;
		} finally {
			HttpUtilities.closeClient(client);
		}
		return null;
	}

	protected void onPostExecute(HttpResponse response) {

		Button button = (Button) activity
				.findViewById(R.id.toggle_motion_detection);
		TextView text = (TextView) activity.findViewById(R.id.connection_error);
		ProgressBar progress = (ProgressBar) activity
				.findViewById(R.id.progress_bar);

		if (exception != null && response == null) {
			text.setText(exception.getMessage());
		} else if (response == null) {
			Log.e(TAG, "Response is null without an exception. "
					+ "The endpoint probably ran into a problem.");
		} else {

			switch (response.getStatusLine().getStatusCode()) {
			case SC_OK:
				Client.downloadLatestSnapshot(activity, (ImageView) activity.findViewById(R.id.snapshotView));
				String current = button.getText().toString();
				button.setText(current.equals("Start") ? "Stop" : "Start");
				Client.toggleDetectionActive();
				break;
			case SC_UNAUTHORIZED:
				text.setText("Unauthorized request, check authentication data");
				break;
			case SC_CONFLICT:
				GCMRegistrar.setRegisteredOnServer(activity, false);
				((MainActivity) activity).manageDeviceRegistration();
				Client.toggleMotionDetection(activity);
				return;
			default:
				StatusLine status = response.getStatusLine();
				text.setText(String.valueOf(status.getStatusCode()) + " "
						+ status.getReasonPhrase());
			}
		}

		((MainActivity)activity).unlockUI();
	}
}
