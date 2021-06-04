package sillygit.servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import sillygit.servent.message.TellPullMessage;
import servent.message.util.MessageUtil;
import sillygit.util.PullCollector;

public class TellPullHandler implements MessageHandler {

	private final Message clientMessage;
	
	public TellPullHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.TELL_PULL) {
			TellPullMessage tellPullMessage = (TellPullMessage) clientMessage;

			String requester = tellPullMessage.getRequesterIpAddress() + ":" + tellPullMessage.getRequesterPort();
			int key = ChordState.chordHash(requester);
			if (key == AppConfig.myServentInfo.getChordId()) {
				PullCollector.addFileInfo(tellPullMessage.getFileInfo());
			} else {
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
				Message tellMessage = new TellPullMessage(
						tellPullMessage.getSenderIpAddress(), tellPullMessage.getSenderPort(),
						nextNode.getIpAddress(), nextNode.getListenerPort(),
						tellPullMessage.getRequesterIpAddress(), tellPullMessage.getRequesterPort(), tellPullMessage.getFileInfo());
				MessageUtil.sendMessage(tellMessage);
			}
		} else {
			AppConfig.timestampedErrorPrint("Tell get handler got a message that is not TELL_GET");
		}
	}

}
