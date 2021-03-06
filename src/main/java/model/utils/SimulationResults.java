package model.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import model.algorithms.Algorithm;
import model.problems.Problem;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Class that holds data about end of the simulation run.
 */
@Getter
@Setter
public class SimulationResults {
    private final List<Double> averageFitness;
    private final List<Double> bestFitness;
    private Problem usedProblem;
    @JsonIgnore
    private String usedProblemInJson;
    private Algorithm usedAlgorithm;
    @JsonIgnore
    private String usedAlgorithmInJson;
    private String nameOfDataset;
    private Integer numberOfDataset;
    private Double lowerBound;
    private Double upperBound;
    @JsonIgnore
    private Boolean showBest;
    @JsonIgnore
    private Boolean showAverage;
    @JsonIgnore
    private Boolean deleted;
    private static final String CSV_SEPARATOR = ",";

    /**
     * Setup data.
     * @param nameOfDataset name of the dataset.
     */
    public SimulationResults(String nameOfDataset) {
        averageFitness = new ArrayList<>();
        bestFitness = new ArrayList<>();
        this.nameOfDataset = nameOfDataset;
    }

    /**
     * Setup data.
     */
    public SimulationResults() {
        averageFitness = new ArrayList<>();
        bestFitness = new ArrayList<>();
    }

    /**
     * Add data of one iteration.
     * @param average Average fitness in iteration.
     * @param best Best fitness in iteration.
     */
    public void addData(Double average, Double best) {
        averageFitness.add(average);
        bestFitness.add(best);
    }

//    public void writeToCsv(File file, Double upperBound, Double lowerBound) throws IOException {
//        List<String> lines = new ArrayList<>();
//        lines.add(this.nameOfDataset + CSV_SEPARATOR + upperBound + CSV_SEPARATOR + lowerBound);
//        for (int i = 0; i < averageFitness.size(); i++) {
//            lines.add(averageFitness.get(i) + CSV_SEPARATOR + bestFitness.get(i));
//        }
//        Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
//    }

    /**
     * Write data to json file.
     * @param file name of the file.
     * @param upperBound the highest value in chart.
     * @param lowerBound the lowest value in chart.
     * @param problem used problem.
     * @param algorithm used algorithm.
     * @throws IOException
     */
    public void writeToJson(File file, Double upperBound, Double lowerBound, Problem problem, Algorithm algorithm) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        this.usedProblem = problem;
        this.usedAlgorithm = algorithm;
        this.upperBound = upperBound;
        this.lowerBound = lowerBound;
        List<String> lines = new ArrayList<>();
        try {
            String json = mapper.writeValueAsString(this);
            lines.add(json);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
    }


//    public void loadFromCsv(File file) throws FileNotFoundException {
//        Scanner sc = new Scanner(file);
//        String line = null;
//        while (sc.hasNextLine()) {
//            if (line == null) {
//                line = sc.nextLine();
//                var parts = line.split(CSV_SEPARATOR);
//                if (parts.length != 3)
//                    throw new FileNotFoundException();
//                this.nameOfDataset = parts[0];
//                this.upperBound = Double.valueOf(parts[1]);
//                this.lowerBound = Double.valueOf(parts[2]);
//            } else {
//                line = sc.nextLine();
//                var parts = line.split(CSV_SEPARATOR);
//                if (parts.length != 2)
//                    throw new FileNotFoundException();
//                averageFitness.add(Double.parseDouble(parts[0]));
//                bestFitness.add(Double.parseDouble(parts[1]));
//            }
//        }
//        showBest = true;
//        showAverage = true;
//        deleted = false;
//    }

    /**
     * Loads data form json file.
     * @param file name of the file.
     * @throws IOException
     */
    public void loadFromJson(File file) throws IOException {
        String jsonString = Files.readString(file.toPath());
        JSONObject obj = new JSONObject(jsonString);

        this.nameOfDataset = obj.getString("nameOfDataset");
        this.upperBound = obj.getDouble("upperBound");
        this.lowerBound = obj.getDouble("lowerBound");
        for (Object decimal: obj.getJSONArray("averageFitness").toList()) {
            averageFitness.add(((BigDecimal) decimal).doubleValue());
        }
        for (Object decimal: obj.getJSONArray("bestFitness").toList()) {
            bestFitness.add(((BigDecimal) decimal).doubleValue());
        }
        this.usedAlgorithmInJson = obj.getJSONObject("usedAlgorithm").toString();
        this.usedProblemInJson = obj.getJSONObject("usedProblem").toString();

        showBest = true;
        showAverage = true;
        deleted = false;
    }
}
