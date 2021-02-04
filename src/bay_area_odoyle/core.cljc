(ns bay-area-odoyle.core
  (:require [bay-area-odoyle.utils :as utils]
            [play-cljc.gl.core :as c]
            [play-cljc.gl.entities-2d :as e]
            [play-cljc.transforms :as t]
            [dungeon-crawler.core :as dungeon]
            #?(:clj  [play-cljc.macros-java :refer [gl math]]
               :cljs [play-cljc.macros-js :refer-macros [gl math]])))

(def slide-count 27)
(def slide-order (let [[before after] (split-at 18 (range slide-count))]
                   (vec (concat before [:dungeon] after))))

(defonce *state (atom {:slide-num 0
                       :slide-images {}}))

(defn on-key-press! [k]
  (case k
    :slide-left (swap! *state update :slide-num
                       (fn [slide-num]
                         (if (get slide-order (dec slide-num))
                           (dec slide-num)
                           (dec (count slide-order)))))
    :slide-right (swap! *state update :slide-num
                        (fn [slide-num]
                          (if (get slide-order (inc slide-num))
                            (inc slide-num)
                            0)))
    nil))

(defn init [game]
  ;; allow transparency in images
  (gl game enable (gl game BLEND))
  (gl game blendFunc (gl game SRC_ALPHA) (gl game ONE_MINUS_SRC_ALPHA))
  (dungeon/init game)
  ;; load images and put them in the state atom
  (dotimes [i slide-count]
    (utils/get-image (str "odoyle/Slide" (inc i) ".png")
      (fn [{:keys [data width height]}]
        (let [;; create an image entity (a map with info necessary to display it)
              entity (e/->image-entity game data width height)
              ;; compile the shaders so it is ready to render
              entity (c/compile game entity)
              ;; assoc the width and height to we can reference it later
              entity (assoc entity :width width :height height)]
          ;; add it to the state
          (swap! *state update :slide-images assoc i entity))))))

(def screen-entity
  {:viewport {:x 0 :y 0 :width 0 :height 0}
   :clear {:color [0 0 0 1] :depth 1}})

(defn tick [game]
  (let [{:keys [slide-num
                slide-images]
         :as state} @*state
        game-width (utils/get-width game)
        game-height (utils/get-height game)
        slide-index (get slide-order slide-num)]
    (when (and (pos? game-width) (pos? game-height))
      ;; render the background
      (c/render game (update screen-entity :viewport
                             assoc :width game-width :height game-height))
      ;; get the current player image to display
      (case slide-index
        :dungeon
        (dungeon/tick game)
        ;; else
        (when-let [image (get slide-images slide-index)]
          (let [image-ratio (/ (:width image) (:height image))
                image-width (* image-ratio game-height)
                image-height game-height
                x (if (> game-width image-width)
                    (-> (- game-width image-width)
                        (/ 2))
                    0)]
            ;; render the player
            (c/render game
              (-> image
                  (t/project game-width game-height)
                  (t/translate x 0)
                  (t/scale image-width image-height))))))))
  ;; return the game map
  game)

