  // ===== Processador Quântico 3 x 3 x 3 — um por rede: candelabro de computador quântico a flutuar, base do Núcleo =====
  MODELS.push(Object.assign({ id: 'processador', name: 'Processador Quântico', color: C.cyan, size: '3 × 3 × 3 blocos (48 × 48 × 48 px)', radius: 3.6, target: 1.6,
    states: ['Ocupado', 'Livre', 'Sem energia'], levels: [.25, .5, 1], levelLabel: 'Canais em uso',
    info: {
      parts: ['<b>A mesma base de contenção do Núcleo</b>', '<b>Candelabro</b> de 4 placas redondas a flutuar, cada vez menores para baixo, ligadas por 4 cabos de luz (como um computador quântico real)',
        'Cada placa tem um aro de <b>8 lâmpadas de canal</b>: as acesas são os crafts a correr em paralelo', '<b>Chip</b> de qubits embaixo, com 4 qubits em pares entrelaçados à volta',
        '<b>4 braços curtos</b> no alto, com as garras a segurar a placa de cima: o candelabro fica suspenso, como nos computadores quânticos reais'],
      anim: ['Ocupado: placas giram em sentidos alternados, pulsos de frio descem até ao chip, qubits giram depressa e saltam raios das placas para os braços',
        'Livre: tudo devagar, sem raios', '<b>Sem energia</b>: candelabro, chip e qubits somem; os braços descem',
        '<b>Surgimento</b>: as placas aparecem de cima para baixo, depois os cabos, o chip e os qubits; os braços sobem'],
      tech: ['1 principal + 26 de estrutura; <b>um por rede</b>; os upgrades somam canais de craft em paralelo',
        'O tier máximo é enorme mas tem teto: os crafts por tick ficam presos ao <code>TickBudget</code> e à config (sem teto, derrubaria o TPS)',
        'Base em JSON; candelabro, chip, qubits, pulsos, braços e raios no BlockEntityRenderer']
    },
    build() {
      const G = new THREE.Group(), g = new THREE.Group(), r = rng(717), strips = [], wins = []; g.scale.setScalar(1.5); G.add(g);
      const sm = () => { const m = glow('#D946EF'); strips.push(m); return m; }, wm = () => { const m = glow('#38BDF8'); wins.push(m); return m; };
      const emitM = coreBase(G, sm, wm);
      const CY = 19.5, chand = new THREE.Group(); g.add(chand);
      const plates = [[27, 5.4], [23.5, 4.6], [20, 3.8], [16.5, 3]].map(([y, R], i) => {
        const grp = new THREE.Group(); grp.position.y = y * P; chand.add(grp);
        oct(grp, 0, 0, R, -.5, .5, 'metal'); roundRing(grp, R + .2, .5, -.35, .35, sm(), 16);
        const lamps = []; for (let k = 0; k < 8; k++) { const a = k / 8 * Math.PI * 2, x = Math.cos(a) * (R - 1.3), z = Math.sin(a) * (R - 1.3); lamps.push(box(grp, x - .4, -1.1, z - .4, x + .4, -.5, z + .4, glow('#22252E'))); }
        return { grp, y, i, lamps, R };
      });
      const cables = []; [[-1.2, 0], [1.2, 0], [0, -1.2], [0, 1.2]].forEach(([x, z]) => cables.push(box(chand, x - .22, 12.5, z - .22, x + .22, 27, z + .22, wm())));
      const chip = new THREE.Group(); chip.position.y = 11 * P; g.add(chip);
      box(chip, -2.4, -.6, -2.4, 2.4, .6, 2.4, 'frame'); const gridM = glow('#38BDF8');
      for (let a = -1.8; a <= 1.8; a += 1.8) { box(chip, a - .22, .6, -2.3, a + .22, .8, 2.3, gridM); box(chip, -2.3, .6, a - .22, 2.3, .8, a + .22, gridM); }
      const qubits = [0, 1, 2, 3].map(i => box(g, -.6, -.6, -.6, .6, .6, .6, glow(i % 2 ? '#E0FBFF' : '#D946EF')));
      const pulses = [0, 1].map(() => { const grp = new THREE.Group(); g.add(grp); const m = new THREE.MeshBasicMaterial({ color: '#ffffff', transparent: true, opacity: .8, blending: THREE.AdditiveBlending, depthWrite: false }); roundRing(grp, 2.6, .35, -.15, .15, m, 16); return { grp, m }; });
      const armsQ = quantumArms(g, CY, sm, 13.6, { long: false }), bolts = makeBolts(g, 3), v1 = new THREE.Vector3(), v2 = new THREE.Vector3();
      const clock = appearClock(2.4); let lastT = null;
      return { group: G, update(t, st, f, variant) {
        const dt = lastT === null ? 0 : Math.min(.05, Math.max(0, t - lastT)); lastT = t;
        const pal = CORE_PALETTES[variant | 0] || CORE_PALETTES[0], off = st === 2, busy = st === 0, spd = busy ? 1 : .3;
        const p = clock(t, off), sL = smooth(p, 0, .2), sArms = smooth(p, .55, .95);
        const lc = (c, k = sL) => off ? '#1E1B2E' : '#' + new THREE.Color('#1E1B2E').lerp(new THREE.Color(c), k).getHexString();
        strips.forEach(m => m.color.set(lc(pal.strip))); wins.forEach(m => m.color.set(lc(pal.win))); emitM.color.set(lc(pal.hi));
        const used = busy ? Math.round(f * 32) : 0;
        plates.forEach(pl => { const s = off ? 0 : smooth(p, .15 + pl.i * .1, .35 + pl.i * .1);
          pl.grp.visible = s > 0; pl.grp.scale.setScalar(Math.max(.001, s)); pl.grp.rotation.y = t * .6 * spd * (pl.i % 2 ? -1 : 1); pl.grp.position.y = (pl.y + .3 * Math.sin(t * 1.3 + pl.i)) * P;
          pl.lamps.forEach((m, k) => m.material.color.set(pl.i * 8 + k < used ? (Math.sin(t * 6 + k + pl.i) > -.3 ? pal.hi : pal.strip) : '#22252E')); });
        const sCab = off ? 0 : smooth(p, .5, .7); cables.forEach(c => c.visible = sCab > 0);
        const sChip = off ? 0 : smooth(p, .65, .85); chip.visible = sChip > 0; chip.scale.setScalar(Math.max(.001, sChip)); chip.rotation.y = t * .3 * spd; chip.position.y = (11 + .3 * Math.sin(t * 2)) * P;
        gridM.color.set(off ? '#1E1B2E' : Math.sin(t * 9 * spd) > 0 ? pal.hi : pal.win);
        const sQ = off ? 0 : smooth(p, .8, 1);
        qubits.forEach((q, i) => { const pair = i >> 1, a = t * (busy ? 3 : 1) * (pair ? -1 : 1) + (i % 2) * Math.PI;
          q.visible = sQ > 0; q.scale.setScalar(Math.max(.001, sQ)); q.position.set(Math.cos(a) * 4 * P, (11 + (pair ? 2 : -2) + .4 * Math.sin(t * 3 + i)) * P, Math.sin(a) * 5 * P); q.material.color.set(i % 2 ? pal.hi : pal.strip); });
        pulses.forEach((pu, i) => { const k = (t * .7 + i * .5) % 1; pu.grp.visible = !off && busy && p >= 1; pu.grp.position.y = (27.5 - 15 * k) * P; pu.grp.scale.setScalar(1.8 - k); pu.m.opacity = .9 * (1 - k); pu.m.color.set(pal.hi); });
        armsQ.update(t, dt, sArms, { off, lc, pal, speed: busy ? .3 : .15 });
        if (!off && busy && p >= 1 && r() < .06) { const b = bolts.find(x => x.life <= 0);
          if (b) { const pl = plates[Math.floor(r() * 4)], a = r() * Math.PI * 2, R0 = pl.R; v1.set(Math.cos(a) * R0, pl.y, Math.sin(a) * R0); armsQ.joints[Math.floor(r() * armsQ.joints.length)].getWorldPosition(v2); g.worldToLocal(v2); v2.divideScalar(P); fireBolt(b, v1, v2, 1, r); } }
        fadeBolts(bolts, dt || 1 / 60, off, pal.bolt);
      } };
    }
  }, PALETTE_ROW));

