package sillygit.servent.handler;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class CommitResolveHandler implements MessageHandler {

    private final Message clientMessage;

    public CommitResolveHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.COMMIT_RESOLVE) {

        } else {
            AppConfig.timestampedErrorPrint("Resolve handler got message that's not of type COMMIT_RESOLVE.");
        }

    }

}
