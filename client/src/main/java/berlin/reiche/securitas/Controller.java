package berlin.reiche.securitas;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

public abstract class Controller {

	final HandlerThread inboxHandlerThread;
	final Handler inboxHandler;
	final List<Handler> outboxHandlers;

	public Controller() {
		inboxHandlerThread = new HandlerThread("Controller");
		inboxHandler = new Handler(inboxHandlerThread.getLooper());
		outboxHandlers = new ArrayList<Handler>();
	}

	public final void dispose() {
		inboxHandlerThread.getLooper().quit();
	}

	public final void addOutboxHandler(Handler handler) {
		outboxHandlers.add(handler);
	}

	public final void removeOutboxHandler(Handler handler) {
		outboxHandlers.remove(handler);
	}

	final void notifyOutboxHandlers(int what, int arg1, int arg2, Object obj) {

		if (!outboxHandlers.isEmpty()) {

			for (Handler handler : outboxHandlers) {
				Message.obtain(handler, what, arg1, arg2, obj).sendToTarget();
			}
		}
	}

}
