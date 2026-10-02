/**
 * Lens Camera Rental - Cart Page Interactions
 * Handles date editor accordion and bridges datepicker events to JSF AJAX.
 */
(function () {
    'use strict';

    window.toggleDateEdit = function (cartItemId) {
        var box = document.getElementById('dateEdit_' + cartItemId);
        if (!box) return;

        var isOpening = (box.style.display === 'none' || !box.style.display);

        // Keep layout compact: close all other date editor panels
        document.querySelectorAll('.cart-date-editor').forEach(function (editor) {
            if (editor !== box) {
                editor.style.display = 'none';
            }
        });

        box.style.display = isOpening ? 'block' : 'none';

        if (isOpening && typeof window.initDatePickers === 'function') {
            window.initDatePickers();
        }
    };

    document.addEventListener('datepicker:rangeSet', function (e) {
        var picker = e.target.closest('.custom-datepicker-wrapper');
        if (!picker) return;

        var cartItemId = picker.getAttribute('data-cart-item-id');
        var detail = e.detail;
        if (!cartItemId || !detail || !detail.startFormatted || !detail.endFormatted) return;

        var idField = document.getElementById('updateCartItemId');
        var startField = document.getElementById('updateStartDate');
        var endField = document.getElementById('updateEndDate');
        var submitBtn = document.getElementById('btnSubmitUpdateDates');

        if (!idField || !startField || !endField || !submitBtn) return;

        idField.value = cartItemId;
        startField.value = detail.startFormatted;
        endField.value = detail.endFormatted;
        submitBtn.click();
    });

    if (window.jsf && window.jsf.ajax) {
        window.jsf.ajax.addOnEvent(function (data) {
            if (data.status === 'success' && typeof window.initDatePickers === 'function') {
                window.initDatePickers();
            }
        });
    }
})();
