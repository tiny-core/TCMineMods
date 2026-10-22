  // ===== Interface Quântica (painel): uma face da moldura colada num bloco, com um cubo de dados pequeno à frente =====
  MODELS.push(Object.assign({ id: 'interface_painel', name: 'Interface (painel)', color: C.cyan, size: 'painel 14 × 14 × 2 px, numa face do bloco', radius: 1.5, target: .55,
    states: IFACE_STATES,
    info: {
      parts: ['Mesma Interface Quântica, em <b>painel</b> que se cola na face de um baú ou máquina (como a interface em "parte" do AE2)',
        '<b>Moldura quadrada</b> igual a uma face do bloco: 4 barras e 4 cantos reforçados com luz',
        '<b>Cubo de dados</b> pequeno (3 × 3 × 3) a flutuar à frente, com os canais espalhados como no bloco'],
      anim: ['Ociosa: cubo neutro a respirar; os voxels de cada canal acendem na sua cor quando a função está ligada', 'Exportar: voxels saem do cubo para dentro do bloco; importar: saem do bloco para o cubo; exposta: plano de leitura; redstone ativo: cantos vermelhos', '<b>Sem energia</b>: o cubo some'],
      tech: ['Mesmas funções do bloco (importar, exportar, redstone, expor a rede); age só na face onde está colado',
        'Moldura em JSON; cubo, voxels e plano de leitura num BlockEntityRenderer leve']
    },
    build() {
      const g = new THREE.Group(), o = new THREE.Group(); g.add(o); o.position.set(-8 * P, 0, -8 * P);
      const host = new THREE.Group(); o.add(host); machineBlock(host, true); host.traverse(m => { m.userData.noExport = true; });
      const strips = []; const sm = () => { const m = glow('#D946EF'); strips.push(m); return m; };
      box(o, 3.2, 1.1, 16, 12.8, 2.5, 17.5, 'frame'); box(o, 3.2, 13.5, 16, 12.8, 14.9, 17.5, 'frame');
      box(o, 1.1, 3.2, 16, 2.5, 12.8, 17.5, 'frame'); box(o, 13.5, 3.2, 16, 14.9, 12.8, 17.5, 'frame');
      [[1, 1], [12.6, 1], [1, 12.6], [12.6, 12.6]].forEach(([x, y]) => { box(o, x, y, 16, x + 2.4, y + 2.4, 17.9, 'metal'); box(o, x + .75, y + .75, 17.9, x + 1.65, y + 1.65, 18.02, sm()); });
      const front = new THREE.Group(); front.position.set(8 * P, 8 * P, 20 * P); o.add(front);
      const cube = new THREE.Group(); front.add(cube); const D = dataCube(cube, 3, 1.15, .2, 778);
      const scanM = new THREE.MeshBasicMaterial({ color: '#ffffff', transparent: true, opacity: .35, blending: THREE.AdditiveBlending, depthWrite: false, side: THREE.DoubleSide });
      const scan = new THREE.Mesh(new THREE.PlaneGeometry(4.4 * P, 4.4 * P), scanM); scan.rotation.x = -Math.PI / 2; cube.add(scan);
      const flyers = []; for (let k = 0; k < 6; k++) flyers.push({ k, m: box(o, -.35, -.35, -.35, .35, .35, .35, glow('#ffffff')) });
      const M = new THREE.Matrix4(), col = new THREE.Color(), clock = appearClock(1.6);
      return { group: g, update(t, st, _f, variant) {
        const pal = CORE_PALETTES[variant | 0] || CORE_PALETTES[0], off = st === 5, act = IFACE_ACTIVE[st] || [], idle = !act.length, red = act.includes(4), exposed = st === 4;
        const p = clock(t, off), sL = smooth(p, 0, .3), sC = smooth(p, .25, .85);
        const lc = (c, k = sL) => off ? '#1E1B2E' : '#' + new THREE.Color('#1E1B2E').lerp(new THREE.Color(c), k).getHexString();
        strips.forEach(m => m.color.set(lc(red ? '#EF4444' : pal.strip)));
        front.visible = !off && sC > 0; front.position.z = (20 + .3 * Math.sin(t * 2)) * P;
        cube.rotation.set(.4, t * (idle ? .2 : .5), .2); cube.scale.setScalar(idle ? 1 + .03 * Math.sin(t * 1.3) : 1);
        const scanY = ((t * .6) % 1) * 2 * D.half - D.half; scan.visible = exposed && p >= 1; scan.position.y = scanY * P;
        D.pts.forEach((q, i) => {
          const h = (q[1] + D.half) / (2 * D.half), s = h <= sC * 1.05 ? .9 + .12 * Math.sin(t * 3 + D.ph[i]) : 0;
          M.makeScale(s, s, s); M.setPosition(q[0] * P, q[1] * P, q[2] * P); D.im.setMatrixAt(i, M);
          cubeVoxelColor(col, D.ch[i], act, pal, t, D.ph[i], idle); if (exposed && Math.abs(q[1] - scanY) < .7) col.lerp(new THREE.Color('#ffffff'), .55); D.im.setColorAt(i, col);
        });
        D.im.instanceMatrix.needsUpdate = true; D.im.instanceColor.needsUpdate = true;
        flyers.forEach(f => { const k = act.length ? act[f.k % act.length] : 0, u = (t * .8 + f.k / 6) % 1, dir = IFACE_DIR[st], out = dir === 'out' || (dir === 'mix' && f.k % 2 === 0), z = out ? 18.2 - 2.4 * u : 15.8 + 2.4 * u, a = f.k * 1.05;
          f.m.visible = !off && p >= 1 && !idle && !exposed; f.m.position.set((8 + Math.cos(a) * 1.6) * P, (8 + Math.sin(a) * 1.6) * P, z * P);
          f.m.material.color.set(FLOW_COLS[k]); f.m.scale.setScalar(u < .15 ? u / .15 : u > .85 ? (1 - u) / .15 : 1); });
      } };
    }
  }, PALETTE_ROW));

