(function () {
    'use strict';

    function localDate(value) {
        if (!value) return null;
        const parts = value.split('-').map(Number);
        if (parts.length !== 3) return null;
        const date = new Date(parts[0], parts[1] - 1, parts[2]);
        if (date.getFullYear() !== parts[0] || date.getMonth() !== parts[1] - 1
                || date.getDate() !== parts[2]) return null;
        date.setHours(0, 0, 0, 0);
        return date;
    }

    function isoDate(date) {
        return date.getFullYear() + '-' + String(date.getMonth() + 1).padStart(2, '0')
            + '-' + String(date.getDate()).padStart(2, '0');
    }

    function tomorrowIso() {
        const tomorrow = new Date();
        tomorrow.setHours(0, 0, 0, 0);
        tomorrow.setDate(tomorrow.getDate() + 1);
        return isoDate(tomorrow);
    }

    function updateRentalPeriod() {
        const section = document.querySelector('[data-rental-period="admin"]');
        if (!section) return;

        const start = document.getElementById('rentalOrderForm:startDate');
        const end = document.getElementById('rentalOrderForm:endDate');
        const duration = section.querySelector('[data-role="duration-display"]');
        if (!start || !end || !duration) return;

        start.min = isoDate(new Date(new Date().setHours(0, 0, 0, 0)));
        if (start.value) {
            const startDate = localDate(start.value);
            if (startDate) {
                const earliestEnd = new Date(startDate);
                earliestEnd.setDate(earliestEnd.getDate() + 1);
                end.min = isoDate(earliestEnd);
                if (end.value && localDate(end.value) <= startDate) end.value = '';
            } else {
                end.value = '';
                end.min = tomorrowIso();
            }
        } else {
            end.value = '';
            end.min = tomorrowIso();
        }

        const startDate = localDate(start.value);
        const endDate = localDate(end.value);
        const days = startDate && endDate
            ? Math.round((endDate.getTime() - startDate.getTime()) / 86400000) : 0;
        duration.textContent = days > 0
            ? 'Duration: ' + days + (days === 1 ? ' day' : ' days')
            : 'Duration: --';

        start.onchange = function () {
            updateRentalPeriod();
            clearPeriodValidationMessage();
        };
        end.onchange = function () {
            updateRentalPeriod();
            clearPeriodValidationMessage();
        };
    }

    function clearPeriodValidationMessage() {
        const message = document.getElementById('rentalOrderForm:rentalPeriodMessageContainer');
        if (message) {
            message.textContent = '';
            message.classList.remove('field-error');
        }
    }

    function bindAvailabilityValidation() {
        if (window._adminCreateOrderAvailabilityValidationBound) return;
        window._adminCreateOrderAvailabilityValidationBound = true;

        document.addEventListener('click', function (event) {
            const button = event.target.closest('#rentalOrderForm\\:checkAvailability');
            if (!button) return;

            const device = document.getElementById('rentalOrderForm:deviceModel');
            const deviceMessage = document.getElementById('rentalOrderForm:deviceModelMessageContainer');
            const start = document.getElementById('rentalOrderForm:startDate');
            const end = document.getElementById('rentalOrderForm:endDate');
            const periodMessage = document.getElementById('rentalOrderForm:rentalPeriodMessageContainer');
            let invalid = false;

            if (!device || !device.value) {
                invalid = true;
                if (deviceMessage) {
                    deviceMessage.textContent = 'Select a device model before checking availability.';
                    deviceMessage.classList.add('field-error');
                }
            } else if (deviceMessage) {
                deviceMessage.textContent = '';
                deviceMessage.classList.remove('field-error');
            }

            const startMissing = !start || !start.value.trim();
            const endMissing = !end || !end.value.trim();
            if (startMissing || endMissing) {
                invalid = true;
                let dateError;
                if (startMissing && endMissing) {
                    dateError = 'Select a start and end date before checking availability.';
                } else if (startMissing) {
                    dateError = 'Select a start date before checking availability.';
                } else {
                    dateError = 'Select an end date before checking availability.';
                }
                if (periodMessage) {
                    periodMessage.textContent = dateError;
                    periodMessage.classList.add('field-error');
                }
            } else {
                clearPeriodValidationMessage();
            }

            if (invalid) {
                event.preventDefault();
                event.stopImmediatePropagation();
            }
        }, true);

        const device = document.getElementById('rentalOrderForm:deviceModel');
        if (device && !device.dataset.availabilityValidationBound) {
            device.dataset.availabilityValidationBound = 'true';
            device.addEventListener('change', function () {
                const message = document.getElementById('rentalOrderForm:deviceModelMessageContainer');
                if (message) {
                    message.textContent = '';
                    message.classList.remove('field-error');
                }
            });
        }
    }

    function syncAddressVisibility() {
        const method = document.getElementById('rentalOrderForm:handoverMethod');
        const panel = document.getElementById('rentalOrderForm:deliveryAddressPanel');
        if (method && panel) panel.hidden = method.value !== 'DELIVERY';
        if (method && !method.dataset.addressToggleBound) {
            method.dataset.addressToggleBound = 'true';
            method.addEventListener('change', function () {
                if (panel) panel.hidden = method.value !== 'DELIVERY';
            });
        }
    }

    function initialize() {
        updateRentalPeriod();
        bindAvailabilityValidation();
        syncAddressVisibility();
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', initialize);
    else initialize();

    if (window.jsf && jsf.ajax) {
        jsf.ajax.addOnEvent(function (data) {
            if (data.status === 'success') window.setTimeout(initialize, 0);
        });
    }
})();
