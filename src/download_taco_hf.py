from datasets import load_dataset

print("Loading TACO dataset...")

dataset = load_dataset(
    "pictograph/taco",
    split="train"
)

print("\n===== DOWNLOAD SUCCESSFUL =====")
print("Total records:", len(dataset))

print("\nDataset columns:")
print(dataset.column_names)

print("\nFirst record keys:")
print(dataset[0].keys())