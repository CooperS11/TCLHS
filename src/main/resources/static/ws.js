// ws.js — self-contained STOMP over SockJS
// Just include this file. It loads its own dependencies and connects automatically.
// Usage on any page:
//   ws.subscribe('/topic/something', function(data) { ... });
//   ws.send('/destination', { key: 'value' });

(function (global) {
  let client    = null;
  let connected = false;
  const pending = []; // subscriptions queued before the connection is ready

  // ── Core functions ────────────────────────────────────────────

  function connect() {
    const socket = new SockJS('/ws');
    client = Stomp.over(socket);
    client.debug = () => {}; // silence debug logs

    client.connect({}, function () {
      connected = true;
      console.info('[ws] connected');
      // Flush any subscriptions registered before connect completed
      pending.forEach(({ topic, cb }) => client.subscribe(topic, cb));
      pending.length = 0;
    }, function (err) {
      connected = false;
      console.warn('[ws] lost connection, retrying in 3s…', err);
      setTimeout(connect, 3000);
    });
  }

  /**
   * Subscribe to a broker topic. Safe to call immediately — queued until connected.
   * @param {string}   topic     e.g. '/topic/tutors'
   * @param {Function} callback  receives the parsed message body
   */
  function subscribe(topic, callback) {
    const handler = function (frame) {
      try { callback(JSON.parse(frame.body)); }
      catch (_) { callback(frame.body); }
    };
    if (connected) client.subscribe(topic, handler);
    else pending.push({ topic, cb: handler });
  }

  /**
   * Send a message to the server (/app prefix added automatically).
   * @param {string} destination  e.g. '/match/request'
   * @param {object} body
   */
  function send(destination, body) {
    if (!connected) { console.warn('[ws] send called before connection was ready'); return; }
    client.send('/app' + destination, {}, JSON.stringify(body));
  }

  function disconnect() {
    if (client) client.disconnect(function () { console.info('[ws] disconnected'); });
    connected = false;
  }

  // Expose API immediately so pages can call ws.subscribe() right away
  global.ws = { subscribe, send, disconnect };

  // ── Self-load dependencies then auto-connect ──────────────────

  function loadScript(src, onload) {
    const s  = document.createElement('script');
    s.src    = src;
    s.onload = onload;
    document.head.appendChild(s);
  }

  loadScript(
    'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js',
    function () {
      loadScript(
        'https://cdn.jsdelivr.net/npm/stomp-websocket@2.3.4/lib/stomp.min.js',
        function () { connect(); }
      );
    }
  );

})(window);
