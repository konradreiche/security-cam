package berlin.reiche.securitas.tasks;

import static berlin.reiche.securitas.tasks.DetectionRequest.DetectionCommand.START;
import static org.apache.http.HttpStatus.SC_CONFLICT;
import static org.apache.http.HttpStatus.SC_OK;
import static org.apache.http.HttpStatus.SC_UNAUTHORIZED;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
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
import berlin.reiche.securitas.controller.IdleState;
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
				if (state == State.DETECTING) {
					what = Action.SET_DETECTION_ACTIVE.code;
				} else {
					what = Action.SET_DETECTION_INACTICE.code;
				}
				controller.notifyOutboxHandlers(what);
				break;
			case SC_UNAUTHORIZED:
				model.setStatus("Unauthorized request, check authentication data");
				state = model.onRequestFail();
				controller.setState(new IdleState(controller));
				if (state == State.DETECTING) {
					what = Action.SET_DETECTION_ACTIVE.code;
				} else {
					what = Action.SET_DETECTION_INACTICE.code;
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
				what = Action.SET_STATUS_TEXT.code;
				controller.notifyOutboxHandlers(what, status);

				state = model.onRequestFail();
				controller.setState(new IdleState(controller));
				if (state == State.DETECTING) {
					what = Action.SET_DETECTION_ACTIVE.code;
				} else {
					what = Action.SET_DETECTION_INACTICE.code;
				}
				controller.notifyOutboxHandlers(what);
			}
		}
	}
}
