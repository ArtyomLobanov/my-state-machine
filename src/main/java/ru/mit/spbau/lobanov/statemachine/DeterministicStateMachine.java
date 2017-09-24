package ru.mit.spbau.lobanov.statemachine;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.*;

public class DeterministicStateMachine<L> implements StateMachine<L> {
    private final List<State> states = new ArrayList<>();
    private final Alphabet<L> alphabet;
    private State initialState;

    public DeterministicStateMachine(Alphabet<L> alphabet) {
        this.alphabet = alphabet;
    }

    public void setInitialState(@NotNull State state) {
        initialState = state;
    }

    public State addState(boolean isTerminal, @NotNull State defaultEdges, @NotNull String label) {
        final State state = new State(isTerminal, alphabet.size(), states.size(), label);
        states.add(state);
        Arrays.fill(state.edges, defaultEdges);
        return state;
    }

    public State addState(boolean isTerminal) {
        return addState(isTerminal, Integer.toString(states.size()));
    }

    public State addState(boolean isTerminal, @NotNull String label) {
        final State state = new State(isTerminal, alphabet.size(), states.size(), label);
        states.add(state);
        Arrays.fill(state.edges, state);
        return state;
    }

    public void setEdge(@NotNull State from, @NotNull L key, @NotNull State to) {
        from.edges[alphabet.getIndex(key)] = to;
    }

    @Override
    @SafeVarargs
    public final boolean accept(L... word) {
        State currentState = initialState;
        for (L symbol : word) {
            currentState = currentState.edges[alphabet.getIndex(symbol)];
        }
        return currentState.isTerminal;
    }

    @Override
    public void writeStateMachine(PrintWriter out) {
        out.println("digraph dsm {");
        for (State state : states) {
            out.print("    S" + state.id + " [label=\"" + state.label + "\"]");
            out.println("[shape=" + (state.isTerminal ? "double":"") + "circle];");
        }
        out.println("SPACE [color=white][label=\"\"];");
        for (State state : states) {
            final Map<State, String> labels = new HashMap<>();
            for (int s = 0; s < alphabet.size(); s++) {
                final State target = state.edges[s];
                final String prefix = labels.containsKey(target) ? labels.get(target) + "," : "";
                labels.put(target, prefix + alphabet.getSymbol(s));
            }
            for (Map.Entry<State, String> edge : labels.entrySet()) {
                out.print("    S" + state.id + " -> ");
                out.print("S" + edge.getKey().id);
                final String label = edge.getValue().length() > alphabet.size() ? "?" : edge.getValue();
                out.println(" [label = \"" + label + "\"];");
            }
        }
        if (initialState != null) {
            out.println("SPACE -> S" + initialState.id + ";");
        }
        out.println("}");
    }

    public DeterministicStateMachine<L> minimize() {
        final boolean[][] areDifferent = new boolean[states.size()][states.size()];
        final Queue<Pair<Integer>> queue = new ArrayDeque<>();
        @SuppressWarnings("unchecked")
        final List<Integer>[][] backEdges = new List[states.size()][alphabet.size()];
        for (int i = 0; i < states.size(); i++) {
            for (int s = 0; s < alphabet.size(); s++) {
                backEdges[i][s] = new ArrayList<>();
            }
        }
        for (int i = 0; i < states.size(); i++) {
            final State currentState = states.get(i);
            for (int s = 0; s < alphabet.size(); s++) {
                backEdges[currentState.edges[s].id][s].add(i);
            }
            for (int j = 0; j < i; j++) {
                if (currentState.isTerminal ^ states.get(j).isTerminal) {
                    areDifferent[i][j] = true;
                    areDifferent[j][i] = true;
                    queue.add(new Pair<>(i, j));
                }
            }
        }
        while (!queue.isEmpty()) {
            final Pair<Integer> pair = queue.poll();
            for (int s = 0; s < alphabet.size(); s++) {
                for (int a : backEdges[pair.first][s]) {
                    for (int b : backEdges[pair.second][s]) {
                        if (!areDifferent[a][b]) {
                            areDifferent[a][b] = true;
                            areDifferent[b][a] = true;
                            queue.add(new Pair<>(a, b));
                        }
                    }
                }
            }
        }
        int[] delegates = new int[states.size()];
        Arrays.fill(delegates, -1);
        int statesCount = 0;
        final DeterministicStateMachine<L> minDsm = new DeterministicStateMachine<>(alphabet);
        final List<State> newStates = new ArrayList<>();
        final List<State> templates = new ArrayList<>();
        for (State state : states) {
            if (delegates[state.id] != -1) {
                continue;
            }
            newStates.add(minDsm.addState(state.isTerminal));
            templates.add(state);
            for (int i = state.id; i < states.size(); i++) {
                if (!areDifferent[state.id][i]) {
                    delegates[i] = statesCount;
                }
            }
            statesCount++;
        }

        for (State newState : newStates) {
            final State template = templates.get(newState.id);
            for (int s = 0; s < alphabet.size(); s++) {
                final int nextID = delegates[template.edges[s].id];
                minDsm.setEdge(newState, alphabet.getSymbol(s), newStates.get(nextID));
            }
        }
        final int initialStateId = delegates[initialState.id];
        minDsm.setInitialState(newStates.get(initialStateId));
        return minDsm;
    }

    public DeterministicStateMachine<L> intersect(DeterministicStateMachine<L> another) {
        if (!alphabet.isIdentical(another.alphabet)) {
            throw new RuntimeException();
        }
        final DeterministicStateMachine<L> result = new DeterministicStateMachine<>(alphabet);
        final Map<Pair<Integer>, State> newStates = new HashMap<>();
        final Queue<Pair<Integer>> queue = new ArrayDeque<>();
        final Pair<Integer> start = new Pair<>(initialState.id, another.initialState.id);
        newStates.put(start, result.addState(initialState.isTerminal && another.initialState.isTerminal));
        queue.add(start);
        while (!queue.isEmpty()) {
            final Pair<Integer> pair = queue.poll();
            final State a = states.get(pair.first);
            final State b = another.states.get(pair.second);
            final State ab = newStates.get(pair);
            for (int s = 0; s < alphabet.size(); s++) {
                final Pair<Integer> next = new Pair<>(a.edges[s].id, b.edges[s].id);
                if (!newStates.containsKey(next)) {
                    newStates.put(next, result.addState(a.edges[s].isTerminal && b.edges[s].isTerminal));
                    queue.add(next);
                }
                ab.edges[s] = newStates.get(next);
            }
        }
        result.setInitialState(newStates.get(start));
        return result;
    }

    public DeterministicStateMachine<L> invert() {
        final DeterministicStateMachine<L> dsm = new DeterministicStateMachine<>(alphabet);
        final State[] dsmStates = new State[states.size()];
        states.forEach(s -> dsmStates[s.id] = dsm.addState(!s.isTerminal, s.label));
        for (State state : states) {
            for (int s = 0; s < state.edges.length; s++) {
                dsmStates[state.id].edges[s] = dsmStates[state.edges[s].id];
            }
        }
        dsm.setInitialState(dsmStates[initialState.id]);
        return dsm;
    }

    public static class State {
        private int id;
        @NotNull
        private String label;
        private final boolean isTerminal;
        private final State[] edges;

        private State(boolean isTerminal, int alphabetSize, int id, @NotNull String label) {
            this.id = id;
            this.isTerminal = isTerminal;
            this.label = label;
            edges = new State[alphabetSize];
        }
    }

    private static class Pair<T> {
        private final T first;
        private final T second;

        private Pair(T first, T second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public int hashCode() {
            return first.hashCode() + 1000_000_007 * second.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Pair<?> pair = (Pair<?>) o;
            return first.equals(pair.first) && second.equals(pair.second);
        }
    }
}
