package berlin.reiche.securitas;

import android.graphics.Bitmap;
import berlin.reiche.securitas.ClientModel.State;

public class ClientModel extends Model<State> {

	public enum State {
		IDLE, WAIT, DETECTING;
	}

	private String status;

	private boolean registeredOnServer;

	private Bitmap snapshot;

	public ClientModel() {
		super();
		state = State.IDLE;
	}

	public synchronized String getStatus() {
		return status;
	}

	public boolean isRegisteredOnServer() {
		return registeredOnServer;
	}
	
	public void onRequestFail() {
		setState(previous);
	}

	
	public void setRegisteredOnServer(boolean flag) {
		registeredOnServer = flag;
	}

	public synchronized void setStatus(String status) {
		this.status = status;
	}

	public void setSnapshot(Bitmap snapshot) {
		this.snapshot = snapshot;
	}

	public Bitmap getSnapshot() {
		return snapshot;
	}

}
