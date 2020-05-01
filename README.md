# Sredded
[![Release Version](https://img.shields.io/nexus/r/https/oss.sonatype.org/com.github.mvv.sredded/sredded_2.13.svg)](https://oss.sonatype.org/content/repositories/releases/com/github/mvv/sredded)
[![Snapshot Version](https://img.shields.io/nexus/s/https/oss.sonatype.org/com.github.mvv.sredded/sredded_2.13.svg)](https://oss.sonatype.org/content/repositories/snapshots/com/github/mvv/sredded)
[![Build Status](https://travis-ci.com/mvv/sredded.svg?branch=master)](https://travis-ci.com/mvv/sredded)

In-memory structured data representation not tied to a particular
serialization format, built from mappings, sequences and scalars, which
include

* Null value
* Booleans
* Int{8,16,32,64}
* Big integers
* Float{32,64}
* Big decimals
* Timestamps (millisecond and nanosecond variants)
* Strings
* Binary blobs

## Using Sredded in your project

Add Sredded to your dependencies

```scala
libraryDependencies += "com.github.mvv.sredded" %% "sredded" % "0.1-M1"
```

## Submodules

* `sredded-generic` provides a macro for automatic derivation of `Structured`
  instances for case classes.
* `sredded-json` is a no-deps JSON printer for structured data
