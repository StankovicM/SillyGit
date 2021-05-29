package servent.message;

import java.io.Serial;

public class NodeQuitMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -5355188664369251956L;

    public NodeQuitMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort) {

        super(MessageType.QUIT, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

    }

}
