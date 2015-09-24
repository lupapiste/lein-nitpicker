(ns leiningen.nitpicker
  (:require [clojure.java.io :as io]
            [leiningen.core.main])
  (:import [java.io File]))

(defn contains-ch? [^String s ^Character c]
  (> (.indexOf s (int c)) -1))

(defn contains-string? [^String s ^String c]
  (.contains s c))

(defn contains-non-ascii? [^String s]
  (some (fn [c] (> (int c) 126)) s))

(defn ext [f]
  (if-let [e (re-find #"\.[a-z]+$" (.getName f))]
    (.substring e 1)))

(def EOF (int -1))
(def NL (int \newline))

(defn ^String read-ln [^java.io.InputStream i]
  (let [buffer (StringBuilder.)]
    (loop [c (.read i)]
      (cond
        (== c NL) (.toString buffer)
        (== c EOF) (when (not (zero? (.length buffer))) (.toString buffer))
        :else (do
                (.append buffer (char c))
                (recur (.read i)))))))

(defn lines [^java.io.InputStream i]
  (when-let [line (read-ln i)]
    (cons line (lazy-seq (lines i)))))

(defn get-line-errors [f [line-no line]]
  (concat
    (if (contains-ch? line \return)
      [[line-no "Contains return"]])
    (if (contains-ch? line \tab)
      [[line-no "Contains tab"]])
    (if (contains-non-ascii? line)
      [[line-no "Contains non-ascii characters"]])
    (if (and (not= (.getName f) "log.js") (contains-string? line "console.log"))
      [[line-no "Contains 'console.log'"]])))

(defn get-file-errors [f]
  (with-open [i (io/input-stream f)]
    [f (doall (mapcat (partial get-line-errors f) (map-indexed vector (lines i))))]))

(defn source-dirs [project]
  (concat (:source-paths project) (:resource-paths project)))

(defn find-files [dirs]
  (flatten (map (comp file-seq io/as-file) dirs)))

(defn get-rel-path [root f]
  (str \. (.substring (.getAbsolutePath f) (.length root))))

(defn nitpicker [project & args]
  (let [config (:nitpicker project)
        exts (set (or (:exts config) ["clj" "js" "css" "html"]))
        excludes (or (:excludes config) [#"\/jquery\/" #"\/theme\/default\/" #"\/public\/lib\/"])
        dirs (or (:sources config) (source-dirs project))
        files (->> (find-files dirs)
                (filter #(exts (ext %)))
                (filter (fn [f] (not-any? (fn [e] (re-seq e (.getAbsolutePath f))) excludes))))
        errors (filter (fn [[_ e]] (seq e)) (map get-file-errors files))]

    (if (seq errors)
      (doseq [[f es] errors]
        (println " " (get-rel-path (:root project) f))
        (doseq [[line-no message] (take 10 es)]
          (println "      line" (inc line-no) ":" message))
        (if (> (count es) 10)
          (println "      ... and" (- (count es) 10) "other errors"))))
    (if (empty? errors)
      (println "Checked" (count files) "files: all OK")
      (leiningen.core.main/abort (str "Checked " (count files) " files: " (count errors) " files had illegal content")))))
