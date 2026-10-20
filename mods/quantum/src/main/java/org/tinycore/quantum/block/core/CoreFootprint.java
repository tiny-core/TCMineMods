package org.tinycore.quantum.block.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Geometria do Núcleo Quântico: um cubo 3×3×3 com o bloco principal no centro e 26 blocos de estrutura em volta.
 * Regra pura (só números), usada pelo {@link QuantumCoreBlock} para verificar o espaço e montar a estrutura, e pelo
 * {@link CoreStructureBlock} para achar o centro a partir das propriedades do próprio bloco.
 *
 * <p>Cada bloco de estrutura guarda a sua posição no cubo em três propriedades de 0 a 2 (ox, oy, oz); o centro é
 * (1, 1, 1). Assim nenhum bloco de estrutura precisa de block entity: o estado do bloco já diz onde está o centro.
 */
public final class CoreFootprint {

    /** Valor das propriedades ox/oy/oz no centro do cubo. */
    public static final int CENTER = 1;
    /** Maior valor das propriedades (o cubo vai de 0 a 2 em cada eixo). */
    public static final int MAX_INDEX = 2;

    /**
     * Deslocamento de um bloco em relação ao centro (−1, 0 ou 1 em cada eixo).
     * {@code record} em Java ≈ {@code record} em C#: classe imutável de dados, com igualdade por valor.
     */
    public record Offset(int dx, int dy, int dz) {

        /** Lê a posição a partir das propriedades ox/oy/oz (0 a 2). */
        public static Offset fromIndices(int x, int y, int z) {
            return new Offset(x - CENTER, y - CENTER, z - CENTER);
        }

        public int indexX() {
            return dx + CENTER;
        }

        public int indexY() {
            return dy + CENTER;
        }

        public int indexZ() {
            return dz + CENTER;
        }

        public boolean isCenter() {
            return dx == 0 && dy == 0 && dz == 0;
        }
    }

    private static final List<Offset> ALL = buildAll();
    private static final List<Offset> STRUCTURE = ALL.stream().filter(o -> !o.isCenter()).toList();

    private CoreFootprint() {}

    /** As 27 posições do cubo, centro incluído. */
    public static List<Offset> all() {
        return ALL;
    }

    /** As 26 posições dos blocos de estrutura (todas menos o centro). */
    public static List<Offset> structure() {
        return STRUCTURE;
    }

    private static List<Offset> buildAll() {
        List<Offset> offsets = new ArrayList<>(27);
        for (int dy = -1; dy <= 1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    offsets.add(new Offset(dx, dy, dz));
                }
            }
        }
        return List.copyOf(offsets);
    }
}
