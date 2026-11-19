package com.mohamedlassri.tp1.bb;

import com.mohamedlassri.tp1.json.JsonAdapterPourGemini;
import com.mohamedlassri.tp1.json.LlmInteraction;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Backing bean de la page index.xhtml.
 */
@Named("bb")
@ViewScoped
public class Bb implements Serializable {

    /** Rôles système disponibles : libellé -> texte envoyé au LLM. */
    private static final Map<String, String> ROLES = new LinkedHashMap<>();

    static {
        ROLES.put("Helpful assistant",
                "You are a helpful assistant. You help the user to find the information they need. "
                        + "If the user type a question, you answer it.");
        ROLES.put("Traducteur anglais-français",
                "You are an interpreter. You translate from English to French and from French to English. "
                        + "If the user type a French text, you translate it into English. "
                        + "If the user type an English text, you translate it into French. "
                        + "If the text contains only one to three words, give some examples of usage of these words "
                        + "in English and in French. Make a bullet list of the examples.");
        ROLES.put("Guide touristique",
                "You are a tourist guide. If the user type the name of a country, you give two of the most "
                        + "interesting places to visit there, with the approximate distances between them. "
                        + "If the user type the name of a place, you give its approximate distance to the country's "
                        + "other major sights.");
        // Rôle système BONUS : personnalisé
        ROLES.put("Grand-mère marocaine",
                "Tu es une grand-mère marocaine pleine de tendresse et d'humour. Tu réponds toujours en "
                        + "français, avec quelques mots de darija, en appelant l'utilisateur 'a wldi' ou 'a binti'. "
                        + "Chaque réponse contient un proverbe marocain (avec sa traduction) et se termine par une "
                        + "invitation à boire un thé à la menthe. Tes réponses sont correctes mais très chaleureuses.");
    }

    private String roleKey = "Helpful assistant";
    private boolean roleSystemeChangeable = true;

    private String question;
    private String reponse;
    private String conversation = "";

    private boolean debug;
    private String texteRequeteJson;
    private String texteReponseJson;

    @Inject
    private JsonAdapterPourGemini jsonAdapter;

    public Bb() {
    }

    // ------------------------------------------------------------------ actions

    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Texte question vide", "Il manque le texte de la question");
            FacesContext.getCurrentInstance().addMessage(null, message);
            return null;
        }

        // Début de la conversation : on envoie le rôle système à l'adapter.
        if (roleSystemeChangeable) {
            jsonAdapter.setSystemRole(ROLES.get(roleKey));
        }

        try {
            LlmInteraction interaction = jsonAdapter.envoyerRequete(question);
            this.reponse = interaction.reponseExtraite();
            if (debug) {
                this.texteRequeteJson = interaction.questionJson();
                this.texteReponseJson = interaction.reponseJson();
            } else {
                this.texteRequeteJson = null;
                this.texteReponseJson = null;
            }
            this.roleSystemeChangeable = false; // le rôle ne peut plus changer en cours de conversation
            afficherConversation();
            this.question = "";
        } catch (Exception e) {
            this.reponse = "Erreur : " + e.getMessage();
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Problème de connexion avec l'API du LLM",
                    "Problème de connexion avec l'API du LLM : " + e.getMessage() + "\n"
                            + Arrays.toString(e.getStackTrace()));
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
        return null;
    }

    /** Démarre une nouvelle conversation (nouvelle vue => nouveaux beans). */
    public String nouveauChat() {
        return "index?faces-redirect=true";
    }

    private void afficherConversation() {
        conversation += "== User:\n" + question + "\n== Assistant:\n" + reponse + "\n\n";
    }

    // ------------------------------------------------------------------ getters / setters

    public Map<String, String> getRoles() {
        return ROLES;
    }

    public String getRoleKey() {
        return roleKey;
    }

    public void setRoleKey(String roleKey) {
        this.roleKey = roleKey;
    }

    public boolean isRoleSystemeChangeable() {
        return roleSystemeChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation;
    }

    public void setConversation(String conversation) {
        this.conversation = conversation;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public String getTexteRequeteJson() {
        return texteRequeteJson;
    }

    public void setTexteRequeteJson(String texteRequeteJson) {
        this.texteRequeteJson = texteRequeteJson;
    }

    public String getTexteReponseJson() {
        return texteReponseJson;
    }

    public void setTexteReponseJson(String texteReponseJson) {
        this.texteReponseJson = texteReponseJson;
    }
}
