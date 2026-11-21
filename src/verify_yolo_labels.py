import os
import random
from PIL import Image, ImageDraw


YOLO_DIR = "data/ecosort_yolo"

CLASS_NAMES = [
    "PLASTIC",
    "METAL",
    "GLASS",
    "PAPER_CARDBOARD"
]

OUTPUT_DIR = "results/yolo_label_check"

os.makedirs(OUTPUT_DIR, exist_ok=True)

random.seed(42)


def verify_split(split, sample_count=4):

    image_dir = os.path.join(
        YOLO_DIR,
        "images",
        split
    )

    label_dir = os.path.join(
        YOLO_DIR,
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

        draw = ImageDraw.Draw(image)

        width, height = image.size

        with open(
            label_path,
            "r",
            encoding="utf-8"
        ) as file:

            lines = file.readlines()

        for line in lines:

            values = line.strip().split()

            if len(values) != 5:
                continue

            class_id = int(values[0])

            center_x = float(values[1])
            center_y = float(values[2])

            box_width = float(values[3])
            box_height = float(values[4])

            # Convert normalized YOLO coordinates
            # back to pixel coordinates

            center_x *= width
            center_y *= height

            box_width *= width
            box_height *= height

            x1 = center_x - box_width / 2
            y1 = center_y - box_height / 2

            x2 = center_x + box_width / 2
            y2 = center_y + box_height / 2

            draw.rectangle(
                [x1, y1, x2, y2],
                outline="red",
                width=6
            )

            draw.text(
                (
                    x1,
                    max(0, y1 - 25)
                ),
                CLASS_NAMES[class_id],
                fill="red"
            )

        output_name = (
            f"{split}_{image_name}"
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
            "Created:",
            output_path
        )


print(
    "\n===== VERIFYING YOLO LABELS =====\n"
)

verify_split("train")
verify_split("val")
verify_split("test")

print(
    "\n===== YOLO LABEL CHECK COMPLETE ====="
)

print(
    "Open:",
    OUTPUT_DIR
)