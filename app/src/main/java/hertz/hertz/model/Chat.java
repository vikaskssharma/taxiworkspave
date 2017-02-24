package hertz.hertz.model;

/**
 * Created by rsbulanon on 11/24/15.
 */
public class Chat {

    private String room;
    private String recipient;
    private String message;
    private String timeStamp;

    public Chat() {
    }

    public Chat(String room, String recipient, String message, String timeStamp) {
        this.room = room;
        this.recipient = recipient;
        this.message = message;
        this.timeStamp = timeStamp;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
