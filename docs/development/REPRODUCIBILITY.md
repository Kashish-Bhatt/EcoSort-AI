# EcoSort AI - Reproducibility Guide

## 1. Project

Project: EcoSort AI

Domain: Circular Economy

Purpose: Waste Sorting and Recycling Quality Controller

## 2. Random Seed

Primary reproducibility seed:

42

Where supported, experiments use the same random seed to improve repeatability and fair comparison.

## 3. Dataset Partition

Total images: 1,407

- Train: 976
- Validation: 212
- Test: 219

The same partition should be retained when comparing model experiments.

## 4. Material Classes

Class mapping:

- 0 = PLASTIC
- 1 = METAL
- 2 = GLASS
- 3 = PAPER_CARDBOARD

Class IDs must remain consistent across:

- training,
- validation,
- test,
- detection inference,
- segmentation inference,
- Java integration.

## 5. Java Environment

Primary implementation language:

Java

Java release target:

17

Build system:

Maven

Main Maven project:

`java/ecosort-core`

## 6. Java Verification

Compile:

```text
mvn compile