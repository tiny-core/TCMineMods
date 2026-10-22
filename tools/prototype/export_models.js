// Exporta as peças PARADAS de cada modelo do protótipo para JSON de bloco do Minecraft.
// Uso: node tools/export_models.js <pasta de saída> [id ...]
// Peça parada = visível e no mesmo sítio em todos os estados e instantes amostrados; o resto (braços, cristais,
// raios...) fica para o BlockEntityRenderer. Depois de exportar, rode no repo:
//   python3 tools/textures/quantum_textures.py   (cria as texturas glow_/glass_/flat_ novas)
//   python3 tools/textures/remap_uv.py           (acerta as UVs pela posição)
const path = require('path'), fs = require('fs');
const PW = process.env.PW_DIR || path.join(__dirname, 'pw');
const req = require('module').createRequire(path.join(PW, 'x.js'));
const { chromium } = req('playwright-core'), spx = req('@sparticuz/chromium').default;

// id do protótipo → arquivo e deslocamento (px) do centro do protótipo para as coordenadas do bloco principal
const MAP = {
  nucleo: ['quantum_core', [8, -16, 8]], colapsador: ['collapser', [8, -16, 8]], regulador: ['stock_regulator', [8, -16, 8]],
  matriz: ['superposition_matrix', [8, 0, 8]], bateria: ['vacuum_battery', [8, -16, 8]], processador: ['quantum_processor', [8, -16, 8]],
  terminal: ['terminal', [8, 0, 8]], tunel: ['quantum_tunnel', [8, -16, 8]], manipulador: ['quantum_manipulator', [8, 0, 16]],
  interface: ['quantum_interface', [8, 0, 8]], interface_painel: ['interface_panel', [8, 0, -8]]
};
const KNOWN_TEX = ['metal', 'frame', 'dark', 'panel', 'trim', 'vent', 'keys'];

const [outDir, ...only] = process.argv.slice(2);
if (!outDir) { console.error('uso: node tools/export_models.js <pasta> [id ...]'); process.exit(1); }

(async () => {
  let html = fs.readFileSync(path.join(__dirname, 'tc-quantum-modelos.html'), 'utf8');
  const lib = f => fs.readFileSync(path.join(PW, 'node_modules', 'three', f), 'utf8');
  html = html.replace(/<link[^>]*fonts\.googleapis[^>]*>/g, '') // sem rede: a fonte bloquearia o carregamento
    .replace(/<script src="[^"]*three\.min\.js"><\/script>/, () => '<script>' + lib('build/three.min.js') + '</script>')
    .replace(/<script src="[^"]*OrbitControls\.js"><\/script>/, () => '<script>' + lib('examples/js/controls/OrbitControls.js') + '</script>');
  const browser = await chromium.launch({ executablePath: await spx.executablePath(), args: [...spx.args, '--enable-unsafe-swiftshader'] });
  const page = await browser.newPage();
  await page.setContent(html, { waitUntil: 'load' }); await page.waitForFunction(() => window.__tcq);
  fs.mkdirSync(outDir, { recursive: true });
  for (const [id, [file, off]] of Object.entries(MAP)) {
    if (only.length && !only.includes(id)) continue;
    const res = await page.evaluate(({ id, off, KNOWN_TEX }) => {
      window.__EXPORT = true; const T = window.__tcq, m = T.MODELS.find(x => x.id === id), inst = m.build(), root = inst.group;
      const meshes = []; root.traverse(o => { if (o.isMesh && !o.isInstancedMesh) meshes.push(o); });
      const shown = o => { for (let p = o; p; p = p.parent) if (!p.visible) return false; return true; };
      const rec = new Map(meshes.map(o => [o, { vis: true, mat: null, moved: false }]));
      let t = 10; const lv = m.levels ? m.levels[1] : 0;
      const sample = st => { inst.update(t, st, lv, 0); root.updateMatrixWorld(true); t += .37 + (t * 7.3 % .5);
        meshes.forEach(o => { const r = rec.get(o); if (!shown(o)) r.vis = false; const e = o.matrixWorld.elements.slice();
          if (!r.mat) r.mat = e; else if (e.some((v, i) => Math.abs(v - r.mat[i]) > 1e-5)) r.moved = true; }); };
      for (let st = 0; st < m.states.length; st++) for (let k = 0; k < 6; k++) sample(st);
      inst.update(t + 50, 0, lv, 0); inst.update(t + 60, 0, lv, 0); root.updateMatrixWorld(true); // cores finais, já surgido
      const texName = mat => { if (mat.map) { for (const [k, v] of Object.entries(T.TEX)) if (v === mat.map) return k; } return null; };
      const hx = c => c.getHexString();
      const Mx = new THREE.Matrix4(), pos = new THREE.Vector3(), q = new THREE.Quaternion(), scl = new THREE.Vector3(), warn = [];
      const aligned = M => { const e = M.elements; for (let i of [0, 1, 2, 4, 5, 6, 8, 9, 10]) { const a = Math.abs(e[i]); if (a > .02 && a < .98) return false; } return true; };
      const score = M => { const e = M.elements; let s = 0; for (let i of [0, 1, 2, 4, 5, 6, 8, 9, 10]) { const a = Math.abs(e[i]); s += Math.min(a, Math.abs(1 - a)); } return s; };
      const rot = (axis, deg) => new THREE.Matrix4()['makeRotation' + axis.toUpperCase()](deg * Math.PI / 180);
      const els = [];
      const pushBox = (center, half, R, material) => {
        let axis = null, deg = 0, M = R.clone();
        if (!aligned(R)) {
          let best = 1e9;
          for (const ax of ['y', 'x', 'z']) for (let d = .25; d < 360; d += .25) { const c = rot(ax, -d).multiply(R), sc = score(c); if (sc < best) { best = sc; axis = ax; deg = d; M = c; } }
          if (best > .05) { warn.push('rotação composta ignorada'); return; }
        }
        let a = 0;
        if (axis) { const s = Math.round(deg / 22.5) * 22.5; a = ((s + 45) % 90 + 90) % 90 - 45; M = rot(axis, s - a).multiply(M); if (Math.abs(s - deg) > .6) warn.push(`ângulo ${deg}° → ${s}°`); }
        const e = M.elements, ex = [0, 1, 2].map(i => Math.abs(e[i]) * half[0] + Math.abs(e[i + 4]) * half[1] + Math.abs(e[i + 8]) * half[2]);
        const c = [center.x * 16 + off[0], center.y * 16 + off[1], center.z * 16 + off[2]], r3 = v => Math.round(v * 1000) / 1000;
        const from = c.map((v, i) => r3(v - ex[i])), to = c.map((v, i) => r3(v + ex[i]));
        if (from.some(v => v < -16) || to.some(v => v > 32)) warn.push('fora de −16..32: ' + from + ' / ' + to);
        const el = { from, to, material };
        if (a !== 0) el.rotation = { angle: axis === 'y' ? -a : a, axis, origin: c.map(r3) };
        els.push(el);
      };
      meshes.forEach(o => {
        const r = rec.get(o); if (!r.vis || r.moved || o.userData.noExport) return; // noExport: bloco de apoio (ex.: o baú atrás do Painel)
        const mat = Array.isArray(o.material) ? o.material[4] : o.material;
        let material;
        if (mat.isMeshBasicMaterial) { if (mat.map) return; material = 'glow_' + hx(mat.color); }
        else if (mat.transparent && mat.opacity < .5) material = 'glass_' + hx(mat.color);
        else { const n = texName(mat); material = n ? (KNOWN_TEX.includes(n) ? n : (warn.push('textura ' + n + ' → metal'), 'metal')) : 'flat_' + hx(mat.color); }
        o.matrixWorld.decompose(pos, q, scl); const R = new THREE.Matrix4().makeRotationFromQuaternion(q), gp = o.geometry.parameters;
        if (o.geometry.type === 'BoxGeometry') pushBox(pos, [gp.width * scl.x * 8, gp.height * scl.y * 8, gp.depth * scl.z * 8], R, material);
        else if (o.geometry.type === 'CylinderGeometry') { // cilindro → octógono de 2 caixas
          const rr = Math.max(gp.radiusTop, gp.radiusBottom) * scl.x * 16, h = gp.height * scl.y * 8;
          pushBox(pos, [rr * .92, h, rr * .92], R, material); pushBox(pos, [rr * .92, h - .01, rr * .92], R.clone().multiply(rot('y', 45)), material);
        } else warn.push('geometria ' + o.geometry.type + ' ignorada');
      });
      return { els, warn: [...new Set(warn)].slice(0, 12), glass: els.some(e => e.material.startsWith('glass_')) };
    }, { id, off, KNOWN_TEX });
    // monta o JSON no formato dos arquivos atuais
    const mats = [...new Set(res.els.map(e => e.material))], key = n => '#t' + mats.indexOf(n);
    const json = { format_version: '26.3', credit: 'TC Quantum — gerado do protótipo 3D (tools/prototype); ajuste no Blockbench' };
    if (res.glass) json.render_type = 'minecraft:translucent';
    json.textures = { particle: 'tcquantum:block/metal' }; mats.forEach((n, i) => json.textures['t' + i] = 'tcquantum:block/' + n);
    json.elements = res.els.map(e => { const o = { from: e.from, to: e.to }; if (e.rotation) o.rotation = e.rotation; o.faces = {};
      for (const f of ['north', 'east', 'south', 'west', 'up', 'down']) o.faces[f] = { uv: [0, 0, 16, 16], texture: key(e.material) };
      return o; });
    fs.writeFileSync(path.join(outDir, file + '.json'), JSON.stringify(json, null, '\t') + '\n');
    console.log(`${id} → ${file}.json: ${res.els.length} peças${res.warn.length ? '\n   avisos: ' + res.warn.join(' | ') : ''}`);
  }
  await browser.close();
})();
