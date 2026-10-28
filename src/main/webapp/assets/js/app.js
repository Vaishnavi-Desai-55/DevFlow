/* DevFlow shared behaviour: menu, confirmations, toasts, auto-submit selects */
(function () {
  var sidebar = document.getElementById('sidebar'), toggle = document.getElementById('menuToggle');
  if (toggle && sidebar) {
    toggle.addEventListener('click', function () { sidebar.classList.toggle('open'); });
    document.addEventListener('click', function (e) {
      if (sidebar.classList.contains('open') && !sidebar.contains(e.target) && e.target !== toggle) sidebar.classList.remove('open');
    });
  }

  // <form data-confirm="Are you sure?"> asks before submitting
  document.querySelectorAll('form[data-confirm]').forEach(function (f) {
    f.addEventListener('submit', function (e) { if (!window.confirm(f.dataset.confirm)) e.preventDefault(); });
  });

  // <select data-autosubmit> submits its form when changed
  document.querySelectorAll('select[data-autosubmit]').forEach(function (s) {
    s.addEventListener('change', function () { s.form.submit(); });
  });

  // <button data-hide="#id"> hides an element (used by "Ignore" on the priority suggestion)
  document.querySelectorAll('[data-hide]').forEach(function (b) {
    b.addEventListener('click', function () { var t = document.querySelector(b.dataset.hide); if (t) t.classList.add('hidden'); });
  });

  // Subtask parent field only matters for the Subtask type
  var type = document.getElementById('issueType'), parent = document.getElementById('parentField');
  if (type && parent) {
    var sync = function () { parent.classList.toggle('hidden', type.value !== 'SUBTASK'); };
    type.addEventListener('change', sync); sync();
  }

  // Success messages fade away
  var flash = document.querySelector('.flash-success');
  if (flash) setTimeout(function () { flash.style.transition = 'opacity .5s'; flash.style.opacity = '0'; }, 4500);

  // Toast notifications: toast('Saved', 'success')
  window.toast = function (message, type) {
    var box = document.getElementById('toasts');
    if (!box) { box = document.createElement('div'); box.id = 'toasts'; document.body.appendChild(box); }
    var t = document.createElement('div');
    t.className = 'toast ' + (type || '');
    t.textContent = message;               // textContent (not innerHTML) so text can never run as HTML
    box.appendChild(t);
    setTimeout(function () { t.remove(); }, 4000);
  };
})();
