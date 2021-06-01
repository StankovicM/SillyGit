package sillygit.cli.command;

import app.AppConfig;
import app.ChordState;
import app.ServentInfo;
import cli.command.CLICommand;
import servent.message.Message;
import servent.message.util.MessageUtil;
import sillygit.servent.message.CommitResolveMessage;
import sillygit.util.CommitCollector;

public class ConflictPushCommand implements CLICommand {

    @Override
    public String commandName() { return "conflictpush"; }

    @Override
    public void execute(String args) {

        if (CommitCollector.isConflicted()) {
            String requesterIpAddress = CommitCollector.getConflictStorageIp();
            int requesterPort = CommitCollector.getConflictStoragePort();
            int key = ChordState.chordHash(requesterIpAddress + ":" + requesterPort);
            ServentInfo nextNode = AppConfig.chordState.getNextNodeForKey(key);
            Message resolveMessage = new CommitResolveMessage(
                    AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort(),
                    nextNode.getIpAddress(), nextNode.getListenerPort(),
                    requesterIpAddress, requesterPort, CommitCollector.getNewFile());
            MessageUtil.sendMessage(resolveMessage);
            CommitCollector.conflictResolved();
        } else {
            AppConfig.timestampedErrorPrint("No conflict has happened.");
        }

    }

}
