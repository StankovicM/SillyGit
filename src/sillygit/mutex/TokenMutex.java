package sillygit.mutex;

import app.AppConfig;
import servent.message.Message;
import servent.message.util.MessageUtil;
import sillygit.servent.message.TokenMessage;

import java.util.concurrent.atomic.AtomicBoolean;

public class TokenMutex {

    private static volatile AtomicBoolean localLock = new AtomicBoolean(false);

    private static volatile boolean token = false;
    private static volatile boolean wantLock = false;

    //Poziva ga samo prvi cvor u sistemu
    public static void init() { token = true; }

    public static void lock() {

        localLock();

        wantLock = true;

        long sleepTime = 1;
        while (!token) {
            try {
                Thread.sleep(sleepTime);
                sleepTime = (sleepTime * 2) > 100 ? 100 : (sleepTime * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    public static void unlock() {

        token = false;
        wantLock = false;

        sendToken();

        localUnlock();

    }

    public static void receiveToken() {

        if (wantLock) {
            token = true;
        } else {
            sendToken();
        }

    }

    private static void sendToken() {

        /*long sleepTime = 1;
        while (true) {
            if (!AppConfig.ALONE)
                break;

            try {
                Thread.sleep(sleepTime);
                sleepTime = (sleepTime * 2) > 100 ? 100 : (sleepTime * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/

        String nextNodeIp = AppConfig.chordState.getNextNodeIp();
        int nextNodePort = AppConfig.chordState.getNextNodePort();

        Message tokenMessage = new TokenMessage(
                AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                nextNodeIp, nextNodePort);
        MessageUtil.sendMessage(tokenMessage);

    }

    private static void localLock() {

        long sleepTime = 1;
        while (true) {
            if (!localLock.get()) {
                if (localLock.compareAndSet(false, true)) {
                    break;
                }
            }

            try {
                Thread.sleep(sleepTime);
                sleepTime = (sleepTime * 2) > 100 ? 100 : (sleepTime * 2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    private static void localUnlock() { localLock.set(false); }

}
