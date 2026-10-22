  // ===== Painel da Rede: monitores multibloco (como no Colony Bridge) =====
  MODELS.push({ id: 'painel', name: 'Painel da Rede', color: C.cyan, size: 'multibloco: junta 1 × 1 até 8 × 4 blocos', radius: 2.4, target: 1, states: ['1 × 1', '2 × 1', '3 × 2'],
    info: {
      parts: ['Mesmo esquema dos <b>Monitores multibloco do Colony Bridge</b>: vários blocos lado a lado formam um ecrã só', 'Cada bloco é o mesmo cubo de ecrã; as bordas internas desaparecem quando se juntam', 'O conteúdo cresce com o tamanho: números → números + gráfico → números + gráfico + ocupação por tipo'],
      anim: ['Números contam suavemente; o gráfico de operações/s corre', 'Barras de ocupação na cor do tipo (âmbar acima de 80%, vermelho acima de 95%)'],
      tech: ['Reaproveita a infraestrutura de monitores do Colony Bridge (candidata a ir para o TC Core: segundo mod a usá-la)', 'O bloco mestre (canto inferior esquerdo) desenha tudo; os outros só formam a moldura', 'Dados que o Núcleo já calcula na "Visão geral"; redesenha no máximo 1 vez por segundo']
    },
    build() {
      const g = new THREE.Group(); let o = null, S = null, size = [1, 1], last = -1, tick = 0;
      const hist = []; for (let i = 0; i < 40; i++) hist.push(.35 + .3 * Math.abs(Math.sin(i * .6)));
      function rebuild(w, h) {
        if (o) g.remove(o); o = new THREE.Group(); g.add(o); o.position.set(-w / 2, 0, -.5);
        S = screenTex(128 * w, 128 * h);
        for (let i = 0; i < w; i++) for (let j = 0; j < h; j++) {
          const side = new THREE.MeshLambertMaterial({ map: sideTex(C.cyan) }), top = new THREE.MeshLambertMaterial({ map: topTex(C.cyan) });
          const geo = new THREE.BoxGeometry(1, 1, 1), uv = geo.attributes.uv;
          for (let k = 16; k < 20; k++) uv.setXY(k, (i + uv.getX(k)) / w, (j + uv.getY(k)) / h);
          const m = new THREE.Mesh(geo, [side, side, top, lit('dark'), new THREE.MeshBasicMaterial({ map: S.tex }), side]); m.position.set(i + .5, j + .5, .5); o.add(m);
        }
        size = [w, h];
      }
      function draw(t) {
        const [w, h] = size, W = 128 * w, H = 128 * h, x = S.x; uiFrame(x, W, H);
        x.fillStyle = UI.panel; x.fillRect(5, 7, W - 10, 18); x.font = '600 11px "Pixelify Sans", monospace'; x.textBaseline = 'middle'; x.textAlign = 'left'; x.fillStyle = UI.hi; x.fillText(w > 1 ? 'Base Principal' : 'Rede', 10, 16.5); x.fillStyle = UI.ok; x.textAlign = 'right'; x.fillText('Online', W - 10, 16.5); x.textAlign = 'left';
        const cols = Math.min(4, w * 2), cw = (W - 10 - 6 * (cols + 1)) / cols, cards = [['Energia', '61%', UI.warn], ['Op/s', String(1200 + Math.floor(Math.sin(t) * 300)), UI.text], ['Crafts', '2', C.trim], ['Tipos', '8 412', UI.text]];
        for (let i = 0; i < cols; i++) uiCard(x, 11 + i * (cw + 6), 31, cw, 34, cards[i][0], cards[i][1], cards[i][2], w > 1 ? 16 : 13);
        if (h >= 2) {
          const cx = 11, cy = 72, cwid = W - 22, chh = 100; x.fillStyle = UI.panel; x.fillRect(cx, cy, cwid, chh); x.fillStyle = UI.muted; x.font = '500 8px "Pixelify Sans", monospace'; x.textBaseline = 'top'; x.fillText('Operações por segundo — últimos 60 s', cx + 6, cy + 4);
          const n = hist.length, bw = (cwid - 12) / n; hist.forEach((v, i) => { x.fillStyle = i === n - 1 ? UI.accent : UI.hi; x.fillRect(cx + 6 + i * bw, cy + chh - 6 - v * 70, Math.max(1, bw - 1), v * 70); });
          const py = 180, bars = [['Itens', '#F97316', .61], ['Fluidos', '#60A5FA', .18], ['Químicos', '#A3E635', .86]];
          bars.forEach(([l, c, v], i) => { const by = py + i * 22; x.fillStyle = UI.muted; x.font = '500 8px "Pixelify Sans", monospace'; x.textBaseline = 'middle'; x.fillText(l, 12, by + 5); x.fillStyle = '#0E1015'; x.fillRect(70, by, W - 120, 10); x.fillStyle = v >= .95 ? UI.bad : v >= .8 ? UI.warn : c; x.fillRect(70, by, (W - 120) * v, 10); x.fillStyle = UI.text; x.textAlign = 'right'; x.fillText(Math.round(v * 100) + '%', W - 12, by + 5); x.textAlign = 'left'; });
        }
        S.tex.needsUpdate = true;
      }
      rebuild(1, 1);
      return { group: g, update(t, st) {
        const want = [[1, 1], [2, 1], [3, 2]][st]; if (want[0] !== size[0] || want[1] !== size[1]) { rebuild(want[0], want[1]); last = -1; }
        tick++; if (st !== last || tick % 60 === 0) { if (tick % 60 === 0) { hist.shift(); hist.push(.3 + .5 * Math.abs(Math.sin(t * 1.3))); } draw(t); last = st; }
      } };
    }
  });

