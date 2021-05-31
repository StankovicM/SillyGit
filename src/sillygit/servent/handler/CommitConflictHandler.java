package sillygit.servent.handler;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class CommitConflictHandler implements MessageHandler {

    private final Message clientMessage;

    public CommitConflictHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.COMMIT_CONFLICT) {
            //TODO
        } else {
            AppConfig.timestampedErrorPrint("Conflict handler got message that's not of type CONFLICT.");
        }

    }

}
