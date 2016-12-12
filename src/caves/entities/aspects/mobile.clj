(ns caves.entities.aspects.mobile
  (:use [caves.world :only [is-empty?]]
        [caves.entities.core :only [defaspect]]))

(defaspect Mobile
  (move [this dest world]
    {:pre [(can-move? this dest world)]}
    (assoc-in world [:entities (:id this) :location] dest))
  (can-move? [this dest world]
    (is-empty? world dest)))