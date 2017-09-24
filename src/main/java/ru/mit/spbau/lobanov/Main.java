package ru.mit.spbau.lobanov;

import ru.mit.spbau.lobanov.statemachine.*;

import java.io.PrintWriter;

public class Main {
    public static void main(String[] args) {
        final PrintWriter out = new PrintWriter(System.out);
        final String letters = "abcdefghijklmnopqrstuvwxyz";
        final String digits = "0123456789";
        final Alphabet<Character> alphabet = new SimpleAlphabet(letters + digits + "_");

        out.println("//Machine, which accept all identifiers");
        final DeterministicStateMachine<Character> allIdentifiers = new DeterministicStateMachine<>(alphabet);
        {
            final DeterministicStateMachine.State drain = allIdentifiers.addState(false, "2");
            final DeterministicStateMachine.State accepted = allIdentifiers.addState(true, "1");
            final DeterministicStateMachine.State initial = allIdentifiers.addState(false, accepted, "0");
            for (char c : digits.toCharArray()) {
                allIdentifiers.setEdge(initial, c, drain);
            }
            allIdentifiers.setInitialState(initial);
            allIdentifiers.writeStateMachine(out);
            out.println("--------------------------------------\n\n");
            testIdentifiers(allIdentifiers);
        }

        out.println("//Non deterministic machine, which accept all key words");
        final NonDeterministicStateMachine<Character> keyWords = new NonDeterministicStateMachine<>(alphabet);
        {
            keyWords.setInitialState(keyWords.addState(false, "S"));
            insertWord("if", keyWords);
            insertWord("then", keyWords);
            insertWord("else", keyWords);
            insertWord("let", keyWords);
            insertWord("in", keyWords);
            insertWord("true", keyWords);
            insertWord("false", keyWords);
            keyWords.writeStateMachine(out);
            out.println("--------------------------------------\n\n");
            testKeyWords(keyWords, true);
        }

        out.println("//Deterministic machine, which accept all key words");
        final DeterministicStateMachine<Character> keyWords2 = keyWords.determine();
        {
            keyWords2.writeStateMachine(out);
            out.println("--------------------------------------\n\n");
            testKeyWords(keyWords2, true);
        }

        out.println("//Deterministic machine, which accept all words except key words");
        final DeterministicStateMachine<Character> notKeyWords = keyWords2.invert();
        {
            notKeyWords.writeStateMachine(out);
            out.println("--------------------------------------\n\n");
            testKeyWords(notKeyWords, false);
        }

        out.println("//Deterministic machine, which accept all correct identifiers");
        final DeterministicStateMachine<Character> correctIdentifiers = notKeyWords.intersect(allIdentifiers);
        {
            correctIdentifiers.writeStateMachine(out);
            out.println("--------------------------------------\n\n");
            testKeyWords(correctIdentifiers, false);
            testIdentifiers(correctIdentifiers);
        }

        out.println("//Optimal deterministic machine, which accept all correct identifiers");
        final DeterministicStateMachine<Character> minCorrectIdentifiers = correctIdentifiers.minimize();
        {
            minCorrectIdentifiers.writeStateMachine(out);
            out.println("--------------------------------------\n\n");
            testKeyWords(minCorrectIdentifiers, false);
            testIdentifiers(minCorrectIdentifiers);
        }

        out.close();
    }

    private static void insertWord(String word, NonDeterministicStateMachine<Character> sm) {
        final NonDeterministicStateMachine.State initial = sm.getInitialState();
        NonDeterministicStateMachine.State current = initial;
        for (int i = 0; i < word.length(); i++) {
            final NonDeterministicStateMachine.State next = sm.addState(i == word.length() - 1);
            sm.addEdge(current, word.charAt(i), next);
            current = next;
        }
    }

    private static void testKeyWords(StateMachine<Character> sm, boolean expected) {
        assert sm.accept('i', 'f') == expected;
        assert sm.accept('t', 'h', 'e', 'n') == expected;
        assert sm.accept('e', 'l', 's', 'e') == expected;
        assert sm.accept('l', 'e', 't') == expected;
        assert sm.accept('i', 'n') == expected;
        assert sm.accept('t', 'r', 'u', 'e') == expected;
        assert sm.accept('f', 'a', 'l', 's', 'e') == expected;
    }

    private static void testIdentifiers(StateMachine<Character> sm) {
        assert sm.accept('a');
        assert sm.accept('a', 'b');
        assert sm.accept('_', 'a', 'b');
        assert sm.accept('c', '_', 'z');
        assert sm.accept('_', '_', '1', '3');
        assert sm.accept('e', '2', 'e', '3');
        assert sm.accept('i', 'd', '_', '3');
        assert sm.accept('a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p',
                'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z');

        assert !sm.accept();
        assert !sm.accept('1');
        assert !sm.accept('1', '2');
        assert !sm.accept('1', '_', 'z');
        assert !sm.accept('8', 'q', 'z');
        assert !sm.accept('2');
        assert !sm.accept('3');
        assert !sm.accept('4');
        assert !sm.accept('5');
        assert !sm.accept('6');
        assert !sm.accept('7');
        assert !sm.accept('8');
        assert !sm.accept('9');
        assert !sm.accept('0');
        assert !sm.accept('0');
    }
}
