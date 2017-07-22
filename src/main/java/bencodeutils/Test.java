package bencodeutils;

import internal.CustomLogger;
import org.apache.commons.io.FileUtils;
import peer.Peer;
import peer.PeerConnection;
import peer.PeerController;
import tracker.*;
import dht.DHTConnection;
import internal.Constants;
import internal.TorrentMeta;
import internal.Utils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

import static internal.Constants.RESOURCE_DIR;

/**
 * Created by ps on 24/3/17.
 */
public class Test {
     static Logger logger= CustomLogger.getInstance();
    public static void main(String[] args) throws InterruptedException, IOException {
        DHTConnection connection=new DHTConnection();
        connection.connect();
        initialize();
         File file=new File(RESOURCE_DIR+"music.torrent");
        byte[] data;
        try {
            /*
                Read the torrent file and create TorrentMeta Object from it.
             */
            data = FileUtils.readFileToByteArray(file);
            logger.debug("Reading torrent file.");
            TorrentMeta meta=TorrentMeta.createTorrentMeta(data);
            logger.debug("TorrentMeta successfully created.");

            //While testing individual peer uncomment below line and comment controller code.

            //individualPeerTest(meta);

            List<Peer> peerList=getInitialPeerList(meta);
            PeerController controller=new PeerController(meta,peerList);
            controller.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    //temporary method for printing the peer data.(ip,port,peer_id).
    private static void printPeerInfo(Set<InetSocketAddress> addresses){
        System.out.println("Found peer :");
        logger.debug("Found following peers.");
       for(InetSocketAddress ip:addresses ){
           logger.debug(ip.toString());
           System.out.println(ip.toString());
       }
    }
    /*
        This method is intended to setup environment for
        client.e.g logging service.User interface.
     */
    public static  void initialize(){
        intializeLogger();
    }
    public static void intializeLogger(){
        //PropertyConfigurator.configure("log4j.properties");
        logger= CustomLogger.getInstance();
    }
    /*
     Method for testing individual peers.

     */
    public  static void individualPeerTest(TorrentMeta meta) throws IOException, InterruptedException {
       List<Peer> peerList=getInitialPeerList(meta);
        PeerConnection connection=new PeerConnection(peerList.get(1),meta,null,null);
        connection.connect();
    }

    /*
        Try all the tracker one by one and collect peers.
        Right now using only one tracker for collecting peers.
     */
    public static List<Peer> getInitialPeerList(TorrentMeta meta){
        TrackerSession session=null;
        System.out.println(meta.getAnnounce());

        List<String> trackerUrls=meta.getAnnouce_list();
        trackerUrls.add(0,meta.getAnnounce());
        TrackerRequestPacket packet= Utils.craftPacket(meta,0,0,meta.getTotalFilesize());
        TrakcerResponsePacket response=null;
        List<Peer> peerList=new ArrayList<Peer>();
        Set<Peer> peers=new HashSet<Peer>();
        for(String str:trackerUrls){
            if(str.startsWith("http")){
                session=new HttpTrackerSession(meta,str);
            }
            else if(str.startsWith("udp")){
                session=new UdpTrackerSession(meta,str);
            }
            else {
                System.out.println("Invalid tracker url :"+ str);
                logger.error("Invalid tracker url :"+ str);
                continue;
            }
            response=session.sendRequest(packet);
            if(response==null){
                System.out.println("Connection time out from :"+ str);
                logger.error("Connection time out from :"+str);
                continue;
            }
            Map<InetSocketAddress,byte[]> peer_info=response.getPeer_info();//for debugging purpose.
            printPeerInfo(peer_info.keySet());//for debugging purpose
            List<Peer> temp=Utils.getPeerList(response);
            peerList.addAll(temp);
            break;
            //peers.addAll(temp);
        }
        //System.out.println("Peer set is :");
        //System.out.println(peers);
        return peerList;
    }
}
