package com.ecosort.inference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class InferenceService {

    // =========================================================
    // PROJECT PATHS
    //
    // Maven runs from:
    // EcoSort-AI/java/ecosort-core
    //
    // Therefore project root is two levels above.
    // =========================================================

    private final Path projectRoot;

    private final Path pythonExecutable;
    private final Path inferenceScript;
    private final Path modelPath;

    private final ObjectMapper objectMapper;


    public InferenceService() {

        Path currentDirectory =
                Paths.get("")
                        .toAbsolutePath()
                        .normalize();

        projectRoot =
                currentDirectory
                        .resolve("../..")
                        .normalize();


        pythonExecutable =
                projectRoot.resolve(
                        ".venv/Scripts/python.exe"
                );


        inferenceScript =
                projectRoot.resolve(
                        "src/ecosort_inference.py"
                );


        /*
         * IMPORTANT:
         * Later, when Experiment 3/final model
         * is better, we only change this filename.
         */

        modelPath =
                projectRoot.resolve(
                        "weights/ecosort_v2_detection_best.pt"
                );


        objectMapper =
                new ObjectMapper();
    }


    // =========================================================
    // DETECTION
    // =========================================================

    public static class Detection {

        private final int classId;
        private final String material;
        private final double confidence;

        private final double x1;
        private final double y1;
        private final double x2;
        private final double y2;


        public Detection(
                int classId,
                String material,
                double confidence,
                double x1,
                double y1,
                double x2,
                double y2) {

            this.classId = classId;
            this.material = material;
            this.confidence = confidence;

            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }


        public int getClassId() {
            return classId;
        }


        public String getMaterial() {
            return material;
        }


        public double getConfidence() {
            return confidence;
        }


        public double getX1() {
            return x1;
        }


        public double getY1() {
            return y1;
        }


        public double getX2() {
            return x2;
        }


        public double getY2() {
            return y2;
        }
    }


    // =========================================================
    // INFERENCE RESULT
    // =========================================================

    public static class InferenceResult {

        private final boolean success;

        private final String imagePath;
        private final String annotatedImagePath;

        private final int imageWidth;
        private final int imageHeight;

        private final int detectionCount;

        private final String highestMaterial;
        private final double highestConfidence;

        private final int plasticCount;
        private final int metalCount;
        private final int glassCount;
        private final int paperCardboardCount;

        private final double inferenceTimeMs;

        private final List<Detection> detections;


        public InferenceResult(
                boolean success,
                String imagePath,
                String annotatedImagePath,
                int imageWidth,
                int imageHeight,
                int detectionCount,
                String highestMaterial,
                double highestConfidence,
                int plasticCount,
                int metalCount,
                int glassCount,
                int paperCardboardCount,
                double inferenceTimeMs,
                List<Detection> detections) {

            this.success = success;

            this.imagePath = imagePath;

            this.annotatedImagePath =
                    annotatedImagePath;

            this.imageWidth = imageWidth;
            this.imageHeight = imageHeight;

            this.detectionCount =
                    detectionCount;

            this.highestMaterial =
                    highestMaterial;

            this.highestConfidence =
                    highestConfidence;

            this.plasticCount =
                    plasticCount;

            this.metalCount =
                    metalCount;

            this.glassCount =
                    glassCount;

            this.paperCardboardCount =
                    paperCardboardCount;

            this.inferenceTimeMs =
                    inferenceTimeMs;

            this.detections =
                    detections;
        }


        public boolean isSuccess() {
            return success;
        }


        public String getImagePath() {
            return imagePath;
        }


        public String getAnnotatedImagePath() {
            return annotatedImagePath;
        }


        public int getImageWidth() {
            return imageWidth;
        }


        public int getImageHeight() {
            return imageHeight;
        }


        public int getDetectionCount() {
            return detectionCount;
        }


        public String getHighestMaterial() {
            return highestMaterial;
        }


        public double getHighestConfidence() {
            return highestConfidence;
        }


        public int getPlasticCount() {
            return plasticCount;
        }


        public int getMetalCount() {
            return metalCount;
        }


        public int getGlassCount() {
            return glassCount;
        }


        public int getPaperCardboardCount() {
            return paperCardboardCount;
        }


        public double getInferenceTimeMs() {
            return inferenceTimeMs;
        }


        public List<Detection> getDetections() {
            return detections;
        }
    }


    // =========================================================
    // RUN INFERENCE
    // =========================================================

    public InferenceResult analyze(
            File imageFile)
            throws Exception {

        validateEnvironment(
                imageFile
        );


        ProcessBuilder processBuilder =
                new ProcessBuilder(

                        pythonExecutable
                                .toString(),

                        inferenceScript
                                .toString(),

                        "--image",

                        imageFile
                                .getAbsolutePath(),

                        "--model",

                        modelPath
                                .toString(),

                        "--confidence",
                        "0.25",

                        "--imgsz",
                        "960"
                );


        processBuilder.directory(
                projectRoot.toFile()
        );


        /*
         * Keep stderr separate.
         *
         * stdout should contain only the JSON
         * generated by ecosort_inference.py.
         */

        Process process =
                processBuilder.start();


        StringBuilder stdout =
                new StringBuilder();


        StringBuilder stderr =
                new StringBuilder();


        Thread errorReader =
                new Thread(
                        () -> readStream(
                                process,
                                stderr,
                                true
                        )
                );


        errorReader.start();


        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        process
                                                .getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {

                stdout.append(
                        line
                );
            }
        }


        int exitCode =
                process.waitFor();


        errorReader.join();


        String jsonOutput =
                stdout
                        .toString()
                        .trim();


        if (jsonOutput.isEmpty()) {

            throw new IllegalStateException(
                    "EcoSort inference returned "
                            + "no JSON output.\n"
                            + stderr
            );
        }


        JsonNode root;

        try {

            root =
                    objectMapper.readTree(
                            jsonOutput
                    );

        } catch (Exception exception) {

            throw new IllegalStateException(
                    "Could not parse EcoSort "
                            + "inference response.\n\n"
                            + "Output:\n"
                            + jsonOutput
                            + "\n\nErrors:\n"
                            + stderr,
                    exception
            );
        }


        boolean success =
                root.path(
                        "success"
                ).asBoolean(
                        false
                );


        if (!success) {

            String error =
                    root.path(
                            "error"
                    ).asText(
                            "Unknown inference error."
                    );


            throw new IllegalStateException(
                    error
            );
        }


        if (exitCode != 0) {

            throw new IllegalStateException(
                    "Inference process exited "
                            + "with code "
                            + exitCode
                            + ".\n"
                            + stderr
            );
        }


        return parseResult(
                root
        );
    }


    // =========================================================
    // PARSE RESULT
    // =========================================================

    private InferenceResult parseResult(
            JsonNode root) {

        String imagePath =
                root.path(
                        "image"
                ).asText();


        String annotatedImagePath =
                root.path(
                        "annotated_image"
                ).asText();


        int imageWidth =
                root.path(
                        "image_size"
                )
                        .path(
                                "width"
                        )
                        .asInt();


        int imageHeight =
                root.path(
                        "image_size"
                )
                        .path(
                                "height"
                        )
                        .asInt();


        int detectionCount =
                root.path(
                        "detection_count"
                ).asInt();


        String highestMaterial =
                root.path(
                        "highest_material"
                ).asText(
                        "UNKNOWN"
                );


        double highestConfidence =
                root.path(
                        "highest_confidence"
                ).asDouble(
                        0.0
                );


        JsonNode counts =
                root.path(
                        "class_counts"
                );


        int plasticCount =
                counts.path(
                        "PLASTIC"
                ).asInt(
                        0
                );


        int metalCount =
                counts.path(
                        "METAL"
                ).asInt(
                        0
                );


        int glassCount =
                counts.path(
                        "GLASS"
                ).asInt(
                        0
                );


        int paperCount =
                counts.path(
                        "PAPER_CARDBOARD"
                ).asInt(
                        0
                );


        double inferenceTimeMs =
                root.path(
                        "inference_time_ms"
                ).asDouble(
                        0.0
                );


        List<Detection> detections =
                new ArrayList<>();


        JsonNode detectionArray =
                root.path(
                        "detections"
                );


        if (detectionArray.isArray()) {

            for (JsonNode node
                    : detectionArray) {

                JsonNode bbox =
                        node.path(
                                "bbox"
                        );


                double x1 =
                        bbox.size() > 0
                                ? bbox.get(0)
                                .asDouble()
                                : 0.0;


                double y1 =
                        bbox.size() > 1
                                ? bbox.get(1)
                                .asDouble()
                                : 0.0;


                double x2 =
                        bbox.size() > 2
                                ? bbox.get(2)
                                .asDouble()
                                : 0.0;


                double y2 =
                        bbox.size() > 3
                                ? bbox.get(3)
                                .asDouble()
                                : 0.0;


                detections.add(
                        new Detection(

                                node.path(
                                        "class_id"
                                ).asInt(),

                                node.path(
                                        "material"
                                ).asText(),

                                node.path(
                                        "confidence"
                                ).asDouble(),

                                x1,
                                y1,
                                x2,
                                y2
                        )
                );
            }
        }


        return new InferenceResult(

                true,

                imagePath,

                annotatedImagePath,

                imageWidth,

                imageHeight,

                detectionCount,

                highestMaterial,

                highestConfidence,

                plasticCount,

                metalCount,

                glassCount,

                paperCount,

                inferenceTimeMs,

                detections
        );
    }


    // =========================================================
    // VALIDATION
    // =========================================================

    private void validateEnvironment(
            File imageFile) {

        if (
                imageFile == null
                        || !imageFile.exists()
        ) {

            throw new IllegalArgumentException(
                    "Input image does not exist."
            );
        }


        if (
                !pythonExecutable
                        .toFile()
                        .exists()
        ) {

            throw new IllegalStateException(
                    "Python environment not found:\n"
                            + pythonExecutable
            );
        }


        if (
                !inferenceScript
                        .toFile()
                        .exists()
        ) {

            throw new IllegalStateException(
                    "Inference script not found:\n"
                            + inferenceScript
            );
        }


        if (
                !modelPath
                        .toFile()
                        .exists()
        ) {

            throw new IllegalStateException(
                    "EcoSort model not found:\n"
                            + modelPath
            );
        }
    }


    // =========================================================
    // STDERR READER
    // =========================================================

    private void readStream(
            Process process,
            StringBuilder destination,
            boolean errorStream) {

        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(

                                        errorStream
                                                ? process
                                                .getErrorStream()
                                                : process
                                                .getInputStream(),

                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {

                destination
                        .append(line)
                        .append(
                                System.lineSeparator()
                        );
            }

        } catch (Exception ignored) {

            // Main analyze() method handles
            // process failures.
        }
    }


    // =========================================================
    // TEST MAIN
    // =========================================================

    public static void main(
            String[] args) {

        try {

            InferenceService service =
                    new InferenceService();


            Path testImage =
                    service.projectRoot.resolve(
                            "data/ecosort_yolo/"
                                    + "images/test/"
                                    + "ecosort_00016.jpg"
                    );


            System.out.println(
                    "\n===== ECOSORT JAVA → PYTHON INFERENCE =====\n"
            );


            InferenceResult result =
                    service.analyze(
                            testImage.toFile()
                    );


            System.out.println(
                    "Success: "
                            + result.isSuccess()
            );


            System.out.println(
                    "Detections: "
                            + result.getDetectionCount()
            );


            System.out.println(
                    "Primary material: "
                            + result.getHighestMaterial()
            );


            System.out.printf(
                    "Highest confidence: %.2f%%%n",
                    result.getHighestConfidence()
                            * 100.0
            );


            System.out.println(
                    "\nObject counts:"
            );


            System.out.println(
                    "Plastic: "
                            + result.getPlasticCount()
            );


            System.out.println(
                    "Metal: "
                            + result.getMetalCount()
            );


            System.out.println(
                    "Glass: "
                            + result.getGlassCount()
            );


            System.out.println(
                    "Paper/Cardboard: "
                            + result.getPaperCardboardCount()
            );


            System.out.printf(
                    "%nPipeline time: %.2f ms%n",
                    result.getInferenceTimeMs()
            );


            System.out.println(
                    "\nAnnotated image:"
            );


            System.out.println(
                    result.getAnnotatedImagePath()
            );


            System.out.println(
                    "\nIndividual detections:"
            );


            for (Detection detection
                    : result.getDetections()) {

                System.out.printf(
                        "%-18s %.2f%%%n",
                        detection.getMaterial(),
                        detection.getConfidence()
                                * 100.0
                );
            }


            System.out.println(
                    "\n===== BRIDGE TEST COMPLETE ====="
            );


        } catch (Exception exception) {

            System.err.println(
                    "\nEcoSort inference failed:"
            );

            exception.printStackTrace();
        }
    }
}