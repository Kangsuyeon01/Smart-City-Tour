import torch
from sklearn.metrics import f1_score


def metric(prediction, label):
    _, max_indices = torch.max(prediction, 1)
    accuracy = (max_indices == label).sum().data.cpu().numpy() / max_indices.size()[0]

    prediction = torch.argmax(prediction, dim=-1).data.cpu().numpy()
    label = label.data.cpu().numpy()

    f1 = f1_score(
        prediction,
        label,
        average="macro",
    )
    return accuracy, f1, prediction, label
