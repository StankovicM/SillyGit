package sillygit.servent.handler;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;

public class CommitSuccessHandler implements MessageHandler {

    private final Message clientMessage;

    public CommitSuccessHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.COMMIT_SUCCESS) {

        } else {
            AppConfig.timestampedErrorPrint("Success handler got message that's not of type COMMIT_SUCCESS.");
        }

    }

}
