package dht;

import bencodeutils.BencodeUtils;
import bencodeutils.Element;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by ps on 7/6/17.
 */
public class DHTConnection {
    String[] dhtBootStrapNodes={"router.utorrent.com","router.bittorrent.com"};
    public void connect() throws IOException {
        Map<String, Element> pingRequest=new TreeMap<String, Element>();
        pingRequest.put("t",new Element("0"));
        pingRequest.put("y",new Element("q"));
        pingRequest.put("q",new Element("ping"));
        Map<String,Element> pingID=new TreeMap<String, Element>();
        pingID.put("id",new Element("pratiks123"));
        pingRequest.put("a",new Element(pingID));

        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        BencodeUtils.encode(pingRequest,stream);
        byte data[]=send(stream.toByteArray());
        if(data==null){
            System.out.println("Nothing returned.");
        }
        System.out.println("length "+data.length);
        Element element=BencodeUtils.decode(data);
        Map<String,Element> root=element.getMap();
        Map<String,Element> idKey=root.get("r").getMap();
        String id=idKey.get("id").getString();
        System.out.println("Remote id is :"+id);

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
}
