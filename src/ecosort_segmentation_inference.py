import argparse
import json
import os
import sys
import time
from pathlib import Path

os.environ["YOLO_VERBOSE"] = "False"

import cv2
import numpy as np
from ultralytics import YOLO


CLASS_NAMES = {
    0: "PLASTIC",
    1: "METAL",
    2: "GLASS",
    3: "PAPER_CARDBOARD"
}


def fail(message):
    print(json.dumps({
        "success": False,
        "error": str(message)
    }))
    sys.exit(1)


def main():

    parser = argparse.ArgumentParser()

    parser.add_argument(
        "--image",
        required=True
    )

    parser.add_argument(
        "--model",
        required=True
    )

    parser.add_argument(
        "--confidence",
        type=float,
        default=0.25
    )

    parser.add_argument(
        "--imgsz",
        type=int,
        default=640
    )

    parser.add_argument(
        "--output",
        default="results/segmentation_inference"
    )

    args = parser.parse_args()

    image_path = Path(args.image)
    model_path = Path(args.model)
    output_dir = Path(args.output)

    if not image_path.exists():
        fail(f"Image not found: {image_path}")

    if not model_path.exists():
        fail(
            f"Segmentation model not found: {model_path}"
        )

    output_dir.mkdir(
        parents=True,
        exist_ok=True
    )

    try:

        start = time.perf_counter()

        model = YOLO(
            str(model_path)
        )

        results = model.predict(
            source=str(image_path),
            conf=args.confidence,
            imgsz=args.imgsz,
            save=False,
            verbose=False
        )

        if not results:
            fail(
                "Segmentation model returned no result."
            )

        result = results[0]

        material_areas = {
            name: 0
            for name in CLASS_NAMES.values()
        }

        instances = []

        # --------------------------------------------------
        # NON-OVERLAPPING PIXEL ASSIGNMENT
        # --------------------------------------------------

        if (
            result.masks is not None
            and result.boxes is not None
        ):

            masks = (
                result.masks.data
                .cpu()
                .numpy()
            )

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

            # Process highest-confidence predictions first.
            order = np.argsort(
                -confidences
            )

            height, width = masks[0].shape

            claimed_pixels = np.zeros(
                (height, width),
                dtype=bool
            )

            for index in order:

                mask = masks[index]

                class_id = int(
                    classes[index]
                )

                material = CLASS_NAMES.get(
                    class_id,
                    f"CLASS_{class_id}"
                )

                confidence = float(
                    confidences[index]
                )

                binary_mask = (
                    mask > 0.5
                )

                # Remove pixels already assigned to a
                # higher-confidence prediction.
                unique_mask = (
                    binary_mask
                    & ~claimed_pixels
                )

                unique_area = int(
                    np.count_nonzero(
                        unique_mask
                    )
                )

                original_area = int(
                    np.count_nonzero(
                        binary_mask
                    )
                )

                overlap_area = (
                    original_area
                    - unique_area
                )

                if unique_area > 0:

                    material_areas[
                        material
                    ] += unique_area

                    claimed_pixels |= (
                        unique_mask
                    )

                instances.append({
                    "class_id":
                        class_id,

                    "material":
                        material,

                    "confidence":
                        round(
                            confidence,
                            6
                        ),

                    "mask_area":
                        unique_area,

                    "original_mask_area":
                        original_area,

                    "overlap_removed":
                        overlap_area
                })

        # --------------------------------------------------
        # COMPOSITION
        # --------------------------------------------------

        total_waste_area = sum(
            material_areas.values()
        )

        composition = {}

        if total_waste_area > 0:

            for (
                material,
                area
            ) in material_areas.items():

                composition[
                    material
                ] = round(
                    (
                        area
                        / total_waste_area
                    )
                    * 100.0,
                    4
                )

        else:

            for material in material_areas:
                composition[
                    material
                ] = 0.0

        # --------------------------------------------------
        # PURITY + CONTAMINATION
        # --------------------------------------------------

        if total_waste_area > 0:

            dominant_material = max(
                material_areas,
                key=material_areas.get
            )

            dominant_area = (
                material_areas[
                    dominant_material
                ]
            )

            purity = (
                dominant_area
                / total_waste_area
            ) * 100.0

            contamination = (
                100.0 - purity
            )

        else:

            dominant_material = "UNKNOWN"
            purity = 0.0
            contamination = 0.0

        # --------------------------------------------------
        # SAVE VISUAL SEGMENTATION
        # --------------------------------------------------

        plotted = result.plot()

        output_image = (
            output_dir
            / (
                image_path.stem
                + "_segmented.jpg"
            )
        )

        saved = cv2.imwrite(
            str(output_image),
            plotted
        )

        if not saved:
            fail(
                "Could not save segmented image."
            )

        # --------------------------------------------------
        # TIMING
        # --------------------------------------------------

        elapsed_ms = (
            time.perf_counter()
            - start
        ) * 1000.0

        # --------------------------------------------------
        # RESPONSE
        # --------------------------------------------------

        response = {

            "success": True,

            "image":
                str(
                    image_path.resolve()
                ),

            "model":
                str(
                    model_path.resolve()
                ),

            "segmented_image":
                str(
                    output_image.resolve()
                ),

            "instance_count":
                len(instances),

            "dominant_material":
                dominant_material,

            "purity":
                round(
                    purity,
                    4
                ),

            "contamination":
                round(
                    contamination,
                    4
                ),

            "total_mask_area":
                int(
                    total_waste_area
                ),

            "material_areas": {
                key: int(value)
                for key, value
                in material_areas.items()
            },

            "composition":
                composition,

            "instances":
                instances,

            "pipeline_time_ms":
                round(
                    elapsed_ms,
                    2
                )
        }

        print(
            json.dumps(
                response,
                ensure_ascii=False
            )
        )

    except Exception as exception:

        fail(exception)


if __name__ == "__main__":
    main()