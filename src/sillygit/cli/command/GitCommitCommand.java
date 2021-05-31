package sillygit.cli.command;

import app.AppConfig;
import cli.command.CLICommand;
import sillygit.util.CommitCollector;

public class GitCommitCommand implements CLICommand {

    @Override
    public String commandName() { return "commit"; }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for add command. Should be add path.");
            return;
        }

        String path = args.replace('/' , '\\');

        /*TODO
         * Proveriti da li je fajl bio menjan, mozemo da zabelezimo lastmodified prilikom pull-a,
         * pa onda da ga opet proverimo prilikom commit-a i ako nije menjan, ne komitujemo
         */

        Thread t = new Thread(new CommitCollector(path));
        t.start();

    }

}
