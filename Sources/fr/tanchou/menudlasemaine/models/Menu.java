package fr.tanchou.menudlasemaine.models;

public class Menu {
    private Repas[][] repasParJour;

    public Menu(Repas[][] repasParJour) {
        this.repasParJour = repasParJour;
    }

    public Repas[][] getRepasParJour() {
        return repasParJour;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String[] joursSemaine = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

        sb.append("----- Menu de la Semaine -----\n");

        for (int i = 0; i < repasParJour.length; i++) {
            sb.append("\n").append(joursSemaine[i]).append(" :\n");
            sb.append("-------------------------\n");

            for (int j = 0; j < repasParJour[i].length; j++) {
                Repas repas = repasParJour[i][j];
                sb.append((j == 0 ? "Midi : " : "Soir : "));

                if (repas.getEntree() != null) {
                    sb.append(repas.getEntree().getNomEntree()).append(" - ");
                }
                sb.append(repas.getPlat().getNomPlat()).append("\n");
            }
        }

        sb.append("\n------------------------------");

        return sb.toString(); // Retourne la chaîne construite
    }
}
