package ru.mit.spbau.lobanov.statemachine;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class NonDeterministicStateMachine<L> implements StateMachine<L> {
    private final List<State> states = new ArrayList<>();
    private final Alphabet<L> alphabet;
    private State initialState;

    public NonDeterministicStateMachine(Alphabet<L> alphabet) {
        this.alphabet = alphabet;
    }

    public void setInitialState(@NotNull State state) {
        initialState = state;
    }

    public State addState(boolean isTerminal, @NotNull String label) {
        final State state = new State(isTerminal, alphabet.size(), states.size(), label);
        states.add(state);
        return state;
    }

    public State addState(boolean isTerminal) {
        return addState(isTerminal, Integer.toString(states.size()));
    }

    public void addEdge(@NotNull State from, @NotNull L key, @NotNull State to) {
        from.edges[alphabet.getIndex(key)].set(to.id);
    }

    @Override
    @SafeVarargs
    public final boolean accept(L... word) {
        BitSet possibleStates = new BitSet();
        possibleStates.set(initialState.id);
        for (L symbol : word) {
            final int symbolId = alphabet.getIndex(symbol);
            final BitSet nextStates = new BitSet();
            possibleStates.stream()
                    .mapToObj(states::get)
                    .map(state -> state.edges[symbolId])
                    .forEach(nextStates::or);
            possibleStates = nextStates;
        }
        return possibleStates.stream()
                .mapToObj(states::get)
                .anyMatch(state -> state.isTerminal);
    }

    public DeterministicStateMachine<L> determine() {
        final DeterministicStateMachine<L> dsm = new DeterministicStateMachine<>(alphabet);
        final HashMap<BitSet, DeterministicStateMachine.State> newStates = new HashMap<>();
        final Queue<BitSet> queue = new ArrayDeque<>();
        final BitSet initialStateMask = new BitSet();
        initialStateMask.set(initialState.id);
        queue.add(initialStateMask);
        newStates.put(initialStateMask, dsm.addState(initialState.isTerminal));
        while (!queue.isEmpty()) {
            final BitSet mask = queue.poll();
            final DeterministicStateMachine.State state = newStates.get(mask);
            for (int s = 0; s < alphabet.size(); s++) {
                final int localS = s;
                final BitSet nextStates = new BitSet();
                mask.stream()
                        .mapToObj(states::get)
                        .map(st -> st.edges[localS])
                        .forEach(nextStates::or);
                final DeterministicStateMachine.State next;

                if (newStates.containsKey(nextStates)) {
                    next = newStates.get(nextStates);
                } else {
                    next = dsm.addState(containsTerminal(nextStates));
                    newStates.put(nextStates, next);
                    queue.add(nextStates);
                }
                dsm.setEdge(state, alphabet.getSymbol(s), next);
            }
        }
        dsm.setInitialState(newStates.get(initialStateMask));
        return dsm;
    }

    @Override
    public void writeStateMachine(PrintWriter out) {
        out.println("digraph dsm {");
        for (State state : states) {
            out.print("    S" + state.id + " [label=\"" + state.label + "\"]");
            out.println("[shape=" + (state.isTerminal ? "double" : "") + "circle];");
        }
        out.println("SPACE [color=white][label=\"\"];");
        for (State state : states) {
            final Map<State, String> labels = new HashMap<>();
            for (int s = 0; s < alphabet.size(); s++) {
                final L symbol = alphabet.getSymbol(s);
                state.edges[s].stream()
                        .forEach(i -> {
                            final State target = states.get(i);
                            final String prefix = labels.containsKey(target) ? labels.get(target) + "," : "";
                            labels.put(target, prefix + symbol);
                        });
            }
            for (Map.Entry<State, String> edge : labels.entrySet()) {
                out.print("    S" + state.id + " -> ");
                out.print("S" + edge.getKey().id);
                out.println(" [label = \"" + edge.getValue() + "\"];");
            }
        }
        if (initialState != null) {
            out.println("SPACE -> S" + initialState.id + ";");
        }
        out.println("}");
    }

    public State getInitialState() {
        return initialState;
    }

    private boolean containsTerminal(BitSet bitSet) {
        return bitSet.stream()
                .mapToObj(states::get)
                .anyMatch(state -> state.isTerminal);
    }

    public static class State {
        private int id;
        @NotNull
        private String label;
        private final boolean isTerminal;
        private final BitSet[] edges;

        private State(boolean isTerminal, int alphabetSize, int id, @NotNull String label) {
            this.id = id;
            this.isTerminal = isTerminal;
            this.label = label;
            edges = new BitSet[alphabetSize];
            Arrays.setAll(edges, i -> new BitSet());
        }
    }
}
