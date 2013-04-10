package berlin.reiche.securitas.util;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Class with factory methods for creating a notification dialog.
 * 
 * @author Konrad Reiche
 * 
 */
public class NotificationDialog {

	/**
	 * Factory method to create a notification dialog based on a string message.
	 * This alert dialog has only one button.
	 * 
	 * @param context
	 *            the context where the notification should be displayed.
	 * @param message
	 *            the message to be displayed.
	 * @return the notification dialog object.
	 */
	public static AlertDialog create(Context context, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message).setCancelable(false)
				.setPositiveButton("OK", null);
		return builder.create();
	}

	/**
	 * Factory method to create a notification dialog based on a message id.
	 * This alert dialog has only one button.ge
	 * 
	 * @param context
	 *            the context where the notification should be displayed.
	 * @param messageId
	 *            the message to be displayed.
	 * @return the notification dialog object.
	 */
	public static AlertDialog create(Context context, int messageId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(messageId).setCancelable(false)
				.setPositiveButton("OK", null);
		return builder.create();
	}

}
