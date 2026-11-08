/* Espande/collassa la bolla del testo inviato al summarizer */
function toggleText(btn) {
    var preview = btn.previousElementSibling;
    var expanded = preview.classList.toggle('expanded');
    btn.innerHTML = expanded ? 'Show less &#x25B2;' : 'Show more &#x25BC;';
}
