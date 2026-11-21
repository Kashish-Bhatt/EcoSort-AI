# EcoSort AI - Dataset Provenance

## 1. Dataset Purpose

The EcoSort AI dataset is used to develop and evaluate a deep-learning-based waste sorting and recycling quality controller.

The dataset supports:

- recyclable material detection,
- object localization,
- instance segmentation,
- material composition analysis,
- purity estimation,
- contamination estimation,
- recycling-quality recommendations.

## 2. Supported Material Classes

EcoSort currently uses four material classes:

| Class ID | Material |
|---|---|
| 0 | PLASTIC |
| 1 | METAL |
| 2 | GLASS |
| 3 | PAPER_CARDBOARD |

## 3. Dataset Size

Total images: 1,407

Dataset partition:

- Training: 976 images
- Validation: 212 images
- Test: 219 images

The segmentation-label preparation pipeline produced 3,414 annotated object masks.

## 4. Dataset Source

The EcoSort dataset was prepared from waste-image data collected for academic deep-learning experimentation.

The repository contains dataset acquisition and preparation utilities, including:

- `src/download_taco_hf.py`
- dataset preparation scripts,
- label conversion scripts,
- dataset auditing utilities.

### Source Verification

Exact original dataset source(s), publisher(s), URL(s), and version(s) must be verified from the acquisition scripts and original dataset records before final submission.

These details must not be guessed.

## 5. Licensing

The exact license of every source dataset must be verified from the original source before final submission.

EcoSort is being developed as an academic and research prototype.

No dataset should be redistributed in a way that conflicts with its original license.

## 6. Label Preparation

EcoSort uses YOLO-compatible annotations.

Object-detection labels identify individual recyclable objects and their material classes.

Polygon annotations were additionally converted into YOLO instance-segmentation labels.

Segmentation preparation produced:

- Train segmentation images: 976
- Validation segmentation images: 212
- Test segmentation images: 219
- Total segmentation objects: 3,414
- Skipped polygons: 0

## 7. Dataset Partitioning

The dataset is separated into independent:

- training,
- validation,
- test

partitions.

### Training Set

Used to optimize model parameters.

### Validation Set

Used for:

- model selection,
- hyperparameter comparison,
- augmentation experiments,
- generalization monitoring.

### Test Set

Reserved for final model evaluation.

The test set should not be used to tune model parameters.

## 8. Leakage and Duplicate Control

The repository contains dataset-quality utilities including:

- `src/check_duplicates.py`
- `src/check_near_duplicates.py`
- `src/dataset_audit.py`

These utilities are used to investigate duplicate and near-duplicate images and reduce the risk of data leakage between training, validation, and test partitions.

Final duplicate-audit results will be retained as project evidence.

## 9. Dataset Preparation

The project contains scripts for dataset preparation and partitioning, including:

- dataset auditing,
- duplicate checking,
- near-duplicate checking,
- dataset splitting,
- stratified splitting,
- YOLO label preparation,
- segmentation-label preparation.

This makes the dataset preparation process reproducible and auditable.

## 10. Dataset Limitations

EcoSort currently supports only:

- plastic,
- metal,
- glass,
- paper/cardboard.

The dataset does not represent every possible waste category.

Performance may vary because of:

- lighting,
- background complexity,
- camera angle,
- object size,
- image resolution,
- blur,
- occlusion,
- transparent materials,
- unusual object shapes,
- class imbalance,
- differences between training images and real-world images.

## 11. External Evaluation

Before final deployment evaluation, EcoSort will also be tested using images that are not part of the training, validation, or test partitions.

This external evaluation will help measure real-world generalization and identify failure cases.