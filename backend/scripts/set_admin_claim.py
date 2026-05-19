#!/usr/bin/env python3
"""
Set or remove an `admin` custom claim for a Firebase user using a service account JSON key.

Usage:
  python set_admin_claim.py --email user@example.com --key path/to/serviceAccountKey.json
  python set_admin_claim.py --uid UID --key path/to/serviceAccountKey.json --remove

Be careful: do NOT commit the service account key to source control.
"""
import argparse
from firebase_admin import credentials, initialize_app, auth


def main():
    parser = argparse.ArgumentParser(description="Set/remove admin custom claim for Firebase user")
    parser.add_argument('--email', help='User email to target')
    parser.add_argument('--uid', help='User UID to target')
    parser.add_argument('--key', required=True, help='Path to Firebase service account JSON key')
    parser.add_argument('--remove', action='store_true', help='Remove the admin claim instead of setting it')
    args = parser.parse_args()

    cred = credentials.Certificate(args.key)
    initialize_app(cred)

    if args.email:
        user = auth.get_user_by_email(args.email)
    elif args.uid:
        user = auth.get_user(args.uid)
    else:
        raise SystemExit('Specify --email or --uid')

    if args.remove:
        auth.set_custom_user_claims(user.uid, {})
        print(f'Removed custom claims for {user.uid}')
    else:
        auth.set_custom_user_claims(user.uid, {'admin': True})
        print(f'Set admin claim for {user.uid}')


if __name__ == '__main__':
    main()
