package me.bossaa55.quinamusical;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import me.bossaa55.quinamusical.objects.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class ControllerQuinesManager implements Initializable {

    private final String ROOT_DIR = "quines";

    @FXML
    private VBox containerCancons;

    @FXML
    private VBox containerInfoQuina;

    @FXML
    private AnchorPane containerItemCancons;

    @FXML
    private AnchorPane root;

    @FXML
    private AnchorPane containerItemQuines;

    @FXML
    private Label labelInfoQuina;

    @FXML
    private Label labelItemCancons;

    @FXML
    private Label labelItemQuines;

    @FXML
    private Label labelTitolQuina;

    @FXML
    private VBox containerQuines;

    @FXML
    void crearQuina(ActionEvent event) {
        try {
            // Carregar la pantalla
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CrearQuina.fxml"));
            Parent quinaScreen = loader.load();

            // Carregar el controlador de la nova pantalla
            ControllerQuinaNova quinaController = loader.getController();
            // Passar la quina a obrir
            quinaController.iniciar(null);

            // Obrir la pantalla
            Stage stage = new Stage();
            stage.setScene(new Scene(
                    quinaScreen,
                    1000,800));
            stage.setTitle("Crear Quina");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/me/bossaa55/quinamusical/images/icon.png"))));
            stage.setOnCloseRequest(e ->{
                Optional<ButtonType> result = Utils.raiseAlert(Alert.AlertType.CONFIRMATION, "Tancar",
                        "Segur que vol tancar? Els canvis no es guardaran.");
                if(result.get() == ButtonType.OK){
                    stage.close();
                }else e.consume();
            });
            stage.showAndWait();
            updateQuines();
        }catch (IOException e){e.printStackTrace();}
    }

    @FXML
    void editarQuina(ActionEvent event) {
        try {
            // Carregar la pantalla
            FXMLLoader loader = new FXMLLoader(getClass().getResource("CrearQuina.fxml"));
            Parent quinaScreen = loader.load();

            // Carregar el controlador de la nova pantalla
            ControllerQuinaNova quinaController = loader.getController();
            // Passar la quina a obrir
            quinaController.iniciar(quinaSeleccionada);

            // Obrir la pantalla
            Stage stage = new Stage();
            stage.setScene(new Scene(
                    quinaScreen,
                    1000,800));
            stage.setTitle("Editar Quina");
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/me/bossaa55/quinamusical/images/icon.png"))));
            stage.setOnCloseRequest(e ->{
                if(!ControllerQuinaNova.isCanvisGuardats()) {
                    Optional<ButtonType> result = Utils.raiseAlert(Alert.AlertType.CONFIRMATION, "Tancar",
                            "Segur que vol tancar? Els canvis no es guardaran.");
                    if (result.get() == ButtonType.OK) {
                        stage.close();
                    } else e.consume();
                }
            });
            stage.showAndWait();
            updateQuines();
        }catch (IOException e){e.printStackTrace();}
    }

    @FXML
    void obrirQuina() {
        if(quinaSeleccionada!=null){
            try {
                // Carregar la pantalla
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Player.fxml"));
                Parent quinaScreen = loader.load();

                // Carregar el controlador de la nova pantalla
                ControllerPlayer quinaController = loader.getController();
                // Passar la quina a obrir
                quinaController.setQuina(quinaSeleccionada);

                // Obrir la pantalla
                Stage stage = new Stage();
                stage.setScene(new Scene(
                        quinaScreen,
                        Screen.getPrimary().getVisualBounds().getWidth()*0.9,
                        Screen.getPrimary().getVisualBounds().getHeight()*0.9));
                stage.setMaximized(true);
                stage.setTitle("Quina Musical - "+quinaSeleccionada.getName());
                stage.getIcons().add(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/me/bossaa55/quinamusical/images/icon.png"))));
                stage.show();
                Stage currentStage = (Stage) root.getScene().getWindow();
                currentStage.close();
            }catch (IOException e){}
        }
    }

    private ArrayList<File> quines = new ArrayList<>();
    private File quinaSeleccionada;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //Posar els elements per defecte
        containerItemQuines.getChildren().clear();
        containerItemCancons.getChildren().clear();
        containerInfoQuina.setVisible(false);
        labelInfoQuina.setVisible(true);

        updateQuines();
    }

    private void updateQuines(){
        quines.clear();
        //Llegir les quines que hi ha guardades
        File root = new File(ROOT_DIR);
        if(root.exists()){
            if(root.isDirectory()){
                //Llistar el contingut del directori
                File[] items=root.listFiles();
                assert items != null;
                for(File item : items){
                    //Si és un directori l'afegim a quines
                    if(item.isDirectory())quines.add(item);
                }
            }
        }

        //Posar les quines a la pantalla
        containerQuines.getChildren().clear();
        for(File quina: quines){
            AnchorPane itemContainer = new AnchorPane();
            Label label = new Label(quina.getName());
            label.getStyleClass().addAll(labelItemQuines.getStyleClass());
            itemContainer.getChildren().add(label);
            AnchorPane.setLeftAnchor(label, AnchorPane.getLeftAnchor(labelItemQuines));
            AnchorPane.setRightAnchor(label, AnchorPane.getRightAnchor(labelItemQuines));

            itemContainer.setOnMouseClicked(event ->{
                obrirQuina(quina);
            });

            containerQuines.getChildren().add(itemContainer);
        }
    }

    private void obrirQuina(File quina){
        //Validar l'estructura de la quina
        File musicDir = new File(quina, "music");
        File infoFile = new File(quina, "info.csv");
        ArrayList<String> cancons = new ArrayList<>();
        if(musicDir.exists()){
            if(musicDir.isDirectory()){
                if(infoFile.exists()){
                    if(infoFile.isFile()){
                        quinaSeleccionada=quina;
                        try {
                            Scanner read = new Scanner(infoFile);
                            while(read.hasNextLine()){
                                String[] linia = read.nextLine().split(";");
                                cancons.add(linia[0]);
                            }
                            read.close();
                        } catch (FileNotFoundException e) {}

                        //Mostrar les cançons per pantalla
                        containerCancons.getChildren().clear();
                        for(String c : cancons){
                            AnchorPane itemContainer = new AnchorPane();
                            Label label = new Label(c.substring(0,c.lastIndexOf(".")));
                            label.getStyleClass().addAll(labelItemCancons.getStyleClass());
                            itemContainer.getChildren().add(label);
                            AnchorPane.setLeftAnchor(label, AnchorPane.getLeftAnchor(labelItemCancons));
                            AnchorPane.setRightAnchor(label, AnchorPane.getRightAnchor(labelItemCancons));
                            containerCancons.getChildren().add(itemContainer);
                        }

                        labelTitolQuina.setText(quina.getName());

                        labelInfoQuina.setVisible(false);
                        containerInfoQuina.setVisible(true);
                    } else Utils.raiseAlert(Alert.AlertType.ERROR, "L'estructura de la quina no és vàlida", "'info.csv' hauria de ser un fitxer");
                } else Utils.raiseAlert(Alert.AlertType.ERROR, "L'estructura de la quina no és vàlida", "El fitxer 'info.csv' no existeix");
            } else Utils.raiseAlert(Alert.AlertType.ERROR, "L'estructura de la quina no és vàlida", "'music' hauria de ser un directori");
        } else Utils.raiseAlert(Alert.AlertType.ERROR, "L'estructura de la quina no és vàlida", "El directori 'music' no existeix");
    }
}
