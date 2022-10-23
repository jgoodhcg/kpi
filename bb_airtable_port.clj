(ns bb-airtable-port
  (:require [babashka.curl :as curl]
            [cheshire.core :as json]))

(def api-key (first *command-line-args*))

(defn get-all-records [db-id table-id api-key]
  (let [url      (str "https://api.airtable.com/v0/" db-id "/" table-id)]
    (loop [response (-> (curl/get
                         url
                         {:headers {"Authorization" (str "Bearer " api-key)}})
                        (:body)
                        (json/parse-string true))
           records []]
      (let [offset (->> response :offset)]
        (if (nil? offset)
          (concat records (->> response :records))
          (recur
           (-> (curl/get
                (str url "?offset=" offset)
                {:headers {"Authorization" (str "Bearer " api-key)}})
               (:body)
               (json/parse-string true))
           (concat records (->> response :records))))))))

(-> (get-all-records "appcu3LYk0kLQ2f6A" "tblXXCTTcI1jJjdIO" api-key)
    first
    println)
