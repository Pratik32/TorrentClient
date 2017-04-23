package BEcodeUtils;

import org.apache.commons.io.output.ByteArrayOutputStream;


import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by ps on 25/3/17.
 */
public class BencodeEncoder {

    int pos=0;
      void encode(Object e, OutputStream data) throws IOException {

        if(e instanceof Map){
            data.write('d');
            //System.out.print("d");
            encodeMap((Map<String,Element>)e,data);
            data.write('e');
            //System.out.print("e");
        }
        else if(e instanceof List){
            data.write('l');
            //System.out.print("l");
            encodeList((List<Element>)e,data);
            //System.out.print("e");
            data.write('e');
        }
        else if(e instanceof Long){
            data.write('i');
            Long l=(Long)e;
            String str=String.valueOf(l);
            /*System.out.print("i");
            System.out.print(str);
            System.out.print("e");*/
            //byte[] result=serializeObject(l);
            data.write(str.getBytes("UTF-8"));
            data.write('e');
        }
        else if(e instanceof byte[]){
            //System.out.println("string");
            byte string_bytes[]=(byte[])(e);
            String len=String.valueOf(new Integer(string_bytes.length));
            data.write(len.getBytes("UTF-8"));
           /* System.out.print(len);
            System.out.print(":");
            System.out.print(new String(string_bytes,"UTF-8"));*/
            data.write(':');
            data.write(string_bytes);
        }
        else if(e instanceof String){
            String str=(String)e;
            String len=String.valueOf(new Integer(str.length()));
            data.write(len.getBytes("UTF-8"));
           // System.out.print(len);
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
