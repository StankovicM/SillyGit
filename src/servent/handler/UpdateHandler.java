package servent.handler;

import java.util.ArrayList;
import java.util.List;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.UpdateMessage;
import servent.message.util.MessageUtil;

public class UpdateHandler implements MessageHandler {

	private Message clientMessage;
	
	public UpdateHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.UPDATE) {
			int servent1Id = ChordState.chordHash(clientMessage.getSenderIpAddress() + ":" + clientMessage.getSenderPort());
			int servent2Id = ChordState.chordHash(AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort());
			if (servent1Id != servent2Id) {
				ServentInfo newNodeInfo = new ServentInfo(clientMessage.getSenderIpAddress(), clientMessage.getSenderPort());
				List<ServentInfo> newNodes = new ArrayList<>();
				newNodes.add(newNodeInfo);
				
				AppConfig.chordState.addNodes(newNodes);
				String newMessageText = "";
				if (clientMessage.getMessageText().equals("")) {
					newMessageText = AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort();
				} else {
					newMessageText = clientMessage.getMessageText() + "," + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort();
				}
				Message nextUpdate = new UpdateMessage(
						clientMessage.getSenderIpAddress(), clientMessage.getSenderPort(),
						AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
						newMessageText);
				MessageUtil.sendMessage(nextUpdate);
			} else {
				String messageText = clientMessage.getMessageText();
				String[] servents = messageText.split(",");
				
				List<ServentInfo> allNodes = new ArrayList<>();
				for (String servent : servents) {
					String serventIp = servent.substring(0, servent.indexOf(':'));
					int serventPort = Integer.parseInt(servent.substring(serventIp.length() + 1));
					allNodes.add(new ServentInfo(serventIp, serventPort));
				}
				AppConfig.chordState.addNodes(allNodes);
			}
		} else {
			AppConfig.timestampedErrorPrint("Update message handler got message that is not UPDATE");
		}
	}

}
