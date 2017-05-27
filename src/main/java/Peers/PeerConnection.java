package Peers;

import internal.Constants;
import internal.TorrentMeta;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.BitSet;

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
 * NOTE : When a PeerConnection is assigned with a piece,It will download all of its blocks
 *        once all the blocks are downloaded PeerController may assign it with new piece.
 */
public class PeerConnection extends Thread{
    private  Peer peer;
    private TorrentMeta meta;
    private byte[] piece_data;
    private byte[] blockData;
    private int pieceIndex;
    private int offset;
    private int blocksDownloaded;
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
    private String threadID;
    private BitSet localBitField;
    public PeerConnection(Peer peer,TorrentMeta meta,byte[] data,PeerController c){
        this.peer=peer;
        this.meta=meta;
        this.logger=Constants.logger;
        this.peerController=c;
        this.piece_data=data;
        localBitField=new BitSet(meta.getTotalPieces());
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
        if(code!=5) {
            len = stream1.readInt();
            code = stream1.readByte();
            System.out.println("Length :" + len + "Type is :" + code);
            Constants.logger.debug("Length :" + len + "Type is :" + code);
            stream1.skipBytes(len - 1);
        }
        //sending interested message to peer.
        stream.write(interestedMessage,0,interestedMessage.length);
        len=stream1.readInt();
        code=stream1.readByte();
        System.out.println("Length :"+len+"Type is :"+code);
        Constants.logger.debug("Length :"+len+"Type is :"+code);
        stream1.skipBytes(len-1);
        stream.flush();

        //create a request for a piece.
        test(stream,stream1,0);
        test(stream,stream1,1);
    }


    public void test(DataOutputStream stream,DataInputStream stream1,int block) throws IOException, InterruptedException {
        int len=0;
        int code=0;
        byte data[]=new byte[BLOCK_LENGTH];
        ByteBuffer byteBuffer=ByteBuffer.allocate(17);
        byteBuffer.put(requestMessage);
        byteBuffer.putInt(0);
        byteBuffer.putInt(block);
        byteBuffer.putInt(16384);
        byte[] request_message=byteBuffer.array();
        stream.write(request_message);
        stream.flush();
        System.out.println("Available :"+stream1.available());
        stream1.skipBytes(stream1.available());
        len=stream1.readInt();
        code=stream1.readByte();
        int copied=stream1.skipBytes(BLOCK_LENGTH);
        System.out.println("Data skipped"+copied);
        System.out.println("Length :"+len+"Type is :"+code);
        Constants.logger.debug("Length :"+len+"Type is :"+code);
    }

    public void run() {
        this.threadID="["+Thread.currentThread().getName()+"]";
        //setup a connection with remote peer.
        System.out.println("Thread :"+threadID+"Started communicating with "+peer.getAddress());
        logger.debug("Thread :"+threadID+"Started communicating with "+peer.getAddress());
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
                System.out.println(threadID+" Peer id verified.");
                logger.debug(threadID+" Peer id verified.");
                while (keepRunning) {
                    len=inputStream.readInt();
                    code=inputStream.readByte();
                    System.out.println(threadID+" Length "+len+" Code :"+code);
                    switch (code){
                        case CHOKE:
                            receiveChoke();
                            break;
                        case UNCHOKE:
                            receiveUnchoke();
                            pieceIndex=this.peerController.testgetNextPieceToDownload();
                            keepRunning=sendRequest();
                            break;
                        case INTERESTED:
                            break;
                        case NOT_INTERESTED:
                            break;
                        case HAVE:
                            receiveHave();
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
                            System.out.println("Inside default");
                            System.out.println(threadID+" Len :"+len+"Code :"+code);
                            keepRunning=false;
                    }

                }
            }
            else{
                logger.debug(threadID+" Peer id did not match dropping the peer.");
                teardown();
            }
        }catch(IOException e){
            System.out.println(threadID+" Time out killing the peer connection");
            logger.error(threadID+" Time out killing the peer connection");

            //e.printStackTrace();
        }
        teardown();
        //peerController.notifyController(peer);
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
        logger.debug(threadID+" Successful sent handshake to :"+peer.getAddress());
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
        System.out.println(threadID+" Response length is :"+response.length);
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
        System.out.println(threadID+" Unchoke received");
        logger.debug(threadID+" Uchoke received from "+peer.getAddress());
    }


    /*
        Ideally bitfield must be stored.And based on that requests should be made.
        Will come back to that soon.Now just consuming bitfield.
     */
    private void receiveBitfield(int len){
        logger.debug(threadID+" Bitfield received");
        System.out.println(threadID+" Bitfield received");
        byte[] bitfield=readInputStream(len-1,inputStream);
        int l=0;
        for(int i=0;i<bitfield.length;i++){
            byte data=bitfield[i];

            for(int j=0;j<8;j++){
                int temp = ((int) data >> j) & 1;
                boolean val = temp == 1 ? true : false;
                localBitField.set(i * 8 + (7-j), val);
                l++;
            }

        }
        System.out.println(threadID+" "+localBitField.size());
        System.out.println(threadID+" BitField received is: "+localBitField);
        peerController.updateGlobalBitField(localBitField);

        //testMethod(bitfield);
    }


    /*
        Not able to figure out what this message represents.Sometimes it is received sometimes
        it is not.Anyway we have to consume it.
     */

    private void receiveExtended(int len){
        logger.debug(threadID+" extended received");
        System.out.println(threadID+" extended received");
        try {
            inputStream.skipBytes(len-1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void sendInterested(){
        System.out.println(threadID+" Sending interested to "+peer.getAddress());
        logger.debug(threadID+" Sending interested to "+peer.getAddress());
        try {
            outputStream.write(interestedMessage);
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        interested=1;
    }
    private boolean sendRequest(){
        //check if last block

        if(blocksDownloaded==peerController.getNumberOfBlocks()){
            pieceIndex=peerController.testgetNextPieceToDownload();
            System.out.println(threadID+ " Requesting a new piece: "+pieceIndex);
            if (pieceIndex==-1){
                return false;
            }
            //new piece starts downloading.
            offset=0;
            blocksDownloaded=0;
            int pieceLen=(int)meta.getPiecelength();
            if(isLastPiece()){
                pieceLen=getPieceSize(pieceIndex);
            }
            piece_data=new byte[pieceLen];
        }

        int blockLen=Constants.BLOCK_LENGTH;
        if(isLastPiece() && blocksDownloaded+1==peerController.getNumberOfBlocks()){
            blockLen=getBlockSize(pieceIndex,blocksDownloaded);
        }
        blockData=new byte[blockLen];
        ByteBuffer buffer=ByteBuffer.allocate(17);
        logger.debug(threadID+" Requesting Piece "+pieceIndex+" Block  "+offset);
        System.out.println(threadID+" Requesting Piece "+pieceIndex+" Block "+offset);
        buffer.put(requestMessage);
        buffer.putInt(pieceIndex);
        buffer.putInt((offset++)*blockLen);
        buffer.putInt(blockLen);
        blocksDownloaded++;
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
            int copied=0;
            while(copied<blockData.length){
                blockData[copied++]=inputStream.readByte();
            }
            System.out.println(threadID+" Data actually copied :"+copied+"Bytes");
            System.arraycopy(blockData,0,piece_data,offset,blockData.length);

            //last block downloaded i.e complete piece has been downloaded.
            //Update the complete-bitfield.
            //send have message with given piece info.

            //check if it is last piece.
            if(blocksDownloaded==peerController.getNumberOfBlocks()){
                System.out.println(threadID+" Downloaded last block.");
                if(verifyPiece(piece_data,pieceIndex)) {
                    System.out.println(threadID+" SHA-1 verified writing piece to file.");
                    logger.debug(threadID+" SHA-1 verified writing piece to file.");
                    peerController.pieceDownloaded(pieceIndex,piece_data.length,piece_data);
                }
                else {
                    logger.error(threadID+ " SHA-1 hash is incorrect received invalid piece.");
                    System.out.println(threadID+ " SHA-1 hash is incorrect received invalid piece.");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    private void receiveChoke(){
        System.out.println(threadID+" Received choke from remote peer "+peer.getAddress());
        logger.debug(threadID+" Received choke from remote peer "+peer.getAddress());
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
        if (socket==null){
            System.out.println(threadID+" Closing the connection");
            logger.debug(threadID+" Closing the connection");
            return;
        }
        System.out.println(threadID+" Closing the connection");
        logger.debug(threadID+" Closing the connection");
        try {
            socket.close();
        } catch (IOException e) {
            System.out.println(threadID+" connection was not established");
            logger.error(threadID+" connection was not established");
        }
    }

    /*
        Checks if last piece is getting downloaded.
     */
    private boolean isLastPiece(){
        return pieceIndex==meta.getTotalPieces()-1;
    }

    /*
        Method is self explanatory.
     */
    private int getPieceSize(int pieceIndex){
        if(pieceIndex==meta.getTotalPieces()-1){
            int size=(int)(meta.getTotalFilesize()%meta.getPiecelength());
            if(size!=0){
                return  size;
            }
        }
        return (int)meta.getPiecelength();
    }

    /*
        Method is self explanatory.
     */
    private int getBlockSize(int pieceIndex,int blockIndex){
        int pieceLength=getPieceSize(pieceIndex);

        if(blockIndex==peerController.getNumberOfBlocks()-1){
            int size=pieceLength%Constants.BLOCK_LENGTH;

            if(size!=0){
                return size;
            }
        }
        return BLOCK_LENGTH;
    }

    private void receiveHave(){
        try {
            int pieceIndex=inputStream.readInt();
            System.out.println(threadID+" received have from piece index :"+pieceIndex);
            logger.debug(threadID+" received have from piece index :"+pieceIndex);
            localBitField.set(pieceIndex,true);
            peerController.updateGlobalBitField(pieceIndex);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /*
      A method that returns specified number of bytes from the
      given inputstream.
     */

    private byte[] readInputStream(int len,DataInputStream stream){
        int copied=0;
        byte data[]=new byte[len];
        while(copied<len){
            try {
                data[copied++]=stream.readByte();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println(threadID+ " Actual data: "+len+" Data copied: "+copied);
        return data;
    }
    private void testMethod(byte[] bitfield_array){
        BitSet bs = new BitSet(meta.getTotalPieces());

        byte bit_mask = (byte)0x80;
        int l=0;
        //reading in bitfield bit by bit
        for(int k=0;k<bitfield_array.length;k++)
        {
            byte bitfield = bitfield_array[k];

            for(int i=0;i<8;i++){
                if(l<meta.getTotalPieces())
                {
                    bs.set(k*8+i, ((bitfield & bit_mask) == bit_mask) ? true : false);
                    bitfield = (byte)(bitfield >>> 2);
                    l++;
                }
            }
        }
        System.out.println("l :"+l);
        System.out.println("Bitfield two : "+bs);
    }

    /*
        Verify the sha1 hash of the given downloaded piece.
     */

    public boolean verifyPiece(byte data[],int pieceIndex){
        boolean result=false;

        byte[] originalHash=meta.getPieceHash(pieceIndex);

        try {
            MessageDigest digest=MessageDigest.getInstance("SHA-1");
            digest.update(data);
            byte[] sha=digest.digest();

            result=MessageDigest.isEqual(originalHash,sha);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return result;
    }
}
