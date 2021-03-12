#!/usr/bin/env bb

(ns chu.letter
  ;; Purpose of the script is to simply compile pdf letters from an edn file
  ;; which looks like:
  ;; { :from {:name "Lord Dummy"
  ;;          :adress "1 here and nowhere else"}
  ;;   :to {:name "Lady Dumsie"
  ;;        :adress "who knows where"}
  ;;   :date { Monday the first April 1900}}
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.pprint :refer [pprint]]
            [clojure.java.io :as io]
            [clojure.java.shell :refer [sh]]
            [babashka.deps :as deps]))

;;;;;;;;;;;;;;;;;;;; DEPS ;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; adds dependency with deps
;; comb is a templating library
(deps/add-deps '{:deps {comb/comb {:mvn/version "0.1.1"}}})
(require '[comb.template :as template])
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;


;; Let's have some commandline parse facility

(def cli-options
  ;; An option with a required argument
  [["-t" "--template TEMPL" "LaTeX template"
    :default "./letter-template.tex"]
   ["-i" "--input INPUT" "edn letter"
    :default-fn (constantly *in*)]
   ["-m" "--message MESSAGE" "edn letter message"]
   ["-f" "--from EDN" "expeditor edn file"]
   ["-2" "--to EDN" "receiver edn file"]
   ["-o" "--output OUT" "output name, will generate out.edn and out.pdf"
    :default "./letter"]
   ["-h" "--help"]])

(defn prefix-key
  "prefix a key with a list of prefixes keys :
  (prefix-key [:a :b] :c) => :a-b-c"
  [prefixes key]
  (keyword (clojure.string/join "-" (map name (conj prefixes key)))))

(defn flatten-keys
  "Transform a nested map into a flatten one with prefixed keys :
  (flataten-keys {:a {:b {:c 0}}}) => {:a-b-c 0}"
  ([m] (flatten-keys [] m))
  ([prefix m]
  (reduce-kv
   (fn [m' k v]
     (if (map? v)
       (merge m' (flatten-keys (conj prefix k) v))
       (assoc m' (prefix-key prefix k) v)))
   {} m)))

(defn mk-pdf
  [template input from to message output]
  (letfn [(rd [i] (or (when i (read-string (slurp i))) {}))]
    (let [full-edn (merge-with merge
                             {:from (rd from)
                              :to (rd to)
                              :message (rd message)}
                             (rd input))
          in-edn (flatten-keys full-edn)]
      (spit "./out.tex" (template/eval (slurp template) in-edn))
      (pprint full-edn (io/writer (str output ".edn")) )
      (println (sh "pdflatex" "./out.tex"))
      (println (sh "mv" "./out.pdf" "./other.pdf"))
      ;; (println (sh "bash" "-c" "rm out.*"))
      (println (sh "mv" "./other.pdf" (str output ".pdf"))))))

(defn main
  []
  (let [opts (parse-opts *command-line-args* cli-options)]
    (if (:help (:options opts))
      (print (:summary opts))
      (let [{:keys [input from to message output template]} (:options opts)]
        (mk-pdf template input from to message output)))))
(main)
