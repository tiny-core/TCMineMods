package org.tinycore.quantum.grid;

/**
 * Um bloco entrelaçado com a rede: onde está, o que é e quanto soma de capacidade (itens, para a Matriz).
 *
 * <p>Fica guardado na rede, não só no bloco: a capacidade de uma Matriz continua a contar com o chunk dela
 * descarregado (plano, §2.1). A capacidade é gravada no momento em que o bloco entra ou muda (módulos, fatia 1.4).
 */
public record NetworkMember(BlockLocation location, MemberKind kind, long capacity) {
}
