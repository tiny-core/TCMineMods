  // ===== Interface Quântica: moldura aberta e simétrica com um cubo de dados (a rede em miniatura) no centro =====
  const CHANNELS = [['Itens', C.item], ['Fluidos', C.fluid], ['Gás', C.chem], ['Energia', C.energy], ['Redstone', '#EF4444']];
  const FLOW_COLS = CHANNELS.map(c => c[1]);
  // Estados do protótipo → canais ativos (0 itens, 1 fluidos, 2 gás, 3 energia, 4 redstone). Ociosa = nenhum.
  const IFACE_STATES = ['Ociosa', 'Exportar itens', 'Importar itens + fluidos + energia', 'Todos (exporta e importa)', 'Exposta a outro mod', 'Sem energia'];
  const IFACE_ACTIVE = [[], [0], [0, 1, 3], [0, 1, 2, 3, 4], [0, 1, 2], []];
  // Sentido das partículas: 'out' = exportar (rede → fora), 'in' = importar (fora → rede), 'mix' = metade de cada
  const IFACE_DIR = ['out', 'out', 'in', 'mix', 'out', 'out'];
  const FACE_DIRS = [[0, 1, 0], [0, -1, 0], [0, 0, 1], [0, 0, -1], [1, 0, 0], [-1, 0, 0]];
  // Cubo de dados: n × n × n voxels nas cores dos canais (pesos: mais itens, menos redstone). Devolve o mesh e os dados.
  function dataCube(parent, n, cell, gap, seed) {
    const r = rng(seed), pts = [], half = (n - 1) / 2 * (cell + gap);
    for (let y = 0; y < n; y++) for (let x = 0; x < n; x++) for (let z = 0; z < n; z++) pts.push([x * (cell + gap) - half, y * (cell + gap) - half, z * (cell + gap) - half]);
    // canais espalhados ao acaso, em partes quase iguais (baralho: cada canal com o mesmo número de voxels, ±1)
    const ch = pts.map((_, i) => i % 5); for (let i = ch.length - 1; i > 0; i--) { const j = Math.floor(r() * (i + 1)); [ch[i], ch[j]] = [ch[j], ch[i]]; }
    const im = voxels(pts, cell, new THREE.MeshBasicMaterial({ color: '#ffffff' })); parent.add(im);
    const c = new THREE.Color('#2A2440'); ch.forEach((k, i) => im.setColorAt(i, c)); im.instanceColor.needsUpdate = true;
    return { im, pts, ch, ph: pts.map(() => r() * 6.28), half };
  }
  // Cor de um voxel do cubo: tom neutro do tema (respira na ociosa); acende na cor do canal se essa função estiver ativa.
  const _vc = new THREE.Color(), _vw = new THREE.Color('#ffffff');
  function cubeVoxelColor(out, k, active, pal, t, ph, idle) {
    if (active.includes(k)) { out.set(FLOW_COLS[k]); if (Math.sin(t * 4 + ph * 7) > .96) out.lerp(_vw, .5); return out; }
    return out.set('#2A2440').lerp(_vc.set(pal.win), idle ? .28 + .14 * Math.sin(t * 1.3 + ph * .3) : .22);
  }
  MODELS.push(Object.assign({ id: 'interface', name: 'Interface Quântica', color: C.cyan, size: '1 bloco (16 × 16 × 16 px)', radius: 1.5, target: .5,
    states: IFACE_STATES,
    info: {
      parts: ['<b>Moldura aberta</b> e simétrica: 12 arestas e 8 cantos reforçados com luz; igual em todas as faces, porque a Interface entra e sai por qualquer lado',
        '<b>Cubo de dados</b> 4 × 4 × 4 a flutuar no centro; cada voxel pertence a um canal (<b>itens</b>, <b>fluidos</b>, <b>gás</b>, <b>energia</b>, <b>redstone</b>), espalhados ao acaso em partes iguais'],
      anim: ['<b>Ociosa</b>: o cubo fica num tom neutro do tema, gira devagar e respira; sem partículas',
        'Os voxels de cada canal <b>acendem na cor do canal</b> quando essa função está ligada na Interface (itens laranja, fluidos azul, gás verde, energia amarela, redstone vermelho); só os canais ativos lançam voxels pelas faces: <b>para fora ao exportar</b> (da rede para o mundo) e <b>para dentro ao importar</b>',
        'Exposta a outro mod (ex.: Storage Bus do AE2 a ler a rede): um plano de leitura varre o cubo de cima a baixo',
        'Redstone ativo: as luzes dos cantos ficam vermelhas', '<b>Sem energia</b>: o cubo desfaz-se e some; a moldura apaga',
        '<b>Surgimento</b>: os cantos acendem e o cubo monta-se voxel a voxel, de baixo para cima'],
      tech: ['Porta da rede para o resto do modpack: importa e exporta <b>itens, fluidos, gás e energia</b>, emite <b>redstone</b> com filtros e <b>expõe a rede</b> a outros mods',
        'Para o AE2, expõe a rede no formato de armazenamento do próprio AE2 (Storage Bus lê tudo, rápido e com quantidades grandes); para os outros mods, como inventário comum com limite de slots na config',
        'Recusa importar de uma rede espelhada (outra Interface vista pelo AE2): evita contar duas vezes e itens em círculo',
        'Cada função numa classe própria (exportar, importar, redstone, exposição), configurada por face na tela',
        'Moldura em JSON; cubo de dados, voxels e plano de leitura num BlockEntityRenderer leve']
    },
    build() {
      const g = new THREE.Group(), strips = []; const sm = () => { const m = glow('#D946EF'); strips.push(m); return m; };
      const F = 1.5, I = .1, A = 8 - I, B = 8 - I - F; // aresta de 1,5 px, recuada 0,1 dos cantos (sem faces no mesmo plano)
      [-1, 1].forEach(sy => [-1, 1].forEach(sz => { const y0 = sy < 0 ? I : 16 - I - F; bx(g, -5.6, y0, sz * A, 5.6, y0 + F, sz * B, 'frame'); }));
      [-1, 1].forEach(sx => [-1, 1].forEach(sz => bx(g, sx * A, 2.4, sz * A, sx * B, 13.6, sz * B, 'frame')));
      [-1, 1].forEach(sx => [-1, 1].forEach(sy => { const y0 = sy < 0 ? I : 16 - I - F; bx(g, sx * A, y0, -5.6, sx * B, y0 + F, 5.6, 'frame'); }));
      [-1, 1].forEach(sx => [-1, 1].forEach(sy => [-1, 1].forEach(sz => {
        const y0 = sy < 0 ? 0 : 13.6, y1 = y0 + 2.4, yc = (y0 + y1) / 2;
        bx(g, sx * 8, y0, sz * 8, sx * 5.6, y1, sz * 5.6, 'metal');
        const m = sm(); bx(g, sx * 8, yc - .45, sz * 6.35, sx * 8.12, yc + .45, sz * 7.25, m); bx(g, sx * 6.35, yc - .45, sz * 8, sx * 7.25, yc + .45, sz * 8.12, m);
        bx(g, sx * 6.35, sy < 0 ? -.12 : 16, sz * 6.35, sx * 7.25, sy < 0 ? 0 : 16.12, sz * 7.25, m);
      })));
      // cubo de dados
      const core = new THREE.Group(); core.position.y = 8 * P; g.add(core);
      const cube = new THREE.Group(); core.add(cube); const D = dataCube(cube, 4, 1.25, .25, 777);
      const scanM = new THREE.MeshBasicMaterial({ color: '#ffffff', transparent: true, opacity: .35, blending: THREE.AdditiveBlending, depthWrite: false, side: THREE.DoubleSide });
      const scan = new THREE.Mesh(new THREE.PlaneGeometry(6.6 * P, 6.6 * P), scanM); scan.rotation.x = -Math.PI / 2; cube.add(scan);
      // voxels a viajar entre o cubo e as faces
      const r = rng(31), flyers = []; for (let i = 0; i < 10; i++) flyers.push({ d: FACE_DIRS[i % 6], k: i % 5, off: [r() - .5, r() - .5], ph: i / 10, m: box(core, -.5, -.5, -.5, .5, .5, .5, glow('#ffffff')) });
      const M = new THREE.Matrix4(), col = new THREE.Color(), clock = appearClock(2);
      return { group: g, update(t, st, _f, variant) {
        const pal = CORE_PALETTES[variant | 0] || CORE_PALETTES[0], off = st === 5, act = IFACE_ACTIVE[st] || [], idle = !act.length, red = act.includes(4), exposed = st === 4;
        const p = clock(t, off), sL = smooth(p, 0, .25), sC = smooth(p, .2, .85);
        const lc = (c, k = sL) => off ? '#1E1B2E' : '#' + new THREE.Color('#1E1B2E').lerp(new THREE.Color(c), k).getHexString();
        strips.forEach(m => m.color.set(lc(red ? '#EF4444' : pal.strip)));
        core.visible = !off && sC > 0;
        cube.rotation.set(.35 + .1 * Math.sin(t * .7), t * (idle ? .18 : .45), .15 * Math.sin(t * .5)); cube.scale.setScalar(idle ? 1 + .03 * Math.sin(t * 1.3) : 1);
        const scanY = ((t * .6) % 1) * 2 * D.half - D.half; scan.visible = exposed && p >= 1; scan.position.y = scanY * P;
        D.pts.forEach((q, i) => {
          const h = (q[1] + D.half) / (2 * D.half), shown = h <= sC * 1.05, s = shown ? .9 + .12 * Math.sin(t * 3 + D.ph[i]) : 0;
          M.makeScale(s, s, s); M.setPosition(q[0] * P, q[1] * P, q[2] * P); D.im.setMatrixAt(i, M);
          cubeVoxelColor(col, D.ch[i], act, pal, t, D.ph[i], idle);
          if (exposed && Math.abs(q[1] - scanY) < .8) col.lerp(new THREE.Color('#ffffff'), .55);
          D.im.setColorAt(i, col);
        });
        D.im.instanceMatrix.needsUpdate = true; D.im.instanceColor.needsUpdate = true;
        flyers.forEach((f, i) => { const k = act.length ? act[i % act.length] : 0, u = (t * .55 + f.ph) % 1, dir = IFACE_DIR[st], out = dir === 'out' || (dir === 'mix' && i % 2 === 0), rr = out ? 3.4 + 5 * u : 8.4 - 5 * u;
          f.m.visible = !off && p >= 1 && !idle && !exposed;
          const ax = f.d, o1 = f.off[0] * 3, o2 = f.off[1] * 3;
          f.m.position.set((ax[0] * rr + (ax[0] ? 0 : o1)) * P, (ax[1] * rr + (ax[1] ? 0 : (ax[0] ? o1 : o2))) * P, (ax[2] * rr + (ax[2] ? 0 : o2)) * P);
          f.m.material.color.set(FLOW_COLS[k]); f.m.scale.setScalar(u < .15 ? u / .15 : u > .85 ? (1 - u) / .15 : 1); });
      } };
    }
  }, PALETTE_ROW));

