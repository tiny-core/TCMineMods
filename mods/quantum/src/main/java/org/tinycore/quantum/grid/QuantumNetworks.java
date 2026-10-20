package org.tinycore.quantum.grid;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;
import org.tinycore.quantum.TcQuantum;
import org.tinycore.quantum.storage.FluidKey;
import org.tinycore.quantum.storage.ItemKey;

import java.util.Optional;
import java.util.UUID;

/**
 * Todas as redes quânticas do servidor, gravadas no mundo ({@code data/tcquantum_networks.dat} do overworld).
 * {@code SavedData} é o mecanismo do Minecraft para dados do mundo que não pertencem a um bloco: o jogo grava o
 * arquivo no save quando {@link #setDirty()} foi chamado.
 *
 * <p>É a "casca" de disco do {@link NetworkDirectory} (onde está a regra). Um arquivo só para o servidor inteiro,
 * guardado no overworld, porque uma rede pode ter blocos em várias dimensões. Grava energia, membros e o conteúdo
 * ({@link NetworkStorage}); item ou fluido que não dá para ler fica guardado como estava (órfão), nunca é apagado.
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
    public NetworkRecord claim(@Nullable UUID requested, BlockLocation at) {
        NetworkRecord record = directory.claim(requested, at, UUID::randomUUID);
        setDirty();
        return record;
    }

    /** Ver {@link NetworkDirectory#detach}. */
    public void detach(UUID id, BlockLocation at) {
        if (directory.detach(id, at)) {
            setDirty();
        }
    }

    /**
     * Põe (ou atualiza) um bloco na rede.
     *
     * @return {@code false} se a rede não existe
     */
    public boolean addMember(UUID id, NetworkMember member) {
        NetworkRecord record = directory.get(id);
        if (record == null) {
            return false;
        }
        if (record.members().put(member)) {
            record.refreshCapacities();
            setDirty();
        }
        return true;
    }

    /** Tira um bloco da rede (quebrado ou desentrelaçado). */
    public void removeMember(UUID id, BlockLocation location) {
        NetworkRecord record = directory.get(id);
        if (record != null && record.members().remove(location)) {
            record.refreshCapacities();
            setDirty();
        }
    }

    /** Rede com o Núcleo mais perto de {@code pos}, na mesma dimensão, até {@code radius} blocos. */
    public @Nullable NetworkRecord nearestCore(String dimension, BlockPos pos, double radius) {
        return directory.nearestCore(dimension,
                core -> Math.sqrt(BlockPos.of(core.pos()).distSqr(pos)), radius);
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
            BlockLocation core = record.core();
            if (core != null) {
                entry.putString("coreDim", core.dimension());
                entry.putLong("corePos", core.pos());
            }
            ListTag members = new ListTag();
            for (NetworkMember member : record.members().all()) {
                CompoundTag m = new CompoundTag();
                m.putString("dim", member.location().dimension());
                m.putLong("pos", member.location().pos());
                m.putString("kind", member.kind().name());
                m.putLong("capacity", member.capacity());
                members.add(m);
            }
            entry.put("members", members);
            entry.put("items", saveItems(record.storage(), registries));
            entry.put("fluids", saveFluids(record.storage(), registries));
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
            BlockLocation core = entry.contains("coreDim", Tag.TAG_STRING)
                    ? new BlockLocation(entry.getString("coreDim"), entry.getLong("corePos"))
                    : null;
            NetworkRecord record = new NetworkRecord(entry.getUUID("id"),
                    new NetworkPower(entry.getLong("energy"), entry.getBoolean("online")), core);
            for (Tag memberTag : entry.getList("members", Tag.TAG_COMPOUND)) {
                CompoundTag m = (CompoundTag) memberTag;
                MemberKind kind = MemberKind.byName(m.getString("kind"));
                if (kind == null) {
                    TcQuantum.LOG.warn("Membro de rede com tipo desconhecido ignorado: {}", m.getString("kind"));
                    continue;
                }
                record.members().put(new NetworkMember(
                        new BlockLocation(m.getString("dim"), m.getLong("pos")), kind, m.getLong("capacity")));
            }
            loadItems(record.storage(), entry.getList("items", Tag.TAG_COMPOUND), registries);
            loadFluids(record.storage(), entry.getList("fluids", Tag.TAG_COMPOUND), registries);
            record.refreshCapacities();
            networks.directory.put(record);
        }
        return networks;
    }

    // ---------------------------------------------------------------- conteúdo (itens e fluidos)

    private static ListTag saveItems(NetworkStorage storage, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        storage.items().forEach((key, amount) -> list.add(entry(key.save(registries), amount)));
        storage.orphanItems().forEach(orphan -> list.add(orphan.copy()));
        return list;
    }

    private static ListTag saveFluids(NetworkStorage storage, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        storage.fluids().forEach((key, amount) -> list.add(entry(key.save(registries), amount)));
        storage.orphanFluids().forEach(orphan -> list.add(orphan.copy()));
        return list;
    }

    private static CompoundTag entry(Tag resource, long amount) {
        CompoundTag entry = new CompoundTag();
        entry.put("res", resource);
        entry.putLong("amount", amount);
        return entry;
    }

    private static void loadItems(NetworkStorage storage, ListTag list, HolderLookup.Provider registries) {
        for (Tag element : list) {
            CompoundTag entry = (CompoundTag) element;
            Optional<ItemKey> key = ItemKey.parse(registries, entry.get("res"));
            if (key.isPresent()) {
                storage.items().restore(key.get(), entry.getLong("amount"));
            } else {
                TcQuantum.LOG.warn("Item guardado na rede não pôde ser lido (mod removido?); mantido como estava: {}",
                        entry);
                storage.addOrphanItem(entry.copy());
            }
        }
    }

    private static void loadFluids(NetworkStorage storage, ListTag list, HolderLookup.Provider registries) {
        for (Tag element : list) {
            CompoundTag entry = (CompoundTag) element;
            Optional<FluidKey> key = FluidKey.parse(registries, entry.get("res"));
            if (key.isPresent()) {
                storage.fluids().restore(key.get(), entry.getLong("amount"));
            } else {
                TcQuantum.LOG.warn("Fluido guardado na rede não pôde ser lido (mod removido?); mantido como estava: {}",
                        entry);
                storage.addOrphanFluid(entry.copy());
            }
        }
    }
}
