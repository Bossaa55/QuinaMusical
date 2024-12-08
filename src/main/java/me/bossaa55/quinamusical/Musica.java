package me.bossaa55.quinamusical;

import java.io.File;

public class Musica {
    private String nom;
    private int inici;
    private File file;
    private boolean haSortit=false;

    public Musica(String nomP, int iniciP){
        this.nom=nomP; this.inici=iniciP;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public int getInici() {
        return inici;
    }

    public void setInici(int inici) {
        this.inici = inici;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean getHaSortit() {
        return haSortit;
    }

    public void setHaSortit(boolean haSortit) {
        this.haSortit = haSortit;
    }
}
