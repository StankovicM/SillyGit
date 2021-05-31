package sillygit.servent.message;

import servent.message.BasicMessage;
import servent.message.MessageType;
import sillygit.util.FileInfo;

import java.io.Serial;

public class CommitResolveMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -6374870942112141653L;

    private final FileInfo fileInfo;

    public CommitResolveMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, FileInfo fileInfo) {

        super(MessageType.COMMIT_RESOLVE, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.fileInfo = fileInfo;

    }

    public FileInfo getFileInfo() { return fileInfo; }

}
