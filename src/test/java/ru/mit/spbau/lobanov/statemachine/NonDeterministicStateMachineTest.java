package ru.mit.spbau.lobanov.statemachine;

import org.junit.Test;

import static org.junit.Assert.*;

public class NonDeterministicStateMachineTest {
    @Test
    public void test() {
        Alphabet<Character> alphabet = new SimpleAlphabet("abcd01");
        NonDeterministicStateMachine<Character> dsm = new NonDeterministicStateMachine<>(alphabet);
        NonDeterministicStateMachine.State initial = dsm.addState(false);
        dsm.setInitialState(initial);
        NonDeterministicStateMachine.State readA1 = dsm.addState(true);
        NonDeterministicStateMachine.State readA2 = dsm.addState(true);
        NonDeterministicStateMachine.State readAB = dsm.addState(true);
        NonDeterministicStateMachine.State readAA = dsm.addState(true);
        dsm.addEdge(initial, 'a', readA1);
        dsm.addEdge(initial, 'a', readA2);
        dsm.addEdge(readA1, 'b', readAB);
        dsm.addEdge(readA2, 'a', readAA);
        assertTrue(dsm.accept('a', 'b'));
        assertTrue(dsm.accept('a', 'a'));
        assertFalse(dsm.accept('c'));
    }
}