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
 */
public class StatusTabController {
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
    public JFXProgressBar progress;
    public AnchorPane anchorpane;
    public AnchorPane setupStatusTab(TorrentMeta meta){
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/statustab.fxml"));
        try {
            loader.load();
            loader.setController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        String titletext=meta.isMultiFileMode()?meta.getFoldername():meta.getFilenames().get(0);
        System.out.println(titletext);
        if (title==null){
            System.out.println("null");
        }
        title.setText(titletext);
        downloadinglabel.setText("Downloading");
        String downloadString="0 B/"+ Utils.getConvertedBytes(meta.getTotalFilesize());
        downloading.setText(downloadString);
        downloadspeed.setText("0 KB/s");
        uploadspeed.setText("0 KB/s");
        uploaded.setText("0KB");
        seeders.setText("52");
        leechers.setText("2");
        eta.setText("? hr ? min");
        progress.setProgress(0.0d);
        return anchorpane;
    }

}
