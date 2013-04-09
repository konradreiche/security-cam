package berlin.reiche.securitas.controller.states;

import android.os.Message;
import android.util.Log;
import berlin.reiche.securitas.Client;
import berlin.reiche.securitas.controller.ClientController;
import berlin.reiche.securitas.controller.tasks.BitmapDownloadTask;
import berlin.reiche.securitas.controller.tasks.DetectionRequest;
import berlin.reiche.securitas.controller.tasks.DetectionRequest.DetectionCommand;
import berlin.reiche.securitas.model.Protocol;
import berlin.reiche.securitas.model.ClientModel.State;

/**
 * Concrete controller state used when the application is currently tracking
 * motion.
 * 
 * @author Konrad Reiche
 * 
 */
public class DetectionState extends ControllerState<State> {

	/**
	 * Tag name for logging.
	 */
	private static final String TAG = DetectionState.class.getSimpleName();

	/**
	 * The latest task for downloading snapshots. The reference is required in
	 * order to be able to cancel it if appropriate.
	 */
	private BitmapDownloadTask bitmapDownloadTask;

	/**
	 * Hides the super class controller field in order to avoid type casting.
	 */
	ClientController controller;

	/**
	 * Default constructor.
	 * 
	 * @param controller
	 *            controller reference for issuing requests.
	 */
	public DetectionState(ClientController controller) {
		super(controller);
		this.controller = controller;
	}

	/**
	 * Handles incoming messages from the interface based on the defined
	 * {@link Protocol}.
	 */
	@Override
	public void handleMessage(Message msg) {
		Protocol request = Protocol.valueOf(msg.what);
		switch (request) {
		case STOP_DETECTION:
			cancelSnapshotDownload();
			requestDetectionStop();
			break;
		case DOWNLOAD_LATEST_SNAPSHOT:
			downloadLatestSnapshot();
			break;
		case DOWNLOAD_MOTION_SNAPSHOT:
			downloadMotionSnapshot(msg.obj.toString());
			break;
		default:
			Log.e(TAG, "Illegal action request: " + request);
			throw new IllegalStateException();
		}
	}

	/**
	 * Downloads a specified snapshot.
	 * 
	 * @param filename
	 *            the filename of the snapshot that should be downloaded.
	 */
	private void downloadMotionSnapshot(String filename) {
		String uri = Client.getEndpoint();
		uri += Protocol.DOWNLOAD_MOTION_SNAPSHOT.operation + filename;
		bitmapDownloadTask = new BitmapDownloadTask(model, controller);
		bitmapDownloadTask.execute(uri);
	}

	/**
	 * Issues a detection stop.
	 */
	private void requestDetectionStop() {
		String uri = Client.getEndpoint() + Protocol.STOP_DETECTION.operation;
		controller.setState(new IdleState(controller));
		new DetectionRequest(model, controller, DetectionCommand.STOP)
				.execute(uri);
	}

	/**
	 * Downloads the latest snapshot, respectively issues to create a current
	 * one.
	 */
	private void downloadLatestSnapshot() {
		String uri = Client.getEndpoint();
		uri += Protocol.DOWNLOAD_LATEST_SNAPSHOT.operation;
		bitmapDownloadTask = new BitmapDownloadTask(model, controller);
		bitmapDownloadTask.execute(uri);
	}

	/**
	 * Cancel the current snapshot. This method is required if the detection
	 * should be turned off in order to cancel the lengthy snapshot requets.
	 */
	private void cancelSnapshotDownload() {
		if (bitmapDownloadTask != null) {
			bitmapDownloadTask.cancel(true);
		}
	}

}
