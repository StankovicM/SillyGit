package servent.handler;

import app.AppConfig;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;

public class NodeQuitPredecessorHandler implements MessageHandler {

    private final Message clientMessage;

    public NodeQuitPredecessorHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.QUIT_PREDECESSOR) {
            ServentInfo successorInfo = new ServentInfo(clientMessage.getSenderIpAddress(), clientMessage.getSenderPort());
            AppConfig.chordState.updateSuccessor(successorInfo);
        } else {
            AppConfig.timestampedErrorPrint("Quit predecessor handler got message that's not of type QUIT_PREDECESSOR.");
        }

    }

}
