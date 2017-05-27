package BEcodeUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by ps on 26/3/17.
 * Utility for decoding and encoding BECODE files.
 *
 */
public class BencodeUtils {


    public static Element decode(byte data[]){
        BencodeDecoder decoder=new BencodeDecoder(data);
        return decoder.decode();
    }
    public static void encode(Object obj, OutputStream stream) throws IOException {
        BencodeEncoder encoder=new BencodeEncoder();
        encoder.encode(obj,stream);
    }
}
