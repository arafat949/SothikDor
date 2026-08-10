"""Shared Firebase Admin initialisation.

Credentials are never stored in the repository. Provide them at runtime with
either of:

  * FIREBASE_SERVICE_ACCOUNT       - the service account JSON itself
  * GOOGLE_APPLICATION_CREDENTIALS - path to the service account JSON file
"""

import json
import os

import firebase_admin
from firebase_admin import credentials

DEFAULT_DATABASE_URL = "https://sothik-dor-default-rtdb.asia-southeast1.firebasedatabase.app"

_MISSING_CREDENTIALS_MESSAGE = (
    "Firebase credentials not found. Set FIREBASE_SERVICE_ACCOUNT to the service "
    "account JSON, or GOOGLE_APPLICATION_CREDENTIALS to the path of the JSON file."
)


def load_credentials():
    raw_json = os.environ.get("FIREBASE_SERVICE_ACCOUNT")
    if raw_json:
        return credentials.Certificate(json.loads(raw_json))

    key_path = os.environ.get("GOOGLE_APPLICATION_CREDENTIALS")
    if key_path:
        if not os.path.isfile(key_path):
            raise RuntimeError("GOOGLE_APPLICATION_CREDENTIALS points to a missing file: " + key_path)
        return credentials.Certificate(key_path)

    raise RuntimeError(_MISSING_CREDENTIALS_MESSAGE)


def database_url():
    return os.environ.get("FIREBASE_URL", DEFAULT_DATABASE_URL)


def init_firebase():
    if not firebase_admin._apps:
        firebase_admin.initialize_app(load_credentials(), {"databaseURL": database_url()})
