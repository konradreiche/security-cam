package berlin.reiche.securitas.controller.tasks;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.os.AsyncTask;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.activies.Action;
import berlin.reiche.securitas.controller.Controller;
import berlin.reiche.securitas.controller.DetectionState;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.Model;
import berlin.reiche.securitas.model.Protocol;
import berlin.reiche.securitas.model.ClientModel.State;
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
			String problem = "Get status failed, due to " + e.getMessage();
			Log.e(TAG, problem);
			int what = Action.ALERT_PROBLEM.code;
			controller.notifyOutboxHandlers(what, problem);
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
			if (content == null) {
				Log.i(TAG, "Status response is null, retry.");
				String uri = Client.getEndpoint();
				uri += Protocol.RESTORE_CLIENT_STATE.operation;
				new StatusTask(model, controller).execute(uri);
				return;
			}

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
				model.setState(State.DETECTING);
				controller.setState(new DetectionState(controller));
				what = Action.SET_DETECTION_MODE.code;
				controller.notifyOutboxHandlers(what);
				break;
			}
		} catch (IOException e) {
			String problem = "The stream of the response could not be created.";
			Log.e(TAG, problem);
			what = Action.UNLOCK_INTERFACE.code;
			controller.notifyOutboxHandlers(what, false);
			what = Action.ALERT_PROBLEM.code;
			controller.notifyOutboxHandlers(what, problem);
		}
	}

	public static String getString(InputStream stream) {
		java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next() : null;
	}

}
