(defproject bay-area-odoyle "0.1.0-SNAPSHOT"
  :repositories [["clojars" {:url "https://clojars.org/repo"
                             :sign-releases false}]]
  :clean-targets ^{:protect false} ["target"]
  :main bay-area-odoyle.start
  :aot [bay-area-odoyle.start])
