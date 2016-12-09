(ns caves.ui.drawing
  (:require [lanterna.screen :as s]))

(def screen-size [80 24])

; TODO: don't assume width and heights
(defn clear-screen 
  [screen]
  (let [[cols rows] screen-size
        blank (apply str (repeat cols \space))]
    (doseq [row (range rows)]
      (s/put-string screen 0 row blank))))

(defn draw-world
  [screen vrows vcols start-x start-y end-x end-y tiles]
  ; Create a list of vectors [vrow-idx mrow-idx] such that
  ; vrow-idx (vieport row index) is the location of the viewport y
  ; and mrow-idx (map row index) is the corresponding y location on the map (world)
  (doseq [[vrow-idx mrow-idx] (map vector
                                   (range 0 vrows)        ; the height of the viewport
                                   (range start-y end-y)) ; the location of the viewport
          :let [row-tiles (subvec (tiles mrow-idx) start-x end-x)]]
    (doseq [vcol-idx (range vcols)
            :let [{:keys [glyph color]} (row-tiles vcol-idx)]]
      (s/put-string screen vcol-idx vrow-idx glyph {:fg color}))))

(defn draw-crossharis
  [screen vcols vrows]
  (let [crosshair-x (int (/ vcols 2))
        crosshair-y (int (/ vrows 2))]
    (s/put-string screen crosshair-x crosshair-y "X" {:fg :red})
    (s/move-cursor screen crosshair-x crosshair-y)))

(defn draw-entity
  [screen start-x start-y {:keys [location glyph color]}]
  (let [[entity-x entity-y] location
        x (- entity-x start-x)
        y (- entity-y start-y)]
    (s/put-string screen x y glyph {:fg color})))

(defn highligh-player
  [screen start-x start-y player]
  (let [[player-x player-y] (:location player)
        x (- player-x start-x)	; x and y position the player within the viewport
        y (- player-y start-y)]
    (s/move-cursor screen x y)))

(defn draw-hud
  [screen game start-x start-y]
  (let [hud-row (dec (second screen-size))
        [x y] (get-in game [:world :entities :player :location])
        info (str "Loc: [" x "-" y "]")
        info (str info " start: [" start-x "-" start-y "]")]
    (s/put-string screen 0 hud-row info)))

(defn get-viewport-coords
  [game player-location vcols vrows]
  (let [[center-x center-y] player-location
        
        tiles (:tiles (:world game))
        
        map-rows (count tiles)
        map-cols (count (first tiles))
        
        ; define the top-left and bottom-right corners of the viewport
        start-x (max 0 (- center-x (int (/ vcols 2))))
        start-y (max 0 (- center-y (int (/ vrows 2))))
        
        end-x (+ start-x vcols)
        end-x (min end-x map-cols)

        end-y (+ start-y vrows)
        end-y (min end-y map-rows)
        
        start-x (- end-x vcols)
        start-y (- end-y vrows)]
    [start-x start-y end-x end-y]))

(defmulti draw-ui
  (fn [ui game screen]
    (:kind ui)))

(defmethod draw-ui :start 
  [ui game screen]
  (s/put-string screen 0 0 "Welcome to the Caves of Clojure!")
  (s/put-string screen 0 1 "Press any key to continue")
  (s/put-string screen 0 4 "Once in the game, press enter to win, and backspace to lose"))

(defmethod draw-ui :play 
  [ui game screen]
  (let [world (:world game)
        {:keys [tiles entities]} world
        player (:player entities)
        [cols rows] screen-size
        vcols cols        ; vcols == "viewport columns"
        vrows (dec rows)  ; vrows == "viewport rows". Save a row on the bottom for UI info
        [start-x start-y end-x end-y] (get-viewport-coords game (:location player) vcols vrows)]
    (draw-world screen vrows vcols start-x start-y end-x end-y tiles)
    (doseq [entity (vals entities)]
      (draw-entity screen start-x start-y entity))
    (draw-hud screen game start-x start-y)
    (highligh-player screen start-x start-y player)))

(defmethod draw-ui :win 
  [ui game screen]
  (s/put-string screen 0 0 "Congratulations, you win!")
  (s/put-string screen 0 1 "Press escape to exit, anything else to restart."))

(defmethod draw-ui :lose 
  [ui game screen]
  (s/put-string screen 0 0 "Sorry, better luck next time.")
  (s/put-string screen 0 1 "Press escape to exit, anything else to go."))

(defn draw-game 
  [game screen]
  (clear-screen screen)
  (doseq [ui (:uis game)]
    (draw-ui ui game screen))
  (s/redraw screen))