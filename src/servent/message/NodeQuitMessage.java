package servent.message;

import app.ServentInfo;
import sillygit.util.FileInfo;

import java.io.Serial;
import java.util.List;
import java.util.Map;

public class NodeQuitMessage extends BasicMessage {

    @Serial
    private static final long serialVersionUID = -2907471909581968609L;

    private final ServentInfo predecessorInfo;
    private final Map<Integer, FileInfo> storageMap;
    private final Map<Integer, Integer> versionMap;
    private final Map<Integer, List<FileInfo>> oldVersions;

    public NodeQuitMessage(String senderIpAddress, int senderPort, String receiverIpAddress, int receiverPort,
                           ServentInfo predecessorInfo, Map<Integer, FileInfo> storageMap,
                           Map<Integer, Integer> versionMap, Map<Integer, List<FileInfo>> oldVersions) {

        super(MessageType.QUIT, senderIpAddress, senderPort, receiverIpAddress, receiverPort);

        this.predecessorInfo = predecessorInfo;
        this.storageMap = storageMap;
        this.versionMap = versionMap;
        this.oldVersions = oldVersions;

    }

    public ServentInfo getPredecessorInfo() { return predecessorInfo; }

    public Map<Integer, FileInfo> getStorageMap() { return storageMap; }

    public Map<Integer, Integer> getVersionMap() { return versionMap; }

    public Map<Integer, List<FileInfo>> getOldVersions() { return oldVersions; }

}
