(ns ztree
  (:require [cljs.core :refer [PersistentQueue]]))

(defn ^String nilize
  "空字符串转nil"
  [^String s]
  (if (= s "") nil s))

;clojure和clojurescript的实现是有区别的：
;clojure
;  (:import [clojure.lang PersistentQueue])
;  PersistentQueue/EMPTY
;  .cons
;  (rseq s)
;clojurescript
;  (:require [cljs.core :refer [PersistentQueue]])
;  (.-EMPTY PersistentQueue)
;  -conj
;  (rseq (vec s))
;  ^:export把要往外暴露的接口标记为export，这样就能够让js代码调用该接口
;  ns命名空间必须跟目录结构保持一致，这样才能保证gcc的高级编译模式能够编译成功


(defn- unlevel- [s]
  ;(.log js/console "unlevel-")
  (map #(dissoc % :ztree/level) s))

(defn- treelike-level- [[keyName parentKeyName sortName _] raw]
  ;检查是否有父节点是不存在的
  (let [[k p] (reduce
                (fn [[k p] t]
                  [(conj k (t keyName))
                   (if (t parentKeyName) (conj p (t parentKeyName)) p)])
                [#{} #{}]
                raw)]
    (if (contains? k nil) (throw (js/Error. "key不能为null")))
    (doseq [t p]
      (if-not (contains? k t)
        (throw (js/Error. (str "找不到父节点: " t))))))
  (loop [q (reduce conj (.-EMPTY PersistentQueue) raw),
         s (sorted-set-by (fn [{xlevel :ztree/level xsortNum sortName xvalue keyName}
                               {ylevel :ztree/level ysortNum sortName yvalue keyName}]
                            (if (= xlevel ylevel)
                              (if (= xsortNum ysortNum)
                                (compare xvalue yvalue)
                                (compare xsortNum ysortNum))
                              (compare xlevel ylevel))))]
    (if-not (peek q)
      s
      (let [t (peek q)
            r (pop q)
            [nq ns] (if-let [pk (nilize (t parentKeyName))]
                      ;有父节点
                      (if-let [p (first (filter #(= (% keyName) pk) s))]
                        ;找到父节点，节点上树
                        [r (-conj s (assoc t :ztree/level (inc (:ztree/level p))))]
                        ;没找到父节点，把节点放回队列尾部
                        [(conj r t) s])
                      ;没有父节点，顶级节点上树
                      [r (-conj s (assoc t :ztree/level 1))])]
        (recur nq ns)))))

(defn- pluck-descendant- [[keyName parentKeyName _ _] s keyList];^clojure.lang.PersistentTreeSet，^cljs.core.PersistentTreeSet
  (loop [q (reduce conj (.-EMPTY PersistentQueue) keyList)  ;待查找队列
         f []  ;结果vec
         l (vec s)   ;被遍历seq
         c (count keyList) ;原始队列长度计数器
         ]
    (if-not (peek q)
      f
      (let [k (peek q)
            r (pop q)
            [nq nf nl nc] (if (> c 0)
                            ;是原始"待查找队列"的元素，既要查本身，也要查子节点
                            (reduce (fn [[q f l c fd] t] ;reduce遍历本节点和子节点的迭代函数，q队列，f结果，l过滤后被遍历seq，c原始队列长度计数器，fd是否找到本节点，t遍历元素
                                      (if fd
                                        (if (= (t parentKeyName) k)
                                          [(conj q (t keyName)) (conj f t) l c true]
                                          [q f (conj l t) c true])
                                        (if (= (t keyName) k)
                                          [q (conj f t) l c true]
                                          [q f (conj l t) c false])
                                        )) [r f [] (dec c) false] l)
                            ;是后入"待查找队列"的元素，只查子节点
                            (reduce (fn [[q f l c] t] ;reduce遍历子节点的迭代函数，q队列，f结果，l过滤后被遍历seq，c原始队列长度计数器，t遍历元素
                                      (if (= (t parentKeyName) k)
                                        [(conj q (t keyName)) (conj f t) l c]
                                        [q f (conj l t) c]))
                              [r f [] (dec c)] l)
                            )]
        (recur nq nf nl nc)))))

(defn- pluck-ancestors- [[keyName parentKeyName _ _] s keyList]
  (vec (second
         (reduce (fn [[m a] t]
                   (if (m (t keyName))
                     [(assoc m (t parentKeyName) true) (conj a t)]
                     [m a]))
           [(reduce (fn [m k] (assoc m k true)) {} keyList) '()] (rseq (vec s)) ))))

(defn- pluck-survivors- [[keyName parentKeyName _ _] s keyList]
  (vec (second
         (reduce (fn [[m r] t]
                   (if (m (t keyName))
                     [m r]
                     (if (m (t parentKeyName))
                       [(assoc m (t keyName) true) r]
                       [m (conj r t)])
                     ))
           [(reduce (fn [m k] (assoc m k true)) {} keyList) []] s))))

(defn- pluck-siblings- [[keyName parentKeyName _ _] s id]
  (loop [l s
         level nil
         level-items []
         t nil]
    ;(.log js/console "pluck-siblings-" (clj->js level-items))
    (let [c (first l)]
      (if (not c)
        (filter #(if t (= (t parentKeyName) (% parentKeyName)) false) level-items)
        (if (and t (not= (c :ztree/level) (t :ztree/level)))
          (filter #(= (t parentKeyName) (% parentKeyName)) level-items)
          ;（t还没找到，且还在同一个level），递归
          ; 或者（t还没找到，且不在同一个level），递归
          ; 或者（t找到了，且在同一个level），递归；
          (recur
            (rest l)
            (c :ztree/level)
            (if (= (c :ztree/level) level) (conj level-items c) [c])
            (or t (if (= (c keyName) id) c nil))
            ))))))

(defn- pluck-prenext- [[keyName _ _ _] siblings id]
  (loop [l siblings
         prepre nil
         pre nil]
    (let [c (first l)
          t (if pre (if (= id (pre keyName)) pre nil) nil)]
      (if (not c)
        [(if t prepre nil) t nil]
        (if t
          [prepre t c]
          (recur
            (rest l)
            pre
            c))))))

(defn- find- [[keyName _ _ _] s id]
  (first (filter #(= (% keyName) id) s)))

(defn- treeize- [[keyName parentKeyName _ childrenName] s]
  (first (reduce
           (fn [[r m] t] ;r树，m地址表
             (if-let [pk (t parentKeyName)] ;pk父节点key
               ;不是top节点
               (let [pa (m pk) ;pa父节点的地址
                     p (get-in r pa) ;p父节点
                     c (if-let [c (p childrenName)] c []) ;c父节点底下的子节点vec
                     np (assoc p childrenName (conj c t)) ;np新的父节点
                     na (conj pa childrenName (count c)) ;na节点的地址
                     ]
                 [(update-in r pa (fn [o n] n) np) (assoc m (t keyName) na)])
               ;是top节点
               [(conj r t) (assoc m (t keyName) [(count r)])]
               ))
           [[] {}] s))
  )

(defprotocol TREE
  (
    ;^:export
    get-raw [_])
  (
    ;^:export
    get-treelike [_])
  (
    ;^:export
    pluck-descendant [_ keyList])
  (
    ;^:export
    pluck-ancestors [_ keyList])
  (
    ;^:export
    pluck-survivors [_ keyList])
  (
    ;^:export
    pluck-siblings [_ id])
  (
    ;^:export
    pluck-prenext [_ id])
  (
    ;^:export
    find [_ id])
  (
    ;^:export
    get-tree [_])
  )


(defrecord Tree [raw treelike-level-fn unlevel-fn
                 pluck-descendant-fn pluck-ancestors-fn pluck-survivors-fn
                 pluck-siblings-fn pluck-prenext-fn
                 find-fn
                 treeize-fn
                 lang]
  TREE
  (get-raw [_]
    (case lang
      :cljs raw
      :js (clj->js raw)))
  (get-treelike [_]
    (case lang
      :cljs (unlevel-fn (treelike-level-fn raw))
      :js (clj->js (unlevel-fn (treelike-level-fn raw)))))
  (pluck-descendant [_ keyList]
    (case lang
      :cljs (pluck-descendant-fn (unlevel-fn (treelike-level-fn raw)) keyList)
      :js (clj->js (pluck-descendant-fn (unlevel-fn (treelike-level-fn raw)) keyList))))
  (pluck-ancestors [_ keyList]
    (case lang
      :cljs (pluck-ancestors-fn (unlevel-fn (treelike-level-fn raw)) keyList)
      :js (clj->js (pluck-ancestors-fn (unlevel-fn (treelike-level-fn raw)) keyList))))
  (pluck-survivors [_ keyList]
    (case lang
      :cljs (pluck-survivors-fn (unlevel-fn (treelike-level-fn raw)) keyList)
      :js (clj->js (pluck-survivors-fn (unlevel-fn (treelike-level-fn raw)) keyList))))
  (pluck-siblings [_ id]
    (case lang
      :cljs (unlevel-fn (pluck-siblings-fn (treelike-level-fn raw) id))
      :js (clj->js (unlevel-fn (pluck-siblings-fn (treelike-level-fn raw) id)))))
  (pluck-prenext [_ id]
    (case lang
      :cljs (unlevel-fn (pluck-prenext-fn (pluck-siblings-fn (treelike-level-fn raw) id) id))
      :js (clj->js (unlevel-fn (pluck-prenext-fn (pluck-siblings-fn (treelike-level-fn raw) id) id)))))
  (find [_ id]
    (case lang
      :cljs (find-fn (unlevel-fn (treelike-level-fn raw)) id)
      :js (clj->js (find-fn (unlevel-fn (treelike-level-fn raw)) id))))
  (get-tree [_]
    (case lang
      :cljs (treeize-fn (unlevel-fn (treelike-level-fn raw)))
      :js (clj->js (treeize-fn (unlevel-fn (treelike-level-fn raw))))))
  )

(defn
  ;^:export
  create-js-tree [[keyName parentKeyName sortName childrenName] raw]
  (let [ks (js->clj [keyName parentKeyName sortName childrenName])]
    (Tree.
      (js->clj raw)
      (memoize (partial treelike-level- ks))
      (memoize unlevel-)
      (memoize (partial pluck-descendant- ks))
      (memoize (partial pluck-ancestors- ks))
      (memoize (partial pluck-survivors- ks))
      (memoize (partial pluck-siblings- ks))
      (memoize (partial pluck-prenext- ks))
      (memoize (partial find- ks))
      (memoize (partial treeize- ks))
      :js)
    ))


(defn
  ;^:export
  create-cljs-tree [[keyName parentKeyName sortName childrenName] raw]
  (let [ks [keyName parentKeyName sortName childrenName]]
    (Tree.
      raw
      (memoize (partial treelike-level- ks))
      (memoize unlevel-)
      (memoize (partial pluck-descendant- ks))
      (memoize (partial pluck-ancestors- ks))
      (memoize (partial pluck-survivors- ks))
      (memoize (partial pluck-siblings- ks))
      (memoize (partial pluck-prenext- ks))
      (memoize (partial find- ks))
      (memoize (partial treeize- ks))
      :cljs)
    ))

;使用cljs来写代码可以获得不少好处：
;    不变性，gcc高级编译，lisp函数式写法，宏，clojure代码重用，记忆函数，尾递归优化等等等等
;cljs非常适合编写依赖库
;
;export的目的就是为了让js可以调用编译好的包，
;如果只是想让cljs调用（参考core_test.cljs），那就不需要export相关代码（可以删掉^:exoprt和aset js/exports）
;
;export的方式有2种
;  一种是^:export
;      这种方式会把被标记为^:export的方法或对象暴露到全局，
;      当前项目不使用这种方式，所以上面的标记都被注释掉，标记方式类似如下
;          (defprotocol (^:export some-fn []) (^:export some-fn-2 []) )
;          (defn ^:export some-fn-3 [] ... )
;      用于直接在html的<script>标签中引入js包
;      然后用namespace.exported_fn的方式调用，参考tree.html，类似如下
;          ztree.create_js_tree(...);
;  另一种是aset js/exports
;      即把暴露的方法或对象绑定到exports上
;      这种方式用于将代码打包为npm依赖包，上传到npm仓库上，
;          参考package.json，npm init,npm login,npm publish
;      需要引用该依赖包的代码可以配置package.json增加 "cl-js-ztree": "^1.0.35",
;      并使用npm install来下载该依赖，然后调用方式类似如下
;          //在.js或.jsx文件中引入
;          import ztree from 'cl-js-ztree';
;          ztree.create_js_tree(...);

(doseq [[k f] {"create_js_tree" create-js-tree
               "create_cljs_tree" create-cljs-tree
               "get_raw" get-raw
               "get_treelike" get-treelike
               "pluck_descendant" pluck-descendant
               "pluck_ancestors" pluck-ancestors
               "pluck_survivors" pluck-survivors
               "pluck_siblings" pluck-siblings
               "pluck_prenext" pluck-prenext
               "find" find
               "get_tree" get-tree
               }]
  (aset js/exports k f))


;下面这种方式测试不成功，但可能适用其他import方式，待学习
;(set! (.-exports js/module) #js {:create-js-tree create-js-tree
;                                 :create-cljs-tree create-cljs-tree
;                                 })
