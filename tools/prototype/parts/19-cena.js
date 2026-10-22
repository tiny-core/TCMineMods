  // ---------- cena ----------
  const canvas = document.getElementById('view');
  const renderer = new THREE.WebGLRenderer({ canvas, antialias: true, alpha: true });
  renderer.setPixelRatio(Math.min(2, window.devicePixelRatio || 1));
  const scene = new THREE.Scene();
  const camera = new THREE.PerspectiveCamera(40, 1, .05, 100); VIEW.camera = camera;
  const controls = new THREE.OrbitControls(camera, canvas); controls.enableDamping = true; controls.dampingFactor = .08;
  scene.add(new THREE.HemisphereLight(0xcfe8ff, 0x1a1208, .75));
  const sun = new THREE.DirectionalLight(0xffffff, .75); sun.position.set(3, 6, 4); scene.add(sun);
  const fill = new THREE.DirectionalLight(0x22b8e8, .25); fill.position.set(-4, 2, -3); scene.add(fill);

  const floor = new THREE.Group(); scene.add(floor);
  const grid = new THREE.GridHelper(9, 9, 0x2e3340, 0x1f232c); grid.position.y = 0.001; floor.add(grid);
  const ground = new THREE.Mesh(new THREE.PlaneGeometry(9, 9), new THREE.MeshLambertMaterial({ color: 0x101218 })); ground.rotation.x = -Math.PI / 2; floor.add(ground);

  // Manequim genérico de 2 blocos de altura (32 px) para dar escala.
  const dummy = new THREE.Group(); scene.add(dummy);
  const dm = flat('#6B7280'), dh = flat('#9CA3AF');
  [[-4, 0, -2, 0, 12, 2], [0, 0, -2, 4, 12, 2]].forEach(b => box(dummy, ...b, dm));
  box(dummy, -4, 12, -2, 4, 24, 2, dh); box(dummy, -8, 12, -2, -4, 24, 2, dm); box(dummy, 4, 12, -2, 8, 24, 2, dm); box(dummy, -4, 24, -4, 4, 32, 4, dh);

  let current = null, active = null, state = 0, level = 1, level2 = 0, spin = true, showDummy = true;
  const chips = document.getElementById('chips'), controlsEl = document.getElementById('controls'), info = document.getElementById('info');

  MODELS.forEach((m, i) => {
    const b = document.createElement('button'); b.className = 'chip'; b.type = 'button'; b.id = 'chip-' + m.id;
    b.innerHTML = `<span class="dot" style="color:${m.color}" aria-hidden="true"></span>${m.name}`;
    b.addEventListener('click', () => select(i)); chips.appendChild(b);
  });

  function renderControls(m) {
    controlsEl.innerHTML = '';
    const lab = document.createElement('span'); lab.className = 'group-label'; lab.textContent = 'Estado'; controlsEl.appendChild(lab);
    m.states.forEach((s, i) => { const b = document.createElement('button'); b.type = 'button'; b.className = 'ctl'; b.id = 'st-' + i; b.textContent = s; b.setAttribute('aria-pressed', String(i === state)); b.addEventListener('click', () => { state = i; renderControls(m); }); controlsEl.appendChild(b); });
    if (m.levels) {
      const ll = document.createElement('span'); ll.className = 'group-label'; ll.style.marginLeft = '10px'; ll.textContent = m.levelLabel; controlsEl.appendChild(ll);
      m.levels.forEach((v, i) => { const b = document.createElement('button'); b.type = 'button'; b.className = 'ctl'; b.id = 'lv-' + i; b.textContent = m.levelNames ? m.levelNames[i] : Math.round(v * 100) + '%'; b.setAttribute('aria-pressed', String(i === level)); b.addEventListener('click', () => { level = i; renderControls(m); }); controlsEl.appendChild(b); });
    }
    if (m.levels2) {
      const l2 = document.createElement('span'); l2.className = 'group-label'; l2.style.marginLeft = '10px'; l2.textContent = m.levelLabel2; controlsEl.appendChild(l2);
      m.levels2.forEach((v, i) => { const b = document.createElement('button'); b.type = 'button'; b.className = 'ctl'; b.textContent = m.levelNames2[i]; b.setAttribute('aria-pressed', String(i === level2)); b.addEventListener('click', () => { level2 = i; renderControls(m); }); controlsEl.appendChild(b); });
    }
    const lab2 = document.createElement('span'); lab2.className = 'group-label'; lab2.style.marginLeft = '10px'; lab2.textContent = 'Vista'; controlsEl.appendChild(lab2);
    [['Girar', () => spin, v => { spin = v; controls.autoRotate = v; }], ['Manequim', () => showDummy, v => { showDummy = v; dummy.visible = v; }]].forEach(([label, get, set]) => {
      const b = document.createElement('button'); b.type = 'button'; b.className = 'ctl'; b.textContent = label; b.setAttribute('aria-pressed', String(get()));
      b.addEventListener('click', () => { set(!get()); b.setAttribute('aria-pressed', String(get())); }); controlsEl.appendChild(b);
    });
  }

  function renderInfo(m) {
    const li = a => a.map(x => `<li>${x}</li>`).join('');
    info.innerHTML = `<h2>TC ${m.name.replace(' (item)', '')}</h2><div class="size">${m.size}</div>
      <h3>Peças</h3><ul>${li(m.info.parts)}</ul>
      <h3>Animação</h3><ul>${li(m.info.anim)}</ul>
      <h3>Como fica no código</h3><ul>${li(m.info.tech)}</ul>`;
  }

  function select(i) {
    const m = MODELS[i];
    if (current) scene.remove(current.group);
    current = m.build(); scene.add(current.group); active = m; state = 0; level = m.levels ? (m.levelStart ?? 1) : 0; if (m.level2Start != null) level2 = m.level2Start;
    document.querySelectorAll('.chip').forEach((c, k) => c.setAttribute('aria-pressed', String(k === i)));
    const big = m.id === 'nucleo';
    const dx = { nucleo: 2.2, terminal: 2.1, formas: 2.1, painel: 2.2, manipulador: 1.2, tunel: 2.8, colapsador: 2.7, matriz: 1.6, bateria: 2.8, processador: 2.8, regulador: 2.8, interface: 1.7, interface_painel: 1.7 }[m.id] || 1.1; dummy.position.set(dx, 0, big ? .6 : .2);
    const h = m.target;
    controls.target.set(0, h, 0); camera.position.set(m.radius * 1.15, h + m.radius * .45, m.radius * 1.45);
    controls.autoRotate = spin; controls.autoRotateSpeed = 1.2; controls.update();
    document.getElementById('hint').textContent = '1 quadrado do chão = 1 bloco · manequim = 2 blocos';
    renderControls(m); renderInfo(m);
    try { localStorage.setItem('tcq-model', m.id); } catch (e) {}
  }

  function resize() { const r = canvas.parentElement.getBoundingClientRect(); renderer.setSize(r.width, r.height, false); camera.aspect = r.width / r.height; camera.updateProjectionMatrix(); }
  window.addEventListener('resize', resize);

  const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches;
  if (reduce) spin = false;
  const clock = new THREE.Clock();
  function loop() { requestAnimationFrame(loop); const t = reduce ? 1.2 : clock.getElapsedTime(); if (current) current.update(t, state, active && active.levels ? active.levels[level] : 0, active && active.levels2 ? active.levels2[level2] : 0); controls.update(); renderer.render(scene, camera); }

  // Gancho para tools/sheet.js (render sem interface)
  window.__tcq = { MODELS, TEX, P, select, camera, controls, frame(t, st, v) { if (current) current.update(t, st, 0, v | 0); controls.update(); renderer.render(scene, camera); return canvas.toDataURL('image/png'); } };
  let start = 0; try { const saved = localStorage.getItem('tcq-model'); const k = MODELS.findIndex(m => m.id === saved); if (k >= 0) start = k; } catch (e) {}
  resize(); select(start); loop();
})();
</script>

</body></html>