from typing import List, Optional
import cv2
import torch
from albumentations import (
    Compose,
    RandomRotate90,
    HorizontalFlip,
    VerticalFlip,
    Resize,
    Normalize,
)
from albumentations.pytorch import ToTensorV2
from torch.utils.data import Dataset


class LandmarkDataset(Dataset):
    def __init__(
        self,
        img_paths: List,
        labels: Optional[List],
        img_size: int,
        is_training: bool = True,
        use_augmentation: bool = True,
    ):
        super(LandmarkDataset, self).__init__()
        self.img_paths = img_paths
        self.labels = labels
        self.img_size = img_size
        self.is_training = is_training
        self.use_augmentation = use_augmentation

    def __len__(self):
        return len(self.img_paths)

    def __getitem__(self, item):
        img = cv2.imread(self.img_paths[item])
        img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
        augmented = self._get_augmentations(self.use_augmentation, self.img_size)(
            image=img
        )
        img = augmented["image"]

        if self.is_training:
            label = self.labels[item]
            return {"input": img, "target": torch.tensor(label, dtype=torch.long)}
        return {"input": img}

    @staticmethod
    def _get_augmentations(use_augmentation: bool, img_size: int) -> Compose:
        if use_augmentation:
            return Compose(
                [
                    RandomRotate90(p=0.5),
                    Resize(img_size, img_size),
                    HorizontalFlip(p=0.5),
                    VerticalFlip(p=0.5),
                    Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
                    ToTensorV2(),
                ]
            )
        else:
            return Compose(
                [
                    Resize(img_size, img_size),
                    Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225]),
                    ToTensorV2(),
                ]
            )
