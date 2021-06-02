package servent.message;

import sillygit.util.FileInfo;

import java.io.Serial;
import java.util.List;
import java.util.Map;

public class WelcomeMessage extends BasicMessage {

	@Serial
	private static final long serialVersionUID = -8981406250652693908L;

	private final Map<Integer, FileInfo> storageMap;
	private final Map<Integer, Integer> versionMap;
	private final Map<Integer, List<FileInfo>> oldVersions;
	
	public WelcomeMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
						  Map<Integer, FileInfo> storageMap, Map<Integer, Integer> versionMap,
						  Map<Integer, List<FileInfo>> oldVersions) {

		super(MessageType.WELCOME, senderIpAddress, senderPort, receiverIpAddress, receiverPort);
		
		this.storageMap = storageMap;
		this.versionMap = versionMap;
		this.oldVersions = oldVersions;

	}

	public Map<Integer, FileInfo> getStorageMap() { return storageMap; }

	public Map<Integer, Integer> getVersionMap() { return versionMap; }

	public Map<Integer, List<FileInfo>> getOldVersions() { return oldVersions; }

}
