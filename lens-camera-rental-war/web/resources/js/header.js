/* ==========================================================================
   Lens Camera Rental - Header Interactive Scripts (header.js)
   - Mobile Navigation Drawer Toggle
   - User Account Dropdown Toggle
   - Outside Click Dismissal
   ========================================================================== */

(function () {
    'use strict';

    function toggleMobileNav(e) {
        if (e) {
            e.stopPropagation();
        }
        var header = document.querySelector('.site-header');
        if (header) {
            header.classList.toggle('nav-open');
        }
    }

    function toggleUserDropdown(e) {
        if (e) {
            e.stopPropagation();
        }
        var menu = document.getElementById('userDropdownMenu');
        if (menu) {
            var isShown = menu.style.display === 'block';
            menu.style.display = isShown ? 'none' : 'block';
        }
    }

    // Expose globally
    window.toggleMobileNav = toggleMobileNav;
    window.toggleUserDropdown = toggleUserDropdown;

    document.addEventListener('click', function (e) {
        // 1. If clicking inside user dropdown menu (e.g. Logout button), let the form submit proceed!
        var userMenu = document.getElementById('userDropdownMenu');
        if (userMenu && userMenu.contains(e.target)) {
            return;
        }

        // 2. Mobile burger button click
        var menuBtn = e.target.closest('.mobile-menu-btn');
        if (menuBtn) {
            toggleMobileNav(e);
            return;
        }

        // 3. User dropdown toggle button click
        var userToggle = e.target.closest('.user-dropdown-toggle');
        if (userToggle) {
            toggleUserDropdown(e);
            return;
        }

        // 4. Dismiss mobile nav when clicking outside site header
        var header = document.querySelector('.site-header');
        if (header && header.classList.contains('nav-open')) {
            if (!header.contains(e.target)) {
                header.classList.remove('nav-open');
            }
        }

        // 5. Dismiss user dropdown menu when clicking outside user dropdown
        if (userMenu && userMenu.style.display === 'block') {
            var userDrop = document.querySelector('.user-dropdown');
            if (!userDrop || !userDrop.contains(e.target)) {
                userMenu.style.display = 'none';
            }
        }
    });

    // 6. Highlight active navigation tab based on URL path
    function initActiveNav() {
        var path = (window.location.pathname || '').toLowerCase();
        var navLinks = document.querySelectorAll('.site-header .nav-links a');
        if (!navLinks || navLinks.length === 0) return;

        var activeTarget = 'home';
        if (path.indexOf('device') !== -1 || path.indexOf('detail') !== -1) {
            activeTarget = 'devices';
        } else if (path.indexOf('pricing') !== -1) {
            activeTarget = 'pricing';
        } else if (path.indexOf('contact') !== -1) {
            activeTarget = 'contact';
        }

        navLinks.forEach(function (link) {
            var href = (link.getAttribute('href') || '').toLowerCase();
            var isActive = false;
            if (activeTarget === 'devices' && (href.indexOf('devices') !== -1 || href.indexOf('detail') !== -1)) {
                isActive = true;
            } else if (activeTarget === 'pricing' && href.indexOf('pricing') !== -1) {
                isActive = true;
            } else if (activeTarget === 'contact' && href.indexOf('contact') !== -1) {
                isActive = true;
            } else if (activeTarget === 'home' && (href.indexOf('index') !== -1 || href.endsWith('/faces/client/pages/index.xhtml') || href.endsWith('/faces/index.xhtml') || href.endsWith('/'))) {
                isActive = true;
            }

            if (isActive) {
                link.classList.add('active');
                link.setAttribute('aria-current', 'page');
            } else {
                link.classList.remove('active');
                link.removeAttribute('aria-current');
            }
        });
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initActiveNav);
    } else {
        initActiveNav();
    }
})();
