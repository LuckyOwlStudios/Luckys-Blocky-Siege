# Lucky's Blocky Siege

A Minecraft 1.21.1 mod that adds siege weapons to Minecraft, for **NeoForge** and **Fabric**.

## Project layout

This is a multiloader project based on [MultiLoader-Template](https://github.com/jaredlll08/MultiLoader-Template):

| Folder | What lives there |
| --- | --- |
| `common/` | Almost all of the mod: blocks, entities, menus, screens, renderers, packets, assets and data. It compiles against plain vanilla Minecraft, so it cannot use any loader API. |
| `neoforge/` | The NeoForge entry point, event hooks, config spec, fluid type and the data generators. |
| `fabric/` | The Fabric entry points, Fabric API hooks, a small properties config and client mixins. |

When common code needs something loader-specific (registering objects, opening menus, sending packets, creative tab builders), it goes through `net.luckystudios.platform.Services`, which loads `IPlatformHelper` and `IRegistryHelper` implementations from each loader's `META-INF/services`.

## Building

```
./gradlew build
```

The jars end up in `fabric/build/libs/` and `neoforge/build/libs/`.

## Running

- NeoForge: `./gradlew :neoforge:runClient` (or `runServer`)
- Fabric: `./gradlew :fabric:runClient` (or `runServer`)

## Data generation

Data generators run on NeoForge and write into `common/src/generated/resources`, so both loaders ship the output:

```
./gradlew :neoforge:runData
```

## Config

- NeoForge: `config/blockysiege-common.toml` and `config/blockysiege-client.toml`
- Fabric: `config/blockysiege-common.properties` and `config/blockysiege-client.properties`
