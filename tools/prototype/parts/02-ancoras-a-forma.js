  // ===== Âncoras: a forma diz o tipo; uma "gema" em degraus no centro =====
  function machineBlock(g, crate) { box(g, 0, 0, 0, 16, 16, 16, crate ? 'crate' : 'machine'); }
  // Meia-largura de cada forma por linha (dy = distância ao centro em px; y = posição com sinal, para o triângulo).
  const SHAPES = {
    hex: { H: 6, hw: dy => 6.5 - dy * .54, bolts: [[-5.1, 0], [5.1, 0], [-2.5, -4.6], [2.5, -4.6], [-2.5, 4.6], [2.5, 4.6]] },
    square: { H: 6, hw: dy => (dy > 5 ? 5 : 6), bolts: [] },
    tri: { H: 6, hw: (dy, y) => .6 + (6 - y) * .55, bolts: [[0, 3.2], [-4, -4], [4, -4]] },
    diamond: { H: 7, hw: dy => Math.max(.4, 7 - dy), bolts: [[0, 5], [0, -5], [-5, 0], [5, 0]] }
  };
  function shape(g, name, cx, cy, s, z0, z1, mat) {
    const S = SHAPES[name], H = S.H * s, rows = Math.max(1, Math.round(2 * H)), out = [];
    for (let k = 0; k < rows; k++) {
      const y0 = cy - H + k * (2 * H / rows), y1 = y0 + 2 * H / rows, yc = (y0 + y1) / 2 - cy;
      const hw = S.hw(Math.abs(yc / s), yc / s) * s; if (hw <= .05) continue;
      out.push(box(g, cx - hw, y0, z0, cx + hw, y1, z1, mat));
    }
    return out;
  }
  const LINKS = {
    hex: { name: 'Âncora de Máquina', color: C.cyan, gem: ['#0E7490', '#22B8E8', '#67E8F9', '#ECFEFF'], form: 'hexágono com 6 parafusos (porca: "mexe em máquinas")', icon: 'engrenagem de 8 dentes com furo, aros claros por cima' },
    square: { name: 'Âncora de Inventário', color: C.trim, gem: ['#9A3412', '#F97316', '#FDBA74', '#FFF7ED'], form: 'quadrado com cantoneiras laranja (caixa: "guarda coisas")', icon: 'mini baú 3D de madeira, com cantos e fecho de luz laranja' },
    tri: { name: 'Emissor de Nível', color: C.red, gem: ['#991B1B', '#F87171', '#FCA5A5', '#FFF1F2'], form: 'triângulo com 3 parafusos (aviso: "avisa por redstone")', icon: 'barras de nível' },
    diamond: { name: 'Colocador / Quebrador', color: C.purple, gem: ['#6B21A8', '#C084FC', '#E9D5FF', '#FAF5FF'], form: 'losango com 4 parafusos (ferramenta: "mexe no mundo")', icon: 'picareta' }
  };
  // Ícones de 7 x 7 em pixel art, extrudidos em 3D (camada de trás escura + camada da frente clara).
  const ICONS = {
    hex: { back: ['....#....', '.#.###.#.', '..#####..', '.##...##.', '###...###', '.##...##.', '..#####..', '.#.###.#.', '....#....'],
           front: ['....#....', '.#.....#.', '..##.##..', '.#.....#.', '##.....##', '.#.....#.', '..##.##..', '.#.....#.', '....#....'] },
    // Inventário: um mini baú 3D de verdade (não pixel art), ver chest3D.
    square: 'chest',
    tri: ['......#', '....#.#', '....#.#', '..#.#.#', '..#.#.#', '#.#.#.#', '#.#.#.#'],
    diamond: ['..###..', '.#...#.', '#..#..#', '...#...', '...#...', '...#...', '...#...']
  };
  // Mini baú 3D (4,6 x 3,8 x 2,2 px): corpo de madeira, tampa, faixas e fecho de luz na cor do tipo.
  function chest3D(g, cx, cy, z0, front) {
    const wood = new THREE.MeshLambertMaterial({ color: '#8B5A2B' }), woodD = new THREE.MeshLambertMaterial({ color: '#5C3A1A' });
    const x0 = cx - 2.3, x1 = cx + 2.3, yb = cy - 1.9;
    box(g, x0, yb, z0, x1, yb + 2.3, z0 + 2, wood);
    box(g, x0 - .15, yb + 2.3, z0 - .1, x1 + .15, yb + 2.65, z0 + 2.15, woodD);
    box(g, x0, yb + 2.65, z0, x1, yb + 3.8, z0 + 2, wood);
    box(g, x0 + .3, yb + 3.8, z0 + .3, x1 - .3, yb + 4, z0 + 1.7, woodD);
    [[x0 - .1, x0 + .4], [x1 - .4, x1 + .1]].forEach(([a, b]) => box(g, a, yb - .05, z0 + 1.6, b, yb + 3.85, z0 + 2.1, front));
    box(g, cx - .45, yb + 1.7, z0 + 2, cx + .45, yb + 2.9, z0 + 2.4, front);
  }
  function icon3D(g, icon, cx, cy, z0, back, front) {
    if (icon === 'chest') return chest3D(g, cx, cy, z0, front);
    const bm = icon.back || icon, fm = icon.front || icon, N = bm.length, cell = 4.4 / N, mid = (N - 1) / 2;
    const pts = mask => { const out = []; mask.forEach((row, r) => [...row].forEach((ch, c) => { if (ch === '#') out.push([cx + (c - mid) * cell, cy + (mid - r) * cell]); })); return out; };
    const mk = (list, mat, zc, d) => { const geo = new THREE.BoxGeometry(cell * P, cell * P, d * P), im = new THREE.InstancedMesh(geo, mat, list.length), m = new THREE.Matrix4(); list.forEach((p, i) => { m.makeTranslation(p[0] * P, p[1] * P, zc * P); im.setMatrixAt(i, m); }); g.add(im); return im; };
    mk(pts(bm), back, z0 + .25, .5); mk(pts(fm), front, z0 + .7, .4);
  }
  function anchorNode(g, kind) {
    // Chip de 8 x 8 px colado na face: substrato, pinos dourados, tampa metálica e o símbolo do tipo gravado em luz.
    const L = LINKS[kind], cx = 8, cy = 8, n = new THREE.Group(); g.add(n);
    const gold = new THREE.MeshLambertMaterial({ color: '#D4A23A' });
    box(n, 4, 4, 16, 12, 12, 16.5, 'dark');
    box(n, 4.6, 4.6, 16.5, 11.4, 11.4, 17, 'panel');
    for (let i = 0; i < 5; i++) { const p = 5.4 + i * 1.2;
      box(n, p, 3.6, 16.05, p + .5, 4.6, 16.4, gold); box(n, p, 11.4, 16.05, p + .5, 12.4, 16.4, gold);
      box(n, 3.6, p, 16.05, 4.6, p + .5, 16.4, gold); box(n, 11.4, p, 16.05, 12.4, p + .5, 16.4, gold); }
    box(n, 5.6, 5.6, 17, 10.4, 10.4, 17.8, 'frame'); box(n, 5.9, 5.9, 17.8, 10.1, 10.1, 17.9, 'metal');
    box(n, 4.8, 10.4, 17, 5.4, 11, 17.2, 'trim');
    const gm = glow(L.color), gb = glow(hex(L.color, -90)); icon3D(n, ICONS[kind], cx, cy, 17.9, gb, gm);
    const traces = [[4.8, 7.6, 5.6, 8.4], [10.4, 7.6, 11.2, 8.4]].map(r => box(n, r[0], r[1], 17, r[2], r[3], 17.05, glow(L.color)));
    const pips = kind === 'hex' ? [C.item, C.fluid, C.energy, C.red].map((c, i) => ({ c, m: box(n, 6.2 + i * 1, 4.75, 17, 6.7 + i * 1, 5.25, 17.08, glow(c)) })) : [];
    const h = halo(L.color === C.cyan ? 'rgba(34,184,232,0.5)' : L.color === C.trim ? 'rgba(249,115,22,0.5)' : L.color === C.red ? 'rgba(248,113,113,0.5)' : 'rgba(192,132,252,0.5)', .3);
    h.position.set(cx * P, cy * P, 18.3 * P); n.add(h);
    return { L, gm, traces, pips, h };
  }
  function animateAnchor(a, t, st) {
    const on = st === 0, off = st === 2, c = a.L.color;
    a.gm.color.set(off ? '#1E293B' : on ? c : hex(c, -60)); a.gm.color.lerp(new THREE.Color('#FFFFFF'), on ? .25 + .25 * Math.sin(t * 4) : 0);
    a.traces.forEach((m, i) => m.material.color.set(off ? '#1E293B' : on && Math.sin(t * 9 + i * 2) > 0 ? c : hex(c, -110)));
    a.h.visible = !off; a.h.material.opacity = on ? .5 + .3 * Math.sin(t * 6) : .15;
    a.pips.forEach((p, i) => p.m.material.color.set(off ? '#1E293B' : (on && i !== 1) || (!on && i === 2) ? p.c : '#2E3340'));
  }
  const anchorInfo = (kind) => {
    const L = LINKS[kind];
    const info = {
      parts: ['Parece um <b>processador</b>: substrato escuro, 20 pinos dourados à volta e tampa metálica', '<b>Ícone 3D</b> em relevo na tampa, na cor do tipo: ' + L.icon, '<b>Marca do pino 1</b> laranja num canto', '<b>Pistas de luz</b> dos lados do chip'],
      anim: ['Nada gira nem pisca: ao transferir, o ícone brilha suavemente e as pistas acendem', 'Ociosa: ícone em tom baixo', 'Sem energia: tudo apagado'],
      tech: ['Só 8 × 8 × 2 px numa face: discreta, menor que o Ender Gate do Powah', 'Rodada pela face onde foi colocada; colisão só no chip', 'Tudo em JSON (o ícone são cubos de 0,6 px); o brilho é textura animada (.mcmeta), sem BlockEntityRenderer', 'O ícone do item é o mesmo chip visto de frente']
    };
    if (kind === 'hex') info.parts.push('<b>4 pontos de canal</b> por baixo da tampa: itens, fluidos, energia, redstone');
    return info;
  };
  [['ancora', 'hex', false], ['ancora_inv', 'square', true], ['emissor', 'tri', false]].forEach(([id, kind, crate]) => {
    const L = LINKS[kind];
    MODELS.push({ id, name: L.name, color: L.color, size: '8 × 8 × 2 px, numa face do bloco', radius: 1.5, target: .55, states: kind === 'tri' ? ['Sinal ligado', 'Sinal desligado', 'Sem energia'] : ['Transferindo', 'Ociosa', 'Sem energia'], info: anchorInfo(kind),
      build() {
        const g = new THREE.Group(), o = new THREE.Group(); g.add(o); o.position.set(-8 * P, 0, -8 * P);
        machineBlock(o, crate); const a = anchorNode(o, kind);
        return { group: g, update(t, st) { animateAnchor(a, t, st); } };
      }
    });
  });
  MODELS.push({ id: 'formas', name: 'Família das Âncoras', color: C.red, size: 'todas com 8 × 8 px', radius: 3, target: .55, states: ['Transferindo', 'Ociosa', 'Sem energia'],
    info: {
      parts: ['Mesmo chip para todas; muda o <b>ícone 3D</b> e a cor', '<b>Engrenagem ciano</b>: Âncora de Máquina', '<b>Baú laranja</b>: Âncora de Inventário', '<b>Barras de nível vermelhas</b>: Emissor de Nível', 'O Colocador/Quebrador passou a bloco inteiro: o Manipulador Quântico'],
      anim: ['Mesma animação para todas: o tipo reconhece-se pela forma e pela cor, de longe e sem abrir a tela'],
      tech: ['O ícone distingue o tipo mesmo para quem não vê bem as cores', 'Daltonismo: a forma basta para distinguir, a cor é só reforço']
    },
    build() {
      const g = new THREE.Group(), list = [];
      [['hex', false], ['square', true], ['tri', false]].forEach(([k, crate], i) => {
        const o = new THREE.Group(); o.position.set((-26 + i * 18) * P, 0, -8 * P); g.add(o);
        if (k === 'diamond') box(o, 0, 0, 0, 16, 16, 16, flat('#5B4A3A')); else machineBlock(o, crate);
        list.push(anchorNode(o, k));
      });
      return { group: g, update(t, st) { list.forEach((a, i) => animateAnchor(a, t + i * .4, st)); } };
    }
  });

