package servent.handler;

import servent.message.Message;

public class NodeQuitHandler implements MessageHandler {

    private final Message clientMessage;

    public NodeQuitHandler(Message clientMessage) {

        this.clientMessage = clientMessage;

    }

    @Override
    public void run() {



    }

}
