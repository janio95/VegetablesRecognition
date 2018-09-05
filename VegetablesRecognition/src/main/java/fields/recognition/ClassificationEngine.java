package fields.recognition;

import org.tensorflow.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ClassificationEngine {

    private byte[] graphDef;
    private List<String> labels;
    private static final int W = 299;
    private static final int H = 299;
    private static final float MEAN = 0;
    private static final float SCALE = 255;
    private static final String INPUT = "Placeholder";
    private static final String OUTPUT = "final_result";
    private static final String PATH_NAME = "model/";

    public ClassificationEngine() {
        File graph = new File(PATH_NAME);
        String absolutePath = graph.getAbsolutePath();
        System.out.println("Opening: " + absolutePath);
        this.graphDef = readAllBytesOrExit(Paths.get(absolutePath, "output_graph.pb"));
        this.labels = readAllLinesOrExit(Paths.get(absolutePath, "output_labels.txt"));
    }

    public ClassificationResults classifyImage(byte[] imageBytes) {
        try (Tensor image = normalizedImageToTensor(imageBytes)) {
            float[] labelProbabilities = executeInceptionGraph(graphDef, image);
            int bestLabelIdx = maxIndex(labelProbabilities);
            System.out.println(
                    String.format(
                            "BEST MATCH: %s (%.2f%% likely)",
                            labels.get(bestLabelIdx), labelProbabilities[bestLabelIdx] * 100f));
            return new ClassificationResults(labels.get(bestLabelIdx), labelProbabilities[bestLabelIdx] * 100f);
        }
    }

    private Tensor normalizedImageToTensor(byte[] imageBytes) {
        try (Graph g = new Graph()) {
            GraphBuilder b = new GraphBuilder(g);
            final Output input = b.constant("input", imageBytes);
            final Output output =
                    b.div(
                            b.sub(
                                    b.resizeBilinear(
                                            b.expandDims(
                                                    b.cast(b.decodeJpeg(input, 3), DataType.FLOAT),
                                                    b.constant("make_batch", 0)),
                                            b.constant("size", new int[]{H, W})),
                                    b.constant("mean", MEAN)),
                            b.constant("scale", SCALE));
            try (Session s = new Session(g)) {
                return s.runner().fetch(output.op().name()).run().get(0);
            }
        }
    }

    private static float[] executeInceptionGraph(byte[] graphDef, Tensor image) {
        try (Graph g = new Graph()) {
            g.importGraphDef(graphDef);
            try (Session s = new Session(g);
                 Tensor<Float> result = s.runner().feed(INPUT, image).fetch(OUTPUT).run().get(0).expect(Float.class)) {
                final long[] rshape = result.shape();
                if (result.numDimensions() != 2 || rshape[0] != 1) {
                    throw new RuntimeException(
                            String.format(
                                    "Expected model to produce a [1 N] shaped tensor where N is the number of labels, instead it produced one with shape %s",
                                    Arrays.toString(rshape)));
                }
                int nlabels = (int) rshape[1];
                return result.copyTo(new float[1][nlabels])[0];
            }
        }
    }

    private static int maxIndex(float[] probabilities) {
        int best = 0;
        for (int i = 1; i < probabilities.length; ++i) {
            if (probabilities[i] > probabilities[best]) {
                best = i;
            }
        }
        return best;
    }

    public static byte[] readAllBytesOrExit(Path path) {
        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            System.err.println("Failed to read [" + path + "]: " + e.getMessage());
            System.exit(1);
        }
        return null;
    }

    public static List<String> readAllLinesOrExit(Path path) {
        try {
            return Files.readAllLines(path, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.err.println("Failed to read [" + path + "]: " + e.getMessage());
            System.exit(0);
        }
        return null;
    }
}
