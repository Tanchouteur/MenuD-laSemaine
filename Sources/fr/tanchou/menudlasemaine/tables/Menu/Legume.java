package fr.tanchou.menudlasemaine.tables.Menu;

public class Legume {
    private int legumeId;
    private String legumeNom;

    public Legume(int legumeId, String legumeNom) {
        this.legumeId = legumeId;
        this.legumeNom = legumeNom;
    }

    public int getLegumeId() {
        return legumeId;
    }
    public void setLegumeId(int legumeId) {
        this.legumeId = legumeId;
    }
    public String getLegumeNom() {
        return legumeNom;
    }
    public void setLegumeNom(String legumeNom) {
        this.legumeNom = legumeNom;
    }
}
