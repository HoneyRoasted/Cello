[Project Page](https://honeyroasted.github.io/cello/landing.html)

# Cello
Cello is a node-based JVM bytecode instrumentation library.

## Building
Fill may be built using jitpack:  
[![Release](https://jitpack.io/v/HoneyRoasted/Cello.svg)](https://jitpack.io/#HoneyRoasted/Cello)

```groovy
repositories {
    maven {url 'https://jitpack.io'}    
}

dependencies {
    implementation 'com.github.HoneyRoasted:Cello:Version'
}
```
Additionally, Cello is continuously built with [GitHub actions](https://github.com/HoneyRoasted/Cello/actions). You
may also download the repository and build from source using the `build.sh` script.