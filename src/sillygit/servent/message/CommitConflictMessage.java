package sillygit.servent.message;

import servent.message.BasicMessage;
import servent.message.MessageType;
import sillygit.util.FileInfo;

import java.io.Serial;

public class CommitConflictMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -8183139979155474709L;

    private final String requesterIpAddress;
    private final int requesterPort;
    private final FileInfo oldFileInfo;
    private final FileInfo newFileInfo;

    public CommitConflictMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
                                 String requesterIpAddress, int requesterPort, FileInfo oldFileInfo, FileInfo newFileInfo) {

        super(MessageType.COMMIT_CONFLICT, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.requesterIpAddress = requesterIpAddress;
        this.requesterPort = requesterPort;
        this.oldFileInfo = oldFileInfo;
        this.newFileInfo = newFileInfo;

    }

    public String getRequesterIpAddress() { return requesterIpAddress; }

    public int getRequesterPort() { return requesterPort; }

    public FileInfo getOldFileInfo() { return oldFileInfo; }

    public FileInfo getNewFileInfo() { return newFileInfo; }

}
