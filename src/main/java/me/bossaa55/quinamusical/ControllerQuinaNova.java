package me.bossaa55.quinamusical;

import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.FileChooser;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.bossaa55.quinamusical.objects.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class ControllerQuinaNova {
    private final String ROOT_DIRECTORY = Paths.get("").toAbsolutePath().toString();

    @FXML
    private AnchorPane root;

    @FXML
    private Button btCancoItem;

    @FXML
    private Button btEliminarItem;

    @FXML
    private HBox ctCanconsItem;

    @FXML
    private VBox ctLlistaCancons;

    @FXML
    private VBox ctPlayer;

    @FXML
    private HBox ctLoading;

    @FXML
    private ImageView ivPlay;

    @FXML
    private Label lbNCancoItem;

    @FXML
    private Label lbStatus;

    @FXML
    private Label lbStatusDescription;

    @FXML
    private Label lbTemps;

    @FXML
    private Label lbTitle;

    @FXML
    private Slider sliderCanco;

    @FXML
    private ScrollPane spSongList;

    @FXML
    private TextField tfTitolQuina;

    private FileChooser fileChooser=new FileChooser();
    private ArrayList<String> rutaCancons = new ArrayList<>();
    private ArrayList<Integer> segonsIniciCancons = new ArrayList<>();
    private String lastOpenedDirectory = "";
    private ArrayList<SongListItem> songListItems = new ArrayList<>();

    private MediaPlayer mediaPlayer;
    private Media media;
    private Timeline playerTimeline = new Timeline();

    private int songSelected=-1;
    private boolean playing=false;
    private static boolean canvisGuardats=true;

    private File quina;

    public static boolean isCanvisGuardats(){
        return canvisGuardats;
    }


    @FXML // Guarda la quina creant un directori de la quina i guardant els fitxers dins.
    void guardarQuina(ActionEvent event) {
        if(!tfTitolQuina.getText().isEmpty()) {
            if(!rutaCancons.isEmpty()){
                lbStatus.setText("Guardant arxius...");
                ctLoading.setVisible(true);
                guardarFitxers();
                canvisGuardats=true;
                ctLoading.setVisible(false);
            }
        }
    }

    @FXML //Avisa a l'usuari si els canvis no s'han guardat i tanca la finestra.
    void tancar(ActionEvent event) {
        if(!canvisGuardats) {
            Optional<ButtonType> result = Utils.raiseAlert(Alert.AlertType.CONFIRMATION, "Tancar",
                    "Segur que vol tancar? Els canvis no es guardaran.");
            if (result.get() == ButtonType.OK) {
                Stage stage = (Stage) root.getScene().getWindow();
                stage.close();
            }
        }else{
            Stage stage = (Stage) root.getScene().getWindow();
            stage.close();
        }
    }

    @FXML //Canviar el punter del mediaPlayer quan es mou l'slider.
    void sliderReproductorRelease(){
        mediaPlayer.seek(Duration.seconds(sliderCanco.getValue()));
        lbTemps.setText(Utils.formatarSegons((int)sliderCanco.getValue()));
    }

    @FXML //Guarda el temps de la cançó actual com a punt d'inici.
    void guardarTemps(ActionEvent event) {
        segonsIniciCancons.set(songSelected, (int) sliderCanco.getValue());
        canvisGuardats=false;
    }

    @FXML //Posa play o pausa al mediaPlayer segons el necessari.
    void togglePlay(){
        if(playing){
            mediaPlayer.pause();
            playerTimeline.stop(); //Parar el playerTimeline que mou l'slider.
            ivPlay.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/me/bossaa55/quinamusical/images/play.png"))));
            playing=false;
        }else{
            mediaPlayer.play();
            playerTimeline.play();
            ivPlay.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/me/bossaa55/quinamusical/images/pause.png"))));
            playing=true;
        }
    }

    @FXML
    void reiniciarPlayer(){ //Mou el punter del mediaPlayer a l'inici de la cançó guardat.
        mediaPlayer.seek(Duration.seconds(segonsIniciCancons.get(songSelected)));
        sliderCanco.setValue(segonsIniciCancons.get(songSelected));
        lbTemps.setText(Utils.formatarSegons((int) sliderCanco.getValue()));
    }

    public void iniciar(File quinaEditar){
        quina=quinaEditar;
        //Definir els tipus de fitxers d'àudio que suporta el programa
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Tots", "*.mp3", "*.wav", "*.ogg", "*.flac"),
                new FileChooser.ExtensionFilter("MP3", "*.mp3"),
                new FileChooser.ExtensionFilter("WAV", "*.wav"),
                new FileChooser.ExtensionFilter("OGG", "*.ogg"),
                new FileChooser.ExtensionFilter("FLAC", "*.flac")
        );

        ctLoading.setVisible(false);
        //Posar el primer item a la llista de cançons
        ctLlistaCancons.getChildren().clear();
        if(quina!=null){
            lbTitle.setText("Editar Quina");
            File info = new File(quina,"info.csv");
            if(info.exists()) {
                try {
                    Scanner scanner = new Scanner(info);
                    while(scanner.hasNextLine()){
                        String[] linia = scanner.nextLine().split(";");
                        if(linia.length==2){
                            rutaCancons.add(quina.getAbsolutePath()+"/music/"+linia[0]);
                            segonsIniciCancons.add(Utils.timeToSeconds(linia[1]));
                            addItemList(linia[0]);
                        }
                    }
                    scanner.close();
                } catch (FileNotFoundException ignored) {
                }
            }
            tfTitolQuina.setText(quina.getName());
        }
        addItemList();
        KeyFrame kayFrameReproductor = new KeyFrame(Duration.millis(1000), ev ->{
            Duration currentTime = mediaPlayer.getCurrentTime();
            sliderCanco.setValue(mediaPlayer.getCurrentTime().toSeconds());
            lbTemps.setText(Utils.formatarSegons((int)currentTime.toSeconds()));
        });
        playerTimeline.getKeyFrames().add(kayFrameReproductor);
        playerTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    //Crear un item del llistat de cançons
    private void addItemList(String text){
        System.out.println(text);
        SongListItem songListItem = new SongListItem();

        songListItem.getSubContainer().setAlignment(ctCanconsItem.getAlignment());
        songListItem.getSubContainer().setPrefWidth(ctCanconsItem.getPrefWidth());
        songListItem.getSubContainer().setSpacing(ctCanconsItem.getSpacing());

        songListItem.getNumerador().setText(String.format("%02d",rutaCancons.size()+1));
        songListItem.getNumerador().setPrefWidth(lbNCancoItem.getPrefWidth());
        songListItem.getNumerador().getStyleClass().addAll(lbNCancoItem.getStyleClass());

        songListItem.getSong().setAlignment(btCancoItem.getAlignment());
        songListItem.getSong().setPrefWidth(btCancoItem.getPrefWidth());
        songListItem.getSong().getStyleClass().addAll(btCancoItem.getStyleClass());
        songListItem.getSong().setPickOnBounds(false);
        //Si s'està creant un item buit s'inicialitza amb "+", si no, es posa el nom de la canço.
        if(text.isEmpty())songListItem.getSong().setText("+");
        else {
            songListItem.getSong().setText(text);
            songListItem.setSongSelected(true);
        }
        songListItem.getSong().setOnMouseClicked(event ->{
            if(!songListItem.isSongSelected()) {
                try {
                    //Si ja s'ha obert el fileChooser s'haurà guardat la ruta de tancament
                    if (!lastOpenedDirectory.isEmpty()) {
                        //Comprovar que aquesta ruta encara existeix
                        File d = new File(lastOpenedDirectory);
                        if (d.exists()) {
                            //Existeix, per tant, canviem la ruta on s'obrirà el fileChooser
                            if (d.isDirectory()) fileChooser.setInitialDirectory(d);
                            else fileChooser.setInitialDirectory(null);
                        } else fileChooser.setInitialDirectory(null);
                    }
                    //Obrir l'explorador amb el fileChooser
                    List<File> fitxers = fileChooser.showOpenMultipleDialog(root.getScene().getWindow());
                    if(fitxers!=null) {
                        //Tractar el primer fitxer i posar-lo al botó.
                        if(!fitxers.isEmpty()) {
                            songListItem.getSong().setText(fitxers.get(0).getName());
                            rutaCancons.add(fitxers.get(0).getAbsolutePath());
                            segonsIniciCancons.add(0);
                            lastOpenedDirectory = fitxers.get(0).getParentFile().getAbsolutePath();
                            songListItem.getEliminar().setDisable(false); //Habilitar el botó d'eliminar cançó
                            songListItem.setSongSelected(true);
                            canvisGuardats = false;
                        }
                        //Recorrer la resta de fitxers i crear els items.
                        for (int i = 1; i < fitxers.size(); i++) {
                            if (fitxers.get(i).isFile()) {
                                //Guardar la ruta i afegir un nou item a la llista de cançons.
                                addItemList(fitxers.get(i).getName());
                                rutaCancons.add(fitxers.get(i).getAbsolutePath());
                                segonsIniciCancons.add(0);
                            }
                        }
                        //Obrir el reproductor a l'ultim item afegit
                        openPlayer(songListItems.get(songListItems.size()-1));
                        addItemList(); //Afegir un item buit.
                        //Fer una pausa per esperar que els items s'afegeixin abans de moure l'scrol pane a baix.
                        PauseTransition pause = new PauseTransition(Duration.millis(100));
                        pause.setOnFinished(e -> spSongList.setVvalue(1));
                        pause.play();
                    }
                } catch (Exception ignored) {
                    //L'usuari ha tancat l'explorador del fileChooser. No cal fer res.
                }
            }else{ //Ja s'ha seleccionat una cançó, per tant, cal mostrar el reproductor
                openPlayer(songListItem);
            }
        });

        songListItem.getEliminar().setText("X");
        songListItem.getEliminar().setPrefWidth(btEliminarItem.getPrefWidth());
        songListItem.getEliminar().getStyleClass().addAll(btEliminarItem.getStyleClass());
        if(text.isEmpty())songListItem.getEliminar().setDisable(true);

        songListItem.getEliminar().setOnMouseClicked(event ->{
            //Recollir l'índex de la cançó al llistat.
            int index=ctLlistaCancons.getChildren().indexOf(songListItem.getMainContainer());
            //Eliminar l'item de les llistes
            rutaCancons.remove(index);
            songListItems.remove(index);
            ctLlistaCancons.getChildren().remove(songListItem.getMainContainer());
            //Actualitzar el numerador de les cançons
            for (int i = index; i < songListItems.size(); i++) {
                songListItems.get(i).getNumerador().setText(String.format("%02d",i+1));
            }
            canvisGuardats=false;
        });
        //Afegir l'item a la llista
        ctLlistaCancons.getChildren().add(songListItem.build());
        songListItems.add(songListItem);
    }

    //Procediment per pura comoditat
    private void addItemList(){
        addItemList("");
    }

    //Obre el reproductor a l'item passat.
    // També tanca el reproductor de l'item al que està. (Només hi ha una instància de reproductor)
    private void openPlayer(SongListItem songListItem){
        int index=songListItems.indexOf(songListItem);
        if(songSelected>=0){ //Si el reproductor està en algun item s'elimina.
            songListItems.get(songSelected).getMainContainer().getChildren().remove(ctPlayer);
        }
        sliderCanco.setValue(0);
        //Declarar el mediaPlayer.
        if(mediaPlayer!=null) mediaPlayer.stop();
        mediaPlayer=null;
        File f = new File(rutaCancons.get(index));

        if(f.exists()){ //Comprovar si el fitxer encara existeix.
            media = new Media(f.toURI().toString());
            mediaPlayer=new MediaPlayer(media);
            //Esperar a que el mediaPlayer s'hagi inicialitzat.
            mediaPlayer.statusProperty().addListener((observable, oldStatus, newStatus) -> {
                System.out.println(media.getMetadata().size());
                for(String s : media.getMetadata().keySet()) System.out.println(s);
                if (newStatus == MediaPlayer.Status.READY) { //Inicialitzar el reproductor.
                    mediaPlayer.seek(Duration.seconds(segonsIniciCancons.get(songSelected)));
                    sliderCanco.setMax(mediaPlayer.getCycleDuration().toSeconds());
                    sliderCanco.setValue(segonsIniciCancons.get(index));
                    lbTemps.setText(Utils.formatarSegons(segonsIniciCancons.get(index)));
                }
            });
        }
        //Posar el reproductor a l'item.
        songListItem.getMainContainer().getChildren().add(ctPlayer);
        songSelected=index;
    }

    private void guardarFitxers(){
        File directori = new File(ROOT_DIRECTORY +"/quines/"+tfTitolQuina.getText()+"/music");
        if(quina!=null){
            if(!quina.getName().equals(tfTitolQuina.getText())){
                File nouDirectori = new File(ROOT_DIRECTORY +"/quines/"+tfTitolQuina.getText());
                quina.renameTo(nouDirectori);
            }
            File[] files = directori.listFiles();
            if(files!=null) {
                for (File f : files) {
                    if (!rutaCancons.contains(f.getAbsolutePath())) f.delete();
                }
            }
        }
        //Si els directoris no existeixen es creen
        if(!directori.exists())directori.mkdirs();
        //Recorrer les rutes de les cançons per copiar els fitxers
        for(String r : rutaCancons){
            File f1 = new File(r);
            File f2 = new File(directori,f1.getName());
            //Si les rutes són diferents s'intenta copiar el fitxer
            if(!f1.getAbsolutePath().equalsIgnoreCase(f2.getAbsolutePath())) {
                lbStatusDescription.setText(r);
                //Comprovar si el fitxer existeix (l'usuari el pot haver mogut o eliminat)
                if(f1.exists()) {
                    //Comprovar si un fitxer amb el mateix nom al directori destinatari existeix.
                    if (f2.exists()) {
                        //Existeix, per tant, preguntar a l'usuari si el vol sobreescriure.
                        Optional<ButtonType> result = Utils.raiseAlert(Alert.AlertType.CONFIRMATION, "Sobreescriure fitxer",
                                "El fitxer '" + f2.getAbsolutePath() + "' ja existeix. El vol sobreescriure?");
                        if (result.get() == ButtonType.OK) {
                            copiarFitxers(f1.toPath(), f2.toPath());
                        }
                    } else {
                        copiarFitxers(f1.toPath(), f2.toPath());
                    }
                }
            }
        }
        //Guardar la música i el temps d'inici a un fitxer csv (info.csv).
        try{
            File info = new File(ROOT_DIRECTORY +"/quines/"+tfTitolQuina.getText()+"/info.csv");
            if(!info.exists()) info.createNewFile();
            PrintStream ps = new PrintStream(info);
            for (int i = 0; i < rutaCancons.size(); i++) {
                File f = new File(rutaCancons.get(i));
                ps.println(f.getName()+";"+Utils.formatarSegons(segonsIniciCancons.get(i)));
            }
            ps.close();
        }catch (Exception ignored){
        }
    }

    //Copia un fitxer de ruta A, a ruta B.
    private void copiarFitxers(Path origen, Path desti){
        try {
            Files.copy(origen,desti);
        } catch (IOException ignored) {
        }
    }

    private class SongListItem{
        private final VBox mainContainer = new VBox();
        private final HBox subContainer = new HBox();
        private final Label numerador = new Label();
        private final Button song = new Button();
        private final Button eliminar = new Button();
        private boolean songSelected = false;

        public VBox build(){
            subContainer.getChildren().addAll(numerador,song,eliminar);
            mainContainer.getChildren().add(subContainer);
            return mainContainer;
        }

        public VBox getMainContainer() {
            return mainContainer;
        }

        public HBox getSubContainer() {
            return subContainer;
        }

        public Label getNumerador() {
            return numerador;
        }

        public Button getSong() {
            return song;
        }

        public Button getEliminar() {
            return eliminar;
        }

        public boolean isSongSelected() {
            return songSelected;
        }

        public void setSongSelected(boolean songSelected) {
            this.songSelected = songSelected;
        }
    }
}
