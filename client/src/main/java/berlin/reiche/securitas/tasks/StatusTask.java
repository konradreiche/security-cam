package berlin.reiche.securitas.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.os.AsyncTask;
import android.util.Log;
import berlin.reiche.securitas.Action;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.ClientModel;
import berlin.reiche.securitas.ClientModel.State;
import berlin.reiche.securitas.Model;
import berlin.reiche.securitas.controller.Controller;
import berlin.reiche.securitas.controller.WaitState;
import berlin.reiche.securitas.util.HttpUtilities;

public class StatusTask extends AsyncTask<String, Void, HttpResponse> {

	public enum ServerStatus {
		IDLE, READY, RUNNING
	}

	private static final String TAG = StatusTask.class.getSimpleName();

	ClientModel model;

	Controller<State> controller;

	public StatusTask(Model<State> model, Controller<State> controller) {
		super();
		this.model = (ClientModel) model;
		this.controller = controller;
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

		int what;
		if (response == null) {
			what = Action.UNLOCK_INTERFACE.code;
			controller.notifyOutboxHandlers(what, false);
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
				what = Action.UNLOCK_INTERFACE.code;
				controller.notifyOutboxHandlers(what, false);
				break;
			case READY:
				what = Action.UNLOCK_INTERFACE.code;
				controller.notifyOutboxHandlers(what, false);
				break;
			case RUNNING:
				controller.setState(new WaitState(controller));
				what = Action.SET_DETECTION_ACTIVE.code;
				controller.notifyOutboxHandlers(what);
				break;
			}
		} catch (IOException e) {
			Log.e(TAG, "The stream of the response could not be created.");
			what = Action.UNLOCK_INTERFACE.code;
			controller.notifyOutboxHandlers(what, false);
		}
	}

	public static String getString(InputStream stream) {
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : null;
	}

}
