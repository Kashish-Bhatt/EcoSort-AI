import json
import os
import random
from collections import Counter

ANNOTATION_FILE = "data/ecosort_dataset/annotations.json"
OUTPUT_DIR = "data/ecosort_dataset/splits"

SEED = 42

TRAIN_RATIO = 0.70
VAL_RATIO = 0.15
TEST_RATIO = 0.15

os.makedirs(OUTPUT_DIR, exist_ok=True)

# ---------------------------------------------------------
# LOAD DATA
# ---------------------------------------------------------

with open(ANNOTATION_FILE, "r", encoding="utf-8") as file:
    records = json.load(file)

print("Total images:", len(records))

# ---------------------------------------------------------
# REPRODUCIBLE SHUFFLE
# ---------------------------------------------------------

random.seed(SEED)

records_copy = records.copy()

random.shuffle(records_copy)

# ---------------------------------------------------------
# SPLIT
# ---------------------------------------------------------

total = len(records_copy)

train_end = int(total * TRAIN_RATIO)
val_end = train_end + int(total * VAL_RATIO)

train_records = records_copy[:train_end]

val_records = records_copy[train_end:val_end]

test_records = records_copy[val_end:]

# ---------------------------------------------------------
# SAVE
# ---------------------------------------------------------

splits = {
    "train": train_records,
    "val": val_records,
    "test": test_records
}

for split_name, split_records in splits.items():

    output_file = os.path.join(
        OUTPUT_DIR,
        f"{split_name}.json"
    )

    with open(
        output_file,
        "w",
        encoding="utf-8"
    ) as file:

        json.dump(
            split_records,
            file,
            indent=2
        )

# ---------------------------------------------------------
# CLASS DISTRIBUTION
# ---------------------------------------------------------

def get_distribution(split_records):

    counter = Counter()

    for record in split_records:

        for obj in record["objects"]:

            counter[obj["class_name"]] += 1

    return counter


print("\n============================")
print("DATASET SPLIT COMPLETE")
print("============================")

for split_name, split_records in splits.items():

    distribution = get_distribution(split_records)

    print(f"\n{split_name.upper()}")

    print("Images:", len(split_records))

    for class_name in [
        "PLASTIC",
        "METAL",
        "GLASS",
        "PAPER_CARDBOARD"
    ]:

        print(
            f"{class_name:20}",
            distribution[class_name]
        )


print("\nRandom seed:", SEED)

print(
    "\nSplit files saved to:",
    OUTPUT_DIR
)