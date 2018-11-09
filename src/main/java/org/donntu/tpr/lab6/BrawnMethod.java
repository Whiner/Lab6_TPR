package org.donntu.tpr.lab6;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.List;
import java.util.Set;

public class BrawnMethod {
    private Table<String, String, Integer> payMatrix = HashBasedTable.create();
    private PlayerTarget aTarget = PlayerTarget.MAX_PRIZE;
    private PlayerTarget bTarget = PlayerTarget.MIN_LOSSES;
    private Table<String, Integer, Double> gameMatrix = HashBasedTable.create();

    public void revertGamerTargets() {
        aTarget = PlayerTarget.MIN_LOSSES;
        bTarget = PlayerTarget.MAX_PRIZE;
    }

    public void addToPayMatrix(int row, int column, int value) {
        payMatrix.put("A" + row, "B" + column, value);
    }

    public void addRowToPayMatrix(int... values) {
        int row_index = payMatrix.rowKeySet().size() + 1;
        for (int i = 0; i < values.length; i++) {
            addToPayMatrix(row_index, i + 1, values[i]);
        }
    }

    public void printPayMatrix() {
        Set<String> columnMap = payMatrix.columnKeySet();
        System.out.printf("|%5s", "");
        for (String s : columnMap) {
            System.out.printf("|%5s", s);
        }
        System.out.println("|");

        for (String rowKey : payMatrix.rowKeySet()) {
            System.out.printf("|%5s", rowKey);
            for (Integer value : payMatrix.row(rowKey).values()) {
                System.out.printf("|%5s", value);
            }
            System.out.println("|");
        }
    }


    public void play(int tourCount, int firstPlayer) {

    }


    public void deleteDominatedStrategies() throws Exception {
        boolean removed;
        do {
            removed = deleteDominatedRows(aTarget);
            if (removed) {
                removed = deleteDominatedColumns(bTarget);
            }
        } while (removed);

    }

    private boolean deleteDominatedColumns(PlayerTarget target) throws Exception {
        List<String> columnKeyArray = Cast.castToStringList(payMatrix.columnKeySet().toArray());
        boolean deleted = false;
        for (int i = 0; i < columnKeyArray.size(); i++) {
            Integer[] comparedValues = Cast.castToInteger(payMatrix.column(columnKeyArray.get(i)).values().toArray());
            for (int j = i + 1; j < columnKeyArray.size(); j++) {
                Integer[] comparedWithValues = Cast.castToInteger(payMatrix.column(columnKeyArray.get(j)).values().toArray());
                String dominated = removeDominatedFromPair(
                        target,
                        comparedValues,
                        comparedWithValues,
                        columnKeyArray.get(i),
                        columnKeyArray.get(j)
                );
                if (dominated != null) {
                    payMatrix.column(dominated).clear();
                    columnKeyArray.remove(dominated);
                    i = -1;
                    deleted = true;
                    break;
                }
            }
        }
        return deleted;
    }

    private boolean deleteDominatedRows(PlayerTarget target) throws Exception {
        List<String> rowKeyArray = Cast.castToStringList(payMatrix.rowKeySet().toArray());
        boolean deleted = false;
        for (int i = 0; i < rowKeyArray.size(); i++) {
            Integer[] comparedValues = Cast.castToInteger(payMatrix.row(rowKeyArray.get(i)).values().toArray());
            for (int j = i + 1; j < rowKeyArray.size(); j++) {
                Integer[] comparedWithValues = Cast.castToInteger(payMatrix.row(rowKeyArray.get(j)).values().toArray());
                String dominated = removeDominatedFromPair(
                        target,
                        comparedValues,
                        comparedWithValues,
                        rowKeyArray.get(i),
                        rowKeyArray.get(j)
                );
                if (dominated != null) {
                    payMatrix.row(dominated).clear();
                    rowKeyArray.remove(dominated);
                    i = -1;
                    deleted = true;
                    break;
                }
            }
        }
        return deleted;
    }

    private String removeDominatedFromPair(
            PlayerTarget target,
            Integer[] comparedValues,
            Integer[] comparedWithValues,
            String keyV,
            String keyV1) throws Exception {
        boolean vDominatesV1;
        boolean v1DominatesV;
        String dominated = null;
        if (target.equals(PlayerTarget.MAX_PRIZE)) {
            vDominatesV1 = isVDominatesV1(comparedWithValues, comparedValues);
            v1DominatesV = isVDominatesV1(comparedValues, comparedWithValues);
        } else {
            vDominatesV1 = isVDominatesV1(comparedValues, comparedWithValues);
            v1DominatesV = isVDominatesV1(comparedWithValues, comparedValues);
        }
        if (vDominatesV1) {
            dominated = keyV;
        }
        if (v1DominatesV) {
            dominated = keyV1;
        }
        return dominated;
    }

    private boolean isVDominatesV1(Integer[] values, Integer[] values1) throws Exception {
        if (values.length != values1.length) {
            throw new Exception("Столбцы должны быть однаковыми по размеру");
        }
        for (int i = 0; i < values.length; i++) {
            if (values[i] < values1[i]) {
                return false;
            }
        }
        return true;
    }
    
    public PlayerTarget getATarget() {
        return aTarget;
    }

    public PlayerTarget getBTarget() {
        return bTarget;
    }
}
