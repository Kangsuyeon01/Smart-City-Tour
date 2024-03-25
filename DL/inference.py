import torch
import ttach as tta

from models.model import LandmarkModel
from utils.inference.load_model import load_saved_model
from utils.inference.single_image_aug import augment_single_image


def predict(image_path, args, model_saved_path, device, voting=True):
    model = LandmarkModel(**args.__dict__)
    model = load_saved_model(model, model_saved_path, device)

    if args.tta:
        tta_transforms = tta.Compose(
            [
                tta.HorizontalFlip(),
                tta.VerticalFlip(),
            ]
        )
        model = tta.ClassificationTTAWrapper(model, tta_transforms)

    model = model.to(device)
    # ==========================================================================
    model.eval()
    with torch.no_grad():
        img = augment_single_image(img_path=image_path, img_size=args.img_size)
        img = img.unsqueeze(0)  # add batch 1
        img = img.to(device)
        output = model(img)
        if voting:
            prediction = output.data.cpu().numpy()
        else:
            prediction = torch.argmax(output, dim=-1).data.cpu().numpy()

    # ==========================================================================
    return prediction[0]
