package BEcodeUtils;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by ps on 24/3/17.
 *
 * This class represents a bencode element.
 * A bencode element maybe a -
 * List,Dictionary,Integer,String.
 *
 */
public class Element implements Serializable{
    /*
        Represents the value of Bencode element.
     */
    private Object value;


    Element(byte[] data){
        this.value=data;
    }
    Element(Map<String,Element> map){
        this.value=map;
    }
    public String getString() {
        String str="";
        try {
            str=new String(getBytes(),"UTF-8");
        }catch (UnsupportedEncodingException e){

        }

        return str;
    }

    public byte[] getBytes(){
        return (byte[])value;
    }
    Element(Long value){
        this.value=value;
    }
    Element(List<Element> list){
        this.value=list;
    }
    public Map<String,Element> getMap(){
        return (Map<String,Element>)value;
    }
    public Long getLong(){
        return (Long)value;
    }
    public List<Element> getList(){
       return  (List<Element>)value;
    }
    public List<String> getListOfString(){
        List<Element> elements=(List<Element>)value;
        List<String> strings=new ArrayList<String>();
        for(Element e:elements){
            strings.add(e.getList().get(0).getString());
        }
        return strings;
    }
    public List<Map<String,Element>> getListOfMap(){
        List<Map<String,Element>> map=new ArrayList<Map<String,Element>>();
        List<Element> elements=(List<Element>)value;
        for(Element e:elements){
            map.add(e.getMap());
        }
        return map;
    }
    public Object getValue(){
        return value;
    }

}
