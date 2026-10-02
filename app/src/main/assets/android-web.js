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
    var apkDownload = document.getElementById('btn-android-download');
    if (apkDownload) apkDownload.hidden = true;
    if (!window.__tonggeThemeObserver) {
        window.__tonggeThemeObserver = new MutationObserver(followSystem);
        window.__tonggeThemeObserver.observe(document.documentElement,
            { attributes: true, attributeFilter: ['data-theme'] });
    }
    var menu = document.getElementById('more-menu');
    function serverLink(id, className) {
        var link = document.createElement('a');
        link.id = id;
        link.className = className;
        link.href = 'tongge://server';
        link.textContent = '服务器设置';
        return link;
    }
    if (menu && !document.getElementById('android-server')) {
        var server = serverLink('android-server', 'menu-item');
        server.innerHTML = '<span class="menu-ico"><svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="2" y="3" width="20" height="8" rx="2"/><rect x="2" y="13" width="20" height="8" rx="2"/><path d="M6 7h.01M6 17h.01"/></svg></span>服务器设置';
        menu.insertBefore(server, document.getElementById('btn-logout'));
    }
    var auth = document.getElementById('screen-auth');
    if (auth && !document.getElementById('android-auth-actions')) {
        var row = document.createElement('div');
        row.id = 'android-auth-actions';
        row.style.cssText = 'display:flex;gap:12px;justify-content:center;flex-wrap:wrap;margin-top:18px';
        var offline = document.createElement('button');
        offline.type = 'button';
        offline.className = 'btn ghost small';
        offline.textContent = '本地快速比对';
        offline.addEventListener('click', function () { document.getElementById('btn-local').click(); });
        row.appendChild(offline);
        var authScan = document.createElement('a');
        authScan.className = 'btn ghost small';
        authScan.href = 'tongge://scan';
        authScan.textContent = '扫一扫';
        row.appendChild(authScan);
        row.appendChild(serverLink('android-auth-server', 'btn ghost small'));
        auth.appendChild(row);
    }
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
