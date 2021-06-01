package sillygit.cli.command;

import app.AppConfig;
import cli.command.CLICommand;
import sillygit.util.CommitCollector;
import sillygit.util.FileUtils;

public class ConflictPullCommand implements CLICommand {

    @Override
    public String commandName() { return "conflictpull"; }

    @Override
    public void execute(String args) {

        if (CommitCollector.isConflicted()) {
            FileUtils.storeFile(AppConfig.WORKING_DIR, CommitCollector.getOldFile(), false);
            AppConfig.chordState.updateLocalWorkingVersion(CommitCollector.getOldFile(),
                    FileUtils.getLastModified(AppConfig.WORKING_DIR, CommitCollector.getOldFile().getPath()));
            CommitCollector.conflictResolved();
        } else {
            AppConfig.timestampedErrorPrint("No conflict has happened.");
        }

    }

}
