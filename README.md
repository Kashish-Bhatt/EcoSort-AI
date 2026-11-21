# ♻️ EcoSort AI

**Deep Learning Based Waste Detection, Segmentation & Recycling Quality Analysis**

EcoSort AI is a deep-learning waste analysis system that identifies recyclable materials from images, performs object detection and instance segmentation, estimates material composition, and calculates recycling purity and contamination.

The project combines **YOLO11-based computer vision**, **Python/Ultralytics inference**, and a **JavaFX desktop application**. A cross-model decision layer compares detection and segmentation results and flags uncertain or conflicting predictions for **Manual Review**.

---

## ✨ Features

- Object detection using **YOLO11s**
- Instance segmentation using **YOLO11s-seg**
- Semantic material maps derived from segmentation masks
- Four recyclable material classes:
  - Plastic
  - Metal
  - Glass
  - Paper/Cardboard
- Material composition estimation
- Purity and contamination calculation
- Detection confidence reporting
- Detection + segmentation cross-model verification
- Manual Review for uncertain/conflicting results
- Persistent analysis history
- JavaFX desktop interface
- Robustness and error-analysis pipeline
- Java implementations of deep-learning fundamentals
- LSTM and VAE demonstrations

---

## 🧠 System Architecture

```text
                     Waste Image
                         │
            ┌────────────┴────────────┐
            │                         │
            ▼                         ▼
      YOLO11s Detector          YOLO11s-seg
        960 × 960                 640 × 640
            │                         │
            │                  Instance Masks
            │                         │
            │                  Material Areas
            │                         │
            └────────────┬────────────┘
                         │
                         ▼
                  Decision Fusion
                         │
              ┌──────────┴──────────┐
              │                     │
          Models Agree        Uncertain /
              │               Models Disagree
              ▼                     │
        Material Result             ▼
                              MANUAL REVIEW
                         │
                         ▼
                 JavaFX Application
                         │
        ┌────────────────┼────────────────┐
        ▼                ▼                ▼
     Purity        Contamination      History
```

---

## 🧱 Technology Stack

### Deep Learning / Computer Vision
- Python
- PyTorch
- Ultralytics YOLO11
- OpenCV
- NumPy

### Application
- Java 17
- JavaFX
- Maven

### Model Integration
- Java `ProcessBuilder`
- Python inference scripts
- JSON-based Java ↔ Python communication

---

## 📊 Dataset

The prepared EcoSort dataset contains:

| Dataset | Images |
|---|---:|
| Training | 976 |
| Validation | 212 |
| Test | 219 |
| **Total** | **1,407** |

**Annotated waste instances:** 3,414

The final held-out test set contains **219 images and 488 object instances**.

### Supported Classes

```text
0 → PLASTIC
1 → METAL
2 → GLASS
3 → PAPER_CARDBOARD
```

---

## 🎯 Detection Model

### Final Model

**YOLO11s @ 960 × 960**

| Metric | Result |
|---|---:|
| Precision | **47.8%** |
| Recall | **30.1%** |
| mAP@50 | **26.6%** |
| mAP@50–95 | **20.2%** |
| CPU Inference | **~760.5 ms/image** |
| End-to-End Throughput | **~1.29 FPS** |

### Per-Class Detection Performance

| Material | mAP@50 | mAP@50–95 |
|---|---:|---:|
| Plastic | 31.9% | 23.7% |
| Metal | **45.9%** | **36.9%** |
| Glass | 1.08% | 0.82% |
| Paper/Cardboard | 27.5% | 19.4% |

---

## 🧪 Baseline Comparison

| Model | Precision | Recall | mAP@50 | mAP@50–95 |
|---|---:|---:|---:|---:|
| YOLO11n @ 640 | 38.48% | **34.22%** | **30.59%** | 19.41% |
| YOLO11s @ 960 | **47.8%** | 30.1% | 26.6% | **20.2%** |

The larger YOLO11s configuration improved **precision and mAP@50–95**, while the YOLO11n baseline retained higher recall and mAP@50. The experiment therefore shows a model capacity/resolution trade-off rather than improvement across every metric.

---

## 🖼️ Segmentation Model

**YOLO11s-seg @ 640 × 640**

| Metric | Result |
|---|---:|
| Mask Precision | **32.4%** |
| Mask Recall | **32.4%** |
| Mask mAP@50 | **21.0%** |
| Mask mAP@50–95 | **14.5%** |

Segmentation masks are also merged class-wise to generate **semantic material maps**.

---

## 📐 Segmentation IoU

| Material | IoU |
|---|---:|
| Plastic | **46.98%** |
| Metal | **45.53%** |
| Glass | 3.37% |
| Paper/Cardboard | 19.82% |

Additional segmentation results:

| Metric | Result |
|---|---:|
| Mean Class IoU | **28.92%** |
| Global IoU | **39.71%** |
| Mean Image IoU | **37.47%** |
| Evaluation Runtime | **483.96 ms/image** |
| Evaluation Throughput | **2.07 FPS** |

---

## ♻️ Purity & Contamination Analysis

EcoSort calculates material composition using predicted segmentation-mask areas.

For a predicted dominant material:

```text
Purity =
Dominant Material Mask Area
──────────────────────────── × 100
 Total Predicted Mask Area
```

and:

```text
Contamination = 100 − Purity
```

This allows the application to distinguish relatively pure recyclable streams from mixed-material waste.

---

## 🛡️ Cross-Model Verification

Detection and segmentation can occasionally disagree, particularly for difficult or ambiguous images.

EcoSort therefore does not blindly trust a single model.

```text
Detection
     │
     ├──────────────┐
     │              │
     ▼              ▼
Segmentation   Confidence Check
     │              │
     └──────┬───────┘
            ▼
      Decision Layer
            │
      ┌─────┴─────┐
      ▼           ▼
    Agree      Uncertain /
      │        Disagreement
      ▼           │
   Result          ▼
             MANUAL REVIEW
```

Manual Review may be triggered when:

- detection confidence is insufficient;
- segmentation finds no supported region;
- detection and segmentation disagree;
- the input may be unsupported or ambiguous.

`MANUAL REVIEW` is **not a fifth trained material class**.

---

## 🖥️ JavaFX Application

The desktop application displays:

- uploaded waste image
- detected objects
- detection confidence
- segmentation result
- primary material
- material composition
- purity score
- contamination percentage
- model-decision information
- recycling recommendation
- inference time
- analysis history

Analysis can also be cancelled while inference is running so another image can be selected.

---

##
