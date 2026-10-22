  // ===== Bateria de Vácuo 3 x 3 x 3 — uma por rede: toro de plasma (tokamak) com solenoide central, base do Núcleo =====
  MODELS.push(Object.assign({ id: 'bateria', name: 'Bateria de Vácuo', color: C.energy, size: '3 × 3 × 3 blocos (48 × 48 × 48 px)', radius: 3.6, target: 1.6,
    states: ['Carregando', 'Descarregando', 'Parada', 'Sem energia'], levels: [.05, .35, .7, 1], levelLabel: 'Carga',
    info: {
      parts: ['<b>A mesma base de contenção do Núcleo</b>', '<b>Toro de plasma</b> (como um reator tokamak) a flutuar na horizontal, com <b>16 bobinas</b> à volta do tubo',
        'Cada bobina tem um filete de luz por fora: as bobinas acesas são o <b>medidor de carga</b> (1 bobina = 1/16)',
        '<b>Solenoide</b> central: coluna de luz vertical que passa pelo meio do toro', '<b>4 braços flutuantes</b> como os do Núcleo'],
      anim: ['Carregando: o plasma corre num sentido e pulsos sobem pelo solenoide', 'Descarregando: o plasma corre ao contrário e saltam raios do toro para os braços',
        'Abaixo de 15% as bobinas ficam vermelhas; cheia, piscam claras', 'Parada: plasma lento',
        '<b>Sem energia</b>: plasma e solenoide apagam; os braços descem', '<b>Surgimento</b>: as bobinas acendem uma a uma, o plasma inflama num clarão e os braços sobem'],
      tech: ['1 principal + 26 de estrutura; <b>uma por rede</b>, capacidade por upgrades (o último tier chega ao teto do <code>long</code>; transferências para outros mods limitadas pelo <code>int</code> da API)',
        'Base, bobinas e solenoide em JSON; plasma, pulsos, braços e raios no BlockEntityRenderer']
    },
    build() {
      const G = new THREE.Group(), g = new THREE.Group(), r = rng(616), strips = [], wins = []; g.scale.setScalar(1.5); G.add(g); // figura desenhada em 2×2×2 e ampliada
      const sm = () => { const m = glow('#D946EF'); strips.push(m); return m; }, wm = () => { const m = glow('#38BDF8'); wins.push(m); return m; };
      const emitM = coreBase(G, sm, wm);
      const CY = 19.5, R = 8, N = 16;
      // solenoide central
      const solM = glow('#A855F7'); oct(g, 0, 0, 1.3, 9.6, 29.2, solM); oct(g, 0, 0, 2, 28.8, 29.8, 'frame'); oct(g, 0, 0, 2, 9.2, 10.2, 'frame');
      // bobinas à volta do tubo do toro (múltiplos de 22,5°: cabem no JSON)
      const coils = [];
      for (let i = 0; i < N; i++) {
        const c = new THREE.Group(); c.rotation.y = i / N * Math.PI * 2; c.position.y = CY * P; g.add(c);
        box(c, R - 2.9, -2.9, -.45, R - 2.3, 2.9, .45, 'frame'); box(c, R + 2.3, -2.9, -.45, R + 2.9, 2.9, .45, 'frame');
        box(c, R - 2.3, 2.3, -.45, R + 2.3, 2.9, .45, 'metal'); box(c, R - 2.3, -2.9, -.45, R + 2.3, -2.3, .45, 'metal');
        const m = glow('#22252E'); box(c, R + 2.9, -2.4, -.35, R + 3.2, 2.4, .35, m); box(c, R - 2.3, 2.9, -.35, R + 2.3, 3.2, .35, m); coils.push(m);
      }
      // plasma e pontos quentes
      const plasma = new THREE.Group(); plasma.position.y = CY * P; g.add(plasma);
      const plasmaM = new THREE.MeshBasicMaterial({ color: '#D946EF', transparent: true, opacity: .55, blending: THREE.AdditiveBlending, depthWrite: false });
      roundRing(plasma, R + .9, 1.8, -.9, .9, plasmaM, 32);
      const hotM = glow('#ffffff'), hots = []; for (let i = 0; i < 6; i++) hots.push(box(plasma, -.6, -.6, -.6, .6, .6, .6, hotM));
      const ign = halo('rgba(255,255,255,0.95)', 1.2); ign.position.y = CY * P; g.add(ign);
      const pulses = [0, 1, 2].map(() => { const grp = new THREE.Group(); g.add(grp); const m = glow('#ffffff'); roundRing(grp, 1.7, .4, -.15, .15, m, 12); return { grp, m }; });
      const armsQ = quantumArms(g, CY, sm), bolts = makeBolts(g, 3), v1 = new THREE.Vector3(), v2 = new THREE.Vector3();
      const clock = appearClock(2.4); let lastT = null, flow = 0;
      return { group: G, update(t, st, f, variant) {
        const dt = lastT === null ? 0 : Math.min(.05, Math.max(0, t - lastT)); lastT = t;
        const pal = CORE_PALETTES[variant | 0] || CORE_PALETTES[0], off = st === 3, charging = st === 0, discharging = st === 1;
        const p = clock(t, off), sL = smooth(p, 0, .2), sCoil = smooth(p, .1, .5), sPl = smooth(p, .45, .7), sArms = smooth(p, .55, .95);
        const lc = (c, k = sL) => off ? '#1E1B2E' : '#' + new THREE.Color('#1E1B2E').lerp(new THREE.Color(c), k).getHexString();
        strips.forEach(m => m.color.set(lc(pal.strip))); wins.forEach(m => m.color.set(lc(pal.win))); emitM.color.set(lc(pal.hi));
        const low = f < .15, n = Math.round(f * N), full = f >= .999;
        coils.forEach((m, i) => { const on = !off && i < n && i < Math.round(N * sCoil); m.color.set(!on ? '#22252E' : low ? C.red : full && Math.sin(t * 4 + i) > .3 ? pal.hi : pal.strip); });
        solM.color.set(lc(pal.win, sPl));
        plasma.visible = !off && sPl > 0; plasma.scale.set(Math.max(.001, sPl), 1, Math.max(.001, sPl));
        plasmaM.color.set(low ? C.red : pal.strip); plasmaM.opacity = .75 + .2 * Math.sin(t * 9);
        flow += dt * (charging ? 2.4 : discharging ? -2.4 : .5);
        hotM.color.set(pal.hi); hots.forEach((h, i) => { const a = flow + i / hots.length * Math.PI * 2; h.position.set(Math.cos(a) * (R + .9) * P, .3 * Math.sin(t * 5 + i) * P, Math.sin(a) * (R + .9) * P); });
        const fl = sPl > 0 && sPl < 1 ? Math.sin(sPl * Math.PI) : 0; ign.visible = fl > 0; ign.scale.setScalar(.5 + fl * 2);
        pulses.forEach((q, i) => { const k = (t * .7 + i / 3) % 1; q.grp.visible = !off && charging && p >= 1; q.grp.position.y = (10 + 19 * k) * P; q.m.color.set(pal.hi); });
        armsQ.update(t, dt, sArms, { off, lc, pal });
        if (!off && discharging && p >= 1 && r() < .07) { const b = bolts.find(x => x.life <= 0);
          if (b) { const a = r() * Math.PI * 2; v1.set(Math.cos(a) * (R + 3), CY, Math.sin(a) * (R + 3)); armsQ.joints[Math.floor(r() * armsQ.joints.length)].getWorldPosition(v2); g.worldToLocal(v2); v2.divideScalar(P); fireBolt(b, v1, v2, 1, r); } }
        fadeBolts(bolts, dt || 1 / 60, off, pal.bolt);
      } };
    }
  }, PALETTE_ROW));

