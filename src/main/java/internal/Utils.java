package internal;

import Peers.Peer;
import Tracker.TrackerRequestPacket;
import Tracker.TrakcerResponsePacket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ps on 6/4/17.
 * This class has been created with an intention that,it will contain general
 * purpose methods like crafting different packets(Tracker:request,response)
 * parsing bencoded responses.new functionalities will be added as needed.
 */
public class Utils {

    public static TrackerRequestPacket craftPacket(TorrentMeta meta){
        //TrackerRequestPacket.Event event = TrackerRequestPacket.Event.STARTED;

        Constants.logger.debug("Crafting tracker request packet");
        TrackerRequestPacket packet=new TrackerRequestPacket(TrackerRequestPacket.Event.STARTED,1028128768,0,0);
        return packet;
    }

    /*
      function for obtaining Peers from response object.
      First check if peer id is null,response sent by server maybe compact.
     */
    public static List<Peer> getPeerList(TrakcerResponsePacket packet){
        List<Peer> peerList=new ArrayList<Peer>();
        Map<InetSocketAddress,byte[]> map=packet.getPeer_info();
            for (Map.Entry<InetSocketAddress,byte[]> e:map.entrySet()){
                Peer peer=new Peer(e.getKey(),e.getValue());
                peerList.add(peer);
            }
        return peerList;
    }

}
