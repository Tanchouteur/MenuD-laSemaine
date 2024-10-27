package fr.tanchou.menudlasemaine;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.enums.MomentSemaine;
import fr.tanchou.menudlasemaine.models.produit.PlatComplet;
import fr.tanchou.menudlasemaine.utils.generateur.*;

public class Main {
    public static void main(String[] args) {

        //MenuService.printTableMenu(MenuService.buildMenu());

        for (int i = 0; i < 1; i++) {
            System.out.println(EntreeFactory.getRandomEntree(MomentJournee.MIDI, MomentSemaine.WEEKEND));
        }
    }
}