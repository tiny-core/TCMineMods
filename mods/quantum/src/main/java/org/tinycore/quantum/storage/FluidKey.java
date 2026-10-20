package org.tinycore.quantum.storage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Identidade de um fluido no armazenamento da rede: o fluido e os componentes dele, sem a quantidade (mB, guardada
 * no {@link ResourcePool} como {@code long}). Mesmo papel do {@link ItemKey}, para o {@code FluidStack} do NeoForge.
 */
public final class FluidKey {

    private final FluidStack prototype;
    private final int hash;

    private FluidKey(FluidStack prototype) {
        this.prototype = prototype;
        this.hash = FluidStack.hashFluidAndComponents(prototype);
    }

    /** Chave de um fluido (a quantidade é ignorada). Não pode estar vazio. */
    public static FluidKey of(FluidStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("FluidKey de fluido vazio");
        }
        return new FluidKey(stack.copyWithAmount(1));
    }

    public Fluid fluid() {
        return prototype.getFluid();
    }

    /** {@code FluidStack} novo deste fluido com {@code amount} mB. */
    public FluidStack toStack(int amount) {
        return prototype.copyWithAmount(amount);
    }

    public Tag save(HolderLookup.Provider registries) {
        return prototype.save(registries);
    }

    /** Lê do disco; vazio se o fluido não existe mais (mod removido) ou qualquer parte do dado é inválida. */
    public static Optional<FluidKey> parse(HolderLookup.Provider registries, @Nullable Tag tag) {
        if (tag == null) {
            return Optional.empty();
        }
        // Estrito: qualquer erro (até um componente inválido) = ilegível. O parse comum do jogo aceitaria um resultado
        // parcial, descartando o que não entendeu, e o recurso voltaria diferente do que foi guardado.
        return FluidStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result()
                .filter(s -> !s.isEmpty()).map(FluidKey::of);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof FluidKey key && hash == key.hash
                && FluidStack.isSameFluidSameComponents(prototype, key.prototype);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public @NotNull String toString() {
        return "FluidKey[" + prototype + "]";
    }
}
