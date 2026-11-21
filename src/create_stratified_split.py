import json
import os
import numpy as np

from iterstrat.ml_stratifiers import MultilabelStratifiedShuffleSplit


ANNOTATION_FILE = "data/ecosort_dataset/annotations.json"
OUTPUT_DIR = "data/ecosort_dataset/splits"

SEED = 42

CLASS_NAMES = [
    "PLASTIC",
    "METAL",
    "GLASS",
    "PAPER_CARDBOARD"
]

CLASS_TO_ID = {
    name: index
    for index, name in enumerate(CLASS_NAMES)
}


os.makedirs(OUTPUT_DIR, exist_ok=True)


# ============================================================
# LOAD DATA
# ============================================================

with open(ANNOTATION_FILE, "r", encoding="utf-8") as file:
    records = json.load(file)


print("Total images:", len(records))


# ============================================================
# CREATE MULTI-LABEL IMAGE MATRIX
# ============================================================

X = np.arange(len(records)).reshape(-1, 1)

Y = np.zeros(
    (len(records), len(CLASS_NAMES)),
    dtype=int
)


for image_index, record in enumerate(records):

    classes_present = set()

    for obj in record["objects"]:
        classes_present.add(obj["class_name"])

    for class_name in classes_present:

        class_id = CLASS_TO_ID[class_name]

        Y[image_index, class_id] = 1


# ============================================================
# FIRST SPLIT:
# 70% TRAIN
# 30% TEMP
# ============================================================

first_split = MultilabelStratifiedShuffleSplit(
    n_splits=1,
    test_size=0.30,
    random_state=SEED
)


train_indices, temp_indices = next(
    first_split.split(X, Y)
)


# ============================================================
# SECOND SPLIT:
# TEMP -> 50% VALIDATION / 50% TEST
#
# Final:
# Train = 70%
# Val   = 15%
# Test  = 15%
# ============================================================

X_temp = X[temp_indices]
Y_temp = Y[temp_indices]


second_split = MultilabelStratifiedShuffleSplit(
    n_splits=1,
    test_size=0.50,
    random_state=SEED
)


val_relative, test_relative = next(
    second_split.split(
        X_temp,
        Y_temp
    )
)


val_indices = temp_indices[val_relative]
test_indices = temp_indices[test_relative]


# ============================================================
# BUILD RECORD LISTS
# ============================================================

train_records = [
    records[i]
    for i in train_indices
]

val_records = [
    records[i]
    for i in val_indices
]

test_records = [
    records[i]
    for i in test_indices
]


splits = {
    "train": train_records,
    "val": val_records,
    "test": test_records
}


# ============================================================
# SAVE SPLITS
# ============================================================

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


# ============================================================
# COUNT OBJECTS
# ============================================================

def object_distribution(split_records):

    counts = {
        name: 0
        for name in CLASS_NAMES
    }

    for record in split_records:

        for obj in record["objects"]:

            counts[obj["class_name"]] += 1

    return counts


# ============================================================
# COUNT IMAGES CONTAINING EACH CLASS
# ============================================================

def image_distribution(split_records):

    counts = {
        name: 0
        for name in CLASS_NAMES
    }

    for record in split_records:

        present = {
            obj["class_name"]
            for obj in record["objects"]
        }

        for class_name in present:
            counts[class_name] += 1

    return counts


# ============================================================
# REPORT
# ============================================================

print("\n================================")
print("STRATIFIED SPLIT COMPLETE")
print("================================")


for split_name, split_records in splits.items():

    obj_counts = object_distribution(split_records)

    img_counts = image_distribution(split_records)

    print(f"\n{split_name.upper()}")
    print("Images:", len(split_records))

    print("\nObject counts:")

    for class_name in CLASS_NAMES:

        print(
            f"{class_name:20}",
            obj_counts[class_name]
        )

    print("\nImages containing class:")

    for class_name in CLASS_NAMES:

        print(
            f"{class_name:20}",
            img_counts[class_name]
        )


# ============================================================
# LEAKAGE CHECK
# ============================================================

train_names = {
    record["image"]
    for record in train_records
}

val_names = {
    record["image"]
    for record in val_records
}

test_names = {
    record["image"]
    for record in test_records
}


train_val_overlap = train_names.intersection(val_names)

train_test_overlap = train_names.intersection(test_names)

val_test_overlap = val_names.intersection(test_names)


print("\n================================")
print("LEAKAGE CHECK")
print("================================")

print(
    "Train/Val overlap:",
    len(train_val_overlap)
)

print(
    "Train/Test overlap:",
    len(train_test_overlap)
)

print(
    "Val/Test overlap:",
    len(val_test_overlap)
)


print("\nRandom seed:", SEED)

print(
    "\nSplit files saved to:",
    OUTPUT_DIR
)