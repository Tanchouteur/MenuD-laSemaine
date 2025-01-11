package fr.tanchou.menudlasemaine.menu;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a weekly menu containing meals for each day and each time of the day.
 * This class uses an immutable structure (record) to encapsulate a list of meals.
 *
 * @param listRepas A two-dimensional array representing the meals for the week.
 *                  Each row corresponds to a day of the week, and each column to a specific time of the day.
 *                  By convention, the first column is for lunch, and the second for dinner.
 */
public record Menu(Repas[][] listRepas) {

    /**
     * Retrieves a specific meal based on the day and time.
     *
     * @param jour   The index of the day (0 = Monday, 1 = Tuesday, etc.).
     * @param moment The index of the time (0 = lunch, 1 = dinner).
     * @return The meal corresponding to the specified day and time.
     */
    public Repas getRepas(int jour, int moment) {
        return listRepas[jour][moment];
    }

    public List<Produits> getProductsUsed() {
        List<Produits> productsUsed = new ArrayList<>();

        for (Repas[] repas : listRepas) {
            for (Repas r : repas) {
                if (r.entree() != null) {
                    productsUsed.add(r.entree());
                }
                productsUsed.addAll(r.plat().getProductPlat());
            }
        }

        return productsUsed;
    }

    /**
     * Generates a string representation of the weekly menu.
     * Displays the meals for each day of the week, including appetizers and main courses.
     *
     * @return A string representing the weekly menu.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String[] joursSemaine = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

        sb.append("----- Weekly Menu -----\n");

        for (int i = 0; i < listRepas.length; i++) {
            sb.append("\n").append(joursSemaine[i]).append(":\n");
            sb.append("-------------------------\n");

            for (int j = 0; j < listRepas[i].length; j++) {
                Repas repas = listRepas[i][j];
                sb.append((j == 0 ? "Lunch: " : "Dinner: "));

                if (repas.entree() != null) {
                    sb.append(repas.entree().getNomProduit()).append(" - ");
                }
                sb.append(repas.plat().getNomPlat()).append("\n");
            }
        }

        sb.append("\n------------------------------");

        return sb.toString(); // Returns the constructed string
    }
}
