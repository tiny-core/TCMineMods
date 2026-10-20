package org.tinycore.quantum.storage;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Identidade de um item no armazenamento da rede: o item e os componentes dele (encantamentos, nome, dano...), sem
 * a quantidade. Dois {@code ItemKey} são iguais se e só se as pilhas se juntariam num inventário; a quantidade fica
 * no {@link ResourcePool} como {@code long}.
 *
 * <p>Imutável: guarda uma cópia com 1 unidade e nunca a entrega para fora (só cópias, em {@link #toStack}).
 */
public final class ItemKey {

    private final ItemStack prototype;
    private final int hash;

    private ItemKey(ItemStack prototype) {
        this.prototype = prototype;
        this.hash = ItemStack.hashItemAndComponents(prototype);
    }

    /** Chave de uma pilha (a quantidade da pilha é ignorada). A pilha não pode estar vazia. */
    public static ItemKey of(ItemStack stack) {
        if (stack.isEmpty()) {
            throw new IllegalArgumentException("ItemKey de pilha vazia");
        }
        return new ItemKey(stack.copyWithCount(1));
    }

    public Item item() {
        return prototype.getItem();
    }

    /** Pilha nova deste item com {@code count} unidades (quem chama respeita o máximo da pilha). */
    public ItemStack toStack(int count) {
        return prototype.copyWithCount(count);
    }

    /** Grava o item (sem quantidade) para o disco. */
    public Tag save(HolderLookup.Provider registries) {
        return prototype.save(registries);
    }

    /** Lê do disco; vazio se o item não existe mais (mod removido) ou qualquer parte do dado é inválida. */
    public static Optional<ItemKey> parse(HolderLookup.Provider registries, @Nullable Tag tag) {
        if (tag == null) {
            return Optional.empty();
        }
        // Estrito: qualquer erro (até um componente inválido) = ilegível. O parse comum do jogo aceitaria um resultado
        // parcial, descartando o que não entendeu, e o recurso voltaria diferente do que foi guardado.
        return ItemStack.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE), tag).result()
                .filter(s -> !s.isEmpty()).map(ItemKey::of);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ItemKey key && hash == key.hash
                && ItemStack.isSameItemSameComponents(prototype, key.prototype);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public @NotNull String toString() {
        return "ItemKey[" + prototype + "]";
    }
}
