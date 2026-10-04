(function () {
    'use strict';

    document.addEventListener('click', function (event) {
        var toggle = event.target.closest('[data-password-toggle]');
        if (!toggle) {
            return;
        }

        var input = document.getElementById(toggle.getAttribute('data-password-toggle'));
        if (!input) {
            return;
        }

        var showPassword = input.type === 'password';
        input.type = showPassword ? 'text' : 'password';
        toggle.setAttribute('aria-label', showPassword ? 'Hide password' : 'Show password');
        toggle.setAttribute('aria-pressed', String(showPassword));
        input.focus({ preventScroll: true });
    });
}());
