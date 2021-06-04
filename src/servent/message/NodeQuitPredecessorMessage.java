package servent.message;

import app.ServentInfo;

import java.io.Serial;

public class NodeQuitPredecessorMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 6692722047026074919L;

    private final ServentInfo previousSuccessor;

    public NodeQuitPredecessorMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
                                      ServentInfo previousSuccessor) {

        super(MessageType.QUIT_PREDECESSOR, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.previousSuccessor = previousSuccessor;

    }

    public ServentInfo getPreviousSuccessor() { return previousSuccessor; }

}
