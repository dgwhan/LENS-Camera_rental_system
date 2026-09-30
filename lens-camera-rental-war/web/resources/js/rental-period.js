/**
 * LENS Camera Rental - Rental Period & Date Picker Script
 * Streamlined, lightweight date range selection (24h increments).
 */
document.addEventListener('DOMContentLoaded', function () {
    const wrapper = document.querySelector('.rental-period-section');
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

    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const MONTHS = [
        'January', 'February', 'March', 'April', 'May', 'June',
        'July', 'August', 'September', 'October', 'November', 'December'
    ];

    function parseDate(str) {
        if (!str || typeof str !== 'string') return null;
        const p = str.trim().split(/[\/\-\.]/);
        if (p.length === 3) {
            // Check yyyy-mm-dd vs dd/mm/yyyy
            const d = p[0].length === 4 ? parseInt(p[2], 10) : parseInt(p[0], 10);
            const m = parseInt(p[1], 10) - 1;
            const y = p[0].length === 4 ? parseInt(p[0], 10) : parseInt(p[2], 10);
            const dt = new Date(y, m, d);
            if (!isNaN(dt.getTime())) {
                dt.setHours(0, 0, 0, 0);
                return dt;
            }
        }
        return null;
    }

    let startDate = parseDate(hiddenStart ? hiddenStart.value : null);
    let endDate = parseDate(hiddenEnd ? hiddenEnd.value : null);
    let picking = 'start'; // 'start' | 'end'

    let viewYear = (startDate || today).getFullYear();
    let viewMonth = (startDate || today).getMonth();

    function pad(n) {
        return n < 10 ? '0' + n : '' + n;
    }

    function formatDisplay(d) {
        return d ? `${pad(d.getDate())} / ${pad(d.getMonth() + 1)} / ${d.getFullYear()}` : 'dd / mm / yyyy';
    }

    function formatIso(d) {
        return d ? `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` : '';
    }

    function updateUI() {
        if (startText) {
            startText.textContent = formatDisplay(startDate);
            startText.classList.toggle('has-value', !!startDate);
        }
        if (endText) {
            endText.textContent = formatDisplay(endDate);
            endText.classList.toggle('has-value', !!endDate);
        }

        const isoStart = formatIso(startDate);
        const isoEnd = formatIso(endDate);
        if (hiddenStart) hiddenStart.value = isoStart;
        if (hiddenEnd) hiddenEnd.value = isoEnd;

        // Sync to global hidden inputs if present (used by detail.xhtml AJAX check)
        const gStart = document.getElementById('startDateHidden');
        const gEnd = document.getElementById('endDateHidden');
        if (gStart) gStart.value = isoStart;
        if (gEnd) gEnd.value = isoEnd;

        // Duration calculation
        if (durationDisplay) {
            if (startDate && endDate) {
                const days = Math.round((endDate.getTime() - startDate.getTime()) / 86400000);
                if (days >= 1) {
                    durationDisplay.textContent = 'DURATION: ' + (days === 1 ? '1 DAY' : days + ' DAYS');
                    durationDisplay.classList.add('is-valid');
                } else {
                    durationDisplay.textContent = 'DURATION: --';
                    durationDisplay.classList.remove('is-valid');
                }
            } else {
                durationDisplay.textContent = 'DURATION: --';
                durationDisplay.classList.remove('is-valid');
            }
        }

        // Active border
        const isOpen = popup && popup.classList.contains('is-open');
        if (startBox) startBox.classList.toggle('is-active', isOpen && picking === 'start');
        if (endBox) endBox.classList.toggle('is-active', isOpen && picking === 'end');
    }

    function renderCalendar() {
        if (!monthTitle || !calendarDays) return;
        monthTitle.textContent = `${MONTHS[viewMonth]} ${viewYear}`;

        if (stepHint) {
            stepHint.textContent = (picking === 'end' && startDate)
                ? 'Select return date (pickup date + 1 or more days)'
                : 'Select start date (pickup date)';
        }

        if (prevBtn) {
            const isCurrentMonth = (viewYear === today.getFullYear() && viewMonth === today.getMonth());
            prevBtn.disabled = isCurrentMonth;
            prevBtn.style.opacity = isCurrentMonth ? '0.35' : '1';
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

            const isPast = cellDate < today;
            const isInvalidEnd = picking === 'end' && startDate && cellDate <= startDate;

            let cls = 'calendar-day-btn';
            let disabledAttr = '';

            if (isPast || isInvalidEnd) {
                cls += ' is-disabled';
                disabledAttr = ' disabled';
            } else {
                const isStart = startDate && cellDate.getTime() === startDate.getTime();
                const isEnd = endDate && cellDate.getTime() === endDate.getTime();
                const inRange = startDate && endDate && cellDate > startDate && cellDate < endDate;

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

    // Event delegation on calendarDays
    if (calendarDays) {
        calendarDays.addEventListener('click', function (e) {
            const btn = e.target.closest('button[data-day]');
            if (!btn || btn.disabled) return;
            const day = parseInt(btn.getAttribute('data-day'), 10);
            const clicked = new Date(viewYear, viewMonth, day);
            clicked.setHours(0, 0, 0, 0);

            if (picking === 'start' || !startDate || clicked <= startDate) {
                startDate = clicked;
                endDate = null;
                picking = 'end';
                updateUI();
                renderCalendar();
            } else {
                endDate = clicked;
                picking = 'start';
                closePopup();
            }
        });
    }

    if (startBox) {
        startBox.addEventListener('click', function (e) {
            e.stopPropagation();
            openPopup('start');
        });
    }

    if (endBox) {
        endBox.addEventListener('click', function (e) {
            e.stopPropagation();
            openPopup(startDate ? 'end' : 'start');
        });
    }

    if (prevBtn) {
        prevBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            viewMonth--;
            if (viewMonth < 0) {
                viewMonth = 11;
                viewYear--;
            }
            renderCalendar();
        });
    }

    if (nextBtn) {
        nextBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            viewMonth++;
            if (viewMonth > 11) {
                viewMonth = 0;
                viewYear++;
            }
            renderCalendar();
        });
    }

    if (resetBtn) {
        resetBtn.addEventListener('click', function (e) {
            e.stopPropagation();
            startDate = null;
            endDate = null;
            picking = 'start';
            updateUI();
            renderCalendar();
        });
    }

    // Dismiss calendar on click outside
    document.addEventListener('click', function (e) {
        if (popup && !wrapper.contains(e.target)) {
            closePopup();
        }
    });

    updateUI();
    renderCalendar();
});
