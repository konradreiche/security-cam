package berlin.reiche.securitas.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.os.AsyncTask;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.ClientModel;
import berlin.reiche.securitas.Model;
import berlin.reiche.securitas.util.HttpUtilities;

public class StatusTask extends AsyncTask<String, Void, HttpResponse> {

	public enum ServerStatus {
		IDLE, READY, RUNNING
	}

	private static final String TAG = StatusTask.class.getSimpleName();

	ClientModel model;

	public StatusTask(Model<ClientModel.State> model) {
		super();
		this.model = (ClientModel)model;
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

		if (response == null) {
			// TODO: introduce model state for locked/unlocked interface and set
			// the model state to unlock here.
			return;
		}

		try {
			String content = getString(response.getEntity().getContent());
			content = content.toUpperCase(Locale.US);
			ServerStatus status = ServerStatus.valueOf(content);
			switch (status) {
			case IDLE:
				if (model.isRegisteredOnServer()) {
					model.setRegisteredOnServer(false);
				}
				// unlockInterface()
				break;
			case READY:
				// unlockInterface()
				break;
			case RUNNING:
				Client.enableMotionDetection();
				break;
			}
		} catch (IOException e) {
			Log.e(TAG, "The stream of the response could not be created.");
			// unlockInterface()
		}
	}

	public static String getString(InputStream stream) {
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : null;
	}

}
