package fr.tanchou.menudlasemaine.tables.Menu;

public class Feculent {
    private int feculentId;
    private String feculentName;

    public Feculent(int feculentId, String feculentName) {
        this.feculentId = feculentId;
        this.feculentName = feculentName;
    }


    public int getFeculentId() {
        return feculentId;
    }

    public void setFeculentId(int feculentId) {
        this.feculentId = feculentId;
    }

    public String getFeculentName() {
        return feculentName;
    }

    public void setFeculentName(String feculentName) {
        this.feculentName = feculentName;
    }
}
