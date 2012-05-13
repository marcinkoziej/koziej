(ns ontork.util
  (:use [clojure.java.io :only [resource]]))

(defn conf [key]
  (with-open [props-file (.openStream (resource "ontork.properties"))]
    (let [props (java.util.Properties.)]
      (.load props props-file)
      (.getProperty props key))))