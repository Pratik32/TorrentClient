package Tracker;

import BEcodeUtils.BencodeUtils;
import BEcodeUtils.Element;
import internal.TorrentMeta;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by ps on 26/3/17.
 */
public class HttpTrackerSession extends TrackerSession{

    private HttpURLConnection connection;
    public HttpTrackerSession(TorrentMeta meta){
        super(meta);
    }
    public Element connect(TrackerRequestPacket packet) {
        URL url=null;
        Element element=null;
        try {
            url = new URL(getTrackerUrl(packet));
            connection = (HttpURLConnection) url.openConnection();
            InputStream stream=connection.getInputStream();
            System.out.println(connection.getResponseCode());
            byte data[]= IOUtils.toByteArray(stream);
            element= BencodeUtils.decode(data);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return element;
    }
}