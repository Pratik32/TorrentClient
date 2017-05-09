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
    long downloaded;
    long uploaded;
    long left;


    public TrackerRequestPacket(Event event,long downloaded, long uploaded, long left){
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

    public long getDownloaded() {
        return downloaded;
    }

    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }

    public long getUploaded() {
        return uploaded;
    }

    public void setUploaded(int uploaded) {
        this.uploaded = uploaded;
    }

    public long getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }



}
