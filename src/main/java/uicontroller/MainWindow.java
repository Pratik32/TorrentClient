package uicontroller;

import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXProgressBar;
import com.jfoenix.effects.JFXDepthManager;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Callback;

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

    public void start(Stage primaryStage) throws Exception {
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
        list.setCellFactory(new Callback<ListView<Download>, ListCell<Download>>() {
            public ListCell<Download> call(ListView<Download> param) {
                return new CustomListViewCell();
            }
        });
        manager=new JFXDepthManager();
        manager.setDepth(list,1);
        ObservableList<Download> downloads= FXCollections.observableArrayList();
        downloads.add(new Download("Linux mint 2.0.1.iso",1.0));
        downloads.add(new Download("Backtrack 5.0.3.iso",0.0));
        list.setItems(downloads);
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