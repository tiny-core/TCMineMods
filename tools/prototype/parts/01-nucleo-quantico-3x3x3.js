  // ===== Núcleo Quântico 3x3x3 — conceito 2: base de contenção baixa de metal, braços flutuantes e cristal que se deforma =====
  const CORE_PALETTES = [
    { name: 'Magenta + Roxo', deep: '#3B0764', mid: '#C026D3', bright: '#7C3AED', hi: '#F5D0FE', strip: '#D946EF', win: '#A855F7', bolt: '#E9D5FF' },
    { name: 'Magenta + Azul', deep: '#3B0764', mid: '#C026D3', bright: '#2563EB', hi: '#67E8F9', strip: '#D946EF', win: '#38BDF8', bolt: '#A5F3FC' },
    { name: 'Azul / Roxo', deep: '#2E1065', mid: '#6D28D9', bright: '#4F46E5', hi: '#7DD3FC', strip: '#8B5CF6', win: '#3B82F6', bolt: '#C7D2FE' },
    { name: 'Azul frio', deep: '#0C2D6B', mid: '#1D4ED8', bright: '#0EA5E9', hi: '#E0F2FE', strip: '#38BDF8', win: '#0EA5E9', bolt: '#BAE6FD' },
    { name: 'Magenta', deep: '#4A044E', mid: '#A21CAF', bright: '#D946EF', hi: '#F5D0FE', strip: '#E879F9', win: '#C026D3', bolt: '#F5D0FE' },
    { name: 'Laranja', deep: '#7C2D12', mid: '#EA580C', bright: '#F59E0B', hi: '#FEF3C7', strip: '#FB923C', win: '#F59E0B', bolt: '#FDE68A' }
  ];
  const PALETTE_ROW = { levels2: CORE_PALETTES.map((_, i) => i), levelNames2: CORE_PALETTES.map(p => p.name), levelLabel2: 'Cor' };
  // Pedra da base: cinzento-arroxeado com rachas e aresta clara, como os blocos da referência.
  function stoneTex(base, seed) {
    return canvasTex((x, r) => {
      for (let i = 0; i < 16; i++) for (let j = 0; j < 16; j++) px(x, hex(base, Math.round((r() - .5) * 14)), i, j);
      px(x, hex(base, 22), 0, 0, 16, 1); px(x, hex(base, 14), 0, 0, 1, 16); px(x, hex(base, -26), 0, 15, 16, 1); px(x, hex(base, -26), 15, 0, 1, 16);
      for (let k = 0; k < 3; k++) { let cx = 2 + Math.floor(r() * 12), cy = 2 + Math.floor(r() * 12); for (let s = 0; s < 4; s++) { px(x, hex(base, -30), cx, cy); cx += r() < .5 ? 1 : 0; cy += r() < .5 ? 1 : -1; cx = Math.min(14, cx); cy = Math.max(1, Math.min(14, cy)); } }
    }, seed);
  }
  TEX.stone = stoneTex('#3B3944', 61); TEX.stoneDark = stoneTex('#25232D', 63); TEX.stonePurple = stoneTex('#43336B', 67);
  // Anel redondo de n blocos (a octRing tem os cantos em X; a referência pede um anel liso).
  function roundRing(g, R, w, y0, y1, mat, n) {
    n = window.__EXPORT ? 16 : (n || 20); const L = 2 * Math.PI * R / n + .35;
    for (let i = 0; i < n; i++) { const s = new THREE.Group(); s.rotation.y = i / n * Math.PI * 2; g.add(s); const e = (i % 2) * .02; box(s, R - w, y0 + e, -L / 2, R, y1 + e, L / 2, mat); } // segmentos alternados 0,02 px acima: as pontas sobrepostas não cintilam
  }
  // Caixa com os cantos em qualquer ordem (para peças espelhadas).
  function bx(g, x0, y0, z0, x1, y1, z1, mat) { return box(g, Math.min(x0, x1), Math.min(y0, y1), Math.min(z0, z1), Math.max(x0, x1), Math.max(y0, y1), Math.max(z0, z1), mat); }
  const smooth = (p, a, b) => { const k = Math.min(1, Math.max(0, (p - a) / (b - a))); return k * k * (3 - 2 * k); };
  // Base de contenção do Núcleo (JSON), partilhada com o Regulador: 2 andares baixos de metal, nichos, pistas de luz, anel e emissor. Devolve o material do emissor.
  function coreBase(g, sm, wm) {
    // ---------- base de contenção (JSON): 2 andares baixos de metal, com painéis, grelhas e pistas de luz ----------
      cbox(g, -14.5, 0, -14.5, 14.5, 1.5, 14.5, 'dark', 2);
      box(g, -10, 1.5, -10, 10, 7, 10, 'dark'); // miolo, visto pelos nichos
      [[-1, -1], [1, -1], [-1, 1], [1, 1]].forEach(([sx, sz]) => {
        // bloco de canto de baixo: metal com tampa, grelha nas duas faces de fora e filete na aresta
        cbox(g, Math.min(sx * 4, sx * 14), 1.5, Math.min(sz * 4, sz * 14), Math.max(sx * 4, sx * 14), 7, Math.max(sz * 4, sz * 14), 'metal', 1.5);
        cbox(g, Math.min(sx * 4.4, sx * 13.6), 7, Math.min(sz * 4.4, sz * 13.6), Math.max(sx * 4.4, sx * 13.6), 7.8, Math.max(sz * 4.4, sz * 13.6), 'frame', 1.5);
        bx(g, sx * 6, 2.6, sz * 14, sx * 11.5, 5.8, sz * 14.25, 'vent');
        bx(g, sx * 14, 2.6, sz * 6, sx * 14.25, 5.8, sz * 11.5, 'vent');
        const cx = sx * 13.05, cz = sz * 13.05; // chanfro da aresta
        const e = new THREE.Group(); e.position.set(cx * P, 0, cz * P); e.rotation.y = sx * sz > 0 ? -Math.PI / 4 : Math.PI / 4; g.add(e);
        box(e, -.6, 2.2, -.2, .6, 6.4, .25, sm());
        // pista de luz no topo do bloco, a apontar para o centro
        bx(g, sx * 6, 7.8, sz * 9.2, sx * 12, 7.95, sz * 9.7, sm());
        bx(g, sx * 9.2, 7.8, sz * 6, sx * 9.7, 7.95, sz * 9.2, sm());
        // bloco de canto de cima (mais baixo), com parafusos
        cbox(g, Math.min(sx * 3, sx * 10), 7.8, Math.min(sz * 3, sz * 10), Math.max(sx * 3, sx * 10), 11.5, Math.max(sz * 3, sz * 10), 'frame', 1);
        bx(g, sx * 3.4, 11.5, sz * 3.4, sx * 9.6, 12, sz * 9.6, 'panel');
        [[4.3, 8.7], [8.7, 4.3]].forEach(([a, b]) => bx(g, sx * (a - .4), 12, sz * (b - .4), sx * (a + .4), 12.3, sz * (b + .4), 'metal'));
        bx(g, sx * 9.9, 8.6, sz * 9.9, sx * 10.3, 11, sz * 10.3, sm());
      });
      box(g, -7.5, 7.8, -7.5, 7.5, 11.5, 7.5, 'dark'); // miolo de cima
      for (let f = 0; f < 4; f++) {
        const d = new THREE.Group(); d.rotation.y = f * Math.PI / 2; g.add(d);
        // nicho de baixo: moldura e a luz (gema ou fendas) sobre um painel
        box(d, -4, 1.5, 10, -3.2, 7, 10.8, 'frame'); box(d, 3.2, 1.5, 10, 4, 7, 10.8, 'frame'); box(d, -4, 6.2, 10, 4, 7, 10.8, 'frame');
        box(d, -3.2, 1.5, 10, 3.2, 6.2, 10.15, 'panel');
        if (f % 2 === 0) { [[-.8, 2.3, .8, 5.5], [-1.7, 3, 1.7, 4.8], [-2.2, 3.5, 2.2, 4.3]].forEach(([x0, y0, x1, y1]) => box(d, x0, y0, 10.15, x1, y1, 10.6, wm())); }
        else { [2.4, 3.6, 4.8].forEach(y => box(d, -2.4, y, 10.15, 2.4, y + .6, 10.5, wm())); }
        // janela larga do andar de cima, com moldura
        box(d, -3, 8.6, 7.5, 3, 10.8, 7.9, wm()); box(d, -3.4, 8.2, 7.4, 3.4, 8.6, 8.1, 'metal'); box(d, -3.4, 10.8, 7.4, 3.4, 11.2, 8.1, 'metal');
        // cabo de luz que desce do nicho até o rodapé
        box(d, -.3, .4, 14.5, .3, 1.5, 14.7, sm());
      }
      // filete de luz em volta do rodapé
      [[-13, 1.1, 14.5, 13, 1.4, 14.7], [-13, 1.1, -14.7, 13, 1.4, -14.5], [14.5, 1.1, -13, 14.7, 1.4, 13], [-14.7, 1.1, -13, -14.5, 1.4, 13]].forEach(b => box(g, ...b, sm()));
      // topo: deque, anel de metal com luz e 4 pinos emissores
      oct(g, 0, 0, 8, 11.5, 12, 'panel');
      roundRing(g, 7.4, 2.2, 12, 13.4, 'frame', 22);
      roundRing(g, 5.1, .5, 12, 13, sm(), 18);
      roundRing(g, 7.6, .4, 13.4, 13.6, sm(), 22);
      [[-1, -1], [1, -1], [-1, 1], [1, 1]].forEach(([sx, sz]) => {
        const px0 = sx * 6.4, pz0 = sz * 6.4;
        box(g, px0 - .6, 12, pz0 - .6, px0 + .6, 15, pz0 + .6, 'metal'); box(g, px0 - .4, 15, pz0 - .4, px0 + .4, 15.6, pz0 + .4, sm());
      });
      const emit = glow('#E0E7FF'); const emitG = new THREE.Group(); g.add(emitG); [4.5, 3.4, 2].forEach((rr, i) => { const c = new THREE.Mesh(new THREE.CylinderGeometry(rr * P, rr * P, (.6 + i * .15) * P, 20), emit); c.position.y = (12 + (.6 + i * .15) / 2) * P; emitG.add(c); });
      return emit;
  }
  MODELS.push({ id: 'nucleo', name: 'Núcleo Quântico', color: C.purple, size: '3 × 3 × 3 blocos (48 × 48 × 48 px)', radius: 3.3, target: 1.55,
    states: ['Online', 'Ocupado', 'Erro', 'Sem energia'],
    levels2: CORE_PALETTES.map((_, i) => i), levelNames2: CORE_PALETTES.map(p => p.name), levelLabel2: 'Cor',
    info: {
      parts: ['<b>Base de contenção</b> baixa, em 2 andares de metal (29 × 13 px): 4 blocos de canto embaixo com grelhas e filete na aresta chanfrada, 4 menores em cima com parafusos',
        '<b>Nichos</b> no meio de cada face: gema azul num lado, fendas de luz no outro; <b>janelas azuis</b> no andar de cima',
        '<b>Pistas de luz</b> no topo dos blocos e em volta do rodapé, e cabos de luz descendo dos nichos',
        '<b>Anel de metal</b> com filetes de luz no topo, <b>4 pinos emissores</b> e o <b>emissor redondo</b>, de onde sobe a coluna de energia',
        '<b>Núcleo</b>: cristal octaédrico de voxels com miolo branco e halo; <b>2 anéis</b> orbitais finos',
        '<b>4 braços flutuantes</b> (2 longos em C, 2 curtos no alto) de segmentos blindados com faixas de luz, <b>juntas magnéticas</b> e garras viradas para o núcleo'],
      anim: ['<b>Sem energia, o núcleo some</b>: sem cristal, anéis, coluna e raios; os braços descem e ficam parados e apagados',
        '<b>Surgimento</b> (≈ 2,5 s) quando a energia volta: as luzes da base acendem, a coluna sobe do emissor, os voxels do cristal voam de fora e juntam-se com um clarão, os anéis abrem-se, os braços sobem e uma rajada de raios fecha a entrada',
        'Depois: braços giram em volta do núcleo e sobem e descem; o cristal pulsa em forma (espinhos) e em cor; raios saltam até as juntas',
        'Ocupado: mais rápido e mais raios. Erro: vermelho a piscar e braços a tremer',
        '<b>Cor</b>: Magenta + Roxo é a padrão de toda a rede; as outras 5 paletas ficam como opção (o dono escolhe no Núcleo)',
        'Para ver o surgimento: clique em "Sem energia" e depois em "Online"'],
      tech: ['<b>Colisão real nos 27 blocos</b> (1 principal + 26 de estrutura), como hoje',
        'Base em JSON (custo zero por frame); braços, cristal, anéis, coluna e raios no BlockEntityRenderer, só até 64 blocos',
        'O surgimento usa o mesmo relógio "online desde" que o Núcleo atual já tem no código (APPEAR_TICKS)',
        'Tudo cabe na janela de −16 a 32 px do modelo (braços com raio de 17 px)']
    },
    build() {
      const g = new THREE.Group(), CY = 28, r = rng(4242);
      const strips = [], wins = [];
      const sm = () => { const m = glow('#8B5CF6'); strips.push(m); return m; };
      const wm = () => { const m = glow('#3B82F6'); wins.push(m); return m; };
      const emit = coreBase(g, sm, wm);
      // ---------- coluna de energia ----------
      const beamFrom = 12.9, beamLen = (CY - 6 - beamFrom) * P;
      const beamMat = new THREE.MeshBasicMaterial({ color: '#6366F1', transparent: true, opacity: .5, blending: THREE.AdditiveBlending, depthWrite: false });
      const beamG = new THREE.Group(); beamG.position.y = beamFrom * P; g.add(beamG);
      const beam = new THREE.Mesh(new THREE.CylinderGeometry(1 * P, 1.6 * P, beamLen, 10, 1, true), beamMat); beam.position.y = beamLen / 2; beamG.add(beam);
      const beamCore = new THREE.Mesh(new THREE.CylinderGeometry(.35 * P, .35 * P, beamLen, 6), glow('#F5F3FF')); beamCore.position.y = beamLen / 2; beamG.add(beamCore);
      const sparkMat = glow('#C7D2FE'), sparks = [];
      for (let i = 0; i < 8; i++) { const s = box(g, -.35, -.35, -.35, .35, .35, .35, sparkMat); s.userData.o = [(r() - .5) * 2, (r() - .5) * 2, r()]; sparks.push(s); }
      // ---------- núcleo: cristal de voxels que se deforma ----------
      const core = new THREE.Group(); core.position.y = CY * P; g.add(core);
      const crystal = new THREE.Group(); core.add(crystal);
      const S = 1.25, R = 6.5, cpts = [];
      for (let i = -7; i <= 7; i++) for (let j = -8; j <= 8; j++) for (let k = -7; k <= 7; k++) {
        const x = i * S, y = j * S, z = k * S, d = Math.abs(x) + Math.abs(y) * .78 + Math.abs(z);
        if (d <= R && d > R - 2.2) cpts.push([x, y, z]);
      }
      const cryst = new THREE.InstancedMesh(new THREE.BoxGeometry(S * 1.04 * P, S * 1.04 * P, S * 1.04 * P), new THREE.MeshBasicMaterial({ color: '#ffffff' }), cpts.length);
      const cdata = cpts.map(p => { const L = Math.hypot(p[0], p[1], p[2]) || 1; const n = [p[0] / L, p[1] / L, p[2] / L];
        return { p, n, ph: r() * Math.PI * 2, delay: r() * .35, fly: [n[0] * (9 + r() * 8) + (r() - .5) * 6, n[1] * (9 + r() * 8) + (r() - .5) * 6, n[2] * (9 + r() * 8) + (r() - .5) * 6] }; });
      const M = new THREE.Matrix4(), col = new THREE.Color(), cA = new THREE.Color(), cB = new THREE.Color(), cH = new THREE.Color(), WHITE = new THREE.Color('#ffffff'), RED = new THREE.Color(C.red);
      cdata.forEach((_, i) => cryst.setColorAt(i, col.set('#ffffff'))); crystal.add(cryst);
      const innerMat = glow('#F5F3FF'); const inner = voxels(spherePoints(2.6), 1.05, innerMat); crystal.add(inner);
      const h = halo('rgba(124,58,237,0.85)', 2.2); core.add(h);
      const ringMat = glow('#818CF8');
      function thinRing(R, tilt) { const t = new THREE.Group(); t.rotation.set(tilt[0], 0, tilt[1]); const rg = new THREE.Group(); t.add(rg); rg.add(voxels(ringPoints(R, .9).filter((_, i) => i % 4 !== 0), .55, ringMat)); core.add(t); return rg; }
      const ring1 = thinRing(10, [Math.PI / 2.6, .25]), ring2 = thinRing(12, [-Math.PI / 3, -.4]);
      // ---------- braços flutuantes ----------
      const armsRoot = new THREE.Group(); armsRoot.position.y = CY * P; g.add(armsRoot);
      const ARMS = [{ yaw: 0, a0: -35, a1: 68 }, { yaw: Math.PI / 2, a0: 40, a1: 72 }, { yaw: Math.PI, a0: -35, a1: 68 }, { yaw: Math.PI * 1.5, a0: 40, a1: 72 }];
      const Ra = 17, arms = [], allJoints = [];
      ARMS.forEach((A, k) => {
        const yawG = new THREE.Group(); yawG.rotation.y = A.yaw; armsRoot.add(yawG);
        const arm = new THREE.Group(); yawG.add(arm);
        const n = Math.max(2, Math.round((A.a1 - A.a0) / 21)), step = (A.a1 - A.a0) / n, L = step * Math.PI / 180 * Ra - 1.6;
        const segs = [], joints = [];
        for (let i = 0; i < n; i++) {
          const a = (A.a0 + (i + .5) * step) * Math.PI / 180;
          const seg = new THREE.Group(); seg.userData.a = a; seg.rotation.z = a; arm.add(seg);
          // eixo x local = para fora do núcleo, y = ao longo do braço, z = largura
          box(seg, -2, -L / 2, -2.4, 2, L / 2, 2.4, 'metal');
          box(seg, -.6, -L / 2 + .5, -2.7, 2.4, L / 2 - .5, 2.7, 'frame');
          box(seg, 2.4, -L / 2 + 1, -.7, 2.7, L / 2 - 1, .7, sm());
          box(seg, -2.3, -L / 2 + 1.2, -.45, -2, L / 2 - 1.2, .45, sm());
          if (i === n - 1) { box(seg, -4.4, L / 2 - 1.2, -1.9, -1.5, L / 2 + 1.3, 1.9, 'frame'); box(seg, -5, L / 2 - .7, -1, -4.4, L / 2 + .8, 1, sm()); }
          if (i === 0 && A.a0 < 0) { box(seg, -3.8, -L / 2 - 1.1, -1.7, -1.3, -L / 2 + 1, 1.7, 'frame'); box(seg, -4.3, -L / 2 - .6, -.8, -3.8, -L / 2 + .6, .8, sm()); }
          segs.push(seg);
          if (i < n - 1) { const j = box(arm, -.75, -.75, -.75, .75, .75, .75, glow('#A5B4FC')); j.userData.a = (A.a0 + (i + 1) * step) * Math.PI / 180; joints.push(j); allJoints.push(j); }
        }
        arms.push({ arm, segs, joints, k });
      });
      // ---------- raios ----------
      const bolts = [];
      for (let b = 0; b < 8; b++) {
        const mk = (color) => { const geo = new THREE.BufferGeometry(); geo.setAttribute('position', new THREE.BufferAttribute(new Float32Array(9 * 3), 3)); const l = new THREE.Line(geo, new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0, blending: THREE.AdditiveBlending, depthWrite: false })); g.add(l); return l; };
        bolts.push({ a: mk('#ffffff'), b: mk('#C7D2FE'), life: 0, max: .2 });
      }
      const v1 = new THREE.Vector3(), v2 = new THREE.Vector3(), tmp = new THREE.Vector3(), perp = new THREE.Vector3(), side = new THREE.Vector3(), dir = new THREE.Vector3();
      function spawnBolt(bolt) {
        dir.set(r() - .5, r() - .5, r() - .5).normalize();
        v1.set(0, CY, 0).addScaledVector(dir, 6);
        if (allJoints.length && r() < .45) { allJoints[Math.floor(r() * allJoints.length)].getWorldPosition(v2); v2.divideScalar(P); }
        else v2.set(0, CY, 0).addScaledVector(dir, 13 + r() * 5);
        tmp.copy(v2).sub(v1); perp.set(r() - .5, r() - .5, r() - .5).cross(tmp).normalize(); side.crossVectors(perp, tmp).normalize();
        [bolt.a, bolt.b].forEach((line, li) => {
          const arr = line.geometry.attributes.position.array;
          for (let j = 0; j < 9; j++) {
            const f = j / 8, env = Math.sin(Math.PI * f) * (2 + li * .4);
            tmp.copy(v1).lerp(v2, f).addScaledVector(perp, (r() - .5) * 2 * env).addScaledVector(side, (r() - .5) * env);
            arr[j * 3] = tmp.x * P; arr[j * 3 + 1] = tmp.y * P; arr[j * 3 + 2] = tmp.z * P;
          }
          line.geometry.attributes.position.needsUpdate = true;
        });
        bolt.max = .14 + r() * .16; bolt.life = bolt.max;
      }
      // ---------- animação ----------
      // Surgimento: começa quando a energia volta (e na primeira vez que o modelo aparece). Estágios de 0 a 1 em APPEAR s.
      const APPEAR = 2.6;
      let lastT = null, spin = 0, crystSpin = 0, spawnAcc = 0, spikeDir = new THREE.Vector3(0, 1, 0), spikeAt = 0, wasOff = true, appearAt = 0, burst = false;
      return { group: g, update(t, st, _f, variant) {
        const dt = lastT === null ? 0 : Math.min(.05, Math.max(0, t - lastT)); lastT = t;
        const pal = CORE_PALETTES[variant | 0] || CORE_PALETTES[0];
        const off = st === 3, err = st === 2, busy = st === 1;
        if (!off && wasOff) { appearAt = t; burst = false; }
        wasOff = off;
        const p = off ? 0 : Math.min(1, (t - appearAt) / APPEAR);
        // estágios do surgimento
        const sLights = smooth(p, 0, .2), sBeam = smooth(p, .12, .35), sCrystal = smooth(p, .28, .62), sFlash = p > .55 && p < .8 ? Math.sin((p - .55) / .25 * Math.PI) : 0;
        const sRings = smooth(p, .5, .75), sArms = smooth(p, .55, .95), sBolts = smooth(p, .62, .7);
        const spd = busy ? 2 : 1, flick = err && Math.sin(t * 14) > 0;
        cA.set(pal.mid); cB.set(pal.bright); cH.set(pal.hi);
        const dark = '#1E1B2E';
        const lightCol = (c) => { if (off) return col.set(dark); col.set(flick ? C.red : c); return col.lerp(tmpC.set(dark), 1 - sLights); };
        strips.forEach(m => m.color.copy(lightCol(pal.strip)));
        wins.forEach(m => m.color.copy(lightCol(pal.win)));
        emit.color.copy(lightCol(pal.hi));
        // coluna: sobe do emissor
        beamG.visible = !off && sBeam > 0; beamG.scale.y = Math.max(.001, sBeam);
        beamMat.color.set(flick ? C.red : pal.win); beamMat.opacity = (.3 + .25 * Math.sin(t * 5 * spd)) * (1 + sFlash); beamCore.material.color.set(pal.hi);
        sparkMat.color.set(flick ? C.red : pal.bolt);
        sparks.forEach(s => { const o = s.userData.o, span = CY - 7 - 14; s.visible = !off && sBeam >= 1; s.position.set(o[0] * P, (14 + ((t * 9 * spd + o[2] * span) % span)) * P, o[1] * P); });
        // núcleo: some sem energia; no surgimento os voxels voam de fora e juntam-se
        core.visible = !off && sCrystal > 0;
        if (core.visible) {
          crystSpin += dt * .6 * spd * (.4 + sCrystal);
          crystal.rotation.set(.22 * Math.sin(t * .7), crystSpin, .15 * Math.sin(t * .5 + 1));
          if (t > spikeAt) { spikeDir.set(r() - .5, r() - .5, r() - .5).normalize(); spikeAt = t + 1.6 + r() * 1.4; }
          const spikeEnv = Math.max(0, Math.sin((spikeAt - t) / 3 * Math.PI)) * (busy ? 1.4 : 1) * sCrystal;
          const base = .93 + .09 * Math.sin(t * 2.4 * spd);
          cdata.forEach((d, i) => {
            const n = d.n, e = smooth(sCrystal, d.delay, d.delay + .65);
            let def = 1 + (.13 * Math.sin(t * 2.1 * spd + n[0] * 2.6 + d.ph) * Math.cos(t * 1.4 * spd + n[1] * 3.1 - n[2] * 2.2)) * sCrystal;
            const dot = n[0] * spikeDir.x + n[1] * spikeDir.y + n[2] * spikeDir.z;
            if (dot > 0) def += .35 * Math.pow(dot, 6) * spikeEnv;
            const s = base * def, sc = (1 + .15 * Math.sin(t * 3 * spd + d.ph)) * e;
            const k = 1 - e;
            M.makeScale(sc, sc, sc); M.setPosition((d.p[0] * s + d.fly[0] * k) * P, (d.p[1] * s + d.fly[1] * k) * P, (d.p[2] * s + d.fly[2] * k) * P); cryst.setMatrixAt(i, M);
            const w = .5 + .5 * Math.sin(t * 1.7 * spd + d.ph * 2 + d.p[1] * .35);
            col.copy(cA).lerp(cB, w);
            if (Math.sin(t * 4.5 * spd + d.ph * 13) > .9) col.lerp(cH, .8);
            if (k > 0) col.lerp(cH, k * .7); // chega claro e escurece ao encaixar
            if (sFlash > 0) col.lerp(WHITE, sFlash * .6);
            if (flick) col.lerp(RED, .7);
            cryst.setColorAt(i, col);
          });
          cryst.instanceMatrix.needsUpdate = true; cryst.instanceColor.needsUpdate = true;
          innerMat.color.set(flick ? '#FFE4E6' : '#ffffff'); inner.scale.setScalar(smooth(sCrystal, .3, 1) * (1 + .25 * Math.sin(t * 3.6 * spd) + sFlash * .8));
          h.material.color.set(flick ? C.red : pal.strip); h.material.opacity = sCrystal * (.65 + .3 * Math.sin(t * 2.4 * spd)) + sFlash * .5;
          h.scale.setScalar(2.2 * (1 + .12 * Math.sin(t * 2.4 * spd) + sFlash * 1.2));
          ringMat.color.set(flick ? C.red : pal.win);
          ring1.scale.setScalar(Math.max(.001, sRings)); ring2.scale.setScalar(Math.max(.001, smooth(p, .58, .82)));
          ring1.rotation.y += dt * 1.1 * spd * (1 + (1 - sRings) * 3); ring2.rotation.y -= dt * .8 * spd * (1 + (1 - sRings) * 3);
        }
        // braços: parados e apagados sem energia; no surgimento sobem e acendem; depois giram e sobem e descem
        spin += dt * .35 * spd * sArms;
        armsRoot.rotation.y = spin;
        arms.forEach(({ arm, segs, joints, k }) => {
          const rest = -3.5, live = 1.6 * Math.sin(t * 1.2 * spd + k * Math.PI / 2) * sArms;
          const bob = rest * (1 - sArms) + live;
          arm.position.set(err ? (r() - .5) * .5 * P : 0, bob * P, err ? (r() - .5) * .5 * P : 0);
          arm.rotation.z = -.1 * (1 - sArms) + .08 * Math.sin(t * .8 * spd + k * 1.3) * sArms;
          const gap = (i) => -.6 * (1 - sArms) + .55 * Math.sin(t * 2.2 * spd - i * .8 + k) * sArms;
          segs.forEach((seg, i) => { const rr = Ra + gap(i); seg.position.set(rr * Math.cos(seg.userData.a) * P, rr * Math.sin(seg.userData.a) * P, 0); });
          joints.forEach((j, i) => {
            const rr = Ra + gap(i + .5); j.position.set(rr * Math.cos(j.userData.a) * P, rr * Math.sin(j.userData.a) * P, 0);
            const pulse = Math.max(0, Math.sin(t * 4 * spd - i * 1.2 + k * 1.7)) * sArms;
            j.material.color.copy(lightCol(pal.strip)).lerp(cH, off || flick ? 0 : pulse * .8);
            j.scale.setScalar(.7 + .3 * sArms + .35 * pulse);
          });
        });
        // raios: rajada no fim do surgimento, depois ritmo normal
        let rate = off ? 0 : (busy ? 9 : err ? 6 : 3.5) * sBolts;
        if (!off && !burst && sBolts > 0) { burst = true; spawnAcc += 6; }
        spawnAcc = Math.min(8, spawnAcc + dt * rate);
        bolts.forEach(b => {
          if (b.life <= 0 && spawnAcc >= 1 && !off) { spawnAcc -= 1; spawnBolt(b); }
          b.life -= dt; const o = off ? 0 : Math.max(0, b.life / b.max);
          b.a.material.opacity = o; b.b.material.opacity = o * .8;
          b.a.material.color.set(flick ? '#FFE4E6' : '#ffffff'); b.b.material.color.set(flick ? C.red : pal.bolt);
        });
        if (off) spawnAcc = 0;
      } };
    }
  });
  const tmpC = new THREE.Color();

