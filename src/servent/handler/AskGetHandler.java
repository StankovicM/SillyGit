package servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.AskGetMessage;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.TellGetMessage;
import servent.message.util.MessageUtil;
import sillygit.util.FileInfo;

public class AskGetHandler implements MessageHandler {

	private final Message clientMessage;
	
	public AskGetHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.ASK_GET) {
			AskGetMessage askGetMessage = (AskGetMessage) clientMessage;

			FileInfo fileInfo = AppConfig.chordState.gitPull(askGetMessage.getPath(), askGetMessage.getVersion(),
					askGetMessage.getSenderIpAddress(), askGetMessage.getSenderPort());
			if (fileInfo != null) {
				int key = ChordState.chordHash(askGetMessage.getSenderIpAddress() + ":" + askGetMessage.getSenderPort());
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
				Message tellMessage = new TellGetMessage(
						AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
						nextNode.getIpAddress(), nextNode.getListenerPort(),
						askGetMessage.getSenderIpAddress(), askGetMessage.getSenderPort(), fileInfo);
				MessageUtil.sendMessage(tellMessage);
			}
		} else {
			AppConfig.timestampedErrorPrint("Ask get handler got a message that is not ASK_GET");
		}

	}

}