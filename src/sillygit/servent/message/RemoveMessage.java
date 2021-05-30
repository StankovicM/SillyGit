package sillygit.servent.message;

import servent.message.BasicMessage;
import servent.message.MessageType;

import java.io.Serial;

public class RemoveMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -3278819780346427831L;

    public RemoveMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, String path) {

        super(MessageType.REMOVE, senderIpAddress, senderPort, receiverIpAddress, receiverPort, path);

    }

}
