package fcu.app.distributionapp.util;

import java.util.*;

public class SettleCalculator {

    public static List<String> calculateInstructions(Map<String, Double> balances, String currencyCode) {
        List<String> instructions = new ArrayList<>();

        PriorityQueue<PersonBalance> debtors = new PriorityQueue<>((a, b) -> Double.compare(a.amount, b.amount));
        PriorityQueue<PersonBalance> creditors = new PriorityQueue<>((a, b) -> Double.compare(b.amount, a.amount));

        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            String person = entry.getKey();
            double amount = entry.getValue();

            if (amount > 0.01) {
                creditors.add(new PersonBalance(person, amount));
            } else if (amount < -0.01) {
                debtors.add(new PersonBalance(person, -amount)); // 正數化方便操作
            }
        }

        while (!debtors.isEmpty() && !creditors.isEmpty()) {
            PersonBalance debtor = debtors.poll();
            PersonBalance creditor = creditors.poll();

            double amountToSettle = Math.min(debtor.amount, creditor.amount);

            instructions.add(String.format(Locale.getDefault(),
                    "%s 要付 %.2f %s 給 %s",
                    debtor.name, amountToSettle, currencyCode, creditor.name));

            if (debtor.amount > amountToSettle) {
                debtor.amount -= amountToSettle;
                debtors.add(debtor);
            }

            if (creditor.amount > amountToSettle) {
                creditor.amount -= amountToSettle;
                creditors.add(creditor);
            }
        }

        return instructions;
    }

    static class PersonBalance {
        String name;
        double amount;

        PersonBalance(String name, double amount) {
            this.name = name;
            this.amount = amount;
        }
    }
}
