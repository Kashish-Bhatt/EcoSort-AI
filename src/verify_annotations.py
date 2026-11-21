import os
import json
import random

from PIL import Image, ImageDraw, ImageFont


DATASET_DIR = "data/ecosort_dataset"
IMAGE_DIR = os.path.join(DATASET_DIR, "images")
ANNOTATION_FILE = os.path.join(DATASET_DIR, "annotations.json")

OUTPUT_DIR = "results/annotation_check"

os.makedirs(OUTPUT_DIR, exist_ok=True)


# ------------------------------------------------------------
# LOAD ANNOTATIONS
# ------------------------------------------------------------

with open(ANNOTATION_FILE, "r", encoding="utf-8") as file:
    annotations = json.load(file)


print("Dataset images:", len(annotations))


# Fixed seed = reproducible sample selection
random.seed(42)

sample_count = min(12, len(annotations))

samples = random.sample(
    annotations,
    sample_count
)


# ------------------------------------------------------------
# DRAW SAMPLE ANNOTATIONS
# ------------------------------------------------------------

for sample_number, record in enumerate(samples, start=1):

    image_path = os.path.join(
        IMAGE_DIR,
        record["image"]
    )

    image = Image.open(image_path).convert("RGB")

    draw = ImageDraw.Draw(image)


    for obj in record["objects"]:

        class_name = obj["class_name"]
        bbox = obj["bbox"]
        polygon = obj["segmentation"]


        # ----------------------------------------------------
        # BOUNDING BOX
        # TACO bbox format = [x, y, width, height]
        # ----------------------------------------------------

        x = bbox[0]
        y = bbox[1]
        width = bbox[2]
        height = bbox[3]

        x2 = x + width
        y2 = y + height


        draw.rectangle(
            [x, y, x2, y2],
            outline="red",
            width=6
        )


        # ----------------------------------------------------
        # CLASS LABEL
        # ----------------------------------------------------

        text_position = (
            x,
            max(0, y - 25)
        )

        draw.text(
            text_position,
            class_name,
            fill="red"
        )


        # ----------------------------------------------------
        # SEGMENTATION POLYGON
        # ----------------------------------------------------

        if polygon:

            try:

                points = [
                    (float(point[0]), float(point[1]))
                    for point in polygon
                ]

                if len(points) >= 3:

                    draw.line(
                        points + [points[0]],
                        fill="yellow",
                        width=5
                    )

            except Exception as error:

                print(
                    "Polygon drawing warning:",
                    record["image"],
                    error
                )


    # --------------------------------------------------------
    # SAVE VISUALIZATION
    # --------------------------------------------------------

    output_name = (
        f"check_{sample_number:02d}_"
        f"{record['image']}"
    )

    output_path = os.path.join(
        OUTPUT_DIR,
        output_name
    )

    image.save(
        output_path,
        quality=90
    )

    print(
        f"Created {sample_number}/{sample_count}:",
        output_path
    )


print("\n================================")
print("ANNOTATION CHECK COMPLETE")
print("================================")

print(
    "Open this folder:",
    OUTPUT_DIR
)