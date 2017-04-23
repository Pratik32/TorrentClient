package internal;

import Tracker.TrackerRequestPacket;
/**
 * Created by ps on 6/4/17.
 */
public class Utils {

    public static TrackerRequestPacket craftPacket(TorrentMeta meta){
        //TrackerRequestPacket.Event event = TrackerRequestPacket.Event.STARTED;

        TrackerRequestPacket packet=new TrackerRequestPacket(TrackerRequestPacket.Event.STARTED,1028128768,0,0);
        return packet;
    }

}
