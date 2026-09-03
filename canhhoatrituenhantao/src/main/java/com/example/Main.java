package com.example;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class Main {

    public record IrisSample(double sepalLength, double sepalWidth, double petalLength, double petalWidth, String species) {
        public double[] features() {
            return new double[]{sepalLength, sepalWidth, petalLength, petalWidth};
        }
    }

    public static void main(String[] args) {
        List<IrisSample> dataset = loadDataset("/IRIS.csv");
        if (dataset.isEmpty()) {
            System.err.println("Không thể đọc được file dữ liệu IRIS.csv!");
            return;
        }

        System.out.println(">>> Đã tải thành công " + dataset.size() + " mẫu dữ liệu.\n");

        // Xáo trộn ngẫu nhiên dữ liệu với seed cố định để đảm bảo kết quả lặp lại được
        Collections.shuffle(dataset, new Random(42));

        // Chia tập dữ liệu: 80% Train, 20% Test
        int trainSize = (int) (dataset.size() * 0.8);
        List<IrisSample> trainData = dataset.subList(0, trainSize);
        List<IrisSample> testData = dataset.subList(trainSize, dataset.size());

        // Huấn luyện & Đánh giá mô hình K-NN (với K = 3)
        int k = 3;
        KNNClassifier classifier = new KNNClassifier(k);
        classifier.fit(trainData);

        int correctPredictions = 0;
        Map<String, Map<String, Integer>> confusionMatrix = new HashMap<>();

        for (IrisSample sample : testData) {
            String actual = sample.species();
            String predicted = classifier.predict(sample.features());

            if (actual.equals(predicted)) {
                correctPredictions++;
            }

            confusionMatrix
                .computeIfAbsent(actual, key -> new HashMap<>())
                .put(predicted, confusionMatrix.get(actual).getOrDefault(predicted, 0) + 1);
        }

        double accuracy = (double) correctPredictions / testData.size() * 100;

        // In kết quả đánh giá
        System.out.println("=== KẾT QUẢ ĐÁNH GIÁ MÔ HÌNH K-NN (K=" + k + ") ===");
        System.out.println("Số lượng tập Train: " + trainData.size());
        System.out.println("Số lượng tập Test:  " + testData.size());
        System.out.printf("Độ chính xác (Accuracy): %.2f%%\n\n", accuracy);

        System.out.println("--- Ma trận nhầm lẫn (Confusion Matrix) ---");
        System.out.printf("%-18s %-15s %-15s %-15s\n", "Thực tế \\ Dự đoán", "Iris-setosa", "Iris-versicolor", "Iris-virginica");
        for (String actual : List.of("Iris-setosa", "Iris-versicolor", "Iris-virginica")) {
            System.out.printf("%-18s", actual);
            for (String pred : List.of("Iris-setosa", "Iris-versicolor", "Iris-virginica")) {
                int count = confusionMatrix.getOrDefault(actual, Collections.emptyMap()).getOrDefault(pred, 0);
                System.out.printf(" %-15d", count);
            }
            System.out.println();
        }

        // Kiểm thử dự đoán mẫu mới
        System.out.println("\n--- Thử nghiệm dự đoán mẫu mới ---");
        double[] newSample = {5.1, 3.5, 1.4, 0.2}; // Đặc trưng điển hình của Iris-setosa
        String predictedClass = classifier.predict(newSample);
        System.out.printf("Đặc trưng đầu vào: [sepal_length=5.1, sepal_width=3.5, petal_length=1.4, petal_width=0.2]\n");
        System.out.println("Dự đoán nhầm thuộc loài: " + predictedClass);
    }

    private static List<IrisSample> loadDataset(String resourcePath) {
        List<IrisSample> samples = new ArrayList<>();
        try (InputStream is = Main.class.getResourceAsStream(resourcePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            
            String line;
            boolean isHeader = true;
            while ((line = br.readLine()) != null) {
                if (isHeader) {
                    isHeader = false; // Bỏ qua dòng tiêu đề
                    continue;
                }
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length == 5) {
                    double sepalLength = Double.parseDouble(parts[0].trim());
                    double sepalWidth = Double.parseDouble(parts[1].trim());
                    double petalLength = Double.parseDouble(parts[2].trim());
                    double petalWidth = Double.parseDouble(parts[3].trim());
                    String species = parts[4].trim();

                    samples.add(new IrisSample(sepalLength, sepalWidth, petalLength, petalWidth, species));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return samples;
    }

    // Lớp xử lý thuật toán K-NN
    static class KNNClassifier {
        private final int k;
        private List<IrisSample> trainData;
        private double[] minValues;
        private double[] maxValues;

        public KNNClassifier(int k) {
            this.k = k;
        }

        public void fit(List<IrisSample> data) {
            this.trainData = new ArrayList<>(data);
            calculateMinMax();
        }

        // Tính khoảng min-max của từng đặc trưng để chuẩn hóa Min-Max
        private void calculateMinMax() {
            minValues = new double[4];
            maxValues = new double[4];
            Arrays.fill(minValues, Double.MAX_VALUE);
            Arrays.fill(maxValues, Double.MIN_VALUE);

            for (IrisSample sample : trainData) {
                double[] feats = sample.features();
                for (int i = 0; i < 4; i++) {
                    if (feats[i] < minValues[i]) minValues[i] = feats[i];
                    if (feats[i] > maxValues[i]) maxValues[i] = feats[i];
                }
            }
        }

        private double[] normalize(double[] features) {
            double[] normalized = new double[features.length];
            for (int i = 0; i < features.length; i++) {
                double range = maxValues[i] - minValues[i];
                normalized[i] = (range == 0) ? 0 : (features[i] - minValues[i]) / range;
            }
            return normalized;
        }

        public String predict(double[] features) {
            double[] normFeatures = normalize(features);

            // Tính khoảng cách Euclid tới tất cả các điểm trong tập train
            List<NeighborDistance> distances = new ArrayList<>();
            for (IrisSample sample : trainData) {
                double[] normTrainFeatures = normalize(sample.features());
                double dist = euclideanDistance(normFeatures, normTrainFeatures);
                distances.add(new NeighborDistance(dist, sample.species()));
            }

            // Sắp xếp lấy K láng giềng gần nhất
            distances.sort(Comparator.comparingDouble(NeighborDistance::distance));

            // Bỏ phiếu (Majority Voting)
            Map<String, Long> votes = distances.stream()
                    .limit(k)
                    .collect(Collectors.groupingBy(NeighborDistance::species, Collectors.counting()));

            return votes.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey)
                    .orElse("Unknown");
        }

        private double euclideanDistance(double[] a, double[] b) {
            double sum = 0;
            for (int i = 0; i < a.length; i++) {
                sum += Math.pow(a[i] - b[i], 2);
            }
            return Math.sqrt(sum);
        }

        private record NeighborDistance(double distance, String species) {}
    }
}