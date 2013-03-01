package berlin.reiche.securitas.controller;

import android.os.Message;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.ClientModel;
import berlin.reiche.securitas.Model;
import berlin.reiche.securitas.Protocol;
import berlin.reiche.securitas.tasks.BitmapDownloadTask;

public class WaitState extends ControllerState<ClientModel.State> {

	public WaitState(ClientController controller) {
		super(controller);
	}

	@Override
	public void handleMessage(Message msg) {
		Protocol request = Protocol.valueOf(msg.what);
		switch (request) {
		case DOWNLOAD_LATEST_SNAPSHOT:
			downloadLatestSnapshot();
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void downloadLatestSnapshot() {
		String uri = Client.endpoint;
		uri += Protocol.DOWNLOAD_LATEST_SNAPSHOT.operation;
		Model<ClientModel.State> model = controller.getModel();
		new BitmapDownloadTask(model).execute(uri);
	}

}
