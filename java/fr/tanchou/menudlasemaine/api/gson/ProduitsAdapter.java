package fr.tanchou.menudlasemaine.api.gson;

import com.google.gson.*;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;

import java.lang.reflect.Type;

public class ProduitsAdapter implements JsonDeserializer<Produits> {
    @Override
    public Produits deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        String nomProduit = jsonObject.get("nomProduit").getAsString();
        int poidsArbitraire = jsonObject.get("poidsArbitraire").getAsInt();
        TypeProduit typeProduit = TypeProduit.valueOf(jsonObject.get("typeProduit").getAsString().toUpperCase());
        int[] poidsMoment = jsonDeserializationContext.deserialize(jsonObject.get("poidsMoment"), int[].class);
        int[] poidsSaison = jsonDeserializationContext.deserialize(jsonObject.get("poidsSaison"), int[].class);

        return new Produits(nomProduit, poidsArbitraire, typeProduit, poidsMoment, poidsSaison);
    }
}

