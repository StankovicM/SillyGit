package app;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.InputMismatchException;
import java.util.Scanner;

import servent.message.NewNodeMessage;
import servent.message.util.MessageUtil;

public class ServentInitializer implements Runnable {

	private String getSomeServent() {

		String bsAddress = AppConfig.BOOTSTRAP_ADDRESS;
		int bsPort = AppConfig.BOOTSTRAP_PORT;

		String toReturn = null;
		try {
			Socket bsSocket = new Socket(bsAddress, bsPort);
			
			PrintWriter bsWriter = new PrintWriter(bsSocket.getOutputStream());
			bsWriter.write("Hail\n" + AppConfig.myServentInfo.getIpAddress() + ":" + AppConfig.myServentInfo.getListenerPort() + "\n");
			bsWriter.flush();
			
			Scanner bsScanner = new Scanner(bsSocket.getInputStream());
			try {
				bsScanner.nextInt();
			} catch (InputMismatchException e) {
				//Dobili smo ip:port kao odgovor a ne kod
				toReturn = bsScanner.nextLine();
			}

			bsSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		return toReturn;

	}
	
	@Override
	public void run() {

		String someServent = getSomeServent();

		//We are the first node in the system
		if (someServent == null) {
			AppConfig.timestampedStandardPrint("First node in Chord system.");
		//Bootstrap gave us ip:port of a node - let that node tell our successor that we are here
		} else {
			String someServentIp = someServent.substring(0, someServent.indexOf(':'));
			int someServentPort = Integer.parseInt(someServent.substring(someServentIp.length() + 1));
			NewNodeMessage nnm = new NewNodeMessage(
					AppConfig.myServentInfo.getIpAddress(),
					AppConfig.myServentInfo.getListenerPort(),
					someServentIp, someServentPort);
			MessageUtil.sendMessage(nnm);
		}

	}

}
