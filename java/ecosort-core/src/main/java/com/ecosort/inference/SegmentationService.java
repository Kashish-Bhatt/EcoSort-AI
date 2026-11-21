package com.ecosort.inference;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumMap;
import java.util.Map;

import com.ecosort.quality.RecyclingQualityAnalyzer.Material;

public class SegmentationService {

    private final Path projectRoot;

    private final Path pythonExecutable;
    private final Path segmentationScript;
    private final Path segmentationModel;

    private final ObjectMapper objectMapper;


    public SegmentationService() {

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

        segmentationScript =
                projectRoot.resolve(
                        "src/ecosort_segmentation_inference.py"
                );

        segmentationModel =
                projectRoot.resolve(
                        "weights/ecosort_v2_segmentation_best.pt"
                );

        objectMapper =
                new ObjectMapper();
    }


    public static class SegmentationResult {

        private final String dominantMaterial;

        private final double purity;
        private final double contamination;

        private final int instanceCount;

        private final long totalMaskArea;

        private final Map<Material, Double>
                composition;

        private final Map<Material, Double>
                materialAreas;

        private final String segmentedImagePath;

        private final double pipelineTimeMs;


        public SegmentationResult(
                String dominantMaterial,
                double purity,
                double contamination,
                int instanceCount,
                long totalMaskArea,
                Map<Material, Double> composition,
                Map<Material, Double> materialAreas,
                String segmentedImagePath,
                double pipelineTimeMs) {

            this.dominantMaterial =
                    dominantMaterial;

            this.purity =
                    purity;

            this.contamination =
                    contamination;

            this.instanceCount =
                    instanceCount;

            this.totalMaskArea =
                    totalMaskArea;

            this.composition =
                    composition;

            this.materialAreas =
                    materialAreas;

            this.segmentedImagePath =
                    segmentedImagePath;

            this.pipelineTimeMs =
                    pipelineTimeMs;
        }


        public String getDominantMaterial() {
            return dominantMaterial;
        }


        public double getPurity() {
            return purity;
        }


        public double getContamination() {
            return contamination;
        }


        public int getInstanceCount() {
            return instanceCount;
        }


        public long getTotalMaskArea() {
            return totalMaskArea;
        }


        public Map<Material, Double>
        getComposition() {

            return composition;
        }


        public Map<Material, Double>
        getMaterialAreas() {

            return materialAreas;
        }


        public String getSegmentedImagePath() {
            return segmentedImagePath;
        }


        public double getPipelineTimeMs() {
            return pipelineTimeMs;
        }
    }


    public boolean isModelAvailable() {

        return segmentationModel
                .toFile()
                .exists();
    }


    public SegmentationResult analyze(
            File imageFile)
            throws Exception {

        validateEnvironment(
                imageFile
        );

        ProcessBuilder builder =
                new ProcessBuilder(

                        pythonExecutable.toString(),

                        segmentationScript.toString(),

                        "--image",
                        imageFile.getAbsolutePath(),

                        "--model",
                        segmentationModel.toString(),

                        "--confidence",
                        "0.25",

                        "--imgsz",
                        "640"
                );

        builder.directory(
                projectRoot.toFile()
        );

        Process process =
                builder.start();


        StringBuilder stdout =
                new StringBuilder();

        StringBuilder stderr =
                new StringBuilder();


        Thread errorThread =
                new Thread(
                        () -> readStream(
                                process,
                                stderr,
                                true
                        )
                );

        errorThread.start();


        try (
                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        process.getInputStream(),
                                        StandardCharsets.UTF_8
                                )
                        )
        ) {

            String line;

            while (
                    (line = reader.readLine())
                            != null
            ) {

                stdout.append(line);
            }
        }


        int exitCode =
                process.waitFor();

        errorThread.join();


        String json =
                stdout.toString()
                        .trim();


        if (json.isEmpty()) {

            throw new IllegalStateException(
                    "Segmentation returned no JSON.\n"
                            + stderr
            );
        }


        JsonNode root =
                objectMapper.readTree(
                        json
                );


        if (
                !root.path("success")
                        .asBoolean(false)
        ) {

            throw new IllegalStateException(
                    root.path("error")
                            .asText(
                                    "Unknown segmentation error."
                            )
            );
        }


        if (exitCode != 0) {

            throw new IllegalStateException(
                    "Segmentation process exited "
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


    private SegmentationResult parseResult(
            JsonNode root) {

        Map<Material, Double> composition =
                new EnumMap<>(
                        Material.class
                );

        Map<Material, Double> areas =
                new EnumMap<>(
                        Material.class
                );


        JsonNode compositionNode =
                root.path(
                        "composition"
                );


        JsonNode areaNode =
                root.path(
                        "material_areas"
                );


        for (Material material
                : Material.values()) {

            String key =
                    material.name();

            composition.put(
                    material,
                    compositionNode
                            .path(key)
                            .asDouble(0.0)
            );

            areas.put(
                    material,
                    areaNode
                            .path(key)
                            .asDouble(0.0)
            );
        }


        return new SegmentationResult(

                root.path(
                        "dominant_material"
                ).asText(
                        "UNKNOWN"
                ),

                root.path(
                        "purity"
                ).asDouble(0.0),

                root.path(
                        "contamination"
                ).asDouble(0.0),

                root.path(
                        "instance_count"
                ).asInt(0),

                root.path(
                        "total_mask_area"
                ).asLong(0),

                composition,

                areas,

                root.path(
                        "segmented_image"
                ).asText(),

                root.path(
                        "pipeline_time_ms"
                ).asDouble(0.0)
        );
    }


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
            !segmentationScript
                    .toFile()
                    .exists()
        ) {

            throw new IllegalStateException(
                    "Segmentation script not found:\n"
                            + segmentationScript
            );
        }


        if (
            !segmentationModel
                    .toFile()
                    .exists()
        ) {

            throw new IllegalStateException(
                    "Segmentation model is not installed yet.\n"
                            + "Expected:\n"
                            + segmentationModel
            );
        }
    }


    private void readStream(
            Process process,
            StringBuilder destination,
            boolean errorStream) {

        try (
            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(

                                    errorStream
                                            ? process.getErrorStream()
                                            : process.getInputStream(),

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
        }
    }


    public static void main(
            String[] args) {

        SegmentationService service =
                new SegmentationService();

        System.out.println(
                "\n===== ECOSORT SEGMENTATION SERVICE ====="
        );

        System.out.println(
                "Segmentation model available: "
                        + service.isModelAvailable()
        );

        if (!service.isModelAvailable()) {

            System.out.println(
                    "\nExpected model:"
            );

            System.out.println(
                    service.segmentationModel
            );

            System.out.println(
                    "\nService is ready. "
                            + "Train/download the segmentation "
                            + "weights and place them at that path."
            );
        }

        System.out.println(
                "\n========================================"
        );
    }
}