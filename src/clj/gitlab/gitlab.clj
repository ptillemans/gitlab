(ns gitlab.gitlab
  (:require [clj-http.client :as client]
            [clojure.spec.alpha :as s]
            [cheshire.core :as json]))


(s/def ::api-token string?)
(s/def ::base-url string?)
(s/def ::config (s/keys :req [::api-token ::base-url]))

(s/def ::options map?)


(defn- headers
  [config]
  (let [token (::api-token config)]
    {"PRIVATE-TOKEN" token}))

(defn create-config
  [base-url token]
  {:pre [(s/valid? ::base-url base-url)
         (s/valid? ::api-token token)]}
  {::base-url base-url
   ::api-token token})

(defn get-projects
  "Get a list of projects, the last modified first."
  [config]
  {:pre [(s/valid? ::config config)]}
  (-> (str (::base-url config) "/projects?order_by=last_activity_at&per_page=10")
      (client/get {:accept :json
                   :headers (headers config)
                   })
      (:body)
      (json/parse-string)))
