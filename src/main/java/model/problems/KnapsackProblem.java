package model.problems;

import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@Setter
public class KnapsackProblem implements Problem {
    private List<Integer> itemWeight;
    private Integer weightOfBackpack;

    public void populateProblem(Integer numOfItems, Integer averageWeightOfItem, Integer weightOfBackpack) {
        this.weightOfBackpack = weightOfBackpack;
        this.itemWeight = new ArrayList<>();
        var rndm = new Random();
        for (int i = 0; i < numOfItems; i++) {
            var item = rndm.nextInt(2*averageWeightOfItem) + 1;
            this.itemWeight.add(item);
        }
    }

    public Integer sumOfItems(List<Integer> items) {
        var sum = 0;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i) == 1)
                sum += itemWeight.get(i);
        }
        return sum;
    }

    public List<Integer> makeOneIndividual() {
        var individual = new ArrayList<Integer>();
        var rndm = new Random();
        do {
            individual = new ArrayList<>();
            for (int i = 0; i < itemWeight.size(); i++) {
                individual.add(rndm.nextInt(2));
            }
        } while (sumOfItems(individual) > weightOfBackpack);
        return individual;
    }

    public Double fitness(List<Integer> individual) {
        return 1.0 / (sumOfItems(individual) * 1.0 / weightOfBackpack);
    }

    public List<Integer> mutate(List<Integer> individual) {
        var tmpIndividual = new ArrayList<>(individual);
        var rndm = new Random();
        do {
            individual = new ArrayList<>(tmpIndividual);
            var index = rndm.nextInt(this.itemWeight.size());
            individual.set(index, ((individual.get(index)) + 1) % 2);
        } while (sumOfItems(individual) > weightOfBackpack);
        return individual;
    }

    public Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2) {
        var rndm = new Random();
        var index = rndm.nextInt() % itemWeight.size();
        var child1 = new ArrayList<Integer>();
        var child2 = new ArrayList<Integer>();
        for (int i = 0; i < itemWeight.size(); i++) {
            if (i < index) {
                child1.add(parent1.get(i));
                child2.add(parent2.get(i));
            } else {
                child2.add(parent1.get(i));
                child1.add(parent2.get(i));
            }
        }
        return new Pair<>(child1, child2);
    }

    @Override
    public String nameForFaces() {
        return "Knapsack Problem";
    }

    @Override
    public String[] nameOfFxmlFiles() {
        var arr = new String[1];
        arr[0] = "KPPage.fxml";
        return arr;
    }
}
