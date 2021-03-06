package peer;

import java.net.InetSocketAddress;

/**
 * Created by ps on 6/4/17.
 * Represents a remote peer.
 *
 */
public class Peer {

    InetSocketAddress address;
    byte[] peer_id;
    double downloadSpeed;
    double uploadSpeed;

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

    @Override
    public int hashCode() {
        return address.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(this==obj){
            return true;
        }
        if(obj instanceof Peer){
            Peer p=(Peer)obj;
            return this.address.toString().equals(p.getAddress().toString());
        }
        return false;
    }
}
