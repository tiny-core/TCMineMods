// Folha de contacto: node tools/sheet.js <id> <estados ex. 0,1,3> <saida.png> [camX,camY,camZ,tx,ty,tz] [paleta]
// Requer PW_DIR com node_modules (playwright-core, three@0.128.0, @sparticuz/chromium).
const path = require('path'), fs = require('fs');
const PW = process.env.PW_DIR || path.join(__dirname, 'pw');
const req = require('module').createRequire(path.join(PW, 'x.js'));
const { chromium } = req('playwright-core'), spx = req('@sparticuz/chromium').default;
const [id, states = '0', out = 'sheet.png', cam, pal = '0'] = process.argv.slice(2);
(async () => {
  let html = fs.readFileSync(path.join(__dirname, 'tc-quantum-modelos.html'), 'utf8');
  const lib = f => fs.readFileSync(path.join(PW, 'node_modules', 'three', f), 'utf8');
  html = html.replace(/<link[^>]*fonts\.googleapis[^>]*>/g, '') // sem rede: a fonte bloquearia o carregamento
    .replace(/<script src="[^"]*three\.min\.js"><\/script>/, () => '<script>' + lib('build/three.min.js') + '</script>')
    .replace(/<script src="[^"]*OrbitControls\.js"><\/script>/, () => '<script>' + lib('examples/js/controls/OrbitControls.js') + '</script>');
  const browser = await chromium.launch({ executablePath: await spx.executablePath(), args: [...spx.args, '--enable-unsafe-swiftshader'] });
  const page = await browser.newPage({ viewport: { width: 900, height: 700 }, reducedMotion: 'reduce' });
  const errs = []; page.on('pageerror', e => errs.push(e.message)); page.on('console', m => m.type() === 'error' && errs.push(m.text()));
  await page.setContent(html, { waitUntil: 'load' }); await page.waitForFunction(() => window.__tcq);
  const data = await page.evaluate(({ id, sts, cam, pal }) => {
    const T = window.__tcq, i = T.MODELS.findIndex(m => m.id === id); if (i < 0) return { err: 'id desconhecido: ' + id + ' (' + T.MODELS.map(m => m.id).join(', ') + ')' };
    const m = T.MODELS[i], shots = [];
    for (const st of sts) {
      T.select(i); T.controls.autoRotate = false;
      if (cam) { const c = cam.split(',').map(Number); T.camera.position.set(c[0], c[1], c[2]); T.controls.target.set(c[3], c[4], c[5]); }
      T.frame(0, st, pal); shots.push({ url: T.frame(6, st, pal), label: m.states ? m.states[st] : String(st) });
    }
    return { shots };
  }, { id, sts: states.split(',').map(Number), cam, pal: +pal });
  if (data.err) { console.error(data.err); process.exit(1); }
  const W = 450, H = 350, cols = Math.min(3, data.shots.length), rows = Math.ceil(data.shots.length / cols);
  const grid = await page.evaluate(async ({ shots, W, H, cols, rows }) => {
    const c = document.createElement('canvas'); c.width = W * cols; c.height = H * rows; const x = c.getContext('2d');
    x.fillStyle = '#111'; x.fillRect(0, 0, c.width, c.height); x.font = '16px sans-serif';
    for (let k = 0; k < shots.length; k++) { const img = new Image(); img.src = shots[k].url; await img.decode();
      const cx = (k % cols) * W, cy = Math.floor(k / cols) * H; x.drawImage(img, cx, cy, W, H); x.fillStyle = '#fff'; x.fillText(shots[k].label, cx + 8, cy + 20); }
    return c.toDataURL('image/png');
  }, { shots: data.shots, W, H, cols, rows });
  fs.writeFileSync(out, Buffer.from(grid.split(',')[1], 'base64'));
  console.log(errs.length ? 'ERROS:\n' + errs.join('\n') : 'ok, sem erros'); await browser.close();
})();
