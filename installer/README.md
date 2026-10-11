# AutoDonut Installer

A lightweight, zero-dependency Java Swing GUI installer for **AutoDonut**.

## Features

- Automatic detection of standard `.minecraft` directory on Windows, macOS, and Linux
- Single-click mod + Fabric API installation into your client `mods/` folder
- **Client only.** There is deliberately no server option: AutoDonut never needs to be installed on a server
- API key configuration UI for the AI advisor feature
- One-click button to download/open the official Fabric Loader installer

## Building the Installer Jar

The installer embeds `autodonut-1.0.0.jar` and `fabric-api-0.161.0+26.2.jar` as internal resources.

```bash
# 1. Build the mod
./gradlew build

# 2. Stage bundled jars
mkdir -p installer/bundled installer/classes
cp build/libs/autodonut-1.0.0.jar installer/bundled/
# (ensure fabric-api jar is in installer/bundled/ as well)

# 3. Compile the installer class
javac -d installer/classes installer/AutoDonutInstaller.java

# 4. Package executable jar
jar cfe AutoDonut-Installer.jar AutoDonutInstaller -C installer/classes . -C installer bundled
```
