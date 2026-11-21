import os
import random
from PIL import Image, ImageDraw


DATASET_DIR = "data/ecosort_segmentation"
OUTPUT_DIR = "results/segmentation_check"

CLASS_NAMES = [
    "PLASTIC",
    "METAL",
    "GLASS",
    "PAPER_CARDBOARD"
]

os.makedirs(OUTPUT_DIR, exist_ok=True)

random.seed(42)


def verify_split(split, sample_count=3):

    image_dir = os.path.join(
        DATASET_DIR,
        "images",
        split
    )

    label_dir = os.path.join(
        DATASET_DIR,
        "labels",
        split
    )

    images = [
        name
        for name in os.listdir(image_dir)
        if name.lower().endswith(
            (".jpg", ".jpeg", ".png")
        )
    ]

    samples = random.sample(
        images,
        min(sample_count, len(images))
    )

    for image_name in samples:

        image_path = os.path.join(
            image_dir,
            image_name
        )

        label_name = (
            os.path.splitext(image_name)[0]
            + ".txt"
        )

        label_path = os.path.join(
            label_dir,
            label_name
        )

        image = Image.open(
            image_path
        ).convert("RGB")

        width, height = image.size

        overlay = Image.new(
            "RGBA",
            image.size,
            (0, 0, 0, 0)
        )

        draw = ImageDraw.Draw(overlay)

        with open(
            label_path,
            "r",
            encoding="utf-8"
        ) as file:

            lines = file.readlines()

        for line in lines:

            values = line.strip().split()

            if len(values) < 7:
                continue

            class_id = int(values[0])

            coordinates = list(
                map(float, values[1:])
            )

            points = []

            for i in range(
                    0,
                    len(coordinates),
                    2):

                x = coordinates[i] * width
                y = coordinates[i + 1] * height

                points.append(
                    (x, y)
                )

            if len(points) < 3:
                continue

            # Semi-transparent mask
            draw.polygon(
                points,
                fill=(255, 0, 0, 70),
                outline=(255, 255, 0, 255)
            )

            # Class name
            x0, y0 = points[0]

            draw.text(
                (x0, y0),
                CLASS_NAMES[class_id],
                fill=(255, 255, 0, 255)
            )

        result = Image.alpha_composite(
            image.convert("RGBA"),
            overlay
        ).convert("RGB")

        output_name = (
            f"{split}_{image_name}"
        )

        output_path = os.path.join(
            OUTPUT_DIR,
            output_name
        )

        result.save(
            output_path,
            quality=90
        )

        print(
            "Created:",
            output_path
        )


print(
    "\n===== SEGMENTATION LABEL VERIFICATION =====\n"
)

verify_split("train")
verify_split("val")
verify_split("test")

print(
    "\n===== CHECK COMPLETE ====="
)

print(
    "Open:",
    OUTPUT_DIR
)
def find_most_objects():

    best_image = None
    best_split = None
    best_count = 0

    for split in ["train", "val", "test"]:

        label_dir = os.path.join(
            DATASET_DIR,
            "labels",
            split
        )

        for label_name in os.listdir(label_dir):

            if not label_name.endswith(".txt"):
                continue

            label_path = os.path.join(
                label_dir,
                label_name
            )

            with open(
                label_path,
                "r",
                encoding="utf-8"
            ) as file:

                count = sum(
                    1 for line in file
                    if line.strip()
                )

            if count > best_count:

                best_count = count
                best_split = split

                best_image = (
                    os.path.splitext(label_name)[0]
                    + ".jpg"
                )

    print("\nMOST OBJECTS FOUND")
    print("Split:", best_split)
    print("Image:", best_image)
    print("Objects:", best_count)


find_most_objects()