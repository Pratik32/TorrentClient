package BEcodeUtils;

import Peers.Peer;
import Peers.PeerConnection;
import Peers.PeerController;
import Tracker.*;
import internal.Constants;
import internal.TorrentMeta;
import internal.Utils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

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
    public static void main(String[] args) throws InterruptedException {
        initialize();
        File file=new File("ubuntu.torrent");
        byte[] data;
        try {
            /*
                Read the torrent file and create TorrentMeta Object from it.
             */
            data = FileUtils.readFileToByteArray(file);
            Constants.logger.debug("Reading torrent file.");
            TorrentMeta meta=TorrentMeta.createTorrentMeta(data);
            Constants.logger.debug("TorrentMeta successfully created.");

            //While testing individual peer uncomment below line and comment controller code.

            //individualPeerTest(meta);

            List<Peer> peerList=getInitialPeerList(meta);
            PeerController controller=new PeerController(meta,peerList);
            //controller.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //temporary method for printing the peer data.(ip,port,peer_id).
    private static void printPeerInfo(Set<InetSocketAddress> addresses){
        System.out.println("Found Peers :");
        Constants.logger.debug("Found following peers.");
       for(InetSocketAddress ip:addresses ){
           Constants.logger.debug(ip.toString());
           System.out.println(ip.toString());
       }
    }
    /*
        This method will is intended to setup environment for
        client.e.g logging service.User interface.
     */
    public static  void initialize(){
        intializeLogger();
    }
    public static void intializeLogger(){
        //PropertyConfigurator.configure("log4j.properties");
        Constants.logger= Logger.getLogger(PeerConnection.class);

    }
    /*
     Method for testing individual peers.

     */
    public  static void individualPeerTest(TorrentMeta meta) throws IOException, InterruptedException {
       List<Peer> peerList=getInitialPeerList(meta);
        PeerConnection connection=new PeerConnection(peerList.get(0),meta,null,null);
        connection.connect();
    }

    public static List<Peer> getInitialPeerList(TorrentMeta meta){
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
        TrackerRequestPacket packet= Utils.craftPacket(meta,0,0,0);
        TrakcerResponsePacket response=session.sendRequest(packet);

        //byte[] in below map represents peer id which does not have any encoding(hence byte[]).
        //peer id may be null ,if compact response is sent by the server.handle that too.
        Map<InetSocketAddress,byte[]> peer_info=response.getPeer_info();//not used for now.
        printPeerInfo(peer_info.keySet());
        List<Peer> peerList=Utils.getPeerList(response);
        return peerList;
    }
}
