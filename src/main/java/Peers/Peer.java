package Peers;

import java.net.InetSocketAddress;

/**
 * Created by ps on 6/4/17.
 */
public class Peer {

    InetSocketAddress address;
    byte[] peer_id;

    public Peer(InetSocketAddress address,byte[] peer_id){
        this.address=address;
        this.peer_id=peer_id;
    }
    public InetSocketAddress getAddress() {
        return address;
    }

    public void setAddress(InetSocketAddress address) {
        this.address = address;
    }

    public byte[] getPeer_id() {
        return peer_id;
    }

    public void setPeer_id( byte[] peer_id) {
        this.peer_id = peer_id;
    }

}
