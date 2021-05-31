package sillygit.servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;
import sillygit.servent.message.CommitErrorMessage;
import sillygit.util.CommitCollector;

public class CommitErrorHandler implements MessageHandler {

    private final Message clientMessage;

    public CommitErrorHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.COMMIT_ERROR) {
            CommitErrorMessage errorMessage = (CommitErrorMessage) clientMessage;

            String requester = errorMessage.getRequesterIpAddress() + ":" + errorMessage.getRequesterPort();
            int key = ChordState.chordHash(requester);
            if (key == AppConfig.myServentInfo.getChordId()) {
                CommitCollector.recordMessage(errorMessage);
            } else {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
                Message nextErrorMessage = new CommitErrorMessage(
                        errorMessage.getSenderIpAddress(), errorMessage.getSenderPort(),
                        nextNode.getIpAddress(), nextNode.getListenerPort(),
                        errorMessage.getRequesterIpAddress(), errorMessage.getRequesterPort(), errorMessage.getCode());
                MessageUtil.sendMessage(nextErrorMessage);
            }
        } else {
            AppConfig.timestampedErrorPrint("Error handler got message that's not of type COMMIT_ERROR.");
        }

    }

}
