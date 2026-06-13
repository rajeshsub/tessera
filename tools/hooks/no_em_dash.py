#!/usr/bin/env python3
"""Reject em dash (U+2014) and en dash (U+2013) in repo content.

Project rule: use plain hyphens. Runs as a pre-commit hook over staged text files.
"""
import sys

BANNED = {
    "—": "em dash (U+2014)",
    "–": "en dash (U+2013)",
}


def main(paths: list[str]) -> int:
    failed = False
    for path in paths:
        try:
            with open(path, encoding="utf-8") as handle:
                lines = handle.readlines()
        except (OSError, UnicodeDecodeError):
            continue
        for number, line in enumerate(lines, start=1):
            for char, label in BANNED.items():
                if char in line:
                    column = line.index(char) + 1
                    print(f"{path}:{number}:{column}: found {label}, use a hyphen")
                    failed = True
    return 1 if failed else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
