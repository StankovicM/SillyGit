package servent.message;

import sillygit.util.FileInfo;

import java.io.Serial;
import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -8981406250652693908L;

	private final Map<Integer, FileInfo> storageMap;
	private final Map<Integer, Integer> versionMap;
	
	public WelcomeMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
						  Map<Integer, FileInfo> storageMap, Map<Integer, Integer> versionMap) {

		super(MessageType.WELCOME, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
		
		this.storageMap = storageMap;
		this.versionMap = versionMap;

	}

	public Map<Integer, FileInfo> getStorageMap() { return storageMap; }

	public Map<Integer, Integer> getVersionMap() { return versionMap; }

}
