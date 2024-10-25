package fr.tanchou.menudlasemaine;

import fr.tanchou.menudlasemaine.enums.MomentJournee;
import fr.tanchou.menudlasemaine.utils.AccompagnementGenerator;
import fr.tanchou.menudlasemaine.utils.MenuService;
import fr.tanchou.menudlasemaine.utils.RepasBuilder;

public class Main {
    public static void main(String[] args) {

        //MenuService.printTableMenu(MenuService.buildMenu());

        for (int i = 0; i < 10; i++) {
            AccompagnementGenerator.generateAccompagnement(MomentJournee.MIDI);
        }

    }
}
