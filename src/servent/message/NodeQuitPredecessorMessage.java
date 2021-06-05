package servent.message;

import java.io.Serial;

public class NodeQuitPredecessorMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 6692722047026074919L;

    public NodeQuitPredecessorMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {

        super(MessageType.QUIT_PREDECESSOR, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

    }

}
