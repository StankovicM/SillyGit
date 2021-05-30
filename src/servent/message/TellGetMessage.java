package servent.message;

import sillygit.util.FileInfo;

import java.io.Serial;

public class TellGetMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -6213394344524749872L;

	private final String requesterIpAddress;
	private final int requesterPort;
	private final FileInfo fileInfo;

	public TellGetMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
						  String requesterIpAddress, int requesterPort, FileInfo fileInfo) {

		super(MessageType.TELL_GET, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

		this.requesterIpAddress = requesterIpAddress;
		this.requesterPort = requesterPort;
		this.fileInfo = fileInfo;

	}

	public String getRequesterIpAddress() { return requesterIpAddress; }

	public int getRequesterPort() { return requesterPort; }

	public FileInfo getFileInfo() { return fileInfo; }

}
