  // ===== Manipulador Quântico: emissor com íris, anel e garras flutuantes (Colocador / Quebrador) =====
  MODELS.push(Object.assign({ id: 'manipulador', name: 'Manipulador Quântico', color: C.trim, size: '1 bloco (16 × 16 × 16 px) + alvo à frente', radius: 1.9, target: .55,
    states: ['Retirar bloco', 'Colocar bloco', 'Retirar fluido', 'Colocar fluido', 'Parado', 'Sem energia'],
    info: {
      parts: ['<b>Base baixa de metal</b> com grelhas e filetes de luz (família do Núcleo) e corpo com a frente aberta para o emissor',
        '<b>Anel emissor</b> redondo na frente, com uma <b>íris</b> de 8 lâminas e uma <b>lente de cristal</b>: a cor diz o que mexe — <b>laranja = blocos</b>, <b>azul = fluidos</b>',
        '<b>4 garras flutuantes</b> nos cantos, com almofadas de luz viradas para a lente',
        '<b>3 setas</b> em cada lateral: correm para a frente ao <b>colocar</b> e para trás ao <b>retirar</b>',
        '<b>2 encaixes de melhoria</b> no topo: <b>Toque de Seda</b> (branco) e <b>Fortuna</b> (verde)'],
      anim: ['A íris gira e abre ou fecha; o cristal gira e pulsa; as garras avançam e recuam',
        'Retirar: o bloco ou fluido à frente encolhe e as partículas entram na lente. Colocar: cresce a partir da lente',
        'Feixe da lente até ao alvo, na cor do modo. Parado: tudo em tom baixo, sem feixe',
        '<b>Sem energia</b>: íris, cristal, feixe e alvo somem e as garras recolhem para dentro do corpo',
        '<b>Surgimento</b>: filetes e anel acendem, as garras saem, a íris abre e o cristal materializa-se no centro'],
      tech: ['Quebra com os mesmos drops de uma picareta com os encantamentos das melhorias (Fortuna I–III, Toque de Seda); tudo vai para a rede',
        'Fluidos: retira fontes para o pool de fluidos e coloca fontes a partir dele', 'Respeita proteções de terreno (FTB Chunks, claims): age como um jogador falso do dono da rede',
        'Corpo, anel e garras em JSON; íris, cristal, feixe e partículas num BlockEntityRenderer; a paleta da rede dá a cor das luzes']
    },
    build() {
      const g = new THREE.Group(), b = new THREE.Group(); g.add(b); b.position.set(0, 0, -8 * P); // o bloco ocupa z -16..0; o alvo fica à frente
      const strips = []; const sm = () => { const m = glow('#D946EF'); strips.push(m); return m; };
      slimBase(b, 3.5, sm);
      // corpo: caixa chanfrada com a frente em z = 3,2; o anel e as garras saem dela
      cbox(b, -6.6, 4.2, -7.6, 6.6, 12.4, 3.2, 'metal', 1.2);
      box(b, -5.6, 12.4, -7, 5.6, 12.7, -6.4, 'frame');
      // setas laterais (formam um ">")
      const chevs = [];
      [-6.6, 6.6].forEach(x => { for (let i = 0; i < 3; i++) { const z = -5.6 + i * 3, m = glow('#2E3340'); box(b, x - .25, 8.5, z, x + .35, 9.5, z + 1.6, m); box(b, x - .25, 7.5, z + 1, x + .35, 8.5, z + 2.4, m); box(b, x - .25, 6.5, z, x + .35, 7.5, z + 1.6, m); chevs.push({ m, i }); } });
      // encaixes de melhoria no topo
      box(b, -5.2, 12.7, -5.5, -1, 13, -.5, 'panel'); box(b, 1, 12.7, -5.5, 5.2, 13, -.5, 'panel');
      const silk = box(b, -4.7, 13, -5, -1.5, 13.25, -1, glow('#F0F9FF')), fort = box(b, 1.5, 13, -5, 4.7, 13.25, -1, glow('#34D399'));
      // cabeça: grupo virado para a frente (rotation.x = 90° leva o eixo y local para +z)
      const head = new THREE.Group(); head.position.set(0, 8.3 * P, 3.2 * P); head.rotation.x = Math.PI / 2; b.add(head);
      roundRing(head, 4, .8, 0, 1.2, 'frame', 18);
      const ringM = glow('#D946EF'); roundRing(head, 3.2, .3, .3, 1.5, ringM, 18);
      roundRing(head, 3, 3, .1, .7, 'dark', 18);
      const irisG = new THREE.Group(); irisG.position.y = .9 * P; head.add(irisG); const irisM = glow(C.trim);
      for (let i = 0; i < 8; i++) { const s = new THREE.Group(); s.rotation.y = i / 8 * Math.PI * 2; irisG.add(s); box(s, -.4, 0, 1.9, .4, .6, 3, irisM); }
      const lensM = glow(C.trim); roundRing(head, 1.5, 1.5, .65, 1.2, lensM, 12);
      const crystal = new THREE.Group(); crystal.position.y = 1.9 * P; head.add(crystal); const crysM = glow('#FFFFFF'); box(crystal, -.6, -.6, -.6, .6, .6, .6, crysM);
      // garras flutuantes nos 4 cantos
      const prongs = [], padM = glow('#38BDF8');
      [[-1, -1], [1, -1], [-1, 1], [1, 1]].forEach(([sx, sy]) => {
        const pg = new THREE.Group(); pg.position.set(sx * 5.4 * P, (8.3 + sy * 3) * P, 0); b.add(pg);
        box(pg, -.7, -.7, 2, .7, .7, 6.8, 'frame'); box(pg, -.9, -.9, 5.6, .9, .9, 6.4, 'metal');
        bx(pg, -sx * .7, -.5, 3.6, -sx * .95, .5, 6, padM); prongs.push(pg);
      });
      // alvo no bloco da frente, feixe e partículas
      const tgt = new THREE.Group(); tgt.position.set(0, 8 * P, 16 * P); b.add(tgt);
      const blockM = new THREE.MeshLambertMaterial({ map: TEX.crate }), waterM = new THREE.MeshLambertMaterial({ color: '#2563EB', transparent: true, opacity: .55 });
      const tb = box(tgt, -8, -8, -8, 8, 8, 8, blockM);
      const beamM = new THREE.MeshBasicMaterial({ color: C.trim, transparent: true, opacity: .4, blending: THREE.AdditiveBlending, depthWrite: false });
      const beam = new THREE.Mesh(new THREE.CylinderGeometry(.06, .03, 11 * P, 8), beamM); beam.rotation.x = Math.PI / 2; beam.position.set(0, 8.3 * P, 10.5 * P); b.add(beam);
      const parts = []; for (let i = 0; i < 10; i++) parts.push(box(b, -.3, -.3, -.3, .3, .3, .3, glow(C.trim)));
      const clock = appearClock(2.2);
      return { group: g, update(t, st, _f, variant) {
        const pal = CORE_PALETTES[variant | 0] || CORE_PALETTES[0], off = st === 5, stop = st === 4 || off, fluid = st === 2 || st === 3, place = st === 1 || st === 3, col = fluid ? C.fluid : C.trim;
        const p = clock(t, off), sB = smooth(p, 0, .25), sR = smooth(p, .2, .5), sP = smooth(p, .35, .65), sL = smooth(p, .55, .85);
        const lc = (c, k = sB) => off ? '#1E1B2E' : '#' + new THREE.Color('#1E1B2E').lerp(new THREE.Color(c), k).getHexString();
        strips.forEach(m => m.color.set(lc(pal.strip)));
        ringM.color.set(lc(stop ? pal.strip : col, sR));
        irisG.visible = !off && sL > 0; irisG.scale.setScalar(Math.max(.001, sL)); irisG.rotation.y = t * (stop ? .3 : place ? 1.6 : -1.6);
        irisM.color.set(stop ? hex(col, -110) : col);
        lensM.color.set(stop ? '#1E293B' : col); crystal.visible = !off && sL > 0;
        crystal.scale.setScalar(Math.max(.001, sL) * (stop ? .8 : .9 + .25 * Math.sin(t * 5))); crystal.rotation.set(t * 1.3, t * 1.9, t * .7); crysM.color.set(stop ? pal.win : '#FFFFFF');
        prongs.forEach((q, i) => { q.position.z = (-4 * (1 - sP) + (stop ? 0 : .6 * Math.sin(t * 3 + i * 1.6))) * P; });
        padM.color.set(lc(stop ? pal.win : col, sP));
        beamM.color.set(col); beam.visible = !stop && p >= 1;
        tgt.visible = !off; tb.material = fluid ? waterM : blockM;
        const k = (t * .5) % 1; tgt.scale.setScalar(stop ? 1 : place ? Math.max(.05, k) : Math.max(.05, 1 - k));
        chevs.forEach(c => { const lit = stop ? false : Math.floor((t * 3) % 3) === (place ? c.i : 2 - c.i); c.m.color.set(lit ? col : '#2E3340'); });
        parts.forEach((q, i) => { const u = (t * .9 + i / 10) % 1, sp = (place ? u : 1 - u) * 3; q.visible = !stop && p >= 1; q.material.color.set(col);
          q.position.set(Math.sin(i * 2.3) * sp * P, (8.3 + Math.cos(i * 1.7) * sp) * P, (place ? 5.6 + u * 8 : 13.6 - u * 8) * P); });
        silk.material.color.set(off ? '#1E1B2E' : stop ? '#94A3B8' : '#F0F9FF'); fort.material.color.set(off ? '#1E1B2E' : stop ? '#065F46' : '#34D399');
      } };
    }
  }, PALETTE_ROW));

