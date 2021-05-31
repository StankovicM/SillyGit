package sillygit.util;

import app.AppConfig;
import servent.message.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class CommitCollector implements Runnable {

    private static volatile boolean working = true;

    private static final Object pendingMessagesLock = new Object();
    private static final Queue<Message> pendingMessages = new LinkedList<>();

    private final String path;

    public CommitCollector(String path) { this.path = path; }

    @Override
    public void run() {

        /*TODO
         * Vrtimo se u petlji dokle god ne prodjemo kroz sve foldere i fajlove na datoj putanji.
         * Ucitavamo ih redom i pozivamo pull u chordstate-u. Kada se pull zavrsi, uspesno ili bezuspesno,
         * dobijamo poruku. Ako je uspesno obavljen, uklonicemo stavku iz reda, a ako nije,
         * javimo da je doslo do konflikta i blokiramo se dok korisnik ne izabere neku od opcija
         */

        //TODO napraviti listu za cekanje odgovora

        //Proverimo da li se radi o fajlu ili o direktorijumu
        if (FileUtils.isPathFile(AppConfig.WORKING_DIR, path)) {
            //Ako je u pitanju fajl, proverimo da li je uopste modifikovan
            if (FileUtils.isModified(AppConfig.WORKING_DIR, path)) {
                //Ucitamo fajl
                FileInfo fileInfo = FileUtils.getFileInfoFromPath(AppConfig.WORKING_DIR, path);
                if (fileInfo != null) {
                    //Komitujemo fajl i dodamo u listu za cekanje odgovora
                    AppConfig.chordState.gitCommit(fileInfo, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());

                    //TODO dodati u listu za cekanje odgovora
                }
            } else {
                AppConfig.timestampedStandardPrint(path + " hasn't been modified. Not committing.");
            }
        } else {
            List<FileInfo> fileInfoList = FileUtils.getDirectoryInfoFromPath(AppConfig.WORKING_DIR, path);
            //TODO proci kroz listu i za sve fajlove pokusati pull i dodati ih u listu za cekanje odgovora
        }

        /*TODO
         * Vrteti se u petlji dok se lista za ocekivane odgovore ne isprazni
         * Ako naidje konflikt blokirati se i pustiti korisnika da odluci o resenju
         * Ako je u pitanju uspeh samo izbaciti fajl iz reda za ocekivane odgovore
         * Ako je doslo do greske obavestiti korisnika, ukloniti fajl i nastaviti
         */


    }

    public static void recordMessage(Message message) {

        synchronized (pendingMessagesLock) {
            pendingMessages.add(message);
        }

    }

    public static void stop() { working = false; }

}
