package org.tinycore.quantum.storage;

import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.ObjLongConsumer;

/**
 * O <b>pool</b> de um tipo de recurso da rede (itens, fluidos...): um mapa "recurso → quantidade" com uma
 * <b>capacidade</b> total. As Matrizes de Superposição não guardam nada; só somam capacidade a este pool, que vive
 * no Núcleo. Por isso um bloco num chunk descarregado não faz itens sumirem da rede.
 * <p>
 * Genérico no tipo da chave ({@code <K>} ≈ generic de C#): no jogo a chave é o recurso (item + componentes, fluido...);
 * nos testes, uma {@code String}. A chave precisa de {@code equals}/{@code hashCode} coerentes (records já têm).
 * <p>
 * Desempenho: inserir e extrair são O(1), um acesso a um mapa de primitivos do fastutil (biblioteca que já vem com o
 * Minecraft) — nada de procurar disco por prioridade, como no AE2. {@code Object2LongOpenHashMap} guarda o
 * {@code long} direto, sem criar um objeto {@code Long} por item (o que um {@code HashMap<K, Long>} faria).
 * <p>
 * Regras:
 * <ul>
 *   <li>quantidades e totais saturam em {@link Amounts#MAX} (nunca dão a volta para negativo);</li>
 *   <li>inserir aceita no máximo o espaço livre; acima da capacidade (uma Matriz foi partida), a rede só deixa
 *       retirar até voltar ao normal — nada é destruído;</li>
 *   <li>{@code simulate = true} responde "quanto daria" sem mudar nada (sempre simular antes de mover de verdade);</li>
 *   <li>cada chave que mudou fica marcada até {@link #drainChanges}, para o servidor mandar ao cliente só as
 *       diferenças.</li>
 * </ul>
 * Não é seguro para várias threads: só a thread do servidor mexe no pool (o Observatório trabalha sobre
 * {@link #snapshot()}).
 */
public final class ResourcePool<K> {

    private final Object2LongOpenHashMap<K> amounts = new Object2LongOpenHashMap<>();
    private final ObjectOpenHashSet<K> changed = new ObjectOpenHashSet<>();
    private long stored;
    private long capacity;

    public ResourcePool() {
        amounts.defaultReturnValue(0L); // recurso que não está no mapa = quantidade 0
    }

    /** Capacidade total (soma das Matrizes deste tipo). */
    public long capacity() {
        return capacity;
    }

    /** Muda a capacidade (Matriz colocada, partida ou com módulos trocados). Negativa conta como zero. */
    public void setCapacity(long capacity) {
        this.capacity = Math.max(0, capacity);
    }

    /** Soma de todas as quantidades guardadas. */
    public long stored() {
        return stored;
    }

    /** Espaço livre; zero quando está cheio ou acima da capacidade. */
    public long free() {
        return Amounts.subtract(capacity, stored);
    }

    /** {@code true} quando há mais guardado do que cabe (uma Matriz foi partida): só se pode retirar. */
    public boolean overCapacity() {
        return stored > capacity;
    }

    /** Quantos recursos diferentes estão guardados. */
    public int types() {
        return amounts.size();
    }

    /** Quantidade guardada de {@code key} (0 se não houver). */
    public long amount(@NotNull K key) {
        return amounts.getLong(Objects.requireNonNull(key));
    }

    /**
     * Guarda até {@code amount} de {@code key}.
     *
     * @param simulate {@code true} = só calcula, não muda nada
     * @return quanto foi (ou seria) aceito: entre 0 e {@code amount}, limitado pelo espaço livre
     */
    public long insert(@NotNull K key, long amount, boolean simulate) {
        Objects.requireNonNull(key);
        if (amount <= 0) {
            return 0;
        }
        long accepted = Math.min(amount, free());
        // A quantidade de um recurso também satura (o total já limita, mas o máximo é o mesmo número).
        long current = amounts.getLong(key);
        accepted = Math.min(accepted, Amounts.subtract(Amounts.MAX, current));
        if (accepted <= 0 || simulate) {
            return Math.max(0, accepted);
        }
        amounts.put(key, current + accepted);
        stored = Amounts.add(stored, accepted);
        changed.add(key);
        return accepted;
    }

    /**
     * Retira até {@code amount} de {@code key}.
     *
     * @param simulate {@code true} = só calcula, não muda nada
     * @return quanto foi (ou seria) retirado: entre 0 e {@code amount}, limitado pelo que existe
     */
    public long extract(@NotNull K key, long amount, boolean simulate) {
        Objects.requireNonNull(key);
        if (amount <= 0) {
            return 0;
        }
        long current = amounts.getLong(key);
        long removed = Math.min(amount, current);
        if (removed <= 0 || simulate) {
            return Math.max(0, removed);
        }
        if (removed == current) {
            amounts.removeLong(key);
        } else {
            amounts.put(key, current - removed);
        }
        stored = Amounts.subtract(stored, removed);
        changed.add(key);
        return removed;
    }

    /** Percorre tudo o que está guardado, sem criar objetos por entrada ({@code fastIterable}). */
    public void forEach(@NotNull ObjLongConsumer<K> action) {
        for (Object2LongMap.Entry<K> entry : Object2LongMaps.fastIterable(amounts)) {
            action.accept(entry.getKey(), entry.getLongValue());
        }
    }

    /** {@code true} se alguma quantidade mudou desde o último {@link #drainChanges}. */
    public boolean hasChanges() {
        return !changed.isEmpty();
    }

    /**
     * Entrega cada recurso que mudou com a quantidade atual (0 = acabou) e limpa a marcação. É o que o servidor
     * manda ao cliente: só as diferenças, não a lista inteira.
     */
    public void drainChanges(@NotNull ObjLongConsumer<K> sink) {
        for (K key : changed) {
            sink.accept(key, amounts.getLong(key));
        }
        changed.clear();
    }

    /** Cópia das quantidades, para trabalho fora da thread do servidor (ex.: o Observatório numa thread própria). */
    public @NotNull Object2LongOpenHashMap<K> snapshot() {
        Object2LongOpenHashMap<K> copy = new Object2LongOpenHashMap<>(amounts);
        copy.defaultReturnValue(0L);
        return copy;
    }
}
