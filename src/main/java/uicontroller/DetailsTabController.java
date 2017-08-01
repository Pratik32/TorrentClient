package uicontroller;

import internal.TorrentMeta;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 * Created by ps on 2/8/17.
 */
public class DetailsTabController {
    public Label name;
    public Label path;
    public Label size;
    public Label hash;
    public Label comments;
    public AnchorPane anchorpane;

    FXMLLoader loader;

    public AnchorPane setupDetailsTab(TorrentMeta meta,String storagePath){
        if (loader==null) {
            loader = new FXMLLoader(getClass().getResource("/detailstab.fxml"));
            loader.setController(this);
        }
        try {
            loader.load();
        } catch (IOException e) {
            System.out.println("cannot load given fxml");
        }
        setContent(meta,storagePath);
        return anchorpane;
    }

    public void setContent(TorrentMeta meta,String storagePath){
        String filename=meta.isMultiFileMode()?meta.getFoldername():meta.getFileNames().get(0);
        name.setText(filename);
        path.setText(storagePath);
        size.setText(String.valueOf(meta.getTotalFilesize()));
        hash.setText(meta.getInfo_hash_hex());
        comments.setText("No comments");
    }


}
