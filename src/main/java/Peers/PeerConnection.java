package Peers;

import internal.Constants;
import internal.TorrentMeta;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.Arrays;
/**
 * Created by ps on 6/4/17.
 *
 * This class represents an interface for connecting a peer
 * Right now Peer class is just a POJO for storing peer related information.
 * The actual handling of message flow is done by this class.
 * In future I may delete this class if I come up with better model.
 * For now this class has different methods for sending different types of messages to peer.
 * Ideally it should extend Thread/Runnable and run independently.
 *
 */
public class PeerConnection {
    private  Peer peer;
    private TorrentMeta meta;

    public static final byte[] interested={0,0,0,1,2};
    public static final byte[] request={0,0,0,13,6};
    public PeerConnection(Peer peer,TorrentMeta meta){
        this.peer=peer;
        this.meta=meta;
    }
    public void connect() throws IOException, InterruptedException {
        System.out.println("In connect()");
        Constants.logger.debug("Connecting peer :"+peer.getAddress().toString());
        System.out.println(peer.getAddress().toString());
        Socket socket=new Socket(peer.getAddress().getAddress(),peer.getAddress().getPort());
        byte[] messsage=createHandshakeMessage(meta.getInfo_hash(),Constants.ID);
        DataOutputStream stream=new DataOutputStream(socket.getOutputStream());
        stream.write(messsage,0,messsage.length);
        stream.flush();
        DataInputStream stream1=new DataInputStream(socket.getInputStream());

        byte response[]=new byte[49+"BitTorrent protocol".length()];
        stream1.readFully(response);
        String response_string=new String(response);
        System.out.println(response_string.length());
        System.out.println(response_string);
        /*
         Below if-else checks if peer_id is matching or not. according to specs one should drop the peer
         if id doesn't match ( ofcousre !) right now the method-checkPeerID() is not giving proper result
         but I have cross-checked with wireshark that peers are valid.Go ahead!
         */
        if(Constants.COMPACT_RESPONSE==0 && checkPeerID(response)){
            System.out.println("Response is ok.");
        }
        else{
            System.out.println("Drop the peer.");
        }


        int len=0;
        int code=0;
        len=stream1.readInt();
        code=stream1.readByte();

        System.out.println("Length :"+len+"Type is :"+code);
        Constants.logger.debug("Length :"+len+"Type is :"+code);
        stream1.skipBytes(len-1);
        len=stream1.readInt();
        code=stream1.readByte();
        System.out.println("Length :"+len+"Type is :"+code);
        Constants.logger.debug("Length :"+len+"Type is :"+code);
        stream1.skipBytes(len-1);
        //sending interested message to peer.
        stream.write(interested,0,interested.length);
        len=stream1.readInt();
        code=stream1.readByte();
        System.out.println("Length :"+len+"Type is :"+code);
        Constants.logger.debug("Length :"+len+"Type is :"+code);
        stream1.skipBytes(len-1);
        stream.flush();

        //create a request for a piece.
        ByteBuffer byteBuffer=ByteBuffer.allocate(17);
        byteBuffer.put(request);
        byteBuffer.putInt(0);
        byteBuffer.putInt(0);
        byteBuffer.putInt(16384);
        byte[] request_message=byteBuffer.array();
        stream.write(request_message);
        stream.flush();
        int pos=0;
        len=stream1.readInt();
        code=stream1.readByte();
        System.out.println("Length :"+len+"Type is :"+code);
        Constants.logger.debug("Length :"+len+"Type is :"+code);
    }

    /*
     creates a handshake message for transmission.
     returns a byte array containing - 19+"BitTorrent Protocol"+info_hash+peer_id.
     if we send each component separately it is not considered as valid BitTorrent message(in this case handsha
     ke message) hence create complete message and then write it to outstream.
     */
    private byte[] createHandshakeMessage(byte[] info_hash,String client_id){
        String pstr = "BitTorrent protocol";		//String identifier of the current BitTorrent protocol
        byte[] msg = new byte[49 + pstr.length()];
        //byte[] response = new byte[msg.length];

        int i = 0;
        int offset = 0;
        msg[i] = ((byte) pstr.length());
        //Sets up the part of the byte array corresponding to the BitTorrent protocol
        i = 1;
        offset = i;
        for (i=i; i<pstr.length()+1; i++)
            msg[i] = (byte) pstr.charAt(i - offset);

        //Sets up the 8 reserved bytes in the array
        for (i=i; i<pstr.length() + 9; i++)
            msg[i] = (byte) 0;

        //Sets up the 20 byte info_hash in the array
        offset = i;
        for (i=i; i<pstr.length() + 29; i++)
            msg[i] = info_hash[i - offset];

        //Sets up the 20 byte peer id in the array
        offset = i;
        for (i=i; i<pstr.length() + 49; i++)
            msg[i] = (byte) client_id.charAt(i - offset);
        return msg;
    }


    /*
       Check if peer id received after sending handshake is same as peer_id in Peer class.
       for now method is not working but I have cross checked using wireshark,the peers are valid.
    */
    public boolean checkPeerID(byte[] response) throws UnsupportedEncodingException {
        System.out.println("Response length is :"+response.length);
        byte[] peer_id=peer.getPeer_id();
        for(byte b:peer_id){
            System.out.print(b+" ");
        }

        System.out.println(peer_id.length);
        for (int x=0; x<20; x++)
        {
            if (response[(response.length-1) - x] !=  peer_id[(peer_id. length-1)- x])
            {

                return false;
                //break;
            }
        }
        return true;
    }
}
