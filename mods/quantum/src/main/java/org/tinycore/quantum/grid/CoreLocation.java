package org.tinycore.quantum.grid;

/**
 * Onde está o Núcleo de uma rede: dimensão ({@code "minecraft:overworld"}) e posição empacotada num {@code long}
 * ({@code BlockPos.asLong()}). Fica sem tipos do Minecraft para o {@link NetworkDirectory} ser testável.
 */
public record CoreLocation(String dimension, long pos) {
}
