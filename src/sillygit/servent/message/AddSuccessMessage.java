package sillygit.servent.message;

import servent.message.BasicMessage;
import servent.message.MessageType;
import sillygit.util.FileInfo;

import java.io.Serial;

public class AddSuccessMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = 7277911492888707017L;

    private final String requesterIpAddress;
    private final int requesterPort;
    private final FileInfo fileInfo;

    public AddSuccessMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
                             String requesterIpAddress, int requesterPort, FileInfo fileInfo) {

        super(MessageType.ADD_SUCCESS, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.requesterIpAddress = requesterIpAddress;
        this.requesterPort = requesterPort;
        this.fileInfo = fileInfo;

    }

    public String getRequesterIpAddress() { return requesterIpAddress; }

    public int getRequesterPort() { return requesterPort; }

    public FileInfo getFileInfo() { return fileInfo; }

}
