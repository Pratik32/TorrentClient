package BEcodeUtils;

import Peers.Peer;
import Peers.PeerConnection;
import Tracker.*;
import internal.TorrentMeta;
import internal.Utils;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ps on 24/3/17.
 */
public class Test {
    public static void main(String[] args) {
        File file=new File("ubuntu.torrent");
        byte[] data;
        try {
            /*
                Read the torrent file and create TorrentMeta Object from it.
             */
            data = FileUtils.readFileToByteArray(file);
            TorrentMeta meta=TorrentMeta.createTorrentMeta(data);
            /*
                Create a tracker session check it's type(http/udp).
             */
            TrackerSession session=null;
            System.out.println(meta.getAnnounce());
            if(meta.getAnnounce().startsWith("http")){
                session=new HttpTrackerSession(meta);
            }
            else if(meta.getAnnounce().startsWith("udp")){
                session=new UdpTrackerSession(meta);
            }
            /*
                Create a request packet and and obtain a response packet.
             */
            TrackerRequestPacket packet= Utils.craftPacket(meta);
            TrakcerResponsePacket response=session.sendRequest(packet);

            //byte[] in below map represents peer id which does not have any encoding(hence byte[]).
            //peer id may be null ,if compact response is sent by the server.handle that too.
            Map<InetSocketAddress,byte[]> peer_info=response.getPeer_info();//not used for now.
            printPeerInfo(peer_info.keySet());
            List<Peer> peerList=Utils.getPeerList(response);
            /*
               Connect to one of the peers for testing.
             */
            PeerConnection connection=new PeerConnection(peerList.get(0),meta);
            connection.connect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //temporary method for printing the peer data.(ip,port,peer_id).
    private static void printPeerInfo(Set<InetSocketAddress> addresses){
        System.out.println("Found Peers :");
       for(InetSocketAddress ip:addresses ){
           System.out.println(ip.toString());
       }
    }
}
