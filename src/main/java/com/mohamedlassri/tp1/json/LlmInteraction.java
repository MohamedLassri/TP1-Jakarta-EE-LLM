package com.mohamedlassri.tp1.json;

/**
 * Résultat d'une interaction avec le LLM.
 *
 * @param questionJson    JSON (mis en forme) de la requête envoyée
 * @param reponseJson     JSON (mis en forme) de la réponse reçue
 * @param reponseExtraite texte de la réponse à afficher
 */
public record LlmInteraction(String questionJson, String reponseJson, String reponseExtraite) {
}
