import os
import json
from datasets import load_dataset


# ============================================================
# 1. OUTPUT DIRECTORIES
# ============================================================

OUTPUT_DIR = "data/ecosort_dataset"
IMAGE_DIR = os.path.join(OUTPUT_DIR, "images")

os.makedirs(IMAGE_DIR, exist_ok=True)


# ============================================================
# 2. ECOSORT CLASS MAPPING
# ============================================================

ECOSORT_MAPPING = {

    "PLASTIC": [
        "Other plastic bottle",
        "Clear plastic bottle",
        "Plastic bottle cap",
        "Disposable plastic cup",
        "Other plastic cup",
        "Plastic lid",
        "Other plastic",
        "Plastic film",
        "Six pack rings",
        "Garbage bag",
        "Other plastic wrapper",
        "Single-use carrier bag",
        "Polypropylene bag",
        "Crisp packet",
        "Spread tub",
        "Tupperware",
        "Disposable food container",
        "Other plastic container",
        "Plastic glooves",
        "Plastic utensils",
        "Squeezable tube",
        "Plastic straw"
    ],

    "METAL": [
        "Aluminium foil",
        "Aluminium blister pack",
        "Metal bottle cap",
        "Food Can",
        "Aerosol",
        "Drink can",
        "Metal lid",
        "Pop tab",
        "Scrap metal"
    ],

    "GLASS": [
        "Glass bottle",
        "Broken glass",
        "Glass cup",
        "Glass jar"
    ],

    "PAPER_CARDBOARD": [
        "Carded blister pack",
        "Toilet tube",
        "Other carton",
        "Egg carton",
        "Drink carton",
        "Corrugated carton",
        "Meal carton",
        "Pizza box",
        "Paper cup",
        "Magazine paper",
        "Tissues",
        "Wrapping paper",
        "Normal paper",
        "Paper bag",
        "Paper straw"
    ]
}


# ============================================================
# 3. FINAL ECOSORT CLASS IDS
# ============================================================

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


# ============================================================
# 4. CREATE REVERSE LOOKUP
#
# Example:
# "Clear plastic bottle" -> "PLASTIC"
# "Drink can"            -> "METAL"
# ============================================================

TACO_TO_ECOSORT = {}

for ecosort_class, taco_classes in ECOSORT_MAPPING.items():

    for taco_class in taco_classes:

        TACO_TO_ECOSORT[taco_class] = ecosort_class


# ============================================================
# 5. LOAD TACO DATASET
# ============================================================

print("\nLoading TACO dataset from Hugging Face cache...")

dataset = load_dataset(
    "pictograph/taco",
    split="train"
)

print("TACO records loaded:", len(dataset))


# ============================================================
# 6. PREPARE EXPORT
# ============================================================

exported_annotations = []

saved_images = 0
saved_objects = 0

class_counts = {
    name: 0
    for name in CLASS_NAMES
}


# ============================================================
# 7. PROCESS EVERY TACO IMAGE
# ============================================================

print("\nCreating EcoSort dataset...\n")


for index, record in enumerate(dataset):

    # --------------------------------------------------------
    # IMAGE
    # --------------------------------------------------------

    image = record["image"].convert("RGB")

    width = image.width
    height = image.height


    # --------------------------------------------------------
    # OBJECT INFORMATION
    # --------------------------------------------------------

    objects = record["objects"]

    category_names = objects["category_names"]
    boxes = objects["bbox"]

    segmentations = record["segmentation"]


    image_annotations = []


    # --------------------------------------------------------
    # PROCESS OBJECTS INSIDE THIS IMAGE
    # --------------------------------------------------------

    for object_index, taco_name in enumerate(category_names):

        # Ignore classes that are not part of EcoSort
        if taco_name not in TACO_TO_ECOSORT:
            continue


        # Convert TACO class -> EcoSort class
        ecosort_class = TACO_TO_ECOSORT[taco_name]

        class_id = CLASS_TO_ID[ecosort_class]


        # ----------------------------------------------------
        # BOUNDING BOX
        # ----------------------------------------------------

        bbox = boxes[object_index]


        # ----------------------------------------------------
        # SEGMENTATION POLYGON
        # ----------------------------------------------------

        polygon = None

        if object_index < len(segmentations):

            segmentation_data = segmentations[object_index]

            polygon = segmentation_data.get("points")


        # ----------------------------------------------------
        # STORE OBJECT
        # ----------------------------------------------------

        annotation = {

            "class_id": class_id,

            "class_name": ecosort_class,

            "original_taco_class": taco_name,

            "bbox": bbox,

            "segmentation": polygon
        }


        image_annotations.append(annotation)

        saved_objects += 1

        class_counts[ecosort_class] += 1


    # ========================================================
    # SKIP IMAGE IF IT CONTAINS NONE OF OUR FOUR CLASSES
    # ========================================================

    if len(image_annotations) == 0:
        continue


    # ========================================================
    # SAVE IMAGE
    # ========================================================

    image_name = f"ecosort_{index:05d}.jpg"

    image_path = os.path.join(
        IMAGE_DIR,
        image_name
    )


    image.save(
        image_path,
        format="JPEG",
        quality=95
    )


    # ========================================================
    # STORE IMAGE-LEVEL INFORMATION
    # ========================================================

    exported_annotations.append({

        "image": image_name,

        "width": width,

        "height": height,

        "objects": image_annotations
    })


    saved_images += 1


    # --------------------------------------------------------
    # PROGRESS
    # --------------------------------------------------------

    if saved_images % 100 == 0:

        print(
            f"Saved {saved_images} images "
            f"and {saved_objects} objects..."
        )


# ============================================================
# 8. SAVE COMPLETE ANNOTATION FILE
# ============================================================

annotation_file = os.path.join(
    OUTPUT_DIR,
    "annotations.json"
)


with open(
    annotation_file,
    "w",
    encoding="utf-8"
) as file:

    json.dump(
        exported_annotations,
        file,
        indent=2
    )


# ============================================================
# 9. SAVE CLASS INFORMATION
# ============================================================

class_file = os.path.join(
    OUTPUT_DIR,
    "classes.json"
)


class_information = {

    "classes": CLASS_NAMES,

    "class_to_id": CLASS_TO_ID,

    "taco_to_ecosort_mapping": ECOSORT_MAPPING
}


with open(
    class_file,
    "w",
    encoding="utf-8"
) as file:

    json.dump(
        class_information,
        file,
        indent=2
    )


# ============================================================
# 10. SAVE DATASET SUMMARY
# ============================================================

summary_file = os.path.join(
    OUTPUT_DIR,
    "dataset_summary.json"
)


summary = {

    "source_dataset": "TACO - Trash Annotations in Context",

    "source_records": len(dataset),

    "exported_images": saved_images,

    "exported_objects": saved_objects,

    "number_of_classes": len(CLASS_NAMES),

    "class_distribution": class_counts
}


with open(
    summary_file,
    "w",
    encoding="utf-8"
) as file:

    json.dump(
        summary,
        file,
        indent=2
    )


# ============================================================
# 11. FINAL REPORT
# ============================================================

print("\n====================================")
print("ECOSORT DATASET CREATED SUCCESSFULLY")
print("====================================")

print("\nImages:", saved_images)

print("Objects:", saved_objects)


print("\nClasses:")

for class_name, class_id in CLASS_TO_ID.items():

    print(
        class_id,
        "->",
        class_name
    )


print("\nClass distribution:")

for class_name, count in class_counts.items():

    print(
        f"{class_name:20} {count}"
    )


print(
    "\nDataset location:",
    OUTPUT_DIR
)

print("\nFiles created:")

print(" - images/")
print(" - annotations.json")
print(" - classes.json")
print(" - dataset_summary.json")