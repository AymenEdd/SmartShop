Set/remove Firebase `admin` custom claim

Steps:

1. Create a Firebase service account key:
   - Go to Firebase Console > Project Settings > Service Accounts
   - Click "Generate new private key" and download the JSON file. Keep it secret.

2. Install the Python admin SDK (in your venv):

```bash
pip install firebase-admin
```

3. Run the helper script (examples):

```bash
# Set admin by email
python scripts/set_admin_claim.py --email admin@example.com --key /path/to/serviceAccountKey.json

# Remove admin claim
python scripts/set_admin_claim.py --email admin@example.com --key /path/to/serviceAccountKey.json --remove
```

4. On the client, force a token refresh so ID token includes new claims:

- Android: call `getIdToken(true)` or sign out + sign in again.

Security note: Never commit the service account JSON to version control. Limit access to keys and rotate if leaked.
