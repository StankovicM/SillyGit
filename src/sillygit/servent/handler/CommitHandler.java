package sillygit.servent.handler;

import app.AppConfig;
import servent.handler.MessageHandler;
import servent.message.Message;
import servent.message.MessageType;
import sillygit.servent.message.CommitMessage;

public class CommitHandler implements MessageHandler {

    private final Message clientMessage;

    public CommitHandler(Message clientMessage) { this.clientMessage = clientMessage; }

    @Override
    public void run() {

        if (clientMessage.getMessageType() == MessageType.COMMIT) {
            CommitMessage commitMessage = (CommitMessage) clientMessage;

            AppConfig.chordState.gitCommit(commitMessage.getFileInfo(), commitMessage.getSenderIpAddress(), commitMessage.getSenderPort());
        } else {
            AppConfig.timestampedErrorPrint("CommitHandler got message that's not of type COMMIT");
        }

    }

}
