  // ===== Peças comuns dos blocos de 1 × 2 no estilo do Núcleo (base baixa de metal, luzes da paleta, surgimento) =====
  // Cor de um medidor: a cor do tipo até 80%, âmbar até 95%, vermelho a partir daí.
  function gaugeColor(f, base) { return f >= .95 ? C.red : f >= .8 ? '#FBBF24' : base; }
  const TYPE = [['Itens', C.item], ['Fluidos', C.fluid], ['Químicos', C.chem]];
  // Base de 1 bloco com h px: rodapé, corpo de metal com grelhas, filetes nas arestas chanfradas e tampa.
  function slimBase(g, h, sm) {
    cbox(g, -8, 0, -8, 8, 1.2, 8, 'dark', 1.5);
    cbox(g, -7.4, 1.2, -7.4, 7.4, h, 7.4, 'metal', 1.5);
    cbox(g, -7.8, h, -7.8, 7.8, h + .7, 7.8, 'frame', 1.5);
    for (let f = 0; f < 4; f++) {
      const d = new THREE.Group(); d.rotation.y = f * Math.PI / 2; g.add(d);
      box(d, -4.5, 1.8, 7.4, 4.5, h - .6, 7.6, 'vent'); box(d, -5, .5, 7.95, 5, .85, 8.1, sm());
    }
    [[-1, -1], [1, -1], [-1, 1], [1, 1]].forEach(([sx, sz]) => {
      const e = new THREE.Group(); e.position.set(sx * 6.95 * P, 0, sz * 6.95 * P); e.rotation.y = sx * sz > 0 ? -Math.PI / 4 : Math.PI / 4; g.add(e);
      box(e, -.45, 1.6, -.15, .45, h - .3, .2, sm());
    });
  }
  // Raios: linhas em zigue-zague de vida curta, entre dois pontos em px do grupo pai.
  function makeBolts(parent, n) {
    const list = [];
    for (let b = 0; b < n; b++) {
      const mk = (color) => { const geo = new THREE.BufferGeometry(); geo.setAttribute('position', new THREE.BufferAttribute(new Float32Array(9 * 3), 3)); const l = new THREE.Line(geo, new THREE.LineBasicMaterial({ color, transparent: true, opacity: 0, blending: THREE.AdditiveBlending, depthWrite: false })); parent.add(l); return l; };
      list.push({ a: mk('#ffffff'), b: mk('#A5F3FC'), life: 0, max: .2 });
    }
    return list;
  }
  const _bt = new THREE.Vector3(), _bp = new THREE.Vector3(), _bs = new THREE.Vector3(), _bd = new THREE.Vector3();
  function fireBolt(bolt, from, to, amp, r) {
    _bd.copy(to).sub(from); _bp.set(r() - .5, r() - .5, r() - .5).cross(_bd).normalize(); _bs.crossVectors(_bp, _bd).normalize();
    [bolt.a, bolt.b].forEach((line, li) => {
      const arr = line.geometry.attributes.position.array;
      for (let j = 0; j < 9; j++) { const f = j / 8, env = Math.sin(Math.PI * f) * amp * (1 + li * .2);
        _bt.copy(from).lerp(to, f).addScaledVector(_bp, (r() - .5) * 2 * env).addScaledVector(_bs, (r() - .5) * env);
        arr[j * 3] = _bt.x * P; arr[j * 3 + 1] = _bt.y * P; arr[j * 3 + 2] = _bt.z * P; }
      line.geometry.attributes.position.needsUpdate = true;
    });
    bolt.max = .12 + r() * .14; bolt.life = bolt.max;
  }
  function fadeBolts(list, dt, off, color) {
    list.forEach(b => { b.life -= dt; const o = off ? 0 : Math.max(0, b.life / b.max); b.a.material.opacity = o; b.b.material.opacity = o * .8; b.b.material.color.set(color); });
  }
  // Relógio do surgimento: devolve o progresso 0..1 desde que a energia voltou (0 sem energia).
  function appearClock(dur) {
    let wasOff = true, at = 0;
    return (t, off) => { if (!off && wasOff) at = t; wasOff = off; return off ? 0 : Math.min(1, (t - at) / dur); };
  }
  // Braços flutuantes da família do Núcleo (2 longos em C e 2 curtos no alto), em volta de uma figura no centro (CY px).
  // Peça comum: o Regulador, a Bateria e o Processador usam a mesma. update() anima; joints servem de ponta para raios.
  function quantumArms(g, CY, sm, Ra = 13.6, opt = {}) { // opt.short = false tira os curtos; opt.long = false deixa só 4 curtos (a segurar algo no alto); opt.a1 = fim dos longos
    const root = new THREE.Group(); root.position.y = CY * P; g.add(root);
    const arms = [], joints = [], r = rng(4141), tc = new THREE.Color();
    const A1 = opt.a1 ?? 58, defs = opt.long === false ? [] : [{ yaw: Math.PI / 4, a0: -38, a1: A1, n: 3 }, { yaw: Math.PI * 1.25, a0: -38, a1: A1, n: 3 }];
    if (opt.long === false) [1, 3, 5, 7].forEach(k => defs.push({ yaw: k * Math.PI / 4, a0: 28, a1: 58, n: 2 }));
    else if (opt.short !== false) defs.push({ yaw: Math.PI * .75, a0: 28, a1: 58, n: 2 }, { yaw: -Math.PI / 4, a0: 28, a1: 58, n: 2 });
    defs.forEach(({ yaw, a0, a1, n }, k) => {
      const step = (a1 - a0) / n, L = step * Math.PI / 180 * Ra - 1.3;
      const yawG = new THREE.Group(); yawG.rotation.y = yaw; root.add(yawG);
      const arm = new THREE.Group(); yawG.add(arm); const ss = [], js = [];
      for (let i = 0; i < n; i++) {
        const a = (a0 + (i + .5) * step) * Math.PI / 180, seg = new THREE.Group(); seg.userData.a = a; seg.rotation.z = a; arm.add(seg);
        box(seg, -1.4, -L / 2, -1.8, 1.4, L / 2, 1.8, 'metal'); box(seg, -.4, -L / 2 + .4, -2, 1.7, L / 2 - .4, 2, 'frame');
        box(seg, 1.7, -L / 2 + .8, -.5, 1.95, L / 2 - .8, .5, sm());
        if (i === n - 1) { box(seg, -3, L / 2 - .9, -1.3, -1.1, L / 2 + 1, 1.3, 'frame'); box(seg, -3.4, L / 2 - .4, -.6, -3, L / 2 + .6, .6, sm()); }
        if (i === 0 && a0 < 0) { box(seg, -2.8, -L / 2 - .9, -1.2, -1.1, -L / 2 + .7, 1.2, 'frame'); box(seg, -3.2, -L / 2 - .5, -.6, -2.8, -L / 2 + .4, .6, sm()); }
        ss.push(seg);
        if (i < n - 1) { const j = box(arm, -.7, -.7, -.7, .7, .7, .7, glow('#A5B4FC')); j.userData.a = (a0 + (i + 1) * step) * Math.PI / 180; js.push(j); joints.push(j); }
      }
      arms.push({ arm, ss, js, k });
    });
    let spin = 0;
    return { joints, update(t, dt, sArms, o) { // o: { off, err, flick, lc, pal, speed }
      spin += dt * (o.speed ?? .2) * sArms; root.rotation.y = spin;
      arms.forEach(({ arm, ss, js, k }) => {
        const bob = -4 * (1 - sArms) + Math.sin(t * 1.1 + k * Math.PI / 2) * sArms;
        arm.position.set(o.err ? (r() - .5) * .5 * P : 0, bob * P, o.err ? (r() - .5) * .5 * P : 0);
        arm.rotation.z = -.12 * (1 - sArms) + .05 * Math.sin(t * .8 + k * 1.3) * sArms;
        const gap = i => -.5 * (1 - sArms) + .4 * Math.sin(t * 2 - i * .8 + k) * sArms;
        ss.forEach((seg, i) => { const rr = Ra + gap(i); seg.position.set(rr * Math.cos(seg.userData.a) * P, rr * Math.sin(seg.userData.a) * P, 0); });
        js.forEach((j, i) => { const rr = Ra + gap(i + .5); j.position.set(rr * Math.cos(j.userData.a) * P, rr * Math.sin(j.userData.a) * P, 0);
          const pl = Math.max(0, Math.sin(t * 3 - i * 1.2 + k * 1.7)) * sArms; j.material.color.set(o.lc(o.pal.strip)); if (!o.off && !o.flick) j.material.color.lerp(tc.set(o.pal.hi), pl * .7); });
      });
    } };
  }

