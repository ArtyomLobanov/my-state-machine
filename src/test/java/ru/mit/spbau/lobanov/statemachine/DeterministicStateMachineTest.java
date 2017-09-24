package ru.mit.spbau.lobanov.statemachine;

import org.junit.Test;
import ru.mit.spbau.lobanov.statemachine.DeterministicStateMachine.State;

import static org.junit.Assert.*;

public class DeterministicStateMachineTest {
    @Test
    public void test() {
        Alphabet<Character> alphabet = new SimpleAlphabet("abcd01");
        DeterministicStateMachine<Character> dsm = new DeterministicStateMachine<>(alphabet);
        State initial = dsm.addState(false);
        dsm.setInitialState(initial);
        State readA = dsm.addState(true);
        State readB = dsm.addState(true);
        dsm.setEdge(initial, 'a', readA);
        dsm.setEdge(readA, 'b', readB);
        assertTrue(dsm.accept('a', 'b'));
        assertTrue(dsm.accept('a'));
        assertFalse(dsm.accept('c'));
    }
}