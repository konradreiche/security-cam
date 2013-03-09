package berlin.reiche.securitas.controller.tasks;

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import berlin.reiche.securitas.Action;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.ClientModel;
import berlin.reiche.securitas.ClientModel.State;
import berlin.reiche.securitas.Model;
import berlin.reiche.securitas.controller.Controller;
import berlin.reiche.securitas.util.FlushedInputStream;
import berlin.reiche.securitas.util.HttpUtilities;

public class BitmapDownloadTask extends AsyncTask<String, Void, Bitmap> {

	IOException exception;

	ClientModel model;

	Controller<State> controller;

	public BitmapDownloadTask(Model<State> model, Controller<State> controller) {
		super();
		this.model = (ClientModel) model;
		this.controller = controller;
	}

	@Override
	protected Bitmap doInBackground(String... url) {

		HttpClient client = HttpUtilities.newHttpClient();
		HttpGet get = new HttpGet(url[0]);
		HttpUtilities.setAuthorization(get, Client.getSettings());

		try {
			HttpResponse response = client.execute(get);
			HttpEntity entity = response.getEntity();
			InputStream inputStream = entity.getContent();
			inputStream = new FlushedInputStream(inputStream);
			return BitmapFactory.decodeStream(inputStream);
		} catch (IOException e) {
			exception = e;
		} finally {
			HttpUtilities.closeClient(client);
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		model.setSnapshot(result);
		controller.notifyOutboxHandlers(Action.SET_SNAPSHOT.code, result);
		controller.notifyOutboxHandlers(Action.UNLOCK_INTERFACE.code, true);
	}

}
