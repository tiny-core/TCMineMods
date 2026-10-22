  // ===== Túnel Quântico 3 x 3 x 3 — um por dimensão: antena de anéis com o par entrelaçado, base do Núcleo =====
  const DIMS = [['Overworld', '#4ADE80'], ['Nether', '#F87171'], ['End', '#C084FC']];
  MODELS.push(Object.assign({ id: 'tunel', name: 'Túnel Quântico', color: C.purple, size: '3 × 3 × 3 blocos (48 × 48 × 48 px)', radius: 3.8, target: 1.8,
    states: ['Ligado (3 dimensões)', 'Ligado (2 dimensões)', 'Só esta dimensão', 'Sem energia'],
    info: {
      parts: ['<b>A mesma base de contenção do Núcleo</b>', '<b>Mastro</b> de antena com <b>5 anéis</b> de levitação que diminuem para cima',
        '<b>Prato emissor</b> no topo e o <b>par entrelaçado</b>: dois cristais espelhados a girar um em volta do outro',
        '<b>3 orbes de dimensão</b> à volta do par (verde Overworld, vermelho Nether, roxo End): acesos = essa dimensão tem Túnel e está ligada',
        'Sem braços: a antena fica sozinha sobre a base'],
      anim: ['Ligado: os anéis giram em sentidos alternados, pulsos sobem pelo mastro, o par gira depressa e um feixe curto sobe com ondas',
        'Só esta dimensão: sem feixe, par a pulsar devagar, orbes das outras dimensões apagados',
        '<b>Sem energia</b>: par, orbes, feixe e ondas somem: os anéis pousam',
        '<b>Surgimento</b>: os anéis sobem um a um, o par aparece num clarão, os orbes acendem e o feixe dispara'],
      tech: ['1 principal + 26 de estrutura; <b>um por dimensão</b> (um segundo na mesma dimensão avisa e não liga)',
        'É o que faz os blocos entrelaçados funcionarem naquela dimensão: sem Túnel, os blocos de lá ficam desligados — vale também para a dimensão do Núcleo', 'Mostra as dimensões ligadas: 3 orbes para Overworld, Nether e End; a lista completa (dimensões de outros mods) fica na tela do Túnel',
        'Base e mastro em JSON; anéis, par, orbes, feixe, ondas e braços no BlockEntityRenderer (o feixe tem 1 bloco e nunca vai ao céu)']
    },
    build() {
      const G = new THREE.Group(), g = new THREE.Group(), r = rng(919), strips = [], wins = []; g.scale.setScalar(1.5); G.add(g);
      const sm = () => { const m = glow('#D946EF'); strips.push(m); return m; }, wm = () => { const m = glow('#38BDF8'); wins.push(m); return m; };
      const emitM = coreBase(G, sm, wm);
      // mastro e prato
      cbox(g, -1.1, 8.8, -1.1, 1.1, 26.5, 1.1, 'metal', .4); box(g, -.2, 9.5, 1.1, .2, 26, 1.25, sm());
      oct(g, 0, 0, 3.6, 26.5, 27.4, 'frame'); oct(g, 0, 0, 2.7, 27.4, 27.9, 'metal'); const dish = glow('#E0E7FF'); oct(g, 0, 0, 1.6, 27.9, 28.1, dish);
      const rings = [[6.2, 12], [5.2, 15.4], [4.3, 18.6], [3.5, 21.6], [2.8, 24.4]].map(([R, y], i) => {
        const grp = new THREE.Group(); g.add(grp); roundRing(grp, R, .9, -.35, .35, 'metal', 16); const m = glow('#D946EF'); roundRing(grp, R - .95, .3, -.2, .2, m, 16);
        return { grp, m, y, rest: 9 + i * .75, i };
      });
      const mastPulses = [0, 1].map(() => box(g, -.5, -.5, -.5, .5, .5, .5, glow('#ffffff')));
      // par entrelaçado e orbes de dimensão
      const top = new THREE.Group(); top.position.y = 30.5 * P; g.add(top);
      const crA = glow('#D946EF'), crB = glow('#A855F7'), a = new THREE.Group(), b = new THREE.Group(); top.add(a, b);
      octa(a, 0, 0, 0, [.5, 1.2, 1.6, 1.2, .5], .45, crA); octa(b, 0, 0, 0, [.5, 1.2, 1.6, 1.2, .5], .45, crB);
      const orbs = DIMS.map(([, c]) => ({ c, m: box(top, -.55, -.55, -.55, .55, .55, .55, glow(c)) }));
      const pflash = halo('rgba(255,255,255,0.9)', .7); top.add(pflash);
      const beamMat = new THREE.MeshBasicMaterial({ color: '#C084FC', transparent: true, opacity: .45, blending: THREE.AdditiveBlending, depthWrite: false });
      const beamG = new THREE.Group(); beamG.position.y = 31.5 * P; g.add(beamG);
      const BL = 11 * P, beam = new THREE.Mesh(new THREE.CylinderGeometry(.4 * P, 1 * P, BL, 10, 1, true), beamMat); beam.position.y = BL / 2; beamG.add(beam);
      const waves = [0, 1, 2].map(() => { const w = new THREE.Group(); g.add(w); const m = new THREE.MeshBasicMaterial({ color: '#E9D5FF', transparent: true, opacity: .6, blending: THREE.AdditiveBlending, depthWrite: false }); roundRing(w, 1.6, .3, -.1, .1, m, 16); return { w, m }; });
      const clock = appearClock(2.4); let lastT = null; // sem braços: a antena fica sozinha sobre a base
      return { group: G, update(t, st, _f, variant) {
        const dt = lastT === null ? 0 : Math.min(.05, Math.max(0, t - lastT)); lastT = t;
        const pal = CORE_PALETTES[variant | 0] || CORE_PALETTES[0], off = st === 3, linked = st <= 1, nDims = st === 0 ? 3 : st === 1 ? 2 : 1;
        const p = clock(t, off), sL = smooth(p, 0, .2), sPair = smooth(p, .5, .7), sBeam = smooth(p, .72, .95);
        const lc = (c, k = sL) => off ? '#1E1B2E' : '#' + new THREE.Color('#1E1B2E').lerp(new THREE.Color(c), k).getHexString();
        strips.forEach(m => m.color.set(lc(pal.strip))); wins.forEach(m => m.color.set(lc(pal.win))); emitM.color.set(lc(pal.hi)); dish.color.set(lc(pal.hi));
        rings.forEach(R => { const up = off ? 0 : smooth(p, .1 + R.i * .07, .3 + R.i * .07);
          R.grp.position.y = (R.rest + (R.y - R.rest) * up + .25 * Math.sin(t * 2 + R.i) * up) * P; R.grp.rotation.y = t * (linked ? 1.1 : .4) * (R.i % 2 ? -1 : 1) * up;
          R.m.color.set(lc(R.i % 2 ? pal.win : pal.strip, up)); });
        mastPulses.forEach((q, i) => { const k = (t * .6 + i / 2) % 1; q.visible = !off && linked && p >= 1; q.position.set(0, (9.5 + 17 * k) * P, 1.4 * P); q.material.color.set(pal.hi); });
        top.visible = !off && sPair > 0; top.scale.setScalar(Math.max(.001, sPair));
        const ang = t * (linked ? 2.2 : .5);
        a.position.set(Math.cos(ang) * 1.4 * P, Math.sin(ang * 2) * .35 * P, Math.sin(ang) * 1.4 * P); b.position.set(-a.position.x, -a.position.y, -a.position.z);
        a.rotation.y = t * 2; b.rotation.y = -t * 2; crA.color.set(pal.strip); crB.color.set(pal.win);
        const pulse = linked ? 1 : .7 + .3 * Math.sin(t * 2); a.scale.setScalar(pulse); b.scale.setScalar(pulse);
        orbs.forEach((o, i) => { const oa = -t * .7 + i / 3 * Math.PI * 2, on = i < nDims;
          o.m.position.set(Math.cos(oa) * 3.6 * P, (.3 * Math.sin(t * 2 + i)) * P, Math.sin(oa) * 3.6 * P);
          o.m.material.color.set(on ? o.c : '#2A2440'); o.m.scale.setScalar(on ? 1 + .15 * Math.sin(t * 4 + i) : .7); });
        const fl = sPair > 0 && sPair < 1 ? Math.sin(sPair * Math.PI) : 0; pflash.visible = fl > 0; pflash.scale.setScalar(.3 + fl);
        beamG.visible = !off && linked && sBeam > 0; beamG.scale.y = Math.max(.001, sBeam); beamMat.color.set(pal.strip); beamMat.opacity = .35 + .15 * Math.sin(t * 6);
        waves.forEach((w, i) => { const k = (t * .6 + i / 3) % 1; w.w.visible = beamG.visible && sBeam >= 1; w.w.position.y = (31.5 + 11 * k) * P; w.w.scale.setScalar(.6 + k * .9); w.m.opacity = .7 * (1 - k); w.m.color.set(pal.hi); });
      } };
    }
  }, PALETTE_ROW));

