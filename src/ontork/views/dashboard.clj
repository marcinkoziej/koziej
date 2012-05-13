(ns ontork.views.dashboard
  (:use [noir.core :only [defpartial defpage]]
        [hiccup.page-helpers])
  (:require [borneo.core :as neo])
  (:require ontork.data))


(defpartial layout [work-list content]
  (html5
   [:head
    [:title "Ontork :-)"]
    (include-css "/css/bootstrap.css")
    (include-js "/js/bootstrap.js")]
   [:body
    [:div {:class "span4 work-list-wrap"}
     [:ul {:class "work-list"}
      (map (fn [i] [:li i]) work-list)]]
    [:div {:class "span8 content-wrap"}
     content
     ]

    ]))



(defpage "/pages/:slug" [slug]
  (layout
   (map (fn [x] (let [s (:slug (neo/props x))]
                  [:a {:href (str "/pages/" s)} s]))

        (ontork.data/all-pages))
   (if slug
     [:p "No page selected"])
   ))
