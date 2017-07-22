package internal;

import org.apache.log4j.Logger;
import peer.PeerConnection;

/**
 * Created by ps on 27/6/17.
 */
public class CustomLogger {

    private static Logger logger;


    public static Logger getInstance(){
       if(logger==null){
           logger=Logger.getLogger(PeerConnection.class);
       }
       return logger;
    }

}
