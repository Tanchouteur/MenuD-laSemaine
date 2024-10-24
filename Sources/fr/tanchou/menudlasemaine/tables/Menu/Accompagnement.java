package fr.tanchou.menudlasemaine.tables.Menu;

public class Accompagnement {
    private int accompagnementId;
    private Legume legume;
    private Feculent feculent;

    public Accompagnement(int accompagnementId, Legume legume, Feculent feculent) {
        this.accompagnementId = accompagnementId;
        this.legume = legume;
        this.feculent = feculent;
    }

    public int getAccompagnementId() {
        return accompagnementId;
    }

    public void setAccompagnementId(int accompagnementId) {
        this.accompagnementId = accompagnementId;
    }

    public Legume getLegume() {
        return legume;
    }

    public void setLegume(Legume legume) {
        this.legume = legume;
    }

    public Feculent getFeculent() {
        return feculent;
    }

    public void setFeculent(Feculent feculent) {
        this.feculent = feculent;
    }
}
