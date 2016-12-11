(ns caves.entities.lichen
  (:use [caves.entities.core :only [Entity get-id add-aspect]]
        [caves.world :only [find-empty-neighbor]]
        [caves.entities.aspects.destructible :only [Destructible take-damage]]))

(defrecord Lichen [id glyph color location hp])

(defn should-grow []
  (< (rand) 0.01))

(defn make-lichen
  [location]
  (->Lichen (get-id) "F" :green location 1))

(defn grow [lichen world]
  (if-let [target (find-empty-neighbor world (:location lichen))]
    (let [new-lichen (make-lichen target)]
      (assoc-in world [:entities (:id new-lichen)] new-lichen))
    world))

(extend-type Lichen Entity
  (tick [this world]
        (if (should-grow)
          (grow this world)
          world)))
  
(add-aspect Lichen Destructible)