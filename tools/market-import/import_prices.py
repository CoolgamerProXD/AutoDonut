#!/usr/bin/env python3
"""
Seed AutoDonut's local price database from DonutSMP Terminal.

    python3 tools/market-import/import_prices.py --out autodonut_market.json

Reads the public all-items page at https://www.donutsmpterminal.com/all_items
and converts it into the exact JSON shape AutoDonut's MarketDatabase uses, so
you can drop it straight into your config folder or load it in-game with
`/autodonuteconomy import <file>`.

Why this is a standalone script and not part of the mod
-------------------------------------------------------
* The mod makes no network requests except the AI advisor, and only when you
  have set a key. Baking a scraper into it would mean every copy of AutoDonut
  hammering someone else's website on a timer. This way you fetch once, on
  purpose, when you actually want fresh numbers.
* It keeps the data out of the mod's jar and out of the git repository. The
  price data belongs to DonutSMP Terminal, not to us.

Manners (all enforced below, not just promised)
-----------------------------------------------
* Checks the site's robots.txt before fetching anything and obeys it. At the
  time of writing robots.txt allows /all_items and explicitly disallows /api/,
  so this reads the public page and never touches the API.
* Sends a descriptive User-Agent so the operators can see what we are.
* Fetches a single page, once, and caches the response. Re-runs read the cache
  unless you pass --refresh.

Requires nothing but a Python 3.9+ standard library.
"""

from __future__ import annotations

import argparse
import json
import re
import sys
import time
import urllib.error
import urllib.request
import urllib.robotparser
from html.parser import HTMLParser
from pathlib import Path
from urllib.parse import urljoin, urlparse

SITE = "https://www.donutsmpterminal.com"
PAGE = f"{SITE}/all_items"
USER_AGENT = (
    "AutoDonut-MarketImport/1.0 "
    "(+https://github.com/CoolgamerProXD/AutoDonut; personal price-db seeding)"
)

# Rows look like, once tags are stripped:
#   <icon:acacia_boat>Acacia BoatC48K-52.2%
#   <icon:ancient_debris>Ancient DebrisS2.7M+92.9%
#   <icon:armadillo_scute>Armadillo ScuteB391+56.4%
# The item id comes from the icon URL, which is far more reliable than the
# display name. Grade is the single capital letter directly before the price.
# Requiring the trailing one-decimal percentage is what disambiguates a price
# like "99K" followed by a change of "0.0%".
ROW_RE = re.compile(
    r"\x00(?P<item>[a-z0-9_]+)\x00"          # the icon marker we injected
    r".*?"                                    # display name, which we ignore
    r"(?P<grade>[SABCDEF])"                   # viability grade
    r"(?P<price>\d[\d,]*(?:\.\d+)?[KMBT]?)"   # price, possibly abbreviated
    r"(?P<change>[+-]?\d+(?:\.\d+)?)%",       # 7-day change
    re.DOTALL,
)

SUFFIXES = {"K": 1_000, "M": 1_000_000, "B": 1_000_000_000, "T": 1_000_000_000_000}
GRADE_ORDER = ["S", "A", "B", "C", "D", "E", "F"]


class _Stripper(HTMLParser):
    """Flattens HTML to text, leaving \\x00<item_id>\\x00 where each item icon was."""

    def __init__(self) -> None:
        super().__init__(convert_charrefs=True)
        self.out: list[str] = []
        self._skip = 0

    def handle_starttag(self, tag, attrs):
        if tag in ("script", "style"):
            self._skip += 1
            return
        if tag == "img":
            src = dict(attrs).get("src") or ""
            m = re.search(r"/item-icons/([A-Za-z0-9_]+)\.png", src)
            if m:
                self.out.append(f"\x00{m.group(1).lower()}\x00")

    def handle_endtag(self, tag):
        if tag in ("script", "style") and self._skip:
            self._skip -= 1

    def handle_data(self, data):
        if not self._skip:
            self.out.append(data)

    def text(self) -> str:
        return re.sub(r"[ \t\r\n\u00a0]+", " ", "".join(self.out))


def check_robots(url: str, *, insist: bool) -> None:
    """Refuse to fetch anything robots.txt disallows for us."""
    robots_url = urljoin(url, "/robots.txt")
    parser = urllib.robotparser.RobotFileParser()
    parser.set_url(robots_url)
    try:
        req = urllib.request.Request(robots_url, headers={"User-Agent": USER_AGENT})
        with urllib.request.urlopen(req, timeout=20) as resp:
            parser.parse(resp.read().decode("utf-8", "replace").splitlines())
    except Exception as exc:  # noqa: BLE001
        if insist:
            raise SystemExit(f"could not read {robots_url} ({exc}); refusing to fetch")
        print(f"warning: could not read robots.txt ({exc})", file=sys.stderr)
        return

    if not parser.can_fetch(USER_AGENT, url):
        raise SystemExit(
            f"robots.txt disallows {urlparse(url).path} for this agent. Not fetching.\n"
            "That is the site owner's call, and it is the right answer here."
        )
    print(f"robots.txt: {urlparse(url).path} is allowed", file=sys.stderr)


def fetch(url: str, cache: Path, *, refresh: bool, max_age_hours: float) -> str:
    if cache.exists() and not refresh:
        age_h = (time.time() - cache.stat().st_mtime) / 3600.0
        if age_h <= max_age_hours:
            print(f"using cached copy ({age_h:.1f}h old): {cache}", file=sys.stderr)
            return cache.read_text("utf-8")

    check_robots(url, insist=True)
    print(f"fetching {url}", file=sys.stderr)
    req = urllib.request.Request(
        url, headers={"User-Agent": USER_AGENT, "Accept": "text/html"}
    )
    try:
        with urllib.request.urlopen(req, timeout=60) as resp:
            body = resp.read().decode("utf-8", "replace")
    except urllib.error.HTTPError as exc:
        raise SystemExit(f"HTTP {exc.code} fetching {url}") from exc
    except urllib.error.URLError as exc:
        raise SystemExit(f"network error fetching {url}: {exc.reason}") from exc

    cache.parent.mkdir(parents=True, exist_ok=True)
    cache.write_text(body, "utf-8")
    print(f"cached to {cache} ({len(body):,} bytes)", file=sys.stderr)
    return body


def parse_price(raw: str) -> float:
    raw = raw.replace(",", "")
    mult = 1
    if raw and raw[-1].upper() in SUFFIXES:
        mult = SUFFIXES[raw[-1].upper()]
        raw = raw[:-1]
    return float(raw) * mult


def parse(html_or_text: str) -> list[dict]:
    """Accepts raw HTML (normal) or an already-flattened text dump (for tests)."""
    if "<" in html_or_text and ">" in html_or_text:
        stripper = _Stripper()
        stripper.feed(html_or_text)
        text = stripper.text()
    else:
        text = html_or_text

    rows: list[dict] = []
    seen: set[str] = set()
    for m in ROW_RE.finditer(text):
        item = m.group("item")
        if item in seen:
            continue
        seen.add(item)
        rows.append(
            {
                "item": item,
                "grade": m.group("grade"),
                "price": parse_price(m.group("price")),
                "change_7d": float(m.group("change")),
            }
        )
    return rows


def to_market_db(rows: list[dict], namespace: str) -> dict[str, list[float]]:
    """AutoDonut's MarketDatabase format: item id -> list of price samples."""
    return {f"{namespace}:{r['item']}": [r["price"]] for r in rows}


def main(argv: list[str] | None = None) -> int:
    ap = argparse.ArgumentParser(
        description="Seed AutoDonut's price database from DonutSMP Terminal.",
        epilog="Data belongs to donutsmpterminal.com. Don't redistribute it; see CREDITS.md.",
    )
    ap.add_argument("--out", type=Path, default=Path("autodonut_market.json"),
                    help="where to write the market database (default: ./autodonut_market.json)")
    ap.add_argument("--cache", type=Path, default=Path(".cache/all_items.html"),
                    help="cache file for the fetched page")
    ap.add_argument("--refresh", action="store_true", help="ignore the cache and refetch")
    ap.add_argument("--max-age-hours", type=float, default=12.0,
                    help="how old the cache may be before refetching (default: 12)")
    ap.add_argument("--from-file", type=Path,
                    help="parse this local HTML file instead of fetching (offline/testing)")
    ap.add_argument("--namespace", default="minecraft", help="item id namespace (default: minecraft)")
    ap.add_argument("--min-grade", choices=GRADE_ORDER,
                    help="only keep items at this viability grade or better")
    ap.add_argument("--min-price", type=float, default=0.0, help="drop items cheaper than this")
    ap.add_argument("--merge", type=Path,
                    help="merge into an existing market json instead of overwriting")
    ap.add_argument("--csv", type=Path, help="also write a human-readable CSV")
    ap.add_argument("--dry-run", action="store_true", help="parse and report, write nothing")
    args = ap.parse_args(argv)

    if args.from_file:
        body = args.from_file.read_text("utf-8")
        print(f"parsing local file {args.from_file}", file=sys.stderr)
    else:
        body = fetch(PAGE, args.cache, refresh=args.refresh, max_age_hours=args.max_age_hours)

    rows = parse(body)
    if not rows:
        print("ERROR: parsed 0 items. The page layout probably changed -- "
              "check ROW_RE in this script.", file=sys.stderr)
        return 1

    total = len(rows)
    if args.min_grade:
        cutoff = GRADE_ORDER.index(args.min_grade)
        rows = [r for r in rows if r["grade"] in GRADE_ORDER[: cutoff + 1]]
    if args.min_price > 0:
        rows = [r for r in rows if r["price"] >= args.min_price]

    print(f"parsed {total} items, keeping {len(rows)}", file=sys.stderr)
    if rows:
        dearest = max(rows, key=lambda r: r["price"])
        print(f"  most valuable: {dearest['item']} at {dearest['price']:,.0f}", file=sys.stderr)

    db = to_market_db(rows, args.namespace)

    if args.merge and args.merge.exists():
        existing = json.loads(args.merge.read_text("utf-8"))
        for key, samples in db.items():
            existing.setdefault(key, [])
            existing[key] = (existing[key] + samples)[-200:]  # MAX_SAMPLES_PER_ITEM
        db = existing
        print(f"merged into {len(db)} entries from {args.merge}", file=sys.stderr)

    if args.dry_run:
        for r in rows[:15]:
            print(f"  {r['grade']} {args.namespace}:{r['item']:<34} {r['price']:>14,.0f} "
                  f"{r['change_7d']:+.1f}%")
        if len(rows) > 15:
            print(f"  ... and {len(rows) - 15} more")
        print("dry run: nothing written", file=sys.stderr)
        return 0

    args.out.parent.mkdir(parents=True, exist_ok=True)
    args.out.write_text(json.dumps(db, indent=2, sort_keys=True), "utf-8")
    print(f"wrote {len(db)} entries to {args.out}", file=sys.stderr)

    if args.csv:
        lines = ["item,grade,price,change_7d_percent"]
        lines += [f"{args.namespace}:{r['item']},{r['grade']},{r['price']:.0f},{r['change_7d']}"
                  for r in rows]
        args.csv.write_text("\n".join(lines) + "\n", "utf-8")
        print(f"wrote {args.csv}", file=sys.stderr)

    print("\nNext: copy it into your config folder as autodonut_market.json, or in-game run",
          file=sys.stderr)
    print(f"  /autodonuteconomy import {args.out.name}", file=sys.stderr)
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
