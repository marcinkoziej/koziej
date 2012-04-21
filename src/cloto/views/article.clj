(ns cloto.views.article
  (:use [noir.core :only [defpage]])
  (:use [clojure.java.io :only [resource file]])
  (:require [clojure.tools.logging :as log])
  (:use [ring.util.response :only [response]])
  (:require stencil.core)
  (:import [com.petebevin.markdown MarkdownProcessor]))



(defpage "/article/:slug" {slug :slug}
  (let [content-file (file  (resource "article") slug)
        get-proc (memoize (constantly (MarkdownProcessor.)))]
    (when (.exists content-file)
      (stencil.core/render-file "design.html"
                                {:content
                                 (.markdown (get-proc)
                                            (slurp content-file))}
      
                                ))))


(defpage "/about" []
  (stencil.core/render-file "design.html"
                            {:content
                             (slurp (resource "about.html"))}
                            )

  )