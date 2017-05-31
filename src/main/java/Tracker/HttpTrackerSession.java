package Tracker;

import BEcodeUtils.BencodeUtils;
import BEcodeUtils.Element;
import internal.Constants;
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
    public HttpTrackerSession(TorrentMeta meta,String announceUrl){
        super(meta,announceUrl);
    }
    public Object connect(TrackerRequestPacket packet) {
        URL url=null;
        Element element=null;
        try {
            url = new URL(getTrackerUrl(packet));
            connection = ((HttpURLConnection)(url.openConnection()));
            connection.setConnectTimeout(20000);
            InputStream stream=connection.getInputStream();
            System.out.println(connection.getResponseCode());
            Constants.logger.debug("Received tracker response with response code :"+connection.getResponseCode());
            byte data[]= IOUtils.toByteArray(stream);
            element= BencodeUtils.decode(data);
            Constants.logger.debug("Successfully decoded tracker responses.");
        } catch (MalformedURLException e) {
            return  null;
        } catch (IOException e) {
            return null;
        }
        return element;
    }
}
