package internal;

import org.apache.commons.io.FileUtils;
import peer.Peer;
import tracker.TrackerRequestPacket;
import tracker.TrakcerResponsePacket;
import org.apache.log4j.Logger;

import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by ps on 6/4/17.
 * This class has been created with an intention that,it will contain general
 * purpose methods like crafting different packets(tracker:request,response,DHT queries)
 * parsing bencoded responses,writing bytes to files.
 */
public class Utils {

    private static  Logger logger=CustomLogger.getInstance();
    public static TrackerRequestPacket craftPacket(TorrentMeta meta,long uploaded,long downloaded,long left){
        logger.debug("Crafting tracker request packet");
        TrackerRequestPacket packet=new TrackerRequestPacket(TrackerRequestPacket.Event.STARTED,downloaded,uploaded,left);
        return packet;
    }

    /*
      function for obtaining peer from response object.
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
        Reads a block from file.
        called by PeerController for sending 'piece' messages to remote peers.
     */
    public static byte[] readFromFile(String filename,int pieceNumber,int pieceLen,int offset,int blockLen){
        byte data[]=new byte[blockLen];
        RandomAccessFile file=null;
        System.out.println("Reading block from file  "+pieceNumber+" "+"Block "+offset);
        logger.debug("Reading block from file  "+pieceNumber+" "+"Block "+offset);
        try{
            file=new RandomAccessFile(filename,"r");
            file.seek(pieceNumber*pieceLen+offset);
            file.read(data);
            file.close();

        }catch (FileNotFoundException e){
            System.out.println("File not found");
            logger.error("Requested file not find.");
        }catch (IOException e){
            logger.error("Unable to write to file.");
        }
        return data;
    }

    public static void writeToFile(String filename,int offset,byte data[]){
        RandomAccessFile file=null;

        try{
            file=new RandomAccessFile(filename,"rw");
            file.seek(offset);
            file.write(data);
        }catch (FileNotFoundException e){
            System.out.println("File not found: "+filename);
            logger.error("File not found: "+filename);
        } catch (IOException e) {
            System.out.println("Unable to write to file.");
            logger.error("Unable to write to file");
        }

    }
    public static void readFromFile(String filename,int offset,byte data[]){
        RandomAccessFile file=null;
        try{
            file=new RandomAccessFile(filename,"r");
            file.seek(offset);
            file.read(data);
        }catch (FileNotFoundException e){
            System.out.println("File not found :"+ filename);
            logger.error("File not found: "+filename);
        }catch (IOException e){

        }
    }
    public static int generateRandomNumber(){
        Random random=new Random();
        return random.nextInt();
    }

    public static String getConvertedBytes(long bytes){
        if (bytes<1024){
            return bytes+"B";
        }
        String units[]={"KB","MB","GB","TB","PB"};
        int exp=(int)(Math.log(bytes)/Math.log(1024));
        double res=(bytes/Math.pow(1024,exp));
        return String.format("%.2f %s",res,units[exp-1]);
    }
    /*
        call this method inorder to create a TorrentMeta
     */
    public static TorrentMeta createTorrentMeta(String filename){
        System.out.println("Thread Id is:"+Thread.currentThread().getName());
        File file=new File(filename);
        TorrentMeta meta=null;
        try {
            byte data[] = FileUtils.readFileToByteArray(file);
            meta=TorrentMeta.createTorrentMeta(data);
        }catch (IOException e){
            System.out.println("[Utils]:Error");
        }
        return meta;
    }
    /*
        Method yet to be completed.
     */
    public static String getETAString(double time){
        String timeString=null;
        return timeString;
    }
}
