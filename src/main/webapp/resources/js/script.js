/* Copie le contenu du textarea "conversation" dans le presse-papiers. */
function copierConversation() {
    const zone = document.getElementById('form:conversation');
    if (!zone) { return; }
    const texte = zone.value;
    if (navigator.clipboard && navigator.clipboard.writeText) {
        navigator.clipboard.writeText(texte);
    } else {
        zone.select();
        document.execCommand('copy');
    }
}
