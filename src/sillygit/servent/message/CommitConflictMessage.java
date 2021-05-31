package sillygit.servent.message;

import servent.message.BasicMessage;
import servent.message.MessageType;
import sillygit.util.FileInfo;

import java.io.Serial;

public class CommitConflictMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -8183139979155474709L;

    private final FileInfo oldFileInfo;
    private final FileInfo newFileInfo;

    public CommitConflictMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
                                 FileInfo oldFileInfo, FileInfo newFileInfo) {

        super(MessageType.COMMIT_CONFLICT, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.oldFileInfo = oldFileInfo;
        this.newFileInfo = newFileInfo;

    }

    public FileInfo getOldFileInfo() { return oldFileInfo; }

    public FileInfo getNewFileInfo() { return newFileInfo; }

}
