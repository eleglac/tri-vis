(ns image-site.edge-detect
  
  (:require [quil.core :as q])
  )

(def kernels {:edge [[-1 -1 -1] [-1 9 -1] [-1 -1 -1]]})

(defn canny-edge-detect [image]
  (->> (grayscale)       ;; flatten rgb to one channel, but perhaps better to do separate edge-detect and average that?  would it make a difference?
       (gaussian-blur 1) ;; noise reduction, radius 1
       (convolution (:edge kernels))
       (one-px-filter)))

(defn grayscale 
  "Take RGB image and return grayscale.  Possibly in quil already?  Or image filtering library?"
  
  [image]
  
  image)

(defn gaussian-blur 
  "Gaussian blur of image.  Definitely in Quil."
  
  [radius image]
  
  image)

(defn convolution [kernel image]
  
  )
