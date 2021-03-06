(ns caves.entities.player
  (:use [caves.entities.core :only [Entity add-aspect]]
        [caves.entities.aspects.receiver :only [Receiver]]
        [caves.entities.aspects.mobile :only [Mobile move can-move?]]
        [caves.entities.aspects.digger :only [Digger dig can-dig?]]
        [caves.entities.aspects.attacker :only [Attacker attack]]
        [caves.entities.aspects.destructible :only [Destructible]]
        [caves.coords :only [destination-coords]]
        [caves.world.core :only [get-entity-at]]))

(defrecord Player [id glyph color location hp max-hp attack name])

(extend-type Player Entity
  (tick [this world]
    world))

(add-aspect Player Mobile)
(add-aspect Player Digger)
(add-aspect Player Attacker)
(add-aspect Player Destructible)
(add-aspect Player Receiver)

(defn make-player [location]
  (map->Player {:id :player
                :name "you"
                :glyph "@"
                :color :white
                :location location
                :hp 40
                :max-hp 40
                :attack 10}))

(defn move-player [world dir]
  (let [player (get-in world [:entities :player])
        target (destination-coords (:location player) dir)
        entity-at-target (get-entity-at world target)]
    (cond
      entity-at-target (attack player entity-at-target world)
      (can-move? player target world) (move player target world)
      (can-dig? player target world) (dig player target world)
      :else world)))