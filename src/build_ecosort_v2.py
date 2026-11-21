from pathlib import Path
import shutil
import json
from collections import Counter


# ============================================================
# PROJECT PATHS
# ============================================================

ROOT = Path(__file__).resolve().parent.parent

# Existing EcoSort V1 segmentation dataset
V1 = ROOT / "data" / "ecosort_segmentation"

# Additional Segmented TrashNet dataset
TRASHNET = (
    ROOT
    / "external_data"
    / "segmented_trashnet"
    / "extracted"
    / "trashnet - Copy"
)

# New combined dataset
OUT = ROOT / "data" / "ecosort_v2"


# ============================================================
# ECOSORT CLASSES
# ============================================================

CLASS_NAMES = {
    0: "PLASTIC",
    1: "METAL",
    2: "GLASS",
    3: "PAPER_CARDBOARD",
}


# ============================================================
# TRASHNET -> ECOSORT CLASS MAPPING
# ============================================================

# TrashNet:
#
# 0 = cardboard
# 1 = glass
# 2 = metal
# 3 = paper
# 4 = plastic
# 5 = trash
#
# EcoSort:
#
# 0 = PLASTIC
# 1 = METAL
# 2 = GLASS
# 3 = PAPER_CARDBOARD

TRASHNET_MAP = {
    0: 3,       # cardboard -> PAPER_CARDBOARD
    1: 2,       # glass -> GLASS
    2: 1,       # metal -> METAL
    3: 3,       # paper -> PAPER_CARDBOARD
    4: 0,       # plastic -> PLASTIC
    5: None,    # unsupported generic trash -> excluded
}


IMAGE_EXTENSIONS = {
    ".jpg",
    ".jpeg",
    ".png",
    ".bmp",
    ".webp",
}


# ============================================================
# COORDINATE CLIPPING
# ============================================================

def clip_coordinate(value):
    """
    YOLO segmentation coordinates must remain
    in the normalized range [0, 1].

    Some TrashNet polygons contain tiny floating-point
    errors such as 1.000013 or -0.000013.
    """

    value = float(value)

    return max(
        0.0,
        min(
            1.0,
            value
        )
    )


def normalize_polygon(parts):
    """
    Preserve polygon geometry while clipping every
    normalized x/y coordinate into [0, 1].
    """

    coordinates = []

    for value in parts[1:]:

        coordinate = clip_coordinate(
            value
        )

        coordinates.append(
            f"{coordinate:.12f}"
        )

    return coordinates


# ============================================================
# CREATE OUTPUT DIRECTORIES
# ============================================================

def create_directories():

    if OUT.exists():

        print(
            f"Removing old V2 dataset: {OUT}"
        )

        shutil.rmtree(
            OUT
        )

    for split in (
        "train",
        "val",
        "test"
    ):

        (
            OUT
            / "images"
            / split
        ).mkdir(
            parents=True,
            exist_ok=True
        )

        (
            OUT
            / "labels"
            / split
        ).mkdir(
            parents=True,
            exist_ok=True
        )


# ============================================================
# COPY ORIGINAL ECOSORT DATA
# ============================================================

def copy_ecosort_split(split):

    image_dir = (
        V1
        / "images"
        / split
    )

    label_dir = (
        V1
        / "labels"
        / split
    )

    image_count = 0
    annotation_count = 0

    class_counts = Counter()

    for image_path in sorted(
        image_dir.iterdir()
    ):

        if (
            image_path.suffix.lower()
            not in IMAGE_EXTENSIONS
        ):
            continue

        label_path = (
            label_dir
            / f"{image_path.stem}.txt"
        )

        if not label_path.exists():

            print(
                "WARNING: Missing V1 label:",
                image_path.name
            )

            continue

        new_stem = (
            f"v1_{image_path.stem}"
        )

        destination_image = (
            OUT
            / "images"
            / split
            / (
                new_stem
                + image_path.suffix.lower()
            )
        )

        destination_label = (
            OUT
            / "labels"
            / split
            / f"{new_stem}.txt"
        )

        lines = []

        for raw_line in (
            label_path
            .read_text(
                encoding="utf-8"
            )
            .splitlines()
        ):

            line = raw_line.strip()

            if not line:
                continue

            parts = line.split()

            class_id = int(
                parts[0]
            )

            if (
                class_id
                not in CLASS_NAMES
            ):

                raise ValueError(
                    "Unexpected V1 class "
                    f"{class_id} in "
                    f"{label_path}"
                )

            # YOLO segmentation:
            # class ID + minimum 3 (x,y) points
            if (
                len(parts) < 7
                or
                (len(parts) - 1) % 2 != 0
            ):

                raise ValueError(
                    "Malformed V1 polygon in "
                    f"{label_path}: "
                    f"{line}"
                )

            coordinates = (
                normalize_polygon(
                    parts
                )
            )

            new_line = " ".join(
                [str(class_id)]
                + coordinates
            )

            lines.append(
                new_line
            )

            class_counts[
                class_id
            ] += 1

            annotation_count += 1

        shutil.copy2(
            image_path,
            destination_image
        )

        destination_label.write_text(
            "\n".join(lines)
            + (
                "\n"
                if lines
                else ""
            ),
            encoding="utf-8"
        )

        image_count += 1

    return {
        "images":
            image_count,

        "annotations":
            annotation_count,

        "classes":
            dict(class_counts),
    }


# ============================================================
# COPY + REMAP TRASHNET DATA
# ============================================================

def copy_trashnet_split(
    source_split,
    destination_split
):

    image_dir = (
        TRASHNET
        / source_split
        / "images"
    )

    label_dir = (
        TRASHNET
        / source_split
        / "labels"
    )

    image_count = 0
    annotation_count = 0

    removed_trash_annotations = 0
    discarded_trash_only_images = 0

    clipped_coordinate_count = 0

    class_counts = Counter()

    for image_path in sorted(
        image_dir.iterdir()
    ):

        if (
            image_path.suffix.lower()
            not in IMAGE_EXTENSIONS
        ):
            continue

        label_path = (
            label_dir
            / f"{image_path.stem}.txt"
        )

        if not label_path.exists():

            print(
                "WARNING: Missing TrashNet label:",
                image_path.name
            )

            continue

        remapped_lines = []

        for raw_line in (
            label_path
            .read_text(
                encoding="utf-8"
            )
            .splitlines()
        ):

            line = raw_line.strip()

            if not line:
                continue

            parts = line.split()

            old_class = int(
                parts[0]
            )

            if (
                old_class
                not in TRASHNET_MAP
            ):

                raise ValueError(
                    "Unexpected TrashNet class "
                    f"{old_class} in "
                    f"{label_path}"
                )

            if (
                len(parts) < 7
                or
                (len(parts) - 1) % 2 != 0
            ):

                raise ValueError(
                    "Malformed TrashNet polygon in "
                    f"{label_path}: "
                    f"{line}"
                )

            new_class = (
                TRASHNET_MAP[
                    old_class
                ]
            )

            # Generic "trash" is outside
            # EcoSort's four supported classes.
            if new_class is None:

                removed_trash_annotations += 1

                continue

            clipped_coordinates = []

            for value in parts[1:]:

                original = float(
                    value
                )

                clipped = (
                    clip_coordinate(
                        original
                    )
                )

                if clipped != original:

                    clipped_coordinate_count += 1

                clipped_coordinates.append(
                    f"{clipped:.12f}"
                )

            new_line = " ".join(
                [str(new_class)]
                + clipped_coordinates
            )

            remapped_lines.append(
                new_line
            )

            class_counts[
                new_class
            ] += 1

            annotation_count += 1

        # If the image contained only the unsupported
        # generic "trash" class, exclude the image.
        if not remapped_lines:

            discarded_trash_only_images += 1

            continue

        new_stem = (
            f"trashnet_"
            f"{source_split}_"
            f"{image_path.stem}"
        )

        destination_image = (
            OUT
            / "images"
            / destination_split
            / (
                new_stem
                + image_path.suffix.lower()
            )
        )

        destination_label = (
            OUT
            / "labels"
            / destination_split
            / f"{new_stem}.txt"
        )

        shutil.copy2(
            image_path,
            destination_image
        )

        destination_label.write_text(
            "\n".join(
                remapped_lines
            )
            + "\n",
            encoding="utf-8"
        )

        image_count += 1

    return {
        "images":
            image_count,

        "annotations":
            annotation_count,

        "removed_trash_annotations":
            removed_trash_annotations,

        "discarded_trash_only_images":
            discarded_trash_only_images,

        "clipped_coordinates":
            clipped_coordinate_count,

        "classes":
            dict(class_counts),
    }


# ============================================================
# VERIFY DATASET
# ============================================================

def verify_dataset():

    print(
        "\n===== VERIFYING ECOSORT V2 ====="
    )

    total_errors = 0

    verification = {}

    for split in (
        "train",
        "val",
        "test"
    ):

        image_dir = (
            OUT
            / "images"
            / split
        )

        label_dir = (
            OUT
            / "labels"
            / split
        )

        images = {
            path.stem
            for path
            in image_dir.iterdir()
            if (
                path.suffix.lower()
                in IMAGE_EXTENSIONS
            )
        }

        labels = {
            path.stem
            for path
            in label_dir.glob(
                "*.txt"
            )
        }

        missing_labels = (
            images - labels
        )

        orphan_labels = (
            labels - images
        )

        bad_polygons = 0
        invalid_classes = 0
        out_of_range = 0

        annotation_count = 0

        class_counts = Counter()

        for label_path in (
            label_dir.glob(
                "*.txt"
            )
        ):

            for line in (
                label_path
                .read_text(
                    encoding="utf-8"
                )
                .splitlines()
            ):

                parts = line.split()

                if not parts:
                    continue

                annotation_count += 1

                class_id = int(
                    parts[0]
                )

                if (
                    class_id
                    not in CLASS_NAMES
                ):

                    invalid_classes += 1

                else:

                    class_counts[
                        class_id
                    ] += 1

                if (
                    len(parts) < 7
                    or
                    (len(parts) - 1) % 2 != 0
                ):

                    bad_polygons += 1

                    continue

                coordinates = [
                    float(value)
                    for value
                    in parts[1:]
                ]

                if any(
                    value < 0.0
                    or
                    value > 1.0
                    for value
                    in coordinates
                ):

                    out_of_range += 1

        print(
            f"\n===== {split.upper()} ====="
        )

        print(
            f"Images: {len(images)}"
        )

        print(
            f"Labels: {len(labels)}"
        )

        print(
            f"Annotations: "
            f"{annotation_count}"
        )

        print(
            f"Missing labels: "
            f"{len(missing_labels)}"
        )

        print(
            f"Orphan labels: "
            f"{len(orphan_labels)}"
        )

        print(
            f"Bad polygons: "
            f"{bad_polygons}"
        )

        print(
            f"Invalid classes: "
            f"{invalid_classes}"
        )

        print(
            f"Out-of-range polygons: "
            f"{out_of_range}"
        )

        print(
            "\nClass distribution:"
        )

        for class_id in range(4):

            print(
                f"  "
                f"{CLASS_NAMES[class_id]:18s}"
                f": "
                f"{class_counts[class_id]}"
            )

        split_errors = (
            len(missing_labels)
            + len(orphan_labels)
            + bad_polygons
            + invalid_classes
            + out_of_range
        )

        total_errors += (
            split_errors
        )

        verification[
            split
        ] = {
            "images":
                len(images),

            "labels":
                len(labels),

            "annotations":
                annotation_count,

            "missing_labels":
                len(missing_labels),

            "orphan_labels":
                len(orphan_labels),

            "bad_polygons":
                bad_polygons,

            "invalid_classes":
                invalid_classes,

            "out_of_range":
                out_of_range,

            "class_distribution":
                {
                    CLASS_NAMES[class_id]:
                        class_counts[class_id]
                    for class_id
                    in range(4)
                }
        }

    return (
        total_errors,
        verification
    )


# ============================================================
# WRITE DATASET YAML
# ============================================================

def write_yaml():

    yaml_text = f"""path: {OUT.as_posix()}

train: images/train
val: images/val
test: images/test

names:
  0: PLASTIC
  1: METAL
  2: GLASS
  3: PAPER_CARDBOARD
"""

    yaml_path = (
        OUT
        / "ecosort_v2_seg.yaml"
    )

    yaml_path.write_text(
        yaml_text,
        encoding="utf-8"
    )

    return yaml_path


# ============================================================
# MAIN
# ============================================================

def main():

    print(
        "======================================"
    )

    print(
        "BUILDING ECOSORT V2 DATASET"
    )

    print(
        "======================================"
    )

    print(
        "\nV1 dataset:"
    )

    print(
        V1
    )

    print(
        "\nAdditional dataset:"
    )

    print(
        TRASHNET
    )

    create_directories()

    report = {
        "datasets": {
            "ecosort_v1":
                str(V1),

            "segmented_trashnet":
                str(TRASHNET),
        },

        "class_mapping": {
            "cardboard":
                "PAPER_CARDBOARD",

            "glass":
                "GLASS",

            "metal":
                "METAL",

            "paper":
                "PAPER_CARDBOARD",

            "plastic":
                "PLASTIC",

            "trash":
                "EXCLUDED",
        },

        "splits": {}
    }


    # ========================================================
    # TRAIN
    # ========================================================

    print(
        "\nCopying original EcoSort TRAIN..."
    )

    v1_train = (
        copy_ecosort_split(
            "train"
        )
    )

    print(
        "Adding TrashNet TRAIN..."
    )

    trashnet_train = (
        copy_trashnet_split(
            "train",
            "train"
        )
    )


    # ========================================================
    # VALIDATION
    # ========================================================

    print(
        "\nCopying original EcoSort VAL..."
    )

    v1_val = (
        copy_ecosort_split(
            "val"
        )
    )

    print(
        "Adding TrashNet VALID..."
    )

    trashnet_val = (
        copy_trashnet_split(
            "valid",
            "val"
        )
    )


    # ========================================================
    # TEST
    #
    # IMPORTANT:
    # Keep the original EcoSort test set untouched.
    # This allows a fair V1 vs V2 comparison.
    # ========================================================

    print(
        "\nCopying untouched original "
        "EcoSort TEST..."
    )

    v1_test = (
        copy_ecosort_split(
            "test"
        )
    )


    report["splits"]["train"] = {
        "ecosort_v1":
            v1_train,

        "trashnet":
            trashnet_train,
    }

    report["splits"]["val"] = {
        "ecosort_v1":
            v1_val,

        "trashnet":
            trashnet_val,
    }

    report["splits"]["test"] = {
        "ecosort_v1":
            v1_test,

        "trashnet":
            "NOT INCLUDED - retained as external test data",
    }


    # ========================================================
    # YAML
    # ========================================================

    yaml_path = (
        write_yaml()
    )


    # ========================================================
    # VERIFY
    # ========================================================

    (
        verification_errors,
        verification
    ) = verify_dataset()

    report[
        "verification"
    ] = verification

    report[
        "verification_errors"
    ] = verification_errors


    # ========================================================
    # SAVE REPORT
    # ========================================================

    report_path = (
        OUT
        / "dataset_report.json"
    )

    report_path.write_text(
        json.dumps(
            report,
            indent=4
        ),
        encoding="utf-8"
    )


    # ========================================================
    # FINAL SUMMARY
    # ========================================================

    print(
        "\n======================================"
    )

    print(
        "ECOSORT V2 BUILD COMPLETE"
    )

    print(
        "======================================"
    )

    print(
        f"\nDataset:\n{OUT}"
    )

    print(
        f"\nYAML:\n{yaml_path}"
    )

    print(
        f"\nReport:\n{report_path}"
    )

    print(
        f"\nVerification errors: "
        f"{verification_errors}"
    )

    if verification_errors == 0:

        print(
            "\nECOSORT V2 DATASET PASSED "
            "STRUCTURAL VERIFICATION."
        )

    else:

        print(
            "\nWARNING: ECOSORT V2 DATASET "
            "HAS VERIFICATION ERRORS."
        )


if __name__ == "__main__":
    main()