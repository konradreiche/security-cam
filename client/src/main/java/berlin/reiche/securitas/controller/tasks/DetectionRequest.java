package berlin.reiche.securitas.controller.tasks;

import static berlin.reiche.securitas.controller.tasks.DetectionRequest.DetectionCommand.START;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.activies.Action;
import berlin.reiche.securitas.controller.Controller;
import berlin.reiche.securitas.controller.DetectionState;
import berlin.reiche.securitas.controller.IdleState;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.Model;
import berlin.reiche.securitas.model.Protocol;
import berlin.reiche.securitas.model.ClientModel.State;
import berlin.reiche.securitas.util.HttpUtilities;

public class DetectionRequest extends AsyncTask<String, Void, HttpResponse> {

	public enum DetectionCommand {
		START, STOP
	};

	IOException exception;

	DetectionCommand command;

	ClientModel model;

	Controller<State> controller;

	private static String TAG = DetectionRequest.class.getSimpleName();

	public DetectionRequest(Model<State> model, Controller<State> controller,
			DetectionCommand command) {
		this.model = (ClientModel) model;
		this.controller = controller;
		this.command = command;
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

		if (exception != null && response == null) {
			model.setStatus(exception.getMessage());
			model.onRequestFail();
			controller.setState(new IdleState(controller));
		} else if (response == null) {
			Log.e(TAG, "Response is null without an exception. "
					+ "The endpoint probably ran into a problem.");
		} else {
			int what;
			State state;
			int code = response.getStatusLine().getStatusCode();
			switch (code) {
			case SC_OK:
				state = (command == START) ? State.DETECTING : State.IDLE;
				model.setState(state);
				boolean detecting = state == State.DETECTING;
				if (detecting) {
					controller.setState(new DetectionState(controller));
					what = Action.SET_DETECTION_MODE.code;
					Handler handler = controller.getInboxHandler();
					handler.sendEmptyMessage(Protocol.DOWNLOAD_LATEST_SNAPSHOT.code);
				} else {
					what = Action.SET_IDLE_MODE.code;
				}
				controller.notifyOutboxHandlers(what);
				break;
			case SC_UNAUTHORIZED:
				model.setStatus("Unauthorized request, check authentication data");
				state = model.onRequestFail();
				controller.setState(new IdleState(controller));
				if (state == State.DETECTING) {
					what = Action.SET_DETECTION_MODE.code;
				} else {
					what = Action.SET_IDLE_MODE.code;
				}
				controller.notifyOutboxHandlers(what);
				break;
			case SC_CONFLICT:
				model.setRegisteredOnServer(false);
				what = Action.SET_REGISTERED_ON_SERVER.code;
				controller.notifyOutboxHandlers(what, false);
				break;
			default:
				StatusLine statusLine = response.getStatusLine();
				String status = statusLine.getStatusCode() + " ";
				status += statusLine.getReasonPhrase();
				model.setStatus(status);
				what = Action.ALERT_PROBLEM.code;
				controller.notifyOutboxHandlers(what, status);

				state = model.onRequestFail();
				controller.setState(new IdleState(controller));
				if (state == State.DETECTING) {
					what = Action.SET_DETECTION_MODE.code;
				} else {
					what = Action.SET_IDLE_MODE.code;
				}
				controller.notifyOutboxHandlers(what);
			}
		}
	}
}
