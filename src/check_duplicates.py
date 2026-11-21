import os
import json
import hashlib
from collections import defaultdict


DATASET_DIR = "data/ecosort_dataset"
IMAGE_DIR = os.path.join(DATASET_DIR, "images")
SPLIT_DIR = os.path.join(DATASET_DIR, "splits")


def load_split(name):

    path = os.path.join(
        SPLIT_DIR,
        f"{name}.json"
    )

    with open(path, "r", encoding="utf-8") as file:
        return json.load(file)


def calculate_hash(image_path):

    sha256 = hashlib.sha256()

    with open(image_path, "rb") as file:

        while True:

            chunk = file.read(1024 * 1024)

            if not chunk:
                break

            sha256.update(chunk)

    return sha256.hexdigest()


splits = {
    "train": load_split("train"),
    "val": load_split("val"),
    "test": load_split("test")
}


hash_records = defaultdict(list)


print("\nCalculating image hashes...\n")


for split_name, records in splits.items():

    print(
        f"Checking {split_name}: "
        f"{len(records)} images"
    )

    for record in records:

        image_name = record["image"]

        image_path = os.path.join(
            IMAGE_DIR,
            image_name
        )

        image_hash = calculate_hash(
            image_path
        )

        hash_records[image_hash].append(
            {
                "split": split_name,
                "image": image_name
            }
        )


# ============================================================
# FIND DUPLICATES
# ============================================================

duplicate_groups = []

cross_split_duplicates = []


for image_hash, items in hash_records.items():

    if len(items) > 1:

        duplicate_groups.append(items)

        split_names = {
            item["split"]
            for item in items
        }

        if len(split_names) > 1:

            cross_split_duplicates.append(items)


# ============================================================
# REPORT
# ============================================================

print("\n================================")
print("DUPLICATE CHECK COMPLETE")
print("================================")

print(
    "Total duplicate groups:",
    len(duplicate_groups)
)

print(
    "Cross-split duplicate groups:",
    len(cross_split_duplicates)
)


if cross_split_duplicates:

    print("\nWARNING: DATA LEAKAGE FOUND\n")

    for group_number, group in enumerate(
        cross_split_duplicates,
        start=1
    ):

        print(
            f"Duplicate group {group_number}:"
        )

        for item in group:

            print(
                " ",
                item["split"],
                "->",
                item["image"]
            )

else:

    print(
        "\nNo exact duplicate images "
        "found across train/val/test."
    )


# ============================================================
# SAVE REPORT
# ============================================================

report_path = os.path.join(
    DATASET_DIR,
    "duplicate_report.json"
)


with open(
    report_path,
    "w",
    encoding="utf-8"
) as file:

    json.dump(
        {
            "duplicate_groups":
                duplicate_groups,

            "cross_split_duplicates":
                cross_split_duplicates
        },
        file,
        indent=2
    )


print(
    "\nReport saved to:",
    report_path
)