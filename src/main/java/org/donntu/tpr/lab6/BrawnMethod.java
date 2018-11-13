package org.donntu.tpr.lab6;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import java.util.*;

public class BrawnMethod {
    private Table<String, String, Integer> payMatrix = HashBasedTable.create();
    private PlayerTarget aTarget = PlayerTarget.MAX_PRIZE;
    private PlayerTarget bTarget = PlayerTarget.MIN_LOSSES;
    private Table<Integer, String, Double> gameTable = HashBasedTable.create();
    private List<Double> aResult = new ArrayList<Double>();
    private List<Double> bResult = new ArrayList<Double>();
    private int currentTurn;
    private int currIteration;
    private int tourCount;

    private Map<String, Integer> aLastStrategy = new HashMap<String, Integer>();
    private Map<String, Integer> bLastStrategy = new HashMap<String, Integer>();

    public void revertGamerTargets() {
        aTarget = PlayerTarget.MIN_LOSSES;
        bTarget = PlayerTarget.MAX_PRIZE;
    }

    public int getAlpha() {
        List<Integer> minList = new ArrayList<Integer>();
        for (String rowKey : payMatrix.rowKeySet()) {
            Map<String, Integer> row = payMatrix.row(rowKey);
            minList.add(Collections.min(row.values()));
        }
        return Collections.max(minList);
    }

    public int getBeta() {
        List<Integer> maxList = new ArrayList<Integer>();
        for (String columnKey : payMatrix.columnKeySet()) {
            Map<String, Integer> column = payMatrix.column(columnKey);
            maxList.add(Collections.max(column.values()));
        }
        return Collections.min(maxList);
    }

    public List<Double> getaResult() {
        return convertToProbability(aResult);
    }

    public List<Double> getbResult() {
        return convertToProbability(bResult);
    }

    public void printaResult() {
        printResult("A");
    }

    public void printbResult() {
        printResult("B");
    }

    private void printResult(String player) {
        List<Double> probability;
        if ("A".equals(player)) {
            probability = getaResult();
        } else if ("B".equals(player)) {
            probability = getbResult();
        } else {
            System.out.println("Неизвестный игрок " + player);
            return;
        }
        System.out.println("Частота для " + player);
        for (int i = 0; i < probability.size(); i++) {
            System.out.println(player + (i + 1) + " = " + probability.get(i));
        }
    }

    private List<Double> convertToProbability(List<Double> result) {
        List<Double> probability = new ArrayList<Double>();
        for (Double res : result) {
            probability.add(res / (double) tourCount);
        }
        return probability;
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

    public void printPayTable() {
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

    public void printGameTable() {
        Set<String> columnMap = gameTable.columnKeySet();
        System.out.printf("|%8s", "n");
        for (String s : columnMap) {
            System.out.printf("|%8s", s);
        }
        System.out.println("|");

        for (Integer rowKey : gameTable.rowKeySet()) {
            int i = 0;
            System.out.printf("|%8d", rowKey);
            for (Double value : gameTable.row(rowKey).values()) {
                if (i != 0 && i != 6) {
                    System.out.printf("|%8.2f", value);
                } else {
                    System.out.printf("|%8.0f", value);
                }
                i++;
            }
            System.out.println("|");
        }
    }

    private void fillListToZero(List<Double> list, int size) {
        for (int i = 0; i < size; i++) {
            list.add(0.0);
        }
    }

    public void play(int tourCount) throws Exception {
        fillListToZero(aResult, payMatrix.rowKeySet().size());
        fillListToZero(bResult, payMatrix.columnKeySet().size());
        deleteDominatedStrategies();
        printPayTable();
        String aStrategy = getFirstStrategy();
        currentTurn = 0;
        currIteration = 1;
        this.tourCount = tourCount;
        String bestStrategy = aStrategy;
        for (int i = 0; i < tourCount * 2; i++) {
            bestStrategy = turn(bestStrategy);
        }
    }

    private String turn(String strategy) {
        String bestStrategy;
        if (currentTurn % 2 == 0) {
            addAToGameTable(strategy);
            Map<String, Integer> row;
            if (currentTurn == 0) {
                row = payMatrix.row(strategy);
            } else {
                row = aLastStrategy;
            }
            bestStrategy = getBestStrategy(row, bTarget);
        } else {
            addBToGameTable(strategy);
            Map<String, Integer> column;
            if (currentTurn == 1) {
                column = payMatrix.column(strategy);
            } else {
                column = bLastStrategy;
            }
            bestStrategy = getBestStrategy(column, aTarget);
            addVToGameTableByLastIteration();
            currIteration++;
        }
        currentTurn++;
        return bestStrategy;
    }

    private void addAToGameTable(String aStrategy) {
        String strategyNumber = aStrategy.substring(1);
        gameTable.put(currIteration, "i", Double.valueOf(strategyNumber)); // номер стратегии А
        aResult.set(Integer.parseInt(strategyNumber) - 1, aResult.get(Integer.parseInt(strategyNumber) - 1) + 1);

        Map<String, Integer> row = payMatrix.row(aStrategy);
        List<Integer> lastStrategyPay = new ArrayList<Integer>();
        for (Map.Entry<String, Integer> entry : row.entrySet()) { //стратегии B
            int e = fillStrategy(entry);//
            lastStrategyPay.add(e);
            aLastStrategy.put(entry.getKey(), e);
        }
        Integer min = Collections.min(lastStrategyPay);
        double v_min = (double) min / currIteration;
        gameTable.put(currIteration, "v_min(n)", v_min); // v min
    }

    private void addBToGameTable(String bStrategy) {
        String strategyNumber = bStrategy.substring(1);
        gameTable.put(currIteration, "j", Double.valueOf(strategyNumber)); // номер стратегии В
        bResult.set(Integer.parseInt(strategyNumber) - 1, bResult.get(Integer.parseInt(strategyNumber) - 1) + 1);

        Map<String, Integer> column = payMatrix.column(bStrategy);
        List<Integer> lastStrategyPay = new ArrayList<Integer>();
        for (Map.Entry<String, Integer> entry : column.entrySet()) { //стратегии B
            int e = fillStrategy(entry);
            lastStrategyPay.add(e);
            bLastStrategy.put(entry.getKey(), e);
        }

        Integer max = Collections.max(lastStrategyPay);
        double v_max = (double) max / currIteration;
        gameTable.put(currIteration, "v_max(n)", v_max); // v max
    }

    private void addVToGameTableByLastIteration() {
        double v_max = gameTable.row(currIteration).get("v_max(n)");
        double v_min = gameTable.row(currIteration).get("v_min(n)");
        gameTable.put(currIteration, "v(n)", (v_max + v_min) / 2); // v(n)
    }


    private int fillStrategy(Map.Entry<String, Integer> entry) {
        int value;
        if (currIteration == 1) {
            value = entry.getValue();
            gameTable.put(currIteration, entry.getKey(), (double) value);
        } else {
            value = (int) (entry.getValue() + gameTable.get(currIteration - 1, entry.getKey()));
            gameTable.put(
                    currIteration,
                    entry.getKey(),
                    (double) value);
        }
        return value;
    }

    private String getFirstStrategy() {
        Set<Map.Entry<String, Map<String, Integer>>> rows = payMatrix.rowMap().entrySet();
        List<Integer> bestOutcomes = new ArrayList<Integer>();
        for (Map.Entry<String, Map<String, Integer>> row : rows) {
            Collection<Integer> values = row.getValue().values();
            bestOutcomes.add(Collections.min(values));
        }
        Integer max = Collections.max(bestOutcomes);
        int strategyNumber = bestOutcomes.indexOf(max) + 1;
        return "A" + strategyNumber;
    }

    private String getBestStrategy(Map<String, Integer> enemyStrategy, PlayerTarget playerTarget) {
        Integer[] integers = ArrayEditor.castToInteger(enemyStrategy.values().toArray());
        int bestOutcome = integers[0];
        Iterator<String> iterator = enemyStrategy.keySet().iterator();
        String bestStrategy = enemyStrategy.keySet().iterator().next();
        String currStrategy;
        for (Integer integer : integers) {
            currStrategy = iterator.next();
            if (playerTarget.equals(PlayerTarget.MAX_PRIZE)) {
                if (integer > bestOutcome) {
                    bestOutcome = integer;
                    bestStrategy = currStrategy;
                }
            } else {
                if (integer < bestOutcome) {
                    bestOutcome = integer;
                    bestStrategy = currStrategy;
                }
            }

        }
        return bestStrategy;
    }


    private void deleteDominatedStrategies() throws Exception {
        boolean removed;
        do {
            removed = deleteDominatedRows(aTarget);
            if (removed) {
                removed = deleteDominatedColumns(bTarget);
            }
        } while (removed);

    }

    private boolean deleteDominatedColumns(PlayerTarget target) throws Exception {
        List<String> columnKeyArray = ArrayEditor.castToStringList(payMatrix.columnKeySet().toArray());
        boolean deleted = false;
        for (int i = 0; i < columnKeyArray.size(); i++) {
            Integer[] comparedValues = ArrayEditor.castToInteger(payMatrix.column(columnKeyArray.get(i)).values().toArray());
            for (int j = i + 1; j < columnKeyArray.size(); j++) {
                Integer[] comparedWithValues = ArrayEditor.castToInteger(payMatrix.column(columnKeyArray.get(j)).values().toArray());
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
        List<String> rowKeyArray = ArrayEditor.castToStringList(payMatrix.rowKeySet().toArray());
        boolean deleted = false;
        for (int i = 0; i < rowKeyArray.size(); i++) {
            Integer[] comparedValues = ArrayEditor.castToInteger(payMatrix.row(rowKeyArray.get(i)).values().toArray());
            for (int j = i + 1; j < rowKeyArray.size(); j++) {
                Integer[] comparedWithValues = ArrayEditor.castToInteger(payMatrix.row(rowKeyArray.get(j)).values().toArray());
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
