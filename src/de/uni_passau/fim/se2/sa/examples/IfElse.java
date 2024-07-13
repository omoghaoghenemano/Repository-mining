package de.uni_passau.fim.se2.sa.examples;

public class IfElse {

    int ifElse(int x, int y) {
        String temp = "idk";
        if (x > y) {
            return x;
        } else if (x == y) {
            return y;
        } else {
            System.out.println(temp);
        }
        return x % y;
    }
}