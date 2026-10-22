  // ===== Ecrã no estilo do Monitor do TC Colony Bridge (usado só no Monitor e no Painel) =====
  const UI = { bg: '#1B1E26', panel: '#242832', border: '#3A3F4B', accent: '#F97316', hi: '#22B8E8', text: '#E6E8EC', muted: '#A3A8B3', ok: '#4ADE80', warn: '#FBBF24', bad: '#F87171' };
  function sideTex(mark) {
    return canvasTex((x) => {
      px(x, '#1E2129', 0, 0, 16, 16); px(x, '#2A2E38', 1, 1, 14, 14); px(x, '#1E2129', 2, 2, 12, 12);
      [[1, 1], [14, 1], [1, 14], [14, 14]].forEach(([a, b]) => px(x, '#4B5263', a, b));
      for (let j = 4; j <= 10; j += 2) px(x, '#0E1015', 4, j, 8, 1);
      px(x, mark, 2, 12, 2, 1);
    }, 3);
  }
  function topTex(mark) { return canvasTex((x) => { px(x, '#1E2129', 0, 0, 16, 16); px(x, '#2A2E38', 1, 1, 14, 14); px(x, '#22252E', 3, 3, 10, 10); px(x, '#15171D', 4, 4, 8, 8); px(x, mark, 6, 7, 4, 2); }, 4); }
  function uiFrame(x, W, H) { x.fillStyle = UI.border; x.fillRect(0, 0, W, H); x.fillStyle = '#0E1015'; x.fillRect(3, 3, W - 6, H - 6); x.fillStyle = UI.bg; x.fillRect(5, 5, W - 10, H - 10); x.fillStyle = UI.accent; x.fillRect(5, 5, W - 10, 2); }
  function uiIcon(x, cx, cy, s, color) {
    const map = ['...#######.', '..#lllllll#', '.#llmmmmmd#', '#llmmmmmdd#', '#mmmmmmdd#.', '#ddddddd#..'];
    map.forEach((r, j) => [...r].forEach((ch, i) => { const c = ch === '#' ? '#0B0C10' : ch === 'l' ? hex(color, 50) : ch === 'm' ? color : ch === 'd' ? hex(color, -50) : null; if (c) { x.fillStyle = c; x.fillRect(cx + i * s, cy + j * s, s, s); } }));
  }
  function uiCard(x, cx, cy, w, h, label, value, color, size) {
    x.fillStyle = UI.panel; x.fillRect(cx, cy, w, h); x.fillStyle = UI.accent; x.fillRect(cx, cy, 2, h);
    x.textBaseline = 'top'; x.textAlign = 'left'; x.font = '500 8px "Pixelify Sans", monospace'; x.fillStyle = UI.muted; x.fillText(label, cx + 6, cy + 3);
    x.font = '700 ' + (size || 14) + 'px "Pixelify Sans", monospace'; x.fillStyle = color || UI.text; x.fillText(value, cx + 6, cy + 13);
  }
  // Cubo de ecrã (mesmo corpo do Monitor do Colony Bridge).
  function screenCube(o, x0, y0, mark, S) {
    const side = new THREE.MeshLambertMaterial({ map: sideTex(mark) }), top = new THREE.MeshLambertMaterial({ map: topTex(mark) });
    const mats = [side, side, top, lit('dark'), S ? new THREE.MeshBasicMaterial({ map: S.tex }) : lit('dark'), side];
    const m = new THREE.Mesh(new THREE.BoxGeometry(1, 1, 1), mats); m.position.set(x0 + .5, y0 + .5, .5); o.add(m); return m;
  }

