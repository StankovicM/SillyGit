package sillygit.util;

import app.AppConfig;
import servent.message.Message;
import servent.message.MessageType;
import sillygit.servent.message.CommitConflictMessage;
import sillygit.servent.message.CommitErrorMessage;
import sillygit.servent.message.CommitSuccessMessage;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CommitCollector implements Runnable {

    private static volatile boolean working = true;

    private static final Object pendingMessagesLock = new Object();
    private static final Set<Message> pendingMessages = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final String path;

    private static volatile boolean conflicted = false;

    private static FileInfo conflictOldFile;
    private static FileInfo conflictNewFile;

    private static String conflictStorageIp;
    private static int conflictStoragePort;

    public CommitCollector(String path) {

        this.path = path;

    }

    @Override
    public void run() {

        List<FileInfo> expectedList = new ArrayList<>();

        //Proverimo da li se radi o fajlu ili o direktorijumu
        if (FileUtils.isPathFile(AppConfig.WORKING_DIR, path)) {
            //Ako je u pitanju fajl, proverimo da li je uopste modifikovan
            if (FileUtils.isModified(AppConfig.WORKING_DIR, path)) {
                //Ucitamo fajl
                FileInfo tmp = FileUtils.getFileInfoFromPath(AppConfig.WORKING_DIR, path);
                if (tmp != null) {
                    FileInfo fileInfo = new FileInfo(tmp.getPath(), tmp.isDirectory(), tmp.getContent(),
                            AppConfig.chordState.getWorkingVersion(path), tmp.getSubFiles());
                    //Komitujemo fajl i dodamo u listu za cekanje odgovora
                    AppConfig.chordState.gitCommit(fileInfo, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());

                    expectedList.add(fileInfo);
                }
            } else {
                AppConfig.timestampedStandardPrint(path + " hasn't been modified. Not committing.");
            }
        } else {
            List<FileInfo> fileInfoList = FileUtils.getDirectoryInfoFromPath(AppConfig.WORKING_DIR, path);
            if (!fileInfoList.isEmpty()) {
                //Prodjemo kroz listu FileInfo-a za sve datoteke u direktorijumu
                for (FileInfo tmp : fileInfoList) {
                    FileInfo fileInfo = new FileInfo(tmp.getPath(), tmp.isDirectory(), tmp.getContent(),
                            AppConfig.chordState.getWorkingVersion(tmp.getPath()), tmp.getSubFiles());
                    if (!working)
                        break;

                    if (fileInfo.isDirectory())
                        continue;

                    //Za svaki slucaj provera
                    if (fileInfo != null) {
                        //Proverimo da li je fajl menjan
                        if (FileUtils.isModified(AppConfig.WORKING_DIR, fileInfo.getPath())) {
                            AppConfig.chordState.gitCommit(fileInfo, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());

                            expectedList.add(fileInfo);
                        } else {
                            AppConfig.timestampedStandardPrint(fileInfo.getPath() + " hasn't been modified. Not committing.");
                        }
                    }
                }
            }
        }

        long sleepTime = 1;
        long timeoutCounter = 0;

        while (!expectedList.isEmpty()) {
            if (!working)
                break;

            Iterator<Message> iterator = pendingMessages.iterator();
            while (iterator.hasNext()) {
                Message message = iterator.next();
                //Proverimo tip poruke
                if (message.getMessageType() == MessageType.COMMIT_SUCCESS) {
                    CommitSuccessMessage successMessage = (CommitSuccessMessage) message;
                    /* Ako je success poruka, azuriramo lokalnu radnu verziju na novu verziju,
                     * zapisemo lastModified atribut i uklonimo poruku iz reda poruka i fajl
                     * iz liste ocekivanih fajlova
                     */
                    if (expectedList.contains(successMessage.getFileInfo())) {
                        FileInfo fileInfo = successMessage.getFileInfo();
                        AppConfig.chordState.updateLocalWorkingVersion(fileInfo, FileUtils.getLastModified(AppConfig.WORKING_DIR, fileInfo.getPath()));

                        AppConfig.timestampedStandardPrint(fileInfo.getPath() + " successfully committed.");

                        iterator.remove();
                        expectedList.remove(fileInfo);

                        sleepTime = 1;
                        timeoutCounter = 0;
                        break;
                    }
                } else if (message.getMessageType() == MessageType.COMMIT_ERROR) {
                    CommitErrorMessage errorMessage = (CommitErrorMessage) message;
                    //Ako je doslo do greske, prijavimo o kakvoj se gresci radi i uklonimo poruku iz reda poruka
                    //i fajl iz liste ocekivanih fajlova
                    if (expectedList.contains(errorMessage.getFileInfo())) {
                        FileInfo fileInfo = errorMessage.getFileInfo();
                        int code = errorMessage.getCode();
                        if (code == -1) {
                            AppConfig.timestampedErrorPrint(fileInfo.getPath() + " needs to be added first.");
                        } else if (code == -2) {
                            AppConfig.timestampedErrorPrint("An error occurred while committing " + fileInfo.getPath());
                        }

                        iterator.remove();
                        expectedList.remove(fileInfo);

                        sleepTime = 1;
                        timeoutCounter = 0;
                        break;
                    }
                } else if (message.getMessageType() == MessageType.COMMIT_CONFLICT) {
                    CommitConflictMessage conflictMessage = (CommitConflictMessage) message;
                    if (expectedList.contains(conflictMessage.getNewFileInfo())) {
                        FileInfo oldFileInfo = conflictMessage.getOldFileInfo();
                        FileInfo newFileInfo = conflictMessage.getNewFileInfo();

                        AppConfig.timestampedErrorPrint("There was a conflict when committing " + newFileInfo + ". Choose your next action: ");
                        conflictOldFile = oldFileInfo;
                        conflictNewFile = newFileInfo;
                        conflictStorageIp = conflictMessage.getSenderIpAddress();
                        conflictStoragePort = conflictMessage.getSenderPort();
                        conflicted = true;
                        sleepTime = 1;
                        while (conflicted) {
                            if (!working)
                                break;

                            try {
                                Thread.sleep(sleepTime);
                                sleepTime = (sleepTime * 2) > 100 ? 100 : (sleepTime * 2);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }

                        iterator.remove();
                        expectedList.remove(newFileInfo);

                        sleepTime = 1;
                        timeoutCounter = 0;
                        break;
                    }
                }
            }

            try {
                Thread.sleep(sleepTime);
                timeoutCounter += sleepTime;
                if (timeoutCounter >= PullCollector.COLLECTOR_TIMEOUT) {
                    AppConfig.timestampedErrorPrint("Didn't get commit responses in time. Was expecting: " + expectedList + ".");
                    break;
                }
                sleepTime = (sleepTime * 2) > 100 ? 100 : (sleepTime * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void recordMessage(Message message) {

        synchronized (pendingMessagesLock) {
            pendingMessages.add(message);
        }

    }

    public static boolean isConflicted() { return conflicted; }

    public static FileInfo getOldFile() {

        if (!conflicted) {
            AppConfig.timestampedErrorPrint("No conflict at the moment.");
            return null;
        }

        return conflictOldFile;

    }

    public static FileInfo getNewFile() {

        if (!conflicted) {
            AppConfig.timestampedErrorPrint("No conflict at the moment");
            return null;
        }

        return conflictNewFile;

    }

    public static String getConflictStorageIp() { return conflictStorageIp; }

    public static int getConflictStoragePort() { return conflictStoragePort; }

    public static void conflictResolved() {

        conflicted = false;
        conflictOldFile = null;
        conflictNewFile = null;
        conflictStorageIp = "";
        conflictStoragePort = -1;

    }

    public static void stop() { working = false; }

}
