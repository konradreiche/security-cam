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
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.model.ClientModel;
import berlin.reiche.securitas.model.ClientModel.State;
import berlin.reiche.securitas.model.Model;
import berlin.reiche.securitas.util.FlushedInputStream;
import berlin.reiche.securitas.util.HttpUtilities;

public class BitmapDownloadTask extends AsyncTask<String, Void, Bitmap> {
	
	ClientModel model;

	ClientController controller;
	
	IOException exception;

	public BitmapDownloadTask(Model<State> model, ClientController controller) {
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
		if (exception != null) {
			controller.alertProblem(exception.getMessage());			
		}
		model.setSnapshot(result);
		controller.setSnapshot(result);
	}

}
