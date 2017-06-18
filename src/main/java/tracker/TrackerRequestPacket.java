package tracker;

/**
 * Created by ps on 2/4/17.
 * Represents a tracker request(http,udp)
 */
public class TrackerRequestPacket {
    public enum Event{
        STARTED(2),
        STOPPED(3),
        COMPLETED(1);

        private final int value;
        Event(final int value){
            this.value=value;
        }
        public int getValue(){
            return value;
        }
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
