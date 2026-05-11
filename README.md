# envguard

A shell-based pre-commit hook tool that scans for accidentally committed environment variables and secrets.

---

## Installation

Clone the repository and run the install script:

```bash
git clone https://github.com/youruser/envguard.git
cd envguard && ./install.sh
```

---

## Usage

Once installed, **envguard** runs automatically before each `git commit`. It scans staged files for common secret patterns such as API keys, tokens, and `.env` variable assignments.

You can also run it manually against your staged changes:

```bash
./envguard.sh
```

**Example output when a secret is detected:**

```
[envguard] WARNING: Possible secret found in src/config.java (line 12)
  > API_KEY=AIzaSyD3x...
[envguard] Commit blocked. Remove secrets before committing.
```

To bypass the hook in exceptional cases *(not recommended)*:

```bash
git commit --no-verify
```

---

## Configuration

Customize which patterns envguard scans for by editing `patterns.conf` in the project root. Each line represents a regex pattern.

---

## Supported Patterns

- API keys and tokens (`API_KEY`, `SECRET_KEY`, `ACCESS_TOKEN`)
- AWS credentials (`AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`)
- Private keys and certificates
- `.env` style variable assignments

---

## Requirements

- Java 11+
- Bash 4.0+
- Git 2.x

---

## License

This project is licensed under the [MIT License](LICENSE).