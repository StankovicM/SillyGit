package sillygit.servent.message;

import servent.message.BasicMessage;
import servent.message.MessageType;
import sillygit.util.FileInfo;

import java.io.Serial;

public class CommitErrorMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -3303723663981278654L;

    private final String requesterIpAddress;
    private final int requesterPort;
    private final FileInfo fileInfo;
    private final int code;

    public CommitErrorMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
                              String requesterIpAddress, int requesterPort, FileInfo fileInfo, int code) {

        super(MessageType.COMMIT_ERROR, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.requesterIpAddress = requesterIpAddress;
        this.requesterPort = requesterPort;
        this.fileInfo = fileInfo;
        this.code = code;

    }

    public String getRequesterIpAddress() { return requesterIpAddress; }

    public int getRequesterPort() { return requesterPort; }

    public FileInfo getFileInfo() { return fileInfo; }

    public int getCode() { return code; }

}
