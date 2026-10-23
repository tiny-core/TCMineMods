package org.tinycore.quantum.client.render;

/**
 * Cores das luzes animadas de uma rede (os efeitos do Núcleo, da Matriz e, depois, dos outros blocos).
 *
 * <p>É a mesma tabela {@code CORE_PALETTES} do protótipo ({@code tools/prototype/parts/01-*}). Por enquanto todos
 * usam {@link #DEFAULT} (Magenta + Roxo); quando a rede tiver dono e cor (fatia 1.6), o servidor manda o índice da
 * paleta e o renderizador escolhe aqui. As cores das texturas {@code glow_*} do JSON seguem a mesma paleta padrão.
 *
 * <p>{@code record} em Java ≈ {@code record} em C#: classe imutável só de dados. Cores em RGB ({@code 0xRRGGBB},
 * sem alfa): quem desenha junta o alfa com {@link #argb}.
 *
 * @param mid    cor média do cristal
 * @param bright cor viva do cristal (alterna com {@code mid})
 * @param hi     brilho mais claro (faíscas do cristal, miolo da coluna, pulsos)
 * @param strip  filetes de luz e juntas dos braços
 * @param win    janelas, coluna de energia e anéis
 * @param bolt   segunda linha dos raios
 */
public record EffectPalette(int mid, int bright, int hi, int strip, int win, int bolt) {

    /** Magenta + Roxo: a paleta padrão de toda rede. */
    public static final EffectPalette DEFAULT =
            new EffectPalette(0xC026D3, 0x7C3AED, 0xF5D0FE, 0xD946EF, 0xA855F7, 0xE9D5FF);

    /** Luz apagada (sem energia), a mesma do protótipo. */
    public static final int DARK = 0x1E1B2E;
    /** Segmento de medidor ou almofada sem luz. */
    public static final int UNLIT = 0x22252E;

    /** Junta um alfa (0 a 1) a uma cor RGB. */
    public static int argb(int rgb, float alpha) {
        int a = Math.round(Math.max(0f, Math.min(1f, alpha)) * 255f);
        return (a << 24) | (rgb & 0xFFFFFF);
    }

    /** Mistura duas cores RGB: {@code k = 0} dá {@code a}, {@code k = 1} dá {@code b}. */
    public static int mix(int a, int b, float k) {
        k = Math.max(0f, Math.min(1f, k));
        int r = Math.round(((a >> 16) & 0xFF) + (((b >> 16) & 0xFF) - ((a >> 16) & 0xFF)) * k);
        int g = Math.round(((a >> 8) & 0xFF) + (((b >> 8) & 0xFF) - ((a >> 8) & 0xFF)) * k);
        int bl = Math.round((a & 0xFF) + ((b & 0xFF) - (a & 0xFF)) * k);
        return (r << 16) | (g << 8) | bl;
    }
}
