module me.bossaa55.quinamusical {
    requires javafx.controls;
    requires javafx.fxml;


    opens me.bossaa55.quinamusical to javafx.fxml;
    exports me.bossaa55.quinamusical;
}