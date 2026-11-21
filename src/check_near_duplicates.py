import os
import json
import imagehash

from PIL import Image


DATASET_DIR = "data/ecosort_dataset"
IMAGE_DIR = os.path.join(DATASET_DIR, "images")
SPLIT_DIR = os.path.join(DATASET_DIR, "splits")

# Smaller distance = more visually similar.
# We start conservatively.
THRESHOLD = 3


def load_split(name):

    with open(
        os.path.join(SPLIT_DIR, f"{name}.json"),
        "r",
        encoding="utf-8"
    ) as file:

        return json.load(file)


splits = {
    "train": load_split("train"),
    "val": load_split("val"),
    "test": load_split("test")
}


print("\nCalculating perceptual hashes...\n")


hashes = {}


for split_name, records in splits.items():

    hashes[split_name] = []

    for index, record in enumerate(records, start=1):

        image_name = record["image"]

        image_path = os.path.join(
            IMAGE_DIR,
            image_name
        )

        with Image.open(image_path) as image:

            perceptual_hash = imagehash.phash(
                image.convert("RGB")
            )

        hashes[split_name].append(
            (
                image_name,
                perceptual_hash
            )
        )

        if index % 100 == 0:

            print(
                f"{split_name}: "
                f"{index}/{len(records)}"
            )


# ============================================================
# CROSS-SPLIT COMPARISON
# ============================================================

near_duplicates = []


def compare_splits(
    split_a,
    split_b
):

    print(
        f"\nComparing "
        f"{split_a} vs {split_b}..."
    )

    for name_a, hash_a in hashes[split_a]:

        for name_b, hash_b in hashes[split_b]:

            distance = hash_a - hash_b

            if distance <= THRESHOLD:

                near_duplicates.append({

                    "split_a": split_a,

                    "image_a": name_a,

                    "split_b": split_b,

                    "image_b": name_b,

                    "phash_distance": distance
                })


compare_splits(
    "train",
    "val"
)

compare_splits(
    "train",
    "test"
)

compare_splits(
    "val",
    "test"
)


# ============================================================
# REPORT
# ============================================================

print("\n================================")
print("NEAR-DUPLICATE CHECK COMPLETE")
print("================================")

print(
    "Threshold:",
    THRESHOLD
)

print(
    "Potential cross-split "
    "near-duplicate pairs:",
    len(near_duplicates)
)


if near_duplicates:

    print(
        "\nPotential matches found."
    )

    for match in near_duplicates[:20]:

        print(
            match["split_a"],
            match["image_a"],
            "<->",
            match["split_b"],
            match["image_b"],
            "| distance:",
            match["phash_distance"]
        )

else:

    print(
        "\nNo suspicious near-duplicate "
        "pairs found."
    )


# ============================================================
# SAVE REPORT
# ============================================================

report_path = os.path.join(
    DATASET_DIR,
    "near_duplicate_report.json"
)


with open(
    report_path,
    "w",
    encoding="utf-8"
) as file:

    json.dump(
        {
            "threshold": THRESHOLD,
            "matches": near_duplicates
        },
        file,
        indent=2
    )


print(
    "\nReport saved to:",
    report_path
)