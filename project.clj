(defproject ztree "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.7.228"]
                 ]
  :plugins [[lein-cljsbuild "1.1.1"]
            ]
  :clean-targets [:target-path "out"]
  :cljsbuild {:builds [{;; lein cljsbuild auto dev
                        :id "dev"
                        :source-paths ["src","test"]
                        :compiler {:main ztree.core_test
                                   :output-to "out/ztree.js" ;导出的主js文件
                                   :output-dir "out/dev" ;在普通模式下编译，导出的主js文件会依赖该目录下的文件，浏览器会请求这个目录下的js文件，所以这个目录下的文件不能删掉
                                   :optimizations :none
                                   :pretty-print true}}
                       {;; 使用gcc的高级压缩模式编译出生产环境用的js，只编译lib
                        ;; lein clean;lein cljsbuild once min;
                        :id "min"
                        :source-paths ["src"] ;深度遍历目录下的所有的cljs文件，并编译这些文件
                        :compiler {:main ztree
                                   :output-to "out/ztree.js" ;导出的主js文件
                                   :output-dir "out/min" ;在高级模式下编译，导出的主js文件不依赖于这个目录下的文件，浏览器只会请求主js文件，可以把这个目录删掉
                                   :optimizations :advanced
                                   :pretty-print false}}]}
  )
