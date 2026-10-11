# Credits & third-party notices

AutoDonut is CC0-1.0 (public domain). That applies to **AutoDonut's own code
only**. This file records everything that inspired or feeds into the project
and the terms attached to each.

---

## BaseFinder / Block ESP — inspiration for the Block ESP feature

> **BaseFinder (Block ESP)** by **[ShadowsIsNothing](https://www.curseforge.com/members/shadowsisnothing)**
> <https://www.curseforge.com/minecraft/mc-mods/block-esp>
> CurseForge project `1662630` · latest file `basefinder-1.1.0+26.2.jar`
> **License: All Rights Reserved**

AutoDonut's Block ESP exists because of this mod. Full credit to
ShadowsIsNothing for the idea and for showing it could be done cleanly and
fully client-side. If you like the feature, **go give the original a download
and a follow** — it has a mini-map, entity detection and freecam that
AutoDonut does not.

### What AutoDonut does and does not take from it

BaseFinder is licensed **All Rights Reserved**, which on CurseForge is also
the default when no licence is chosen. That means nobody may copy its code,
decompile it, redistribute its jar, or ship a modified version — permission
would have to come from the author directly.

So AutoDonut does none of those things:

| | |
|---|---|
| Code copied from BaseFinder | **None.** `src/client/java/com/autodonut/client/esp/` was written from scratch against the public Minecraft and Fabric APIs. |
| BaseFinder jar bundled or redistributed | **No.** The installer bundles only AutoDonut and Fabric API. |
| BaseFinder decompiled or inspected | **No.** |
| Taken | The *idea* of a client-side block highlighter, and some sensible defaults. Ideas aren't copyrightable; code is. |

The two mods are independent and can be installed side by side. If you'd
rather use the original, turn AutoDonut's Block ESP off and leave it off —
nothing else in AutoDonut depends on it.

### A note on "I'm not distributing it, so it doesn't matter"

Worth being clear, because it's an easy and very common thing to get wrong:
**this repository is public, and a public repository is distribution.**
Copying All Rights Reserved code into it would be infringement even if you
personally never gave the jar to anyone, and it's the sort of thing that
attracts a DMCA takedown of the whole repo.

Writing our own implementation costs a little more effort and makes the
problem go away completely. Using the original mod yourself, privately, is of
course entirely your business.

---

## DonutSMP Terminal — optional market price data

> **DonutSMP Terminal** · <https://www.donutsmpterminal.com>

`tools/market-import/` can read the public
[all-items price page](https://www.donutsmpterminal.com/all_items) and convert
it into AutoDonut's local price-database format, to seed your market data with
realistic numbers.

How that tool behaves, and why:

- **It is not part of the mod.** It's a standalone script *you* run on your own
  machine, on purpose. AutoDonut itself makes no network requests except the AI
  advisor, and only when you've set a key.
- **It respects `robots.txt`.** That file allows `/all_items` and explicitly
  **disallows `/api/`**, so the tool reads the public page and never touches
  the API.
- **It identifies itself** with a descriptive User-Agent, requests the page
  **once** and caches the result.
- **No scraped data is committed to this repository.** The output lands in
  your own `config/` directory. The price data belongs to DonutSMP Terminal.

If you run a public service on top of their numbers, ask them first — and
consider supporting the site, since they're the ones doing the collection.

---

## Minecraft, Fabric and libraries

AutoDonut is an unofficial mod. Minecraft is a trademark of Mojang AB; this
project is not affiliated with or endorsed by Mojang, Microsoft, or DonutSMP.

| Project | Role | Licence |
|---|---|---|
| [Fabric Loader / Fabric API](https://fabricmc.net/) | Mod loader and APIs | Apache-2.0 |
| [Mod Menu](https://modrinth.com/mod/modmenu) | Optional settings integration (compile-only) | MIT |
| [Gson](https://github.com/google/gson) | Config and database serialisation (via Minecraft) | Apache-2.0 |
| [Eclipse JDT Core (ECJ)](https://www.eclipse.org/jdt/core/) | Offline type-check harness only; not shipped | EPL-2.0 |

Every external API AutoDonut calls is listed in
[`API-SURFACE.md`](API-SURFACE.md).
