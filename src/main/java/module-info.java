module me.bossaa55.quinamusical {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.desktop;


    opens me.bossaa55.quinamusical to javafx.fxml;
    exports me.bossaa55.quinamusical;
}