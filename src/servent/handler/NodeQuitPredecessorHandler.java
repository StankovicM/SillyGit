package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NodeQuitPredecessorMessage;

public class NodeQuitPredecessorHandler implements MessageHandler {

    private final Message clientMessage;

    public NodeQuitPredecessorHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.QUIT_PREDECESSOR) {
            NodeQuitPredecessorMessage quitMessage = (NodeQuitPredecessorMessage) clientMessage;
            ServentInfo successorInfo = new ServentInfo(quitMessage.getSenderIpAddress(), quitMessage.getSenderPort());
            AppConfig.chordState.updateSuccessor(successorInfo);
        } else {
            AppConfig.timestampedErrorPrint("Quit predecessor handler got message that's not of type QUIT_PREDECESSOR.");
        }

    }

}
