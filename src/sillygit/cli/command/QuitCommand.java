package sillygit.cli.command;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import cli.CLIParser;
import cli.command.CLICommand;
import servent.SimpleServentListener;
import servent.message.*;
import servent.message.util.MessageUtil;
import sillygit.mutex.TokenMutex;
import sillygit.util.CommitCollector;
import sillygit.util.FileInfo;
import sillygit.util.FileUtils;
import sillygit.util.PullCollector;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuitCommand implements CLICommand {

    private final CLIParser parser;
    private final SimpleServentListener listener;

    public QuitCommand(CLIParser parser, SimpleServentListener listener) {

        this.parser = parser;
        this.listener = listener;

    }

    @Override
    public String commandName() { return "quit"; }

    @SuppressWarnings("Duplicates")
    @Override
    public void execute(String args) {

        //mutex lock
        TokenMutex.lock();

        //Postavimo flag i zapocinjemo proceduru
        AppConfig.CAN_QUIT = false;

        Map<Integer, FileInfo> storage = AppConfig.chordState.getStorageMap();
        Map<Integer, Integer> versions = AppConfig.chordState.getVersionMap();
        Map<Integer, List<FileInfo>> oldVersions = new HashMap<>();
        for (Map.Entry<Integer, FileInfo> m : storage.entrySet()) {
            int version = versions.get(m.getKey());
            for (int i = version; i >= 0; i--) {
                String filePath = m.getValue().getPath() + "." + i;
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

                //FileUtils.removeFile(AppConfig.STORAGE_DIR, filePath);
            }
        }
        //Saljemo direktnom sledbeniku svoje skladiste
        String successorIp = AppConfig.chordState.getNextNodeIp();
        int successorPort = AppConfig.chordState.getNextNodePort();
        Message quitMessage = new NodeQuitMessage(AppConfig.myServentInfo.getIpAddress(),
                AppConfig.myServentInfo.getListenerPort(), successorIp, successorPort,
                AppConfig.chordState.getPredecessor(), storage, versions, oldVersions);
        MessageUtil.sendMessage(quitMessage);

        //Cekamo da nam stigne odgovor od prethodnika da je u redu da se ugasimo
        long sleepTime = 1;
        while (true) {
            if (AppConfig.CAN_QUIT)
                break;

            try {
                Thread.sleep(sleepTime);
                sleepTime = (sleepTime * 2) > 100 ? 100 : (sleepTime * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        //Mozemo da napustimo sistem
        //Brisemo svoje fajlove
        for (Map.Entry<Integer, FileInfo> m : storage.entrySet()) {
            int version = versions.get(m.getKey());
            for (int i = version; i >= 0; i--) {
                String filePath = m.getValue().getPath() + "." + i;
                FileUtils.removeFile(AppConfig.STORAGE_DIR, filePath);
            }
        }

        //mutex unlock
        TokenMutex.unlock();

        //Pogasimo sve niti
        AppConfig.timestampedStandardPrint("Quitting...");
        parser.stop();
        listener.stop();
        PullCollector.stop();
        CommitCollector.stop();

        //Javimo bootstrap-u da nas izbaci iz spiska aktivnih
        String bsAddress = AppConfig.BOOTSTRAP_ADDRESS;
        int bsPort = AppConfig.BOOTSTRAP_PORT;

        try {
            Socket bsSocket = new Socket(bsAddress, bsPort);

            PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
            bsWriter.write("Bye\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");
            bsWriter.flush();

            bsSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
