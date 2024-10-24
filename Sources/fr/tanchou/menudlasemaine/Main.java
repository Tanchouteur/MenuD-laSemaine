package fr.tanchou.menudlasemaine;

import fr.tanchou.menudlasemaine.utils.PlatComposeFactory;

public class Main {
    public static void main(String[] args) {
        ModelPrincipal modelPrincipal = new ModelPrincipal();

        System.out.println(PlatComposeFactory.getRandomPlatCompose(100));
    }
}
