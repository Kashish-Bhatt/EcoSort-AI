import csv
import json
import time
from pathlib import Path

import cv2
import numpy as np
from ultralytics import YOLO


ROOT = Path(__file__).resolve().parent.parent

IMAGE_DIR = ROOT / "data" / "robustness_test"

OUTPUT_DIR = ROOT / "results" / "robustness"
DETECTION_DIR = OUTPUT_DIR / "detection"
SEGMENTATION_DIR = OUTPUT_DIR / "segmentation"

DETECTION_DIR.mkdir(parents=True, exist_ok=True)
SEGMENTATION_DIR.mkdir(parents=True, exist_ok=True)

DETECTION_MODEL = (
    ROOT / "weights" / "exp2_yolo11s_960_best.pt"
)

SEGMENTATION_MODEL = (
    ROOT / "weights" / "ecosort_segmentation_best.pt"
)

CLASS_NAMES = {
    0: "PLASTIC",
    1: "METAL",
    2: "GLASS",
    3: "PAPER_CARDBOARD"
}

CONFIDENCE_THRESHOLD = 0.25


def analyse_segmentation(result):

    material_areas = {
        name: 0
        for name in CLASS_NAMES.values()
    }

    if (
        result.masks is None
        or result.boxes is None
    ):
        return (
            "NONE",
            0.0,
            0.0,
            material_areas
        )

    masks = result.masks.data.cpu().numpy()

    classes = (
        result.boxes.cls
        .cpu()
        .numpy()
        .astype(int)
    )

    confidences = (
        result.boxes.conf
        .cpu()
        .numpy()
    )

    order = np.argsort(-confidences)

    height, width = masks[0].shape

    claimed = np.zeros(
        (height, width),
        dtype=bool
    )

    for index in order:

        class_id = int(classes[index])

        material = CLASS_NAMES.get(
            class_id
        )

        if material is None:
            continue

        mask = masks[index] > 0.5

        unique = mask & ~claimed

        area = int(
            np.count_nonzero(unique)
        )

        material_areas[material] += area

        claimed |= unique

    total = sum(material_areas.values())

    if total == 0:
        return (
            "NONE",
            0.0,
            0.0,
            material_areas
        )

    dominant = max(
        material_areas,
        key=material_areas.get
    )

    purity = (
        material_areas[dominant]
        / total
        * 100.0
    )

    contamination = 100.0 - purity

    return (
        dominant,
        purity,
        contamination,
        material_areas
    )


def main():

    detection_model = YOLO(
        str(DETECTION_MODEL)
    )

    segmentation_model = YOLO(
        str(SEGMENTATION_MODEL)
    )

    extensions = {
        ".jpg",
        ".jpeg",
        ".png",
        ".bmp",
        ".webp"
    }

    images = sorted(
        p for p in IMAGE_DIR.iterdir()
        if p.suffix.lower() in extensions
    )

    rows = []

    print(
        "\n===== ECOSORT ROBUSTNESS TEST =====\n"
    )

    for number, image_path in enumerate(
        images,
        start=1
    ):

        print(
            f"[{number}/{len(images)}] "
            f"{image_path.name}"
        )

        # ------------------------------
        # DETECTION
        # ------------------------------

        start = time.perf_counter()

        detection_result = (
            detection_model.predict(
                source=str(image_path),
                conf=CONFIDENCE_THRESHOLD,
                imgsz=960,
                verbose=False
            )[0]
        )

        detection_ms = (
            time.perf_counter() - start
        ) * 1000.0

        detections = []

        if detection_result.boxes is not None:

            for box in detection_result.boxes:

                class_id = int(
                    box.cls[0].item()
                )

                confidence = float(
                    box.conf[0].item()
                )

                detections.append({
                    "material":
                        CLASS_NAMES.get(
                            class_id,
                            f"CLASS_{class_id}"
                        ),
                    "confidence":
                        round(
                            confidence,
                            4
                        )
                })

        if detections:

            best_detection = max(
                detections,
                key=lambda item:
                    item["confidence"]
            )

            detected_material = (
                best_detection["material"]
            )

            detection_confidence = (
                best_detection["confidence"]
            )

        else:

            detected_material = "NONE"
            detection_confidence = 0.0

        detection_image = (
            DETECTION_DIR
            / f"{image_path.stem}_detected.jpg"
        )

        cv2.imwrite(
            str(detection_image),
            detection_result.plot()
        )

        # ------------------------------
        # SEGMENTATION
        # ------------------------------

        start = time.perf_counter()

        segmentation_result = (
            segmentation_model.predict(
                source=str(image_path),
                conf=CONFIDENCE_THRESHOLD,
                imgsz=640,
                verbose=False
            )[0]
        )

        segmentation_ms = (
            time.perf_counter() - start
        ) * 1000.0

        (
            dominant,
            purity,
            contamination,
            material_areas
        ) = analyse_segmentation(
            segmentation_result
        )

        segmentation_image = (
            SEGMENTATION_DIR
            / f"{image_path.stem}_segmented.jpg"
        )

        cv2.imwrite(
            str(segmentation_image),
            segmentation_result.plot()
        )

        total_area = sum(
            material_areas.values()
        )

        composition = {}

        for material, area in (
            material_areas.items()
        ):

            if total_area > 0:

                percentage = (
                    area
                    / total_area
                    * 100.0
                )

            else:

                percentage = 0.0

            composition[material] = round(
                percentage,
                2
            )

        row = {
            "image":
                image_path.name,

            "detected_material":
                detected_material,

            "detection_confidence":
                round(
                    detection_confidence,
                    4
                ),

            "detection_count":
                len(detections),

            "segmentation_material":
                dominant,

            "purity_percent":
                round(
                    purity,
                    2
                ),

            "contamination_percent":
                round(
                    contamination,
                    2
                ),

            "plastic_percent":
                composition["PLASTIC"],

            "metal_percent":
                composition["METAL"],

            "glass_percent":
                composition["GLASS"],

            "paper_cardboard_percent":
                composition[
                    "PAPER_CARDBOARD"
                ],

            "detection_ms":
                round(
                    detection_ms,
                    2
                ),

            "segmentation_ms":
                round(
                    segmentation_ms,
                    2
                )
        }

        rows.append(row)

        print(
            f"    Detection: "
            f"{detected_material} "
            f"({detection_confidence:.2f})"
        )

        print(
            f"    Segmentation: "
            f"{dominant} | "
            f"Purity {purity:.1f}% | "
            f"Contamination "
            f"{contamination:.1f}%"
        )

    # ------------------------------
    # CSV
    # ------------------------------

    csv_path = (
        OUTPUT_DIR
        / "robustness_results.csv"
    )

    if rows:

        with open(
            csv_path,
            "w",
            newline="",
            encoding="utf-8"
        ) as file:

            writer = csv.DictWriter(
                file,
                fieldnames=rows[0].keys()
            )

            writer.writeheader()
            writer.writerows(rows)

    # ------------------------------
    # JSON
    # ------------------------------

    summary = {
        "images_tested":
            len(rows),

        "confidence_threshold":
            CONFIDENCE_THRESHOLD,

        "detection_model":
            str(DETECTION_MODEL),

        "segmentation_model":
            str(SEGMENTATION_MODEL),

        "results":
            rows
    }

    json_path = (
        OUTPUT_DIR
        / "robustness_summary.json"
    )

    with open(
        json_path,
        "w",
        encoding="utf-8"
    ) as file:

        json.dump(
            summary,
            file,
            indent=4
        )

    print(
        "\n===== ROBUSTNESS TEST COMPLETE ====="
    )

    print(
        f"Images tested: {len(rows)}"
    )

    print(
        f"CSV : {csv_path}"
    )

    print(
        f"JSON: {json_path}"
    )


if __name__ == "__main__":
    main()