(ns cloto.util
  (:use [clojure.java.io :only [resource]]))

(defn conf [key]
  (with-open [props-file (.openStream (resource "cloto.properties"))]
    (let [props (java.util.Properties.)]
      (.load props props-file)
      (.getProperty props key))))