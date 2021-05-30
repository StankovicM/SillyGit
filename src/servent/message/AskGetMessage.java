package servent.message;

import java.io.Serial;

public class AskGetMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -8558031124520315033L;

	private final String path;
	private final int version;

	public AskGetMessage(String senderIpAddress, int senderPort,
						 String receiverIpAddress, int receiverPort,
						 String path, int version) {

		super(MessageType.ASK_GET, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

		this.path = path;
		this.version = version;

	}

	public String getPath() { return path; }

	public int getVersion() { return version; }

}
