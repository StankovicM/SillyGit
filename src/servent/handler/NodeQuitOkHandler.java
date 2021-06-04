package servent.handler;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;

public class NodeQuitOkHandler implements MessageHandler {

    private final Message clientMessage;

    public NodeQuitOkHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.QUIT_OK) {
            AppConfig.CAN_QUIT = true;
        } else {
            AppConfig.timestampedErrorPrint("Quit ok handler got message that's not of type QUIT_OK.");
        }

    }

}
