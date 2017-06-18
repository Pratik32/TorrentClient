package interfaces;

import internal.TorrentMeta;

/**
 * Created by ps on 14/6/17.
 *
 * This interface will be used by UI components to get different
 * details related to torrent.(meta data) and also dynamic params
 * (downloaded,uploaded,download speed,upload speed).
 * This interface will be implemented by PeerController.
 */
public interface UIController {

    long getDownloaded();
    long getUploaded();
    int getDownloadSpeed();
    int getUploadSpeed();
    TorrentMeta getTorrentMeta();

}
