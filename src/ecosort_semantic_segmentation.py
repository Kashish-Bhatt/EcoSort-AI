import argparse
import json
from pathlib import Path

import cv2
import numpy as np
from ultralytics import YOLO


CLASS_NAMES = {
    0: "PLASTIC",
    1: "METAL",
    2: "GLASS",
    3: "PAPER_CARDBOARD"
}


def main():
    parser = argparse.ArgumentParser()

    parser.add_argument("--image", required=True)
    parser.add_argument(
        "--model",
        default="weights/ecosort_segmentation_best.pt"
    )
    parser.add_argument("--confidence", type=float, default=0.25)
    parser.add_argument("--imgsz", type=int, default=640)
    parser.add_argument(
        "--output",
        default="results/semantic_segmentation"
    )

    args = parser.parse_args()

    image_path = Path(args.image)
    output_dir = Path(args.output)
    output_dir.mkdir(parents=True, exist_ok=True)

    image = cv2.imread(str(image_path))

    if image is None:
        raise FileNotFoundError(image_path)

    height, width = image.shape[:2]

    model = YOLO(args.model)

    result = model.predict(
        source=str(image_path),
        conf=args.confidence,
        imgsz=args.imgsz,
        verbose=False
    )[0]

    # Each pixel stores its semantic class.
    # 0 = background
    # 1..4 = EcoSort material classes
    semantic_map = np.zeros(
        (height, width),
        dtype=np.uint8
    )

    confidence_map = np.zeros(
        (height, width),
        dtype=np.float32
    )

    if (
        result.masks is not None
        and result.boxes is not None
    ):
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

        # Higher-confidence instance wins where masks overlap.
        order = np.argsort(confidences)

        for index in order:
            class_id = int(classes[index])
            confidence = float(confidences[index])

            mask = cv2.resize(
                masks[index],
                (width, height),
                interpolation=cv2.INTER_NEAREST
            )

            pixels = mask > 0.5

            replace = (
                pixels
                & (confidence >= confidence_map)
            )

            semantic_map[replace] = class_id + 1
            confidence_map[replace] = confidence

    # Visualization only.
    palette = np.array([
        [0, 0, 0],
        [255, 0, 255],
        [0, 255, 255],
        [255, 255, 0],
        [0, 165, 255]
    ], dtype=np.uint8)

    semantic_visual = palette[semantic_map]

    overlay = cv2.addWeighted(
        image,
        0.55,
        semantic_visual,
        0.45,
        0
    )

    map_path = (
        output_dir
        / f"{image_path.stem}_semantic_map.png"
    )

    overlay_path = (
        output_dir
        / f"{image_path.stem}_semantic_overlay.jpg"
    )

    cv2.imwrite(
        str(map_path),
        semantic_visual
    )

    cv2.imwrite(
        str(overlay_path),
        overlay
    )

    pixel_counts = {}

    total_waste_pixels = int(
        np.count_nonzero(semantic_map)
    )

    for class_id, name in CLASS_NAMES.items():
        count = int(
            np.count_nonzero(
                semantic_map == class_id + 1
            )
        )

        percentage = (
            count / total_waste_pixels * 100.0
            if total_waste_pixels > 0
            else 0.0
        )

        pixel_counts[name] = {
            "pixels": count,
            "percentage": round(percentage, 4)
        }

    response = {
        "success": True,
        "type": "semantic_material_segmentation",
        "image": str(image_path.resolve()),
        "semantic_map": str(map_path.resolve()),
        "semantic_overlay": str(overlay_path.resolve()),
        "total_waste_pixels": total_waste_pixels,
        "classes": pixel_counts
    }

    print(
        json.dumps(
            response,
            indent=2
        )
    )


if __name__ == "__main__":
    main()