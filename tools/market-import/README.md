# Market price importer

Seeds AutoDonut's local price database with real numbers from
[DonutSMP Terminal](https://www.donutsmpterminal.com), so the deal finder and
Chest Sell tiers have something sensible to compare against on day one instead
of waiting for you to walk past a few hundred `/ah` listings.

```bash
# look first, write nothing
python3 tools/market-import/import_prices.py --dry-run

# write a database you can import in-game
python3 tools/market-import/import_prices.py --out autodonut_market.json

# only the stuff actually worth trading
python3 tools/market-import/import_prices.py --min-grade B --min-price 1000 \
    --out autodonut_market.json --csv prices.csv

# top up a database you already have instead of replacing it
python3 tools/market-import/import_prices.py --merge ~/.minecraft/config/autodonut_market.json \
    --out ~/.minecraft/config/autodonut_market.json
```

Then either drop the file into `.minecraft/config/autodonut_market.json`, or
load it in-game:

```
/autodonuteconomy import autodonut_market.json
```

Python 3.9+. No dependencies.

## Why it isn't built into the mod

The mod makes no network requests at all except the AI advisor, and only once
you've set a key. A scraper inside the mod would mean every copy of AutoDonut
polling someone else's website forever. This way the fetch happens once, on
your machine, because you asked for it.

It also keeps their data out of the jar and out of this git repository.

## How it behaves

- **Obeys `robots.txt`.** It reads and parses the real file before fetching and
  bails out if the page is disallowed. Right now that file allows `/all_items`
  and explicitly disallows `/api/` — so this reads the public page and never
  touches the API.
- **Identifies itself** with a descriptive User-Agent.
- **Fetches once and caches** (`.cache/all_items.html`, 12 h by default). Re-runs
  are free. `--refresh` forces a new fetch.
- **Fails loudly** if it parses zero rows, rather than quietly writing an empty
  database — that's the signal the page layout changed.

## Output format

Exactly what `MarketDatabase` reads — item id to a list of price samples:

```json
{
  "minecraft:ancient_debris": [2700000.0],
  "minecraft:diamond": [48000.0]
}
```

`--merge` appends to the existing samples rather than overwriting, respecting
the 200-sample-per-item cap the mod uses.

## Caveats worth knowing

- **Prices on the page are abbreviated**, so `48K` imports as exactly `48000`.
  Precision below the displayed digits is gone. Fine for seeding a baseline;
  don't treat it as exact.
- **These are DonutSMP's prices, on DonutSMP's economy.** If you're running
  your own server — which is the whole point of AutoDonut — treat them as a
  starting sanity-check, not truth. Your own `/ah` scans will outvote them as
  real samples accumulate.
- **Item ids come from the page's icon filenames.** If an id doesn't exist in
  your version of the game the mod simply never matches it, which is harmless.

## If it stops working

The page layout changing is the expected failure, and the script will tell you
by reporting `parsed 0 items`. The fix is `ROW_RE` near the top of
`import_prices.py`; the comment above it shows the shape it expects. Run the
tests after editing:

```bash
python3 tools/market-import/test_import.py
```

## Attribution

Price data belongs to DonutSMP Terminal. Don't redistribute the output, and if
you build anything public on top of it, ask them first. See
[`../../CREDITS.md`](../../CREDITS.md).
