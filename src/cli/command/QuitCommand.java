package cli.command;

import app.AppConfig;
import cli.CLIParser;
import servent.SimpleServentListener;

public class QuitCommand implements CLICommand {

    private final CLIParser parser;
    private final SimpleServentListener listener;

    public QuitCommand(CLIParser parser, SimpleServentListener listener) {

        this.parser = parser;
        this.listener = listener;

    }

    @Override
    public String commandName() { return "quit"; }

    @Override
    public void execute(String args) {

        AppConfig.timestampedStandardPrint("Quitting...");
        parser.stop();
        listener.stop();

    }

}
