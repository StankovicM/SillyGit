package sillygit.servent.message;

import servent.message.BasicMessage;
import servent.message.MessageType;
import sillygit.util.FileInfo;

import java.io.Serial;

public class CommitSuccessMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -159298719611217055L;

    private final FileInfo fileInfo;

    public CommitSuccessMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, FileInfo fileInfo) {

        super(MessageType.COMMIT_SUCCESS, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.fileInfo = fileInfo;

    }

    public FileInfo getFileInfo() { return fileInfo; }

}
