(ns ontork.freebase
  (:use [clojure.java.io :only [resource reader]])
  (:import java.util.Properties)
  (:import [com.google.api.client.http.javanet NetHttpTransport])
  (:import [com.google.api.client.json.jackson JacksonFactory])
  (:import [com.google.api.client.http.json JsonHttpRequestInitializer])
  (:import [com.google.api.services.freebase Freebase Freebase$Builder
            FreebaseRequest])
  (:use [clojure.data.json :as json])
  (:use [ontork.util :only [conf]])
  (:require clojure.string)
  )


(def fbtypes (make-hierarchy))

(derive fbtypes :type-text :basic)
(derive fbtypes :type-uri :type-text)
(derive fbtypes :common-webpape :type-text)
(derive fbtypes :common-image :type-text)


(defn get-api-key []
  (conf "google.apikey"))


(defn make-builder
  "Creates and returns a request builder needed for api calls.
Use (.build builder) to get a request."
  []
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


(defn name->kw
  "Convert a FB name like /common/topic to :common-topic"
  [fb-name]
  (assert (= (first fb-name) \/))
  (keyword (clojure.string/join "-" (rest (clojure.string/split fb-name #"[/]")))))

(defn kw->name
  "Convert a keyword like :common-topic back to FB's /common/topic"
  [kw]
  (str "/" (clojure.string/join "/"
            (clojure.string/split (name kw) #"-")))
  )

(defn fb-name
  "get an FB name"
  [n]
  (if (keyword? n)
    (kw->name n)
    n))


(defn list-properties
  "Lists properties for a type"
  [fb for-type]
  (get-in 
   (search (.build fb) {
               :id (fb-name for-type)
               "/type/type/properties"
               [{ :id nil :type [] :expected_type nil :name nil}]})
   [:result (keyword "/type/type/properties")])
  )


