package fr.tanchou.menudlasemaine.models;

public class Incompatibilite {
    private final int incompatibiliteId;
    private final Viande viande; // Peut être null
    private final Legume legume; // Peut être null
    private final Feculent feculent; // Peut être null

    public Incompatibilite(int incompatibiliteId, Viande viande, Legume legume, Feculent feculent) {
        this.incompatibiliteId = incompatibiliteId;
        this.viande = viande;
        this.legume = legume;
        this.feculent = feculent;
    }

    // Getters et setters
    public int getIncompatibiliteId() {
        return incompatibiliteId;
    }

    public Viande getViande() {
        return viande;
    }

    public Legume getLegume() {
        return legume;
    }

    public Feculent getFeculent() {
        return feculent;
    }
}
