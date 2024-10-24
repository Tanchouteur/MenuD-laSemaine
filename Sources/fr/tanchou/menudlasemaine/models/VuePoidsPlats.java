package fr.tanchou.menudlasemaine.models;

public class VuePoidsPlats {
    private final int platId;
    private final String nomPlat;
    private final int poidsFamille;
    private final int poidsSaison;
    private final int poidsMomentJournee;
    private final int poidsMomentSemaine;

    public VuePoidsPlats(int platId, String nomPlat, int poidsFamille,
                         int poidsSaison, int poidsMomentJournee, int poidsMomentSemaine) {
        this.platId = platId;
        this.nomPlat = nomPlat;
        this.poidsFamille = poidsFamille;
        this.poidsSaison = poidsSaison;
        this.poidsMomentJournee = poidsMomentJournee;
        this.poidsMomentSemaine = poidsMomentSemaine;
    }

    // Getters et Setters
    public int getPlatId() {
        return platId;
    }

    public String getNomPlat() {
        return nomPlat;
    }

    public int getPoidsFamille() {
        return poidsFamille;
    }

    public int getPoidsSaison() {
        return poidsSaison;
    }

    public int getPoidsMomentJournee() {
        return poidsMomentJournee;
    }

    public int getPoidsMomentSemaine() {
        return poidsMomentSemaine;
    }
}
