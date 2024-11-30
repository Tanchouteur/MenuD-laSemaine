package fr.tanchou.menudlasemaine.menu;

public class Menu {
    private final Repas[][] listRepas;

    public Menu(Repas[][] listRepas) {
        this.listRepas = listRepas;
    }

    public Repas[][] getListRepas() {
        return listRepas;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String[] joursSemaine = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

        sb.append("----- Menu de la Semaine -----\n");

        for (int i = 0; i < listRepas.length; i++) {
            sb.append("\n").append(joursSemaine[i]).append(" :\n");
            sb.append("-------------------------\n");

            for (int j = 0; j < listRepas[i].length; j++) {
                Repas repas = listRepas[i][j];
                sb.append((j == 0 ? "Midi : " : "Soir : "));

                if (repas.getEntree() != null) {
                    sb.append(repas.getEntree().getNomProduit()).append(" - ");
                }
                sb.append(repas.getPlat().getNomPlat()).append("\n");
            }
        }

        sb.append("\n------------------------------");

        return sb.toString(); // Retourne la chaîne construite
    }
}
