# TP 1 - Accès direct à l'API Gemini (Jakarta EE / JSF)

## Avant de lancer
1. Récupérer une clé sur https://aistudio.google.com/apikey
2. Créer la variable d'environnement `GEMINI_KEY` (puis redémarrer IntelliJ / le terminal / Payara).
3. Java 17+, Payara 6 (Jakarta EE 10).

## Lancer
`mvn clean package` puis déployer `target/tp1.war` sur Payara (ou plugin IntelliJ).
URL : http://localhost:8080/tp1/

## Architecture
- `Bb` (backing bean) -> `JsonAdapterPourGemini` (JSON + historique) -> `LlmClientPourGemini` (client REST)
- `LlmInteraction` : record (questionJson, reponseJson, reponseExtraite)
- `RequeteException` : exception métier
- Package : `com.mohamedlassri.tp1`

## Manipulations pour le bonus (ne pas committer les erreurs)
1. Mettre une mauvaise clé -> noter le message d'erreur (HTTP 400 API key not valid).
2. Dans `JsonAdapterPourGemini.creerRequeteJson`, remplacer `message("user", question)` par `message("system", question)` -> noter l'erreur.
3. Revenir en arrière : `git restore .` (ou `git checkout -- .`, ou `git reset --hard HEAD`).

## Git
git init && git add . && git commit -m "TP1" && git remote add origin <url> && git push -u origin main
"# TP1-Jakarta-EE-LLM" 
