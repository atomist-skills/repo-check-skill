(ns atomist.main
  (:require [atomist.api :as api]
            [clojure.data]
            [atomist.cljs-log :as log]
            [cljs.core.async :as async :refer [<!] :refer-macros [go]]
            [goog.string :as gstring]
            [goog.string.format]
            [atomist.local-runner :as l]
            ))

(defn ^:export handler
  [data sendreponse]
  (api/make-request
   data
   sendreponse
   (-> (api/dispatch
        {:OnComment (-> (api/finished)
                        (parse-comment))})
       (api/add-skill-config)
       (api/log-event)
       (api/status))))


