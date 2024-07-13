package de.uni_passau.fim.se2.sa.examples;

public class ExtendedExample {
    public static void main(final String[] args) {
        int sum = 0;
        for (int i = 1; i < 10; i++) {
            sum = sum + i;
        }

        while (sum > 0) {
            if (sum % 2 == 0) {
                sum = sum / 2;
            } else {
                sum = sum + 1;
            }
        }

        switch (sum) {
            case 0:
                System.out.println("zero");
            case 1: {
                final String one = "one";
                System.out.println(one);
            }
            default:
                System.out.println("DEFAULT");
        }

        if (args.length == 0) {
            return;
        }

        int i = 0;
        do {
            final String arg = args[i];
            System.out.println(arg);
        } while (++i < args.length);
    }
}
