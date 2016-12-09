(ns caves.entities.core)

(def ids (atom 0))

(defprotocol Entity
  (tick [this world]
        "Update the world to handle the passing of a tick for this entity."))

(defn get-id []
  (swap! ids inc))