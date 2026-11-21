# EcoSort AI - Responsible Use and Limitations

## 1. Intended Use

EcoSort AI is an academic deep-learning prototype for waste sorting and recycling-quality analysis.

The system is designed to:

- detect supported recyclable materials,
- identify individual waste objects,
- segment material regions,
- estimate material composition,
- estimate purity,
- estimate contamination,
- provide recycling recommendations,
- retain analysis history for demonstration and evaluation.

## 2. Supported Materials

EcoSort currently supports four material categories:

- PLASTIC
- METAL
- GLASS
- PAPER_CARDBOARD

## 3. Unsupported Waste

Materials outside the supported classes may not be recognized correctly.

The system should not force an unsupported object into one of the four known classes when confidence is insufficient.

Low-confidence or unusual inputs should be treated as requiring manual review.

## 4. Prediction Uncertainty

Deep-learning predictions are probabilistic.

A high confidence score does not guarantee that a prediction is correct.

EcoSort therefore uses confidence information when presenting predictions and recycling guidance.

Where confidence is insufficient, manual verification should be preferred.

## 5. Purity and Contamination

Purity and contamination are model-based estimates.

They are calculated from detected/segmented material regions and therefore depend on segmentation quality.

These values are not laboratory measurements of chemical or physical material composition.

## 6. Potential Failure Conditions

Performance may decrease under conditions including:

- poor lighting,
- severe shadows,
- motion blur,
- low-resolution images,
- cluttered backgrounds,
- partially hidden objects,
- overlapping waste,
- very small objects,
- unusual viewing angles,
- transparent materials,
- reflective surfaces,
- objects substantially different from the training distribution.

## 7. Class-Specific Performance

Different material classes may have different detection performance.

Final class-wise metrics will therefore be reported rather than relying only on one overall accuracy value.

Classes with weaker performance will be explicitly identified in the final error analysis.

## 8. Human Oversight

EcoSort should not be used as the sole decision-maker for:

- safety-critical recycling operations,
- hazardous-material identification,
- regulatory compliance,
- industrial process control,
- financial or legal recycling certification.

Uncertain cases should be reviewed by a human operator.

## 9. Environmental Risk

Incorrect waste classification may contaminate recycling streams.

EcoSort therefore favors explicit uncertainty and manual-review recommendations over presenting uncertain predictions as guaranteed facts.

## 10. Privacy

The intended input is waste imagery.

Users should avoid uploading images containing unnecessary:

- faces,
- identification documents,
- private information,
- sensitive information.

## 11. Dataset Bias

The model can inherit limitations or biases from its training dataset.

Waste appearance may differ across:

- geographic regions,
- manufacturers,
- packaging styles,
- collection systems,
- lighting conditions,
- camera hardware.

Real-world robustness must therefore be evaluated separately.

## 12. Out-of-Distribution Inputs

EcoSort is not guaranteed to correctly recognize objects that are substantially different from its training distribution.

Out-of-distribution inputs may produce:

- low confidence,
- incorrect classification,
- missed detections,
- incorrect segmentation.

## 13. Academic Status

EcoSort AI is currently an educational and research prototype.

It is not certified as a commercial or industrial recycling-control system.

## 14. Responsible Deployment Recommendation

Industrial deployment should only be considered after:

- larger and more diverse dataset collection,
- stronger external validation,
- class-wise performance improvement,
- robustness testing,
- latency benchmarking,
- failure analysis,
- human-in-the-loop validation,
- monitoring for model drift.

The final project evaluation will include an evidence-based deployment recommendation.