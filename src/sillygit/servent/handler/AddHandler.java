package sillygit.servent.handler;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import sillygit.servent.message.AddMessage;

public class AddHandler implements MessageHandler {

	private Message clientMessage;
	
	public AddHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ADD) {
			AddMessage addMessage = (AddMessage) clientMessage;

			AppConfig.chordState.gitAdd(addMessage.getFileInfo(), addMessage.getSenderIpAddress(), addMessage.getSenderPort());
		} else {
			AppConfig.timestampedErrorPrint("Put handler got a message that is not PUT");
		}

	}

}
