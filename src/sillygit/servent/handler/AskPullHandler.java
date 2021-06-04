package sillygit.servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.handler.MessageHandler;
import sillygit.servent.message.AskPullMessage;
import servent.message.Message;
import servent.message.MessageType;
import sillygit.servent.message.TellPullMessage;
import servent.message.util.MessageUtil;
import sillygit.util.FileInfo;

public class AskPullHandler implements MessageHandler {

	private final Message clientMessage;
	
	public AskPullHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ASK_PULL) {
			AskPullMessage askPullMessage = (AskPullMessage) clientMessage;

			FileInfo fileInfo = AppConfig.chordState.gitPull(askPullMessage.getPath(), askPullMessage.getVersion(),
					askPullMessage.getSenderIpAddress(), askPullMessage.getSenderPort());
			if (fileInfo != null) {
				int key = ChordState.chordHash(askPullMessage.getSenderIpAddress() + ":" + askPullMessage.getSenderPort());
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
				Message tellMessage = new TellPullMessage(
						AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
						nextNode.getIpAddress(), nextNode.getListenerPort(),
						askPullMessage.getSenderIpAddress(), askPullMessage.getSenderPort(), fileInfo);
				MessageUtil.sendMessage(tellMessage);
			}
		} else {
			AppConfig.timestampedErrorPrint("Ask get handler got a message that is not ASK_GET");
		}

	}

}