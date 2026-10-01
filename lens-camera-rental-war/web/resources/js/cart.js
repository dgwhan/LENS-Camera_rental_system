/**
 * Lens Camera Rental - Cart & Rental Period Integration Script (cart.js)
 * Coordinates collapsible date editing, custom datepicker events,
 * and JSF AJAX re-renders.
 */
(function () {
    'use strict';

    /**
     * Toggles visibility of the inline rental date editor for a cart item.
     * Closes any other open editors to prevent multiple expanded items.
     * @param {string} cartItemId The unique identifier of the cart item.
     */
    window.toggleDateEdit = function (cartItemId) {
        var box = document.getElementById('dateEdit_' + cartItemId);
        if (!box) return;

        var isHidden = (box.style.display === 'none' || box.style.display === '');

        // Close any other open date editors in the cart to keep layout compact
        document.querySelectorAll('.cart-date-editor').forEach(function (editor) {
            if (editor !== box) {
                editor.style.display = 'none';
            }
        });

        box.style.display = isHidden ? 'block' : 'none';

        if (isHidden && typeof window.initDatePickers === 'function') {
            window.initDatePickers();
        }
    };

    /**
     * Closes the inline rental date editor for a cart item.
     * @param {string} cartItemId The unique identifier of the cart item.
     */
    window.closeDateEdit = function (cartItemId) {
        var box = document.getElementById('dateEdit_' + cartItemId);
        if (box) {
            box.style.display = 'none';
        }
    };

    /**
     * Listens for the custom 'datepicker:rangeSet' event dispatched by datepicker.js.
     * When user clicks "APPLY DATES", transfers selected dates to hidden inputs
     * and submits JSF Ajax form update.
     */
    document.addEventListener('datepicker:rangeSet', function (e) {
        var picker = e.target.closest ? e.target.closest('.custom-datepicker-wrapper') : null;
        if (!picker) {
            picker = e.target;
        }
        if (!picker || !picker.id) return;

        var pickerId = picker.id; // e.g. "picker_20"
        var cartItemId = pickerId.replace('picker_', '');

        var startFormatted = (e.detail && e.detail.startFormatted) ? e.detail.startFormatted : '';
        var endFormatted = (e.detail && e.detail.endFormatted) ? e.detail.endFormatted : '';

        if (!startFormatted || !endFormatted) {
            var startHidden = picker.querySelector('[data-role="start-hidden"]');
            var endHidden = picker.querySelector('[data-role="end-hidden"]');
            if (startHidden && endHidden) {
                startFormatted = startHidden.value;
                endFormatted = endHidden.value;
            }
        }

        if (!cartItemId || !startFormatted || !endFormatted) {
            return;
        }

        var idField = document.getElementById('updateCartItemId');
        var startField = document.getElementById('updateStartDate');
        var endField = document.getElementById('updateEndDate');
        var submitBtn = document.getElementById('btnSubmitUpdateDates');

        if (idField && startField && endField && submitBtn) {
            idField.value = cartItemId;
            startField.value = startFormatted;
            endField.value = endFormatted;
            submitBtn.click();
        }
    });

    /**
     * Re-initializes DatePicker instances whenever JSF AJAX re-renders parts of the page.
     */
    if (window.jsf && window.jsf.ajax) {
        window.jsf.ajax.addOnEvent(function (data) {
            if (data.status === 'success') {
                if (typeof window.initDatePickers === 'function') {
                    window.initDatePickers();
                }
            }
        });
    }
})();
