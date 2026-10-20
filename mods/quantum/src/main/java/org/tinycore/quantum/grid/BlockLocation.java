package org.tinycore.quantum.grid;

/**
 * Posição de um bloco da rede (Núcleo ou membro): dimensão ({@code "minecraft:overworld"}) e posição empacotada
 * num {@code long} ({@code BlockPos.asLong()}). Fica sem tipos do Minecraft para as regras de {@code grid} serem
 * testáveis sem o jogo.
 */
public record BlockLocation(String dimension, long pos) {
}
