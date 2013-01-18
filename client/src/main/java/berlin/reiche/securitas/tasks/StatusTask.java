package berlin.reiche.securitas.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import com.google.android.gcm.GCMRegistrar;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.controller.MainActivity;
import berlin.reiche.securitas.util.HttpUtilities;

public class StatusTask extends AsyncTask<String, Void, HttpResponse> {

	public enum ServerStatus {
		IDLE, READY, RUNNING
	}

	private static final String TAG = StatusTask.class.getSimpleName();

	Activity activity;

	public StatusTask(Activity activity) {
		super();
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
			Log.e(TAG, "Get status failed, due to " + e.getMessage());
			return null;
		} finally {
			HttpUtilities.closeClient(client);
		}
	}

	@Override
	protected void onPostExecute(HttpResponse response) {

		MainActivity activity = (MainActivity) this.activity;
		if (response == null) {
			activity.unlockInterface();
			return;
		}

		try {
			String content = getString(response.getEntity().getContent());
			content = content.toUpperCase(Locale.US);
			ServerStatus status = ServerStatus.valueOf(content);
			switch (status) {
			case IDLE:
				if (GCMRegistrar.isRegisteredOnServer(activity)) {
					GCMRegistrar.setRegisteredOnServer(activity, false);
					activity.manageDeviceRegistration();
				}
				activity.unlockInterface();
				break;
			case READY:
				activity.unlockInterface();
				break;
			case RUNNING:
				Client.enableMotionDetection();
				break;
			}
		} catch (IOException e) {
			Log.e(TAG, "The stream of the response could not be created.");
			activity.unlockInterface();
		}
	}

	public static String getString(InputStream stream) {
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : null;
	}

}
