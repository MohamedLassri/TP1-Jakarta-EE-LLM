package com.mohamedlassri.tp1.json;

import com.mohamedlassri.tp1.llm.LlmClientPourGemini;
import com.mohamedlassri.tp1.llm.RequeteException;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonWriter;
import jakarta.json.JsonWriterFactory;
import jakarta.json.stream.JsonGenerator;

import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Adaptateur JSON pour l'API Gemini.
 * Conserve l'historique de conversation (API sans état).
 * Une instance par backing bean (@Dependent).
 */
@Dependent
public class JsonAdapterPourGemini implements Serializable {

    @Inject
    private LlmClientPourGemini llmClient;

    /** Rôle système à envoyer au début de la conversation. */
    private String systemRole = "You are a helpful assistant.";

    /** Requête JSON cumulée (historique). Stockée en String pour rester sérialisable. */
    private String requeteJson;

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
    }

    /** Efface l'historique pour démarrer une nouvelle conversation. */
    public void reinitialiser() {
        this.requeteJson = null;
    }

    /**
     * Envoie la question au LLM avec tout l'historique.
     *
     * @param question question posée par l'utilisateur
     * @return JSON requête, JSON réponse et texte extrait
     * @throws RequeteException si l'appel échoue ou si la réponse est inattendue
     */
    public LlmInteraction envoyerRequete(String question) throws RequeteException {
        String ancienneRequete = this.requeteJson;
        try {
            if (requeteJson == null) {
                requeteJson = creerRequeteJson(systemRole, question);
            } else {
                requeteJson = ajouteQuestionDansJsonRequete(requeteJson, question);
            }
            String requeteEnvoyee = requeteJson;
            String reponseJson = llmClient.envoyerRequete(requeteEnvoyee);
            String reponseExtraite = extractReponse(reponseJson);

            // Ajoute la réponse (rôle "model") à l'historique pour la suite.
            requeteJson = ajouterMessage(requeteJson, "model", reponseExtraite);

            return new LlmInteraction(prettyPrinting(requeteEnvoyee),
                    prettyPrinting(reponseJson), reponseExtraite);
        } catch (RequeteException e) {
            this.requeteJson = ancienneRequete;
            throw e;
        } catch (JsonException e) {
            this.requeteJson = ancienneRequete;
            throw new RequeteException("Problème de format JSON : " + e.getMessage(), e);
        }
    }

    // ------------------------------------------------------------------ méthodes privées

    private JsonObject message(String role, String texte) {
        return Json.createObjectBuilder()
                .add("role", role)
                .add("parts", Json.createArrayBuilder()
                        .add(Json.createObjectBuilder().add("text", texte)))
                .build();
    }

    private JsonObject lire(String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            return reader.readObject();
        }
    }

    /** Crée le JSON de la 1ère requête : system_instruction + 1ère question. */
    private String creerRequeteJson(String role, String question) {
        JsonObject requete = Json.createObjectBuilder()
                .add("system_instruction", Json.createObjectBuilder()
                        .add("parts", Json.createArrayBuilder()
                                .add(Json.createObjectBuilder().add("text", role))))
                .add("contents", Json.createArrayBuilder().add(message("user", question)))
                .build();
        return requete.toString();
    }

    /** Ajoute une question utilisateur au tableau "contents". */
    private String ajouteQuestionDansJsonRequete(String json, String question) {
        return ajouterMessage(json, "user", question);
    }

    private String ajouterMessage(String json, String role, String texte) {
        JsonObject requete = lire(json);
        JsonArrayBuilder contents = Json.createArrayBuilder(requete.getJsonArray("contents"))
                .add(message(role, texte));
        return Json.createObjectBuilder(requete).add("contents", contents).build().toString();
    }

    /** Extrait le texte de la réponse JSON. */
    private String extractReponse(String reponseJson) throws RequeteException {
        JsonObject racine = lire(reponseJson);
        JsonArray candidates = racine.getJsonArray("candidates");
        if (candidates == null || candidates.isEmpty()) {
            throw new RequeteException("Réponse sans candidat (requête peut-être bloquée) : " + reponseJson);
        }
        JsonObject content = candidates.getJsonObject(0).getJsonObject("content");
        if (content == null || !content.containsKey("parts")) {
            throw new RequeteException("Réponse sans contenu : " + reponseJson);
        }
        StringBuilder texte = new StringBuilder();
        for (JsonObject part : content.getJsonArray("parts").getValuesAs(JsonObject.class)) {
            texte.append(part.getString("text", ""));
        }
        return texte.toString();
    }

    /** Met en forme un texte JSON pour qu'il soit lisible. */
    private String prettyPrinting(String json) {
        JsonObject objet = lire(json);
        JsonWriterFactory factory = Json.createWriterFactory(Map.of(JsonGenerator.PRETTY_PRINTING, true));
        StringWriter sw = new StringWriter();
        try (JsonWriter writer = factory.createWriter(sw)) {
            writer.writeObject(objet);
        }
        return sw.toString();
    }
}
