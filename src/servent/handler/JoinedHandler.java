package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import sillygit.mutex.TokenMutex;

public class JoinedHandler implements MessageHandler {

    private final Message clientMessage;

    public JoinedHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.JOINED) {
            TokenMutex.unlock();
        } else {
            AppConfig.timestampedErrorPrint("Joined handler got message that's not of type JOINED.");
        }

    }

}
