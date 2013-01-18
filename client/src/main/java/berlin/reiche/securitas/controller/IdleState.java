package berlin.reiche.securitas.controller;

import android.os.Message;
import berlin.reiche.securitas.Protocol;

/**
 * IdleState is one of different controller states.
 * 
 * @author Konrad Reiche
 * 
 */
public class IdleState extends ControllerState {

	@Override
	public void handleMessage(Message msg) {

		Protocol request = Protocol.valueOf(msg.what);
		switch (request) {
		case START:
		case STOP:
		}

	}
}
