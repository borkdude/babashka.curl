(ns babashka.curl-test
  (:require [babashka.curl :as curl]
            [clojure.test :refer [deftest is testing]]
            [cheshire.core :as json]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(deftest get-test
  (is (str/includes? (curl/get "https://httpstat.us/200")
                     "200"))
  (is (= 200
         (-> (curl/get "https://httpstat.us/200"
                       {:headers {"Accept" "application/json"}})
             (json/parse-string true)
             :code))))

(deftest post-test
  (is (subs (curl/post "https://postman-echo.com/post")
            0 10))
  (is (str/includes?
       (curl/post "https://postman-echo.com/post"
                  {:body "From Clojure"})
       "From Clojure"))
  (testing "file-body"
    (is (str/includes?
         (curl/post "https://postman-echo.com/post"
                    {:body (io/file "README.md")})
         "babashka.curl")))
  (testing "form-params"
    (is (str/includes?
         (curl/post "https://postman-echo.com/post"
                    {:form-params {"name" "michiel"}})
         "michiel"))))

(deftest basic-auth-test
  (is (re-find #"authenticated.*true"
       (curl/get "https://postman-echo.com/basic-auth" {:basic-auth ["postman" "password"]}))))

(deftest raw-args-test
  (is (str/includes?
       (curl/post "https://postman-echo.com/post"
                  {:body "From Clojure"
                   :raw-args ["-D" "-"]})
       "200 OK")))


;; untested, but works:
;; $ export BABASHKA_CLASSPATH=src
;; $ cat README.md | bb "(require '[babashka.curl :as curl]) (curl/post \"https://postman-echo.com/post\" {:raw-args [\"-d\" \"@-\"]})"
;; "{\"args\":{},\"data\":\"\",\"files\":{},\"form\":{\"# babashka.curlA tiny [curl](https://curl.haxx.se/) wrapper via idiomatic Clojure,
