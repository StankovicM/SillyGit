package servent.handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.NewNodeMessage;
import servent.message.SorryMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;
import sillygit.util.FileInfo;
import sillygit.util.FileUtils;

public class NewNodeHandler implements MessageHandler {

	private final Message clientMessage;
	
	public NewNodeHandler(Message clientMessage) {
		this.clientMessage = clientMessage;
	}
	
	@Override
	public void run() {
		if (clientMessage.getMessageType() == MessageType.NEW_NODE) {
			String newNodeIp = clientMessage.getSenderIpAddress();
			int newNodePort = clientMessage.getSenderPort();
			ServentInfo newNodeInfo = new ServentInfo(newNodeIp, newNodePort);
			
			//check if the new node collides with another existing node.
			if (AppConfig.chordState.isCollision(newNodeInfo.getChordId())) {
				Message sry = new SorryMessage(
						AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
						clientMessage.getSenderIpAddress(), clientMessage.getSenderPort());
				MessageUtil.sendMessage(sry);
				return;
			}
			
			//check if he is my predecessor
			boolean isMyPred = AppConfig.chordState.isKeyMine(newNodeInfo.getChordId());
			if (isMyPred) { //if yes, prepare and send welcome message
				ServentInfo hisPred = AppConfig.chordState.getPredecessor();
				if (hisPred == null) {
					hisPred = AppConfig.myServentInfo;
				}
				
				AppConfig.chordState.setPredecessor(newNodeInfo);

				Map<Integer, FileInfo> myStorage = AppConfig.chordState.getStorageMap();
				Map<Integer, FileInfo> hisStorage = new HashMap<>();

				Map<Integer, Integer> myVersions = AppConfig.chordState.getVersionMap();
				Map<Integer, Integer> hisVersions = new HashMap<>();

				int myId = AppConfig.myServentInfo.getChordId();
				int hisPredId = hisPred.getChordId();
				int newNodeId = newNodeInfo.getChordId();
				
				for (Entry<Integer, FileInfo> fileInfoEntry : myStorage.entrySet()) {
					if (hisPredId == myId) { //i am first and he is second
						if (myId < newNodeId) {
							if (fileInfoEntry.getKey() <= newNodeId && fileInfoEntry.getKey() > myId) {
								hisStorage.put(fileInfoEntry.getKey(), fileInfoEntry.getValue());
								hisVersions.put(fileInfoEntry.getKey(), myVersions.get(fileInfoEntry.getKey()));
							}
						} else {
							if (fileInfoEntry.getKey() <= newNodeId || fileInfoEntry.getKey() > myId) {
								hisStorage.put(fileInfoEntry.getKey(), fileInfoEntry.getValue());
								hisVersions.put(fileInfoEntry.getKey(), myVersions.get(fileInfoEntry.getKey()));
							}
						}
					}
					if (hisPredId < myId) { //my old predecesor was before me
						if (fileInfoEntry.getKey() <= newNodeId) {
							hisStorage.put(fileInfoEntry.getKey(), fileInfoEntry.getValue());
							hisVersions.put(fileInfoEntry.getKey(), myVersions.get(fileInfoEntry.getKey()));
						}
					} else { //my old predecesor was after me
						if (hisPredId > newNodeId) { //new node overflow
							if (fileInfoEntry.getKey() <= newNodeId || fileInfoEntry.getKey() > hisPredId) {
								hisStorage.put(fileInfoEntry.getKey(), fileInfoEntry.getValue());
								hisVersions.put(fileInfoEntry.getKey(), myVersions.get(fileInfoEntry.getKey()));
							}
						} else { //no new node overflow
							if (fileInfoEntry.getKey() <= newNodeId && fileInfoEntry.getKey() > hisPredId) {
								hisStorage.put(fileInfoEntry.getKey(), fileInfoEntry.getValue());
								hisVersions.put(fileInfoEntry.getKey(), myVersions.get(fileInfoEntry.getKey()));
							}
						}
						
					}
					
				}
				for (Integer key : hisStorage.keySet()) { //remove his values from my map
					myStorage.remove(key);
					myVersions.remove(key);
				}
				AppConfig.chordState.setStorageMap(myStorage);
				AppConfig.chordState.setVersionMap(myVersions);

				//Brisemo sve uskaldistene verzije za fajlove koje predajemo novom cvoru,
				//a stare verzije ucitavamo i takodje saljemo pre brisanja
				Map<Integer, List<FileInfo>> oldVersions = new HashMap<>();
				for (Map.Entry<Integer, FileInfo> m : hisStorage.entrySet()) {
					int version = hisVersions.get(m.getKey());
					for (int i = version; i >= 0; i--) {
						//Putanja do verzije fajla
						String filePath = m.getValue().getPath() + "." + i;
						//Ucitavamo samo starije verzije
						if (i < version) {
							//Ucitamo fajl
							FileInfo tmp = FileUtils.getFileInfoFromPath(AppConfig.STORAGE_DIR, filePath);
							if (tmp != null && tmp.isFile()) {
								//Postavimo odgovarajucu verziju i putanju bez verzije
								FileInfo fileInfo = new FileInfo(m.getValue().getPath(), tmp.isDirectory(), tmp.getContent(),
										i, tmp.getSubFiles());
								//Dodamo fajl u odgovarajucu listu
								int key = ChordState.chordHash(fileInfo.getPath());
								oldVersions.putIfAbsent(key, new ArrayList<>());
								oldVersions.get(key).add(fileInfo);
							}
						}
						//Izbrisemo fajl nakon ucitavanja
						FileUtils.removeFile(AppConfig.STORAGE_DIR, filePath);
					}
				}

				WelcomeMessage wm = new WelcomeMessage(
						AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
						newNodeIp, newNodePort, hisStorage, hisVersions, oldVersions);
				MessageUtil.sendMessage(wm);
			} else { //if he is not my predecessor, let someone else take care of it
				ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(newNodeInfo.getChordId());
				NewNodeMessage nnm = new NewNodeMessage(newNodeIp, newNodePort, nextNode.getIpAddress(), nextNode.getListenerPort());
				MessageUtil.sendMessage(nnm);
			}
			
		} else {
			AppConfig.timestampedErrorPrint("NEW_NODE handler got something that is not new node message.");
		}

	}

}
