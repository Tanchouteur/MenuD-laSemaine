package fr.tanchou.menudlasemaine.utils;

import fr.tanchou.menudlasemaine.models.Menu;
import fr.tanchou.menudlasemaine.models.Repas;

public class MenuService {

    public static void printListeMenu(Menu menu) {
        String[] joursSemaine = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};

        System.out.println("----- Menu de la Semaine -----");

        for (int i = 0; i < menu.getRepasParJour().length; i++) {
            System.out.println("\n" + joursSemaine[i] + " :");
            System.out.println("-------------------------");

            for (int j = 0; j < menu.getRepasParJour()[i].length; j++) {
                Repas repas = menu.getRepasParJour()[i][j];
                System.out.println((j == 0 ? "Midi : " : "Soir : "));
                if (repas.getEntree() != null){
                   System.out.print(repas.getEntree().getNomEntree());
                }
                System.out.println(" - " + repas.getPlat().getNomPlat());
            }
        }

        System.out.println("\n------------------------------");
    }


    public static Menu buildMenu() {

        String[] jours = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        String[] moments = {"midi", "soir"};

        Repas[][] repasParJour = new Repas[jours.length][moments.length];

        for (int i = 0; i < jours.length; i++) {
            for (int j = 0; j < moments.length; j++) {
                repasParJour[i][j] = RepasBuilder.buildRepa();
            }
        }

        return new Menu(repasParJour);
    }

    public static void printTableMenu(Menu menu) {
        String[] joursSemaine = {"Lundi", "Mardi", "Mercredi", "Jeudi", "Vendredi", "Samedi", "Dimanche"};
        String[] momentsJournee = {"Midi", "Soir"};

        // Bordure supérieure du tableau
        System.out.print("+" + "-".repeat(30));
        for (int i = 0; i < joursSemaine.length; i++) {
            System.out.print("+" + "-".repeat(40)); // Largeur de 30 pour chaque jour
        }
        System.out.println("+");

        // Entêtes des jours
        System.out.printf("| %-18s ", " "); // Colonne vide pour moments de la journée
        for (String jour : joursSemaine) {
            System.out.printf("| %-28s ", jour);
        }
        System.out.println("|");

        // Bordure entre les entêtes et les repas
        System.out.print("+" + "-".repeat(40));
        for (int i = 0; i < joursSemaine.length; i++) {
            System.out.print("+" + "-".repeat(40));
        }
        System.out.println("+");

        // Affichage des repas (Midi et Soir)
        for (int m = 0; m < momentsJournee.length; m++) {
            System.out.printf("| %-18s ", momentsJournee[m]);
            for (int j = 0; j < menu.getRepasParJour().length; j++) {
                Repas repas = menu.getRepasParJour()[j][m];
                String repasText = "";


                if (repas.getEntree() != null) {
                    repasText += repas.getEntree().getNomEntree();
                }else {
                    repasText += "pas d'entrée ";
                }
                repasText += " - " + repas.getPlat().getNomPlat();

                // Limiter la longueur du texte pour ne pas déborder
                repasText = repasText.length() > 35 ? repasText.substring(0,35) + "..." : repasText;

                System.out.printf("| %-28s ", repasText);
            }
            System.out.println("|");

            // Ligne de séparation entre chaque moment de la journée
            System.out.print("+" + "-".repeat(40));
            for (int i = 0; i < joursSemaine.length; i++) {
                System.out.print("+" + "-".repeat(40));
            }
            System.out.println("+");
        }
    }

}
