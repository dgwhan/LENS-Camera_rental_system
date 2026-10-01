/**
 * Lens Camera Rental - Cart DatePicker Script (datepicker.js)
 * Synchronized with Device Detail (rental-period.js)
 * Table calendar grid, lead time rule (>= 1 day in advance), and cart AJAX integration.
 */
(function () {
    'use strict';

    const MONTHS = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];

    const pad = n => (n < 10 ? '0' : '') + n;
    const fmt = d => d ? `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}` : '';
    const fmtDisp = d => d ? `${pad(d.getDate())} / ${pad(d.getMonth() + 1)} / ${d.getFullYear()}` : 'dd / mm / yyyy';

    function parseDate(str) {
        if (!str || typeof str !== 'string') return null;
        const p = str.trim().split(/[\/\-\.]/);
        if (p.length !== 3) return null;
        const [d, m, y] = p[0].length === 4 ? [+p[2], +p[1] - 1, +p[0]] : [+p[0], +p[1] - 1, +p[2]];
        const dt = new Date(y, m, d);
        return dt.getFullYear() === y && dt.getMonth() === m && dt.getDate() === d ? (dt.setHours(0, 0, 0, 0), dt) : null;
    }

    function init(wrapper) {
        if (!wrapper) return;

        const startBox = wrapper.querySelector('[data-role="start-box"]');
        const endBox = wrapper.querySelector('[data-role="end-box"]');
        const startText = wrapper.querySelector('[data-role="start-text"]');
        const endText = wrapper.querySelector('[data-role="end-text"]');
        const durationDisplay = wrapper.querySelector('[data-role="duration-display"]');
        const popup = wrapper.querySelector('.rental-calendar-popup');
        const monthTitle = wrapper.querySelector('[data-role="month-title"]');
        const stepHint = wrapper.querySelector('[data-role="step-hint"]');
        const calendarDays = wrapper.querySelector('[data-role="calendar-days"]');
        const prevBtn = wrapper.querySelector('[data-role="prev-month"]');
        const nextBtn = wrapper.querySelector('[data-role="next-month"]');
        const resetBtn = wrapper.querySelector('[data-role="reset-dates"]');
        const hiddenStart = wrapper.querySelector('[data-role="start-hidden"]');
        const hiddenEnd = wrapper.querySelector('[data-role="end-hidden"]');
        const cancelBtn = wrapper.querySelector('[data-role="cancel-btn"]');
        const applyBtn = wrapper.querySelector('[data-role="apply-btn"]');

        const initialStart = parseDate(wrapper.getAttribute('data-initial-start') || (hiddenStart && hiddenStart.value));
        const initialEnd = parseDate(wrapper.getAttribute('data-initial-end') || (hiddenEnd && hiddenEnd.value));

        let start = initialStart;
        let end = initialEnd;
        if (start && end && end <= start) end = null;

        const today = new Date();
        today.setHours(0, 0, 0, 0);

        // Booking lead-time rule: at least 1 calendar day in advance
        const minStartDate = new Date(today);
        minStartDate.setDate(today.getDate() + 1);

        // If existing start date is expired, reset active selection for user picking
        if (start && start < minStartDate) {
            start = null;
            end = null;
        }

        let picking = 'start';
        let viewYear = (start || minStartDate).getFullYear();
        let viewMonth = (start || minStartDate).getMonth();

        function updateUI() {
            if (startText) {
                startText.textContent = fmtDisp(start);
                startText.classList.toggle('has-value', !!start);
            }
            if (endText) {
                endText.textContent = fmtDisp(end);
                endText.classList.toggle('has-value', !!end);
            }
            if (hiddenStart) hiddenStart.value = fmt(start);
            if (hiddenEnd) hiddenEnd.value = fmt(end);

            const isOpen = popup && popup.classList.contains('is-open');
            if (startBox) startBox.classList.toggle('is-active', isOpen && picking === 'start');
            if (endBox) endBox.classList.toggle('is-active', isOpen && picking === 'end');

            if (durationDisplay) {
                if (start && end && end > start) {
                    const days = Math.round((end.getTime() - start.getTime()) / 86400000);
                    durationDisplay.textContent = 'DURATION: ' + (days === 1 ? '1 DAY' : days + ' DAYS');
                    durationDisplay.classList.add('is-valid');
                } else {
                    durationDisplay.textContent = 'DURATION: --';
                    durationDisplay.classList.remove('is-valid');
                }
            }
        }

        function renderCalendar() {
            if (!monthTitle || !calendarDays) return;
            monthTitle.textContent = `${MONTHS[viewMonth]} ${viewYear}`;

            if (stepHint) {
                stepHint.textContent = (picking === 'end' && start)
                    ? 'Select return date (pickup date + 1 or more days)'
                    : 'Select start date (at least 1 day in advance)';
            }

            if (prevBtn) {
                const isCurrentMinMonth = (viewYear === minStartDate.getFullYear() && viewMonth === minStartDate.getMonth());
                prevBtn.disabled = isCurrentMinMonth;
                prevBtn.style.opacity = isCurrentMinMonth ? '0.35' : '1';
            }

            const firstDay = new Date(viewYear, viewMonth, 1).getDay(); // 0 is Sunday
            const totalDays = new Date(viewYear, viewMonth + 1, 0).getDate();

            let html = '<tr>';
            for (let i = 0; i < firstDay; i++) {
                html += '<td><span class="calendar-day-btn is-empty"></span></td>';
            }

            let col = firstDay;
            for (let d = 1; d <= totalDays; d++) {
                if (col === 7) {
                    html += '</tr><tr>';
                    col = 0;
                }

                const cellDate = new Date(viewYear, viewMonth, d);
                cellDate.setHours(0, 0, 0, 0);

                const isPast = cellDate < minStartDate;
                const isInvalidEnd = picking === 'end' && start && cellDate <= start;

                let cls = 'calendar-day-btn';
                let disabledAttr = '';

                if (isPast || isInvalidEnd) {
                    cls += ' is-disabled';
                    disabledAttr = ' disabled';
                } else {
                    const isStart = start && cellDate.getTime() === start.getTime();
                    const isEnd = end && cellDate.getTime() === end.getTime();
                    const inRange = start && end && cellDate > start && cellDate < end;

                    if (isStart) cls += ' is-start-date';
                    if (isEnd) cls += ' is-end-date';
                    if (inRange) cls += ' is-in-range';
                }

                html += `<td><button type="button" class="${cls}" data-day="${d}"${disabledAttr}>${d}</button></td>`;
                col++;
            }

            while (col > 0 && col < 7) {
                html += '<td><span class="calendar-day-btn is-empty"></span></td>';
                col++;
            }
            html += '</tr>';

            calendarDays.innerHTML = html;
        }

        function openPopup(target) {
            picking = target || 'start';
            if (popup) popup.classList.add('is-open');
            updateUI();
            renderCalendar();
        }

        function closePopup() {
            if (popup) popup.classList.remove('is-open');
            updateUI();
        }

        // Delegate day click
        if (calendarDays) {
            calendarDays.onclick = function (e) {
                const btn = e.target.closest('button[data-day]');
                if (!btn || btn.disabled) return;
                const day = parseInt(btn.getAttribute('data-day'), 10);
                const clicked = new Date(viewYear, viewMonth, day);
                clicked.setHours(0, 0, 0, 0);

                if (picking === 'start' || !start || clicked <= start) {
                    start = clicked;
                    end = null;
                    picking = 'end';
                    updateUI();
                    renderCalendar();
                } else {
                    end = clicked;
                    picking = 'start';
                    updateUI();
                    renderCalendar();
                    closePopup();
                }
            };
        }

        if (startBox) {
            startBox.onclick = function (e) {
                e.stopPropagation();
                if (popup && popup.classList.contains('is-open') && picking === 'start') {
                    closePopup();
                } else {
                    openPopup('start');
                }
            };
        }

        if (endBox) {
            endBox.onclick = function (e) {
                e.stopPropagation();
                if (popup && popup.classList.contains('is-open') && picking === 'end') {
                    closePopup();
                } else {
                    openPopup(start ? 'end' : 'start');
                }
            };
        }

        if (prevBtn) {
            prevBtn.onclick = function (e) {
                e.stopPropagation();
                if (viewYear === minStartDate.getFullYear() && viewMonth === minStartDate.getMonth()) return;
                if (--viewMonth < 0) { viewMonth = 11; viewYear--; }
                renderCalendar();
            };
        }

        if (nextBtn) {
            nextBtn.onclick = function (e) {
                e.stopPropagation();
                if (++viewMonth > 11) { viewMonth = 0; viewYear++; }
                renderCalendar();
            };
        }

        if (resetBtn) {
            resetBtn.onclick = function (e) {
                e.stopPropagation();
                start = null;
                end = null;
                picking = 'start';
                updateUI();
                renderCalendar();
            };
        }

        // Close calendar popup when clicking outside the datepicker wrapper
        document.addEventListener('click', function (e) {
            if (popup && popup.classList.contains('is-open') && !wrapper.contains(e.target)) {
                closePopup();
            }
        });

        // Cancel button: revert selection and collapse editor
        if (cancelBtn) {
            cancelBtn.onclick = function (e) {
                e.preventDefault();
                e.stopPropagation();
                closePopup();
                start = initialStart;
                end = initialEnd;
                updateUI();
                const editor = wrapper.closest('.cart-date-editor');
                if (editor) {
                    editor.style.display = 'none';
                }
            };
        }

        // Apply button: validate and trigger JSF Ajax update
        if (applyBtn) {
            applyBtn.onclick = function (e) {
                e.preventDefault();
                e.stopPropagation();
                if (!start || !end) {
                    alert('Please select both start date and end date.');
                    return;
                }
                if (end <= start) {
                    alert('Rental end date must be after start date.');
                    return;
                }
                if (start < minStartDate) {
                    alert('Rental orders must be placed at least 1 day in advance.');
                    return;
                }
                closePopup();
                updateUI();
                wrapper.dispatchEvent(new CustomEvent('datepicker:rangeSet', {
                    bubbles: true,
                    detail: {
                        startDate: start,
                        endDate: end,
                        startFormatted: fmt(start),
                        endFormatted: fmt(end),
                        totalDays: Math.round((end - start) / 86400000)
                    }
                }));
                if (hiddenStart) hiddenStart.dispatchEvent(new Event('change', { bubbles: true }));
                if (hiddenEnd) hiddenEnd.dispatchEvent(new Event('change', { bubbles: true }));
            };
        }

        updateUI();
        wrapper._datepickerActive = true;
    }

    function initAll() {
        document.querySelectorAll('.custom-datepicker-wrapper').forEach(el => {
            if (!el._datepickerActive || !el.contains(el.querySelector('[data-role="calendar-days"]'))) {
                init(el);
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAll);
    } else {
        initAll();
    }

    window.initDatePickers = initAll;
    window.DatePicker = init;
})();
