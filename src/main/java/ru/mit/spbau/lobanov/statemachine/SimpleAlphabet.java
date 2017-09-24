package ru.mit.spbau.lobanov.statemachine;

public class SimpleAlphabet extends Alphabet<Character> {

    private final String alphabet;

    public SimpleAlphabet(String alphabet) {
        final long distinctSymbolsCount = alphabet.chars()
                .distinct()
                .count();
        if (distinctSymbolsCount != alphabet.length()) {
            throw new RuntimeException();
        }
        this.alphabet = alphabet;
    }

    @Override
    public Character getSymbol(int index) {
        return (char) alphabet.charAt(index);
    }

    @Override
    public int getIndex(Character symbol) {
        return alphabet.indexOf(symbol);
    }

    @Override
    public int size() {
        return alphabet.length();
    }
}
