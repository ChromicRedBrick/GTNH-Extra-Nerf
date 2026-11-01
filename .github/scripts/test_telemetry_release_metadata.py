"""The release descriptor is a public CLI contract used to register published JARs."""

import json
import subprocess
import sys
import tempfile
import unittest
from pathlib import Path
from zipfile import ZipFile, ZipInfo


SCRIPT = Path(__file__).with_name("telemetry_release_metadata.py")
# Independently measured with a SHA-256 tool on these fixed ZIP fixtures.
FIRST_SHA256 = "83f257cf3416c70b33a161571bfbdd800004ce46753a8b8af3073c88f5098c3c"


def release_jar(path: Path, value: int = 1, tag: str = "0.0.6", version: str | None = None) -> None:
    entries = [
        ("META-INF/MANIFEST.MF", f"Manifest-Version: 1.0\r\nExtraNerf-Telemetry-Release: {tag}\r\n\r\n".encode()),
        ("mcmod.info", json.dumps([{"modid": "gtnhextranerf", "version": tag if version is None else version}], separators=(",", ":")).encode()),
        ("fixture.class", bytes([value])),
    ]
    with ZipFile(path, "w") as jar:
        for name, data in entries:
            entry = ZipInfo(name, (2026, 1, 1, 0, 0, 0))
            entry.create_system = 0
            entry.external_attr = 32
            jar.writestr(entry, data)


class TelemetryReleaseMetadataTest(unittest.TestCase):
    def test_descriptor_has_one_version_and_its_registered_jar_hash(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            artifact = root / "release.jar"
            descriptor = root / "release.json"
            release_jar(artifact)
            result = subprocess.run(
                [sys.executable, str(SCRIPT), "--jar", str(artifact), "--tag", "0.0.6", "--output", str(descriptor)],
                capture_output=True,
                text=True,
                timeout=15,
            )
            self.assertEqual(0, result.returncode, result.stderr)
            self.assertEqual(
                {"modVersion": "0.0.6", "jarSha256": FIRST_SHA256, "artifact": "release.jar"},
                json.loads(descriptor.read_text(encoding="utf-8")),
            )

    def test_untagged_jar_cannot_be_registered_as_a_release(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            artifact = root / "untagged.jar"
            descriptor = root / "release.json"
            release_jar(artifact, tag="")
            result = subprocess.run(
                [sys.executable, str(SCRIPT), "--jar", str(artifact), "--tag", "", "--output", str(descriptor)],
                capture_output=True, text=True, timeout=15,
            )
            self.assertNotEqual(0, result.returncode)
            self.assertFalse(descriptor.exists())

    def test_release_tag_and_mod_version_must_identify_the_same_release(self) -> None:
        with tempfile.TemporaryDirectory() as temporary:
            root = Path(temporary)
            artifact = root / "mismatched.jar"
            descriptor = root / "release.json"
            release_jar(artifact, tag="0.0.7", version="0.0.6")
            result = subprocess.run(
                [sys.executable, str(SCRIPT), "--jar", str(artifact), "--tag", "0.0.7", "--output", str(descriptor)],
                capture_output=True, text=True, timeout=15,
            )
            self.assertNotEqual(0, result.returncode)
            self.assertFalse(descriptor.exists())


if __name__ == "__main__":
    unittest.main()
