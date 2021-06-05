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
            NodeQuitMessage quitMessage = (NodeQuitMessage) clientMessage;
            AppConfig.chordState.updateStorage(quitMessage.getStorageMap(), quitMessage.getVersionMap(),
                    quitMessage.getOldVersions());
            AppConfig.chordState.updatePredecessor(quitMessage.getPredecessorInfo());
        } else {
            AppConfig.timestampedErrorPrint("Quit handler got message that's not of type QUIT.");
        }

    }

}
