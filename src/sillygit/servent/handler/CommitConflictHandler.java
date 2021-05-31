package sillygit.servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;
import sillygit.servent.message.CommitConflictMessage;
import sillygit.util.CommitCollector;

public class CommitConflictHandler implements MessageHandler {

    private final Message clientMessage;

    public CommitConflictHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.COMMIT_CONFLICT) {
            CommitConflictMessage conflictMessage = (CommitConflictMessage) clientMessage;

            String requester = conflictMessage.getRequesterIpAddress() + ":" + conflictMessage.getRequesterPort();
            int key = ChordState.chordHash(requester);
            if (key == AppConfig.myServentInfo.getChordId()) {
                CommitCollector.recordMessage(conflictMessage);
            } else {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
                Message nextConflictMessage = new CommitConflictMessage(
                        conflictMessage.getSenderIpAddress(), conflictMessage.getSenderPort(),
                        nextNode.getIpAddress(), nextNode.getListenerPort(),
                        conflictMessage.getRequesterIpAddress(), conflictMessage.getRequesterPort(),
                        conflictMessage.getOldFileInfo(), conflictMessage.getNewFileInfo());
                MessageUtil.sendMessage(nextConflictMessage);
            }
        } else {
            AppConfig.timestampedErrorPrint("Conflict handler got message that's not of type CONFLICT.");
        }

    }

}
