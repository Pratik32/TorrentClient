package Peers;

import internal.Constants;
import internal.TorrentMeta;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.Socket;
import java.util.Arrays;

/**
 * Created by ps on 6/4/17.
 */
public class PeerConnection {
    private  Peer peer;
    private TorrentMeta meta;

    public PeerConnection(Peer peer,TorrentMeta meta){
        this.peer=peer;
        this.meta=meta;
    }
    public void connect() throws IOException {
        System.out.println("In connect()");
        Socket socket=new Socket(peer.getAddress().getAddress(),peer.getAddress().getPort());
        byte[] messsage=createHandshakeMessage(meta.getInfo_hash(), Constants.ID);
        DataOutputStream stream=new DataOutputStream(socket.getOutputStream());
        stream.write(messsage,0,messsage.length);
        stream.flush();
        DataInputStream stream1=new DataInputStream(socket.getInputStream());

        byte response[]=new byte[49+"BitTorrent Protocol".length()];

        stream1.readFully(response);
        String response_string=new String(response);
        System.out.println(response_string.length());
        System.out.println(response_string);
        if(checkPeerID(response)){
            System.out.println("Response is ok.");
        }
        else{
            System.out.println("Drop the peer.");
        }


        //byte[] data= IOUtils.toByteArray(stream1);
    }
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
    public boolean checkPeerID(byte[] response) throws UnsupportedEncodingException {
        System.out.println("Response length is :"+response.length);

        System.out.println("Original peer id :"+peer.getPeer_id());
        byte[] peer_id=peer.getPeer_id();
        System.out.println(peer_id.length);
        for (int x=1; x<=18; x++)
        {
            if (response[response.length - x] !=  peer_id[peer_id. length- x])
            {
                return false;
                //break;
            }
        }
        return true;
    }
}
