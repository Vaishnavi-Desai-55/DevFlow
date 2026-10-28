/* Dashboard and analytics charts. Data comes from the server (MySQL) as JSON and refreshes every 20 seconds. */
(function () {
  var root = document.getElementById('charts');
  if (!root || typeof Chart === 'undefined') return;
  var url = root.dataset.url, charts = {};
  var colors = ['#6366f1', '#0ea5e9', '#f59e0b', '#8b5cf6', '#10b981', '#ef4444', '#14b8a6', '#f97316', '#64748b', '#ec4899'];
  var statusColors = ['#94a3b8', '#3b82f6', '#8b5cf6', '#f59e0b', '#10b981'];
  var priorityColors = ['#cbd5e1', '#94a3b8', '#38bdf8', '#fb923c', '#ef4444'];

  function upsert(id, type, labels, values, palette, extra) {
    var canvas = document.getElementById(id);
    if (!canvas) return;
    if (charts[id]) {
      charts[id].data.labels = labels;
      charts[id].data.datasets[0].data = values;
      charts[id].update();
      return;
    }
    var options = Object.assign({ responsive: true, maintainAspectRatio: false, plugins: { legend: { display: type === 'doughnut', position: 'bottom' } } }, extra || {});
    charts[id] = new Chart(canvas, {
      type: type,
      data: { labels: labels, datasets: [{ data: values, backgroundColor: palette, borderRadius: type === 'bar' ? 6 : 0, borderWidth: 0 }] },
      options: options
    });
  }

  function render(d) {
    document.querySelectorAll('[data-stat]').forEach(function (el) { if (d[el.dataset.stat] !== undefined) el.textContent = d[el.dataset.stat]; });
    upsert('chartStatus', 'doughnut', Object.keys(d.byStatus), Object.values(d.byStatus), statusColors, { cutout: '62%' });
    upsert('chartPriority', 'bar', Object.keys(d.byPriority), Object.values(d.byPriority), priorityColors,
      { scales: { y: { beginAtZero: true, ticks: { precision: 0 } } } });
    upsert('chartAssignee', 'bar', Object.keys(d.byAssignee), Object.values(d.byAssignee), colors,
      { indexAxis: 'y', scales: { x: { beginAtZero: true, ticks: { precision: 0 } } } });
    upsert('chartProjects', 'bar', d.projects.map(function (p) { return p.key; }), d.projects.map(function (p) { return p.percent; }), colors,
      { scales: { y: { beginAtZero: true, max: 100, ticks: { callback: function (v) { return v + '%'; } } } },
        plugins: { legend: { display: false }, tooltip: { callbacks: { label: function (c) { var p = d.projects[c.dataIndex]; return p.done + ' of ' + p.total + ' done (' + p.percent + '%)'; } } } } });
  }

  function load() {
    fetch(url, { headers: { 'X-Requested-With': 'fetch' } })
      .then(function (r) { if (!r.ok) throw new Error('bad status'); return r.json(); })
      .then(render)
      .catch(function () { /* keep the last numbers on screen if a refresh fails */ });
  }
  load();
  setInterval(load, 20000);
})();
