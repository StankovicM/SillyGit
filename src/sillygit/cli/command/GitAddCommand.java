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
            AppConfig.timestampedStandardPrint("Invalid argument for add command. Should be add path.");
            return;
        }

        String path = args.replace('/' , '\\');

        if (FileUtils.isPathFile(AppConfig.WORKING_DIR, path)) {
            FileInfo fileInfo = FileUtils.getFileInfoFromPath(AppConfig.WORKING_DIR, path);
            if (fileInfo != null) {
                AppConfig.chordState.gitAdd(fileInfo, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());
            }
        } else {
            List<FileInfo> fileInfoList = FileUtils.getDirectoryInfoFromPath(AppConfig.WORKING_DIR, path);
            if (!fileInfoList.isEmpty()) {
                for (FileInfo fileInfo : fileInfoList) {
                    AppConfig.chordState.gitAdd(fileInfo, AppConfig.myServentInfo.getIpAddress(), AppConfig.myServentInfo.getListenerPort());
                }
            }
        }

    }

}
