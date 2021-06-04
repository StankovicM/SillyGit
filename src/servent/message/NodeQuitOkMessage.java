package servent.message;

import java.io.Serial;

public class NodeQuitOkMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -7939075499063417031L;

    public NodeQuitOkMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {

        super(MessageType.QUIT_OK, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

    }

}
