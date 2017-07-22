package internal;

import bencodeutils.BencodeUtils;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import peer.Peer;
import peer.PeerController;
import tracker.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;

import static internal.Constants.HTTP;
import static internal.Constants.HTTPS;
import static internal.Constants.UDP;

/**
 * Created by ps on 27/6/17.
 * Scheduler schedules a new torrent session.
 * It creates a PeerController and starts it.
 * All the pre-computation required to start a PeerController
 * are done by Scheduler which includes bencode decoding,initial
 * tracker request.
 *
 */
public class Scheduler {

    List<PeerController> controllerList;
    String errorString=" ";
    private final String INVALID_ENCODING="Invalid encoding of torrent meta file.";
    private final String SCHEDULAR_STRING="[Scheduler]: ";
    private Logger logger;


    public Scheduler(){
        controllerList=new ArrayList<PeerController>();
        logger=CustomLogger.getInstance();
    }
    /*
        Method is self explanatory.
        It also checks, if same session is already going on.
     */
    public boolean schedule(String filename){
        boolean result=true;
        File file=new File(filename);
        try {
            byte data[]= FileUtils.readFileToByteArray(file);
            TorrentMeta meta=TorrentMeta.createTorrentMeta(data);
            List<Peer> peers=getInitialPeerList(meta);
            if(peers.size()<=0){
                result=false;
                errorString="No peers found.";
                logger.error(SCHEDULAR_STRING+errorString);
                return result;
            }
            PeerController controller=new PeerController(meta,peers);
            controllerList.add(controller);
            controller.start();
        } catch (IOException e) {
            result=false;
            errorString=INVALID_ENCODING;
            logger.error(SCHEDULAR_STRING +errorString);
            return result;
        }


        return result;
    }

    /*

        iterate through all peercontroller and check if
        torrentmetas are equal,return true if equal.
     */
    public boolean sessionExists(String filename){
        try {
            byte data[] = FileUtils.readFileToByteArray(new File(filename));
            TorrentMeta meta = TorrentMeta.createTorrentMeta(data);
            for (PeerController controller : controllerList) {
                if (controller.getTorrentMeta().equals(meta)){
                    return true;
                }
            }
        }catch (IOException e){

        }
        return  false;
    }

    public  PeerController getPeerController(int index){
        return controllerList.get(index);
    }


    /*
        While getting the response,check it's response code and then proceed.
     */
    public  List<Peer> getInitialPeerList(TorrentMeta meta){
        TrackerSession session=null;
        System.out.println(meta.getAnnounce());

        List<String> trackerUrls=meta.getAnnouce_list();
        trackerUrls.add(0,meta.getAnnounce());
        TrackerRequestPacket packet= Utils.craftPacket(meta,0,0,meta.getTotalFilesize());
        TrakcerResponsePacket response=null;
        List<Peer> peerList=new ArrayList<Peer>();
        Set<Peer> peers=new HashSet<Peer>();
        for(String str:trackerUrls){
            if(str.startsWith(HTTP) || str.startsWith(HTTPS)){
                session=new HttpTrackerSession(meta,str);
            }
            else if(str.startsWith(UDP)){
                session=new UdpTrackerSession(meta,str);
            }
            else {
                System.out.println(SCHEDULAR_STRING+"Invalid tracker url :"+ str);
                logger.error(SCHEDULAR_STRING+"Invalid tracker url :"+ str);
                continue;
            }
            response=session.sendRequest(packet);
            if(response.getStatusCode()==0 || response.getStatusCode()==-1){
                System.out.println(SCHEDULAR_STRING+"Connection time out from :"+ str);
                logger.error(SCHEDULAR_STRING+"Connection time out from :"+str);
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


    private  void printPeerInfo(Set<InetSocketAddress> addresses){
        System.out.println(SCHEDULAR_STRING+"Found peer :");
        logger.debug(SCHEDULAR_STRING+"Found following peers.");
        for(InetSocketAddress ip:addresses ){
            logger.debug(ip.toString());
            System.out.println(ip.toString());
        }
    }

    public String getErrorString(){
        return errorString;
    }
    public  PeerController getThisPeerController(){
        return getPeerController(controllerList.size()-1);
    }
}
