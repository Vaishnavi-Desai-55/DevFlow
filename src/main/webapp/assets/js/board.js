/* Kanban board: drag and drop + filters. The SERVER decides if a move is allowed (WorkflowService). */
(function () {
  var board = document.getElementById('board');
  if (!board) return;
  var csrf = document.querySelector('meta[name="csrf-token"]').content;
  var url = board.dataset.transitionUrl;
  var canMove = board.dataset.canMove === 'true';
  var dragged = null;

  function refresh() {
    document.querySelectorAll('.column').forEach(function (col) {
      var body = col.querySelector('.column-body');
      var visible = body.querySelectorAll('.issue-card:not(.hidden)').length;
      col.querySelector('.count').textContent = body.querySelectorAll('.issue-card').length;
      body.classList.toggle('is-empty', visible === 0);
    });
  }

  if (canMove) {
    document.querySelectorAll('.issue-card').forEach(function (card) {
      card.addEventListener('dragstart', function (e) {
        dragged = card; card.classList.add('dragging');
        e.dataTransfer.effectAllowed = 'move'; e.dataTransfer.setData('text/plain', card.dataset.id);
      });
      card.addEventListener('dragend', function () {
        card.classList.remove('dragging');
        document.querySelectorAll('.column').forEach(function (c) { c.classList.remove('drop-ok'); });
      });
    });

    document.querySelectorAll('.column').forEach(function (col) {
      col.addEventListener('dragover', function (e) { if (dragged) { e.preventDefault(); col.classList.add('drop-ok'); } });
      col.addEventListener('dragleave', function (e) { if (!col.contains(e.relatedTarget)) col.classList.remove('drop-ok'); });
      col.addEventListener('drop', function (e) {
        e.preventDefault(); col.classList.remove('drop-ok');
        var card = dragged; dragged = null;
        if (!card || card.closest('.column') === col) return;

        card.classList.add('loading');                           // loading indicator while the server checks
        fetch(url, {
          method: 'POST',
          headers: { 'X-CSRF-Token': csrf, 'X-Requested-With': 'fetch', 'Content-Type': 'application/x-www-form-urlencoded' },
          body: new URLSearchParams({ issueId: card.dataset.id, toStatus: col.dataset.status })
        }).then(function (r) {
          return r.json().then(function (data) { return { ok: r.ok && data.ok, data: data }; })
                         .catch(function () { return { ok: false, data: { message: r.status === 401 ? 'Your session expired. Please sign in again.' : 'Unexpected server response.' } }; });
        }).then(function (res) {
          if (res.ok) {                                          // move the card ONLY after the server said yes
            col.querySelector('.column-body').prepend(card);
            window.toast(res.data.message, 'success');
          } else {
            window.toast(res.data.message || 'That move is not allowed.', 'error');
          }
        }).catch(function () {
          window.toast('Network error. The issue was not moved.', 'error');
        }).finally(function () { card.classList.remove('loading'); refresh(); });
      });
    });
  }

  // Client-side filters (search, type, priority, assignee)
  var q = document.getElementById('boardSearch'), type = document.getElementById('boardType'),
      prio = document.getElementById('boardPriority'), who = document.getElementById('boardAssignee');
  function applyFilters() {
    var text = (q.value || '').toLowerCase();
    document.querySelectorAll('.issue-card').forEach(function (c) {
      var ok = (!text || (c.dataset.key + ' ' + c.dataset.title).toLowerCase().indexOf(text) >= 0)
            && (!type.value || c.dataset.type === type.value)
            && (!prio.value || c.dataset.priority === prio.value)
            && (!who.value || c.dataset.assignee === who.value);
      c.classList.toggle('hidden', !ok);
    });
    refresh();
  }
  [q, type, prio, who].forEach(function (el) { if (el) el.addEventListener('input', applyFilters); });
  refresh();
})();
