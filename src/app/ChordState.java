package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import servent.message.AskGetMessage;
import servent.message.Message;
import servent.message.PutMessage;
import servent.message.WelcomeMessage;
import servent.message.util.MessageUtil;
import sillygit.util.FileInfo;
import sillygit.util.FileUtils;

/**
 * This class implements all the logic required for Chord to function.
 * It has a static method <code>chordHash</code> which will calculate our chord ids.
 * It also has a static attribute <code>CHORD_SIZE</code> that tells us what the maximum
 * key is in our system.
 * 
 * Other public attributes and methods:
 * <ul>
 *   <li><code>chordLevel</code> - log_2(CHORD_SIZE) - size of <code>successorTable</code></li>
 *   <li><code>successorTable</code> - a map of shortcuts in the system.</li>
 *   <li><code>predecessorInfo</code> - who is our predecessor.</li>
 *   <li><code>valueMap</code> - DHT values stored on this node.</li>
 *   <li><code>init()</code> - should be invoked when we get the WELCOME message.</li>
 *   <li><code>isCollision(int chordId)</code> - checks if a servent with that Chord ID is already active.</li>
 *   <li><code>isKeyMine(int key)</code> - checks if we have a key locally.</li>
 *   <li><code>getNextNodeForKey(int key)</code> - if next node has this key, then return it, otherwise returns the nearest predecessor for this key from my successor table.</li>
 *   <li><code>addNodes(List<ServentInfo> nodes)</code> - updates the successor table.</li>
 *   <li><code>putValue(int key, int value)</code> - stores the value locally or sends it on further in the system.</li>
 *   <li><code>getValue(int key)</code> - gets the value locally, or sends a message to get it from somewhere else.</li>
 * </ul>
 * @author bmilojkovic
 *
 */
public class ChordState {

	public static int CHORD_SIZE;

	public static int chordHash(String value) {

		try {
			MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			messageDigest.update(value.getBytes());
			BigInteger hash = new BigInteger(messageDigest.digest());
			hash = hash.mod(BigInteger.valueOf(64));
			return hash.intValue();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
			return -1;
		}

	}
	
	private int chordLevel; //log_2(CHORD_SIZE)
	
	private ServentInfo[] successorTable;
	private ServentInfo predecessorInfo;
	
	//we DO NOT use this to send messages, but only to construct the successor table
	private List<ServentInfo> allNodeInfo;
	
	private Map<Integer, FileInfo> storageMap;
	private Map<Integer, Integer> versionMap;
	private Map<Integer, FileInfo> workingMap;

	public ChordState() {
		this.chordLevel = 1;
		int tmp = CHORD_SIZE;
		while (tmp != 2) {
			if (tmp % 2 != 0) { //not a power of 2
				throw new NumberFormatException();
			}
			tmp /= 2;
			this.chordLevel++;
		}
		
		successorTable = new ServentInfo[chordLevel];
		for (int i = 0; i < chordLevel; i++) {
			successorTable[i] = null;
		}
		
		predecessorInfo = null;
		allNodeInfo = new ArrayList<>();

		storageMap = new ConcurrentHashMap<>();
		versionMap = new ConcurrentHashMap<>();
		workingMap = new ConcurrentHashMap<>();

	}
	
	/**
	 * This should be called once after we get <code>WELCOME</code> message.
	 * It sets up our initial value map and our first successor so we can send <code>UPDATE</code>.
	 * It also lets bootstrap know that we did not collide.
	 */
	public void init(WelcomeMessage welcomeMsg) {
		//set a temporary pointer to next node, for sending of update message
		successorTable[0] = new ServentInfo(welcomeMsg.getSenderIpAddress(), welcomeMsg.getSenderPort());

		this.storageMap = new ConcurrentHashMap<>(welcomeMsg.getStorageMap());
		this.versionMap = new ConcurrentHashMap<>(welcomeMsg.getVersionMap());

		//tell bootstrap this node is not a collider
		try {
			Socket bsSocket = new Socket(AppConfig.BOOTSTRAP_ADDRESS, AppConfig.BOOTSTRAP_PORT);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("New\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");
			
			bsWriter.flush();
			bsSocket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public int getChordLevel() {
		return chordLevel;
	}
	
	public ServentInfo[] getSuccessorTable() {
		return successorTable;
	}

	public String getNextNodeIp() {

		return successorTable[0].getIpAddress();

	}

	public int getNextNodePort() {
		return successorTable[0].getListenerPort();
	}
	
	public ServentInfo getPredecessor() {
		return predecessorInfo;
	}
	
	public void setPredecessor(ServentInfo newNodeInfo) {
		this.predecessorInfo = newNodeInfo;
	}

	public Map<Integer, FileInfo> getStorageMap() { return storageMap; }

	public void setStorageMap(Map<Integer, FileInfo> storageMap) { this.storageMap = storageMap; }

	public Map<Integer, Integer> getVersionMap() { return versionMap; }

	public void setVersionMap(Map<Integer, Integer> versionMap) { this.versionMap = versionMap; }

	public boolean isCollision(int chordId) {
		if (chordId == AppConfig.myServentInfo.getChordId()) {
			return true;
		}
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() == chordId) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Returns true if we are the owner of the specified key.
	 */
	public boolean isKeyMine(int key) {
		if (predecessorInfo == null) {
			return true;
		}
		
		int predecessorChordId = predecessorInfo.getChordId();
		int myChordId = AppConfig.myServentInfo.getChordId();
		
		if (predecessorChordId < myChordId) { //no overflow
			if (key <= myChordId && key > predecessorChordId) {
				return true;
			}
		} else { //overflow
			if (key <= myChordId || key > predecessorChordId) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Main chord operation - find the nearest node to hop to to find a specific key.
	 * We have to take a value that is smaller than required to make sure we don't overshoot.
	 * We can only be certain we have found the required node when it is our first next node.
	 */
	public ServentInfo getNextNodeForKey(int key) {
		if (isKeyMine(key)) {
			return AppConfig.myServentInfo;
		}
		
		//normally we start the search from our first successor
		int startInd = 0;
		
		//if the key is smaller than us, and we are not the owner,
		//then all nodes up to CHORD_SIZE will never be the owner,
		//so we start the search from the first item in our table after CHORD_SIZE
		//we know that such a node must exist, because otherwise we would own this key
		if (key < AppConfig.myServentInfo.getChordId()) {
			int skip = 1;
			while (successorTable[skip].getChordId() > successorTable[startInd].getChordId()) {
				startInd++;
				skip++;
			}
		}
		
		int previousId = successorTable[startInd].getChordId();
		
		for (int i = startInd + 1; i < successorTable.length; i++) {
			if (successorTable[i] == null) {
				AppConfig.timestampedErrorPrint("Couldn't find successor for " + key);
				break;
			}
			
			int successorId = successorTable[i].getChordId();
			
			if (successorId >= key) {
				return successorTable[i-1];
			}
			if (key > previousId && successorId < previousId) { //overflow
				return successorTable[i-1];
			}
			previousId = successorId;
		}
		//if we have only one node in all slots in the table, we might get here
		//then we can return any item
		return successorTable[0];
	}

	private void updateSuccessorTable() {
		//first node after me has to be successorTable[0]
		
		int currentNodeIndex = 0;
		ServentInfo currentNode = allNodeInfo.get(currentNodeIndex);
		successorTable[0] = currentNode;
		
		int currentIncrement = 2;
		
		ServentInfo previousNode = AppConfig.myServentInfo;
		
		//i is successorTable index
		for(int i = 1; i < chordLevel; i++, currentIncrement *= 2) {
			//we are looking for the node that has larger chordId than this
			int currentValue = (AppConfig.myServentInfo.getChordId() + currentIncrement) % CHORD_SIZE;
			
			int currentId = currentNode.getChordId();
			int previousId = previousNode.getChordId();
			
			//this loop needs to skip all nodes that have smaller chordId than currentValue
			while (true) {
				if (currentValue > currentId) {
					//before skipping, check for overflow
					if (currentId > previousId || currentValue < previousId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				} else { //node id is larger
					ServentInfo nextNode = allNodeInfo.get((currentNodeIndex + 1) % allNodeInfo.size());
					int nextNodeId = nextNode.getChordId();
					//check for overflow
					if (nextNodeId < currentId && currentValue <= nextNodeId) {
						//try same value with the next node
						previousId = currentId;
						currentNodeIndex = (currentNodeIndex + 1) % allNodeInfo.size();
						currentNode = allNodeInfo.get(currentNodeIndex);
						currentId = currentNode.getChordId();
					} else {
						successorTable[i] = currentNode;
						break;
					}
				}
			}
		}
		
	}

	/**
	 * This method constructs an ordered list of all nodes. They are ordered by chordId, starting from this node.
	 * Once the list is created, we invoke <code>updateSuccessorTable()</code> to do the rest of the work.
	 * 
	 */
	public void addNodes(List<ServentInfo> newNodes) {
		allNodeInfo.addAll(newNodes);
		
		allNodeInfo.sort(new Comparator<ServentInfo>() {
			
			@Override
			public int compare(ServentInfo o1, ServentInfo o2) {
				return o1.getChordId() - o2.getChordId();
			}
			
		});
		
		List<ServentInfo> newList = new ArrayList<>();
		List<ServentInfo> newList2 = new ArrayList<>();
		
		int myId = AppConfig.myServentInfo.getChordId();
		for (ServentInfo serventInfo : allNodeInfo) {
			if (serventInfo.getChordId() < myId) {
				newList2.add(serventInfo);
			} else {
				newList.add(serventInfo);
			}
		}
		
		allNodeInfo.clear();
		allNodeInfo.addAll(newList);
		allNodeInfo.addAll(newList2);
		if (newList2.size() > 0) {
			predecessorInfo = newList2.get(newList2.size()-1);
		} else {
			predecessorInfo = newList.get(newList.size()-1);
		}
		
		updateSuccessorTable();
	}

	/**
	 * Adds the file to storage if it's not already present.
	 * @param fileInfo - Object containing all relevent information about the file.
	 */
	public void gitAdd(FileInfo fileInfo) {

		int key = ChordState.chordHash(fileInfo.getPath());
		if (isKeyMine(key)) {
			//Proverimo da li vec imamo fajl
			if (storageMap.containsKey(key))
				AppConfig.timestampedStandardPrint("We already have " + fileInfo.getPath() + ".");

			//Dodajemo kopiju podatka o datoteci
			storageMap.put(key, new FileInfo(fileInfo));
			//Verzija bi trebalo da bude 0, nije bitna za direktorijume
			versionMap.put(key, fileInfo.getVersion());

			//Ako je u pitanju fajl, napravimo datoteku, a ako je datoteka, samo cuvamo podatak o njoj
			if (fileInfo.isFile()) {
				if (!FileUtils.storeFile(AppConfig.STORAGE_DIR, fileInfo, true)) {
					AppConfig.timestampedErrorPrint("Failed to store " + fileInfo.getPath() + ".");
				}
			}

			AppConfig.timestampedStandardPrint("File " + fileInfo.getPath() + " stored successfully.");
		} else {
			AppConfig.timestampedStandardPrint("Not our key, forwarding the file " + fileInfo.getPath() + ".");

			ServentInfo nextNode = getNextNodeForKey(key);
			PutMessage pm = new PutMessage(
					AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
					nextNode.getIpAddress(), nextNode.getListenerPort(), fileInfo);
			MessageUtil.sendMessage(pm);
		}

	}

	//	/**
//	 * The chord get operation. Gets the value locally if key is ours, otherwise asks someone else to give us the value.
//	 * @return <ul>
//	 *			<li>The value, if we have it</li>
//	 *			<li>-1 if we own the key, but there is nothing there</li>
//	 *			<li>-2 if we asked someone else</li>
//	 *		   </ul>
//	 */
//	public int getValue(int key) {
//		if (isKeyMine(key)) {
//			if (valueMap.containsKey(key)) {
//				return valueMap.get(key);
//			} else {
//				return -1;
//			}
//		}
//
//		ServentInfo nextNode = getNextNodeForKey(key);
//		AskGetMessage agm = new AskGetMessage(
//				AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
//				nextNode.getIpAddress(), nextNode.getListenerPort(), String.valueOf(key));
//		MessageUtil.sendMessage(agm);
//
//		return -2;
//	}

	public FileInfo gitPull(String path, int version, String requesterIp, int requesterPort) {

		int key = ChordState.chordHash(path);
		FileInfo toReturn = null;
		if (isKeyMine(key)) {
			//Proverimo da li imamo ovaj fajl
			if (storageMap.containsKey(key)) {
				FileInfo localFile = storageMap.get(key);

				//Ako je u pitanju direktorijum, vracamo podatak o njemu
				if (localFile.isDirectory()) {
					if (version != -1)
						AppConfig.timestampedStandardPrint("Can't specify version for a directory.");

					toReturn = new FileInfo(localFile);
				} else {
					//Ako je u pitanju fajl, proverimo da li je prosledjena verzija, ako nije uzmemo najnoviju
					if (version == -1)
						version = versionMap.get(key);

					//Proverimo da li je korisnik zatrazio noviju verziju nego sto postoji
					if (version > versionMap.get(key)) {
						AppConfig.timestampedStandardPrint("Version " + version +
								" doesn't exist, retrieving latest version: " + versionMap.get(key) + ".");
						version = versionMap.get(key);
					}

					//Ucitamo fajl iz skladista
					String filePath = path + "." + version;
					FileInfo fileInfo = FileUtils.getFileInfoFromPath(AppConfig.STORAGE_DIR, filePath);
					if (fileInfo != null) {
						toReturn = new FileInfo(localFile.getPath(), fileInfo.getContent(), version);
					}
				}
			} else {
				AppConfig.timestampedStandardPrint("We have the key, but file " + path + "<" + key + "> doesn't exist.");
			}
		} else {
			AppConfig.timestampedStandardPrint("Not our key, forwarding the pull request for " + path + ", version " + version + ".");

			ServentInfo nextNode = getNextNodeForKey(key);
			Message askMessage = new AskGetMessage(requesterIp, requesterPort,
					nextNode.getIpAddress(), nextNode.getListenerPort(), path, version);
			MessageUtil.sendMessage(askMessage);
		}

		return toReturn;

	}

	public void gitCommit() {



	}

	public void gitRemove() {



	}

}
