package peer;

import interfaces.UIController;
import internal.CustomLogger;
import org.apache.log4j.Logger;
import tracker.HttpTrackerSession;
import tracker.TrackerRequestPacket;
import tracker.TrackerSession;
import tracker.TrakcerResponsePacket;
import internal.Constants;
import internal.TorrentMeta;
import internal.Utils;

import java.nio.ByteBuffer;
import java.util.*;

import static internal.Constants.BLOCK_LENGTH;

/**
 * Created by ps on 7/5/17.
 *
 * Represents a Controller for all PeerConnection instances(of a single torrent session).
 * This class will calculate all parameters(piece length,block length).
 * Also it will keep the track of which pieces/blocks are downloaded/downloading.
 * which peer is downloading which block.Scheduling a download for peer.
 * Keeping track of active peer list.
 * A downloading strategy(Rarest first,Sequential.) for downloading pieces.
 * This class also implements UIController which provides interface for UI
 * components.
 *
 */
public class PeerController implements UIController {
    private long pieceLength;
    private int blockLength;
    private int numberOfBlocks;//per piece.
    private int numberOfPieces;
    private long totalFileSize=0;
    private List<Peer> peerList;
    private int pieceNo=0;
    private long trackerParams[]=new long[3];//downloaded,left,uploaded.
    private BitSet globalBitField;
    private BitSet workingBitField;
    private BitSet completedBitField;
    private int[] pieceFrequency;
    private Logger logger;
    private List<PeerConnection> peerConnections;


    /*
        There is a proper strategy for selecting active-peer list.(Choke/Unchoke algorithm.)
        Need to implement that.Right now just selecting first five peers.
     */
    private List<Peer> activePeerList;
    TorrentMeta meta;

    public PeerController(TorrentMeta meta,List<Peer> list){
        this.meta=meta;
        this.peerList=list;
        trackerParams[1]=meta.getTotalFilesize();
        globalBitField=new BitSet(meta.getTotalPieces());
        workingBitField=new BitSet(meta.getTotalPieces());
        completedBitField=new BitSet(meta.getTotalPieces());
        pieceFrequency=new int[meta.getTotalPieces()];
        logger= CustomLogger.getInstance();
        peerConnections=new ArrayList<PeerConnection>();
        initParams();
    }

    /*
        starts the peer controller.
     */
    public void start(){
        try {
            logger.debug("PeerController started");
            byte[] piece_data = new byte[(int) pieceLength];
            activePeerList = getActivePeerList();

            //Assign a PeerConnection to each peer.
            for (Peer p : activePeerList) {
                PeerConnection connection = new PeerConnection(p, meta, piece_data, this);
                peerConnections.add(connection);
                connection.start();
            }
            for (PeerConnection p:peerConnections){
                p.join();
            }
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

    }


    /*
       This method will return next piece to download.
       Based on rarest first method.(based on bittorent docs.)
     */
    public synchronized int getNextPieceToDownload(BitSet set){
       return getNextPiece(set);
    }

    /*
      method is self explanatory.
     */
    public synchronized int testgetNextPieceToDownload(){
        if(pieceNo<numberOfPieces){
            return pieceNo++;
        }
        return -1;
    }
    /*
      setting up initial parameters.

     */
    private void initParams(){
        pieceLength=meta.getPiecelength();
        blockLength=BLOCK_LENGTH;
        numberOfBlocks=(int)(pieceLength/BLOCK_LENGTH);
        totalFileSize=meta.getTotalFilesize();
        numberOfPieces=meta.getTotalPieces();
        logger.debug("Init Params: "+"piece length :"+pieceLength+"blockLength :"+blockLength+"TotalSize :"+totalFileSize+"pieces:"+numberOfPieces+"blocks per piece"+numberOfBlocks);
        System.out.println("Init Params: "+"piece length : "+pieceLength+" blockLength :"+blockLength+" TotalSize :"+totalFileSize+" pieces: "+meta.getTotalPieces()+" blocks per piece "+numberOfBlocks);
    }


    /*
        method for updating peerlist(not the activePeerList).
        This method will make Trakcer request.
        in return it will receive list of peers returned by tracker.
        this method must be called after "interval" amount of time has passed.
     */
    private List<Peer> updatePeerList(long downloaded,long uploaded,long left){
        TrackerRequestPacket packet= Utils.craftPacket(meta,downloaded,uploaded,left);
        TrackerSession session=new HttpTrackerSession(meta,meta.getAnnounce());
        TrakcerResponsePacket responsePacket=session.sendRequest(packet);
        return Utils.getPeerList(responsePacket);
    }

     /*
        This method is supposed to return activePeerList based on choke algorithm
        for now returning first 5 peers.
        as per the docs active peerset is set of 5 peers.
      */

    private List<Peer> getActivePeerList(){
        List<Peer> active=new ArrayList<Peer>(5);
        System.out.println(peerList.size());
        for(int i=0;i<5;i++){
            //Peer p=peerList.remove(i);
            active.add(peerList.get(i));
        }
        return active;
    }
    public int getNumberOfBlocks() {
        return numberOfBlocks;
    }

    public void setNumberOfBlocks(int numberOfBlocks) {
        this.numberOfBlocks = numberOfBlocks;
    }

    public synchronized void pieceDownloaded(int pieceIndex,int pieceLength,byte[] data,String threadID){
        completedBitField.set(pieceIndex,true);
        workingBitField.set(pieceIndex,false);
        pieceFrequency[pieceIndex]=-1;
        trackerParams[0]+=pieceLength;
        trackerParams[1]-=trackerParams[0];
        List<String> filename=new ArrayList<String>(meta.getFilenames());
        List<Long> filesize=new ArrayList<Long>(meta.getFilesizes());

        System.out.println(threadID+ " files :" +filename);
        System.out.println(threadID+ " file sizes "+ filesize);
        int pieceOffset=pieceIndex*(int)this.pieceLength;
        int pieceEnd=pieceOffset+(pieceLength-1);

        int fileStart=0;
        int fileEnd=-1;
        for(int i=0;i<filename.size();i++){
            fileStart=fileEnd+1;
            fileEnd=fileStart+filesize.get(i).intValue()-1;
            if((pieceOffset<fileStart && pieceEnd<fileStart)||(pieceOffset>fileEnd && pieceEnd>fileEnd)){
                System.out.println(threadID+" Piece is not the part of :"+ filename.get(i));
            }else{
                int start=Math.max(pieceOffset,fileStart);
                int end=Math.min(pieceEnd,fileEnd);
                int offset=Math.abs(start-fileStart);
                byte[] temp=new byte[end-start+1];
                System.out.println(threadID+ " start :"+start+ "end "+end+"offset "+offset);
                System.arraycopy(data,Math.abs(start-pieceOffset),temp,0,temp.length);
                Utils.writeToFile(filename.get(i),offset,temp);
            }
        }
    }

    public synchronized void  updateGlobalBitField(BitSet set){
        globalBitField.or(set);
        for(int i=0;i<meta.getTotalPieces();i++){
            if(set.get(i)==true && !completedBitField.get(i)){
                globalBitField.set(i,true);
                pieceFrequency[i]++;
            }
        }
    }

    public synchronized void updateGlobalBitField(int index){
        globalBitField.set(index,true);
        pieceFrequency[index]++;
    }


    /*
        Implementing rarest first strategy for choosing next piece to download.
     */
    private synchronized int getNextPiece(BitSet peerBitSet){
        int pieceIndex=-1;

        int minFreq=0;

        List<Integer> list=new ArrayList<Integer>();
        for(int i=0;i<globalBitField.length();i++){
            if(globalBitField.get(i) && peerBitSet.get(i) && !workingBitField.get(i) && !completedBitField.get(i) &&(minFreq==0 || minFreq>pieceFrequency[i])){
                minFreq=pieceFrequency[i];
            }
        }

        for(int i=0;i<globalBitField.length();i++){
            if(globalBitField.get(i) && peerBitSet.get(i) && !workingBitField.get(i) && !completedBitField.get(i) &&(pieceFrequency[i]==minFreq)){
                list.add(i);
            }
        }

        //choose randomly.
        System.out.println("Size of list "+ list.size());
        if(list.size()>0) {
            Random random = new Random();
            int index = random.nextInt(list.size());
            pieceIndex = list.get(index);
            workingBitField.set(pieceIndex, true);
        }
        System.out.println("Next choosen piece is : "+pieceIndex+" frequency "+minFreq);
        return pieceIndex;

    }
    /*
        This method will be called by PeerConnection.
        To let PeerController know that it is closing.
        Here PeerConnection may choose to start another PeerConnection.
     */
    public void notifyController(Peer p){
        System.out.println(p.getAddress()+" is offline");
        logger.debug(p.getAddress()+" is offline");
        startNewPeerConnection();
    }

    private synchronized  void startNewPeerConnection(){
        if(!peerList.isEmpty()) {
            Peer p = peerList.remove(0);
            PeerConnection connection=new PeerConnection(p,meta,new byte[(int)pieceLength],this);
            connection.start();
        }
        else{
            //maybe request new peers or just do nothing.
        }
    }

    /*
        To serve 'have' requests.
     */
    public byte[] readBlockData(int pieceIndex,int offset,int blockLen){
        trackerParams[2]+=blockLen;
        int blockOffset=pieceIndex*(int)this.pieceLength+offset;
        int blockEnd=blockOffset+(blockLen-1);

        List<String> filename=meta.getFilenames();
        List<Long>   filesize=meta.getFilesizes();

        int fileStart=0;
        int fileEnd=-1;
        ByteBuffer buffer=ByteBuffer.allocate(blockLen);
        for(int i=0;i<filename.size();i++){
            fileStart=fileEnd+1;
            fileEnd=filesize.get(i).intValue();
            if((blockOffset<fileStart && blockEnd<fileStart)||(blockOffset>fileEnd && blockEnd>fileEnd)){
                System.out.println("Block is not the part of file: "+filename.get(i));
            }
            else{
                int start=Math.max(blockOffset,fileStart);
                int end=Math.min(blockEnd,fileEnd);
                int fileOffset=Math.abs(start-fileStart);
                byte[] temp=new byte[end-start+1];
                Utils.readFromFile(filename.get(i),fileOffset,temp);
                buffer.put(temp);
            }

        }
        return buffer.array();
    }

    public long getDownloaded() {
        return trackerParams[0];
    }

    public long getUploaded() {
        return trackerParams[2];
    }

    public int getDownloadSpeed() {
        return 0;
    }

    public int getUploadSpeed() {
        return 0;
    }

    public TorrentMeta getTorrentMeta() {
        return meta;
    }
    public void removePeerConnetion(PeerConnection connection){
        peerConnections.remove(connection);
    }

    public long perUnitDownloadSpeed(){
        long totalDownloaded=0;

        for(PeerConnection connection:peerConnections){
            totalDownloaded+=connection.getPerUnitDownloaded();
        }
        return totalDownloaded;
    }
    public String getETA(long perUnitDownloadSpeed){
        double time=(double) totalFileSize/perUnitDownloadSpeed;
        String timeString=Utils.getETAString(time);
        return timeString;
    }
}
