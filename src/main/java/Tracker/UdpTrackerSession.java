package Tracker;

import BEcodeUtils.BencodeUtils;
import BEcodeUtils.Element;
import internal.Constants;
import internal.TorrentMeta;
import internal.Utils;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static internal.Constants.*;

/**
 * Created by ps on 30/3/17.
 * For UdpTrackerSession some extra work has to be done.
 * First we have to send a 'connection' request which will return a transaction id.
 * transaction id will be used in sending announce request.
 */
public class UdpTrackerSession extends TrackerSession {

    DatagramSocket socket;
    private int transactionId;
    public UdpTrackerSession(TorrentMeta meta){
        super(meta);
    }
    public Object connect(TrackerRequestPacket packet) {
        URL url=null;
        ByteBuffer response=null;
        try {
            //url = new URL(getTrackerUrl(packet));
            URI uri=new URI(meta.getAnnounce());
            System.out.println();
            InetSocketAddress address=new InetSocketAddress(uri.getHost(),uri.getPort());
            System.out.println("InetAddress is: "+address.toString());
            socket=new DatagramSocket();
            socket.connect(address);

            //send a connection request first with given transaction id.
            byte[] data=createConnectionRequest();
            DatagramPacket datagramPacket=new DatagramPacket(data,data.length,address);
            socket.send(datagramPacket);
            ByteBuffer buffer=receive(socket);
            long connectionId=parse(buffer);
            byte[] announceRequest=craftAnnounceRequest(connectionId,packet);
            DatagramPacket announcePacket=new DatagramPacket(announceRequest,announceRequest.length,address);
            socket.send(announcePacket);
            response=receive(socket);

            System.out.println(response.getInt());
            System.out.println("transactionId "+response.getInt()+" Interval "+response.getInt()+" leechers "+response.getInt()+" seeders "+response.getInt());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return response;
    }
    private byte[] createConnectionRequest(){
        ByteBuffer buffer=ByteBuffer.allocate(UDP_CONNECTION_MESSAGE_LEN);
        buffer.putLong(UDP_CONNECT_REQUEST_MAGIC);
        buffer.putInt(0);
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
            e.printStackTrace();
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
            buffer.putInt(1);
            buffer.putInt(transactionId);
            System.out.println("Info hash :"+str);
            buffer.put(str.getBytes("ISO-8859-1"));
            System.out.println("Peer id: "+ID);
            buffer.put(ID.getBytes("ISO-8859-1"));
            System.out.println("downloaded: "+0);
            buffer.putLong(0);
            System.out.println("left :"+meta.getTotalFilesize());
            buffer.putLong(meta.getTotalFilesize());
            System.out.println("uploaded :"+0);
            buffer.putLong(0);
            System.out.println("event :"+2);
            buffer.putInt(2);
            buffer.putInt(0);
            buffer.putInt(0);
            buffer.putInt(-1);
            System.out.println("Port :"+6881);
            buffer.putShort((short) (6881 & 0xFFFF));
        }catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return buffer.array();
    }

}
