package fr.tanchou.menudlasemaine;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.utils.generateur.AccompagnementGenerator;

public class Main {
    public static void main(String[] args) {

        //MenuService.printTableMenu(MenuService.buildMenu());

        for (int i = 0; i < 10; i++) {
            System.out.println(AccompagnementGenerator.generateAccompagnement(MomentJournee.MIDI));
        }
    }
}