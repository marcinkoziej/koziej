;; should be renamed to ontork.pages
(ns ontork.data
  (:require [borneo.core :as neo])
  (:use [ontork.util :only [conf]])
  (:require [clojure.repl :as repl])
  (:use [clojure.set :only [intersection]]))


;; DB connection

(def ^:dynamic neo-db (atom nil))


(defmacro with-db [& code]
  `(do
    (when-not neo/*neo-db*
      (neo/start! (conf "db.path")))
    ~@code))


(defn shutdown
  ([] (shutdown 0))
  ([sig]
      (print "disconnecting from Neo4j")
      (when neo/*neo-db*
        (neo/stop!)
        (alter-var-root #'neo/*neo-db* (constantly nil)))))


(repl/set-break-handler! shutdown)


;; Indexing

;; Pointers

(def ^:dynamic pointers {})

(defn declare-pointer
  "Declares a pointer index"
  [named node-or-rel & fields]
  (alter-var-root #'pointers
                  (fn [ptrs]
                    (assoc ptrs named {
                                       :type node-or-rel
                                       :fields (set fields)
                                       }))))

(defn pointer-index [named]
  (let [idesc (get pointers named)]
    (if (= (:type idesc) :node)
      (.forNodes (neo/index) (name named))
      (.forRelationships (neo/index) (name named)))))

(defn find-pointed
  "Searches a named pointer index for provided fields
fields are a sequence k1 v1 k2 v2
returns a set of nodes/relationships" 
  [named & fields]
  (let [idx (pointer-index named)
        fields (if (and (= 1  (count fields))
                        (map? (first fields)))
                 (first fields)
                 (apply hash-map fields))
        ]
    (apply intersection
           (map
            (fn [[f v]]
              (set (iterator-seq (.get idx (name f) v))))
            fields
            ))))

(defn update-pointer
  ([obj named]
     (update-pointer obj named (get-in pointers [named :fields])))
  ([obj named fields]
     (let [idx (pointer-index named)]
       ;remove old
       (doseq  [f fields]
         (.remove idx obj (name f)))
       (doseq [f fields]
         (.add idx obj (name f) (neo/prop obj f)))
       obj)))


(defn slug-index [] (with-db (.  (neo/index) forNodes "pages-slug")))


(defn pages-node []
  "Starting node for pages"
  (with-db
    (when-let [r (first (neo/rels (neo/root) :contains-pages))]
      (neo/end-node r))))


(defn init []
  (with-db
    (when-not (pages-node)
      (neo/create-child! (neo/root) :contains-pages {}))))


(defn put-page
  ([old-slug slug title text]
     (with-db
       (neo/with-tx
         (let [old (.getSingle  (.get (slug-index) "slug" old-slug))
               pages (pages-node)
               ]

           (let [page-props {:slug slug :text text :title title}
                 page
                 (if old
                   (do (neo/set-props! old page-props) old)
                   (neo/create-child! pages :is-page page-props)
                   )]

             (when (and old (not (= old-slug slug)))
               (.remove (slug-index) page "slug" old-slug)
               (.add (slug-index) page "slug" slug))

             (when (nil? old)
               (.add (slug-index) page "slug" slug))
             page
             )))))
  ([slug title text]
     (put-page slug slug title text)))


(defn delete-page [slug]
  (with-db
    (let [n (.get (slug-index) "slug" slug)]
      (neo/delete-node! n))))

(defn all-pages []
  (-> (neo/root)
      (neo/walk  :contains-pages)
      (neo/traverse :all-but-start :is-page))
  )

