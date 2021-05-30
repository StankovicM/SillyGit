package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellGetMessage;
import servent.message.util.MessageUtil;
import sillygit.util.PullCollector;

public class TellGetHandler implements MessageHandler {

	private final Message clientMessage;
	
	public TellGetHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.TELL_GET) {
			TellGetMessage tellGetMessage = (TellGetMessage) clientMessage;

			String requester = tellGetMessage.getRequesterIpAddress() + ":" + tellGetMessage.getRequesterPort();
			int key = ChordState.chordHash(requester);
			if (key == AppConfig.myServentInfo.getChordId()) {
				PullCollector.addFileInfo(tellGetMessage.getFileInfo());
			} else {
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
				Message tellMessage = new TellGetMessage(
						tellGetMessage.getSenderIpAddress(), tellGetMessage.getSenderPort(),
						nextNode.getIpAddress(), nextNode.getListenerPort(),
						tellGetMessage.getRequesterIpAddress(), tellGetMessage.getRequesterPort(), tellGetMessage.getFileInfo());
				MessageUtil.sendMessage(tellMessage);
			}
		} else {
			AppConfig.timestampedErrorPrint("Tell get handler got a message that is not TELL_GET");
		}
	}

}
