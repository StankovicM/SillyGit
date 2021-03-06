package sillygit.servent.message;

import servent.message.BasicMessage;
import servent.message.MessageType;
import sillygit.util.FileInfo;

import java.io.Serial;

public class CommitMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -431691342282645366L;

    private final FileInfo fileInfo;

    public CommitMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, FileInfo fileInfo) {

        super(MessageType.COMMIT, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.fileInfo = fileInfo;

    }

    public FileInfo getFileInfo() { return fileInfo; }

}
