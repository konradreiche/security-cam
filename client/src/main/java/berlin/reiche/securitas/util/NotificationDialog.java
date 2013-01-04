package berlin.reiche.securitas.util;

import android.app.AlertDialog;
import android.content.Context;

public class NotificationDialog {

	public static AlertDialog create(Context context, String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(message).setCancelable(false)
				.setPositiveButton("OK", null);
		return builder.create();
	}

	public static AlertDialog create(Context context, int messageId) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setMessage(messageId).setCancelable(false)
				.setPositiveButton("OK", null);
		return builder.create();
	}

}
