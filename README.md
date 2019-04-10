# Open Unofficial ZKM Retrace Tool

> ***NOTE: This tool is not directly related to Zelix Pty Ltd and is not a part of any Zelix KlassMaster™ packages. It is rather a third-party software, not affiliated with Zelix Pty Ltd.***

## Problem

Sometimes it may be needed by a person who does not own Zelix KlassMaster™ to "decrypt"/"deobfuscate" a stacktrace related to the software they own (which was obfuscated by another person or a member of their company which is currently out of their reach).


## Solution

Given the **remappings file** ZKM creates upon obfuscation and the obfuscated stacktrace, you can generate the "real" stacktrace using this tool.


## Keep in mind

This is not a complete substitution for Zelix KlassMaster™'s official retrace tool. It lacks a variety of validation mechanisms and may not always work as expected *(basically, it was solely written for myself by myself, but then I thought it may be useful to release it `\(._.)/`)*. **Feel free to contribute if you want to :)**


## Usage

Navigate to the [releases page](https://github.com/MeGysssTaa/zelix-retracer/releases) and download the latest release. Then run the JAR in CLI (bash/cmd), specifying one program argument — path to the ZKM remappings file you want to use for retracing:

```bash
java -jar zelix-retracer-1.0.0.jar ~/my-projects/proj2/obf/zkm-mappings.txt
```

Wait a little bit while the program is parsing the specified mappings file. This may take a while for really big projects.

Then you will be prompted to enter the stacktrace you want to retrace. Just copy-paste it. Then hit **ENTER** to move on to the next line and type `/retrace`. Then just allow zelix-retracer up to a few milliseconds (I hope that's not too much for you) to transform your stacktrace. As the result, you will be given the **original** (unobfuscated/unscrambled/remapped) stacktrace.

> **CAUTION:** zelix-retracer does **not** perform any sort of validation at any steps. Make sure you provide it the proper remappings file and the stacktrace you input is valid and supported.

**Please note:** zelix-retracer only retraces lines that start with `at` (with or without leading whitespace). Other lines are kept as is.


## Building (Gradle)

1. Download or clone this repository.
2. `cd` into it.
3. Run `./gradlew build`. **Profit!**


## License

[Apache License 2.0](https://github.com/MeGysssTaa/zelix-retracer/blob/master/LICENSE)
