package berlin.reiche.securitas.controller;

import android.os.Message;
import berlin.reiche.securitas.Protocol;

public class WaitState extends ControllerState {

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
		
	}

}
