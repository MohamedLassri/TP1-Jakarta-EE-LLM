package com.mohamedlassri.tp1.llm;

import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.Serializable;

/**
 * Client REST pour l'API Gemini (endpoint generateContent).
 * La clé est lue dans la variable d'environnement GEMINI_KEY.
 */
@ApplicationScoped
public class LlmClientPourGemini implements Serializable {

    private static final String MODELE = "gemini-3.5-flash-lite";
    private static final String URL_BASE =
            "https://generativelanguage.googleapis.com/v1beta/models/" + MODELE + ":generateContent";

    private final String cle;
    private transient Client clientRest;

    public LlmClientPourGemini() {
        this.cle = System.getenv("GEMINI_KEY");
        this.clientRest = ClientBuilder.newClient();
    }

    /**
     * Envoie la requête JSON à l'API et retourne le corps JSON de la réponse.
     *
     * @throws RequeteException si la clé est absente, ou si l'API répond avec une erreur HTTP.
     */
    public String envoyerRequete(String requeteJson) throws RequeteException {
        if (cle == null || cle.isBlank()) {
            throw new RequeteException("La variable d'environnement GEMINI_KEY n'est pas définie. "
                    + "Définissez-la puis redémarrez Payara / l'IDE.");
        }
        WebTarget cible = clientRest.target(URL_BASE).queryParam("key", cle);
        try (Response reponse = cible.request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(requeteJson, MediaType.APPLICATION_JSON))) {
            String corps = reponse.readEntity(String.class);
            if (reponse.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
                throw new RequeteException("Erreur HTTP " + reponse.getStatus() + " "
                        + reponse.getStatusInfo().getReasonPhrase() + " : " + corps);
            }
            return corps;
        } catch (RequeteException e) {
            throw e;
        } catch (Exception e) {
            throw new RequeteException("Impossible de joindre l'API du LLM : " + e.getMessage(), e);
        }
    }

    @PreDestroy
    public void fermer() {
        if (clientRest != null) {
            clientRest.close();
        }
    }
}
