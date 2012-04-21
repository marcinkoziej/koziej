(ns cloto.views.test
  (:use [noir.core :only [defpage]])
  (:use [clojure.java.io :only [ resource file]])
  (:require [clojure.tools.logging :as log])
  (:use [ring.util.response :only [response]])
  )

(defpage "/test" []
  (slurp ( resource "design.html")))