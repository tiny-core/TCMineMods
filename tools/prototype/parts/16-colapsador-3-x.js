  // ===== Colapsador 3 x 3 x 3 — mesma linguagem do Núcleo: base baixa de metal, braços flutuantes, buraco negro =====
  MODELS.push({ id: 'colapsador', name: 'Colapsador', color: '#D946EF', size: '3 × 3 × 3 blocos (48 × 48 × 48 px)', radius: 3.4, target: 1.4,
    states: ['Destruindo', 'Ocioso', 'Erro', 'Sem energia'],
    levels2: CORE_PALETTES.map((_, i) => i), levelNames2: CORE_PALETTES.map(p => p.name), levelLabel2: 'Cor',
    info: {
      parts: ['<b>Base baixa de metal</b> (45 × 13 px), irmã da do Núcleo: 4 blocos de canto com grelhas e filete na aresta, nichos com <b>bocas de entrada</b> (setas de luz a apontar para dentro)',
        '<b>Poço gravitacional</b> no topo da base: anéis concêntricos a descer até um centro escuro',
        '<b>Buraco negro</b>: esfera negra com o <b>anel de fotões</b> sempre virado para quem olha e um halo de lente',
        '<b>Disco de acreção</b> inclinado em 5 faixas (claro por dentro, escuro por fora), ondulado',
        '<b>4 braços de contenção</b> curtos, com juntas magnéticas e garras, a segurar o disco'],
      anim: ['Destruindo: itens sobem pelas bocas, giram em espiral, <b>esticam-se</b> e somem num clarão no horizonte; o disco gira rápido e partículas caem nele',
        'O buraco negro <b>pulsa e deforma-se</b>; o disco ondula; raios saltam entre o disco e as juntas dos braços',
        'Os braços giram ao contrário do disco e sobem e descem desencontrados',
        'Ocioso: disco lento, sem itens, poucos raios. Erro: vermelho a piscar, braços a tremer',
        '<b>Sem energia, o buraco negro some</b> e os braços descem apagados',
        '<b>Surgimento</b> (≈ 2,5 s): luzes e poço acendem, um ponto de luz implode no centro, o buraco negro abre com uma onda de choque, o disco forma-se de dentro para fora e os braços sobem'],
      tech: ['1 principal + 26 de estrutura (3 × 3 × 3), colisão real, como o Núcleo',
        'Base em JSON; buraco negro, disco, braços, itens, partículas e raios num BlockEntityRenderer (até 64 blocos)',
        'Os itens a cair são só visuais, um por vez e com teto: o item já foi destruído no servidor',
        'Destrói só o excesso acima de cada regra; nunca a Reserva nem itens com encantamentos ou nome',
        'Mesmas paletas de cor do Núcleo']
    },
    build() {
      const G = new THREE.Group(), g = new THREE.Group(), CY = 20, r = rng(777); g.scale.setScalar(1.5); G.add(g); // desenhado em 2×2×2 e ampliado para 3×3×3
      const strips = [], wins = [];
      const sm = () => { const m = glow('#D946EF'); strips.push(m); return m; };
      const wm = () => { const m = glow('#38BDF8'); wins.push(m); return m; };
      // ---------- base (JSON) ----------
      cbox(g, -15, 0, -15, 15, 1.2, 15, 'dark', 2);
      box(g, -10, 1.2, -10, 10, 5.5, 10, 'dark');
      [[-1, -1], [1, -1], [-1, 1], [1, 1]].forEach(([sx, sz]) => {
        cbox(g, Math.min(sx * 4, sx * 14.5), 1.2, Math.min(sz * 4, sz * 14.5), Math.max(sx * 4, sx * 14.5), 5.5, Math.max(sz * 4, sz * 14.5), 'metal', 1.5);
        cbox(g, Math.min(sx * 4.4, sx * 14.1), 5.5, Math.min(sz * 4.4, sz * 14.1), Math.max(sx * 4.4, sx * 14.1), 6.2, Math.max(sz * 4.4, sz * 14.1), 'frame', 1.5);
        bx(g, sx * 6, 2, sz * 14.5, sx * 12, 4.6, sz * 14.75, 'vent');
        bx(g, sx * 14.5, 2, sz * 6, sx * 14.75, 4.6, sz * 12, 'vent');
        const e = new THREE.Group(); e.position.set(sx * 13.55 * P, 0, sz * 13.55 * P); e.rotation.y = sx * sz > 0 ? -Math.PI / 4 : Math.PI / 4; g.add(e);
        box(e, -.6, 1.8, -.2, .6, 5, .25, sm());
        bx(g, sx * 6, 6.2, sz * 9.4, sx * 12.5, 6.35, sz * 9.9, sm()); bx(g, sx * 9.4, 6.2, sz * 6, sx * 9.9, 6.35, sz * 12.5, sm());
        // 4 pés de âncora dos braços, com luz no topo
        cbox(g, Math.min(sx * 9.5, sx * 12.5), 6.2, Math.min(sz * 9.5, sz * 12.5), Math.max(sx * 9.5, sx * 12.5), 8.2, Math.max(sz * 9.5, sz * 12.5), 'frame', .6);
        bx(g, sx * 10.2, 8.2, sz * 10.2, sx * 11.8, 8.5, sz * 11.8, sm());
      });
      for (let f = 0; f < 4; f++) {
        const d = new THREE.Group(); d.rotation.y = f * Math.PI / 2; g.add(d);
        box(d, -4, 1.2, 10, -3.2, 5.5, 10.8, 'frame'); box(d, 3.2, 1.2, 10, 4, 5.5, 10.8, 'frame'); box(d, -4, 4.8, 10, 4, 5.5, 10.8, 'frame');
        box(d, -3.2, 1.2, 10, 3.2, 4.8, 10.15, 'panel');
        // boca de entrada: 2 setas em V a apontar para dentro
        [[0, 3.6], [1, 2.4]].forEach(([k, y]) => { const m = wm(); [-1, 1].forEach(s => { const a = new THREE.Group(); a.position.set(s * 1.1 * P, y * P, 10.3 * P); a.rotation.z = s * .7; d.add(a); box(a, -1.3, -.3, 0, 1.3, .3, .35, m); }); });
      }
      // poço gravitacional: anéis em degraus que descem até o centro escuro
      cbox(g, -9, 5.5, -9, 9, 7, 9, 'frame', 2.5);
      roundRing(g, 8, 1.6, 7, 7.9, 'metal', 24);
      const well = [];
      [[6.4, 7.4, 1.2], [5, 7.1, 1], [3.7, 6.8, .9], [2.5, 6.5, .8]].forEach(([R, y, w], i) => { const m = i % 2 ? wm() : sm(); roundRing(g, R, w, y - .4, y, m, 20); well.push(m); });
      oct(g, 0, 0, 1.6, 6, 6.2, flat('#050308'));
      // ---------- buraco negro ----------
      const core = new THREE.Group(); core.position.y = CY * P; core.scale.setScalar(.72); g.add(core); // menor: o disco não toca nos braços
      const holeG = new THREE.Group(); core.add(holeG);
      const hole = voxels(spherePoints(4.6), 1, new THREE.MeshBasicMaterial({ color: '#030106' })); holeG.add(hole);
      holeG.add(new THREE.Mesh(new THREE.SphereGeometry(4.3 * P, 16, 12), new THREE.MeshBasicMaterial({ color: '#000000' })));
      const photon = new THREE.Group(); core.add(photon);
      const phM = glow('#ffffff'), phM2 = glow('#D946EF');
      const ph1 = voxels(ringPoints(5.5, .6), .6, phM); ph1.rotation.x = Math.PI / 2; photon.add(ph1);
      const ph2 = voxels(ringPoints(6.2, .8), .45, phM2); ph2.rotation.x = Math.PI / 2; photon.add(ph2);
      const lens = halo('rgba(217,70,239,0.6)', 1.5); core.add(lens);
      const disc = new THREE.Group(); disc.rotation.set(.32, 0, -.14); core.add(disc);
      const spinDisc = new THREE.Group(); disc.add(spinDisc);
      const bandDefs = [[6.8, 'hi', .7], [8, 'bright', .8], [9.2, 'mid', .85], [10.4, 'strip', .9], [11.5, 'deep', 1]];
      const bands = bandDefs.map(([R, key, s], i) => {
        const pts = ringPoints(R, s * 1.05), mat = glow('#ffffff'), im = voxels(pts, s, mat); spinDisc.add(im);
        return { im, pts, mat, key, R, i, ph: r() * 6 };
      });
      const shock = voxels(ringPoints(10, .8), .5, new THREE.MeshBasicMaterial({ color: '#ffffff', transparent: true, opacity: 1, blending: THREE.AdditiveBlending, depthWrite: false }));
      shock.rotation.x = Math.PI / 2; const shockG = new THREE.Group(); shockG.add(shock); core.add(shockG);
      const spark = halo('rgba(255,255,255,0.95)', .6); core.add(spark);
      // partículas a cair no disco e itens a ser engolidos
      const fallMat = glow('#F5D0FE'), falls = [];
      for (let i = 0; i < 20; i++) falls.push(box(core, -.3, -.3, -.3, .3, .3, .3, fallMat));
      const ITEM_COLS = ['#9CA3AF', '#D4A23A', '#7A5530', '#4ADE80', '#60A5FA', '#E5E7EB'];
      const items = [0, 1, 2].map(i => { const grp = new THREE.Group(); g.add(grp); const m = new THREE.MeshLambertMaterial({ color: ITEM_COLS[i] }); box(grp, -1, -1, -1, 1, 1, 1, m); return { grp, m, start: -10, face: i }; });
      const flash = halo('rgba(255,255,255,0.9)', .5); core.add(flash);
      // ---------- braços de contenção ----------
      const armsRoot = new THREE.Group(); armsRoot.position.y = CY * P; g.add(armsRoot);
      const Ra = 13, arms = [], allJoints = [];
      [0, 1, 2, 3].forEach(k => {
        const yawG = new THREE.Group(); yawG.rotation.y = k * Math.PI / 2 + Math.PI / 4; armsRoot.add(yawG);
        const arm = new THREE.Group(); yawG.add(arm);
        const a0 = -38, a1 = 42, n = 3, step = (a1 - a0) / n, L = step * Math.PI / 180 * Ra - 1.4, segs = [], joints = [];
        for (let i = 0; i < n; i++) {
          const a = (a0 + (i + .5) * step) * Math.PI / 180;
          const seg = new THREE.Group(); seg.userData.a = a; seg.rotation.z = a; arm.add(seg);
          box(seg, -1.6, -L / 2, -2, 1.6, L / 2, 2, 'metal');
          box(seg, -.4, -L / 2 + .4, -2.2, 1.9, L / 2 - .4, 2.2, 'frame');
          box(seg, 1.9, -L / 2 + .8, -.55, 2.15, L / 2 - .8, .55, sm());
          box(seg, -1.85, -L / 2 + 1, -.4, -1.6, L / 2 - 1, .4, sm());
          if (i === n - 1) { box(seg, -3.6, L / 2 - 1, -1.5, -1.2, L / 2 + 1.1, 1.5, 'frame'); box(seg, -4.1, L / 2 - .5, -.8, -3.6, L / 2 + .7, .8, sm()); }
          if (i === 0) { box(seg, -3.2, -L / 2 - 1, -1.4, -1.1, -L / 2 + .8, 1.4, 'frame'); box(seg, -3.6, -L / 2 - .5, -.7, -3.2, -L / 2 + .5, .7, sm()); }
          segs.push(seg);
          if (i < n - 1) { const j = box(arm, -.6, -.6, -.6, .6, .6, .6, glow('#A5B4FC')); j.userData.a = (a0 + (i + 1) * step) * Math.PI / 180; joints.push(j); allJoints.push(j); }
        }
        arms.push({ arm, segs, joints, k });
      });
      // ---------- raios ----------
      const bolts = [];
      for (let b = 0; b < 6; b++) {
        const mk = (color) => { const geo = new THREE.BufferGeometry(); geo.setAttribute('position', new THREE.BufferAttribute(new Float32Array(9 * 3), 3)); const l = new THREE.Line(geo, new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0, blending: THREE.AdditiveBlending, depthWrite: false })); g.add(l); return l; };
        bolts.push({ a: mk('#ffffff'), b: mk('#A5F3FC'), life: 0, max: .2 });
      }
      const v1 = new THREE.Vector3(), v2 = new THREE.Vector3(), tmp = new THREE.Vector3(), perp = new THREE.Vector3(), side = new THREE.Vector3(), cam = new THREE.Vector3();
      function spawnBolt(bolt) {
        const ang = r() * Math.PI * 2, R0 = (8 + r() * 3) * .72;
        v1.set(Math.cos(ang) * R0, 0, Math.sin(ang) * R0).applyEuler(disc.rotation).add(tmp.set(0, CY, 0));
        allJoints[Math.floor(r() * allJoints.length)].getWorldPosition(v2); g.worldToLocal(v2); v2.divideScalar(P);
        tmp.copy(v2).sub(v1); perp.set(r() - .5, r() - .5, r() - .5).cross(tmp).normalize(); side.crossVectors(perp, tmp).normalize();
        [bolt.a, bolt.b].forEach((line, li) => {
          const arr = line.geometry.attributes.position.array;
          for (let j = 0; j < 9; j++) { const f = j / 8, env = Math.sin(Math.PI * f) * (1.4 + li * .3);
            tmp.copy(v1).lerp(v2, f).addScaledVector(perp, (r() - .5) * 2 * env).addScaledVector(side, (r() - .5) * env);
            arr[j * 3] = tmp.x * P; arr[j * 3 + 1] = tmp.y * P; arr[j * 3 + 2] = tmp.z * P; }
          line.geometry.attributes.position.needsUpdate = true;
        });
        bolt.max = .12 + r() * .14; bolt.life = bolt.max;
      }
      // ---------- animação ----------
      const APPEAR = 2.6, M = new THREE.Matrix4(), col = new THREE.Color(), tc = new THREE.Color(), dark = '#1E1B2E';
      let lastT = null, discSpin = 0, armSpin = 0, spawnAcc = 0, wasOff = true, appearAt = 0, burst = false, nextItem = 0, itemIdx = 0;
      return { group: G, update(t, st, _f, variant) {
        const dt = lastT === null ? 0 : Math.min(.05, Math.max(0, t - lastT)); lastT = t;
        const pal = CORE_PALETTES[variant | 0] || CORE_PALETTES[0];
        const off = st === 3, err = st === 2, busy = st === 0;
        if (!off && wasOff) { appearAt = t; burst = false; }
        wasOff = off;
        const p = off ? 0 : Math.min(1, (t - appearAt) / APPEAR);
        const sLights = smooth(p, 0, .2), sImplode = smooth(p, .15, .35), sHole = smooth(p, .33, .55), sShock = smooth(p, .33, .7), sDisc = smooth(p, .45, .85), sArms = smooth(p, .55, .95), sBolts = smooth(p, .7, .78);
        const spd = busy ? 1.8 : .6, flick = err && Math.sin(t * 14) > 0;
        const lightCol = (c) => { if (off) return col.set(dark); col.set(flick ? C.red : c); return col.lerp(tc.set(dark), 1 - sLights); };
        strips.forEach(m => m.color.copy(lightCol(pal.strip)));
        wins.forEach(m => m.color.copy(lightCol(pal.win)));
        well.forEach((m, i) => { m.color.copy(lightCol(i % 2 ? pal.win : pal.strip)); if (!off) m.color.lerp(tc.set('#ffffff'), Math.max(0, Math.sin(t * 5 * spd + i * 1.4)) * .5 * sLights); });
        // implosão: um ponto de luz encolhe até o centro antes de o buraco abrir
        spark.visible = !off && sImplode > 0 && sHole < 1; spark.scale.setScalar(Math.max(.001, (1 - sImplode) * 1.6 + .2) * (1 - sHole));
        // buraco negro: some sem energia; pulsa e deforma
        core.visible = !off && sImplode > 0;
        const pulse = 1 + .06 * Math.sin(t * 2.6 * (busy ? 1.6 : 1));
        holeG.scale.set(sHole * pulse * (1 + .05 * Math.sin(t * 3.1)), sHole * pulse * (1 + .05 * Math.cos(t * 2.3)), sHole * pulse * (1 + .05 * Math.sin(t * 2.7 + 1)));
        if (VIEW.camera) { VIEW.camera.getWorldPosition(cam); photon.lookAt(cam); }
        photon.scale.setScalar(Math.max(.001, sHole) * (1 + .04 * Math.sin(t * 4)));
        phM.color.set(flick ? '#FECACA' : pal.hi); phM2.color.set(flick ? C.red : pal.strip);
        lens.material.color.set(flick ? C.red : pal.strip); lens.material.opacity = sHole * (.5 + .2 * Math.sin(t * 2.2)); lens.scale.setScalar(1.5 + .1 * Math.sin(t * 2.2));
        // onda de choque ao abrir
        shockG.visible = sShock > 0 && sShock < 1; shock.scale.setScalar(.1 + sShock * 1.6); shock.material.opacity = 1 - sShock; shock.material.color.set(pal.hi);
        shockG.rotation.copy(disc.rotation);
        // disco: forma-se de dentro para fora, gira e ondula
        discSpin += dt * (busy ? 2.2 : .7) * (.3 + sDisc);
        spinDisc.rotation.y = discSpin;
        disc.rotation.x = .32 + .04 * Math.sin(t * .9); disc.rotation.z = -.14 + .03 * Math.cos(t * .7);
        bands.forEach(b => {
          const vis = smooth(sDisc, b.i * .14, b.i * .14 + .35);
          b.im.visible = vis > 0;
          b.pts.forEach((pt, i) => { const a = i / b.pts.length * Math.PI * 2, wav = .5 * Math.sin(a * 3 + t * 2 * spd + b.ph);
            const rr = b.R * (.4 + .6 * vis); M.makeTranslation(Math.cos(a) * rr * P, wav * P, Math.sin(a) * rr * P); b.im.setMatrixAt(i, M); });
          b.im.instanceMatrix.needsUpdate = true;
          b.mat.color.set(flick ? C.red : pal[b.key]); if (!flick) b.mat.color.lerp(tc.set('#ffffff'), (1 - vis) * .7 + .15 * Math.max(0, Math.sin(t * 6 * spd - b.i)));
        });
        // partículas a cair em espiral no horizonte
        fallMat.color.set(flick ? C.red : pal.bolt);
        falls.forEach((f, i) => { const k = (t * (busy ? .5 : .2) + i / falls.length) % 1, rr = 11.5 * (1 - k) + 4.6 * k, a = i * 1.7 + k * 14;
          f.visible = sDisc >= 1 && (busy || i % 3 === 0);
          tmp.set(Math.cos(a) * rr * P, Math.sin(i) * .8 * (1 - k) * P, Math.sin(a) * rr * P).applyEuler(disc.rotation); f.position.copy(tmp); });
        // itens: sobem por uma boca, espiralam, esticam e somem num clarão
        if (busy && sDisc >= 1 && t > nextItem) { const it = items[itemIdx++ % items.length]; it.start = t; it.face = Math.floor(r() * 4); it.m.color.set(ITEM_COLS[Math.floor(r() * ITEM_COLS.length)]); nextItem = t + .9 + r() * .6; }
        let flashK = 0;
        items.forEach(it => {
          const k = (t - it.start) / 1.6; it.grp.visible = !off && busy && k >= 0 && k < 1;
          if (!it.grp.visible) return;
          const fa = it.face * Math.PI / 2, rise = smooth(k, 0, .3), swirl = smooth(k, .25, 1);
          const sx = Math.sin(fa) * 15, sz = Math.cos(fa) * 15;
          const rr = 13 * (1 - swirl) + 4.5 * swirl, a = fa + swirl * 9;
          const x = sx * (1 - rise) + Math.sin(a) * rr * rise, z = sz * (1 - rise) + Math.cos(a) * rr * rise, y = 3 * (1 - rise) + CY * rise;
          it.grp.position.set(x * P, y * P, z * P);
          const stretch = 1 + swirl * swirl * 3, thin = Math.max(.05, 1 - swirl * swirl);
          it.grp.scale.set(thin * .8, thin * .8, stretch * .8); it.grp.lookAt(0, CY * P * 1.5, 0); it.grp.rotateZ(t * 6);
          if (k > .9) flashK = Math.max(flashK, (1 - k) * 10);
        });
        flash.visible = flashK > 0; flash.scale.setScalar(.4 + flashK * .8); flash.material.color.set(pal.hi);
        // braços: giram ao contrário do disco, sobem e descem
        armSpin -= dt * .3 * (busy ? 1.6 : 1) * sArms; armsRoot.rotation.y = armSpin;
        arms.forEach(({ arm, segs, joints, k }) => {
          const bob = -3 * (1 - sArms) + 1.2 * Math.sin(t * 1.3 + k * Math.PI / 2) * sArms;
          arm.position.set(err ? (r() - .5) * .4 * P : 0, bob * P, err ? (r() - .5) * .4 * P : 0);
          arm.rotation.z = -.15 * (1 - sArms) + .07 * Math.sin(t * .9 + k * 1.3) * sArms;
          const gap = i => -.5 * (1 - sArms) + .45 * Math.sin(t * 2.2 - i * .8 + k) * sArms;
          segs.forEach((seg, i) => { const rr = Ra + gap(i); seg.position.set(rr * Math.cos(seg.userData.a) * P, rr * Math.sin(seg.userData.a) * P, 0); });
          joints.forEach((j, i) => { const rr = Ra + gap(i + .5); j.position.set(rr * Math.cos(j.userData.a) * P, rr * Math.sin(j.userData.a) * P, 0);
            const pl = Math.max(0, Math.sin(t * 4 - i * 1.2 + k * 1.7)) * sArms;
            j.material.color.copy(lightCol(pal.strip)).lerp(tc.set(pal.hi), off || flick ? 0 : pl * .8); j.scale.setScalar(.7 + .3 * sArms + .3 * pl); });
        });
        // raios entre o disco e as juntas
        const rate = off ? 0 : (busy ? 4 : err ? 5 : 1.2) * sBolts;
        if (!off && !burst && sBolts > 0) { burst = true; spawnAcc += 5; }
        spawnAcc = off ? 0 : Math.min(6, spawnAcc + dt * rate);
        bolts.forEach(b => {
          if (b.life <= 0 && spawnAcc >= 1 && !off) { spawnAcc -= 1; spawnBolt(b); }
          b.life -= dt; const o = off ? 0 : Math.max(0, b.life / b.max);
          b.a.material.opacity = o; b.b.material.opacity = o * .8; b.b.material.color.set(flick ? C.red : pal.bolt);
        });
      } };
    }
  });

