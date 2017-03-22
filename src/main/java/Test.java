import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by ps on 17/3/17.
 */
public class Test {
    public static void main(String[] args) throws Exception {
        Scanner sc=new Scanner(System.in);
        String filename=sc.next();
        BencodeParser parser=new BencodeParser();
        TorrentMeta meta=parser.parseTorrentFile(filename);
        TrackerConnection connection=new TrackerConnection(meta);
        URI uri=connection.getAnnounceURI();
        connection.getPeerUrls(uri);
        System.out.println();
        //printTorrentMeta(meta);

    }
    private static void printTorrentMeta(TorrentMeta meta){
        System.out.println("announce :");
        System.out.print(meta.getAnnounce());
        System.out.println();
        System.out.println("announce-list");
        for(String s:meta.getAnnouce_list()){
            System.out.println(s);
        }
        System.out.println("created by :");
        if(meta.getCreatedby()!=null){
            System.out.print(meta.getCreatedby());
        }
        System.out.println();
        System.out.println("creation date :");
        Date date=new Date(Long.parseLong(meta.getCreation_date())*1000);
        System.out.print(date);

        System.out.println();
        System.out.println("files info :");
        for(Map.Entry<String,Long> e:meta.getFiles().entrySet()){
            System.out.println(e.getKey()+"  "+e.getValue());
        }
        System.out.println("piece-length");
        System.out.println(meta.getPiecelength());
    }
}