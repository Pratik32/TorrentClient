import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;

/**
 * Created by ps on 30/3/17.
 */
public class test {
    public static void main(String[] args) throws Exception {
        BencodeParser parser=new BencodeParser();
        TorrentMeta meta=parser.parseTorrentFile("ubuntu.torrent");
        TrackerConnection connection=new TrackerConnection(meta);
        URI uri=connection.getAnnounceURI();
        Map<String,String> map=connection.getPeerUrls(uri);
        System.out.println(map);
    }
}
