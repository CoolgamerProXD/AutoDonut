#!/usr/bin/env python3
"""
Regression tests for the price importer.

    python3 tools/market-import/test_import.py

The fixture below is synthetic on purpose - it mimics the page's structure
without copying DonutSMP Terminal's actual data into this repository. The
awkward shapes it covers (bare prices, K/M/B suffixes, unsigned percentages,
four-digit percentages, decoys inside <script>) are all real cases taken from
the live page.
"""

import json
import sys
import tempfile
from pathlib import Path

sys.path.insert(0, str(Path(__file__).parent))
import import_prices as mod  # noqa: E402


def row(item, name, grade, price, change):
    return (
        f'<a class="row" href="/item/{item}">'
        f'<img src="https://www.donutsmpterminal.com/item-icons/{item}.png" alt="{name} in DonutSMP"/>'
        f'<span><b>{name}</b></span><span>{grade}</span><span>{price}</span><span>{change}%</span></a>'
    )


FIXTURE = (
    "<!doctype html><html><head>"
    '<script>var decoy = "notanitem Z9K+1.0%";</script>'
    "<style>.row{color:red}</style></head><body>"
    '<img src="/donut-logo.png" alt="site logo">'
    + row("test_bare_price", "Test Bare Price", "B", "391", "+56.4")
    + row("test_k_suffix", "Test K Suffix", "C", "48K", "-52.2")
    + row("test_m_suffix", "Test M Suffix", "S", "2.7M", "+92.9")
    + row("test_b_suffix", "Test B Suffix", "D", "75B", "0.0")
    + row("test_unsigned_change", "Test Unsigned Change", "C", "99K", "0.0")
    + row("test_big_change", "Test Big Change", "S", "50K", "+2361.8")
    + row("test_decimal_price", "Test Decimal Price", "C", "3.9K", "-91.7")
    + row("test_comma_price", "Test Comma Price", "A", "1,234", "-0.3")
    + row("test_grade_letter_in_name", "A Sad Dog Cask", "A", "109", "+18.5")
    + "</body></html>"
)

EXPECTED = {
    "test_bare_price": 391.0,
    "test_k_suffix": 48_000.0,
    "test_m_suffix": 2_700_000.0,
    "test_b_suffix": 75_000_000_000.0,
    "test_unsigned_change": 99_000.0,
    "test_big_change": 50_000.0,
    "test_decimal_price": 3_900.0,
    "test_comma_price": 1_234.0,
    "test_grade_letter_in_name": 109.0,
}

failures = []


def check(label, got, want):
    if got != want:
        failures.append(f"{label}: got {got!r}, want {want!r}")
        print(f"  FAIL {label}: got {got!r}, want {want!r}")
    else:
        print(f"  ok   {label}")


def main():
    print("parsing fixture...")
    rows = mod.parse(FIXTURE)
    by_item = {r["item"]: r for r in rows}

    check("item count", len(rows), len(EXPECTED))
    check("decoy inside <script> ignored", "notanitem" in by_item, False)
    check("site logo is not an item", "donut-logo" in by_item, False)

    for item, price in EXPECTED.items():
        check(f"price of {item}", by_item.get(item, {}).get("price"), price)

    check("grade parsed", by_item["test_m_suffix"]["grade"], "S")
    check("negative change", by_item["test_k_suffix"]["change_7d"], -52.2)
    check("unsigned change", by_item["test_unsigned_change"]["change_7d"], 0.0)
    check("four-digit change", by_item["test_big_change"]["change_7d"], 2361.8)

    print("checking output shape matches MarketDatabase (Map<String, List<Double>>)...")
    db = mod.to_market_db(rows, "minecraft")
    check("namespaced key", "minecraft:test_k_suffix" in db, True)
    check("value is a list", isinstance(db["minecraft:test_k_suffix"], list), True)
    check("sample is a float", isinstance(db["minecraft:test_k_suffix"][0], float), True)
    check("json round-trips", json.loads(json.dumps(db))["minecraft:test_m_suffix"], [2_700_000.0])

    print("checking filters...")
    graded = [r for r in rows if r["grade"] in mod.GRADE_ORDER[: mod.GRADE_ORDER.index("B") + 1]]
    check("min-grade B keeps only S/A/B", {r["grade"] for r in graded}, {"S", "A", "B"})

    print("checking --merge keeps existing samples...")
    with tempfile.TemporaryDirectory() as tmp:
        existing = Path(tmp) / "existing.json"
        existing.write_text(json.dumps({"minecraft:test_k_suffix": [1.0, 2.0]}))
        out = Path(tmp) / "out.json"
        fixture = Path(tmp) / "fixture.html"
        fixture.write_text(FIXTURE)
        rc = mod.main(["--from-file", str(fixture), "--out", str(out), "--merge", str(existing)])
        check("merge exit code", rc, 0)
        merged = json.loads(out.read_text())
        check("merge appended, did not clobber",
              merged["minecraft:test_k_suffix"], [1.0, 2.0, 48_000.0])

    print()
    if failures:
        print(f"{len(failures)} FAILURE(S)")
        return 1
    print("all checks passed")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
