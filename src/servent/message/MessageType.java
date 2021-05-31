package servent.message;

public enum MessageType {
	NEW_NODE, WELCOME, SORRY, UPDATE, PUT, ASK_GET,
	TELL_GET, POISON, QUIT, REMOVE, COMMIT, COMMIT_CONFLICT,
	COMMIT_SUCCESS, COMMIT_RESOLVE, COMMIT_ERROR,
}
