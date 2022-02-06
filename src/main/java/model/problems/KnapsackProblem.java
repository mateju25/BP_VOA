package model.problems;

import controllers.base.BaseController;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.util.Pair;
import lombok.Getter;
import lombok.Setter;
import model.utils.AlgorithmResults;
import model.utils.DistinctColors;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class KnapsackProblem implements Problem {
    private List<Integer> itemWeight;
    private Integer weightOfBackpack;
    private List<Color> colorsOfItems;

    public void populateProblem(Integer numOfItems, Integer averageWeightOfItem, Integer weightOfBackpack) {
        this.weightOfBackpack = weightOfBackpack;
        this.itemWeight = new ArrayList<>();
        this.colorsOfItems = new ArrayList<>();
        for (int i = 0; i < numOfItems; i++) {
            var item = BaseController.rndm.nextInt(2 * averageWeightOfItem) + 1;
            this.itemWeight.add(item);
        }
        for (int i = 0; i < numOfItems; i++) {
            var item = Color.web(DistinctColors.colors[BaseController.rndm.nextInt(DistinctColors.colors.length)]);
            this.colorsOfItems.add(item);
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
        do {
            individual = new ArrayList<>();
            for (int i = 0; i < itemWeight.size(); i++) {
                individual.add(BaseController.rndm.nextInt(3)%2);
            }
        } while (sumOfItems(individual) > weightOfBackpack || sumOfItems(individual) == 0);
        return individual;
    }

    public Double fitness(List<Integer> individual) {
        return 1.0 / (sumOfItems(individual) * 1.0 / weightOfBackpack);
    }

    public List<Integer> mutate(List<Integer> individual) {
        var tmpIndividual = new ArrayList<>(individual);
        do {
            individual = new ArrayList<>(tmpIndividual);
            var index = BaseController.rndm.nextInt(this.itemWeight.size());
            individual.set(index, ((individual.get(index)) + 1) % 2);
        } while (sumOfItems(individual) > weightOfBackpack || sumOfItems(individual) == 0);
        return individual;
    }

    public Pair<List<Integer>, List<Integer>> simpleCrossover(List<Integer> parent1, List<Integer> parent2) {
        var child1 = new ArrayList<Integer>();
        var child2 = new ArrayList<Integer>();
        do {
            var index = BaseController.rndm.nextInt(itemWeight.size());
            child1 = new ArrayList<>();
            child2 = new ArrayList<>();
            for (int i = 0; i < itemWeight.size(); i++) {
                if (i < index) {
                    child1.add(parent1.get(i));
                    child2.add(parent2.get(i));
                } else {
                    child2.add(parent1.get(i));
                    child1.add(parent2.get(i));
                }
            }
        } while ((sumOfItems(child1) > weightOfBackpack) || (sumOfItems(child2) > weightOfBackpack || sumOfItems(child1) == 0 || sumOfItems(child2) == 0));
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

    @Override
    public void visualize(Canvas canvas, AlgorithmResults data) {
        if (data != null) {
            var gc = canvas.getGraphicsContext2D();
            gc.setFill(Color.web("#F7EDE2"));
            gc.fillRect(-5, -5, canvas.getWidth() + 5, canvas.getHeight() + 5);
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(5);
            var HEIGHT_OF_CONTAINER = 445;
            gc.strokeLine(350, 73, 350, 500);
            gc.strokeLine(650, 73, 650, 500);
            gc.strokeLine(350, 500, 650, 500);
            var best = data.getBestIndividual();
            var level = 0;
            for (int i = 0; i < best.size(); i++) {
                if (best.get(i) == 0)
                    continue;
                var weight = itemWeight.get(i);
                gc.setFill(colorsOfItems.get(i));
                gc.fillRect(350, 500 - level - Math.round(((weight * 1.0) / weightOfBackpack) * HEIGHT_OF_CONTAINER), 300, Math.round(((weight * 1.0) / weightOfBackpack) * HEIGHT_OF_CONTAINER) );
                level += Math.round(((weight * 1.0) / weightOfBackpack) * HEIGHT_OF_CONTAINER);
            }
        }
    }
}
