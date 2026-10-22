  // ===== Regulador de Estoque 3 x 3 x 3 — linguagem do Núcleo: tesserato (hipercubo 4D) de arestas de luz com o item dentro =====
  MODELS.push(Object.assign({ id: 'regulador', name: 'Regulador de Estoque', color: C.trim, size: '3 × 3 × 3 blocos (48 × 48 × 48 px)', radius: 3.6, target: 1.6, states: ['Estoque ok', 'Pedindo craft', 'Sem receita', 'Sem energia'],
    info: {
      parts: ['<b>A mesma base de contenção do Núcleo</b>: 2 andares baixos de metal, nichos com gema e fendas de luz, janelas, pistas de luz, anel e emissor no topo',
                '<b>Tesserato</b>: hipercubo 4D projetado (um cubo dentro de outro, ligados pelas arestas), só com 32 arestas de luz, a flutuar',
        '<b>Item regulado</b> grande a girar devagar no centro do cristal',
        'As arestas <b>acendem de baixo para cima</b> até ao estoque atual; as arestas na <b>altura do alvo</b> "manter N" brilham em branco-azulado (sem anel à volta)',
        '<b>2 braços flutuantes</b> em C, como os do Núcleo, a segurar o cristal'],
      anim: ['O tesserato gira na 4.ª dimensão: o cubo de dentro passa para fora e o de fora para dentro, sem parar', 'Estoque ok: arestas na cor da paleta acima do anel; tudo calmo',
        'Pedindo craft: arestas laranja abaixo do anel, o nível sobe até ao alvo e saltam raios dos braços para o cristal',
        'Sem receita: vermelho a piscar e braços a tremer',
        '<b>Sem energia</b>: tesserato, item e anel somem; os braços descem apagados',
        '<b>Surgimento</b>: as arestas desenham-se de baixo para cima, o item aparece num clarão e os braços sobem'],
      tech: ['1 principal + 26 de estrutura (3 × 3 × 3), colisão real, como o Núcleo e o Colapsador', 'Um só por base: guarda um número grande de pedidos (regras)', 'A figura mostra a regra com mais falta',
        'Base em JSON; cristal, item, anel, braços e raios num BlockEntityRenderer (o item usa o renderizador de itens do jogo)']
    },
    build() {
      const G = new THREE.Group(), g = new THREE.Group(), r = rng(1515), strips = []; g.scale.setScalar(1.5); G.add(g); // desenhado em 2×2×2 e ampliado para 3×3×3
      const sm = () => { const m = glow('#D946EF'); strips.push(m); return m; };
      const CY = 19.5;
      // ---------- base: a mesma do Núcleo (3 × 3, sem ampliar) ----------
      const wins = []; const wm = () => { const m = glow('#38BDF8'); wins.push(m); return m; };
      const emitM = coreBase(G, sm, wm);
      // ---------- tesserato: hipercubo 4D (16 vértices, 32 arestas) projetado em 3D; cada aresta em 3 troços ----------
      const V4 = []; for (let i = 0; i < 16; i++) V4.push([i & 1 ? 1 : -1, i & 2 ? 1 : -1, i & 4 ? 1 : -1, i & 8 ? 1 : -1]);
      const E4 = []; for (let i = 0; i < 16; i++) for (let b = 0; b < 4; b++) { const j = i ^ (1 << b); if (j > i) E4.push([i, j]); }
      const S4 = 3.5, D4 = 3, HB = CY - 7, HT = CY + 7, NS = 3;
      const core = new THREE.Group(); core.position.y = CY * P; g.add(core);
      const crystal = new THREE.Group(); core.add(crystal);
      const unit = new THREE.BoxGeometry(.85 * P, .85 * P, P), P3 = V4.map(() => new THREE.Vector3());
      const segs = []; E4.forEach(([a, b]) => { for (let i = 0; i < NS; i++) { const m = glow('#ffffff'), mesh = new THREE.Mesh(unit, m); crystal.add(mesh); segs.push({ mesh, m, a, b, i, h: 0 }); } });
      const vtx = V4.map(() => box(crystal, -.75, -.75, -.75, .75, .75, .75, glow('#ffffff')));
      const Z = new THREE.Vector3(0, 0, 1), A = new THREE.Vector3(), B = new THREE.Vector3(), Dv = new THREE.Vector3();
      function project(ang) { // gira nos planos XW e YZ e projeta com perspetiva a partir da 4.ª dimensão
        const c = Math.cos(ang), s = Math.sin(ang), c2 = Math.cos(ang * .5), s2 = Math.sin(ang * .5);
        V4.forEach((v, i) => { const x = v[0] * c - v[3] * s, w = v[0] * s + v[3] * c, y = v[1] * c2 - v[2] * s2, z = v[1] * s2 + v[2] * c2, k = D4 / (D4 - w) * S4; P3[i].set(x * k, y * k, z * k); });
      }
      const ig = new THREE.Group(); ig.add(itemSprite(INGOT16, IRON, 7)); core.add(ig);
      const glowH = halo('rgba(124,58,237,0.5)', 1.6); core.add(glowH);
      const flash = halo('rgba(255,255,255,0.95)', 1); core.add(flash);
      const armsQ = quantumArms(g, CY, sm), joints = armsQ.joints;
      const bolts = makeBolts(g, 2), v1 = new THREE.Vector3(), v2 = new THREE.Vector3();
      const clock = appearClock(2.4), tc = new THREE.Color(), DIM = '#2A2440';
      let lastT = null, fill = -1;
      return { group: G, update(t, st, _f, variant) {
        const dt = lastT === null ? 0 : Math.min(.05, Math.max(0, t - lastT)); lastT = t;
        const pal = CORE_PALETTES[variant | 0] || CORE_PALETTES[0], off = st === 3, craft = st === 1, err = st === 2, flick = err && Math.sin(t * 12) > 0;
        const p = clock(t, off), sL = smooth(p, 0, .2), sCr = smooth(p, .2, .6), sItem = smooth(p, .55, .75), sArms = smooth(p, .55, .95);
        const lc = (c, k = sL) => off ? '#1E1B2E' : '#' + new THREE.Color('#1E1B2E').lerp(new THREE.Color(flick ? C.red : c), k).getHexString();
        strips.forEach(m => m.color.set(lc(pal.strip))); wins.forEach(m => m.color.set(lc(pal.win))); emitM.color.set(lc(pal.hi));
        // nível: ok acima do alvo; pedindo craft sobe até ao alvo e recomeça; sem receita fica baixo
        const goal = .6, want = st === 0 ? .85 : craft ? .15 + (goal - .15) * ((t * .12) % 1) : .2;
        fill = fill < 0 || !dt ? want : fill + (want - fill) * Math.min(1, dt * 3);
        core.visible = !off && sCr > 0;
        const ang = t * (craft ? .55 : .3); project(ang); crystal.rotation.y = t * .15;
        const onC = craft ? C.trim : err ? (flick ? C.red : '#5B1D1D') : pal.strip;
        segs.forEach(sg => { A.copy(P3[sg.a]).lerp(P3[sg.b], sg.i / NS); B.copy(P3[sg.a]).lerp(P3[sg.b], (sg.i + 1) / NS); Dv.subVectors(B, A);
          const len = Math.max(.01, Dv.length() - .2); sg.mesh.position.copy(A).add(B).multiplyScalar(.5 * P); sg.mesh.quaternion.setFromUnitVectors(Z, Dv.normalize()); sg.mesh.scale.z = len;
          sg.h = Math.min(1, Math.max(0, (CY + (A.y + B.y) / 2 - HB) / (HT - HB))); sg.mesh.visible = sg.h <= sCr * 1.05;
          if (Math.abs(sg.h - goal) < .045 && sCr >= 1) { sg.m.color.set(flick ? C.red : pal.hi).lerp(tc.set('#ffffff'), .35 + .25 * Math.sin(t * 4)); } else if (sg.h <= fill) { sg.m.color.set(onC); sg.m.color.lerp(tc.set(pal.hi), .25 * Math.max(0, Math.sin(t * 3 - sg.h * 8))); } else sg.m.color.set(DIM).lerp(tc.set(pal.win), .2); });
        vtx.forEach((m, i) => { m.position.copy(P3[i]).multiplyScalar(P); m.visible = (CY + P3[i].y - HB) / (HT - HB) <= sCr * 1.05; m.material.color.set(flick ? C.red : pal.hi); });
        glowH.material.color.set(flick ? C.red : pal.strip); glowH.material.opacity = sCr * (.35 + .15 * Math.sin(t * 2));
        ig.visible = sItem > 0; ig.scale.setScalar(Math.max(.001, sItem)); ig.rotation.y = t * .7; ig.position.y = .5 * Math.sin(t * 1.6) * P;
        const fl = sItem > 0 && sItem < 1 ? Math.sin(sItem * Math.PI) : 0; flash.visible = fl > 0; flash.scale.setScalar(.4 + fl * 1.2);
        armsQ.update(t, dt, sArms, { off, err, flick, lc, pal });
        // raios dos braços para o cristal, só ao pedir craft
        if (!off && craft && p >= 1 && r() < .05) { const b = bolts.find(x => x.life <= 0);
          if (b) { joints[Math.floor(r() * joints.length)].getWorldPosition(v1); g.worldToLocal(v1); v1.divideScalar(P); v2.set(0, CY + (r() - .5) * 8, 0); fireBolt(b, v1, v2, 1.2, r); } }
        fadeBolts(bolts, dt || 1 / 60, off, pal.bolt);
      } };
    }
  }, PALETTE_ROW));

