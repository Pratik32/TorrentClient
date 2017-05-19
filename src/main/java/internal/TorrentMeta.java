package internal;

import BEcodeUtils.BencodeDecoder;
import BEcodeUtils.BencodeEncoder;
import BEcodeUtils.BencodeUtils;
import BEcodeUtils.Element;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.*;

/**
 * Created by ps on 17/3/17.
 */
public class TorrentMeta {

    String announce;
    List<String> annouce_list;
    String createdby;
    Date creation_date;
    Map<String,Long> files;
    long piecelength;
    byte[] pieces;
    String info_hash_hex;
    byte[] info_hash;
    int totalPieces;



    public String getInfo_hash_hex() {
        return info_hash_hex;
    }

    public void setInfo_hash_hex(String info_hash_hex) {
        this.info_hash_hex = info_hash_hex;
    }

    public byte[] getPieces() {
        return pieces;
    }

    public void setPieces(byte[] pieces) {
        this.pieces = pieces;
    }

    public byte[] getInfo_hash() {
        return info_hash;
    }

    public void setInfo_hash(byte[] info_hash) {
        this.info_hash = info_hash;
    }


    public String getAnnounce() {
        return announce;
    }

    public List<String> getAnnouce_list() {
        return annouce_list;
    }

    public String getCreatedby() {
        return createdby;
    }

    public Date getCreation_date() {
        return creation_date;
    }

    public Map<String, Long> getFiles() {
        return files;
    }

    public long getPiecelength() {
        return piecelength;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }

    public void setAnnouce_list(List<String> annouce_list) {
        this.annouce_list = annouce_list;
    }

    public void setCreatedby(String createdby) {
        this.createdby = createdby;
    }

    public void setCreation_date(Date creation_date) {
        this.creation_date = creation_date;
    }

    public void setFiles(Map<String, Long> files) {
        this.files = files;
    }

    public void setPiecelength(long piecelength) {
        this.piecelength = piecelength;
    }

    public int getTotalPieces(){
        return totalPieces;
    }
    public void setTotalPieces(int n){
        totalPieces=n;
    }
    public List<String> getFileSize(){
        List<String> file_sizes=null;
        return file_sizes;
    }

    public static TorrentMeta createTorrentMeta(byte[] data) throws IOException {
        TorrentMeta meta=new TorrentMeta();

        Element element= BencodeUtils.decode(data);
        Map<String,Element> dictionary=element.getMap();

        //set announce url.
        String announce_url=dictionary.get("announce").getString();
        System.out.println(announce_url);
        meta.setAnnounce(announce_url);

        //set announce-list.
        List<String> announce_list=dictionary.get("announce-list").getListOfString();
        System.out.println(announce_list);
        meta.setAnnouce_list(announce_list);

        //set Date.
        long long_date=dictionary.get("creation date").getLong();
        Date date=new Date(long_date);
        System.out.println(date);
        meta.setCreation_date(date);

        //set File-Size map.
        Map<String,Element> info=dictionary.get("info").getMap();
        Map<String,Long> files=getfiles(info);
        meta.setFiles(files);

        //set piece length
        Long piece_length=info.get("piece length").getLong();
        System.out.println(piece_length);
        meta.setPiecelength(piece_length);

        //set pieces
        byte[] pieces=info.get("pieces").getBytes();
        meta.setPieces(pieces);
        meta.setTotalPieces(pieces.length/20);

        //set info_hashes.
        byte[] info_value={0};
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        BencodeUtils.encode(dictionary.get("info").getValue(),stream);
        info_value=stream.toByteArray();
        Object[] hashes=getsha1Hash(info_value);
        meta.setInfo_hash((byte[]) hashes[0]);
        meta.setInfo_hash_hex((String) hashes[1]);

        return meta;
    }
    private static  Map<String,Long> getfiles(Map<String,Element> info){
        Map<String,Long> files=new HashMap<String, Long>();
        if(info.containsKey("files")){
            List<Map<String,Element>> dictionaries=info.get("files").getListOfMap();
            for(Map<String,Element> e:dictionaries){
                String filename=e.get("path").getList().get(0).getString();
                Long size=e.get("length").getLong();
                files.put(filename,size);
            }
        }else{
            String filename=info.get("name").getString();
            Long size=info.get("length").getLong();
            System.out.println(size);
            files.put(filename,size);
        }
        return files;
    }
    private static  Object[] getsha1Hash(byte[] data){
        Object[] sha1_hashes=new Object[2];
        byte[] info_hash= DigestUtils.sha1(data);
        sha1_hashes[0]=info_hash;
        String info_hash_hex=DigestUtils.sha1Hex(data);
        System.out.println(info_hash_hex);
        sha1_hashes[1]=info_hash_hex;
        return sha1_hashes;
    }

    /*
        returns addition of all file size.
     */
    public long getTotalFilesize(){
        long totalSize=0;
        for(Long l:files.values()){
            totalSize+=l;
        }
        return totalSize;
    }
    /*
        returns filenames in the torrent file.
     */
    public List<String> getFileNames(){
        List<String> fileList=new ArrayList<String>();
        for(String name:files.keySet()){
            fileList.add(name);
        }
        return fileList;
    }
}