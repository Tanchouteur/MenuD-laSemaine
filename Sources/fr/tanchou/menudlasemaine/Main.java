package fr.tanchou.menudlasemaine;

public class Main {
    public static void main(String[] args) {
        ModelPrincipal modelPrincipal = new ModelPrincipal();
        System.out.println(modelPrincipal.getAccompagnementGenerator().generateAccompagnement().getNomAccompagnement());
    }
}
