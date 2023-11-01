# OPKJE

The Java Edition of the Rhytick OPK engine.

![.](https://github.com/skjsjhb/OPKJE/actions/workflows/gradle.yml/badge.svg)
![.](https://github.com/skjsjhb/OPKJE/actions/workflows/codeql.yml/badge.svg)

## WIP

This project is still being actively maintained. Submitted commits might not be accurate, and the feature is not
complete. The documents may also be outdated. Any major or minor changes may take place in any future, without further
notice.

## Brief

This project is part of the **OPFW Series**. OPFW Series is a set of native libraries and their wrappers to support the
run of Opticia, our music based game.

OPKJE, stands for 'J Edition of OPK', uses LWJGL (and the JSE platform) to implement OPFW APIs. It was originally
designed to cover the missing platform support for macOS, and has eventually became the cross platform solution for
Opticia on PC platforms.

## Build from Source

### TL; DR

- JDK 20

- `./gradlew build`

### Prerequisites

To build OPKJE, you'll need the following prerequisites:

- JDK 20 or later version.

    - **DO NOT try to use an earlier version!**

      GraalVM requires a JDK internal method which is not provided in earlier JDK releases. Also, some JDK 20 exclusive
      methods are used by our code (e.g. `threadId`).

      **We've tried using an earlier JDK and the build has failed.** If you still want to know more to be convinced.

    - The JDK used for official builds:

      ```
      openjdk version "20.0.2" 2023-07-18
      OpenJDK Runtime Environment Temurin-20.0.2+9 (build 20.0.2+9)
      OpenJDK 64-Bit Server VM Temurin-20.0.2+9 (build 20.0.2+9, mixed mode, sharing)
      ```

- Prebuilt Opticia scripts.

    - Though not needed immediately during the build process, you'll need them to test the build output.

### Build Process

OPKJE follows standard Gradle build process. The following shell script should just work:

```shell
git clone https://github.com/skjsjhb/OPKJE.git --depth 1
cd OPKJE
chmod +x ./gradlew
./gradlew build
```

## License

Copyright (C) 2023 Ted "skjsjhb" Gao. All rights reserved.

The code in this repository is currently proprietary. However, **we are't meant to make Opticia a proprietary game**.
Since the game is still in an early stage, we have no choice but to limit the usages of our source code to avoid
possible risks. We'll reconsider the license when the game releases.
