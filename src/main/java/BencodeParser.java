import org.apache.commons.codec.digest.DigestUtils;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static java.nio.file.Files.readAllBytes;

/**
 * Created by ps on 14/3/17.
 */
public class BencodeParser {

    List<String> decodedStrings;

    BencodeParser(){
        decodedStrings=new ArrayList<String>();
    }
    public TorrentMeta parseTorrentFile(String filename) throws IOException, NoSuchAlgorithmException {
        File file=new File(filename);
        BufferedReader reader=new BufferedReader(new FileReader(file));
        String s="";
        StringBuilder builder=new StringBuilder();
        while((s=reader.readLine())!=null){
            builder.append(s);
        }
        String complete_file=builder.toString();
        s=complete_file.substring(0,complete_file.indexOf(":pieces")-1);
        decode(s,0);
        TorrentMeta meta=createTorrentMetaFile(complete_file,file);
        return meta;
    }

    private int decode(String s,int idx){
        char c=s.charAt(idx);
        switch(c){
            case 'i':
                idx=parseInteger(s,idx);
                break;
            case 'l':
                idx=parseList(s,idx+1);
                break;
            case 'd':
                idx=parseDictionary(s,idx+1);
                break;
            default :
                idx=parseString(s,idx);
        }
        return idx;
    }

    private int parseDictionary(String s,int idx){

        while(idx<s.length() && s.charAt(idx)!='e'){
            idx=decode(s,idx);
        }
        return idx+1;
    }

    private int parseList(String s,int idx){
        //System.out.println("parseList");
        while(idx<s.length() && s.charAt(idx)!='e'){
            idx=decode(s,idx);
        }
        return idx+1;
    }

    private int parseString(String s,int idx){
        //System.out.println("parseString");
        int colon=s.indexOf(":",idx);
        int length=Integer.parseInt(s.substring(idx,colon));
        String str=s.substring(colon+1,colon+1+length);
        decodedStrings.add(str);
        return (colon+1+length);
    }

    private int parseInteger(String s,int idx){
        //System.out.println("parseInteger");
        int end_idx=s.indexOf("e",idx);
        int number=Integer.parseInt(s.substring(idx+1,end_idx));
        decodedStrings.add(String.valueOf(number));
        return (end_idx+1);
    }
    private TorrentMeta createTorrentMetaFile(String complete_file,File file) throws IOException, NoSuchAlgorithmException {
        System.out.println(decodedStrings);
        TorrentMeta meta=new TorrentMeta();
        String annouce=decodedStrings.get(1);
        meta.setAnnounce(annouce);
        List<String> annouce_list=new ArrayList<String>();
        int i=3;
        while(i<decodedStrings.size()) {
            if (decodedStrings.get(i).equals("comment") || decodedStrings.get(i).equalsIgnoreCase("created by") || decodedStrings.get(i).equalsIgnoreCase("creation date")) {
                break;
            }
            else{
                annouce_list.add(decodedStrings.get(i));
                i++;
            }
        }
        meta.setAnnouce_list(annouce_list);
        i=decodedStrings.indexOf("created by");
        if(i!=-1){
            meta.setCreatedby(decodedStrings.get(i+1));
        }
        i=decodedStrings.indexOf("creation date");
        if(i!=-1){
            meta.setCreation_date(decodedStrings.get(i+1));
        }
        i=decodedStrings.indexOf("length");
        HashMap<String,Long> files=new HashMap<String, Long>();
        while(i<decodedStrings.size() && decodedStrings.get(i).equalsIgnoreCase("length")){
            i++;
            Long size=Long.valueOf(decodedStrings.get(i));
            i=i+2;
            String filename=decodedStrings.get(i);
            files.put(filename,size);
            i++;
        }
        meta.setFiles(files);

        i=decodedStrings.indexOf("piece length");
        meta.setPiecelength(Long.parseLong(decodedStrings.get(i+1)));

        int idx=complete_file.indexOf("info")+4;
        String info_key=complete_file.substring(idx,complete_file.lastIndexOf("e"));
        String info_hash=" ";
        info_hash=getSHA1String(idx,file);
        byte[] raw_hash=getSHA1Raw(idx,file);
        System.out.println(info_hash);
        meta.setInfo_hash(info_hash);
        meta.setInfo_hash_raw(raw_hash);
        String pieces=getPieces(complete_file,complete_file.lastIndexOf("e")-1);
        meta.setPieces(pieces);
        return meta;
    }
    private String getPieces(String info_key,int len){
        int temp=info_key.indexOf("pieces");
        int start=info_key.indexOf(":",temp);
        String pieces=info_key.substring(start+1,len);
        return pieces;
    }
    private String getSHA1String(int idx,File file) throws IOException {
        byte[] original=Files.readAllBytes(file.toPath());
        byte[] data= Arrays.copyOfRange(original,idx,original.length-1);
        String ans=DigestUtils.sha1Hex(data);
        return ans;
    }
    private byte[] getSHA1Raw(int idx,File file) throws IOException, NoSuchAlgorithmException {
        byte[] original=Files.readAllBytes(file.toPath());
        byte[] data= Arrays.copyOfRange(original,idx,original.length-1);
        MessageDigest digest=MessageDigest.getInstance("SHA-1");
        byte[] hash=digest.digest(data);
        System.out.println(TrackerConnection.toHexString(hash));
        return hash;
    }

}