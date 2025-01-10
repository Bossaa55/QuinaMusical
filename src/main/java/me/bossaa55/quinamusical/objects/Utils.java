package me.bossaa55.quinamusical.objects;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class Utils {
    public static Optional<ButtonType> raiseAlert(Alert.AlertType alertType, String title, String content){
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait();
    }

    public static String formatarSegons(int segons){
        return String.format("%02d:%02d", segons/60, segons % 60);
    }

    //Retorna format 00:00 a segons. Exemple: 1:20 -> 80
    public static int timeToSeconds(String time){
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
}
