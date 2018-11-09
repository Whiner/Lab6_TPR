package org.donntu.tpr.lab6;

public class Main {
    public static void main(String[] args) throws Exception {
        BrawnMethod brawnMethod = new BrawnMethod();
        brawnMethod.addRowToPayMatrix(4, 3, 2, 6, 5);
        brawnMethod.addRowToPayMatrix(3, 3, 1, 4, 1);
        brawnMethod.addRowToPayMatrix(3, -2, 1, -5, 0);
        brawnMethod.addRowToPayMatrix(-4, 0, 6, 0, 5);
        brawnMethod.addRowToPayMatrix(2, 1, 5, 3, -1);
        brawnMethod.printPayTable();
        System.out.println();
        brawnMethod.deleteDominatedStrategies();
        brawnMethod.printPayTable();

        brawnMethod.play(100);

        brawnMethod.printGameTable();
    }
}
