import argparse
import json
import os
import sys
import time
from pathlib import Path

# Prevent Ultralytics logs from contaminating stdout.
# stdout must contain JSON only.
os.environ["YOLO_VERBOSE"] = "False"

from ultralytics import YOLO


CLASS_NAMES = {
    0: "PLASTIC",
    1: "METAL",
    2: "GLASS",
    3: "PAPER_CARDBOARD"
}


def fail(message):
    print(
        json.dumps({
            "success": False,
            "error": message
        })
    )
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
        default=960
    )

    parser.add_argument(
        "--output",
        default="results/inference"
    )

    args = parser.parse_args()


    image_path = Path(args.image)
    model_path = Path(args.model)
    output_dir = Path(args.output)


    if not image_path.exists():
        fail(
            f"Image not found: {image_path}"
        )

    if not model_path.exists():
        fail(
            f"Model not found: {model_path}"
        )


    output_dir.mkdir(
        parents=True,
        exist_ok=True
    )


    try:

        start_time = time.perf_counter()

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
                "Model returned no result."
            )


        result = results[0]

        detections = []


        # ==============================================
        # DETECTION RESULTS
        # ==============================================

        if result.boxes is not None:

            for box in result.boxes:

                class_id = int(
                    box.cls[0].item()
                )

                confidence = float(
                    box.conf[0].item()
                )

                coordinates = (
                    box.xyxy[0]
                    .cpu()
                    .tolist()
                )

                detections.append({

                    "class_id":
                        class_id,

                    "material":
                        CLASS_NAMES.get(
                            class_id,
                            f"CLASS_{class_id}"
                        ),

                    "confidence":
                        round(
                            confidence,
                            6
                        ),

                    "bbox": [
                        round(
                            float(value),
                            2
                        )
                        for value
                        in coordinates
                    ]
                })


        # ==============================================
        # SAVE ANNOTATED IMAGE
        # ==============================================

        annotated_image = result.plot()

        output_image_path = (
            output_dir
            / (
                image_path.stem
                + "_annotated.jpg"
            )
        )


        # result.plot() returns a BGR numpy array.
        import cv2

        cv2.imwrite(
            str(output_image_path),
            annotated_image
        )


        elapsed_ms = (
            time.perf_counter()
            - start_time
        ) * 1000.0


        # ==============================================
        # SUMMARY
        # ==============================================

        class_counts = {
            material: 0
            for material
            in CLASS_NAMES.values()
        }


        for detection in detections:

            class_counts[
                detection["material"]
            ] += 1


        if detections:

            highest_detection = max(
                detections,
                key=lambda item:
                    item["confidence"]
            )

            highest_confidence = (
                highest_detection[
                    "confidence"
                ]
            )

            highest_material = (
                highest_detection[
                    "material"
                ]
            )

        else:

            highest_confidence = 0.0
            highest_material = "UNKNOWN"


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

            "annotated_image":
                str(
                    output_image_path.resolve()
                ),

            "image_size": {
                "width":
                    int(
                        result.orig_shape[1]
                    ),

                "height":
                    int(
                        result.orig_shape[0]
                    )
            },

            "detection_count":
                len(detections),

            "highest_confidence":
                round(
                    highest_confidence,
                    6
                ),

            "highest_material":
                highest_material,

            "class_counts":
                class_counts,

            "detections":
                detections,

            "inference_time_ms":
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

        fail(
            str(exception)
        )


if __name__ == "__main__":
    main()