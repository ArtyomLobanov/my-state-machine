package ru.mit.spbau.lobanov.statemachine;

public abstract class Alphabet<L> {
    abstract L getSymbol(int index);
    abstract int getIndex(L symbol);
    abstract int size();

    public final boolean isIdentical(Alphabet<L> another) {
        if (another == this) {
            return true;
        }
        if (another.size() != another.size()) {
            return false;
        }
        for (int i = 0; i < size(); i++) {
            if (!another.getSymbol(i).equals(getSymbol(i))) {
                return false;
            }
        }
        return true;
    }
}
