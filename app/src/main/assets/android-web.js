(function () {
    function followSystem() {
        var dark = window.__tonggeSystemDark === true;
        var theme = dark ? 'dark' : 'light';
        if (document.documentElement.getAttribute('data-theme') !== theme)
            document.documentElement.setAttribute('data-theme', theme);
        document.documentElement.style.colorScheme = theme;
        var meta = document.querySelector('meta[name="theme-color"]');
        if (meta) meta.content = dark ? '#000000' : '#f2f2f7';
        try { localStorage.removeItem('dsh_theme'); } catch (e) {}
        var manual = document.getElementById('btn-theme');
        if (manual) manual.hidden = true;
    }
    followSystem();
    if (!window.__tonggeThemeObserver) {
        window.__tonggeThemeObserver = new MutationObserver(followSystem);
        window.__tonggeThemeObserver.observe(document.documentElement,
            { attributes: true, attributeFilter: ['data-theme'] });
    }
    var menu = document.getElementById('more-menu');
    if (menu && !document.getElementById('android-updates')) {
        var update = document.createElement('a');
        update.id = 'android-updates';
        update.className = 'menu-item';
        update.href = 'tongge://updates';
        update.innerHTML = '<span class="menu-ico"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 12a9 9 0 0 1 9-9 9.75 9.75 0 0 1 6.74 2.74L21 8M21 3v5h-5M21 12a9 9 0 0 1-9 9 9.75 9.75 0 0 1-6.74-2.74L3 16M8 16H3v5"/></svg></span>检查更新';
        menu.insertBefore(update, document.getElementById('btn-logout'));
    }
    if (document.getElementById('android-scan')) return;
    var actions = document.querySelector('.home-groups-actions');
    if (!actions) return;
    var scan = document.createElement('a');
    scan.id = 'android-scan';
    scan.className = 'btn ghost small';
    scan.href = 'tongge://scan';
    scan.title = '扫一扫';
    scan.setAttribute('aria-label', '扫一扫');
    scan.style.cssText = 'display:inline-flex;align-items:center;justify-content:center;min-width:40px;min-height:40px;padding:8px;box-sizing:border-box';
    // Lucide scan-line.
    scan.innerHTML = '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 7V4h3M17 4h3v3M20 17v3h-3M7 20H4v-3M7 12h10"/></svg>';
    actions.insertBefore(scan, actions.firstChild);
    var style = document.createElement('style');
    style.textContent = '.home-groups-head{flex-wrap:wrap}.home-groups-actions{flex-wrap:wrap}';
    document.head.appendChild(style);
})();
