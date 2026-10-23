package org.tinycore.quantum.client.render.core;

import org.tinycore.quantum.client.render.AppearClock;
import org.tinycore.quantum.client.render.EffectPalette;

/**
 * O "instante" da animação do Núcleo num quadro: relógio, estágios do surgimento, giro dos braços e cores. O
 * {@link CoreEffectsRenderer} preenche um objeto destes por quadro (sempre o mesmo, sem criar outro) e as peças
 * ({@link CoreCrystal}, {@link CoreRings}, {@link CoreBeam}, {@link CoreArms}, {@link CoreBolts}) só o leem.
 *
 * <p>Os números dos estágios copiam o {@code update()} do protótipo ({@code tools/prototype/parts/01-*}): as luzes
 * acendem, a coluna sobe, o cristal junta-se com um clarão, os anéis abrem-se, os braços sobem e os raios começam.
 *
 * <p>Medidas em pixels de modelo com a origem no <b>chão do 3×3×3, no meio</b> (o mesmo sistema do protótipo).
 */
final class CoreAnimation {

    /** Altura do centro do cristal (e dos braços) acima do chão do 3×3×3, em px. */
    static final float CENTER_Y = 28f;
    /** Velocidade do giro dos braços (rad/s), a do protótipo. */
    private static final double ARM_SPIN = 0.35;

    /** Segundos (com fração) para as ondas da animação; volta a zero a cada dia do jogo (um salto a cada 20 min). */
    float seconds;
    /** Giro dos braços em volta do cristal (radianos); parado sem energia. */
    float spin;
    /** Sem energia: só os braços, parados e apagados. */
    boolean off;
    /** Semente do Núcleo: dois Núcleos nunca têm os mesmos raios e espinhos. */
    int seed;
    EffectPalette palette = EffectPalette.DEFAULT;

    /** Progresso do surgimento (0 a 1) e cada estágio já suavizado. */
    float appear;
    float lights;
    float beam;
    float crystal;
    float flash;
    float ring1;
    float ring2;
    float arms;
    float bolts;
    /** Rajada de raios no fim do surgimento (1 no pico). */
    float burst;

    /** Preenche o quadro a partir do progresso do surgimento e dos relógios. */
    void update(float seconds, float appear, double runSeconds, int seed) {
        this.seconds = seconds;
        this.appear = appear;
        this.seed = seed;
        this.off = appear <= 0f;
        this.spin = (float) ((runSeconds * ARM_SPIN) % (2 * Math.PI));
        lights = AppearClock.stage(appear, 0f, 0.2f);
        beam = AppearClock.stage(appear, 0.12f, 0.35f);
        crystal = AppearClock.stage(appear, 0.28f, 0.62f);
        flash = appear > 0.55f && appear < 0.8f ? (float) Math.sin((appear - 0.55f) / 0.25f * Math.PI) : 0f;
        ring1 = AppearClock.stage(appear, 0.5f, 0.75f);
        ring2 = AppearClock.stage(appear, 0.58f, 0.82f);
        arms = AppearClock.stage(appear, 0.55f, 0.95f);
        bolts = AppearClock.stage(appear, 0.62f, 0.7f);
        burst = appear > 0.62f && appear < 0.8f ? (float) Math.sin((appear - 0.62f) / 0.18f * Math.PI) : 0f;
    }

    /** Cor de uma luz (filete, junta) neste quadro: apagada sem energia, acendendo no surgimento. */
    int light(int rgb) {
        return off ? EffectPalette.DARK : EffectPalette.mix(EffectPalette.DARK, rgb, lights);
    }

    /**
     * Número "aleatório" fixo de 0 a 1 para uma combinação de inteiros (sempre o mesmo para os mesmos valores). Dá
     * raios e espinhos variados sem guardar estado nem criar objetos {@code Random} a cada quadro.
     */
    static float noise(int a, int b, int c) {
        int h = a * 0x27D4EB2D ^ b * 0x165667B1 ^ c * 0x9E3779B9;
        h ^= h >>> 15;
        h *= 0x2C1B3C6D;
        h ^= h >>> 12;
        h *= 0x297A2D39;
        h ^= h >>> 15;
        return (h >>> 8) / 16_777_216f;
    }
}
