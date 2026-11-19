package com.mohamedlassri.tp1.llm;

/**
 * Exception levée quand une requête vers l'API du LLM échoue
 * (clé absente ou invalide, erreur HTTP, JSON inattendu, etc.).
 */
public class RequeteException extends Exception {

    public RequeteException(String message) {
        super(message);
    }

    public RequeteException(String message, Throwable cause) {
        super(message, cause);
    }
}
