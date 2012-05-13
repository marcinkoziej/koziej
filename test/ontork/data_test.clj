(ns ontork.data-test
  (:use ontork.data)
  (:use borneo.core)
  (:use clojure.test))


;; test if the index is changed according to field change.
(declare-pointer :names :node :name)

(deftest test-pointers
  (with-db
    (try
      (purge!)
      (with-tx (-> (root)
                   (create-child! :knows {:name "Asia"})
                   (update-pointer :names)
                   (set-prop! :name "Basia")
                   (update-pointer :names)))

      (is (= #{} (find-pointed :names :name "Asia")))
      (let [r (find-pointed :names :name "Basia")]
        (is (= 1 (count r)))
        (is (= (prop (first r) :name) "Basia")))

      (finally
       (shutdown)))
    ))