  // ===== Monitor: frente limpa só com fluxo, ícone e quantidade =====
  MODELS.push({ id: 'monitor', name: 'Monitor', color: C.cyan, size: '1 bloco (16 × 16 × 16 px)', radius: 1.5, target: .55, states: ['Entrando', 'Saindo', 'Sem fluxo'],
    info: {
      parts: ['Mesmo corpo do Monitor do TC Colony Bridge: cubo com laterais escuras e ranhuras', '<b>Frente limpa</b>: só o <b>fluxo</b> com seta, o <b>ícone</b> do item e a <b>quantidade</b> na rede'],
      anim: ['<b>▲ verde</b> a entrar, <b>▼ vermelho</b> a sair, em itens por segundo', 'Sem fluxo: a linha de cima desaparece', 'O número conta suavemente até ao valor novo'],
      tech: ['Corpo em JSON (orientable, como o do Colony Bridge)', 'Frente desenhada por BlockEntityRenderer com as classes de pintura do core; redesenha só quando o número muda', 'Desligado a mais de 32 blocos']
    },
    build() {
      const g = new THREE.Group(), o = new THREE.Group(); g.add(o); o.position.set(-.5, 0, -.5);
      const S = screenTex(128, 128); screenCube(o, 0, 0, C.cyan, S); let last = '';
      function draw(st, t) {
        const x = S.x, W = 128; uiFrame(x, W, W);
        if (st !== 2) { const up = st === 0, c = up ? UI.ok : UI.bad; x.fillStyle = c; const ax = 40, ay = 17;
          x.beginPath(); if (up) { x.moveTo(ax, ay + 10); x.lineTo(ax + 6, ay); x.lineTo(ax + 12, ay + 10); } else { x.moveTo(ax, ay); x.lineTo(ax + 6, ay + 10); x.lineTo(ax + 12, ay); } x.fill();
          x.font = '700 14px "Pixelify Sans", monospace'; x.textBaseline = 'middle'; x.textAlign = 'left'; x.fillText((up ? 34 : 12) + '/s', ax + 17, ay + 5.5); }
        uiIcon(x, 42, 42, 4, '#CBD5E1');
        const n = 1204556 + Math.floor((st === 0 ? 34 : st === 1 ? -12 : 0) * t);
        x.fillStyle = UI.text; x.font = '700 20px "Pixelify Sans", monospace'; x.textAlign = 'center'; x.textBaseline = 'middle'; x.fillText((n / 1e6).toFixed(2).replace('.', ',') + ' M', W / 2, 98); x.textAlign = 'left';
        S.tex.needsUpdate = true;
      }
      let tick = 0;
      return { group: g, update(t, st) { tick++; const k = st + '|' + Math.floor(t); if (k !== last || tick % 60 === 0) { draw(st, t); last = k; } } };
    }
  });

  // Item "de Minecraft": sprite de 16 x 16 pixels extrudido 1 pixel, como um item largado no chão.
  // Desenho próprio (não é a textura do jogo). o = contorno, l/m/d = claro/meio/escuro.
  const INGOT16 = ['................', '................', '................', '.......oooooo...', '......olllllmo..', '.....ollmmmmmdo.', '....ollmmmmmddo.', '...ollmmmmmddo..', '..ollmmmmmddo...', '.ommmmmmmddo....', '.oddddddddo.....', '..oooooooo......', '................', '................', '................', '................'];
  function itemSprite(map, pal, sizePx) {
    const pts = [], cols = [], s = sizePx / 16;
    map.forEach((r, j) => [...r].forEach((ch, i) => { if (pal[ch]) { pts.push([(i - 7.5) * s, (7.5 - j) * s, 0]); cols.push(pal[ch]); } }));
    const geo = new THREE.BoxGeometry(s * P, s * P, s * P), im = new THREE.InstancedMesh(geo, new THREE.MeshLambertMaterial({ color: '#ffffff' }), pts.length), m = new THREE.Matrix4(), c = new THREE.Color();
    pts.forEach((p, i) => { m.makeTranslation(p[0] * P, p[1] * P, 0); im.setMatrixAt(i, m); im.setColorAt(i, c.set(cols[i])); });
    im.instanceColor.needsUpdate = true; return im;
  }
  const IRON = { o: '#3A3F4B', l: '#F8FAFC', m: '#CBD5E1', d: '#8B95A5' };

