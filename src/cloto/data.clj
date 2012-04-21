(ns cloto.data
  (:require [borneo.core :as neo])
  (:use [cloto.util :only [conf]])
  (:require [clojure.repl :as repl]))

(def ^:dynamic neo-db (atom nil))


(defmacro with-db [& code]
  `(do
    (when-not neo/*neo-db*
      (neo/start! (conf "db.path")))
    ~@code))


(defn shutdown []
  (print "disconnecting from Neo4j")
  (when neo/*neo-db*
    (neo/stop!)))


(repl/set-break-handler! shutdown)


(defn slug-index [] (with-db (.  (neo/index) forNodes "pages-slug")))


(defn pages-node []
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