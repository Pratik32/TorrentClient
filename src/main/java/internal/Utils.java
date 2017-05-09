package internal;

import Peers.Peer;
import Tracker.TrackerRequestPacket;
import Tracker.TrakcerResponsePacket;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ps on 6/4/17.
 * This class has been created with an intention that,it will contain general
 * purpose methods like crafting different packets(Tracker:request,response)
 * parsing bencoded responses,writing bytes to files..new functionalities will be added as needed.
 */
public class Utils {

    public static TrackerRequestPacket craftPacket(TorrentMeta meta,long uploaded,long downloaded,long left){
        Constants.logger.debug("Crafting tracker request packet");
        TrackerRequestPacket packet=new TrackerRequestPacket(TrackerRequestPacket.Event.STARTED,downloaded,uploaded,left);
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


    /*
        This method will accept a piece(basically a byte array) and will write it
         to the destination file.Function parameters are yet to be decided.
     */
    public static void writeToFile(String filename,byte data[],int pieceNumber,int pieceLength){
        File file=new File(filename);
        System.out.println("Writing to file :"+filename+"piece number :"+pieceNumber);
        try {
            DataOutputStream stream=new DataOutputStream(new FileOutputStream(file));
            stream.write(data,pieceNumber,pieceLength);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done writing to file.");
    }

}
