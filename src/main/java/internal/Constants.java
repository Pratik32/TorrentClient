package internal;

import org.apache.log4j.Logger;

/**
 * Created by ps on 26/3/17.
 */
public class Constants {


    //Trakcer Constants.
    public static String INFO_HASH="info_hash=";

    public static final String PEER_ID="&peer_id=";

    public static final String EVENT="&event=";

    public static final String UPLOADED="&uploaded=";

    public static final String DOWNLOADED="&downloaded=";

    public static final String LEFT="&left=";

    public static final String PORT="&port=";

    public static final String COMPACT="&compact=";

    public static  final String CORRUPT="&corrupt=";

    public static final String KEY="&key=";

    public static final String NO_PEER_ID="&no_peer_id=";

    public static final String ID="-TO0042-0ab8e8a31019";

    public static final String EVENT_STARTED="started";
    public static final String EVENT_COMPLETED="completed";
    public static final String EVENT_STOPPED="stopped";

    public static final String PORT_PEER="6881";
    public static final int TIMEOUT=10000;

    //This is used to check if server is sending a compact response or not.
    //Also,it is used while decrafting the response packet.
    public static  int COMPACT_RESPONSE=0;


    //PeerController constants.
    public static final int BLOCK_LENGTH=16384;

    //PeerConnection constants.
    public static final String HANDSHAKE_STRING="BitTorrent protocol";
    public static final int CHOKE=0;
    public static final int UNCHOKE=1;
    public static final int INTERESTED=2;
    public static final int NOT_INTERESTED=3;
    public static final int HAVE=4;
    public static final int BIT_FIELD=5;
    public static final int REQUEST=6;
    public static final int PIECE=7;
    public static final int EXTENDED=20;
    public static final byte[] interestedMessage={0,0,0,1,2};
    public static final byte[] requestMessage={0,0,0,13,6};
    public static final byte[] chokeMessage={0,0,0,1,0};
    public static final byte[] notinterestedMessage={0,0,0,1,3};
    public static final byte[] keepAliveMessage={0,0,0,0};
    public static final byte[] haveMessage={0,0,0,5,4};
    public static final byte[] unchokeMessage={0,0,0,1,1};
    //Udp tracker constants.
    public static final int UDP_CONNECTION_MESSAGE_LEN=16;
    public static final long UDP_CONNECT_REQUEST_MAGIC = 0x41727101980L;
    public static final int UDP_PACKET_LENGTH=512;
    public static final int UDP_ANNOUNCE_MESSAGE_LEN=98;

    //Logger.

    public static Logger logger;
}
