package ru.mit.spbau.lobanov.statemachine;

import java.io.OutputStream;
import java.io.PrintWriter;

public interface StateMachine<L> {
    void writeStateMachine(PrintWriter out);
    @SuppressWarnings("unchecked")
    boolean accept(L... word);
}
