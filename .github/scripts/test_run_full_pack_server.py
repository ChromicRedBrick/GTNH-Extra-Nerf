#!/usr/bin/env python3

import subprocess
import sys
import tempfile
import textwrap
import unittest
from pathlib import Path


SCRIPT = Path(__file__).with_name("run_full_pack_server.py")


class FullPackServerHarnessTest(unittest.TestCase):

    def test_ready_server_receives_stop_and_preserves_evidence(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            fake_gradle = root / "fake_gradle.py"
            fake_gradle.write_text(
                textwrap.dedent(
                    """\
                    import sys
                    from pathlib import Path

                    root = Path.cwd()
                    if "runFullPackServer" not in sys.argv or "-PfullPackCi" not in sys.argv:
                        raise SystemExit(10)

                    runtime = root / "fake-runtime"
                    (runtime / "logs").mkdir(parents=True)
                    (root / "build" / "fullpack").mkdir(parents=True, exist_ok=True)
                    (root / "build" / "fullpack" / "server-runtime.path").write_text(
                        str(runtime), encoding="utf-8"
                    )
                    (runtime / "logs" / "latest.log").write_text(
                        "server log\\n", encoding="utf-8"
                    )

                    print('[Server thread/INFO]: Done (1.234s)! For help, type "help" or "?"', flush=True)
                    if sys.stdin.readline().strip() != "stop":
                        raise SystemExit(11)
                    print("Stopping server", flush=True)
                    """
                ),
                encoding="utf-8",
            )

            result = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT),
                    "--root",
                    str(root),
                    "--ready-timeout",
                    "5",
                    "--stop-timeout",
                    "5",
                    "--gradle-command",
                    sys.executable,
                    str(fake_gradle),
                ],
                capture_output=True,
                text=True,
                timeout=15,
                check=False,
            )

            self.assertEqual(0, result.returncode, result.stdout + result.stderr)
            evidence = root / "build" / "fullpack" / "server-evidence"
            self.assertIn("Done (1.234s)", (evidence / "console.log").read_text(encoding="utf-8"))
            self.assertEqual("server log\n", (evidence / "latest.log").read_text(encoding="utf-8"))

    def test_patch_audit_failure_cannot_pass_after_server_ready(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            fake_gradle = root / "fake_gradle.py"
            fake_gradle.write_text(
                textwrap.dedent(
                    """\
                    import sys
                    from pathlib import Path

                    root = Path.cwd()
                    runtime = root / "fake-runtime"
                    (runtime / "logs").mkdir(parents=True)
                    (root / "build" / "fullpack").mkdir(parents=True, exist_ok=True)
                    (root / "build" / "fullpack" / "server-runtime.path").write_text(
                        str(runtime), encoding="utf-8"
                    )
                    failure = "GTNH Extra Nerf integrity check failed: missing patch"
                    (runtime / "logs" / "latest.log").write_text(failure, encoding="utf-8")
                    print(failure, flush=True)
                    print('[Server thread/INFO]: Done (1.234s)! For help, type "help" or "?"', flush=True)
                    if sys.stdin.readline().strip() != "stop":
                        raise SystemExit(11)
                    """
                ),
                encoding="utf-8",
            )

            result = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT),
                    "--root",
                    str(root),
                    "--ready-timeout",
                    "5",
                    "--stop-timeout",
                    "5",
                    "--gradle-command",
                    sys.executable,
                    str(fake_gradle),
                ],
                capture_output=True,
                text=True,
                timeout=15,
                check=False,
            )

            self.assertNotEqual(0, result.returncode, result.stdout + result.stderr)
            self.assertIn("GTNH Extra Nerf integrity check failed", result.stdout)

    def test_crash_report_fails_even_when_server_exits_cleanly(self) -> None:
        with tempfile.TemporaryDirectory() as temporary_directory:
            root = Path(temporary_directory)
            fake_gradle = root / "fake_gradle.py"
            fake_gradle.write_text(
                textwrap.dedent(
                    """\
                    import sys
                    from pathlib import Path

                    root = Path.cwd()
                    runtime = root / "fake-runtime"
                    (runtime / "logs").mkdir(parents=True)
                    (runtime / "crash-reports").mkdir()
                    (runtime / "crash-reports" / "crash.txt").write_text(
                        "simulated crash", encoding="utf-8"
                    )
                    (root / "build" / "fullpack").mkdir(parents=True, exist_ok=True)
                    (root / "build" / "fullpack" / "server-runtime.path").write_text(
                        str(runtime), encoding="utf-8"
                    )
                    (runtime / "logs" / "latest.log").write_text("server log", encoding="utf-8")
                    print('[Server thread/INFO]: Done (1.234s)! For help, type "help" or "?"', flush=True)
                    if sys.stdin.readline().strip() != "stop":
                        raise SystemExit(11)
                    """
                ),
                encoding="utf-8",
            )

            result = subprocess.run(
                [
                    sys.executable,
                    str(SCRIPT),
                    "--root",
                    str(root),
                    "--ready-timeout",
                    "5",
                    "--stop-timeout",
                    "5",
                    "--gradle-command",
                    sys.executable,
                    str(fake_gradle),
                ],
                capture_output=True,
                text=True,
                timeout=15,
                check=False,
            )

            self.assertNotEqual(0, result.returncode, result.stdout + result.stderr)
            evidence_crash = (
                root / "build" / "fullpack" / "server-evidence" / "crash-reports" / "crash.txt"
            )
            self.assertEqual("simulated crash", evidence_crash.read_text(encoding="utf-8"))


if __name__ == "__main__":
    unittest.main()
