package sillygit.servent.handler;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import sillygit.mutex.TokenMutex;

public class TokenHandler implements MessageHandler {

    private final Message clientMessage;

    public TokenHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.TOKEN) {
            TokenMutex.receiveToken();
        } else {
            AppConfig.timestampedErrorPrint("Token handler got message that's not of type TOKEN.");
        }

    }

}
