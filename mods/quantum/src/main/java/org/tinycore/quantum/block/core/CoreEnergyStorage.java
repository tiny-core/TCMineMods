package org.tinycore.quantum.block.core;

import net.neoforged.neoforge.energy.IEnergyStorage;
import org.tinycore.quantum.grid.NetworkRecord;
import org.tinycore.quantum.storage.Amounts;

/**
 * Entrada de energia (FE) do Núcleo, no formato que cabos e geradores de qualquer mod entendem
 * ({@code IEnergyStorage} do NeoForge). Só recebe: a energia é da rede e não sai por aqui.
 *
 * <p>A API usa {@code int}; a rede guarda {@code long}. Toda leitura passa por {@link Amounts#toInt} (plano, §2.4),
 * então uma rede com mais de 2,1 mil milhões de FE aparece "cheia" para os outros mods, sem estourar.
 */
final class CoreEnergyStorage implements IEnergyStorage {

    private final QuantumCoreBlockEntity core;

    CoreEnergyStorage(QuantumCoreBlockEntity core) {
        this.core = core;
    }

    @Override
    public int receiveEnergy(int toReceive, boolean simulate) {
        NetworkRecord network = core.network();
        if (network == null) {
            return 0;
        }
        long accepted = network.power().receive(toReceive, QuantumCoreBlockEntity.energyCapacity(), simulate);
        if (!simulate && accepted > 0) {
            core.markNetworksDirty();
        }
        return (int) accepted; // nunca passa de toReceive, que já é int
    }

    @Override
    public int extractEnergy(int toExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        NetworkRecord network = core.network();
        return network == null ? 0 : Amounts.toInt(network.power().stored());
    }

    @Override
    public int getMaxEnergyStored() {
        return Amounts.toInt(QuantumCoreBlockEntity.energyCapacity());
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return core.network() != null;
    }
}
