package fr.tanchou.menudlasemaine.models;

public class Accompagnement {
    private Legume legume;
    private Feculent feculent;

    public Accompagnement(Legume legume, Feculent feculent) {
        this.legume = legume;
        this.feculent = feculent;
    }

    public Accompagnement(Feculent feculent) {
        this.legume = null;
        this.feculent = feculent;
    }
    public Accompagnement(Legume legume) {
        this.legume = null;
        this.feculent = feculent;
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

    public String getNomAccompagnement() {
        String nomAccompagnement = "";
        if (legume != null) {
            nomAccompagnement += legume.getLegumeNom();
        }
        if (feculent != null) {
            nomAccompagnement += " " + feculent.getFeculentName();
        }
        return nomAccompagnement;
    }
}