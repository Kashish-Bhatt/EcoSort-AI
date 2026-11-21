import os
import json
import shutil


DATASET_DIR = "data/ecosort_dataset"
IMAGE_DIR = os.path.join(DATASET_DIR, "images")
SPLIT_DIR = os.path.join(DATASET_DIR, "splits")

YOLO_DIR = "data/ecosort_yolo"

CLASS_NAMES = [
    "PLASTIC",
    "METAL",
    "GLASS",
    "PAPER_CARDBOARD"
]


def create_directories():

    for split in ["train", "val", "test"]:

        os.makedirs(
            os.path.join(
                YOLO_DIR,
                "images",
                split
            ),
            exist_ok=True
        )

        os.makedirs(
            os.path.join(
                YOLO_DIR,
                "labels",
                split
            ),
            exist_ok=True
        )


def convert_bbox(
        bbox,
        image_width,
        image_height):

    # TACO:
    # [x, y, width, height]

    x = bbox[0]
    y = bbox[1]
    width = bbox[2]
    height = bbox[3]

    # YOLO:
    # center_x center_y width height
    # all normalized 0-1

    center_x = (
        x + width / 2.0
    ) / image_width

    center_y = (
        y + height / 2.0
    ) / image_height

    normalized_width = (
        width / image_width
    )

    normalized_height = (
        height / image_height
    )

    return (
        center_x,
        center_y,
        normalized_width,
        normalized_height
    )


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


    object_count = 0


    for index, record in enumerate(
            records,
            start=1):

        image_name = record["image"]

        source_image = os.path.join(
            IMAGE_DIR,
            image_name
        )

        destination_image = os.path.join(
            YOLO_DIR,
            "images",
            split,
            image_name
        )


        # Copy image only if not already there
        if not os.path.exists(
                destination_image):

            shutil.copy2(
                source_image,
                destination_image
            )


        label_name = (
            os.path.splitext(
                image_name
            )[0]
            + ".txt"
        )


        label_path = os.path.join(
            YOLO_DIR,
            "labels",
            split,
            label_name
        )


        with open(
            label_path,
            "w",
            encoding="utf-8"
        ) as label_file:


            for obj in record["objects"]:

                class_id = obj["class_id"]

                bbox = obj["bbox"]


                (
                    center_x,
                    center_y,
                    width,
                    height
                ) = convert_bbox(
                    bbox,
                    record["width"],
                    record["height"]
                )


                # Safety validation

                values = [
                    center_x,
                    center_y,
                    width,
                    height
                ]


                if not all(
                    0.0 <= value <= 1.0
                    for value in values
                ):

                    print(
                        "WARNING invalid box:",
                        image_name,
                        bbox
                    )

                    continue


                label_file.write(

                    f"{class_id} "
                    f"{center_x:.6f} "
                    f"{center_y:.6f} "
                    f"{width:.6f} "
                    f"{height:.6f}\n"
                )


                object_count += 1


        if index % 100 == 0:

            print(
                f"{split}: "
                f"{index}/{len(records)}"
            )


    print(
        f"{split} objects: "
        f"{object_count}"
    )


def create_yaml():

    yaml_path = os.path.join(
        YOLO_DIR,
        "ecosort.yaml"
    )


    absolute_path = os.path.abspath(
        YOLO_DIR
    ).replace("\\", "/")


    content = f"""path: {absolute_path}

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

        file.write(content)


    print(
        "\nYOLO configuration:",
        yaml_path
    )


def main():

    print(
        "\n===== PREPARING ECOSORT YOLO DATASET ====="
    )

    create_directories()

    process_split("train")
    process_split("val")
    process_split("test")

    create_yaml()


    print(
        "\n===================================="
    )

    print(
        "YOLO DATASET PREPARATION COMPLETE"
    )

    print(
        "===================================="
    )

    print(
        "\nDataset location:",
        YOLO_DIR
    )


if __name__ == "__main__":
    main()