package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NodeQuitPredecessorMessage;

public class NodeQuitPredecessorHandler implements MessageHandler {

    private final Message clientMessage;

    public NodeQuitPredecessorHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.QUIT_PREDECESSOR) {
            AppConfig.chordState.removeNode((NodeQuitPredecessorMessage) clientMessage);
        } else {
            AppConfig.timestampedErrorPrint("Quit predecessor handler got message that's not of type QUIT_PREDECESSOR.");
        }

    }

}
