/**
 * LENS Camera Rental - Customer Catalog & Pricing Page Controller Bridge
 * Connects DeviceFilter and Pagination events with URL navigation
 */
(function () {
    'use strict';

    function initCatalogBridge() {
        const filterWrapper = document.getElementById('deviceCatalogFilter');
        if (filterWrapper) {
            filterWrapper.addEventListener('deviceFilter:change', function (e) {
                const detail = e.detail;
                const currentUrl = new URL(window.location.href);

                // Update Filter Query Params
                if (detail.search && detail.search.trim()) {
                    currentUrl.searchParams.set('search', detail.search.trim());
                } else {
                    currentUrl.searchParams.delete('search');
                }

                if (detail.type) {
                    currentUrl.searchParams.set('type', detail.type);
                } else {
                    currentUrl.searchParams.delete('type');
                }

                if (detail.brand) {
                    currentUrl.searchParams.set('brand', detail.brand);
                } else {
                    currentUrl.searchParams.delete('brand');
                }

                if (detail.rate) {
                    currentUrl.searchParams.set('rate', detail.rate);
                } else {
                    currentUrl.searchParams.delete('rate');
                }

                // Reset page to 1 when filters change
                currentUrl.searchParams.delete('page');

                window.location.href = currentUrl.toString();
            });
        }

        const paginationNav = document.getElementById('catalogPagination');
        if (paginationNav) {
            paginationNav.addEventListener('pagination:change', function (e) {
                const newPage = e.detail.page;
                const currentUrl = new URL(window.location.href);
                if (newPage > 1) {
                    currentUrl.searchParams.set('page', newPage);
                } else {
                    currentUrl.searchParams.delete('page');
                }
                window.location.href = currentUrl.toString();
            });
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initCatalogBridge);
    } else {
        initCatalogBridge();
    }
})();
