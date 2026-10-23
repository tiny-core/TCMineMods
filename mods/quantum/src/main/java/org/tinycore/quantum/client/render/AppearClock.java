package org.tinycore.quantum.client.render;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;

/**
 * Relógio do <b>surgimento</b>: quanto da animação de "ligar" já passou em cada bloco (0 = desligado, 1 = pronto).
 *
 * <p>O servidor só diz se o bloco está ligado; quando a animação começou é um detalhe visual, então é o cliente que
 * conta: lembra o último estado visto de cada posição e marca o instante em que passou de desligado para ligado. Um
 * bloco visto pela primeira vez já ligado (mundo a carregar, chegar perto) aparece pronto, sem animação.
 *
 * <p>Cada renderizador (Núcleo, Matriz) tem o seu relógio, com a duração do seu protótipo. Posições que não são
 * desenhadas há {@link #FORGET_AFTER_TICKS} ticks são esquecidas, para o mapa não crescer sem fim.
 */
public final class AppearClock {

    private static final long FORGET_AFTER_TICKS = 600;
    private static final long CLEANUP_EVERY_TICKS = 200;

    private final float durationTicks;
    private final Map<BlockPos, State> states = new HashMap<>();

    private static final class State {
        boolean online;
        long onlineSince = Long.MIN_VALUE;
        long lastSeen;
        /** Início da contagem de "tempo ligado" (pausa enquanto desligado) e o valor guardado ao desligar. */
        long runStart;
        long frozenRun;
    }

    /** Tempo ligado do último bloco consultado, em ticks: para quando desliga e continua de onde parou ao religar. */
    private double lastRunTicks;

    /** @param durationTicks duração do surgimento em ticks (20 ticks = 1 s) */
    public AppearClock(float durationTicks) {
        this.durationTicks = durationTicks;
    }

    /** Progresso do surgimento do bloco em {@code pos} neste quadro: 0 desligado, 1 pronto. */
    public float progress(BlockPos pos, boolean online, long gameTime, float partialTick) {
        State state = states.get(pos);
        if (state == null) {
            state = new State();
            state.online = online; // visto pela primeira vez: sem animação
            states.put(pos.immutable(), state);
        } else if (online && !state.online) {
            state.onlineSince = gameTime;
            state.runStart = gameTime - state.frozenRun;
        } else if (!online && state.online) {
            state.frozenRun = gameTime - state.runStart;
        }
        state.online = online;
        state.lastSeen = gameTime;
        if (gameTime % CLEANUP_EVERY_TICKS == 0 && states.size() > 1) {
            states.values().removeIf(s -> gameTime - s.lastSeen > FORGET_AFTER_TICKS);
        }
        lastRunTicks = online ? gameTime - state.runStart + partialTick : state.frozenRun;
        if (!online) {
            return 0f;
        }
        if (state.onlineSince == Long.MIN_VALUE) {
            return 1f;
        }
        return Math.min(1f, (gameTime - state.onlineSince + partialTick) / durationTicks);
    }

    /**
     * Tempo ligado (ticks) do bloco passado no último {@link #progress}: o relógio das peças que devem ficar paradas
     * onde estavam quando a energia acaba (os braços), em vez de saltar. Chamar logo depois de {@code progress}.
     */
    public double runTicks() {
        return lastRunTicks;
    }

    /** Transição suave de 0 (antes de {@code from}) a 1 (depois de {@code to}), como o {@code smooth} do protótipo. */
    public static float stage(float progress, float from, float to) {
        float t = Math.max(0f, Math.min(1f, (progress - from) / (to - from)));
        return t * t * (3f - 2f * t);
    }
}
