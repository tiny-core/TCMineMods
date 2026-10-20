package org.tinycore.quantum;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Config do TC Quantum ({@code config/tcquantum-common.toml}). É do tipo <b>COMMON</b> de propósito: a config do
 * tipo SERVER é sincronizada para os clientes, e estes valores só interessam ao servidor.
 * <p>
 * {@code ModConfigSpec} monta o arquivo e valida os limites; cada {@code XxxValue} lê o valor atual com
 * {@code .get()} (o arquivo pode ser editado com o jogo aberto).
 */
public final class Config {
    private static final ModConfigSpec.Builder B = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue TICK_BUDGET_MICROS = B
            .comment("Tempo máximo que cada rede pode usar por tick, em microssegundos (2000 = 2 ms).",
                    "O que não couber passa para o tick seguinte: a rede nunca faz o mundo esperar.")
            .defineInRange("tickBudgetMicros", 2000, 100, 50_000);

    public static final ModConfigSpec.IntValue IDLE_BACKOFF_MAX_TICKS = B
            .comment("Espera máxima, em ticks, de um bloco sem trabalho (Âncora, Interface) antes de olhar de novo.",
                    "A espera dobra a cada ciclo vazio (1, 2, 4, ...) e volta a 1 quando aparece trabalho.")
            .defineInRange("idleBackoffMaxTicks", 40, 1, 400);

    public static final ModConfigSpec.LongValue CORE_ENERGY_CAPACITY = B
            .comment("Energia que o Núcleo Quântico guarda sozinho, em FE (as Baterias somam a isto quando existirem).")
            .defineInRange("coreEnergyCapacity", 1_000_000L, 1_000L, Long.MAX_VALUE);

    public static final ModConfigSpec.IntValue CORE_ENERGY_PER_TICK = B
            .comment("Consumo base da rede, em FE por tick, enquanto o Núcleo está carregado. 0 = rede sem consumo.")
            .defineInRange("coreEnergyPerTick", 16, 0, 1_000_000);

    public static final ModConfigSpec.IntValue STANDBY_RESUME_TICKS = B
            .comment("Sem energia, a rede entra em espera e só volta com energia guardada para este número de ticks",
                    "de consumo (100 = 5 segundos). Evita que ela fique ligando e desligando com pouca entrada.")
            .defineInRange("standbyResumeTicks", 100, 1, 72_000);

    public static final ModConfigSpec.LongValue MATRIX_ITEM_CAPACITY = B
            .comment("Capacidade de itens que cada Matriz de Superposição (Itens) soma à rede, sem módulos.")
            .defineInRange("matrixItemCapacity", 65_536L, 1L, Long.MAX_VALUE);

    public static final ModConfigSpec.IntValue AUTO_ENTANGLE_RADIUS = B
            .comment("Um bloco do mod colocado até esta distância (em blocos) de um Núcleo entra sozinho na rede dele.",
                    "0 = desligado (só pelo Entrelaçador).")
            .defineInRange("autoEntangleRadius", 16, 0, 256);

    public static final ModConfigSpec.IntValue ENTANGLE_LINES_RANGE = B
            .comment("Com o Entrelaçador na mão, mostra as ligações dos blocos até esta distância (em blocos) do jogador.")
            .defineInRange("entangleLinesRange", 48, 8, 128);

    public static final ModConfigSpec SPEC = B.build();

    private Config() {}
}
