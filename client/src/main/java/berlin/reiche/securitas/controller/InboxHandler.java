package berlin.reiche.securitas.controller;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

final class InboxHandler extends Handler {
	
	Controller controller;
	
	InboxHandler(Looper looper, Controller controller) {
		super(looper);
		this.controller = controller;
	}

	@Override
	public void handleMessage(Message msg) {
		controller.handleMessage(msg);
	}
}
