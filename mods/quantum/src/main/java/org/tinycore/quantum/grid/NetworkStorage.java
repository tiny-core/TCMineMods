package org.tinycore.quantum.grid;

import net.minecraft.nbt.Tag;
import org.tinycore.quantum.storage.FluidKey;
import org.tinycore.quantum.storage.ItemKey;
import org.tinycore.quantum.storage.ResourcePool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * O conteúdo de uma rede: um {@link ResourcePool} por tipo de recurso (plano, §2.2). Itens e fluidos agora;
 * químicos do Mekanism na fase 6. A capacidade de cada pool vem dos membros ({@link NetworkRecord#refreshCapacities}).
 *
 * <p><b>Órfãos:</b> registros do disco que não deu para ler (o item ou fluido é de um mod que saiu do modpack) ficam
 * guardados exatamente como estavam e são gravados de volta. Se o mod voltar, o conteúdo volta; nada some em silêncio.
 */
public final class NetworkStorage {

    private final ResourcePool<ItemKey> items = new ResourcePool<>();
    private final ResourcePool<FluidKey> fluids = new ResourcePool<>();
    private final List<Tag> orphanItems = new ArrayList<>();
    private final List<Tag> orphanFluids = new ArrayList<>();

    public ResourcePool<ItemKey> items() {
        return items;
    }

    public ResourcePool<FluidKey> fluids() {
        return fluids;
    }

    /** Registros de itens ilegíveis (cada um com item e quantidade, como estavam no disco). */
    public List<Tag> orphanItems() {
        return Collections.unmodifiableList(orphanItems);
    }

    public List<Tag> orphanFluids() {
        return Collections.unmodifiableList(orphanFluids);
    }

    void addOrphanItem(Tag entry) {
        orphanItems.add(entry);
    }

    void addOrphanFluid(Tag entry) {
        orphanFluids.add(entry);
    }
}
