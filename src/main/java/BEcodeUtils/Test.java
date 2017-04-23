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

/**
 * Created by ps on 24/3/17.
 */
public class Test {
    public static void main(String[] args) {
        File file=new File("ubuntu.torrent");
        byte[] data;
        try {
            data = FileUtils.readFileToByteArray(file);
            TorrentMeta meta=TorrentMeta.createTorrentMeta(data);
            TrackerSession session=null;
            System.out.println(meta.getAnnounce());
            if(meta.getAnnounce().startsWith("http")){
                session=new HttpTrackerSession(meta);
            }
            else if(meta.getAnnounce().startsWith("udp")){
                session=new UdpTrackerSession(meta);
            }
            TrackerRequestPacket packet= Utils.craftPacket(meta);
            TrakcerResponsePacket response=session.sendRequest(packet);

            Map<InetSocketAddress,String> peer_info=response.getPeer_info();

            List<Peer> peerList=new ArrayList<Peer>();
            for (Map.Entry<InetSocketAddress,String> e:peer_info.entrySet()){
             Peer peer=new Peer(e.getKey(),e.getValue());
                peerList.add(peer);
            }

            PeerConnection connection=new PeerConnection(peerList.get(0),meta);
            connection.connect();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
