package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class NodeQuitUpdateMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -2956202789195633736L;

    private final ServentInfo quittingNode;

    public NodeQuitUpdateMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
                                 ServentInfo quittingNode) {

        super(MessageType.QUIT_UPDATE, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.quittingNode = quittingNode;

    }

    public ServentInfo getQuittingNode() { return quittingNode; }

}
