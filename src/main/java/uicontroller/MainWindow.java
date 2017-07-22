package uicontroller;

import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.effects.JFXDepthManager;
import internal.CustomLogger;
import internal.Scheduler;
import internal.TorrentMeta;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Tab;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import peer.PeerController;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by ps on 15/6/17.
 */
public class MainWindow extends Application implements Initializable{

    public HBox menubar;
    public JFXListView list;
    JFXDepthManager manager;
    private Scheduler scheduler;
    private Logger logger;
    Stage primary;
    Tab tab;
    public void start(Stage primaryStage) throws Exception {
        primary=primaryStage;
        FXMLLoader loader=new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        Parent parent=loader.load();
        Scene scene=new Scene(parent,900,500);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void initialize(URL location, ResourceBundle resources) {
        logger=CustomLogger.getInstance();
        list.setCellFactory(new Callback<ListView<Download>, ListCell<Download>>() {
            public ListCell<Download> call(ListView<Download> param) {
                return new CustomListViewCell();
            }
        });
        manager=new JFXDepthManager();
        manager.setDepth(list,2);

        scheduler=new Scheduler();
        ObservableList<Download> downloads= FXCollections.observableArrayList();
        downloads.add(new Download("Linux mint 2.0.1.iso",1.0));
        downloads.add(new Download("Backtrack 5.0.3.iso",0.0));
        list.setItems(downloads);
    }


    public void onAddButtonClicked(){

        FileChooser fileChooser=new FileChooser();
        fileChooser.setTitle("Choose torrent file");
        FileChooser.ExtensionFilter extensionFilter=new FileChooser.ExtensionFilter("torrent files (*.torrent)","*.torrent");
        fileChooser.getExtensionFilters().add(extensionFilter);
        File file=fileChooser.showOpenDialog(primary);
        System.out.println(file.toString());
        //startNewTorrentSession(file.toString());
    }
    private void startNewTorrentSession(String filename){
        if(scheduler.sessionExists(filename)){
            System.out.println("Session already exits.");
            logger.debug("session already exists");
            //show a dialog box or some message box.
        }else{
            boolean result=scheduler.schedule(filename);
            if(!result){
                String errorString=scheduler.getErrorString();
                System.out.println("Error ocurred while scheduling :"+errorString);
                //show errorString in some dialog box.
            }else{
                PeerController controller=scheduler.getThisPeerController();
                initializeUIElements(controller.getTorrentMeta(),controller);
            }
        }

    }
    /*
     call methods for individual elements.
     */
    private void initializeUIElements(TorrentMeta meta,PeerController controller){
        addToListView(meta);
        initializeTabPane(meta,controller);
    }
    /*
     Add given download to the list view.
     initialize ProgressBar,labels etc.
     */
    private void addToListView(TorrentMeta meta){

    }

    /*
      Initializes the tabpane.
      setup individual tabs.
     */
    private void initializeTabPane(TorrentMeta meta,PeerController controller){
        setupStatusTab(meta);
        setupDetailsTab(meta);
        setupFilesTab(meta);
        setupTrackerTab(meta);
        setupPeersTab(meta,controller);
    }
    private void setupDetailsTab(TorrentMeta meta){

    }
    private void setupFilesTab(TorrentMeta meta){

    }
    private void setupPeersTab(TorrentMeta meta,PeerController controller){

    }
    private void setupTrackerTab(TorrentMeta meta){

    }
    private void setupStatusTab(TorrentMeta meta){

    }

}


class CustomListViewCell extends JFXListCell<Download>{
    @FXML
    public JFXProgressBar  progress;
    @FXML
    public Label filename;
    @FXML
    public Label status;
    @FXML
    public VBox customcell;

    FXMLLoader fxmlLoader;
    @Override
    public void updateIndex(int i) {
        super.updateIndex(i);

        if(i==-1){
            setGraphic(null);
        }
    }

    @Override
    public void updateItem(Download item, boolean empty) {
        super.updateItem(item, empty);
        if(item==null){
            setText(null);
            setGraphic(null);
        }
        else{
            if(fxmlLoader==null){
                fxmlLoader=new FXMLLoader(getClass().getResource("/customlistcell.fxml"));
                fxmlLoader.setController(this);

                try{
                    fxmlLoader.load();
                }catch (IOException e){
                    System.out.println("Error loading fxml loader.");
                }
            }
            filename.setText(item.filename);
            status.setText("Completed");

            setText(null);
            setGraphic(customcell);
        }
    }
}


class Download{
    String filename;
    double progress;

    Download(String filename,double progress){
        this.filename=filename;
        this.progress=progress;
    }
}