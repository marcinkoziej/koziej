(ns cloto.server
  (:require [noir.server :as server]))

(server/load-views "src/cloto/views/")

(def ^{:dynamic :private} *srv* nil)

(defn -main [& m]
  (let [mode (keyword (or (first m) :dev))
        port (Integer. (get (System/getenv) "PORT" "8080"))]
    (let [server (server/start port {:mode mode
                                     :ns 'cloto})]
      (alter-var-root #'*srv* (constantly server))
      server
      )))

