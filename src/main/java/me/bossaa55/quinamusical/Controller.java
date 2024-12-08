package me.bossaa55.quinamusical;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.image.ImageView;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javafx.stage.Stage;
import javafx.util.Duration;

import java.text.Normalizer;
import java.util.*;
import java.util.List;
import java.nio.file.Paths;
import java.util.regex.Pattern;

public class Controller implements Initializable {

    @FXML
    private HBox infoContainer;

    @FXML
    private Label etiNomCanco;

    @FXML
    private ImageView pauseButton;

    @FXML
    private ImageView reiniciarButton;

    @FXML
    private Button btPartidaNova;

    @FXML
    private Button btTreureCanco;

    @FXML
    private VBox songsVBox;

    @FXML
    private Label scrollbarItem;

    @FXML
    private TextField songsTextField;

    @FXML
    private ScrollPane scrollPane;

    private Stage stage;

    private final String musicDirectory= Paths.get("").toAbsolutePath() +"/music";
    //Conte el nom, temps de tornada i fitxer de cada canço
    private final ArrayList<Musica> musicInfo = new ArrayList<>();

    //Guarda el nombre de cançons que s'han tret
    private int nCanconsTretes=0;

    private MediaPlayer mediaPlayer;
    private Media media;

    //Saber si la música s'està reproduint.
    private boolean musicIsPlaying=false;
    //Guarda l'index de la canço que s'està reproduint.
    private int playing=0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        //LLegir el fitxer de música
        File musicInfoFile = new File(musicDirectory+"/info.txt");
        try{
            //El fitxer xisteix
            if(musicInfoFile.exists()){
                Scanner llegir = new Scanner(musicInfoFile);
                //Llegir el contingut del fitxer
                int nLinies=0;
                while (llegir.hasNext()){
                    String linia = llegir.nextLine();
                    String[] info = linia.split(";");
                    if(info.length==2) {
                        int duracio = timeToSeconds(info[1]);
                        if(duracio>-1) {
                            musicInfo.add(new Musica(info[0].trim(), duracio));
                            nLinies++;
                        }else{
                            //El format del fixer no és correcte
                            raiseAlert(Alert.AlertType.ERROR, "Error",
                                    "El format del fitxer no és correcte\n" +
                                            "Linia "+nLinies+": "+linia);
                            Platform.exit();
                        }
                    }else{
                        //El format del fixer no és correcte
                        raiseAlert(Alert.AlertType.ERROR, "Error",
                                "El format del fitxer no és correcte\n" +
                                        "Linia "+nLinies+": "+linia);
                        Platform.exit();
                    }
                }

                //Buscar en el directori tots els fitxers que hi ha escrits a info.txt
                int nArxiusTrobats=0;
                ArrayList<String> fitxersNoTrobats = new ArrayList<>();
                for (int i = 0; i < musicInfo.size(); i++) {
                    File f = new File(musicDirectory+"/"+musicInfo.get(i).getNom());
                    if(f.exists()){
                        nArxiusTrobats++;
                        musicInfo.get(i).setFile(f);
                    }
                    else {
                        //Si no es troba el fitxer s'afegeix a la llista de fitxers no trobats
                        fitxersNoTrobats.add(musicInfo.get(i).getNom());
                        musicInfo.remove(i);
                        i--;
                    }
                }

                //Si hi ha fitxers que no s'han trobat, informar a l'usuari.
                if(!fitxersNoTrobats.isEmpty()){
                    StringBuilder sortida= new StringBuilder("No s'han trobat els següents fitxers:");
                    for(String s : fitxersNoTrobats){
                        sortida.append("\n").append(s);
                    }
                    //Mostrar la informació
                    raiseAlert(Alert.AlertType.WARNING, "Lost Media",sortida.toString());
                }

            }else{
                //No s'ha trobat el fitxer info.txt, informa i tanca.
                raiseAlert(Alert.AlertType.ERROR, "Error","No s'ha trobat el fitxer: "+musicInfoFile.getAbsolutePath());
                Platform.exit();
            }
        }catch (FileNotFoundException ignored){
        }

        //Posar els elements visuals a valors inicials
        etiNomCanco.setVisible(false);
        songsVBox.getChildren().remove(scrollbarItem);
        pauseButton.setVisible(false);
        reiniciarButton.setVisible(false);
        btPartidaNova.setDisable(true);
    }

    //Crea un Alert amb les propietats especificades, el mostra i espera.
    private Optional<ButtonType> raiseAlert(Alert.AlertType alertType, String title, String content){
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    //Retorna format 00:00 a segons. Exemple: 1:20 -> 80
    private int timeToSeconds(String time){
        String[] separat=time.split(":");
        try{
            if(separat.length==2){
                int min=Integer.parseInt(separat[0]);
                int sec=Integer.parseInt(separat[1]);
                return min*60+sec;
            }else if(separat.length==1){
                return Integer.parseInt(separat[0]);
            }
            return 0;
        }catch (NumberFormatException e){
            return -1;
        }
    }

    //Possar la canço de l'index
    private void setMedia(int index){
        //Parar el reproductor.
        if(mediaPlayer!=null) mediaPlayer.stop();
        musicIsPlaying=false;
        //Declarar un media amb la nova canço.
        media=new Media(musicInfo.get(index).getFile().toURI().toString());
        //Declarar el MediaPlayr amb el nou media.
        mediaPlayer = new MediaPlayer(media);
        //Esperar a que el Media Player s'hagi inicialitzat abans de fer el seek.
        mediaPlayer.statusProperty().addListener((observable, oldStatus, newStatus) -> {
            if (newStatus == MediaPlayer.Status.READY) {
                mediaPlayer.seek(Duration.seconds(musicInfo.get(index).getInici())); //Moure el punter
                //Reproduir canço.
                playFadeIn();
            }
        });
        //Actualitzar l'índex de la canço que s'està reproduint.
        playing=index;
        etiNomCanco.setVisible(true);
        etiNomCanco.setText(musicInfo.get(index).getNom().substring(0,musicInfo.get(index).getNom().lastIndexOf(".")));
    }

    //Genera una canço nova.
    @FXML
    void cancoNova(ActionEvent event) {
        if(nCanconsTretes<musicInfo.size()){
            Random r = new Random();
            int indx=r.nextInt(musicInfo.size());
            while(musicInfo.get(indx).getHaSortit())indx=r.nextInt(musicInfo.size());
            setMedia(indx);
            addItemToList(indx);
            musicInfo.get(indx).setHaSortit(true);
            nCanconsTretes++;
            pauseButton.setVisible(true);
            reiniciarButton.setVisible(true);
            if(nCanconsTretes==musicInfo.size())btTreureCanco.setDisable(true);
            btPartidaNova.setDisable(false);
            //Natejar el quadre de cerca del llistat de cançons.
            songsTextField.setText("");
            searchSong();
        }
    }

    @FXML
    void pausarCanco(MouseEvent event) { //Pausa la canço
        if(musicIsPlaying){
            musicIsPlaying=false;
            mediaPlayer.pause();
            pauseButton.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/me/bossaa55/quinamusical/images/play.png"))));
        }else{
            if(mediaPlayer!=null){
                playFadeIn();
            }
        }
    }

    @FXML
    void partidaNova(ActionEvent event) { //Reinicia tots els valors per començar una partida nova.
        Optional<ButtonType> result= raiseAlert(Alert.AlertType.CONFIRMATION, "Partida Nova",
                "Segur que vols començar una partida nova? " +
                        "Tots els valors es posen per defecte, no es pot desfer.");
        if(result.get() == ButtonType.OK) {
            mediaPlayer.stop();
            musicIsPlaying = false;
            mediaPlayer = null;
            etiNomCanco.setVisible(false);
            pauseButton.setVisible(false);
            reiniciarButton.setVisible(false);
            btTreureCanco.setDisable(false);
            btPartidaNova.setDisable(true);
            songsVBox.getChildren().removeAll(songList);
            songList.clear();
            songsTextField.setText("");
        }
    }

    @FXML
    void reiniciarCanco() { //Posa la canço des de la tornada
        Duration d = Duration.seconds(musicInfo.get(playing).getInici());
        mediaPlayer.seek(d);
        playFadeIn();
    }

    List<Label> songList= new ArrayList<>();

    private void addItemToList(int index){
        String nom = musicInfo.get(index).getNom();
        Label label = new Label((songList.size()+1)+". "+nom.substring(0,nom.lastIndexOf(".")));
        label.getStyleClass().addAll(scrollbarItem.getStyleClass());
        final int i=index;
        label.setOnMouseClicked(event ->{
            setMedia(i);
        });
        songList.add(0,label);
        songsVBox.getChildren().add(0,label);
    }

    @FXML
    void searchSong() {
        songsVBox.getChildren().removeAll(songList);
        for(Label l : songList){
            if(stringContains(l.getText(),songsTextField.getText())){
                songsVBox.getChildren().add(l);
            }
        }
    }

    private boolean stringContains(String string, String sequence){
        String normalized1 = normalize(string);
        String normalized2 = normalize(sequence);
        for (int i = 0; i < string.length()-sequence.length(); i++) {
            if(normalized1.substring(i,i+sequence.length()).equalsIgnoreCase(normalized2))return true;
        }
        return false;
    }

    //Normalitza un string treient accents dels caràcters
    private static String normalize(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("").toLowerCase();
    }

    private void playFadeIn() {
        musicIsPlaying=true;
        pauseButton.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/me/bossaa55/quinamusical/images/pause.png"))));
        mediaPlayer.setVolume(0);
        mediaPlayer.play();
        Timeline fadeInTimeline = new Timeline();
        KeyFrame keyFrame = new KeyFrame(Duration.millis(50), event -> {
            if (mediaPlayer.getVolume() < 1) {
                mediaPlayer.setVolume(mediaPlayer.getVolume() + 0.1);
            } else {
                mediaPlayer.setVolume(1);
                fadeInTimeline.stop();
            }
        });
        fadeInTimeline.getKeyFrames().add(keyFrame);
        fadeInTimeline.setCycleCount(Timeline.INDEFINITE);
        fadeInTimeline.play();
    }

    @FXML
    void openInfo(MouseEvent event) {
        infoContainer.setVisible(true);
    }

    @FXML
    void closeInfo(ActionEvent event) {
        infoContainer.setVisible(false);
    }

    @FXML
    void obrirDocumentacio(ActionEvent event) {
        openWebpage("https://github.com/Bossaa55/QuinaMusical");
    }

    @FXML
    void obrirGithub(ActionEvent event) {
        openWebpage("https://github.com/Bossaa55");
    }

    @FXML
    void obrirLinkedin(ActionEvent event) {
        openWebpage("https://www.linkedin.com/in/jordi-bossacoma-frigola-9b2054224/");
    }

    private void openWebpage(String urlString) {
        try {
            Desktop.getDesktop().browse(new URI(urlString));
        } catch (IOException | URISyntaxException ignored) {
        }
    }
}