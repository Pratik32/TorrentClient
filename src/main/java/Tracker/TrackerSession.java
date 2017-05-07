package Tracker;

import BEcodeUtils.Element;
import internal.Constants;
import internal.TorrentMeta;
import javafx.scene.control.TableColumn;

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
 * Created by ps on 26/3/17.
 */
public  abstract class TrackerSession {

    protected String tracker_url;
    protected byte[] info;
    private int interval_time;
    protected TorrentMeta meta;

    TrackerSession(TorrentMeta meta ){
        this.meta=meta;
        this.info=this.meta.getInfo_hash();
        this.tracker_url=this.meta.getAnnounce();
    }

    /*
        This function extracts IP:port pair and peer_id(if compact=0) and returns.
        Problem is, if compact flag is set the response does not contain peer_id field.
        hence I have to handle it programmatically. Thanks to decoding scheme nothing much
        had to be done.Tracker server behaves in a weird way,Sometimes it accepts compact=0
        and sometimes it does not.hence be carefull while sending tracker request.if 400 is
        returned by server,next time send request with compact flag set.
     */
    private Map<InetSocketAddress,byte[]> getPeerInfo(Element response){
        List<InetSocketAddress> peers=new ArrayList<InetSocketAddress>();
        Map<InetSocketAddress,byte[]> peers_info=new HashMap<InetSocketAddress,byte[]>();
        Element element=response.getMap().get("peers");
        if(element.getValue() instanceof byte[]){
            COMPACT_RESPONSE=1;
            System.out.println("in");
            byte ip[]=element.getBytes();
            System.out.println(ip.length);
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
        Element element=connect(packet);
        TrakcerResponsePacket responsePacket=craftPacket(element);
        return responsePacket;
    }
    public abstract Element connect(TrackerRequestPacket packet);

    protected String getTrackerUrl(TrackerRequestPacket packet){
        String event_type="";
        switch (packet.getEvent()){
            case COMPLETED:
                    event_type="completed";
                    break;
            case STARTED:
                    event_type="started";
                    break;
            case STOPPED:
                   event_type="stopped";
        }
        String url=tracker_url+"?"+INFO_HASH+getEncodedString(info)
                +PEER_ID+ID
                +PORT+"6881"
                +UPLOADED+packet.getUploaded()
                +DOWNLOADED+packet.getDownloaded()
                +LEFT+packet.getLeft()
                +EVENT+event_type
                +COMPACT+1;
        System.out.println(url);
        Constants.logger.debug("Trakcer url is :"+url);
        return url;

    }
    private String getEncodedString(byte[] data){
        String encoded_string="";
        String temp= null;
        try {
            temp = new String(data,"ISO-8859-1");

            encoded_string= URLEncoder.encode(temp,"ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encoded_string;
    }
    public  TrakcerResponsePacket craftPacket(Element element){
        Constants.logger.debug("Crafting the response packet.");
        Map<InetSocketAddress,byte[]> peer_info=getPeerInfo(element);
        TrakcerResponsePacket packet=new TrakcerResponsePacket();
        packet.setPeer_info(peer_info);
        return  packet;
    }
}
