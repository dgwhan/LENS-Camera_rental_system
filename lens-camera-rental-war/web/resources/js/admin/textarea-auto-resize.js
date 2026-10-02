document.addEventListener("DOMContentLoaded", function () {
    var textareas = document.querySelectorAll("textarea.auto-resize, textarea[id$='description']");
    textareas.forEach(function (textarea) {
        var resize = function () {
            textarea.style.height = "auto";
            textarea.style.height = Math.max(textarea.scrollHeight + 2, 80) + "px";
        };
        textarea.addEventListener("input", resize);
        resize();
    });
});
