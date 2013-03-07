package berlin.reiche.securitas.controller;

import android.os.Message;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.ClientModel.State;
import berlin.reiche.securitas.Protocol;
import berlin.reiche.securitas.controller.tasks.BitmapDownloadTask;

public class WaitState extends ControllerState<State> {

	public WaitState(Controller<State> controller) {
		super(controller);
	}

	@Override
	public void handleMessage(Message msg) {
		Protocol request = Protocol.valueOf(msg.what);
		switch (request) {
		case DOWNLOAD_LATEST_SNAPSHOT:
			downloadLatestSnapshot();
			break;
		case DOWNLOAD_MOTION_SNAPSHOT:
			downloadMotionSnapshot(msg.obj.toString());
			break;
		default:
			throw new IllegalStateException();
		}
	}

	private void downloadMotionSnapshot(String filename) {
		String uri = Client.endpoint;
		uri += Protocol.DOWNLOAD_MOTION_SNAPSHOT.operation + filename;
		new BitmapDownloadTask(model, controller).execute(uri);
	}

	private void downloadLatestSnapshot() {
		String uri = Client.endpoint;
		uri += Protocol.DOWNLOAD_LATEST_SNAPSHOT.operation;
		new BitmapDownloadTask(model, controller).execute(uri);
	}

}
