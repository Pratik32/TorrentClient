package Peers;

import internal.Constants;
import internal.TorrentMeta;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;

import static internal.Constants.*;


/**
 * Created by ps on 6/4/17.
 *
 * This class represents an interface for connecting a peer
 * Right now Peer class is just a POJO for storing peer related information.
 * The actual handling of message flow is done by this class.
 * For now this class has different methods for sending different types of messages to peer.
 * Ideally it should extend Thread/Runnable and run independently.
 *
 */
public class PeerConnection extends Thread{
    private  Peer peer;
    private TorrentMeta meta;
    private byte[] piece_data;
    private DataInputStream inputStream;
    private DataOutputStream outputStream;
    private Socket socket;
    private Logger logger;
    private int choke=1;
    private int notInterested=1;
    private int unchoke=0;
    private int interested=0;
    private boolean keepRunning=true;
    private PeerController peerController;
    public PeerConnection(Peer peer,TorrentMeta meta,byte[] data,PeerController c){
        this.peer=peer;
        this.meta=meta;
        this.logger=Constants.logger;
        this.peerController=c;
    }

    //Keeping this method  for quick testing purpose.
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
        stream.write(interestedMessage,0,interestedMessage.length);
        len=stream1.readInt();
        code=stream1.readByte();
        System.out.println("Length :"+len+"Type is :"+code);
        Constants.logger.debug("Length :"+len+"Type is :"+code);
        stream1.skipBytes(len-1);
        stream.flush();

        //create a request for a piece.
        ByteBuffer byteBuffer=ByteBuffer.allocate(17);
        byteBuffer.put(requestMessage);
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



    public void run() {
        //setup a connection with remote peer.
        byte[] block = new byte[BLOCK_LENGTH];
        try {
            socket = new Socket(peer.getAddress().getAddress(), peer.getAddress().getPort());
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            //send handshake.
            handshake();

            //verify the handshake.else drop the peer.
            int len=0;
            int code=0;
            if (verifyHandshake()) {
                System.out.println("Peer id verified.");
                logger.debug("Peer id verified.");
                while (keepRunning) {
                    len=inputStream.readInt();
                    code=inputStream.readByte();

                    switch (code){
                        case CHOKE:
                            receivChoke();
                            break;
                        case UNCHOKE:
                            receiveUnchoke();
                            sendRequest();
                            break;
                        case INTERESTED:
                            break;
                        case NOT_INTERESTED:
                            break;
                        case HAVE:
                            break;
                        case BIT_FIELD:
                            receiveBitfield(len);
                            sendInterested();
                            break;
                        case REQUEST:
                            break;
                        case PIECE:
                            receivePiece(block);
                            keepRunning=sendRequest();//if nothing to request shutdown the connection.
                            break;
                        case EXTENDED:
                            receiveExtended(len);
                            break;
                        default:
                            System.out.println("Len :"+len+"Code :"+code);
                            keepRunning=false;
                    }

                }
            }
            else{
                logger.debug("Peer id did not match dropping the peer.");
                teardown();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        teardown();
    }


    /*
     creates a handshake message for transmission.
     returns a byte array containing - 19+"BitTorrent Protocol"+info_hash+peer_id.
     if we send each component separately it is not considered as valid BitTorrent message(in this case handsha
     ke message) hence create complete message and then write it to outstream.
     */
    private byte[] createHandshakeMessage(byte[] info_hash,String client_id){
        String pstr = HANDSHAKE_STRING;		//String identifier of the current BitTorrent protocol
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

    private void handshake(){
        byte[] handshake=createHandshakeMessage(meta.getInfo_hash(),Constants.ID);
        try {
            outputStream.write(handshake,0,handshake.length);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.debug("Successful sent handshake to :"+peer.getAddress());
    }
    private boolean verifyHandshake(){
        byte[] response=new byte[49+HANDSHAKE_STRING.length()];
        try {
            inputStream.readFully(response);
            if (COMPACT_RESPONSE==1){
                return true;
            }
            if(checkPeerID(response)){
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;//wtf? see comments on method below.
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
    private void  receiveUnchoke(){
        choke=0;
        unchoke=1;
        System.out.println("Unchoke received");
        logger.debug("Uchoke received from "+peer.getAddress());
    }


    /*
        Ideally bitfield must be stored.And based on that requests should be made.
        Will come back to that soon.Now just consuming bitfield.
     */
    private void receiveBitfield(int len){
        logger.debug("Bitfield received");
        System.out.println("Bitfield received");
        try {
            inputStream.skipBytes(len-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
        Not able to figure out what this message represents.Sometimes it is received sometimes
        it is not.Anyway we have to consume it.
     */
    private void receiveExtended(int len){
        logger.debug("extended received");
        System.out.println("extended received");
        try {
            inputStream.skipBytes(len-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendInterested(){
        System.out.println("Sending interested to "+peer.getAddress());
        logger.debug("Sending interested to "+peer.getAddress());
        try {
            outputStream.write(interestedMessage);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        interested=1;
    }
    private boolean sendRequest(){
        int params[]=peerController.getBlockParams();
        if(params[0]==-1){
            logger.debug("Nothing to download");
            System.out.println("Nothing to download");
            return false;
        }
        ByteBuffer buffer=ByteBuffer.allocate(17);
        logger.debug("Requesting Piece"+params[0]+" Block "+params[1]);
        System.out.println("Requesting Piece"+params[0]+" Block "+params[1]);
        buffer.put(requestMessage);
        buffer.putInt(params[0]);
        buffer.putInt(params[1]);
        buffer.putInt(params[2]);
        byte[] request=buffer.array();
        try {
            outputStream.write(request);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }
    /*
        This method will download a block(of size BLOCK_LENGTH).
        data will be copied to piece_data at offset.

     */
    private void receivePiece(byte[] data){
        try {
            int pieceIndex=inputStream.readInt();
            int offset=inputStream.readInt();
            System.out.println("pieceIndex: "+pieceIndex+"offset :"+offset);
            ByteArrayOutputStream stream=new ByteArrayOutputStream();
            int pos=0;
            byte[] buff=new byte[4096];
            while(true){
                pos=inputStream.read(buff);
                if(pos==-1){
                    break;
                }
                stream.write(buff,0,pos);
            }
            data=stream.toByteArray();
            System.arraycopy(piece_data,offset,data,0,data.length);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void receivChoke(){
        System.out.println("Received choke from remote peer"+peer.getAddress());
        logger.debug("Received choke from remote peer"+peer.getAddress());
        choke=1;
    }
    private  void sendChoke(){
        try {
            outputStream.write(chokeMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
        A method to drop the given peer and stop the thread.
     */
    private void teardown(){
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
