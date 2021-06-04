package sillygit.servent.message;

import servent.message.BasicMessage;
import servent.message.MessageType;
import sillygit.util.FileInfo;

import java.io.Serial;

public class AddMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = 5163039209888734276L;

	private final FileInfo fileInfo;

	public AddMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort, FileInfo fileInfo) {

		super(MessageType.ADD, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

		this.fileInfo = fileInfo;

	}

	public FileInfo getFileInfo() { return fileInfo; }

}
