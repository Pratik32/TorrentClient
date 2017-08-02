package uicontroller;

import com.jfoenix.controls.JFXProgressBar;
import internal.TorrentMeta;
import internal.Utils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

/**
 * Created by ps on 25/7/17.
 * Represents the content of status-tab in tabpane.
 * When no torrent session is scheduled, the status-tab
 * will show No Content message.When a download is scheduled
 * or when item on session list is clicked the status-tab
 * is updated with given torrent session's data.
 */
public class StatusTabController {

    public static final String FXML_FILE ="/status.fxml";
    @FXML
    public Label title;
    public Label downloadinglabel;
    public Label downloadspeed;
    public Label uploadspeed;
    public Label downloading;
    public Label eta;
    public Label leechers;
    public Label seeders;
    public Label uploaded;
    @FXML
    public JFXProgressBar progress;
    public AnchorPane anchorpane;
    FXMLLoader loader;
    public AnchorPane setupStatusTab(TorrentMeta meta){
        if (loader==null) {
            loader = new FXMLLoader(getClass().getResource(FXML_FILE));
            loader.setController(this);
        }
        try {
            loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        setContent(meta);
        return anchorpane;
    }
    public void setContent(TorrentMeta meta){
        String titletext=meta.isMultiFileMode()?meta.getFoldername():meta.getFilenames().get(0);
        System.out.println(titletext);
        if (progress==null){
            System.out.println("error");
        }
        if (title==null){
            System.out.println("null");
        }
        title.setText(titletext);
        downloadinglabel.setText("Downloading");
        String downloadString="200MB/"+ Utils.getConvertedBytes(meta.getTotalFilesize());
        downloading.setText(downloadString);
        downloadspeed.setText("500KB/s");
        uploadspeed.setText("52KB/s");
        uploaded.setText("20KB");
        seeders.setText("52");
        leechers.setText("2");
        eta.setText("1hr 20min");
        progress.setProgress(0.0d);
    }

}
