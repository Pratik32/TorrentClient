package BEcodeUtils;

import java.io.EOFException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ps on 24/3/17.
 *
 * This class is used for decoding torrent-file.
 * As torrent file does not have any encoding(UTF,ASCII)
 * Parsing is done at byte level.
 */
public class BencodeDecoder {

    //Data from torrent file.
    private final byte[] data;

    //current byte read.
    private int current_byte=0;
    private int pos=0;

    public BencodeDecoder(byte[] data){
        this.data=data;
    }

    protected Element decode(){
        Element element=null;

        current_byte=getNextByte();

        switch (current_byte){
            case 'd':
                element=parseDictionary();
                break;
            case 'i':
                element=parseInteger();
                break;
            case 'l':
                System.out.println("list");
                element=parseList();
                break;
            default:
                pos--;
                element=parseString();

        }
        return element;
    }
    private int getNextByte(){
        try{
            if(pos>=data.length){
                throw new EOFException();
            }
        }
        catch (EOFException e){
            System.out.println("End of file reached.");
            return -1;
        }

        int temp=data[pos++];
        return  temp;
        /*int temp=0;
        if(current_byte==0){
            temp
        }*/
    }
    private int read(){
        int temp=data[pos++];
        return temp;
    }
    private byte[] read(int strlen){
        byte[] array=new byte[strlen];
        for(int i=0;i<strlen;i++){
            array[i]=data[pos++];
        }
        return array;
    }
    private Element parseDictionary(){
        Map<String,Element> dictionary=new HashMap<String, Element>();
        while(current_byte!='e'){
            String key=parseString().getString();
            Element value=decode();
            dictionary.put(key,value);
            current_byte=getNextByte();
            if(current_byte!='e') {
                pos--;
            }
            //System.out.println(data[pos+1]-'0');
        }
        //System.out.println("dictionary closed");
        Element e=new Element(dictionary);
        return e;
    }
    private Element parseString(){
        current_byte=getNextByte();
        try {
            if (current_byte < '0' || current_byte > '9') {
            }
        }catch (Exception e){
            System.out.println("Expected string length as integer found : "+current_byte);
        }
        int strlen=current_byte-'0';
        int curr=current_byte-'0';
        int prev=0;

        while(curr>=0 && curr<=9){
            strlen=curr+prev*10;
            current_byte=getNextByte();
            curr=current_byte-'0';
            prev=strlen;
        }
        byte[] array=read(strlen);
        Element element=new Element(array);
        System.out.println(element.getString());
        return element;
    }
    private Element parseInteger(){
        current_byte=getNextByte();
        long strlen=current_byte-'0';
        int curr=current_byte-'0';
        long prev=0;

        while(curr>=0 && curr<=9){
            strlen=curr+prev*10;
            current_byte=getNextByte();
            curr=current_byte-'0';
            prev=strlen;
        }

        Element element=new Element(new Long(strlen));
        return element;
    }
    private Element parseList(){
        List<Element> list=new ArrayList<Element>();
        while(current_byte!='e'){
            list.add(decode());
            current_byte=getNextByte();
            if (current_byte!='e'){
                pos--;
            }
        }
        Element element=new Element(list);
        return element;
    }
}
