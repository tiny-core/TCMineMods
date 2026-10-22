  // ===== Terminal (3 × 1 × 2 no jogo; modelo de 2 blocos centrado): mesa de metal com hologramas =====
  MODELS.push(Object.assign({ id: 'terminal', name: 'Terminal', color: C.cyan, size: 'modelo 32 × 32 × 16 px, centrado; ocupa 3 × 1 × 2 blocos', radius: 2.4, target: 1, states: ['Ligado', 'Em uso', 'Sem energia'],
    info: {
      parts: ['<b>Mesa baixa de metal</b> (família do Núcleo): grelhas, janela de luz e filetes no rodapé',
        '<b>Consola inclinada</b> com <b>teclado holográfico</b> (teclas de luz) e um <b>cubo de dados</b> a girar por cima',
        '<b>2 pilones</b> atrás com linhas de luz e uma <b>ponte projetora</b> no alto',
        '<b>Holograma panorâmico</b>: painel central e dois laterais curvados para o jogador, com a grade da rede a correr, nas cores da paleta',
        '<b>Feixes de projeção</b> translúcidos da ponte até os painéis'],
      anim: ['Em uso: os painéis aproximam-se do jogador, a grade corre mais depressa e as teclas piscam',
        '<b>Sem energia</b>: hologramas, feixes, teclas e cubo somem; só a estrutura fica, apagada',
        '<b>Surgimento</b>: a ponte acende, os feixes abrem-se em leque, o painel central estica-se do meio para os lados, os laterais desdobram-se e as teclas acendem fila a fila'],
      tech: ['No jogo: principal embaixo no meio + 5 partes (3 × 2), como já está no código da fatia 1.5', 'Mesa, pilones e ponte em JSON; hologramas, feixes, teclas e cubo no BlockEntityRenderer (textura dinâmica de baixa resolução)']
    },
    build() {
      const g = new THREE.Group(), r = rng(818), strips = [], wins = [];
      const sm = () => { const m = glow('#D946EF'); strips.push(m); return m; };
      const wm = () => { const m = glow('#38BDF8'); wins.push(m); return m; };
      cbox(g, -15.5, 0, -7.5, 15.5, 1.2, 7.5, 'dark', 1.5);
      cbox(g, -14.5, 1.2, -6.8, 14.5, 7.5, 6.8, 'metal', 1.5);
      cbox(g, -15, 7.5, -7.2, 15, 8.2, 7.2, 'frame', 1.5);
      [-1, 1].forEach(s => { bx(g, s * 4, 2, 6.8, s * 12.5, 6.6, 7, 'vent'); bx(g, s * 13.5, 2, -4.5, s * 14.6, 6.6, 4.5, 'vent'); });
      box(g, -3, 2.5, 6.8, 3, 6, 7.05, 'panel'); box(g, -2.3, 3.2, 7.05, 2.3, 5.3, 7.25, wm());
      box(g, -14, .45, 7.45, 14, .8, 7.6, sm()); box(g, -14, .45, -7.6, 14, .8, -7.45, sm());
      [[-1, -1], [1, -1], [-1, 1], [1, 1]].forEach(([sx, sz]) => { const e = new THREE.Group(); e.position.set(sx * 14.05 * P, 0, sz * 6.35 * P); e.rotation.y = sx * sz > 0 ? -Math.PI / 4 : Math.PI / 4; g.add(e); box(e, -.45, 1.6, -.15, .45, 7.2, .2, sm()); });
      // consola inclinada com teclado holográfico
      const desk = new THREE.Group(); desk.position.set(0, 8.2 * P, 6.5 * P); desk.rotation.x = Math.PI / 9; g.add(desk);
      box(desk, -13, 0, -8, 13, 1.2, 0, 'frame'); box(desk, -13, 1, -.5, 13, 1.35, 0, sm());
      box(desk, -11, 1.2, -7, 11, 1.3, -1.5, 'panel');
      const keys = [];
      for (let row = 0; row < 3; row++) for (let col = 0; col < 12; col++) { const x = -9.6 + col * 1.75, z = -6.2 + row * 1.6, m = glow('#38BDF8'); box(desk, x - .6, 1.3, z - .5, x + .6, 1.55, z + .5, m); keys.push({ m, row, col }); }
      const cube = new THREE.Group(); cube.position.set(0, 13 * P, 3 * P); g.add(cube); const cubeM = glow('#38BDF8');
      [-1, 1].forEach(a => [-1, 1].forEach(b => { const k = 1.2; box(cube, a * k - .12, -k, b * k - .12, a * k + .12, k, b * k + .12, cubeM); box(cube, -k, a * k - .12, b * k - .12, k, a * k + .12, b * k + .12, cubeM); box(cube, a * k - .12, b * k - .12, -k, a * k + .12, b * k + .12, k, cubeM); }));
      // pilones e ponte projetora
      [-1, 1].forEach(s => {
        cbox(g, s * 13.5 - 1.6, 8.2, -6.8, s * 13.5 + 1.6, 29, -2.6, 'frame', .6);
        bx(g, s * 11.85, 9.5, -5.2, s * 11.95, 28, -4.2, sm());
        [13, 19, 25].forEach(y => bx(g, s * 11.6, y, -7.1, s * 15.4, y + .7, -2.3, 'metal'));
      });
      box(g, -12, 28, -6.4, 12, 30, -3, 'frame'); box(g, -12.2, 29.7, -6.6, 12.2, 30.2, -2.8, 'metal');
      const slit = wm(); box(g, -10, 27.8, -5.2, 10, 28, -4.2, slit);
      // holograma: textura desenhada com a paleta
      const hc = document.createElement('canvas'); hc.width = 64; hc.height = 32; const hx = hc.getContext('2d');
      const htex = new THREE.CanvasTexture(hc); htex.magFilter = htex.minFilter = THREE.NearestFilter; htex.wrapS = htex.wrapT = THREE.RepeatWrapping;
      let drawnFor = '';
      function drawHolo(pal) {
        const key = pal.name; if (key === drawnFor) return; drawnFor = key;
        hx.clearRect(0, 0, 64, 32); hx.fillStyle = 'rgba(56,189,248,0.10)'; hx.fillRect(0, 0, 64, 32);
        const rr = rng(5); for (let i = 1; i < 63; i += 4) for (let j = 2; j < 31; j += 4) { const v = rr(); hx.fillStyle = v > .85 ? pal.strip : v > .5 ? pal.win : 'rgba(255,255,255,0.25)'; hx.globalAlpha = v > .5 ? .9 : .5; hx.fillRect(i, j, 3, 3); }
        hx.globalAlpha = 1; hx.fillStyle = pal.win; hx.fillRect(0, 0, 64, 1); hx.fillRect(0, 31, 64, 1); hx.fillStyle = pal.strip; hx.fillRect(2, 1, 14, 1);
        htex.needsUpdate = true;
      }
      const hm = new THREE.MeshBasicMaterial({ map: htex, transparent: true, depthWrite: false, blending: THREE.AdditiveBlending, side: THREE.DoubleSide });
      const holo = new THREE.Group(); holo.position.set(0, 19.5 * P, -3 * P); g.add(holo);
      const center = new THREE.Mesh(new THREE.PlaneGeometry(16 * P, 11 * P), hm); holo.add(center);
      const wings = [[-8, 1], [8, -1]].map(([x, s]) => { const piv = new THREE.Group(); piv.position.x = x * P; holo.add(piv); const pl = new THREE.Mesh(new THREE.PlaneGeometry(7 * P, 10 * P), hm); pl.position.x = -s * 3.5 * P; piv.add(pl); return { piv, s }; });
      const beamM = new THREE.MeshBasicMaterial({ color: '#38BDF8', transparent: true, opacity: .07, depthWrite: false, blending: THREE.AdditiveBlending, side: THREE.DoubleSide });
      const fan = new THREE.Mesh(new THREE.PlaneGeometry(20 * P, 3.5 * P), beamM); fan.rotation.x = -Math.PI / 2.4; fan.position.set(0, 26.4 * P, -3.6 * P); g.add(fan);
      const clock = appearClock(2.4);
      return { group: g, update(t, st, _f, variant) {
        const pal = CORE_PALETTES[variant | 0] || CORE_PALETTES[0], off = st === 2, use = st === 1;
        const p = clock(t, off), sL = smooth(p, 0, .2), sFan = smooth(p, .15, .4), sCenter = smooth(p, .3, .6), sWings = smooth(p, .5, .8), sKeys = smooth(p, .6, 1);
        const lc = (c, k = sL) => off ? '#1E1B2E' : '#' + new THREE.Color('#1E1B2E').lerp(new THREE.Color(c), k).getHexString();
        strips.forEach(m => m.color.set(lc(pal.strip))); wins.forEach(m => m.color.set(lc(pal.win)));
        drawHolo(pal);
        fan.visible = !off && sFan > 0; fan.scale.x = Math.max(.001, sFan); beamM.color.set(pal.win); beamM.opacity = .07 + .03 * Math.sin(t * 5);
        holo.visible = !off && sCenter > 0; center.scale.x = Math.max(.001, sCenter);
        wings.forEach(w => { w.piv.visible = sWings > 0; w.piv.rotation.y = w.s * (Math.PI / 7) * sWings - w.s * (1 - sWings) * Math.PI / 2; w.piv.scale.setScalar(Math.max(.001, sWings)); });
        holo.position.z = (use ? -1.6 + .3 * Math.sin(t * 3) : -3) * P;
        hm.opacity = (.75 + .15 * Math.sin(t * 23) * Math.sin(t * 3)) * sCenter; htex.offset.y = (t * (use ? .35 : .12)) % 1;
        keys.forEach(k => { const on = !off && sKeys * 3 > k.row; k.m.color.set(!on ? '#1E1B2E' : use && Math.sin(t * 9 + k.col * 1.7 + k.row) > .6 ? pal.hi : (k.col + k.row) % 5 === 0 ? pal.strip : pal.win); });
        cube.visible = !off && sKeys > 0; cube.scale.setScalar(Math.max(.001, sKeys)); cube.rotation.set(t * .7, t * 1.1, 0); cube.position.y = (13 + .4 * Math.sin(t * 1.8)) * P; cubeM.color.set(pal.hi);
      } };
    }
  }, PALETTE_ROW));

