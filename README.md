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

Apache Commons Codec:
https://mvnrepository.com/artifact/commons-codec/commons-codec/1.10

Apache httpclient:
https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient/4.5.2

log4j:
https://mvnrepository.com/artifact/log4j/log4j/1.2.17

java 1.8.101+ (or javafx+java 1.8+)
http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

Screenshot:

![Screenshot]( /src/main/resources/sample.png?raw=true "GUI")


