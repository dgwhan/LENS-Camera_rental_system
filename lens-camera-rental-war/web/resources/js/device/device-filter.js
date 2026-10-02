/**
 * LENS Camera Rental - Reusable DeviceFilter Controller
 * Strictly adheres to approved visual design reference:
 * - Immediate dispatch on desktop/tablet input change
 * - Pristine default state with zero visual clutter
 * - Dynamic active filter chips with individual removal
 * - Synchronized mobile modal drawer architecture
 * - Responsive 2-row tablet wrapping
 */
(function () {
    'use strict';

    function initDeviceFilter(wrapper) {
        if (!wrapper) return;

        // Elements - Desktop / Tablet
        const desktopSearch = wrapper.querySelector('[data-role="desktop-search"]');
        const desktopSearchClear = wrapper.querySelector('[data-role="desktop-search-clear"]');
        const desktopSearchWrap = wrapper.querySelector('[data-role="desktop-search-wrap"]');
        const typeSelect = wrapper.querySelector('[data-role="desktop-type"]');
        const brandSelect = wrapper.querySelector('[data-role="desktop-brand"]');
        const rateSelect = wrapper.querySelector('[data-role="desktop-rate"]');
        const resetBtn = wrapper.querySelector('[data-role="reset-btn"]');

        // Elements - Mobile
        const mobileSearch = wrapper.querySelector('[data-role="mobile-search"]');
        const mobileSearchClear = wrapper.querySelector('[data-role="mobile-search-clear"]');
        const mobileSearchWrap = wrapper.querySelector('[data-role="mobile-search-wrap"]');
        const mobileTrigger = wrapper.querySelector('[data-role="mobile-trigger"]');
        const mobileBadge = wrapper.querySelector('[data-role="mobile-badge"]');
        const drawerOverlay = wrapper.querySelector('[data-role="drawer-overlay"]');
        const drawerClose = wrapper.querySelector('[data-role="drawer-close"]');
        const drawerType = wrapper.querySelector('[data-role="drawer-type"]');
        const drawerBrand = wrapper.querySelector('[data-role="drawer-brand"]');
        const drawerRate = wrapper.querySelector('[data-role="drawer-rate"]');
        const drawerReset = wrapper.querySelector('[data-role="drawer-reset"]');
        const drawerApply = wrapper.querySelector('[data-role="drawer-apply"]');

        // Active Tray Elements
        const activeTray = wrapper.querySelector('[data-role="active-tray"]');
        const chipsContainer = wrapper.querySelector('[data-role="chips-container"]');
        const clearAllBtn = wrapper.querySelector('[data-role="clear-all"]');
        const matchDisplay = wrapper.querySelector('[data-role="match-display"]');

        // Initial State from Data Attributes
        let search = wrapper.getAttribute('data-search') || '';
        let type = wrapper.getAttribute('data-type') || '';
        let brand = wrapper.getAttribute('data-brand') || '';
        let rate = wrapper.getAttribute('data-rate') || '';
        let filteredCount = parseInt(wrapper.getAttribute('data-filtered-count') || '0', 10);
        let totalCount = parseInt(wrapper.getAttribute('data-total-count') || '0', 10);

        // Sync inputs from initial state
        if (desktopSearch) desktopSearch.value = search;
        if (mobileSearch) mobileSearch.value = search;
        if (typeSelect) typeSelect.value = type;
        if (drawerType) drawerType.value = type;
        if (brandSelect) brandSelect.value = brand;
        if (drawerBrand) drawerBrand.value = brand;
        if (rateSelect) rateSelect.value = rate;
        if (drawerRate) drawerRate.value = rate;

        function getActiveCount() {
            let count = 0;
            if (search.trim().length > 0) count++;
            if (type && type !== '') count++;
            if (brand && brand !== '') count++;
            if (rate && rate !== '') count++;
            return count;
        }

        function getRateLabel(rateValue) {
            if (!rateValue) return '';
            if (rateSelect) {
                const opt = rateSelect.querySelector(`option[value="${rateValue}"]`);
                if (opt) return opt.textContent.replace(' / day', '');
            }
            return rateValue;
        }

        function renderActiveChips() {
            const activeCount = getActiveCount();

            // Toggle Search clear button visibility
            if (desktopSearchWrap) {
                desktopSearchWrap.classList.toggle('has-value', search.trim().length > 0);
            }
            if (mobileSearchWrap) {
                mobileSearchWrap.classList.toggle('has-value', search.trim().length > 0);
            }

            // Toggle Reset Button state (Subdued/Disabled when 0 active, enabled when > 0)
            if (resetBtn) {
                resetBtn.classList.toggle('is-active', activeCount > 0);
                resetBtn.disabled = activeCount === 0;
            }

            // Toggle Mobile Badge
            if (mobileBadge) {
                if (activeCount > 0) {
                    mobileBadge.textContent = activeCount;
                    mobileBadge.style.display = 'inline-flex';
                } else {
                    mobileBadge.style.display = 'none';
                }
            }

            // Update Match Count Display
            if (matchDisplay) {
                matchDisplay.innerHTML = `Matches: <strong class="match-count-strong">${filteredCount}</strong> of <strong class="match-count-strong">${totalCount}</strong> models`;
            }
            if (drawerApply) {
                drawerApply.textContent = filteredCount > 0 ? `Apply (${filteredCount} Results)` : 'Apply';
            }

            // Render Tray
            if (!activeTray || !chipsContainer) return;

            if (activeCount === 0) {
                activeTray.classList.remove('is-active');
                chipsContainer.innerHTML = '';
                return;
            }

            // Tray is visible
            activeTray.classList.add('is-active');
            chipsContainer.innerHTML = '';

            // 1. Search Chip
            if (search.trim().length > 0) {
                chipsContainer.appendChild(createChip('Query', `"${search.trim()}"`, function () {
                    search = '';
                    if (desktopSearch) desktopSearch.value = '';
                    if (mobileSearch) mobileSearch.value = '';
                    triggerFilterChange();
                }));
            }

            // 2. Type Chip
            if (type && type !== '') {
                chipsContainer.appendChild(createChip('Type', type, function () {
                    type = '';
                    if (typeSelect) typeSelect.value = '';
                    if (drawerType) drawerType.value = '';
                    triggerFilterChange();
                }));
            }

            // 3. Brand Chip
            if (brand && brand !== '') {
                chipsContainer.appendChild(createChip('Brand', brand, function () {
                    brand = '';
                    if (brandSelect) brandSelect.value = '';
                    if (drawerBrand) drawerBrand.value = '';
                    triggerFilterChange();
                }));
            }

            // 4. Rate Chip
            if (rate && rate !== '') {
                chipsContainer.appendChild(createChip('Rate', getRateLabel(rate), function () {
                    rate = '';
                    if (rateSelect) rateSelect.value = '';
                    if (drawerRate) drawerRate.value = '';
                    triggerFilterChange();
                }));
            }
        }

        function createChip(label, value, onRemove) {
            const chip = document.createElement('div');
            chip.className = 'device-filter-chip';
            chip.innerHTML = `<span>${label}: <strong>${value}</strong></span>`;

            const removeBtn = document.createElement('button');
            removeBtn.type = 'button';
            removeBtn.className = 'chip-remove-btn';
            removeBtn.setAttribute('aria-label', `Remove ${label} filter`);
            removeBtn.innerHTML = '&#10005;'; // x icon
            removeBtn.addEventListener('click', function (e) {
                e.stopPropagation();
                onRemove();
            });

            chip.appendChild(removeBtn);
            return chip;
        }

        function resetAllFilters() {
            search = '';
            type = '';
            brand = '';
            rate = '';

            if (desktopSearch) desktopSearch.value = '';
            if (mobileSearch) mobileSearch.value = '';
            if (typeSelect) typeSelect.value = '';
            if (drawerType) drawerType.value = '';
            if (brandSelect) brandSelect.value = '';
            if (drawerBrand) drawerBrand.value = '';
            if (rateSelect) rateSelect.value = '';
            if (drawerRate) drawerRate.value = '';

            triggerFilterChange();
        }

        function triggerFilterChange() {
            // Update wrapper data attributes
            wrapper.setAttribute('data-search', search);
            wrapper.setAttribute('data-type', type);
            wrapper.setAttribute('data-brand', brand);
            wrapper.setAttribute('data-rate', rate);

            const activeCount = getActiveCount();
            renderActiveChips();

            // Custom DOM Event
            const filterState = {
                search: search,
                type: type,
                brand: brand,
                rate: rate,
                activeCount: activeCount,
                wrapper: wrapper
            };

            const changeEvt = new CustomEvent('deviceFilter:change', {
                bubbles: true,
                detail: filterState
            });
            wrapper.dispatchEvent(changeEvt);
        }

        // Search Input Handlers (Debounced)
        let searchDebounceTimer;
        function handleSearchInput(newVal) {
            search = newVal;
            if (desktopSearch && desktopSearch.value !== newVal) desktopSearch.value = newVal;
            if (mobileSearch && mobileSearch.value !== newVal) mobileSearch.value = newVal;

            clearTimeout(searchDebounceTimer);
            searchDebounceTimer = setTimeout(function () {
                triggerFilterChange();
            }, 300);
        }

        if (desktopSearch) {
            desktopSearch.addEventListener('input', function () {
                handleSearchInput(this.value);
            });
            desktopSearch.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    clearTimeout(searchDebounceTimer);
                    triggerFilterChange();
                }
            });
        }

        if (desktopSearchClear) {
            desktopSearchClear.addEventListener('click', function () {
                handleSearchInput('');
                if (desktopSearch) desktopSearch.focus();
            });
        }

        if (mobileSearch) {
            mobileSearch.addEventListener('input', function () {
                handleSearchInput(this.value);
            });
            mobileSearch.addEventListener('keydown', function (e) {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    clearTimeout(searchDebounceTimer);
                    triggerFilterChange();
                }
            });
        }

        if (mobileSearchClear) {
            mobileSearchClear.addEventListener('click', function () {
                handleSearchInput('');
                if (mobileSearch) mobileSearch.focus();
            });
        }

        // Desktop Dropdown Handlers (Immediate Dispatch)
        if (typeSelect) {
            typeSelect.addEventListener('change', function () {
                type = this.value;
                if (drawerType) drawerType.value = type;
                triggerFilterChange();
            });
        }

        if (brandSelect) {
            brandSelect.addEventListener('change', function () {
                brand = this.value;
                if (drawerBrand) drawerBrand.value = brand;
                triggerFilterChange();
            });
        }

        if (rateSelect) {
            rateSelect.addEventListener('change', function () {
                rate = this.value;
                if (drawerRate) drawerRate.value = rate;
                triggerFilterChange();
            });
        }

        // Reset Buttons
        if (resetBtn) {
            resetBtn.addEventListener('click', function (e) {
                e.preventDefault();
                resetAllFilters();
            });
        }

        if (clearAllBtn) {
            clearAllBtn.addEventListener('click', function (e) {
                e.preventDefault();
                resetAllFilters();
            });
        }

        // Mobile Drawer Interactivity
        function openDrawer() {
            if (drawerOverlay) {
                // Sync current state to drawer
                if (drawerType) drawerType.value = type;
                if (drawerBrand) drawerBrand.value = brand;
                if (drawerRate) drawerRate.value = rate;
                drawerOverlay.classList.add('is-open');
                document.body.style.overflow = 'hidden';
            }
        }

        function closeDrawer() {
            if (drawerOverlay) {
                drawerOverlay.classList.remove('is-open');
                document.body.style.overflow = '';
            }
        }

        if (mobileTrigger) {
            mobileTrigger.addEventListener('click', function (e) {
                e.preventDefault();
                openDrawer();
            });
        }

        if (drawerClose) {
            drawerClose.addEventListener('click', function () {
                closeDrawer();
            });
        }

        if (drawerOverlay) {
            drawerOverlay.addEventListener('click', function (e) {
                if (e.target === drawerOverlay) {
                    closeDrawer();
                }
            });
        }

        document.addEventListener('keydown', function (e) {
            if (e.key === 'Escape' && drawerOverlay && drawerOverlay.classList.contains('is-open')) {
                closeDrawer();
            }
        });

        if (drawerReset) {
            drawerReset.addEventListener('click', function () {
                if (drawerType) drawerType.value = '';
                if (drawerBrand) drawerBrand.value = '';
                if (drawerRate) drawerRate.value = '';
            });
        }

        if (drawerApply) {
            drawerApply.addEventListener('click', function () {
                if (drawerType) type = drawerType.value;
                if (drawerBrand) brand = drawerBrand.value;
                if (drawerRate) rate = drawerRate.value;

                if (typeSelect) typeSelect.value = type;
                if (brandSelect) brandSelect.value = brand;
                if (rateSelect) rateSelect.value = rate;

                closeDrawer();
                triggerFilterChange();
            });
        }

        // Initial Chip Render
        renderActiveChips();
    }

    function initAll() {
        const instances = document.querySelectorAll('.device-filter-wrapper');
        instances.forEach(initDeviceFilter);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initAll);
    } else {
        initAll();
    }
})();
