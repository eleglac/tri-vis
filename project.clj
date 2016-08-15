(defproject tri-vis "0.1-alpha"

  :description "Inspired by a Macbook cover."

  :url "http://pontingdynamics.org/tri-vis"

  :license {:name "Not A Clue License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [[org.clojure/clojure "1.8.0"]
                 [trystan/delaunay-triangulation "1.0.1"]
                 [quil "2.4.0"]]

  :main tri-vis.core )
