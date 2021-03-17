;; Copyright © 2021 Atomist, Inc.
;;
;; Licensed under the Apache License, Version 2.0 (the "License");
;; you may not use this file except in compliance with the License.
;; You may obtain a copy of the License at
;;
;;     http://www.apache.org/licenses/LICENSE-2.0
;;
;; Unless required by applicable law or agreed to in writing, software
;; distributed under the License is distributed on an "AS IS" BASIS,
;; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
;; See the License for the specific language governing permissions and
;; limitations under the License.

(ns atomist.main
  (:require [atomist.api :as api]
            [clojure.data]
            [atomist.cljs-log :as log]
            [cljs.core.async :as async :refer [<!] :refer-macros [go]]
            [goog.string :as gstring]
            [goog.string.format]
            [atomist.async :refer-macros [go-safe <?]]
            [atomist.local-runner :as l]
            [cljs-node-io.core :as io]
            ["fast-glob" :as fast-glob]
            ["js-yaml" :as yaml]))

(defn yamls [cwd globs]
  (->> globs
       (mapcat #(seq (.sync fast-glob % #js {:cwd cwd})))
       (map #(io/file cwd %))
       (map #(.getPath %))
       (map #(js->clj (.safeLoad yaml (io/slurp %) 'utf8')))
       (mapcat keys)))

(defn create-ref-from-event
  [handler]
  (fn [request]
    (let [[commit] (-> request :subscription :result first)
          repo (:git.commit/repo commit)
          org (:git.repo/org repo)]
      (handler (assoc request :ref {:repo (:git.repo/name repo)
                                    :owner (:git.org/name org)
                                    :sha (:git.commit/sha commit)}
                      :token (:github.org/installation-token org))))))

(defn scan [basedir globs]
  (go-safe (->> (yamls basedir globs)
                (map #(#{"snakeCase"} %))
                (into []))))

(defn perform-check [handler]
  (fn [request]
    (go-safe
     (try
       (let [warnings (<? (scan
                           (-> request :project :path)
                           (:glob-patterns request)))
             summary (gstring/format "warnings:  %d" (count warnings))
             text (str warnings)]
         (<? (handler (assoc request
                             :atomist/status {:code 0 :reason "custom-check-complete"}
                             :checkrun/output {:title (:checkrun request)
                                               :summary summary
                                               :text text}
                             :checkrun/conclusion "neutral"))))
       (catch :default ex
         (assoc request
                :atomist/status {:code 0 :reason "failed to run check"}
                :checkrun/output {:title (:checkrun request)
                                  :summary "Test Failed"
                                  :text (str ex)}
                :checkrun/conclusion "neutral"))))))

(defn ^:export handler
  [data sendreponse]
  (api/make-request
   data
   sendreponse
   (-> (api/finished)
       (api/mw-dispatch
        {:on-push.edn (-> (api/finished)
                          (perform-check)
                          (api/with-github-check-run :name :checkrun)
                          (api/clone-ref)
                          (create-ref-from-event))})
       (api/add-skill-config)
       (api/log-event)
       (api/status))))


