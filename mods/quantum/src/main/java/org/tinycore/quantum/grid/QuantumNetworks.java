package org.tinycore.quantum.grid;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;
import org.tinycore.quantum.TcQuantum;

import java.util.UUID;

/**
 * Todas as redes quânticas do servidor, gravadas no mundo ({@code data/tcquantum_networks.dat} do overworld).
 * {@code SavedData} é o mecanismo do Minecraft para dados do mundo que não pertencem a um bloco: o jogo grava o
 * arquivo no save quando {@link #setDirty()} foi chamado.
 *
 * <p>É a "casca" de disco do {@link NetworkDirectory} (onde está a regra). Um arquivo só para o servidor inteiro,
 * guardado no overworld, porque uma rede pode ter blocos em várias dimensões.
 */
public final class QuantumNetworks extends SavedData {

    private static final String FILE_NAME = TcQuantum.MOD_ID + "_networks";
    private static final SavedData.Factory<QuantumNetworks> FACTORY =
            new SavedData.Factory<>(QuantumNetworks::new, QuantumNetworks::load, null);

    private final NetworkDirectory directory = new NetworkDirectory();

    /** Registro único do servidor (de qualquer dimensão, sempre o do overworld). */
    public static QuantumNetworks get(ServerLevel level) {
        return level.getServer().overworld().getDataStorage().computeIfAbsent(FACTORY, FILE_NAME);
    }

    public @Nullable NetworkRecord get(UUID id) {
        return directory.get(id);
    }

    /** Ver {@link NetworkDirectory#claim}. */
    public NetworkRecord claim(@Nullable UUID requested, CoreLocation at) {
        NetworkRecord record = directory.claim(requested, at, UUID::randomUUID);
        setDirty();
        return record;
    }

    /** Ver {@link NetworkDirectory#detach}. */
    public void detach(UUID id, CoreLocation at) {
        if (directory.detach(id, at)) {
            setDirty();
        }
    }

    // ---------------------------------------------------------------- NBT

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (NetworkRecord record : directory.records()) {
            CompoundTag entry = new CompoundTag();
            entry.putUUID("id", record.id());
            entry.putLong("energy", record.power().stored());
            entry.putBoolean("online", record.power().online());
            CoreLocation core = record.core();
            if (core != null) {
                entry.putString("coreDim", core.dimension());
                entry.putLong("corePos", core.pos());
            }
            list.add(entry);
        }
        tag.put("networks", list);
        return tag;
    }

    private static QuantumNetworks load(CompoundTag tag, HolderLookup.Provider registries) {
        QuantumNetworks networks = new QuantumNetworks();
        for (Tag element : tag.getList("networks", Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) element;
            if (!entry.hasUUID("id")) {
                TcQuantum.LOG.warn("Rede quântica sem id ignorada no {}.dat", FILE_NAME);
                continue;
            }
            CoreLocation core = entry.contains("coreDim", Tag.TAG_STRING)
                    ? new CoreLocation(entry.getString("coreDim"), entry.getLong("corePos"))
                    : null;
            networks.directory.put(new NetworkRecord(entry.getUUID("id"),
                    new NetworkPower(entry.getLong("energy"), entry.getBoolean("online")), core));
        }
        return networks;
    }
}
