(ns tri-vis.core
  (:require [delaunay-triangulation.core :refer [triangulate]] 
            [quil.core :as q]
            [quil.middleware :as m]))

(def hsb-max 100)
(def tri-size 64)

(defn intensity 
  "Given an input time in milliseconds, generate a float representing a
  percentage of some maximal value.  Currently the float is generated using
  a sin(x)^2 function but any continuous periodic function should work."
  [t]

  (let [duration 8000
        scale    (/ duration q/PI)]
    (Math/pow (Math/sin (/ t scale)) 2)))

(defn define-color 
  "Given three points (which ostensibly represent the corners of a triangle),
  generate a color (hue, saturation, value, alpha). Set the fill to that color.
  This can be done in many ways."
  [[x1 y1] [x2 y2] [x3 y3]]
  
  (let [skew (mod (q/frame-count) hsb-max)
        hue  (mod (+ skew x1 y1 y2) hsb-max)
        sat  (* hsb-max (intensity (q/millis)))
        vlu  hsb-max
        alph hsb-max]
  (q/color hue sat vlu alph)))

(defn get-corners
  "Create a list of [x y] corresponding to the corners of equilateral 
  triangles which tile the visible window. n is one-half the base length of 
  a single triangle (in pixels), and in effect sets the scale of the tiling."
  [n]
  
  (let [offset-x (* 2 n) 
        offset-y (* n (Math/sqrt 3))

        cols (+ 3 (* 2 (/ (q/width) offset-x)))
        rows (Math/floor (/ (q/height) offset-y))]

    (for [i (range (inc cols)) 
          j (range (inc rows))]
      [(- (* i n) offset-x)                            
       (* (+ (* j 2) (mod i 2)) offset-y)])))

(defn draw-tri 
  "Sets fill color (using define-color to create a fill color based on
  the triangle points) and then draws a triangle."
  [[x1 y1] [x2 y2] [x3 y3]]

  (q/fill (define-color [x1 y1] [x2 y2] [x3 y3])) 
  (q/triangle x1 y1 x2 y2 x3 y3))

(defn draw-tris 
  "Essentially just a wrapper for repeated calls to draw-tri."
  [tris]

  (doseq [[p1 p2 p3] (map identity (:triangles tris))] 
    (draw-tri p1 p2 p3)))

(defn setup 
  "Besides establishing basic environmental parameters (color-mode, frame-rate,
  and background), a list of the screen-tiling triangles is generated"
  []

  (q/color-mode :hsb hsb-max)
  (q/frame-rate 30) 
  (q/background hsb-max)
  (q/no-stroke)
 
  {:tris (triangulate (get-corners tri-size))})

(defn update-state
  "Currently does nothing but pass through current state, which is the list of 
  triangles as created in (setup).  
  update-state could potentially change the position of the triangle
  corners to allow for more interesting visual effects."
  [state]

  state)

(defn draw-state
  "Actually render the triangles!"
  [state]

  (q/background 100)

  (draw-tris (:tris state)))

(q/defsketch image-site
  :title      "Triangles"
  :setup      setup
  :update     update-state
  :draw       draw-state
  :size       :fullscreen
  :middleware [m/fun-mode])

(defn -main [] nil)
