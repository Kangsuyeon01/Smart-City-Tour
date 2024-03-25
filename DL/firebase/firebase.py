import os
import firebase_admin
from firebase_admin import credentials
from firebase_admin import storage
from firebase_admin import db

cred = credentials.Certificate("./firebase/cred.json")  # server key load

firebaseConfig = {
    "apiKey": "AIzaSyCCbjlNNWLXPUbDQ2SlZQuS9fvGmvscu_o",
    "authDomain": "ai-landmark-app.firebaseapp.com",
    "projectId": "ai-landmark-app",
    "storageBucket": "ai-landmark-app.appspot.com",
    "messagingSenderId": "130331030804",
    "appId": "1:130331030804:web:8fe57a00e54f74ccfd7e01",
    "measurementId": "G-Y4VL2C3S2S",
    "databaseURL": "https://ai-landmark-app-default-rtdb.asia-southeast1.firebasedatabase.app/",
}

firebase_admin.initialize_app(cred, firebaseConfig)


def download_image_from_firebase(index: str) -> str:
    """
    :param index: image index (UNIX Time)
    :return: image saved path on local
    """
    print("***" * 20)
    print("Download and Save Image From Firebase Storage")
    print("***" * 20)

    if not os.path.exists("./downloaded_images"):
        os.mkdir("./downloaded_images")

    blob_name = f"images/{index}.jpg"
    save_image_path = os.path.join("./downloaded_images", f"{index}.jpg")
    bucket = storage.bucket()
    blob = bucket.blob(blob_name)
    blob.download_to_filename(save_image_path)
    return save_image_path


def save_result_to_db(prediction: str, index: str) -> None:
    """
    :param prediction: prediction of image
    :param index: image index (UNIX Time)
    :return: None
    """
    print("***" * 20)
    print("Save Result to Firebase Database")
    print("***" * 20)

    ref = db.reference(f"results/{index}")
    ref.update({"result": prediction})
