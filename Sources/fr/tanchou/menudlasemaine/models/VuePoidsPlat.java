package fr.tanchou.menudlasemaine.models;

public class VuePoidsPlat {
    private int platId;                 // Identifiant du plat
    private String nomPlat;             // Nom du plat
    private double poidsFamille;        // Poids en fonction de la famille
    private double poidsSaison;         // Poids en fonction de la saison
    private double poidsMomentJournee;  // Poids en fonction du moment de la journée
    private double poidsMomentSemaine;  // Poids en fonction du moment de la semaine

    // Constructeur
    public VuePoidsPlat(int platId, String nomPlat, double poidsFamille, double poidsSaison, double poidsMomentJournee, double poidsMomentSemaine) {
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

    public void setPlatId(int platId) {
        this.platId = platId;
    }

    public String getNomPlat() {
        return nomPlat;
    }

    public void setNomPlat(String nomPlat) {
        this.nomPlat = nomPlat;
    }

    public double getPoidsFamille() {
        return poidsFamille;
    }

    public void setPoidsFamille(double poidsFamille) {
        this.poidsFamille = poidsFamille;
    }

    public double getPoidsSaison() {
        return poidsSaison;
    }

    public void setPoidsSaison(double poidsSaison) {
        this.poidsSaison = poidsSaison;
    }

    public double getPoidsMomentJournee() {
        return poidsMomentJournee;
    }

    public void setPoidsMomentJournee(double poidsMomentJournee) {
        this.poidsMomentJournee = poidsMomentJournee;
    }

    public double getPoidsMomentSemaine() {
        return poidsMomentSemaine;
    }

    public void setPoidsMomentSemaine(double poidsMomentSemaine) {
        this.poidsMomentSemaine = poidsMomentSemaine;
    }

    @Override
    public String toString() {
        return "VuePoidsPlat{" +
                "platId=" + platId +
                ", nomPlat='" + nomPlat + '\'' +
                ", poidsFamille=" + poidsFamille +
                ", poidsSaison=" + poidsSaison +
                ", poidsMomentJournee=" + poidsMomentJournee +
                ", poidsMomentSemaine=" + poidsMomentSemaine +
                '}';
    }
}
