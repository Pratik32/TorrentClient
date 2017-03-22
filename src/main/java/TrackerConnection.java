import com.sun.javafx.collections.SortableList;
import com.sun.javafx.fxml.builder.URLBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.security.URIParameter;
import java.util.*;

/**
 * Created by ps on 19/3/17.
 */
public class TrackerConnection {
    private TorrentMeta meta;

    public static final char[] HEX_CHARS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    TrackerConnection(TorrentMeta meta) {
        this.meta = meta;
    }


    public Map<String, String> getPeerUrls() {
        Map<String, String> peers = new HashMap<String, String>();

        return peers;
    }

    public URI getAnnounceURI() throws Exception {
        String temp1=new String(meta.getInfo_hash_raw(),"ISO-8859-1");
        String info_hash =URLEncoder.encode(temp1,"ISO-8859-1");

        String temp = "4%93%06t%EF%3B%B91%7F%B5%F2c%CC%A80%F5%26%85%23%5B";
        String url = meta.getAnnounce() + "?" + "info_hash="
                + info_hash
                + "&peer_id=-TO0042-0ab8e8a31019"
                + "&port=6881"
                + "&event=started"
                + "&uploaded=0"
                + "&downloaded=0"
                + "&left=1104052224"
                + "&corrupt=0"
                + "&key=2I3J3T7I"
                + "&compact=1"
                + "&no_peer_id=1";
        URI uri = new URI(url);
        System.out.println(uri);
        return uri;
    }


    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        if (bytes.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder(bytes.length * 3);

        for (byte b : bytes) {
            byte hi = (byte) ((b >> 4) & 0x0f);
            byte lo = (byte) (b & 0x0f);

            sb.append('%').append(HEX_CHARS[hi]).append(HEX_CHARS[lo]);
        }
        return sb.toString();
    }

    public Map<String, String> getPeerUrls(URI uri) {
        URL url = null;
        SortedMap<String,Object> map=new TreeMap<String,Object>();
        HttpURLConnection urlConnection = null;
        Map<String, String> peers = null;
        try {
            url = uri.toURL();
            urlConnection = (HttpURLConnection) url.openConnection();
            //urlConnection.setUseCaches(false);
            //urlConnection.connect();
            String reponse = new Scanner(urlConnection.getInputStream()).useDelimiter("^").next();
            map= (SortedMap<String, Object>) Bdecoder.decode(reponse);
            Object peerlist=map.get("peers");
            if(peerlist instanceof String){
                List<InetSocketAddress> list=getPeerList(peerlist);
                System.out.println(list);
            }
            else if(peerlist instanceof Iterable<?>){
                System.out.println("It is a dictionary");
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return peers;

    }
    private List<InetSocketAddress> getPeerList(Object peers){
        List<InetSocketAddress>  list=new ArrayList<InetSocketAddress>();
        int address_size=6;
        try {
            System.out.println(((String)peers).getBytes("ISO-8859-1").length);
            ByteBuffer byteBuffer=ByteBuffer.wrap(((String)peers).getBytes("ISO-8859-1"));

            for(int of=0;of<byteBuffer.capacity();of+=address_size){
                InetSocketAddress address=peerAddress(byteBuffer,of,address_size-2);
                list.add(address);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return list;
    }
    final InetSocketAddress peerAddress(ByteBuffer bb, int ofs, int ip_length) {
        if (ofs < 0 || ofs >= bb.capacity())
            return null;

        byte[] ip = ByteBuffer.allocate(ip_length).putInt(bb.getInt(ofs)).array();
        int port = bb.getShort(ofs + ip_length) & 0xFFFF;

        try {
            return new InetSocketAddress(InetAddress.getByAddress(ip), port);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }
}