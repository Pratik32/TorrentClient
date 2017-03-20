import com.sun.javafx.fxml.builder.URLBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;

import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.URIParameter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by ps on 19/3/17.
 */
public class TrackerConnection {
    private  TorrentMeta meta;

    public static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    TrackerConnection(TorrentMeta meta){
        this.meta=meta;
    }


    public Map<String,String> getPeerUrls(){
        Map<String,String> peers=new HashMap<String,String>();

        return peers;
    }
    public URI getAnnounceURI() throws Exception {
        String info_hash=toHexString(meta.getInfo_hash_raw());
        String url=meta.getAnnounce()+"?"+"info_hash="
                +info_hash
                +"&peer_id=-TO0042-0ab8e8a31019"+"&port=6881"
                +"&event=started"
                +"&uploaded=0"
                +"&downloaded=0"
                +"&left=1028128768"
                +"&compact=1"
                +"&no_peer_id=0";
        URI uri=new URI(url);
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



}
