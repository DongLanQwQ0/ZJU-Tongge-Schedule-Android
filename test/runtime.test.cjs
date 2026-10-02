const {test} = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const vm = require('node:vm');
const path = require('node:path');
const assets = path.join(__dirname, '../app/src/main/assets/web');
const apiSource = fs.readFileSync(path.join(assets, 'api.js'), 'utf8');
const appSource = fs.readFileSync(path.join(assets, 'app.js'), 'utf8');

function runtime(base, storage = new Map(), fetch = async () => ({ok:true, text:async () => '{}'})) {
    const context = vm.createContext({window:base ? {__tonggeServerBase:base} : {},
        document:{baseURI:'https://web.example.com/tongge/?code=12345678'}, URL, fetch,
        localStorage:{getItem:k => storage.get(k) ?? null, setItem:(k,v) => storage.set(k,v), removeItem:k => storage.delete(k)}});
    vm.runInContext(apiSource, context);
    return context.window.API;
}

test('API routes to configured host, port and deployment path', async () => {
    let observed;
    const api = runtime('https://new.example.com:8443/schedule/', new Map(), async (url, options) => {
        observed = {url, options}; return {ok:true, text:async () => '{}'};
    });
    api.setToken('test-token');
    await api.request('api/groups', 'POST', {name:'test'});
    assert.equal(observed.url, 'https://new.example.com:8443/schedule/api/groups');
    assert.equal(observed.options.headers.Authorization, 'Bearer test-token');
    assert.equal(observed.options.body, '{"name":"test"}');
});

test('logins are isolated even across different deployments on the same host', () => {
    const storage = new Map();
    runtime('https://example.com/one/', storage).setToken('first-token');
    assert.equal(runtime('https://example.com/two/', storage).getToken(), '');
    const first = runtime('https://example.com/one/', storage);
    assert.equal(first.getToken(), 'first-token');
    first.setToken('');
    assert.equal(runtime('https://example.com/one/', storage).getToken(), '');
});

test('web deployment keeps its existing relative API behavior', async () => {
    let url;
    await runtime(null, new Map(), async value => {
        url = value; return {ok:true, text:async () => '{}'};
    }).meta();
    assert.equal(url, 'https://web.example.com/tongge/api/meta');
});

test('late 401 from an old request cannot delete a newly established login', async () => {
    let finish;
    const api = runtime('https://example.com/', new Map(), () => new Promise(resolve => {finish = resolve;}));
    api.setToken('old-token');
    const pending = api.me();
    api.setToken('new-token');
    finish({ok:false, status:401, text:async () => '{"error":"expired"}'});
    await assert.rejects(pending);
    assert.equal(api.getToken(), 'new-token');
});

for (const result of ['success', 'offline', 'expired']) {
    test('saved login ' + result + ' does not replace an opened offline comparison', async () => {
        const match = appSource.match(/async function restoreSession\(\) \{[\s\S]*?\n    \}\n\n    \/\/ -+ 启动/);
        assert.ok(match, 'real startup function exists');
        let finish, fail;
        const state = {screen:null, me:null};
        const context = vm.createContext({state, API:{getToken:() => 'saved-token',
            me:() => new Promise((resolve,reject) => {finish=resolve; fail=reject;})},
            show:id => {state.screen=id;}, goHome:async () => {state.screen='home';}, toast:() => {}});
        vm.runInContext(match[0] + '\npending=restoreSession();', context);
        assert.equal(state.screen, 'auth');
        state.screen = 'local';
        if (result === 'success') finish({nickname:'test'});
        else fail(Object.assign(new Error(result), {status:result === 'offline' ? 0 : 401}));
        await context.pending;
        assert.equal(state.screen, 'local');
    });
}
