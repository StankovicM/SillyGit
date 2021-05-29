package sillygit.cli.command;

import app.AppConfig;
import cli.command.CLICommand;
import sillygit.util.FileInfo;
import sillygit.util.FileUtils;

import java.util.List;

public class GitAddCommand implements CLICommand {

    @Override
    public String commandName() { return "add"; }

    @Override
    public void execute(String args) {

        if (args == null || args.isEmpty()) {
            AppConfig.timestampedStandardPrint("Invalid argument for add command. Should be add name.");
            return;
        }

        String name = args.replace('/' , '\\');
        String path = AppConfig.WORKING_DIR + "\\" + name;

        if (FileUtils.isPathFile(path)) {
            FileInfo fileInfo = FileUtils.getFileInfoFromPath(path);
            if (fileInfo != null) {
                AppConfig.chordState.gitAdd(fileInfo);
            }
        } else {
            List<FileInfo> fileInfoList = FileUtils.getDirectoryInfoFromPath(path);
            if (!fileInfoList.isEmpty()) {
                for (FileInfo fileInfo : fileInfoList) {
                    AppConfig.chordState.gitAdd(fileInfo);
                }
            }
        }

    }

}
