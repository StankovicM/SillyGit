package sillygit.servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;
import sillygit.servent.message.CommitSuccessMessage;
import sillygit.util.CommitCollector;

public class CommitSuccessHandler implements MessageHandler {

    private final Message clientMessage;

    public CommitSuccessHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.COMMIT_SUCCESS) {
            CommitSuccessMessage successMessage = (CommitSuccessMessage) clientMessage;

            String requester = successMessage.getRequesterIpAddress() + ":" + successMessage.getRequesterPort();
            int key = ChordState.chordHash(requester);
            if (key == AppConfig.myServentInfo.getChordId()) {
                CommitCollector.recordMessage(successMessage);
            } else {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
                Message nextSuccessMessage = new CommitSuccessMessage(
                        successMessage.getSenderIpAddress(), successMessage.getSenderPort(),
                        nextNode.getIpAddress(), nextNode.getListenerPort(),
                        successMessage.getRequesterIpAddress(), successMessage.getRequesterPort(),
                        successMessage.getFileInfo());
                MessageUtil.sendMessage(nextSuccessMessage);
            }
        } else {
            AppConfig.timestampedErrorPrint("Success handler got message that's not of type COMMIT_SUCCESS.");
        }

    }

}
