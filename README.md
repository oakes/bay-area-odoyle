These are my slides for a talk I gave to the bay area clojure meetup.

To build this project, you'll need the Clojure CLI tool:

https://clojure.org/guides/deps_and_cli


To develop the native version:

```
clj -M:dev native

# NOTE: On Mac OS, you need to add the macos alias:

clj -M:dev:macos native
```


To build the native version as a jar file:

```
clj -M:prod uberjar
```


To develop in a browser with live code reloading:

```
clj -M:dev
```


To build a release version for the web:

```
clj -M:prod:play-cljc
```
