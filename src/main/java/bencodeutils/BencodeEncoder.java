package bencodeutils;

import org.apache.commons.io.output.ByteArrayOutputStream;


import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by ps on 25/3/17.
 * Encodes a byte stream into bencode elements(dictionary,list,string,number)
 */
public class BencodeEncoder {

    int pos=0;
      void encode(Object e, OutputStream data) throws IOException {

        if(e instanceof Map){
            data.write('d');
            encodeMap((Map<String,Element>)e,data);
            data.write('e');
        }
        else if(e instanceof List){
            data.write('l');
            encodeList((List<Element>)e,data);
            data.write('e');
        }
        else if(e instanceof Long){
            data.write('i');
            Long l=(Long)e;
            String str=String.valueOf(l);
            data.write(str.getBytes("UTF-8"));
            data.write('e');
        }
        else if(e instanceof byte[]){
            byte string_bytes[]=(byte[])(e);
            String len=String.valueOf(new Integer(string_bytes.length));
            data.write(len.getBytes("UTF-8"));
            data.write(':');
            data.write(string_bytes);
        }
        else if(e instanceof String){
            String str=(String)e;
            String len=String.valueOf(new Integer(str.length()));
            data.write(len.getBytes("UTF-8"));
            data.write(':');
            //System.out.print(":");
            data.write(str.getBytes("UTF-8"));
            //System.out.print(str);
        }
    }
    private void encodeMap(Object obj,OutputStream data) throws IOException {
        Map<String,Element> map=(Map<String,Element>)obj;

        Set<String> l=map.keySet();
        List<String> temp=new ArrayList<String>(l);
        Collections.sort(temp);
        for (String key:temp){
            Element value=map.get(key);
            encode(key,data);
            encode(value.getValue(),data);
        }
    }
    private  void encodeList(Object obj,OutputStream data)throws IOException{
        List<Element> list=(List<Element>)obj;
        for(Element e:list){
            encode(e.getValue(),data);
        }
    }
    private byte[] serializeObject(Long l){
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        byte[] result={0};
        try {
            ObjectOutput output=new ObjectOutputStream(stream);
            output.writeObject(l);
            result=stream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }
}
