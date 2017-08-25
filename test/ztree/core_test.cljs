(ns ztree.core-test
  (:require [ztree :as ztree]))

(defn test []
  (.log js/console "---------in cljs tree---------")

  (let [d [
           { :key "3" :parentKey "1" :sortNum 3 :name "d"}
           { :key "7" :parentKey "2" :sortNum 7 :name "h"}
           { :key "4" :parentKey "1" :sortNum 8 :name "e"}
           { :key "0" :parentKey nil :sortNum 0 :name "a"}
           { :key "6" :parentKey "5" :sortNum 6 :name "g"}
           { :key "2" :parentKey "0" :sortNum 2 :name "c"}
           { :key "1" :parentKey "0" :sortNum 1 :name "b"}
           { :key "5" :parentKey "2" :sortNum 5 :name "f"}
           { :key "8" :parentKey "1" :sortNum 3 :name "i"}
           ]
        tree (ztree/create-cljs-tree [:key :parentKey :sortNum :children] d)
        ]
    (.log js/console "raw-list" (clj->js (ztree/get-raw tree)))
    (.log js/console "treelike" (clj->js (ztree/get-treelike tree)))
    (.log js/console "full-tree" (clj->js (ztree/get-tree tree)))
    (.log js/console "descendant" (clj->js (ztree/pluck-descendant tree ["1"])))
    (.log js/console "ancestors" (clj->js (ztree/pluck-ancestors tree ["5"])))
    (.log js/console "survivors" (clj->js (ztree/pluck-survivors tree ["2"])))
    (.log js/console "siblings" (clj->js (ztree/pluck-siblings tree "100")))
    (.log js/console "siblings" (clj->js (ztree/pluck-siblings tree "0")))
    (.log js/console "siblings" (clj->js (ztree/pluck-siblings tree "3")))
    (.log js/console "siblings" (clj->js (ztree/pluck-siblings tree "6")))
    (.log js/console "prenext" (clj->js (ztree/pluck-prenext tree "6")))
    (.log js/console "prenext" (clj->js (ztree/pluck-prenext tree "4")))
    (.log js/console "prenext 8" (clj->js (ztree/pluck-prenext tree "8")))
    (.log js/console "prenext" (clj->js (ztree/pluck-prenext tree "0")))
    (.log js/console "find" (clj->js (ztree/find tree "6")))
    ))

(test)
