(ns user
  (:use cloto.freebase)
  ( :use [clojure.pprint :only [pprint]])
  ( :use [clojure.java.io :only [writer]]))


(def b (make-builder))

(def fb (.build b))

(println (search  fb {
                      :type []
                      :id "/en/warsaw_frederic_chopin_airport"
                      "/aviation/airport/hub_for" [{:id nil}]
                    }))



(pprint (search  fb {
                      :id "/common/topic"
                     "/type/type/properties"
                     [{
                       :id nil :type [] :name nil :expected_type nil
                      
                       }]
                      }))


(println (search  fb {
                      :/type/type/properties []
                      :id "/type/type"
                      }))


(def r (search  fb [ {
                        :type "/type/link"
                        :source {
                                 :id "/aviation/airport"
                                 }
                      :target  { :type "/type/property"
                                :id nil }
                      :master_property nil
                      :target_value nil
                      }]))


(def r (search  fb {
                      :name [{}]
                      :id "/en/warsaw_frederic_chopin_airport"
                    }))


(pprint r (writer "/tmp/a"))