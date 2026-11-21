import os
import shutil
from PIL import Image

SOURCE = "data/ecosort_yolo"
DESTINATION = "data/ecosort_yolo_colab"

MAX_SIZE = 1280
JPEG_QUALITY = 85

splits = ["train", "val", "test"]

print("\n===== PREPARING COLAB DATASET =====\n")

for split in splits:

    source_images = os.path.join(
        SOURCE, "images", split
    )

    source_labels = os.path.join(
        SOURCE, "labels", split
    )

    destination_images = os.path.join(
        DESTINATION, "images", split
    )

    destination_labels = os.path.join(
        DESTINATION, "labels", split
    )

    os.makedirs(
        destination_images,
        exist_ok=True
    )

    os.makedirs(
        destination_labels,
        exist_ok=True
    )

    image_files = [
        file
        for file in os.listdir(source_images)
        if file.lower().endswith(
            (".jpg", ".jpeg", ".png")
        )
    ]

    print(
        f"{split}: {len(image_files)} images"
    )

    for index, filename in enumerate(
            image_files,
            start=1):

        source_image_path = os.path.join(
            source_images,
            filename
        )

        destination_image_path = os.path.join(
            destination_images,
            filename
        )

        with Image.open(
                source_image_path) as image:

            image = image.convert("RGB")

            width, height = image.size

            scale = min(
                1.0,
                MAX_SIZE / max(width, height)
            )

            if scale < 1.0:

                new_width = int(
                    width * scale
                )

                new_height = int(
                    height * scale
                )

                image = image.resize(
                    (new_width, new_height),
                    Image.Resampling.LANCZOS
                )

            image.save(
                destination_image_path,
                "JPEG",
                quality=JPEG_QUALITY,
                optimize=True
            )

        label_name = (
            os.path.splitext(filename)[0]
            + ".txt"
        )

        shutil.copy2(
            os.path.join(
                source_labels,
                label_name
            ),
            os.path.join(
                destination_labels,
                label_name
            )
        )

        if index % 100 == 0:

            print(
                f"{split}: "
                f"{index}/{len(image_files)}"
            )


# Copy and rewrite YAML

yaml_content = """path: /content/ecosort_yolo_colab

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
    os.path.join(
        DESTINATION,
        "ecosort.yaml"
    ),
    "w",
    encoding="utf-8"
) as file:

    file.write(yaml_content)


print(
    "\n===== COLAB DATASET READY ====="
)

print(
    "Location:",
    DESTINATION
)