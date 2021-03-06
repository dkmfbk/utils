package eu.fbk.utils.svm.run;

import eu.fbk.utils.core.CommandLine;
import eu.fbk.utils.eval.ConfusionMatrix;
import eu.fbk.utils.svm.Classifier;
import eu.fbk.utils.svm.LabelledVector;
import eu.fbk.utils.svm.Vector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by alessio on 26/04/17.
 */

public class ExperimentList {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExperimentList.class);
    private static final Boolean useID = false;

    public static void main(String[] args) {
        try {

            final CommandLine cmd = CommandLine.parser().withName("experiment-list")
                    .withOption("i", "vectors", "Input file with vectors", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("e", "experiments", "Input file with experiments", "FILE", CommandLine.Type.FILE_EXISTING, true, false, true)
                    .withOption("o", "output", "Output file", "FILE", CommandLine.Type.FILE, true, false, true)
                    .withLogger(LoggerFactory.getLogger("eu.fbk")).parse(args);

            File outputFile = cmd.getOptionValue("output", File.class);
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));

            File vectorFile = cmd.getOptionValue("vectors", File.class);
            List<String> lines;
            lines = Files.readAllLines(vectorFile.toPath());

            HashMap<String, LabelledVector> vectorIndex = new HashMap<>();
            List<LabelledVector> vectors = new ArrayList<>();
            int idInt = 0;
            for (String line : lines) {
                String[] parts = line.split("\\s+");
                final Vector.Builder builder = eu.fbk.utils.svm.Vector.builder();

                idInt++;
                String id = Integer.toString(idInt);
                int start = 0;
                if (useID) {
                    id = parts[0];
                    start = 1;
                }
                Integer label = Integer.parseInt(parts[start]);
                for (int i = start + 1; i < parts.length; i++) {
                    String part = parts[i];
                    String[] splitted = part.split(":");
                    String featName = "feat" + splitted[0];
                    Float featValue = Float.parseFloat(splitted[1]);
                    builder.set(featName, featValue);
                }
                LabelledVector vector = builder.setID(id).build().label(label);
                vectors.add(vector);
                vectorIndex.put(id, vector);
            }

            File experimentsFile = cmd.getOptionValue("experiments", File.class);
            List<String> configLines = Files.readAllLines(experimentsFile.toPath());
            for (int i1 = 0; i1 < configLines.size(); i1++) {
                String configLine = configLines.get(i1);

                System.out.println(String.format("Line %d/%d", i1 + 1, configLines.size()));

                configLine = configLine.trim();
                if (configLine.startsWith("#")) {
                    continue;
                }
                String[] configParts = configLine.split("\\s+");

                Integer numLabels = Integer.parseInt(configParts[1]);

                Float c = null;
                try {
                    c = Float.parseFloat(configParts[2]);
                } catch (Exception e) {
                    // ignored
                }
                Float gamma = null;
                try {
                    gamma = Float.parseFloat(configParts[3]);
                } catch (Exception e) {
                    // ignored
                }

                int numWeights = configParts.length - 4;
                float[] weights = new float[numWeights];

                if (numWeights != numLabels) {
//                    System.out.println("Weights: " + numWeights);
//                    System.out.println("Labels: " + numLabels);
//                    throw new Exception("Incoherent information about weights");
                    weights = new float[numLabels];
                    for (int i = 0; i < numLabels; i++) {
                        weights[i] = 1;
                    }
                } else {
                    for (int i = 4; i < configParts.length; i++) {
                        weights[i - 4] = Float.parseFloat(configParts[i]);
                    }
                }

                Classifier.Parameters parameters;
                String type = configParts[0];
                if (type.equals("0")) {
                    parameters = Classifier.Parameters.forSVMLinearKernel(numLabels, weights, c);
                } else if (type.equals("1")) {
                    parameters = Classifier.Parameters.forSVMPolyKernel(numLabels, weights, c, gamma, null, null);
                } else if (type.equals("2")) {
                    parameters = Classifier.Parameters.forSVMRBFKernel(numLabels, weights, c, gamma);
                } else {
                    throw new Exception("No type specified");
                }

                HashMap<String, Integer> results = new HashMap<>();
                ConfusionMatrix confusionMatrix = Classifier.crossValidate(parameters, vectors, 10, results);
                writer.append(configLine).append("\n");
                writer.append(confusionMatrix.toString()).append("\n");
                writer.append(Integer.toString(results.size())).append("\n");
                for (String key : results.keySet()) {
                    StringBuffer buffer = new StringBuffer();
                    buffer.append(key).append("\t").append(vectorIndex.get(key).getLabel()).append("\t").append(results.get(key));
                    writer.append(buffer.toString()).append("\n");
                }
                writer.flush();
            }

            writer.close();
        } catch (Exception e) {
            CommandLine.fail(e);
        }
    }
}
