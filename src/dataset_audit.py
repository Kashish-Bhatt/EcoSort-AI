import json
from collections import Counter

ANNOTATION_FILE = "TACO/data/annotations.json"

with open(ANNOTATION_FILE, "r", encoding="utf-8") as file:
    data = json.load(file)

category_names = {
    category["id"]: category["name"]
    for category in data["categories"]
}

counts = Counter(
    annotation["category_id"]
    for annotation in data["annotations"]
)

print("\n===== TACO DATASET AUDIT =====\n")

print("Total images:", len(data["images"]))
print("Total annotations:", len(data["annotations"]))
print("Total categories:", len(data["categories"]))

print("\n===== CATEGORY DISTRIBUTION =====\n")

for category_id, name in category_names.items():
    print(f"{name:35} {counts[category_id]}")

print("\n==============================")
# ==========================================
# ECOSORT MATERIAL CLASS MAPPING
# ==========================================

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
    ],

}


print("\n===== PROPOSED ECOSORT CLASS DISTRIBUTION =====\n")

name_to_id = {
    category["name"]: category["id"]
    for category in data["categories"]
}

ecosort_counts = {}

for ecosort_class, taco_classes in ECOSORT_MAPPING.items():

    total = 0

    for taco_class in taco_classes:

        if taco_class in name_to_id:
            category_id = name_to_id[taco_class]
            total += counts[category_id]

    ecosort_counts[ecosort_class] = total


for ecosort_class, total in ecosort_counts.items():
    print(f"{ecosort_class:25} {total}")

print("\nTotal mapped annotations:", sum(ecosort_counts.values()))
print("\n===== CLASS PERCENTAGES =====\n")

mapped_total = sum(ecosort_counts.values())

for ecosort_class, total in ecosort_counts.items():
    percentage = (total / mapped_total) * 100

    print(
        f"{ecosort_class:25} "
        f"{total:5} "
        f"({percentage:.2f}%)"
    )