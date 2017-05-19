package Peers;

import Tracker.HttpTrackerSession;
import Tracker.TrackerRequestPacket;
import Tracker.TrackerSession;
import Tracker.TrakcerResponsePacket;
import internal.Constants;
import internal.TorrentMeta;
import internal.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static internal.Constants.BLOCK_LENGTH;

/**
 * Created by ps on 7/5/17.
 *
 * Represents a Controller for all PeerConnection instances.
 * This class will calculate all parameters(piece length,block length).
 * Also it will keep the track of which pieces/blocks are downloaded/downloading.
 * which peer is downloading which block.Scheduling a download for peer.
 * Keeping track of active peer list.
 * A downloading strategy(Rarest first) for downloading pieces.
 */
public class PeerController {
    private long pieceLength;
    private int blockLength;
    private int numberOfBlocks;//per piece.
    private int numberOfPieces;
    private long totalFileSize=0;
    private HashMap<Peer,List<Integer>> blocksMap=new HashMap<Peer,List<Integer>>();
    private List<Peer> peerList;
    private int blockNo=0;
    private int pieceNo=0;
    private long trackerParams[]=new long[3];//downloaded,left,uploaded.

    /*
        There is a proper strategy for selecting active-peer list.(Choke/Unchoke algorithm.)
        Need to implement that.Right now just selecting first five peers.
     */
    private List<Peer> activePeerList;
    TorrentMeta meta;

    public PeerController(TorrentMeta meta,List<Peer> list){
        this.meta=meta;
        this.peerList=list;
        trackerParams[2]=meta.getTotalFilesize();
        initParams();
    }

    /*

     */
    public void start(){
        try {
            Constants.logger.debug("PeerController started");
            byte[] piece_data = new byte[(int) pieceLength];
            activePeerList = getActivePeerList();

            //Assign a PeerConnection to each peer.
            List<PeerConnection> connectionList = new ArrayList<PeerConnection>();
            for (Peer p : activePeerList) {
                PeerConnection connection = new PeerConnection(p, meta, piece_data, this);
                connectionList.add(connection);
                connection.start();
            }
            for (PeerConnection p:connectionList){
                p.join();
            }
            //Utils.writeToFile(meta.getFileNames().get(0),piece_data,pieceNo,(int) pieceLength);
        }catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public synchronized  int[] getBlockParams(){
        int[] params=new int[3];
        System.out.println("Block number "+blockNo);
        if(numberOfBlocks>blockNo){
            params[0]=pieceNo;
            params[1]=blockNo++;
        }else{
            params[0]=-1;
            params[1]=-1;
        }
        params[2]=BLOCK_LENGTH;
        return params;
    }


    /*
       This method will return next piece to download.
       Based on rarest first method.(based on bittorent docs.)
     */
    public synchronized int getNextPieceToDownload(){
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
        numberOfPieces=(int)(Math.ceil(meta.getTotalFilesize()/pieceLength));
        Constants.logger.debug("Init Params: "+"piece length :"+pieceLength+"blockLength :"+blockLength+"TotalSize :"+totalFileSize+"pieces:"+numberOfPieces+"blocks per piece"+numberOfBlocks);
        System.out.println("Init Params: "+"piece length :"+pieceLength+"blockLength :"+blockLength+"TotalSize :"+totalFileSize+"pieces:"+meta.getTotalPieces()+"blocks per piece"+numberOfBlocks);
    }


    /*
        method for updating peerlist(not the activePeerList).
        This method will make Trakcer request.
        in return it will receive list of peers returned by tracker.
        this method must be called after "interval" amount of time has passed.

     */
    private List<Peer> updatePeerList(long downloaded,long uploaded,long left){
        TrackerRequestPacket packet= Utils.craftPacket(meta,downloaded,uploaded,left);
        TrackerSession session=new HttpTrackerSession(meta);
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
        for(int i=0;i<1;i++){
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

    public void pieceDownloaded(int pieceIndex,int pieceLength,byte[] data){
        Utils.writeToFile(meta.getFileNames().get(0),data,pieceIndex,pieceLength);
        //update different bitsets(global,complete).
    }

    public long getPieceLength() {
        return pieceLength;
    }

}
