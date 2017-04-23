package Tracker;

/**
 * Created by ps on 2/4/17.
 */
public class TrackerRequestPacket {
    public enum Event{
        STARTED,
        STOPPED,
        COMPLETED
    }
    Event event;
    int downloaded;
    int uploaded;
    int left;


    public TrackerRequestPacket(Event event, int downloaded, int uploaded, int left){
        this.event=event;
        this.downloaded=downloaded;
        this.uploaded=uploaded;
        this.left=left;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public int getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }

    public int getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }



}
