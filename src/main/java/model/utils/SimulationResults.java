package model.utils;

import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@Getter
@Setter
public class SimulationResults {
    private final List<Double> averageFitness;
    private final List<Double> bestFitness;
    private String nameOfDataset;
    private Integer numberOfDataset;
    private Double lowerBound;
    private Double upperBound;
    private Boolean showBest;
    private Boolean showAverage;
    private Boolean deleted;
    private static final String CSV_SEPARATOR = ",";

    public SimulationResults(String nameOfDataset) {
        averageFitness = new ArrayList<>();
        bestFitness = new ArrayList<>();
        this.nameOfDataset = nameOfDataset;
    }

    public SimulationResults() {
        averageFitness = new ArrayList<>();
        bestFitness = new ArrayList<>();
    }

    public void addData(Double average, Double best) {
        averageFitness.add(average);
        bestFitness.add(best);
    }

    public void writeToCsv(File file, Double upperBound, Double lowerBound) throws IOException {
        List<String> lines = new ArrayList<>();
        lines.add(this.nameOfDataset + CSV_SEPARATOR + upperBound + CSV_SEPARATOR + lowerBound);
        for (int i = 0; i < averageFitness.size(); i++) {
            lines.add(averageFitness.get(i) + CSV_SEPARATOR + bestFitness.get(i));
        }
        Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
    }

    public void loadFromCsv(File file) throws FileNotFoundException {
        Scanner sc = new Scanner(file);
        String line = null;
        while (sc.hasNextLine()) {
            if (line == null) {
                line = sc.nextLine();
                var parts = line.split(CSV_SEPARATOR);
                if (parts.length != 3)
                    throw new FileNotFoundException();
                this.nameOfDataset = parts[0];
                this.upperBound = Double.valueOf(parts[1]);
                this.lowerBound = Double.valueOf(parts[2]);
            } else {
                line = sc.nextLine();
                var parts = line.split(CSV_SEPARATOR);
                if (parts.length != 2)
                    throw new FileNotFoundException();
                averageFitness.add(Double.parseDouble(parts[0]));
                bestFitness.add(Double.parseDouble(parts[1]));
            }
        }
        showBest = true;
        showAverage = true;
        deleted = false;
    }
}
