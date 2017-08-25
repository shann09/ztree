# ztree

an immutable list->tree lib, write by clojurescirpt, and it can be use with ant design's Table or Tree

根据list中元素的id和parentId等字段把list转换为tree，可以配合蚂蚁金服(antd)的Table和Tree组件使用，也可以单独使用

获取tree中的子孙节点、祖先节点、兄弟节点、上下兄弟节点

## Usage

编译：

```
lein clean;lein cljsbuild auto dev;
或
lein clean;lein cljsbuild once min;
```

查看：

```
在浏览器中打开tree.html，打开console查看打印结果
```

说明：

ztree是一个用cljs写的list->tree库，也是一个打包cljs为npm依赖包的教程，源码中有足够丰富的注释

```
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

```

cljs: 参考ztree/core_test.cljs

```
(defn test []
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
    (.log js/console "raw-list" (.stringify js/JSON (clj->js (ztree/get-raw tree))))
    (.log js/console "treelike" (.stringify js/JSON (clj->js (ztree/get-treelike tree))))
    (.log js/console "full-tree" (.stringify js/JSON (clj->js (ztree/get-tree tree))))
    (.log js/console "descendant" (.stringify js/JSON (clj->js (ztree/pluck-descendant tree ["1"]))))
    (.log js/console "ancestors" (.stringify js/JSON (clj->js (ztree/pluck-ancestors tree ["5"]))))
    (.log js/console "survivors" (.stringify js/JSON (clj->js (ztree/pluck-survivors tree ["2"]))))
    (.log js/console "siblings" (.stringify js/JSON (clj->js (ztree/pluck-siblings tree "100"))))
    (.log js/console "siblings" (.stringify js/JSON (clj->js (ztree/pluck-siblings tree "0"))))
    (.log js/console "siblings" (.stringify js/JSON (clj->js (ztree/pluck-siblings tree "3"))))
    (.log js/console "siblings" (.stringify js/JSON (clj->js (ztree/pluck-siblings tree "6"))))
    (.log js/console "prenext" (.stringify js/JSON (clj->js (ztree/pluck-prenext tree "6"))))
    (.log js/console "prenext" (.stringify js/JSON (clj->js (ztree/pluck-prenext tree "4"))))
    (.log js/console "prenext 8" (.stringify js/JSON (clj->js (ztree/pluck-prenext tree "8"))))
    (.log js/console "prenext" (.stringify js/JSON (clj->js (ztree/pluck-prenext tree "0"))))
    (.log js/console "find" (.stringify js/JSON (clj->js (ztree/find tree "6"))))
    ))
    输出如下：
      raw-list [
      {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
      {"key":"7","parentKey":"2","sortNum":7,"name":"h"},
      {"key":"4","parentKey":"1","sortNum":8,"name":"e"},
      {"key":"0","parentKey":null,"sortNum":0,"name":"a"},
      {"key":"6","parentKey":"5","sortNum":6,"name":"g"},
      {"key":"2","parentKey":"0","sortNum":2,"name":"c"},
      {"key":"1","parentKey":"0","sortNum":1,"name":"b"},
      {"key":"5","parentKey":"2","sortNum":5,"name":"f"},
      {"key":"8","parentKey":"1","sortNum":3,"name":"i"}]
      
      treelike [
      {"key":"0","parentKey":null,"sortNum":0,"name":"a"},
      {"key":"1","parentKey":"0","sortNum":1,"name":"b"},
      {"key":"2","parentKey":"0","sortNum":2,"name":"c"},
      {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
      {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
      {"key":"5","parentKey":"2","sortNum":5,"name":"f"},
      {"key":"7","parentKey":"2","sortNum":7,"name":"h"},
      {"key":"4","parentKey":"1","sortNum":8,"name":"e"},
      {"key":"6","parentKey":"5","sortNum":6,"name":"g"}]
      
      full-tree [
      {"key":"0","parentKey":null,"sortNum":0,"name":"a","children":[
         {"key":"1","parentKey":"0","sortNum":1,"name":"b","children":[
             {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
             {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
             {"key":"4","parentKey":"1","sortNum":8,"name":"e"}]},
         {"key":"2","parentKey":"0","sortNum":2,"name":"c","children":[
             {"key":"5","parentKey":"2","sortNum":5,"name":"f","children":[
                 {"key":"6","parentKey":"5","sortNum":6,"name":"g"}]},
             {"key":"7","parentKey":"2","sortNum":7,"name":"h"}]}]}]
      
      descendant [
      {"key":"1","parentKey":"0","sortNum":1,"name":"b"},
      {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
      {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
      {"key":"4","parentKey":"1","sortNum":8,"name":"e"}]
      
      ancestors [
      {"key":"0","parentKey":null,"sortNum":0,"name":"a"},
      {"key":"2","parentKey":"0","sortNum":2,"name":"c"},
      {"key":"5","parentKey":"2","sortNum":5,"name":"f"}]
      
      survivors [
      {"key":"0","parentKey":null,"sortNum":0,"name":"a"},
      {"key":"1","parentKey":"0","sortNum":1,"name":"b"},
      {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
      {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
      {"key":"4","parentKey":"1","sortNum":8,"name":"e"}]
      
      siblings []
      
      siblings [
      {"key":"0","parentKey":null,"sortNum":0,"name":"a"}]
      
      siblings [
      {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
      {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
      {"key":"4","parentKey":"1","sortNum":8,"name":"e"}]
      
      siblings [
      {"key":"6","parentKey":"5","sortNum":6,"name":"g"}]
      
      prenext [
      null,
      {"key":"6","parentKey":"5","sortNum":6,"name":"g"},
      null]
      
      prenext [
      {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
      {"key":"4","parentKey":"1","sortNum":8,"name":"e"},
      null]
      
      prenext 8 [
      {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
      {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
      {"key":"4","parentKey":"1","sortNum":8,"name":"e"}]
      
      prenext [
      null,
      {"key":"0","parentKey":null,"sortNum":0,"name":"a"},
      null]
      
      find {"key":"6","parentKey":"5","sortNum":6,"name":"g"}
```

js: 参考tree.html

```
var tree = ztree.create_js_tree(
    ["key","parentKey","sortNum","children"],
    [
      { "key": "3" ,"parentKey": "1" ,"sortNum": 3, "name": "d"},
      { "key": "7" ,"parentKey": "2" ,"sortNum": 7, "name": "h"},
      { "key": "4" ,"parentKey": "1" ,"sortNum": 8 ,"name": "e"},
      { "key": "0" ,"parentKey": null ,"sortNum": 0 ,"name": "a"},
      { "key": "6" ,"parentKey": "5" ,"sortNum": 6, "name": "g"},
      { "key": "2" ,"parentKey": "0" ,"sortNum": 2, "name": "c"},
      { "key": "1" ,"parentKey": "0" ,"sortNum": 1, "name": "b"},
      { "key": "5" ,"parentKey": "2" ,"sortNum": 5, "name": "f"},
      { "key": "8" ,"parentKey": "1" ,"sortNum": 3, "name": "i"},
    ]);
  console.log("tree",tree);
  console.log("raw",ztree.get_raw(tree));
  console.log("treelike",ztree.get_treelike(tree));
  console.log("full-tre",ztree.get_tree(tree));
  console.log("descendant",ztree.pluck_descendant(tree,["1"]));
  console.log("ancestors",ztree.pluck_ancestors(tree,["5"]));
  console.log("survivors",ztree.pluck_survivors(tree,["2"]));
  console.log("siblings",ztree.pluck_siblings(tree,"100"));
  console.log("siblings",ztree.pluck_siblings(tree,"0"));
  console.log("siblings",ztree.pluck_siblings(tree,"3"));
  console.log("siblings",ztree.pluck_siblings(tree,"3"));
  console.log("siblings",ztree.pluck_siblings(tree,"6"));
  console.log("prenext",ztree.pluck_prenext(tree,"6"));
  console.log("prenext",ztree.pluck_prenext(tree,"4"));
  console.log("prenext",ztree.pluck_prenext(tree,"0"));
  console.log("prenext",ztree.pluck_prenext(tree,"8"));
  console.log("find",ztree.find(tree,"6"));
```

发布：

```
lein new ztree

npm init //生成package.json，需修改一些配置

npm login //需要在npm官网注册账号

npm publish //发布

npm unpublish <package>@<version>

```


## License

Copyright © 2017 shann09

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
