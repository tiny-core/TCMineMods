  // ===== Terminal Portátil (item): scanner de mão com holograma livre =====
  MODELS.push({ id: 'portatil', name: 'Terminal Portátil (item)', color: C.cyan, size: 'item 3D (na mão), 12 × 17 × 5 px', radius: 1.15, target: .6, states: ['Ligado', 'Fora de alcance'],
    info: {
      parts: ['<b>Punho</b> com aderência laranja e gatilho', '<b>Cabeça</b> chanfrada com projetor no topo e 2 botões', '<b>Holograma</b> inclinado por cima do projetor, sem nada a atravessá-lo', '<b>Recetor</b> lateral com orbe roxo (o sinal até ao Núcleo), encaixado na cabeça'],
      anim: ['Ligado: holograma cintila e o orbe brilha', 'Fora de alcance: holograma some, orbe apagado e luz vermelha no punho'],
      tech: ['Abre as abas Rede, Fabricação e Observatório do Terminal', 'Alcance pela config (padrão: mesma dimensão); recarrega numa Bateria de Vácuo', 'Modelo de item 3D em JSON; holograma por textura animada']
    },
    build() {
      const g = new THREE.Group(), o = new THREE.Group(); g.add(o); o.position.set(-5 * P, 0, -2.5 * P);
      cbox(o, 3, 0, 1, 7, 8, 4, 'metal', .6); [1.5, 3.5, 5.5].forEach(y => box(o, 2.8, y, .8, 7.2, y + .9, 4.2, 'trim'));
      box(o, 4.3, 6, 4, 5.7, 7.5, 5, 'frame');
      cbox(o, 0, 8, 0, 10, 11.5, 5, 'frame', 1); box(o, .5, 11.5, .5, 9.5, 12, 4.5, 'metal');
      box(o, 1.5, 9, 4.95, 3.5, 10.5, 5.15, glow(C.trim)); box(o, 6.5, 9, 4.95, 8.5, 10.5, 5.15, glow(C.cyan));
      cbox(o, 2.5, 12, 1.5, 7.5, 13, 3.5, 'metal', .5); const slit = box(o, 3, 13, 2, 7, 13.3, 3, glow(C.cyan));
      const ledM = glow('#14532D'); box(o, 4.5, 2, 4.05, 5.5, 2.8, 4.25, ledM);
      box(o, -1.2, 8.8, 1.2, 0, 10.8, 3.8, 'frame'); box(o, -1.5, 9.3, 1.7, -1.2, 10.3, 3.3, 'trim');
      const orb = voxelsShaded(spherePoints(1), .9, 1, ['#4C1D95', '#A855F7', '#F3E8FF']); orb.position.set(-2.3 * P, 9.8 * P, 2.5 * P); o.add(orb);
      const hm = new THREE.MeshBasicMaterial({ map: TEX.holo, transparent: true, depthWrite: false, blending: THREE.AdditiveBlending, side: THREE.DoubleSide });
      const holo = new THREE.Group(); holo.position.set(5 * P, 13.4 * P, 2.5 * P); holo.rotation.x = -Math.PI / 8; o.add(holo);
      const plane = new THREE.Mesh(new THREE.PlaneGeometry(9 * P, 5.5 * P), hm); plane.position.y = 3.5 * P; holo.add(plane);
      const beam = new THREE.Mesh(new THREE.PlaneGeometry(4 * P, .8 * P), new THREE.MeshBasicMaterial({ color: C.cyan, transparent: true, opacity: .15, depthWrite: false, blending: THREE.AdditiveBlending, side: THREE.DoubleSide }));
      beam.rotation.x = Math.PI / 2; beam.position.y = .1 * P; holo.add(beam);
      return { group: g, update(t, st) {
        const on = st === 0; holo.visible = on; hm.opacity = .8 + .15 * Math.sin(t * 23); TEX.holo.offset.y = (t * .15) % 1;
        orb.material.color.set(on ? '#ffffff' : '#2A2140'); slit.material.color.set(on ? C.cyan : '#164E63'); ledM.color.set(on ? UI.ok : (Math.sin(t * 6) > 0 ? C.red : '#450A0A'));
      } };
    }
  });

