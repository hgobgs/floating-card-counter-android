package com.example.pokertracker;

import java.util.Arrays;

public class CardStore {

    public static final String[] RANKS = {
            "A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"
    };

    public static final String[] SUITS = {"♠", "♥", "♦", "♣"};

    private static final int[] COUNTS = new int[52];

    public static int index(String rank, String suit) {
        int r = Arrays.asList(RANKS).indexOf(rank);
        int s = Arrays.asList(SUITS).indexOf(suit);
        if (r < 0 || s < 0) return -1;
        return r * 4 + s;
    }

    public static synchronized void add(String rank, String suit) {
        int i = index(rank, suit);
        if (i >= 0) COUNTS[i]++;
    }

    public static synchronized void removeLast(String rank, String suit) {
        int i = index(rank, suit);
        if (i >= 0 && COUNTS[i] > 0) COUNTS[i]--;
    }

    public static synchronized int count(String rank, String suit) {
        int i = index(rank, suit);
        return i >= 0 ? COUNTS[i] : 0;
    }

    public static synchronized int remaining(String rank, String suit) {
        return 4 - count(rank, suit);
    }

    public static synchronized void clear() {
        Arrays.fill(COUNTS, 0);
    }

    public static synchronized int totalSeen() {
        int sum = 0;
        for (int v : COUNTS) sum += v;
        return sum;
    }
}

