  // ===== Matriz de Superposição 1 x 2: espinha de servidor com 5 células em berços magnéticos =====
  MODELS.push(Object.assign({ id: 'matriz', name: 'Matriz de Superposição', color: C.item, size: '1 × 2 blocos (16 × 32 × 16 px)', radius: 2.4, target: 1.05,
    states: [...TYPE.map(t => t[0]), 'Mista', 'Sem energia'], levels: [.1, .5, .85, 1], levelLabel: 'Ocupação',
    info: {
      parts: ['<b>Base baixa de metal</b> com grelhas e filetes de luz (família do Núcleo)',
        '<b>Espinha</b> atrás, como a de um rack de servidor, com linha de dados na frente e <b>medidores</b> de 10 segmentos dos dois lados',
        '<b>5 berços magnéticos</b>: cada um é um par de braços que sai da espinha e abraça uma célula pelos lados, com almofadas de luz',
        '<b>5 células</b> (4,2 px) espaçadas por toda a altura do bloco, a flutuar entre as almofadas, uma por Módulo; encaixe vazio = berço apagado, sem célula',
        '<b>Estado de superposição</b>: cada célula tem uma cópia fantasma que entra e sai de fase, deslocada e girada',
        'Conteúdo = <b>cubinhos na cor do tipo</b> que imitam o que está guardado: <b>itens</b> (laranja) empilhados numa grade que enche de baixo para cima, um de cada vez a saltar de lugar; <b>fluidos</b> (azul) em colunas que ondulam como água, com a superfície mais clara e bolhas; <b>químicos</b> (verde) soltos a vaguear e a bater nas paredes como gás',
        '<b>Coroa</b> no topo com luz de estado e um anel a flutuar'],
      anim: ['Células sobem e descem nos berços e giram devagar; os fantasmas cintilam fora de fase',
        'Pulsos de dados correm pela espinha entre as células; medidores na cor da paleta até 80%, âmbar até 95%, vermelho acima',
        '<b>Sem energia</b>: células, fantasmas, anel e pulsos somem; berços e espinha apagados',
        '<b>Surgimento</b>: a espinha acende de baixo para cima, as almofadas de cada berço acendem e a célula materializa-se entre elas, uma a uma'],
      tech: ['1 principal + 1 de estrutura (2 de altura)', 'Base, espinha e berços em JSON; células, fantasmas, conteúdo, pulsos e medidores no BlockEntityRenderer',
        'O tipo do módulo dá o conteúdo da célula; a paleta da rede (opção A) dá a cor das luzes']
    },
    build() {
      const g = new THREE.Group(), r = rng(515), strips = [];
      const sm = () => { const m = glow('#D946EF'); strips.push(m); return m; };
      slimBase(g, 3.5, sm);
      // espinha
      cbox(g, -2.2, 4.2, -7.6, 2.2, 30.8, -3.6, 'frame', .6);
      box(g, -1.4, 4.2, -3.6, 1.4, 30.8, -3.2, 'metal');
      const spineM = glow('#38BDF8'); box(g, -.35, 4.7, -3.2, .35, 30.3, -3.05, spineM);
      const segs = [];
      for (let i = 0; i < 10; i++) { const y = 5.2 + i * 2.5, m = glow('#22252E'); box(g, -2.4, y, -7, -2.2, y + 1.8, -4.2, m); box(g, 2.2, y, -7, 2.4, y + 1.8, -4.2, m); segs.push(m); }
      // coroa
      cbox(g, -3.2, 30.8, -8, 3.2, 32, -2.8, 'metal', .8); const status = glow('#4ADE80'); box(g, -1.2, 31.1, -2.8, 1.2, 31.7, -2.65, status);
      const crown = new THREE.Group(); crown.position.set(0, 31.4 * P, 1 * P); g.add(crown); const crownM = glow('#D946EF'); roundRing(crown, 2.2, .4, -.15, .15, crownM, 14);
      // berços e células
      const glassM = new THREE.MeshLambertMaterial({ color: '#C4B5FD', transparent: true, opacity: .16, depthWrite: false });
      const E = 2.1, CZ = 1.2, MINI = .62, N = 36;
      const TYPE_COLS = [['#C2410C', '#F97316', '#FDBA74'], ['#1D4ED8', '#60A5FA', '#DBEAFE'], ['#4D7C0F', '#A3E635', '#ECFCCB']];
      const cells = [6.6, 12, 17.4, 22.8, 28.2].map((y, i) => {
        // berço: braços que saem da espinha e envolvem a célula pelos lados
        const pads = glow('#22252E');
        [-1, 1].forEach(s => {
          box(g, s * 1.4, y - .6, -3.8, s * 4.3, y + .6, -2.6, 'metal');
          bx(g, s * 3.7, y - .8, -2.6, s * 5, y + .8, CZ + 1.4, 'frame');
          bx(g, s * 3.55, y - .6, -1.6, s * 3.7, y + .6, CZ + 1, pads);
        });
        const grp = new THREE.Group(); grp.position.set(0, y * P, CZ * P); g.add(grp);
        const spin = new THREE.Group(); grp.add(spin);
        const shell = box(spin, -E, -E, -E, E, E, E, glassM);
        const edgeM = glow('#D946EF');
        const edges = (parent, m, k) => [-1, 1].forEach(a => [-1, 1].forEach(b => { box(parent, a * k - .16, -k, b * k - .16, a * k + .16, k, b * k + .16, m); box(parent, -k, a * k - .16, b * k - .16, k, a * k + .16, b * k + .16, m); box(parent, a * k - .16, b * k - .16, -k, a * k + .16, b * k + .16, k, m); }));
        edges(spin, edgeM, E);
        const ghost = new THREE.Group(); grp.add(ghost);
        const ghostM = new THREE.MeshBasicMaterial({ color: '#38BDF8', transparent: true, opacity: .4, blending: THREE.AdditiveBlending, depthWrite: false }); edges(ghost, ghostM, E * .92);
        // conteúdo: N cubinhos na cor do tipo, posições calculadas a cada quadro
        const mini = new THREE.InstancedMesh(new THREE.BoxGeometry(MINI * P, MINI * P, MINI * P), new THREE.MeshBasicMaterial({ color: '#ffffff', transparent: true, opacity: 1 }), N);
        for (let k = 0; k < N; k++) mini.setColorAt(k, new THREE.Color('#ffffff'));
        spin.add(mini);
        const seeds = []; for (let k = 0; k < N; k++) seeds.push({ x: (r() - .5) * 3, y: (r() - .5) * 3, z: (r() - .5) * 3, vx: r() - .5, vy: r() - .5, vz: r() - .5, ph: r() * 6, shade: r() });
        const fl = halo('rgba(255,255,255,0.9)', .5); grp.add(fl);
        return { grp, spin, shell, edgeM, ghost, ghostM, mini, seeds, fl, pads, filled: true, y, i, hopAt: 0, hop: -1, hopFrom: 0, hopTo: 0 };
      });
      // Posições da grade 3×3×3 (itens empilhados de baixo para cima).
      const LAT = []; for (let yy = 0; yy < 3; yy++) for (let zz = 0; zz < 3; zz++) for (let xx = 0; xx < 3; xx++) LAT.push([(xx - 1) * 1.25, (yy - 1) * 1.25, (zz - 1) * 1.25]);
      const Mx = new THREE.Matrix4(), Cx = new THREE.Color(), lim = E - .55;
      const hidden = () => { Mx.makeScale(0, 0, 0); return Mx; };
      function fillCell(c, type, f, t, dt) {
        const cols = TYPE_COLS[type], n = c.mini.count;
        if (type === 0) { // itens: cubos numa grade que enche de baixo para cima; de vez em quando um salta para outro lugar
          const count = Math.max(2, Math.round(f * 27));
          if (t > c.hopAt) { c.hop = Math.floor(r() * count); c.hopFrom = t; c.hopAt = t + .7 + r(); c.hopTo = Math.floor(r() * count); }
          for (let k = 0; k < n; k++) {
            if (k >= count) { c.mini.setMatrixAt(k, hidden()); continue; }
            let [x, y, z] = LAT[k];
            if (k === c.hop) { const q = Math.min(1, (t - c.hopFrom) / .6), [tx, ty, tz] = LAT[c.hopTo]; x += (tx - x) * q; z += (tz - z) * q; y += (ty - y) * q + Math.sin(q * Math.PI) * 1.2; }
            const s = .9 + .1 * Math.sin(t * 3 + k);
            Mx.makeRotationY(t * .8 + k); Mx.scale(new THREE.Vector3(s, s, s)); Mx.setPosition(x * P, y * P, z * P); c.mini.setMatrixAt(k, Mx);
            c.mini.setColorAt(k, Cx.set(c.seeds[k].shade > .66 ? cols[2] : c.seeds[k].shade > .33 ? cols[1] : cols[0]));
          }
        } else if (type === 1) { // fluido: colunas de cubos cuja altura ondula; o topo é mais claro (superfície)
          const cols4 = 4, step = 1.05, base = -lim, levelMax = 2 * lim;
          let k = 0;
          for (let cx = 0; cx < cols4; cx++) for (let cz = 0; cz < cols4; cz++) {
            const x = (cx - 1.5) * step, z = (cz - 1.5) * step;
            const h = Math.max(.2, (.1 + .9 * f) * levelMax + .45 * Math.sin(t * 2.6 + cx * 1.2 + cz * .8) + .25 * Math.sin(t * 1.7 - cz * 1.4));
            const stack = Math.min(4, Math.ceil(h / step));
            for (let j = 0; j < 2 && k < n; j++, k++) {
              const isTop = j === 1 || stack <= 1, yy = isTop ? base + h - step / 2 : base + (h - step) / 2;
              const sy = isTop ? 1 : Math.max(.1, (h - step) / step);
              Mx.makeScale(1.1, j === 1 ? 1 : sy, 1.1); Mx.setPosition(x * P, (j === 1 ? yy : base + Math.max(.05, h - step) / 2) * P, z * P);
              if (j === 0 && stack <= 1) Mx.makeScale(0, 0, 0);
              c.mini.setMatrixAt(k, Mx); c.mini.setColorAt(k, Cx.set(j === 1 ? cols[2] : cols[1]));
            }
          }
          for (; k < n; k++) { // bolhas a subir
            const s = c.seeds[k], kk = (t * .45 + s.ph) % 1, lvl = (.1 + .9 * f) * levelMax;
            Mx.makeScale(.45, .45, .45); Mx.setPosition(s.x * .6 * P, (base + lvl * kk) * P, s.z * .6 * P); c.mini.setMatrixAt(k, kk < .95 ? Mx : hidden()); c.mini.setColorAt(k, Cx.set(cols[2]));
          }
        } else { // gás: cubinhos soltos a vaguear e a bater nas paredes, mais quanto mais cheia
          const count = 6 + Math.round(f * (n - 6));
          for (let k = 0; k < n; k++) {
            const s = c.seeds[k];
            s.vx += (r() - .5) * dt * 3; s.vy += (r() - .5) * dt * 3; s.vz += (r() - .5) * dt * 3;
            const sp = Math.hypot(s.vx, s.vy, s.vz) || 1, mx = 1.6; if (sp > mx) { s.vx *= mx / sp; s.vy *= mx / sp; s.vz *= mx / sp; }
            s.x += s.vx * dt * 2; s.y += s.vy * dt * 2; s.z += s.vz * dt * 2;
            if (Math.abs(s.x) > lim) { s.x = Math.sign(s.x) * lim; s.vx *= -1; } if (Math.abs(s.y) > lim) { s.y = Math.sign(s.y) * lim; s.vy *= -1; } if (Math.abs(s.z) > lim) { s.z = Math.sign(s.z) * lim; s.vz *= -1; }
            if (k >= count) { c.mini.setMatrixAt(k, hidden()); continue; }
            const sc = .55 + .3 * Math.sin(t * 3 + s.ph);
            Mx.makeScale(sc, sc, sc); Mx.setPosition(s.x * P, s.y * P, s.z * P); c.mini.setMatrixAt(k, Mx); c.mini.setColorAt(k, Cx.set(s.shade > .5 ? cols[1] : cols[2]));
          }
        }
        c.mini.material.opacity = type === 2 ? .75 : 1;
        c.mini.instanceMatrix.needsUpdate = true; c.mini.instanceColor.needsUpdate = true;
      }
      const pulses = [0, 1, 2].map(() => box(g, -.45, -.45, -.3, .45, .45, .3, glow('#ffffff')));
      const clock = appearClock(2.4); let lastT = null;
      return { group: g, update(t, st, f, variant) {
        const pal = CORE_PALETTES[variant | 0] || CORE_PALETTES[0], off = st === 4;
        const p = clock(t, off), sL = smooth(p, 0, .2), sSpine = smooth(p, .05, .35);
        const lc = (c, k = sL) => off ? '#1E1B2E' : '#' + new THREE.Color('#1E1B2E').lerp(new THREE.Color(c), k).getHexString();
        strips.forEach(m => m.color.set(lc(pal.strip)));
        spineM.color.set(lc(pal.win, sSpine));
        const gc = gaugeColor(f, pal.strip), n = Math.round(f * segs.length), blink = f >= .999 && Math.sin(t * 6) < 0;
        segs.forEach((m, i) => m.color.set(off ? '#1E1B2E' : i < Math.round(n * sSpine) ? (i === n - 1 && blink ? '#22252E' : gc) : '#22252E'));
        status.color.set(off ? '#1E1B2E' : f >= .95 ? (Math.sin(t * 5) > 0 ? C.red : '#5B1D1D') : f >= .8 ? '#FBBF24' : '#4ADE80');
        crown.visible = !off && p >= .9; crown.rotation.y = t * .8; crown.position.y = (32.6 + .3 * Math.sin(t * 1.6)) * P; crownM.color.set(pal.strip);
        pulses.forEach((q, i) => { const k = (t * .55 + i / 3) % 1, up = i % 2 === 0; q.visible = !off && p >= 1; q.position.set(0, (4.8 + 25.4 * (up ? k : 1 - k)) * P, -3.1 * P); q.material.color.set(pal.hi); });
        const dt = lastT === null ? 0 : Math.min(.05, Math.max(0, t - lastT)); lastT = t;
        cells.forEach(c => {
          const padOn = off ? 0 : smooth(p, .25 + c.i * .11, .35 + c.i * .11), s = off ? 0 : smooth(p, .32 + c.i * .11, .5 + c.i * .11);
          c.pads.color.set(c.filled ? lc(pal.win, padOn) : '#22252E');
          const type = st === 3 ? [0, 1, 2, 0, 1][c.i] : st;
          c.grp.visible = c.filled && s > 0;
          c.grp.position.y = (c.y + .25 * Math.sin(t * 1.5 + c.i * 1.7)) * P; c.grp.scale.setScalar(Math.max(.001, s));
          c.spin.rotation.set(0, (type === 1 ? .15 * Math.sin(t * .5) : t * (.35 + c.i * .06) * (c.i % 2 ? -1 : 1)), 0);
          c.edgeM.color.set(pal.strip);
          const ph = Math.max(0, Math.sin(t * 2.3 + c.i * 1.9)) * Math.max(0, Math.sin(t * 7.1 + c.i));
          c.ghost.visible = s >= 1; c.ghostM.opacity = .15 + .55 * ph; c.ghostM.color.set(pal.win);
          c.ghost.position.set(Math.sin(t * 1.7 + c.i) * .6 * P, Math.cos(t * 1.3 + c.i * 2) * .35 * P, 0); c.ghost.rotation.set(.2 + .3 * Math.sin(t + c.i), -t * .6, .25 * Math.cos(t * .8));
          const flash = s > .6 && s < 1 ? Math.sin((s - .6) / .4 * Math.PI) : 0; c.fl.visible = flash > 0; c.fl.scale.setScalar(.3 + flash);
          if (c.grp.visible) fillCell(c, type, f, t, dt);
        });
      } };
    }
  }, PALETTE_ROW));

