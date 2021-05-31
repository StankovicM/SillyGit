package sillygit.cli.command;

import app.AppConfig;
import cli.command.CLICommand;
import sillygit.util.FileInfo;
import sillygit.util.FileUtils;

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

        /* Prekopirano iz GitAddCommand
        if (FileUtils.isPathFile(AppConfig.WORKING_DIR, path)) {
            FileInfo fileInfo = FileUtils.getFileInfoFromPath(AppConfig.WORKING_DIR, path);
            if (fileInfo != null) {
                AppConfig.chordState.gitAdd(fileInfo);
            }
        } else {
            List<FileInfo> fileInfoList = FileUtils.getDirectoryInfoFromPath(AppConfig.WORKING_DIR, path);
            if (!fileInfoList.isEmpty()) {
                for (FileInfo fileInfo : fileInfoList) {
                    AppConfig.chordState.gitAdd(fileInfo);
                }
            }
        }
        */

        if (FileUtils.isPathFile(AppConfig.WORKING_DIR, path)) {
            FileInfo fileInfo = FileUtils.getFileInfoFromPath(AppConfig.WORKING_DIR, path);

        }

    }

}
