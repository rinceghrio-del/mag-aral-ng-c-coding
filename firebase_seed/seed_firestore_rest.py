"""
Kapag hindi ka pinapayagang gumawa ng Service Account key (may error na
"Key creation is not allowed... restricted by organization policies"),
gamitin ang script na ito sa halip. Hindi ito nangangailangan ng
serviceAccountKey.json — gagamit lang tayo ng email/password ng isang
account na naka-register na sa app mo, at ang Web API Key na makikita sa
google-services.json.

Setup:
    pip install requests

Patakbo:
    python seed_firestore_rest.py
(Tatanungin ka ng script ng Project ID, Web API Key, email, at password.)
"""

import json
import sys
import requests

def get_input(prompt, default=None):
    suffix = f" [{default}]" if default else ""
    value = input(f"{prompt}{suffix}: ").strip()
    return value or default

print("=== RUSTECH C++ Academy — Firestore Seeder (REST, walang service account) ===\n")

project_id = get_input("Firebase Project ID (hal. rustech-cpp-academy)")
api_key = get_input("Web API Key (galing sa google-services.json, 'current_key' field)")
email = get_input("Email (ng account na naka-register mo na sa app)")
password = get_input("Password ng account na 'yun")

# 1. Mag-sign in gamit ang Firebase Auth REST API para makakuha ng ID token.
print("\nNagsa-sign in...")
signin_resp = requests.post(
    f"https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key={api_key}",
    json={"email": email, "password": password, "returnSecureToken": True},
)
if signin_resp.status_code != 200:
    print("Hindi maka-sign in:", signin_resp.text)
    sys.exit(1)

id_token = signin_resp.json()["idToken"]
print("Sign-in successful.")

BASE_URL = f"https://firestore.googleapis.com/v1/projects/{project_id}/databases/(default)/documents"
HEADERS = {"Authorization": f"Bearer {id_token}", "Content-Type": "application/json"}


def to_firestore_value(value):
    """Ikino-convert ang plain Python value papuntang Firestore REST format."""
    if isinstance(value, bool):
        return {"booleanValue": value}
    if isinstance(value, int):
        return {"integerValue": str(value)}
    if isinstance(value, float):
        return {"doubleValue": value}
    if isinstance(value, str):
        return {"stringValue": value}
    if isinstance(value, list):
        return {"arrayValue": {"values": [to_firestore_value(v) for v in value]}}
    if isinstance(value, dict):
        return {"mapValue": {"fields": {k: to_firestore_value(v) for k, v in value.items()}}}
    return {"nullValue": None}


def to_firestore_fields(doc: dict):
    return {k: to_firestore_value(v) for k, v in doc.items() if k != "id"}


with open("lessons_seed.json", "r", encoding="utf-8") as f:
    data = json.load(f)

# 2. I-upload ang lessons (PATCH gamit ang document ID = lessonId).
for lesson in data["lessons"]:
    lesson_id = lesson["id"]
    url = f"{BASE_URL}/lessons/{lesson_id}"
    body = {"fields": to_firestore_fields(lesson)}
    resp = requests.patch(url, headers=HEADERS, json=body)
    if resp.status_code == 200:
        print(f"Na-upload ang lesson: {lesson_id}")
    else:
        print(f"BIGO sa lesson {lesson_id}:", resp.text)

# 3. I-upload ang quizzes bilang subcollection: quizzes/{lessonId}/questions/{autoId}
for lesson_id, questions in data["quizzes"].items():
    # Parent placeholder doc
    parent_url = f"{BASE_URL}/quizzes/{lesson_id}"
    requests.patch(parent_url, headers=HEADERS, json={"fields": {"lessonId": to_firestore_value(lesson_id)}})

    for q in questions:
        url = f"{BASE_URL}/quizzes/{lesson_id}/questions"
        body = {"fields": to_firestore_fields(q)}
        resp = requests.post(url, headers=HEADERS, json=body)
        if resp.status_code != 200:
            print(f"BIGO sa quiz question ng {lesson_id}:", resp.text)

    print(f"Na-upload ang quiz para sa: {lesson_id} ({len(questions)} questions)")

print("\nTapos na! Naka-seed na ang Firestore.")
