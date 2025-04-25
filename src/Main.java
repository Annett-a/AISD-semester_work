import java.util.Random;
import java.io.*;
import java.util.Arrays;

public class Main {

    private static long iterationCount;

    // Настройки прогрева и замеров
    private static final int WARMUP_RUNS  = 10;
    private static final int MEASURE_RUNS = 1000;

    private static int getMax(int[] arr) {
        int max = arr[0];
        for (int i = 1; i < arr.length; i++) {
            iterationCount++;
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }

    public static void radixSort(int[] arr) {
        int n = arr.length;
        int[] output = new int[n];
        int[] count  = new int[10];

        int maxVal = getMax(arr);

        for (int exp = 1; maxVal / exp > 0; exp *= 10) {
            for (int i = 0; i < 10; i++) {
                count[i] = 0;
                iterationCount++;
            }
            for (int i = 0; i < n; i++) {
                int digit = (arr[i] / exp) % 10;
                count[digit]++;
                iterationCount++;
            }
            for (int i = 1; i < 10; i++) {
                count[i] += count[i - 1];
                iterationCount++;
            }
            for (int i = n - 1; i >= 0; i--) {
                int digit = (arr[i] / exp) % 10;
                output[count[digit] - 1] = arr[i];
                count[digit]--;
                iterationCount++;
            }
            for (int i = 0; i < n; i++) {
                arr[i] = output[i];
                iterationCount++;
            }
        }
    }

    public static void generateTestData(String filename,
                                        int numSets,
                                        int minSize,
                                        int maxSize,
                                        int maxValue) throws IOException {
        Random rand = new Random();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(numSets + "\n");
            for (int s = 0; s < numSets; s++) {
                int size = rand.nextInt(maxSize - minSize + 1) + minSize;
                writer.write(size + "\n");
                for (int i = 0; i < size; i++) {
                    writer.write(Integer.toString(rand.nextInt(maxValue + 1)));
                    if (i < size - 1) writer.write(" ");
                }
                writer.newLine();
            }
        }
    }

    public static void runTests(String filename,
                                int maxValue) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            int numSets = Integer.parseInt(reader.readLine().trim());
            Random rand = new Random();

            int[] warmup = new int[1_000];
            for (int w = 0; w < WARMUP_RUNS; w++) {
                for (int i = 0; i < warmup.length; i++) {
                    warmup[i] = rand.nextInt(maxValue + 1);
                }
                radixSort(warmup.clone());
            }

            for (int set = 1; set <= numSets; set++) {
                int size = Integer.parseInt(reader.readLine().trim());
                String[] parts = reader.readLine().trim().split("\\s+");
                int[] original = new int[size];
                for (int i = 0; i < size; i++) {
                    original[i] = Integer.parseInt(parts[i]);
                }

                long totalTime  = 0;
                long totalIters = 0;

                for (int run = 0; run < MEASURE_RUNS; run++) {
                    int[] arr = Arrays.copyOf(original, size);
                    iterationCount = 0;

                    long t0 = System.nanoTime();
                    radixSort(arr);
                    long t1 = System.nanoTime();

                    totalTime  += (t1 - t0);
                    totalIters += iterationCount;
                }

                long avgTimeMicro = (totalTime / MEASURE_RUNS);
                long avgIters     = totalIters / MEASURE_RUNS;

                System.out.printf(
                        "Набор %2d (n=%5d): среднее время = %5d μs, среднее итераций = %10d%n",
                        set, size, avgTimeMicro, avgIters
                );
            }
        }
    }

    public static void main(String[] args) {
        String filename = "test_data.txt";
        int numSets = 50;
        int minSize = 100;
        int maxSize = 10_000;
        int maxValue = 100_000;

        File f = new File(filename);
        try {
            if (!f.exists()) {
                System.out.println("Генерируем тестовые данные...");
                generateTestData(filename, numSets, minSize, maxSize, maxValue);
                System.out.println("Генерация завершена.");
            }
            runTests(filename, maxValue);
        } catch (IOException e) {
            System.err.println("I/O ошибка: " + e.getMessage());
        }
    }
}

