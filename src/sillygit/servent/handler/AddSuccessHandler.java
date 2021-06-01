package sillygit.servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;
import sillygit.servent.message.AddSuccessMessage;
import sillygit.util.FileInfo;
import sillygit.util.FileUtils;

public class AddSuccessHandler implements MessageHandler {

    private final Message clientMessage;

    public AddSuccessHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.ADD_SUCCESS) {
            AddSuccessMessage successMessage = (AddSuccessMessage) clientMessage;

            String requester = successMessage.getRequesterIpAddress() + ":" + successMessage.getRequesterPort();
            int key = ChordState.chordHash(requester);
            if (key == AppConfig.myServentInfo.getChordId()) {
                FileInfo fileInfo = successMessage.getFileInfo();
                long lastModified = FileUtils.getLastModified(AppConfig.WORKING_DIR, fileInfo.getPath());
                AppConfig.timestampedStandardPrint(fileInfo.getPath() + " stored successfully.");
                AppConfig.chordState.addToWorkingMap(fileInfo, lastModified);
            } else {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
                Message nextSuccessMessage = new AddSuccessMessage(
                        successMessage.getSenderIpAddress(), successMessage.getSenderPort(),
                        nextNode.getIpAddress(), nextNode.getListenerPort(),
                        successMessage.getRequesterIpAddress(), successMessage.getRequesterPort(),
                        successMessage.getFileInfo());
                MessageUtil.sendMessage(nextSuccessMessage);
            }
        } else {
            AppConfig.timestampedErrorPrint("Add success handler got message that's not of type ADD_SUCCESS.");
        }

    }

}
