(ns cloto.workspace
  (:require [borneo.core :as neo])
  (:require [cloto.freebase :as freebase])
  (:require [clojure.string :as str])
  (:use [cloto.data :only [with-db]])
  (:use [clojure.pprint :only [pprint]])
  )



(defn basename [fbn]
  (last (str/split fbn #"[/]"))
  )

(defn type-index []
  (with-db (.forNodes (neo/index) "type-type")))


(defn find-type-node [fbn]
  (.getSingle (.get (type-index) "type" (freebase/fb-name fbn)))

  )

(declare get-or-create)

(defn fill-props!
  "Gets props from FB and fills them in the node.
type-node - node
tp - type name/kw"
  [type-node tp]
  (let [bd (freebase/make-builder)
        prop-list (freebase/list-properties bd tp)
        extension-property? (fn [p] (some #(= % "/type/extension") (:type p)))]
    (neo/with-tx
      (doseq [prop (filter (comp not extension-property?) prop-list)]
        (pprint prop)
        (let [prop-node (get-or-create (:expected_type prop) true)]
          (->
           (neo/create-rel! type-node :property prop-node)
           (neo/set-props!
            (select-keys prop [:id :name :expected_type])
            ))))
      (when (neo/prop? type-node :stub)
        (neo/set-prop! type-node :stub nil)))
    type-node)
  )

(defn get-or-create
  "Create a type node representing a FB type
it will have :property relations to nodes representing properties info.
"
  ([tp] (get-or-create tp false))
  ([tp create-stub?]
     (let [fbn (freebase/fb-name tp)]
       (neo/with-tx
         (if-let [type-node (find-type-node tp)]
           (if (and (not create-stub?) (neo/prop? type-node :stub))
             (fill-props! type-node tp)
             type-node)
           
           ;; else, let's create it
           (let [type-node (neo/create-child! (neo/root) :type {:type fbn})]
             (.add (type-index) type-node "type" fbn)
             (if create-stub?
               ;; just create a stub node to be updated later
               (neo/set-prop! type-node :stub true)
               ;; create stub type nodes for each property, add necessary
               ;; property descriptors from FB
               (fill-props! type-node tp))))))))



(defn type-node [tp]
  (with-db
    (neo/root)

    )
  )


(defn add-type [tp]
  

  )
