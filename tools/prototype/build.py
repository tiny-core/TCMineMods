import glob,os
root=os.path.dirname(os.path.abspath(__file__))
out=''.join(open(f,encoding='utf-8').read() for f in sorted(glob.glob(os.path.join(root,'parts','*'))))
open(os.path.join(root,'tc-quantum-modelos.html'),'w',encoding='utf-8').write(out)
