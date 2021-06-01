package sillygit.servent.handler;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import servent.message.util.MessageUtil;
import sillygit.servent.message.CommitResolveMessage;
import sillygit.util.FileInfo;

public class CommitResolveHandler implements MessageHandler {

    private final Message clientMessage;

    public CommitResolveHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.COMMIT_RESOLVE) {
            CommitResolveMessage resolveMessage = (CommitResolveMessage) clientMessage;

            String requester = resolveMessage.getRequesterIpAddress() + ":" + resolveMessage.getRequesterPort();
            int key = ChordState.chordHash(requester);
            if (key == AppConfig.myServentInfo.getChordId()) {
                FileInfo fileInfo = resolveMessage.getFileInfo();
                AppConfig.chordState.resolveConflict(fileInfo);
            } else {
                ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
                Message nextResolveMessage = new CommitResolveMessage(
                        resolveMessage.getSenderIpAddress(), resolveMessage.getSenderPort(),
                        nextNode.getIpAddress(), nextNode.getListenerPort(),
                        resolveMessage.getRequesterIpAddress(), resolveMessage.getRequesterPort(),
                        resolveMessage.getFileInfo());
                MessageUtil.sendMessage(nextResolveMessage);
            }
        } else {
            AppConfig.timestampedErrorPrint("Resolve handler got message that's not of type COMMIT_RESOLVE.");
        }

    }

}
