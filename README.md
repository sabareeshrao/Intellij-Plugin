# AeroTopo Lesson Runner — IntelliJ Plugin

Native IntelliJ IDEA execution engine for the AeroTopo interview-learning curriculum.

The architecture deliberately keeps responsibilities separate:

- **Intellij-Plugin** — real IntelliJ lesson execution.
- **Java-Practice-Project** — AeroTopo source and Books → Chapters → Lessons → Steps.
- **Experiment-VS-Code** — browser simulator.

## Current capability

Milestone 1 loads the existing lesson JSON directly from an opened `Java-Practice-Project`, displays it in a native **AeroTopo Lessons** Tool Window, persists learner position, and supports **Previous / Replay / Next**.

The first implemented real IDE action is `openFile`.

This means the Lesson 1 proof is already meaningful:

```text
Lesson 1 / Step 1
        |
       Next
        |
        v
Lesson 1 / Step 2: openFile("pom.xml")
        |
        v
real pom.xml opens in real IntelliJ
        |
    Previous
        |
        v
previous lesson position + prior editor/caret restored
```

Unsupported lesson actions are shown explicitly and do not advance the lesson.

## Build

Requirements are handled by the checked-in Gradle wrapper:

```bash
./gradlew build
./gradlew buildPlugin
```

The installable ZIP is produced under:

```text
build/distributions/
```

GitHub Actions also uploads it as the **AeroTopo-Lesson-Runner** artifact.

## Run a development IDE

```bash
./gradlew runIde
```

Then open a local checkout of `Java-Practice-Project` in the development IntelliJ instance and open:

**View → Tool Windows → AeroTopo Lessons**

## Source compatibility

The exact curriculum commit used by the plugin is recorded in `LESSON_ENGINE_REF`.

See `PROJECT_STATE.md` for the handover, implemented capability list, first manual proof, and next milestone.


## Automatic development updates

After configuring this custom repository once, IntelliJ can discover later development builds as plugin updates:

`https://raw.githubusercontent.com/sabareeshrao/Intellij-Plugin/plugin-repository/updatePlugins.xml`

See `docs/AUTOMATIC_UPDATES.md` for setup details.
