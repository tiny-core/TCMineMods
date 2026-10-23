package org.tinycore.quantum.block.core;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.tinycore.quantum.block.core.CoreFootprint.Offset;

/**
 * Forma do Núcleo Quântico no mundo: o contorno de seleção e a colisão seguem o modelo, não o cubo 3×3×3.
 *
 * <p>São duas caixas, em pixels relativos ao bloco do centro (o mesmo sistema do {@code quantum_core.json}):
 * a <b>base de contenção</b>, que ocupa o chão 3×3 inteiro, e uma caixa à volta do <b>cristal</b>. Os braços e o
 * resto do que é animado ficam de fora: o contorno do Minecraft é fixo e eles mexem-se a cada quadro.</p>
 *
 * <p>Um {@link VoxelShape} é a forma de um bloco feita de caixas (usada para a mira, o contorno e a colisão). Pode
 * passar dos limites do próprio bloco, e é assim que cada uma das 27 peças mostra o contorno do Núcleo inteiro.
 * As formas são calculadas uma vez, no carregamento da classe ({@code static}), e partilhadas por todas as peças.</p>
 *
 * <p>Usado por {@link QuantumCoreBlock} (centro) e {@link CoreStructureBlock} (as 26 peças em volta). Mudou a base
 * ou o cristal no protótipo ({@code tools/prototype/parts/01-*}), atualize as caixas aqui.</p>
 */
public final class CoreShape {

    /** Base de contenção: chão 3×3 inteiro, da camada de baixo até o topo dos pinos emissores (15,6 px). */
    private static final VoxelShape BASE = Block.box(-16, -16, -16, 32, -0.4, 32);
    /** Cristal: centro a 12 px de altura no bloco do centro, com folga para os espinhos e o pulso. */
    private static final VoxelShape CRYSTAL = Block.box(1, 5, 1, 15, 19, 15);
    /** O Núcleo inteiro, visto do bloco do centro. */
    private static final VoxelShape WHOLE = Shapes.or(BASE, CRYSTAL);

    /** Contorno visto de cada peça (índice = x·9 + y·3 + z), o Núcleo inteiro deslocado. */
    private static final VoxelShape[] OUTLINES = new VoxelShape[27];
    /** Colisão de cada peça: só o pedaço do Núcleo que cai dentro do próprio bloco (vazio onde não há modelo). */
    private static final VoxelShape[] COLLISIONS = new VoxelShape[27];

    static {
        for (Offset o : CoreFootprint.all()) {
            int i = index(o.indexX(), o.indexY(), o.indexZ());
            OUTLINES[i] = WHOLE.move(-o.dx(), -o.dy(), -o.dz());
            // AND com o bloco: corta a forma no limite da peça, para cada uma colidir só no próprio espaço.
            COLLISIONS[i] = Shapes.join(OUTLINES[i], Shapes.block(), BooleanOp.AND).optimize();
        }
    }

    private CoreShape() {
    }

    /** Contorno de seleção (e mira) da peça nos índices 0..2 de cada eixo (1 = centro). */
    public static @NotNull VoxelShape outline(int indexX, int indexY, int indexZ) {
        return OUTLINES[index(indexX, indexY, indexZ)];
    }

    /** Colisão da peça nos índices 0..2 de cada eixo (1 = centro). */
    public static @NotNull VoxelShape collision(int indexX, int indexY, int indexZ) {
        return COLLISIONS[index(indexX, indexY, indexZ)];
    }

    private static int index(int x, int y, int z) {
        return x * 9 + y * 3 + z;
    }
}
