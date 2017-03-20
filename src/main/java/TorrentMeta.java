import java.util.HashMap;
import java.util.List;

/**
 * Created by ps on 17/3/17.
 */
public class TorrentMeta {

    String announce;
    List<String> annouce_list;
    String createdby;
    String creation_date;
    HashMap<String,Long> files;
    long piecelength;
    String pieces;
    String info_hash;

    public byte[] getInfo_hash_raw() {
        return info_hash_raw;
    }

    public void setInfo_hash_raw(byte[] info_hash_raw) {
        this.info_hash_raw = info_hash_raw;
    }

    byte[] info_hash_raw;
    public String getPieces() {
        return pieces;
    }

    public void setPieces(String pieces) {
        this.pieces = pieces;
    }

    public String getInfo_hash() {
        return info_hash;
    }

    public void setInfo_hash(String info_hash) {
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

    public String getCreation_date() {
        return creation_date;
    }

    public HashMap<String, Long> getFiles() {
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

    public void setCreation_date(String creation_date) {
        this.creation_date = creation_date;
    }

    public void setFiles(HashMap<String, Long> files) {
        this.files = files;
    }

    public void setPiecelength(long piecelength) {
        this.piecelength = piecelength;
    }

    public List<String> getFileSize(){
        List<String> file_sizes=null;
        return file_sizes;
    }
}