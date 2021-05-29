package servent.message;

import sillygit.util.FileInfo;

import java.io.Serial;

public class PutMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = 5163039209888734276L;

	private final FileInfo fileInfo;

	public PutMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, FileInfo fileInfo) {

		super(MessageType.PUT, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

		this.fileInfo = fileInfo;

	}

	public FileInfo getFileInfo() { return fileInfo; }

}
