package fintrend.ai.platform.backend.fintrend_ai_platform_backend.ModelEvaluation.service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class ModelEvaluationService {

    // MAPE = (1/N) * Σ |(actualPrice - predictedPrice) / actualPrice| * 100
    public double calculateMAPE(List<Double> actualPrices, List<Double> predictedPrices) {
        if (actualPrices.size() != predictedPrices.size() || actualPrices.isEmpty()) {
            throw new IllegalArgumentException("Lists must be of equal size and not empty.");
        }
        double sum = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("Index,Predicted,Actual,Difference,PercentageError\n");
        for (int i = 0; i < actualPrices.size(); i++) {
            double actual = actualPrices.get(i);
            double predicted = predictedPrices.get(i);
            double diff = predicted - actual;
            double percentError = actual != 0 ? Math.abs(diff / actual) * 100 : 0;
            sum += actual != 0 ? Math.abs((actual - predicted) / actual) : 0;
            sb.append(i + 1).append(",")
              .append(predicted).append(",")
              .append(actual).append(",")
              .append(diff).append(",")
              .append(percentError).append("\n");
        }
        // Write to /resources/model_evaluation.csv
        try (FileWriter writer = new FileWriter("src/main/resources/model_evaluation.csv")) {
            writer.write(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error writing to file");
        }

        return (sum / actualPrices.size()) * 100;
    }


}
