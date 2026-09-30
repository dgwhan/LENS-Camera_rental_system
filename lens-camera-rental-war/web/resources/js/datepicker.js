/**
 * Lens Camera Rental - Custom Date Range Picker Script (datepicker.js)
 * Minimal datepicker for rental period selection and cart updates.
 */
(function () {
    'use strict';

    const MONTHS = ['January', 'February', 'March', 'April', 'May', 'June',
                    'July', 'August', 'September', 'October', 'November', 'December'];

    const pad = n => (n < 10 ? '0' : '') + n;
    const fmt = d => d ? `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}` : '';
    const fmtDisp = d => d ? `${pad(d.getDate())} / ${pad(d.getMonth() + 1)} / ${d.getFullYear()}` : 'dd / mm / yyyy';

    function parseDate(str) {
        if (!str) return null;
        const p = str.trim().split(/[\/\-\.]/);
        if (p.length !== 3) return null;
        const [d, m, y] = p[0].length === 4 ? [+p[2], +p[1] - 1, +p[0]] : [+p[0], +p[1] - 1, +p[2]];
        const dt = new Date(y, m, d);
        return dt.getFullYear() === y && dt.getMonth() === m && dt.getDate() === d ? (dt.setHours(0, 0, 0, 0), dt) : null;
    }

    function init(wrapper) {
        const $ = s => wrapper.querySelector(`[data-role="${s}"]`);
        const startDisp = $('start-display'), endDisp = $('end-display');
        const startInput = $('start-hidden'), endInput = $('end-hidden');
        const title = $('calendar-title'), grid = $('days-grid');
        const prevBtn = $('prev-month'), nextBtn = $('next-month');
        const startBox = $('start-box'), endBox = $('end-box');

        let start = parseDate(wrapper.getAttribute('data-initial-start') || (startInput && startInput.value));
        let end = parseDate(wrapper.getAttribute('data-initial-end') || (endInput && endInput.value));
        if (start && end && end <= start) end = null;

        let picking = 'start';
        const today = new Date();
        today.setHours(0, 0, 0, 0);

        if (start && start < today) {
            start = null;
            end = null;
        }

        let year = (start || today).getFullYear();
        let month = (start || today).getMonth();

        function syncUI() {
            if (startDisp) {
                startDisp.textContent = fmtDisp(start);
                startDisp.classList.toggle('is-empty', !start);
            }
            if (endDisp) {
                endDisp.textContent = fmtDisp(end);
                endDisp.classList.toggle('is-empty', !end);
            }
            if (startInput) startInput.value = fmt(start);
            if (endInput) endInput.value = fmt(end);
            if (startBox) startBox.classList.toggle('is-active', picking === 'start');
            if (endBox) endBox.classList.toggle('is-active', picking === 'end');
        }

        function render() {
            if (title) title.textContent = `${MONTHS[month]} ${year}`;
            if (prevBtn) {
                const isCur = year === today.getFullYear() && month === today.getMonth();
                prevBtn.disabled = isCur;
                prevBtn.style.opacity = isCur ? '0.35' : '1';
            }
            if (!grid) return;

            const firstDay = new Date(year, month, 1).getDay();
            const daysInMonth = new Date(year, month + 1, 0).getDate();
            const prevDays = new Date(year, month, 0).getDate();

            let h = '';
            for (let i = firstDay - 1; i >= 0; i--) {
                h += `<div class="datepicker-day-cell is-outside"><span class="datepicker-day-num">${prevDays - i}</span></div>`;
            }
            for (let d = 1; d <= daysInMonth; d++) {
                const date = new Date(year, month, d);
                date.setHours(0, 0, 0, 0);
                const isPast = date < today;
                const isStart = start && date.getTime() === start.getTime();
                const isEnd = end && date.getTime() === end.getTime();
                const inRange = start && end && date > start && date < end;
                let c = 'datepicker-day-cell';
                if (isPast) c += ' is-disabled';
                if (date.getTime() === today.getTime()) c += ' is-today';
                if (isStart) c += ' is-range-start';
                if (isEnd) c += ' is-range-end';
                if (inRange) c += ' is-in-range';
                h += `<div class="${c}" data-day="${d}"><span class="datepicker-day-num">${d}</span></div>`;
            }
            const rem = (7 - ((firstDay + daysInMonth) % 7)) % 7;
            for (let n = 1; n <= rem; n++) {
                h += `<div class="datepicker-day-cell is-outside"><span class="datepicker-day-num">${n}</span></div>`;
            }
            grid.innerHTML = h;
        }

        if (grid) {
            grid.onclick = e => {
                const cell = e.target.closest('.datepicker-day-cell[data-day]');
                if (!cell || cell.classList.contains('is-disabled') || cell.classList.contains('is-outside')) return;
                const clicked = new Date(year, month, +cell.getAttribute('data-day'));
                clicked.setHours(0, 0, 0, 0);

                if (picking === 'start' || !start || clicked <= start) {
                    start = clicked;
                    end = null;
                    picking = 'end';
                } else {
                    end = clicked;
                    picking = 'start';
                }
                syncUI();
                render();
            };
        }

        if (startBox) startBox.onclick = () => { picking = 'start'; syncUI(); };
        if (endBox) endBox.onclick = () => { picking = start ? 'end' : 'start'; syncUI(); };

        if (prevBtn) prevBtn.onclick = () => {
            if (year === today.getFullYear() && month === today.getMonth()) return;
            if (--month < 0) { month = 11; year--; }
            render();
        };
        if (nextBtn) nextBtn.onclick = () => {
            if (++month > 11) { month = 0; year++; }
            render();
        };

        const resetBtn = $('reset-btn');
        if (resetBtn) resetBtn.onclick = () => {
            start = null;
            end = null;
            picking = 'start';
            syncUI();
            render();
        };

        const applyBtn = $('apply-btn');
        if (applyBtn) applyBtn.onclick = () => {
            if (!start || !end || end <= start || start < today) return;
            syncUI();
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
            if (startInput) startInput.dispatchEvent(new Event('change', { bubbles: true }));
            if (endInput) endInput.dispatchEvent(new Event('change', { bubbles: true }));
        };

        syncUI();
        render();
        wrapper._datepickerActive = true;
    }

    function initAll() {
        document.querySelectorAll('.custom-datepicker-wrapper').forEach(el => {
            if (!el._datepickerActive || !el.contains(el.querySelector('[data-role="days-grid"]'))) {
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
