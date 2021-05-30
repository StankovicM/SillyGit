package sillygit.util;

import app.AppConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class PullCollector implements Runnable {

    public static final int COLLECTOR_TIMEOUT = 10000;

    private static volatile boolean working = true;

    //Zajednicki skup FileInfo objekata za sve collectore. Omogucava da se vise pullova izvrsava u isto vreme
    private static final Set<FileInfo> collectedFileInfo = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final String path;
    private final int version;

    public PullCollector(String path, int version) {

        this.path = path;
        this.version = version;

    }

    @Override
    public void run() {

        /*TODO
         * Korak 1: pozovemo gitPull i cekamo da dobijemo odgovor
         * Korak 2: proverimo da li smo dobili fajl ili folder
         * Korak 3: ako je fajl, zapisemo ga u working directory, i zavrsavamo
         * Korak 4: ako je folder, kreiramo njega kao i celu njegovu pod-strukturu
         */

        //Pravimo listu datoteka za koje cekamo informacije i dodajemo putanju za koju ja zapocet pull
        Set<String> expectedFileInfo = new HashSet<>();
        expectedFileInfo.add(path);

        //Saljemo prvi pull zahtev
        FileInfo firstFileInfo = AppConfig.chordState.gitPull(path, version,
                AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());
        if (firstFileInfo != null) {
            addFileInfo(firstFileInfo);
        }

        //Pravimo listu FileInfo objekata iz koje cemo posle napraviti celu strukturu direktorijuma i fajlova
        //U working direktorijumu
        List<FileInfo> fileInfoList = new ArrayList<>();
        int sleepTime = 1;

        int timeoutCounter = 0;
        boolean timeout = false;

        //Vrtimo se u petlji dokle god ima fajlova koje ocekujemo
        while (!expectedFileInfo.isEmpty()) {
            if (!working) break;

            //Prolazimo kroz listu prikupljenih podataka o fajlovima
            Iterator<FileInfo> iterator = collectedFileInfo.iterator();
            while (iterator.hasNext()) {
                FileInfo fileInfo = iterator.next();
                String path = fileInfo.getPath();

                //Proverimo da li smo dobili neki od ocekivanih fajlova
                if (expectedFileInfo.contains(path)) {
                    //Ako je u pitanju direktorijum, dodajemo sve pod-direktorijume i datoteke u spisak ocekivanih
                    //i saljemo pull upite za njih
                    if (fileInfo.isDirectory()) {
                        for (String s : fileInfo.getSubFiles()) {
                            expectedFileInfo.add(s);
                            FileInfo pulledFileInfo = AppConfig.chordState.gitPull(
                                    s, version, AppConfig.myServentInfo.getIpAddress(),
                                    AppConfig.myServentInfo.getListenerPort());
                            if (pulledFileInfo != null) {
                                //Ako je fajl bio kod nas, dodajemo ga u skup prikupljenih fajlova
                                addFileInfo(pulledFileInfo);
                            }
                        }
                    }

                    AppConfig.timestampedStandardPrint("Got " + fileInfo);

                    //Dodajemo informacije za trenutnu datoteku u konacnu listu, uklanjamo fajl iz prikupljenih i ocekivanih
                    fileInfoList.add(fileInfo);
                    iterator.remove();
                    expectedFileInfo.remove(path);

                    sleepTime = 1;
                    timeoutCounter = 0;
                    break;
                }
            }

            try {
                Thread.sleep(sleepTime);
                timeoutCounter += sleepTime;
                if (timeoutCounter >= COLLECTOR_TIMEOUT) {
                    AppConfig.timestampedErrorPrint("Didn't get pull responses in time. Was expecting: " + expectedFileInfo + ".");
                    timeout = true;
                    break;
                }
                sleepTime = sleepTime * 2 > 100 ? 100 : sleepTime * 2;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (working && !timeout) {
            AppConfig.timestampedStandardPrint("Pulled file(s) successfully " + fileInfoList.size());

            //TODO napraviti novu metodu u fileutils koja ce napraviti od ove liste strukturu fajlova
        }

    }

    public static void addFileInfo(FileInfo fileInfo) {

        collectedFileInfo.add(fileInfo);

    }

    public static void stop() { working = false; }

}
