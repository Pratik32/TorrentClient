package dht;

import bencodeutils.BencodeUtils;
import bencodeutils.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ps on 7/6/17.
 */
public class DHTConnection {
    public static void main(String[] args) throws IOException {
        DHTConnection connection=new DHTConnection();
        connection.connect();
    }
    String[] dhtBootStrapNodes={"router.utorrent.com","router.bittorrent.com"};
    public void connect() throws IOException {
        Map<String,Element> queryRequest=new HashMap<String, Element>();
        Map<String,Element> queryParam=new HashMap<String, Element>();
        queryParam.put("id",new Element(new String("abcdefghij0123456789")));
        queryParam.put("info_hash",new Element(new String("f2e59d5609a5e3bb80995547e404fc95f799909e")));
        queryRequest.put("a",new Element(queryParam));
        queryRequest.put("q",new Element(new String("get_peers")));
        queryRequest.put("t",new Element(new String("aa")));
        queryRequest.put("y",new Element(new String("q")));
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        BencodeUtils.encode(queryRequest,stream);
        byte output[]=send(stream.toByteArray());
        Element element=BencodeUtils.decode(output);
        Map<String,Element> response=element.getMap();
        Map<String,Element> map=response.get("r").getMap();
        System.out.println("ID is "+ map.get("id").getString());
        byte value[]=map.get("nodes").getBytes();
        byte ip[]=new byte[6];
        System.arraycopy(value,0,ip,0,6);
        ByteBuffer byteBuffer=ByteBuffer.wrap(ip);
        InetSocketAddress address;
        address=getInetSocketAddress(byteBuffer,0,4);
        System.out.println(address);

    }
    public byte[] send( byte data[]){
        try{
            DatagramSocket socket=new DatagramSocket();
            byte receivedata[]=new byte[1000];
            InetAddress address=InetAddress.getByName(dhtBootStrapNodes[1]);
            System.out.println(address);
            DatagramPacket packet=new DatagramPacket(data,data.length,address,6881);
            socket.send(packet);
            System.out.println("sent");
            DatagramPacket packet1=new DatagramPacket(receivedata,receivedata.length);
            socket.receive(packet1);
            System.out.println(packet.getLength());
            ByteBuffer buffer=ByteBuffer.wrap(packet1.getData(),0,packet1.getLength());
            return buffer.array();
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
}
