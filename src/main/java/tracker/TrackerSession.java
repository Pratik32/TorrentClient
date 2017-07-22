package tracker;

import bencodeutils.Element;
import internal.Constants;
import internal.CustomLogger;
import internal.TorrentMeta;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static internal.Constants.*;
/**
 * Created by ps on 26/3/17
 * Represents a TrackerSession(http,udp).
 * General representation of tracker session.
 * contains common methods related to tracker components.
 * [added on 19/07/17]: I have made 'connect' method to return nothing(void)
 * in order to make code error proof.This is how http works,first
 * you connect,then check status code,and according to status code
 * proceed.
 *
 */
public  abstract class TrackerSession {

    protected String tracker_url;
    protected byte[] info;
    private int interval_time;
    protected TorrentMeta meta;
    protected Logger logger;
    protected int status;
    public static enum STATUSCODE{
        OK(1),TIMOUT(2),MALFORMED_URL(3),URISYNTAX(4);
        int status;
        STATUSCODE(int num){
            this.status=num;
        }

        public int getStatus() {
            return status;
        }
    }

    TrackerSession(TorrentMeta meta,String annouceUrl ){
        this.meta=meta;
        this.info=this.meta.getInfo_hash();
        this.tracker_url=annouceUrl;
        logger= CustomLogger.getInstance();
    }

    /*
        This function extracts IP:port pair and peer_id(if compact=0) and returns.
        Problem is, if compact flag is set the response does not contain peer_id field.
        hence I have to handle it programmatically. Thanks to decoding scheme nothing much
        had to be done.tracker server behaves in a weird way,Sometimes it accepts compact=0
        and sometimes it does not.hence be carefull while sending tracker request.if 400 is
        returned by server,next time send request with compact flag set.
     */
    private Map<InetSocketAddress,byte[]> getPeerInfo(Element response){
        int status=0;
        Map<InetSocketAddress,byte[]> peers_info=new HashMap<InetSocketAddress,byte[]>();
        Element element=response.getMap().get("peers");
        if(element.getValue() instanceof byte[]){
            COMPACT_RESPONSE=1;
            byte ip[]=element.getBytes();
            ByteBuffer byteBuffer=ByteBuffer.wrap(ip);
            InetSocketAddress address;
            for(int offset=0;offset<byteBuffer.capacity();offset+=6){
                address=getInetSocketAddress(byteBuffer,offset,4);
                peers_info.put(address,null);
            }
        }
        else if(element.getValue() instanceof List) {
            List<Map<String,Element>> map=element.getListOfMap();
            peers_info=getPeers(map);
        }
        return peers_info;
    }

    /*
        getPeerInfo is overloaded so that it can decode both ByteBuffer
        and Element.Handling everything in one getPeerInfo method would
        have been problematic.
     */
    private Map<InetSocketAddress,byte[]> getPeerInfo(ByteBuffer buffer){
        COMPACT_RESPONSE=1;
        Map<InetSocketAddress,byte[]> peers_info=new HashMap<InetSocketAddress,byte[]>();
        for(int offset=20;offset<buffer.limit();offset+=6){
            InetSocketAddress address=getInetSocketAddress(buffer,offset,4);
            peers_info.put(address,null);
        }
        return peers_info;

    }
    /*
        returns InetSocketAddress.
        structure of IP address in bytebuffer response:
        6 bytes= 4 bytes ip(integer)+2 bytes port(short).
     */
    private InetSocketAddress getInetSocketAddress(ByteBuffer buffer,int offset,int size){
        byte ip_address[]=ByteBuffer.allocate(size).putInt(buffer.getInt(offset)).array();
        int port= buffer.getShort(offset+size) & 0xFFFF;
        InetSocketAddress address=null;
        try {
            address=new InetSocketAddress(InetAddress.getByAddress(ip_address),port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return address;
    }

    /*
         When compact response=0 We get extra information about given
         peer i.e it's peer_id which is extracted in below method.
     */
    private Map<InetSocketAddress,byte[]> getPeers(List<Map<String,Element>> map){
        List<InetSocketAddress> peers=new ArrayList<InetSocketAddress>();
        Map<InetSocketAddress,byte[]> peers_info=new HashMap<InetSocketAddress,byte[]>();
        for(Map<String,Element> m:map){
            String ip=m.get("ip").getString();
            String port=m.get("port").getString();
            InetSocketAddress address=new InetSocketAddress(ip,Integer.parseInt(port));
            byte[] peer_id=m.get("peer id").getBytes();
            peers_info.put(address,peer_id);
            peers.add(address);
        }
        return peers_info;
    }


    public TrakcerResponsePacket sendRequest(TrackerRequestPacket packet ){
        connect(packet);
        Object obj=null;
        TrakcerResponsePacket responsePacket=new TrakcerResponsePacket();
        if(getStatus()==STATUSCODE.OK.getStatus()) {
            obj=getTrackerResponse();
            //in case of connection timeout send.
            if (obj == null) {
                System.out.println("Null response returned.");
                responsePacket.setStatusCode(-1);
                return responsePacket;
            }else{
                responsePacket=craftPacket(obj);
                responsePacket.setStatusCode(1);
            }
            //special case for handling Timeout related errors.
        }else if(getStatus()==STATUSCODE.TIMOUT.getStatus()){
            responsePacket.setStatusCode(0);
        }else{
            responsePacket.setStatusCode(-1);
        }
        return responsePacket;
    }

    /*
        The contract for connect method had to be changed.
        Because UDP tracker cannot return an Element(because response is not bencoded.)
     */
    public abstract void connect(TrackerRequestPacket packet);

    public abstract Object getTrackerResponse();
    protected String getTrackerUrl(TrackerRequestPacket packet){
        String event_type="";
        switch (packet.getEvent()){
            case COMPLETED:
                event_type=EVENT_COMPLETED;
                break;
            case STARTED:
                event_type=EVENT_STARTED;
                break;
            case STOPPED:
                event_type=EVENT_STOPPED;
        }
        String url=tracker_url+"?"+INFO_HASH+getEncodedString(info)
                +PEER_ID+ID
                +PORT+PORT_PEER
                +UPLOADED+packet.getUploaded()
                +DOWNLOADED+packet.getDownloaded()
                +LEFT+packet.getLeft()
                +EVENT+event_type
                +COMPACT+1;
        System.out.println(url);
        logger.debug("Trakcer url is :"+url);
        return url;

    }
    private String getEncodedString(byte[] data){
        String encoded_string="";
        try {
            String temp= new String(data,"ISO-8859-1");
            System.out.println("Info hash "+temp);
            encoded_string= URLEncoder.encode(temp,"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encoded_string;
    }
    /*
        This method is checks if given response is of which type.
        case Element:
            It will call getPeerInfo with Element as param(morph 1)
        case ByteBuffer:
            It will call getPeerInfo with ByteBuffer as param(morph 2)
     */
    private  TrakcerResponsePacket craftPacket(Object obj){
        logger.debug("Crafting the response packet.");
        Map<InetSocketAddress,byte[]> peer_info=null;
        if(obj instanceof Element) {
            Element element=(Element)obj;
            peer_info = getPeerInfo(element);
        }
        else{
            ByteBuffer buffer=(ByteBuffer)obj;
            peer_info=getPeerInfo(buffer);
        }
        TrakcerResponsePacket packet=new TrakcerResponsePacket();
        packet.setPeer_info(peer_info);
        return  packet;
    }
    protected int getStatus(){
        return status;
    }
}
