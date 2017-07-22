package tracker;

import java.net.InetSocketAddress;
import java.util.Map;

/**
 * Created by ps on 2/4/17.
 * Represents tracker response.
 */
public class TrakcerResponsePacket {

    private int statusCode;
    Map<InetSocketAddress,byte[]> peer_info;
    int interval;
    int min_interval;
    int complete;
    int incomplete;


    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
    }

    public int getMin_interval() {
        return min_interval;
    }

    public void setMin_interval(int min_interval) {
        this.min_interval = min_interval;
    }

    public int getComplete() {
        return complete;
    }

    public void setComplete(int complete) {
        this.complete = complete;
    }

    public int getIncomplete() {
        return incomplete;
    }

    public void setIncomplete(int incomplete) {
        this.incomplete = incomplete;
    }

    public Map<InetSocketAddress, byte[]> getPeer_info() {
        return peer_info;
    }

    public void setPeer_info(Map<InetSocketAddress, byte[]> peer_info) {
        this.peer_info = peer_info;
    }


}
