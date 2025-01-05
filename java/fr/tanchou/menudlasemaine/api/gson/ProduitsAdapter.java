package fr.tanchou.menudlasemaine.api.gson;

import com.google.gson.*;
import fr.tanchou.menudlasemaine.enums.TypeProduit;
import fr.tanchou.menudlasemaine.menu.Produits;

import java.lang.reflect.Type;

/**
 * Custom deserializer for the Produits class using Gson.
 * This class is responsible for converting a JSON representation of a Produit object into an instance of the Produits class.
 */
public class ProduitsAdapter implements JsonDeserializer<Produits> {

    /**
     * Deserializes the provided JSON element into an instance of the Produits class.
     * The method extracts necessary properties like `nomProduit`, `poidsArbitraire`, `typeProduit`, `poidsMoment`, and `poidsSaison` from the JSON object
     * and uses them to construct a new Produits object.
     *
     * @param jsonElement The JSON element that needs to be deserialized.
     * @param type        The type of the object that is being deserialized (for Produits, this will be Produits.class).
     * @param jsonDeserializationContext Provides methods for deserializing other objects.
     * @return A new instance of Produits based on the provided JSON data.
     * @throws JsonParseException If there is an error during the deserialization process.
     */
    @Override
    public Produits deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        // Convert the JSON element into a JsonObject for easy extraction of properties
        JsonObject jsonObject = jsonElement.getAsJsonObject();

        // Extract properties from the JSON object
        String nomProduit = jsonObject.get("nomProduit").getAsString();
        int poidsArbitraire = jsonObject.get("poidsArbitraire").getAsInt();
        TypeProduit typeProduit = TypeProduit.valueOf(jsonObject.get("typeProduit").getAsString().toUpperCase());
        int[] poidsMoment = jsonDeserializationContext.deserialize(jsonObject.get("poidsMoment"), int[].class);
        int[] poidsSaison = jsonDeserializationContext.deserialize(jsonObject.get("poidsSaison"), int[].class);

        // Return a new Produits object initialized with the extracted values
        return new Produits(nomProduit, poidsArbitraire, typeProduit, poidsMoment, poidsSaison);
    }
}