from pathlib import Path
from PIL import Image
import shutil

ROOT = Path(__file__).resolve().parent.parent

SOURCE = ROOT / "data" / "ecosort_v2"
OUTPUT = ROOT / "data" / "ecosort_v2_kaggle"

MAX_SIZE = 1280

IMAGE_EXTENSIONS = {
    ".jpg",
    ".jpeg",
    ".png",
    ".webp",
    ".bmp"
}


def main():

    if OUTPUT.exists():
        shutil.rmtree(OUTPUT)

    print("===== PREPARING ECOSORT V2 FOR KAGGLE =====")

    for split in ("train", "val", "test"):

        source_images = SOURCE / "images" / split
        source_labels = SOURCE / "labels" / split

        output_images = OUTPUT / "images" / split
        output_labels = OUTPUT / "labels" / split

        output_images.mkdir(
            parents=True,
            exist_ok=True
        )

        output_labels.mkdir(
            parents=True,
            exist_ok=True
        )

        images = [
            p for p in source_images.iterdir()
            if p.suffix.lower() in IMAGE_EXTENSIONS
        ]

        print(
            f"\n{split.upper()}: "
            f"{len(images)} images"
        )

        for index, image_path in enumerate(
            images,
            start=1
        ):

            destination = (
                output_images
                / f"{image_path.stem}.jpg"
            )

            with Image.open(image_path) as image:

                image = image.convert("RGB")

                image.thumbnail(
                    (MAX_SIZE, MAX_SIZE),
                    Image.Resampling.LANCZOS
                )

                image.save(
                    destination,
                    "JPEG",
                    quality=90,
                    optimize=True
                )

            label = (
                source_labels
                / f"{image_path.stem}.txt"
            )

            if not label.exists():
                raise FileNotFoundError(
                    f"Missing label: {label}"
                )

            shutil.copy2(
                label,
                output_labels
                / label.name
            )

            if (
                index % 250 == 0
                or index == len(images)
            ):

                print(
                    f"  Processed "
                    f"{index}/{len(images)}"
                )

    # Copy metadata files

    shutil.copy2(
        SOURCE / "dataset_report.json",
        OUTPUT / "dataset_report.json"
    )

    yaml_text = f"""path: /kaggle/working/ecosort_v2

train: images/train
val: images/val
test: images/test

names:
  0: PLASTIC
  1: METAL
  2: GLASS
  3: PAPER_CARDBOARD
"""

    (
        OUTPUT / "ecosort_v2_seg.yaml"
    ).write_text(
        yaml_text,
        encoding="utf-8"
    )

    print(
        "\n===== KAGGLE DATASET READY ====="
    )

    print(OUTPUT)


if __name__ == "__main__":
    main()