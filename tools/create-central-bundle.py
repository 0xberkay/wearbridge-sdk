#!/usr/bin/env python3
"""Package a signed Maven-local publication for manual Central Portal upload."""

import argparse
import hashlib
import json
from pathlib import Path
import zipfile


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("version")
    args = parser.parse_args()
    version = args.version
    if not version or any(c not in "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ.-" for c in version):
        parser.error("Invalid version")
    relative = Path("io/github/0xberkay/wearbridge-sdk") / version
    source = Path.home() / ".m2/repository" / relative
    stem = f"wearbridge-sdk-{version}"
    artifacts = [source / (stem + suffix) for suffix in
                 (".aar", ".pom", ".module", "-sources.jar", "-javadoc.jar")]
    files = []
    for artifact in artifacts:
        for file in (artifact, Path(str(artifact) + ".asc")):
            if not file.is_file():
                raise SystemExit(f"Missing publication file: {file}. Run publishToMavenLocal with signing configured.")
            files.append(file)

    # Metadata is part of the publication; all referenced files must match it.
    metadata = json.loads((source / (stem + ".module")).read_text())
    for variant in metadata.get("variants", []):
        for reference in variant.get("files", []):
            artifact = source / reference["url"]
            if artifact not in artifacts:
                raise SystemExit(f"Metadata references an unbundled artifact: {artifact}")
            data = artifact.read_bytes()
            if len(data) != reference["size"]:
                raise SystemExit(f"Metadata size mismatch: {artifact}")
            for algorithm in ("md5", "sha1", "sha256", "sha512"):
                if algorithm in reference and hashlib.new(algorithm, data).hexdigest() != reference[algorithm]:
                    raise SystemExit(f"Metadata {algorithm} mismatch: {artifact}")

    root = Path(__file__).resolve().parents[1]
    output = root / "wearbridge-sdk/build/distributions" / f"{stem}-bundle.zip"
    output.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(output, "w", zipfile.ZIP_DEFLATED) as archive:
        for file in files:
            data = file.read_bytes()
            name = str(relative / file.name)
            archive.writestr(name, data)
            for algorithm in ("md5", "sha1"):
                archive.writestr(name + "." + algorithm, hashlib.new(algorithm, data).hexdigest())
    print(output)
    print("Included signed AAR, POM, Gradle metadata, sources, javadoc, and checksums.")


if __name__ == "__main__":
    main()
