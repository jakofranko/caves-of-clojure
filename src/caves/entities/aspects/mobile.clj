(ns caves.entities.aspects.mobile
  (:use [caves.world :only [is-empty?]]
        [caves.entities.core :only [defaspect]]))

(defaspect Mobile
  (move [this world dest]
    	{:pre [(can-move? this world dest)]}
    	(assoc-in world [:entities (:id this) :location] dest))
  (can-move? [this world dest] 
             (is-empty? world dest)))