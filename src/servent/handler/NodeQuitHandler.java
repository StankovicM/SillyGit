package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NodeQuitMessage;

public class NodeQuitHandler implements MessageHandler {

    private final Message clientMessage;

    public NodeQuitHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.QUIT) {
            AppConfig.chordState.update((NodeQuitMessage) clientMessage);
        } else {
            AppConfig.timestampedErrorPrint("Quit handler got message that's not of type QUIT.");
        }

    }

}
