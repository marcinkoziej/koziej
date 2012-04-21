(ns cloto.freebase
  (:use [clojure.java.io :only [resource reader]])
  (:import java.util.Properties)
  (:import [com.google.api.client.http.javanet NetHttpTransport])
  (:import [com.google.api.client.json.jackson JacksonFactory])
  (:import [com.google.api.client.http.json JsonHttpRequestInitializer])
  (:import [com.google.api.services.freebase Freebase Freebase$Builder
            FreebaseRequest])
  (:use [clojure.data.json :as json])
  (:use [cloto.util :only [conf]])
  )


(defn get-api-key []
  (conf "google.apikey"))


(defn make-builder []
  (let [httpTransport (new NetHttpTransport)
        jsonFactory (new JacksonFactory)
        ]
    (doto (Freebase/builder httpTransport jsonFactory)
      (.setJsonHttpRequestInitializer 
       (proxy [JsonHttpRequestInitializer] []
         (initialize [req]
           (doto req
             (.setPrettyPrint true)
             (.setKey (get-api-key)))))))))


(defn search [fb query]
  (json/read-json
   (reader (.executeAsInputStream
            (.mqlread fb (json/json-str query))))))
