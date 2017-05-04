package internal;

/**
 * Created by ps on 26/3/17.
 */
public class Constants {

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

    //This is used to check if server is sending a compact response or not.
    //Also,it is used while decrafting the response packet.
    public static  int COMPACT_RESPONSE=0;

}
