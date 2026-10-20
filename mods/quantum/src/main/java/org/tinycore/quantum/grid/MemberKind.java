package org.tinycore.quantum.grid;

import org.jetbrains.annotations.Nullable;

/**
 * Tipos de bloco que entram numa rede pelo Entrelaçador. Cada tipo tem uma cor, usada nas linhas de partículas que
 * ligam o bloco ao Núcleo (plano, §2.1: "cor por tipo de bloco"). Novos tipos (Matriz de Fluidos, Âncoras, Bateria)
 * entram aqui nas próximas fatias. O nome do enum vai para o disco: não renomear.
 *
 * <p>{@code enum} em Java ≈ {@code enum} em C#, mas cada valor é um objeto e pode ter campos e métodos.
 */
public enum MemberKind {

    /** Matriz de Superposição de itens: soma capacidade de itens à rede. Laranja (canal de itens). */
    ITEM_MATRIX(0xF97316);

    private final int color;

    MemberKind(int color) {
        this.color = color;
    }

    /** Cor RGB (0xRRGGBB) das partículas deste tipo. */
    public int color() {
        return color;
    }

    /** Tipo pelo nome gravado no disco; {@code null} se o nome não existe mais (mod mais antigo/mais novo). */
    public static @Nullable MemberKind byName(String name) {
        for (MemberKind kind : values()) {
            if (kind.name().equals(name)) {
                return kind;
            }
        }
        return null;
    }
}
