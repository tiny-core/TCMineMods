  // ===== Entrelaçador (item): bastão com anel e par entrelaçado =====
  MODELS.push({ id: 'entrelacador', name: 'Entrelaçador (item)', color: C.purple, size: 'item 3D (na mão), 16 × 26 × 3 px', radius: 1.25, target: .8, states: ['Com rede guardada', 'Vazio'],
    info: {
      parts: ['<b>Cabeça</b>: anel octogonal de metal com um anel de luz roxa por dentro', '<b>Orbe</b> roxo no centro do anel, com os mesmos tons da esfera do Núcleo', '<b>Par entrelaçado</b>: duas partículas (ciano e laranja) sempre em posições espelhadas', '<b>Cabo</b> com 4 anéis de punho laranja, guarda com linha de luz e pomo de cristal'],
      anim: ['Com rede guardada: orbe aceso e as duas partículas movem-se sempre espelhadas, como um par entrelaçado', 'Vazio: orbe escuro, sem partículas'],
      tech: ['Modelo de item 3D em JSON; orbe e partículas por textura animada', 'Segurar mostra as ligações no mundo (partículas, só para quem segura)', 'Clique no Núcleo guarda a rede; clique num bloco entrelaça-o']
    },
    build() {
      const g = new THREE.Group(), o = new THREE.Group(); g.add(o); o.position.set(-8 * P, 0, -8 * P);
      const pom = octa(o, 8, -.6, 8, [1, 2.2, 2.2, 1], .6, glow(C.purple));
      box(o, 7, 0, 7, 9, 9, 9, 'metal');
      [1.2, 3.2, 5.2, 7.2].forEach(y => box(o, 6.7, y, 6.7, 9.3, y + .9, 9.3, 'trim'));
      cbox(o, 4.5, 9, 6.5, 11.5, 10.6, 9.5, 'frame', 1); const guardGlow = box(o, 5.5, 9.9, 9.5, 10.5, 10.3, 9.65, glow(C.purple));
      box(o, 7.3, 10.6, 7.3, 8.7, 13, 8.7, 'metal');
      const head = new THREE.Group(); head.position.set(8 * P, 19 * P, 8 * P); head.rotation.x = Math.PI / 2; o.add(head);
      octRing(head, 0, 0, 6.2, 1.4, -1, 1, 'frame');
      const ringM = glow(C.purple); octRing(head, 0, 0, 4.9, .45, -.6, .6, ringM);
      box(o, 1.2, 18, 7.4, 1.8, 20, 8.6, 'trim'); box(o, 14.2, 18, 7.4, 14.8, 20, 8.6, 'trim'); box(o, 7, 25, 7.4, 9, 25.6, 8.6, 'trim');
      const orbG = new THREE.Group(); orbG.position.set(0, 19 * P, 0); g.add(orbG);
      const orb = voxelsShaded(spherePoints(2.4), 1, 2.4, ['#3B0764', '#9333EA', '#E9D5FF']); orbG.add(orb);
      const hl = halo('rgba(168,85,247,0.7)', .55); orbG.add(hl);
      const pa = box(orbG, -.45, -.45, -.45, .45, .45, .45, glow(C.cyan)), pb = box(orbG, -.45, -.45, -.45, .45, .45, .45, glow(C.trim));
      return { group: g, update(t, st) {
        const on = st === 0;
        orb.material.color.set(on ? '#ffffff' : '#2A2140'); hl.visible = on; hl.material.opacity = .55 + .2 * Math.sin(t * 3);
        ringM.color.set(on ? C.purple : '#2E1065'); guardGlow.material.color.set(on ? C.purple : '#2E1065'); pom.forEach(m => m.material.color.set(on ? C.purple : '#2E1065'));
        pa.visible = pb.visible = on;
        const x = Math.sin(t * 2.1) * 3.4, y = Math.sin(t * 4.2) * 1.6;
        pa.position.set(x * P, y * P, .3 * P); pb.position.set(-x * P, -y * P, .3 * P);
      } };
    }
  });

  // ---------- textos e imagens desenhados em ecrãs (CanvasTexture atualizada) ----------
  function screenTex(w, h) {
    const c = document.createElement('canvas'); c.width = w; c.height = h; const x = c.getContext('2d'); x.imageSmoothingEnabled = false;
    const tex = new THREE.CanvasTexture(c); tex.magFilter = THREE.NearestFilter; tex.minFilter = THREE.NearestFilter; return { c, x, tex };
  }
  // Lingote genérico em pixel art (não é a textura do jogo), desenhado em (ox, oy) com pixels de tamanho s.
  function drawIngot(x, ox, oy, s, light, mid, dark) {
    const map = ['...#######.', '..#lllllll#', '.#llmmmmmd#', '#llmmmmmdd#', '#mmmmmmdd#.', '#ddddddd#..'];
    map.forEach((r, j) => [...r].forEach((ch, i) => { const col = ch === '#' ? '#1F2937' : ch === 'l' ? light : ch === 'm' ? mid : ch === 'd' ? dark : null; if (col) { x.fillStyle = col; x.fillRect(ox + i * s, oy + j * s, s, s); } }));
  }

