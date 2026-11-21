import os
import json
import shutil


# ============================================================
# PATHS
# ============================================================

DATASET_DIR = "data/ecosort_dataset"
IMAGE_DIR = os.path.join(DATASET_DIR, "images")
SPLIT_DIR = os.path.join(DATASET_DIR, "splits")

OUTPUT_DIR = "data/ecosort_segmentation"

CLASS_NAMES = [
    "PLASTIC",
    "METAL",
    "GLASS",
    "PAPER_CARDBOARD"
]


# ============================================================
# CREATE OUTPUT DIRECTORIES
# ============================================================

def create_directories():

    for split in ["train", "val", "test"]:

        os.makedirs(
            os.path.join(
                OUTPUT_DIR,
                "images",
                split
            ),
            exist_ok=True
        )

        os.makedirs(
            os.path.join(
                OUTPUT_DIR,
                "labels",
                split
            ),
            exist_ok=True
        )


# ============================================================
# NORMALIZE SEGMENTATION POLYGON
#
# Original polygon:
# [[x1,y1], [x2,y2], ...]
#
# YOLO segmentation:
# class_id x1 y1 x2 y2 x3 y3 ...
#
# Coordinates must be normalized 0-1.
# ============================================================

def normalize_polygon(
        polygon,
        image_width,
        image_height):

    normalized = []

    for point in polygon:

        if len(point) < 2:
            continue

        x = float(point[0])
        y = float(point[1])

        x = x / image_width
        y = y / image_height

        # Numerical safety
        x = max(0.0, min(1.0, x))
        y = max(0.0, min(1.0, y))

        normalized.extend([
            x,
            y
        ])

    return normalized


# ============================================================
# PROCESS ONE SPLIT
# ============================================================

def process_split(split):

    split_file = os.path.join(
        SPLIT_DIR,
        f"{split}.json"
    )

    with open(
        split_file,
        "r",
        encoding="utf-8"
    ) as file:

        records = json.load(file)


    print(
        f"\nProcessing {split}: "
        f"{len(records)} images"
    )


    image_count = 0
    segmentation_count = 0
    skipped_polygons = 0


    for index, record in enumerate(
            records,
            start=1):

        image_name = record["image"]

        source_image = os.path.join(
            IMAGE_DIR,
            image_name
        )

        destination_image = os.path.join(
            OUTPUT_DIR,
            "images",
            split,
            image_name
        )


        # ----------------------------------------------------
        # LABEL FILE
        # ----------------------------------------------------

        label_name = (
            os.path.splitext(image_name)[0]
            + ".txt"
        )

        label_path = os.path.join(
            OUTPUT_DIR,
            "labels",
            split,
            label_name
        )


        valid_segmentations = []


        # ----------------------------------------------------
        # PROCESS OBJECT POLYGONS
        # ----------------------------------------------------

        for obj in record["objects"]:

            polygon = obj.get(
                "segmentation"
            )

            class_id = obj[
                "class_id"
            ]


            # Polygon needs at least
            # three points.
            if (
                polygon is None
                or len(polygon) < 3
            ):

                skipped_polygons += 1
                continue


            normalized = normalize_polygon(
                polygon,
                record["width"],
                record["height"]
            )


            # Three points =
            # six coordinate values.
            if len(normalized) < 6:

                skipped_polygons += 1
                continue


            valid_segmentations.append(
                (
                    class_id,
                    normalized
                )
            )


        # ----------------------------------------------------
        # SAVE ONLY IMAGES WITH AT LEAST ONE VALID MASK
        # ----------------------------------------------------

        if not valid_segmentations:
            continue


        if not os.path.exists(
                destination_image):

            shutil.copy2(
                source_image,
                destination_image
            )


        with open(
            label_path,
            "w",
            encoding="utf-8"
        ) as label_file:

            for (
                class_id,
                normalized
            ) in valid_segmentations:

                coordinates = " ".join(
                    f"{value:.6f}"
                    for value in normalized
                )

                label_file.write(
                    f"{class_id} "
                    f"{coordinates}\n"
                )

                segmentation_count += 1


        image_count += 1


        if index % 100 == 0:

            print(
                f"{split}: "
                f"{index}/{len(records)}"
            )


    print(
        f"{split} segmentation images:",
        image_count
    )

    print(
        f"{split} segmentation objects:",
        segmentation_count
    )

    print(
        f"{split} skipped polygons:",
        skipped_polygons
    )


# ============================================================
# CREATE YAML
# ============================================================

def create_yaml():

    yaml_path = os.path.join(
        OUTPUT_DIR,
        "ecosort_seg.yaml"
    )

    absolute_path = os.path.abspath(
        OUTPUT_DIR
    ).replace("\\", "/")


    yaml_content = f"""path: {absolute_path}

train: images/train
val: images/val
test: images/test

names:
  0: PLASTIC
  1: METAL
  2: GLASS
  3: PAPER_CARDBOARD
"""


    with open(
        yaml_path,
        "w",
        encoding="utf-8"
    ) as file:

        file.write(
            yaml_content
        )


    print(
        "\nConfiguration:",
        yaml_path
    )


# ============================================================
# MAIN
# ============================================================

def main():

    print(
        "\n======================================"
    )

    print(
        "PREPARING ECOSORT SEGMENTATION DATASET"
    )

    print(
        "======================================"
    )


    create_directories()

    process_split("train")
    process_split("val")
    process_split("test")

    create_yaml()


    print(
        "\n======================================"
    )

    print(
        "SEGMENTATION DATASET COMPLETE"
    )

    print(
        "======================================"
    )

    print(
        "\nLocation:",
        OUTPUT_DIR
    )


if __name__ == "__main__":
    main()