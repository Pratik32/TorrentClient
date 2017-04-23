package Tracker;

import BEcodeUtils.BencodeUtils;
import BEcodeUtils.Element;
import internal.TorrentMeta;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.*;

/**
 * Created by ps on 30/3/17.
 */
public class UdpTrackerSession extends TrackerSession {

    DatagramSocket socket;
    public UdpTrackerSession(TorrentMeta meta){
        super(meta);
    }
    public Element connect(TrackerRequestPacket packet) {
        URL url=null;
        Element element=null;
        try {
            url = new URL(getTrackerUrl(packet));
            InetSocketAddress address=InetSocketAddress.createUnresolved(url.getHost(),url.getPort());
            socket=new DatagramSocket();
            socket.connect(address);
            byte data[]={0};
            //byte data[]= IOUtils.toByteArray(stream);
            element= BencodeUtils.decode(data);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return element;
    }

}
