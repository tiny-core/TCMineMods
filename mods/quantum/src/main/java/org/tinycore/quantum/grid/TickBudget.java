package org.tinycore.quantum.grid;

import org.jetbrains.annotations.NotNull;

import java.util.function.LongSupplier;

/**
 * Orçamento de tempo de uma rede num tick: o agendador pergunta {@link #hasTime()} antes de cada trabalho e para
 * quando o tempo acaba; o resto fica para o tick seguinte. É a regra "a rede nunca faz o mundo esperar"
 * (princípio 1 do plano).
 * <p>
 * O relógio vem de fora ({@code LongSupplier} ≈ {@code Func<long>} de C#): no jogo é {@code System::nanoTime}; nos
 * testes, um relógio falso que avança quando o teste quer.
 */
public final class TickBudget {

    private final LongSupplier clock;
    private long start;
    private long budgetNanos;

    public TickBudget(@NotNull LongSupplier clock) {
        this.clock = clock;
    }

    /** Relógio real do sistema. */
    public static TickBudget system() {
        return new TickBudget(System::nanoTime);
    }

    /** Começa a contar um tick com {@code budgetNanos} de tempo disponível. */
    public void begin(long budgetNanos) {
        this.budgetNanos = Math.max(0, budgetNanos);
        this.start = clock.getAsLong();
    }

    /** {@code true} enquanto ainda há tempo neste tick. */
    public boolean hasTime() {
        return elapsedNanos() < budgetNanos;
    }

    /** Tempo usado desde {@link #begin} (para o Painel e o "tempo de tick usado" do Núcleo). */
    public long elapsedNanos() {
        return clock.getAsLong() - start;
    }
}
