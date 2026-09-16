package com.craisinlord.antarchy.content.computer.blockle;

public final class BlockleGame {
    private BlockleGame() {
    }

    public static String evaluate(String answer, String guess) {
        if (answer == null || guess == null || answer.length() != 5 || guess.length() != 5) return "XXXXX";
        char[] result = {'X', 'X', 'X', 'X', 'X'};
        int[] remaining = new int[26];
        for (int index = 0; index < 5; index++) {
            if (answer.charAt(index) == guess.charAt(index)) {
                result[index] = 'G';
            } else {
                remaining[answer.charAt(index) - 'a']++;
            }
        }
        for (int index = 0; index < 5; index++) {
            if (result[index] == 'G') {
                continue;
            }
            int letter = guess.charAt(index) - 'a';
            if (letter >= 0 && letter < remaining.length && remaining[letter] > 0) {
                result[index] = 'Y';
                remaining[letter]--;
            }
        }
        return new String(result);
    }
}
