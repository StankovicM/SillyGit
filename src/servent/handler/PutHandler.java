package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.PutMessage;

public class PutHandler implements MessageHandler {

	private Message clientMessage;
	
	public PutHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.PUT) {
			PutMessage putMessage = (PutMessage) clientMessage;

			AppConfig.chordState.gitAdd(putMessage.getFileInfo(), putMessage.getSenderIpAddress(), putMessage.getSenderPort());
		} else {
			AppConfig.timestampedErrorPrint("Put handler got a message that is not PUT");
		}

	}

}
