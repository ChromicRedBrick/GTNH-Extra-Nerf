#!/usr/bin/env python3

import argparse
import shutil
import subprocess
import sys
import threading
import time
from pathlib import Path


READY_MARKER = "]: Done ("
PATCH_AUDIT_FAILURE = "GTNH Extra Nerf integrity check failed"


def copy_server_evidence(root: Path, evidence_directory: Path) -> None:
    runtime_path_file = root / "build" / "fullpack" / "server-runtime.path"
    if not runtime_path_file.is_file():
        return

    runtime_directory = Path(runtime_path_file.read_text(encoding="utf-8").strip())
    latest_log = runtime_directory / "logs" / "latest.log"
    if latest_log.is_file():
        shutil.copy2(latest_log, evidence_directory / "latest.log")

    crash_reports = runtime_directory / "crash-reports"
    if crash_reports.is_dir():
        shutil.copytree(crash_reports, evidence_directory / "crash-reports", dirs_exist_ok=True)


def patch_audit_failed(evidence_directory: Path) -> bool:
    for log_name in ("console.log", "latest.log"):
        log_path = evidence_directory / log_name
        if log_path.is_file() and PATCH_AUDIT_FAILURE in log_path.read_text(encoding="utf-8", errors="replace"):
            return True
    return False


def has_crash_report(evidence_directory: Path) -> bool:
    crash_report_directory = evidence_directory / "crash-reports"
    return crash_report_directory.is_dir() and any(
        path.is_file() for path in crash_report_directory.rglob("*")
    )


def stop_process(process: subprocess.Popen[str], timeout: float) -> int:
    try:
        return process.wait(timeout=timeout)
    except subprocess.TimeoutExpired:
        process.terminate()
        try:
            process.wait(timeout=10)
        except subprocess.TimeoutExpired:
            process.kill()
            process.wait()
        return 124


def run_server(
    root: Path,
    gradle_command: list[str],
    ready_timeout: float,
    stop_timeout: float,
) -> int:
    evidence_directory = root / "build" / "fullpack" / "server-evidence"
    if evidence_directory.exists():
        shutil.rmtree(evidence_directory)
    evidence_directory.mkdir(parents=True, exist_ok=True)
    console_log_path = evidence_directory / "console.log"
    ready = threading.Event()

    command = [
        *gradle_command,
        "--no-daemon",
        "--stacktrace",
        "-PfullPackCi",
        "runFullPackServer",
    ]
    with console_log_path.open("w", encoding="utf-8", newline="") as console_log:
        process = subprocess.Popen(
            command,
            cwd=root,
            stdin=subprocess.PIPE,
            stdout=subprocess.PIPE,
            stderr=subprocess.STDOUT,
            text=True,
            encoding="utf-8",
            errors="replace",
            bufsize=1,
        )

        def read_output() -> None:
            assert process.stdout is not None
            for line in process.stdout:
                sys.stdout.write(line)
                sys.stdout.flush()
                console_log.write(line)
                console_log.flush()
                if READY_MARKER in line:
                    ready.set()

        output_thread = threading.Thread(target=read_output, name="full-pack-server-output", daemon=True)
        output_thread.start()

        deadline = time.monotonic() + ready_timeout
        while not ready.is_set() and process.poll() is None and time.monotonic() < deadline:
            ready.wait(timeout=0.1)

        if not ready.is_set():
            if process.poll() is None:
                print(f"Server did not become ready within {ready_timeout:g} seconds", file=sys.stderr)
                process.terminate()
            exit_code = stop_process(process, stop_timeout)
            output_thread.join(timeout=5)
            copy_server_evidence(root, evidence_directory)
            return exit_code if exit_code != 0 else 1

        assert process.stdin is not None
        process.stdin.write("stop\n")
        process.stdin.flush()
        exit_code = stop_process(process, stop_timeout)
        output_thread.join(timeout=5)

    copy_server_evidence(root, evidence_directory)
    if exit_code != 0:
        print(f"Full-pack server exited with code {exit_code}", file=sys.stderr)
        return exit_code
    if patch_audit_failed(evidence_directory):
        print("PatchAudit reported an integrity failure", file=sys.stderr)
        return 1
    if has_crash_report(evidence_directory):
        print("The full-pack server created a crash report", file=sys.stderr)
        return 1
    return 0


def parse_arguments() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Run and verify the GTNH full-pack server")
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--ready-timeout", type=float, default=1200)
    parser.add_argument("--stop-timeout", type=float, default=180)
    parser.add_argument("--gradle-command", nargs="+", default=["./gradlew"])
    return parser.parse_args()


def main() -> int:
    arguments = parse_arguments()
    return run_server(
        arguments.root.resolve(),
        arguments.gradle_command,
        arguments.ready_timeout,
        arguments.stop_timeout,
    )


if __name__ == "__main__":
    raise SystemExit(main())
