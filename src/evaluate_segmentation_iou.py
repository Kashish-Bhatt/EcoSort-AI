import json
import time
from pathlib import Path

import cv2
import numpy as np
from ultralytics import YOLO


PROJECT_ROOT = Path(__file__).resolve().parent.parent

MODEL_PATH = (
    PROJECT_ROOT
    / "weights"
    / "ecosort_segmentation_best.pt"
)

IMAGE_DIR = (
    PROJECT_ROOT
    / "data"
    / "ecosort_segmentation"
    / "images"
    / "test"
)

LABEL_DIR = (
    PROJECT_ROOT
    / "data"
    / "ecosort_segmentation"
    / "labels"
    / "test"
)

OUTPUT_DIR = (
    PROJECT_ROOT
    / "results"
    / "final_evaluation"
)

OUTPUT_DIR.mkdir(
    parents=True,
    exist_ok=True
)


CLASS_NAMES = {
    0: "PLASTIC",
    1: "METAL",
    2: "GLASS",
    3: "PAPER_CARDBOARD"
}


def load_ground_truth(
    label_path,
    width,
    height
):

    masks = {
        class_id: np.zeros(
            (height, width),
            dtype=np.uint8
        )
        for class_id in CLASS_NAMES
    }

    if not label_path.exists():
        return masks

    with open(
        label_path,
        "r",
        encoding="utf-8"
    ) as file:

        for line in file:

            values = line.strip().split()

            if len(values) < 7:
                continue

            class_id = int(
                float(values[0])
            )

            coordinates = list(
                map(
                    float,
                    values[1:]
                )
            )

            points = []

            for index in range(
                0,
                len(coordinates),
                2
            ):

                x = int(
                    coordinates[index]
                    * width
                )

                y = int(
                    coordinates[index + 1]
                    * height
                )

                x = max(
                    0,
                    min(width - 1, x)
                )

                y = max(
                    0,
                    min(height - 1, y)
                )

                points.append(
                    [x, y]
                )

            polygon = np.array(
                points,
                dtype=np.int32
            )

            cv2.fillPoly(
                masks[class_id],
                [polygon],
                1
            )

    return masks


def predicted_masks(
    result,
    width,
    height
):

    masks = {
        class_id: np.zeros(
            (height, width),
            dtype=np.uint8
        )
        for class_id in CLASS_NAMES
    }

    if (
        result.masks is None
        or result.boxes is None
    ):
        return masks

    prediction_masks = (
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

    order = np.argsort(
        -confidences
    )

    claimed = np.zeros(
        (height, width),
        dtype=bool
    )

    for index in order:

        class_id = int(
            classes[index]
        )

        if class_id not in masks:
            continue

        mask = prediction_masks[index]

        mask = cv2.resize(
            mask,
            (width, height),
            interpolation=cv2.INTER_NEAREST
        )

        binary = (
            mask > 0.5
        )

        unique = (
            binary
            & ~claimed
        )

        masks[class_id][unique] = 1

        claimed |= unique

    return masks


def main():

    print(
        "\n===== ECOSORT SEGMENTATION IoU EVALUATION =====\n"
    )

    model = YOLO(
        str(MODEL_PATH)
    )

    image_extensions = {
        ".jpg",
        ".jpeg",
        ".png",
        ".bmp",
        ".webp"
    }

    image_paths = sorted(
        path
        for path in IMAGE_DIR.iterdir()
        if path.suffix.lower()
        in image_extensions
    )

    intersections = {
        class_id: 0
        for class_id in CLASS_NAMES
    }

    unions = {
        class_id: 0
        for class_id in CLASS_NAMES
    }

    image_ious = []

    inference_times = []

    evaluated = 0

    for number, image_path in enumerate(
        image_paths,
        start=1
    ):

        image = cv2.imread(
            str(image_path)
        )

        if image is None:
            continue

        height, width = image.shape[:2]

        label_path = (
            LABEL_DIR
            / (
                image_path.stem
                + ".txt"
            )
        )

        gt = load_ground_truth(
            label_path,
            width,
            height
        )

        start = time.perf_counter()

        result = model.predict(
            source=str(image_path),
            conf=0.25,
            imgsz=640,
            verbose=False
        )[0]

        inference_ms = (
            time.perf_counter()
            - start
        ) * 1000.0

        inference_times.append(
            inference_ms
        )

        pred = predicted_masks(
            result,
            width,
            height
        )

        per_image_intersection = 0
        per_image_union = 0

        for class_id in CLASS_NAMES:

            gt_mask = (
                gt[class_id] > 0
            )

            pred_mask = (
                pred[class_id] > 0
            )

            intersection = int(
                np.logical_and(
                    gt_mask,
                    pred_mask
                ).sum()
            )

            union = int(
                np.logical_or(
                    gt_mask,
                    pred_mask
                ).sum()
            )

            intersections[
                class_id
            ] += intersection

            unions[
                class_id
            ] += union

            per_image_intersection += (
                intersection
            )

            per_image_union += union

        if per_image_union > 0:

            image_ious.append(
                per_image_intersection
                / per_image_union
            )

        evaluated += 1

        if (
            number % 25 == 0
            or number == len(image_paths)
        ):

            print(
                f"Processed "
                f"{number}/"
                f"{len(image_paths)}"
            )

    class_ious = {}

    valid_class_ious = []

    for class_id, name in (
        CLASS_NAMES.items()
    ):

        union = unions[class_id]

        if union > 0:

            iou = (
                intersections[class_id]
                / union
            )

            valid_class_ious.append(
                iou
            )

        else:
            iou = 0.0

        class_ious[name] = round(
            iou,
            6
        )

    mean_iou = (
        float(
            np.mean(
                valid_class_ious
            )
        )
        if valid_class_ious
        else 0.0
    )

    global_intersection = sum(
        intersections.values()
    )

    global_union = sum(
        unions.values()
    )

    global_iou = (
        global_intersection
        / global_union
        if global_union > 0
        else 0.0
    )

    mean_image_iou = (
        float(
            np.mean(
                image_ious
            )
        )
        if image_ious
        else 0.0
    )

    average_inference_ms = (
        float(
            np.mean(
                inference_times
            )
        )
        if inference_times
        else 0.0
    )

    fps = (
        1000.0
        / average_inference_ms
        if average_inference_ms > 0
        else 0.0
    )

    summary = {
        "evaluated_images":
            evaluated,

        "class_iou":
            class_ious,

        "mean_class_iou":
            round(
                mean_iou,
                6
            ),

        "global_iou":
            round(
                global_iou,
                6
            ),

        "mean_image_iou":
            round(
                mean_image_iou,
                6
            ),

        "average_inference_ms":
            round(
                average_inference_ms,
                3
            ),

        "fps":
            round(
                fps,
                3
            )
    }

    output_file = (
        OUTPUT_DIR
        / "segmentation_iou.json"
    )

    with open(
        output_file,
        "w",
        encoding="utf-8"
    ) as file:

        json.dump(
            summary,
            file,
            indent=4
        )

    print(
        "\n===== FINAL IoU RESULTS ====="
    )

    for name, value in (
        class_ious.items()
    ):

        print(
            f"{name:18s}: "
            f"{value:.4f}"
        )

    print(
        f"\nMean class IoU : "
        f"{mean_iou:.4f}"
    )

    print(
        f"Global IoU     : "
        f"{global_iou:.4f}"
    )

    print(
        f"Mean image IoU : "
        f"{mean_image_iou:.4f}"
    )

    print(
        f"Average runtime: "
        f"{average_inference_ms:.2f} ms"
    )

    print(
        f"FPS            : "
        f"{fps:.2f}"
    )

    print(
        "\nSaved to:"
    )

    print(
        output_file
    )


if __name__ == "__main__":
    main()