package org.tinycore.quantum.storage;

/**
 * Quantidades grandes em texto curto: 1234 → "1.2k", 3 400 000 → "3.4M", até "9.2E" (o maior {@code long}).
 * Usado em mensagens, telas e monitores. Corta em vez de arredondar, para nunca mostrar mais do que existe
 * (999 999 → "999k", não "1000k"). Sem dependência do Minecraft: roda no servidor e nos testes.
 */
public final class QuantityFormat {

    private static final String[] SUFFIXES = {"k", "M", "G", "T", "P", "E"};

    private QuantityFormat() {}

    public static String compact(long value) {
        if (value < 0) {
            // Long.MIN_VALUE não tem positivo; as quantidades do mod nunca são negativas, isto é só defesa.
            return value == Long.MIN_VALUE ? "-9.2E" : "-" + compact(-value);
        }
        if (value < 1_000) {
            return Long.toString(value);
        }
        long unit = 1_000;
        int index = 0;
        while (index < SUFFIXES.length - 1 && value / unit >= 1_000) {
            unit *= 1_000;
            index++;
        }
        long whole = value / unit;
        if (whole >= 100) {
            return whole + SUFFIXES[index];
        }
        long tenths = (value / (unit / 10)) % 10;
        return tenths == 0 ? whole + SUFFIXES[index] : whole + "." + tenths + SUFFIXES[index];
    }
}
