import socket
import os
import argparse
import numpy as np
import torch

from firebase.firebase import download_image_from_firebase
from firebase.firebase import save_result_to_db

from inference import predict

from utils.common.fix_seed import seed_everything
from utils.common.constant import LABEL_DICT
from utils.common.translation import str2bool


def pipe(args, device):
    host = socket.gethostbyname(socket.gethostname())
    port = 9999

    server_sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server_sock.bind((host, port))
    server_sock.listen(1)

    while True:
        print("기다리는 중")
        client_sock, addr = server_sock.accept()

        print("Connected by", addr)

        # Receive Data Length From Android
        data = client_sock.recv(1024)
        length = int.from_bytes(data, "little")
        print(f"data length from android: {length}")

        # Receive Data From Android
        data_from_android = client_sock.recv(length)
        print(f"data from android : {data_from_android}")

        # data를 더 이상 받을 수 없을 때
        if len(data_from_android) <= 0:
            break

        # Decode Byte to str
        data_from_android = data_from_android.decode()
        print(f"Decoded data: {data_from_android}")

        # Download Image From Firebase Storage
        image_saved_path = download_image_from_firebase(data_from_android)
        model_saved_path = args.model_saved_path

        print(image_saved_path)

        # Predict Value by downloaded image
        prediction = predict(
            image_saved_path, args, model_saved_path, device, voting=False
        )
        prediction = np.argmax(prediction)
        prediction = str(prediction)

        # Update Realtime Database
        save_result_to_db(prediction, data_from_android)

        print(f"prediction: {prediction}")

        # Encode data to Send Android
        data_to_android = data_from_android.encode()

        # Send Length to Android
        length = len(data_to_android)
        client_sock.sendall(length.to_bytes(length, byteorder="little"))

        # Send Data to Android
        print(data_to_android)
        client_sock.sendall(data_to_android)

        client_sock.close()

    server_sock.close()


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--seed", type=int, default=42)
    parser.add_argument("--img_size", type=int, default=224)
    parser.add_argument("--device", type=int, default=0)
    parser.add_argument(
        "--model_saved_path",
        type=str,
        default="./models/saved_model/1/loss/loss_best.pt",
    )
    parser.add_argument("--backbone", type=str, default="resnet50d")
    parser.add_argument(
        "--tta", type=str2bool, default="False", help="test time augmentation"
    )
    parser.add_argument("--num_classes", type=str, default=len(LABEL_DICT))

    args = parser.parse_args()
    # ===========================================================================
    seed_everything(seed=args.seed)
    os.environ["CUDA_VISIBLE_DEVICES"] = f"{args.device}"
    device = torch.device("cuda" if torch.cuda.is_available() else "cpu")
    # ===========================================================================
    pipe(args, device)
