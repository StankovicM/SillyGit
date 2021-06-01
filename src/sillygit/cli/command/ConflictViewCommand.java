package sillygit.cli.command;

import app.AppConfig;
import cli.command.CLICommand;
import sillygit.util.CommitCollector;

public class ConflictViewCommand implements CLICommand {

    @Override
    public String commandName() { return "conflictview"; }

    @Override
    public void execute(String args) {

        if (CommitCollector.isConflicted()) {
            AppConfig.timestampedStandardPrint("Stored version: " + CommitCollector.getOldContent());
            AppConfig.timestampedStandardPrint("Your version: " + CommitCollector.getNewContent());
            AppConfig.timestampedStandardPrint("Would you like to pull stored changes or push yours?");
        } else {
            AppConfig.timestampedErrorPrint("No conflict to view at the moment.");
        }

    }

}
