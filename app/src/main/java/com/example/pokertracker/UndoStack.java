package com.example.pokertracker;

import java.util.ArrayDeque;
import java.util.Deque;

public class UndoStack {

    private static final Deque<Move> STACK = new ArrayDeque<>();

    public static synchronized void push(String rank, String suit, int player) {
        STACK.push(new Move(rank, suit, player));
    }

    public static synchronized Move pop() {
        return STACK.poll();
    }

    public static synchronized void clear() {
        STACK.clear();
    }

    public static synchronized int size() {
        return STACK.size();
    }

    public static class Move {
        public final String rank;
        public final String suit;
        public final int player; // 0..3, or -1 for board/common

        public Move(String rank, String suit, int player) {
            this.rank = rank;
            this.suit = suit;
            this.player = player;
        }
    }
}
