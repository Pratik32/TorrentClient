package tracker;

import internal.Constants;
import internal.TorrentMeta;
import internal.Utils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;

import static internal.Constants.*;

/**
 * Created by ps on 30/3/17.
 * For UdpTrackerSession some extra work has to be done.
 * First we have to send a 'connection' request which will return a transaction id.
 * transaction id will be used in sending announce request.
 * Extends TrackerSession so as to represent higher level hierarchy.
 * Refer : http://www.bittorrent.org/beps/bep_0015.html
 */
public class UdpTrackerSession extends TrackerSession {

    DatagramSocket socket;
    private int transactionId;
    private int ACTION_CONNECT=0;
    private int ACTION_ANNOUNCE=1;
    private ByteBuffer response;
    public UdpTrackerSession(TorrentMeta meta,String announceUrl){
        super(meta,announceUrl);
    }
    public void connect(TrackerRequestPacket packet) {
        URL url=null;
        logger.debug("Request is of UDP type.");
        try {
            //url = new URL(getTrackerUrl(packet));
            URI uri=new URI(tracker_url);
            InetSocketAddress address=new InetSocketAddress(uri.getHost(),uri.getPort());
            System.out.println("InetAddress is: "+address.toString());
            socket=new DatagramSocket();
            socket.setSoTimeout(TIMEOUT);
            socket.connect(address);

            //send a connection request first with given transaction id.
            logger.debug("Sending UDP  connection request to "+address.toString());
            byte[] data=createConnectionRequest();
            DatagramPacket datagramPacket=new DatagramPacket(data,data.length,address);
            socket.send(datagramPacket);
            ByteBuffer buffer=receive(socket);
            if (buffer==null){
                return;
            }
            long connectionId=parse(buffer);
            logger.debug("Received connectionId :"+connectionId+" "+"sending announce request.");
            byte[] announceRequest=craftAnnounceRequest(connectionId,packet);
            DatagramPacket announcePacket=new DatagramPacket(announceRequest,announceRequest.length,address);
            socket.send(announcePacket);
            response=receive(socket);
            if (response==null){
                return;
            }
            System.out.println(response.getInt());
            System.out.println("transactionId "+response.getInt()+" Interval "+response.getInt()+" leechers "+response.getInt()+" seeders "+response.getInt());
            status=STATUSCODE.OK.getStatus();
        } catch (MalformedURLException e) {
            status=STATUSCODE.MALFORMED_URL.getStatus();
        } catch (IOException e) {
            status=STATUSCODE.TIMOUT.getStatus();
            System.out.println("Null response returned from :"+tracker_url);
            logger.error("Null response returned from :"+tracker_url);
        } catch (URISyntaxException e) {
            status=STATUSCODE.URISYNTAX.getStatus();
            System.out.println("Invalid URI syntax");
            logger.error("Invalid URI syntax");
        }
    }
    private byte[] createConnectionRequest(){
        ByteBuffer buffer=ByteBuffer.allocate(UDP_CONNECTION_MESSAGE_LEN);
        buffer.putLong(UDP_CONNECT_REQUEST_MAGIC);
        buffer.putInt(ACTION_CONNECT);
        this.transactionId=Utils.generateRandomNumber();
        System.out.println("Generated transaction id is :"+transactionId);
        buffer.putInt(transactionId);
        return buffer.array();
    }
    private ByteBuffer receive(DatagramSocket socket){
        byte array[]=new byte[UDP_PACKET_LENGTH];
        DatagramPacket  packet=new DatagramPacket(array,array.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            System.out.println("Received null response from "+tracker_url);
            status=STATUSCODE.TIMOUT.getStatus();
            return null;
        }
        System.out.println("Received response from socket :"+packet.getLength());
        return ByteBuffer.wrap(packet.getData(),0,packet.getLength());
    }

    /*
        parse a connect response.
        A connectioon response contains following data.
        action(int).
        transactionId(int)
        connectionId(long)
     */
    private long  parse(ByteBuffer buffer){
        int action =buffer.getInt();
        System.out.println("Action is :"+action);
        int transactionId=buffer.getInt();
        long connectionId=buffer.getLong();
        System.out.println("transactiondId "+transactionId+" connectionId "+connectionId);
        return connectionId;
    }

    private byte[] craftAnnounceRequest(long connectionId,TrackerRequestPacket packet){
        ByteBuffer buffer=ByteBuffer.allocate(UDP_ANNOUNCE_MESSAGE_LEN);
        try {
            String str=new String(meta.getInfo_hash(),"ISO-8859-1");
            buffer.putLong(connectionId);
            buffer.putInt(ACTION_ANNOUNCE);
            buffer.putInt(transactionId);
            System.out.println("Info hash :"+str);
            buffer.put(str.getBytes("ISO-8859-1"));
            System.out.println("Peer id: "+ID);
            buffer.put(ID.getBytes("ISO-8859-1"));
            System.out.println("downloaded: "+packet.getDownloaded());
            buffer.putLong(packet.getDownloaded());
            System.out.println("left :"+packet.getLeft());
            buffer.putLong(packet.getLeft());
            System.out.println("uploaded :"+packet.getUploaded());
            buffer.putLong(packet.getUploaded());
            System.out.println("event :"+packet.getEvent().getValue());
            buffer.putInt(packet.getEvent().getValue());
            buffer.putInt(0); //default IP address.
            buffer.putInt(0); //default key.
            buffer.putInt(-1);//num_want(don't know what is that.)
            System.out.println("Port :"+6881);
            buffer.putShort((short) (6881 & 0xFFFF));
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return buffer.array();
    }
    public Object getTrackerResponse(){
        return response;
    }
    public int getStatusCode(){
        return status;
    }

}
