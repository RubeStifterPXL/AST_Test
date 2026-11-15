#!/usr/bin/env python3
"""Maakt een Markdown-samenvatting van de Surefire JUnit-XML-rapporten.

Gebruik: python3 ci/summary.py target/surefire-reports "chromium / shard 1" >> "$GITHUB_STEP_SUMMARY"

Toont totalen, elke falende test (met de eerste regel van de fout) en elke test die pas na
een retry slaagde ("flaky"), zodat die altijd zichtbaar is en onderzocht wordt.
"""
import glob
import os
import sys
import xml.etree.ElementTree as ET


def first_line(text, limit=220):
    line = (text or "").strip().splitlines()[0] if (text or "").strip() else ""
    line = line.replace("|", "\\|")
    return line if len(line) <= limit else line[:limit] + "..."


def main():
    report_dir = sys.argv[1] if len(sys.argv) > 1 else "target/surefire-reports"
    label = sys.argv[2] if len(sys.argv) > 2 else ""
    files = sorted(glob.glob(os.path.join(report_dir, "TEST-*.xml")))

    total = failed = errored = skipped = 0
    failures, flakes = [], []
    for path in files:
        root = ET.parse(path).getroot()
        for case in root.iter("testcase"):
            total += 1
            name = f"{case.get('classname', '').rsplit('.', 1)[-1]} › {case.get('name')}"
            failure = case.find("failure")
            error = case.find("error")
            if failure is not None or error is not None:
                node = failure if failure is not None else error
                if failure is not None:
                    failed += 1
                else:
                    errored += 1
                failures.append((name, first_line(node.get("message") or node.text)))
            elif case.find("skipped") is not None:
                skipped += 1
            flaky = case.findall("flakyFailure") + case.findall("flakyError")
            if flaky:
                flakes.append((name, first_line(flaky[0].get("message") or flaky[0].text)))

    passed = total - failed - errored - skipped
    status = "✅" if failed + errored == 0 else "❌"
    print(f"## {status} QAcademy web tests {('- ' + label) if label else ''}\n")
    print("| Tests | Geslaagd | Gefaald | Fout | Overgeslagen | Flaky (pas groen na retry) |")
    print("|---|---|---|---|---|---|")
    print(f"| {total} | {passed} | {failed} | {errored} | {skipped} | {len(flakes)} |\n")
    if not files:
        print("> Geen JUnit-rapporten gevonden: de build faalde vóór de tests (compilatie of setup).\n")
    if failures:
        print("### Gefaalde tests\n")
        print("| Test | Fout |\n|---|---|")
        for name, message in failures:
            print(f"| {name} | {message} |")
        print()
    if flakes:
        print("### ⚠️ Flaky tests: slaagden pas na een retry, onderzoeken!\n")
        print("| Test | Fout bij eerste poging |\n|---|---|")
        for name, message in flakes:
            print(f"| {name} | {message} |")
        print()
    print("Rapporten: zie de artifacts `web-report-*` (HTML + JUnit-XML) en `web-traces-*` "
          "(Playwright-traces en screenshots van gefaalde tests, openen met `npx playwright show-trace <zip>`).\n")


if __name__ == "__main__":
    main()
