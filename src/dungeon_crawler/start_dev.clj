(ns dungeon-crawler.start-dev
  (:require [dungeon-crawler.start :as start]
            [dungeon-crawler.core :as c]
            [clojure.spec.test.alpha :as st]
            [clojure.spec.alpha :as s]
            [play-cljc.gl.core :as pc]
            [paravim.start]
            [paravim.core])
  (:import [org.lwjgl.glfw GLFW]
           [dungeon_crawler.start Window]))

(defn start-paravim [game]
  (let [game (paravim.start/init game)
        *focus-on-game? (atom true)
        *last-tick-error (atom 0)]
    (extend-type Window
      start/Events
      (on-mouse-move [{:keys [handle]} xpos ypos]
        (if @*focus-on-game?
          (try
            (start/on-mouse-move! handle xpos ypos)
            (catch Exception e (.printStackTrace e)))
          (paravim.start/on-mouse-move! game handle xpos ypos)))
      (on-mouse-click [{:keys [handle]} button action mods]
        (if @*focus-on-game?
          (try
            (start/on-mouse-click! handle button action mods)
            (catch Exception e (.printStackTrace e)))
          (paravim.start/on-mouse-click! game handle button action mods)))
      (on-key [{:keys [handle]} keycode scancode action mods]
        (if (and (= action GLFW/GLFW_PRESS)
                 (= keycode GLFW/GLFW_KEY_ESCAPE)
                 (= (paravim.core/get-mode) 'NORMAL))
          (when-not (and (pos? @*last-tick-error) (not @*focus-on-game?)) ;; stay in paravim if game has error
            (swap! *focus-on-game? not))
          (if @*focus-on-game?
            (try
              (start/on-key! handle keycode scancode action mods)
              (catch Exception e (.printStackTrace e)))
            (paravim.start/on-key! game handle keycode scancode action mods))))
      (on-char [{:keys [handle]} codepoint]
        (if @*focus-on-game?
          (try
            (start/on-char! handle codepoint)
            (catch Exception e (.printStackTrace e)))
          (paravim.start/on-char! game handle codepoint)))
      (on-resize [{:keys [handle]} width height]
        (try
          (start/on-resize! handle width height)
          (catch Exception e (.printStackTrace e)))
        (paravim.start/on-resize! game handle width height))
      (on-scroll [{:keys [handle]} xoffset yoffset]
        (if @*focus-on-game?
          (try
            (start/on-scroll! handle xoffset yoffset)
            (catch Exception e (.printStackTrace e)))
          (paravim.start/on-scroll! game handle xoffset yoffset)))
      (on-tick [this game]
        (cond-> (try
                  (let [game (assoc (c/tick game) :paravim.core/clear? false)]
                    (when (pos? @*last-tick-error)
                      (reset! *last-tick-error 0))
                    game)
                  (catch Exception e
                    (let [current-ms (System/currentTimeMillis)]
                      (when (> (- current-ms @*last-tick-error) 1000)
                        (reset! *last-tick-error current-ms)
                        (.printStackTrace e)))
                    (assoc game :paravim.core/clear? true)))
                (not @*focus-on-game?)
                paravim.core/tick)))
    game))

(defn start []
  (st/instrument)
  (let [window (start/->window)
        game (pc/->game (:handle window))
        game (try
               (start-paravim game)
               (catch Throwable e
                 (.printStackTrace e)
                 game))]
    (start/start game window)))

(defn -main []
  (start))

