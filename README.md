# A Bittorrent Client

An implementation of [bittorrent protocol](https://wiki.theory.org/index.php/BitTorrentSpecification) in java.

Description
-----------

Supports following Bittorrent enhancement proposals(BEPs):

* Bittorrent protocol specification:
  This is the basic implmentation of bittorrent protocol.
* Multi-tracker support:
  Supports 'announce-list' in .torrent file.(multiple tracker requests.)
* UDP tracker protocol:
  Implemenation of UDP tracker protocol (BEP13).
* Compact response from trackers:
  Supports compact response from tracker.Trackers send ip:port pair(omitting peer ID.)
 
Required libraires
------------------

Apache Commons IO:

https://mvnrepository.com/artifact/commons-io/commons-io/2.5
  
 


