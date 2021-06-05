package servent.handler;

import app.AppConfig;
import app.ChordState;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NodeQuitUpdateMessage;
import servent.message.util.MessageUtil;

public class NodeQuitUpdateHandler implements MessageHandler {

    private final Message clientMessage;

    public NodeQuitUpdateHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.QUIT_UPDATE) {
            int servent1Id = ChordState.chordHash(clientMessage.getSenderIpAddress() + ":" +
                    clientMessage.getSenderPort());
            int servent2Id = ChordState.chordHash(AppConfig.myServentInfo.getIpAddress() + ":" +
                    AppConfig.myServentInfo.getListenerPort());

            NodeQuitUpdateMessage updateMessage = (NodeQuitUpdateMessage) clientMessage;

            AppConfig.chordState.removeNode(updateMessage.getQuittingNode());

            if (servent1Id == servent2Id) {
                System.out.println("It's a me!!!");
            } else {
                System.out.println(AppConfig.chordState.getNextNodeIp() + ":" + AppConfig.chordState.getNextNodePort());
                Message nextUpdateMessage = new NodeQuitUpdateMessage(
                        clientMessage.getSenderIpAddress(), clientMessage.getSenderPort(),
                        AppConfig.chordState.getNextNodeIp(), AppConfig.chordState.getNextNodePort(),
                        updateMessage.getQuittingNode());
                MessageUtil.sendMessage(nextUpdateMessage);
            }
        } else {
            AppConfig.timestampedErrorPrint("Quit update handler got message that's not of type QUIT_UPDATE.");
        }

    }

}
