package berlin.reiche.securitas.model;

import android.graphics.Bitmap;
import berlin.reiche.securitas.model.ClientModel.State;

/**
 * Concrete model for this application.
 * 
 * @author Konrad Reiche
 * 
 */
public class ClientModel extends Model<State> {

	/**
	 * {@link Enum} state for the model. Either the detection is active
	 * (detecting) or inactive (idle).
	 */
	public enum State {
		IDLE, DETECTING;
	}

	/**
	 * Whether the device is registered on the backend server.
	 */
	private boolean registeredOnServer;

	/**
	 * The current snapshot taken on the backend server.
	 */
	private Bitmap snapshot;

	/**
	 * Default constructor.
	 */
	public ClientModel() {
		super();
		state = State.IDLE;
	}

	/**
	 * @return whether the device is registered on the backend server.
	 */
	public boolean isRegisteredOnServer() {
		return registeredOnServer;
	}

	/**
	 * @param registeredOnServer
	 *            whether the device is registered on the backend server.
	 */
	public void setRegisteredOnServer(boolean registeredOnServer) {
		this.registeredOnServer = registeredOnServer;
	}

	/**
	 * @return the current snapshot taken on the backend server.
	 */
	public Bitmap getSnapshot() {
		return snapshot;
	}

	/**
	 * @param snapshot
	 *            the current snapshot taken on the backend server.
	 */
	public void setSnapshot(Bitmap snapshot) {
		this.snapshot = snapshot;
	}

}
