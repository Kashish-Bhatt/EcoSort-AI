import json

ANNOTATION_FILE = "data/ecosort_dataset/annotations.json"
TARGET_IMAGE = "ecosort_00302.jpg"

with open(ANNOTATION_FILE, "r", encoding="utf-8") as file:
    annotations = json.load(file)

for record in annotations:

    if record["image"] == TARGET_IMAGE:

        print("\nIMAGE:", record["image"])
        print("WIDTH:", record["width"])
        print("HEIGHT:", record["height"])

        print("\nNUMBER OF OBJECTS:", len(record["objects"]))

        for index, obj in enumerate(record["objects"], start=1):

            print("\n==============================")
            print("OBJECT", index)
            print("==============================")

            print("EcoSort class:")
            print(obj["class_name"])

            print("Original TACO class:")
            print(obj["original_taco_class"])

            print("Bounding box:")
            print(obj["bbox"])

            print("Segmentation:")
            print(obj["segmentation"])

        break