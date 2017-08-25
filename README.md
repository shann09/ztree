# ztree

用clojurescript写的js库，根据list中元素的id和parentId等字段把list转换为tree，获取tree中的子孙节点、祖先节点、兄弟节点、上下兄弟节点等

可以用js调用，也可以被clojurescript调用，源码稍作修改也可以被clojure调用

可用于配合蚂蚁金服(antd)的Table和Tree组件使用。

list->tree js/cljs lib，work with ant design's Table or Tree. 

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

示例树的层级结构如下：

     0
   /   \
  1      2
 /|\    / \
3 8 4  5  7
       |
       6

```

js: 参考tree.html

```
;创建一棵树，
;  第一个参数[id字段 父节点id字段 排序字段 子节点字段]是定义树的结构，
;      前3个可根据list中字段名称灵活定义，子节点字段可以指定生成的json-tree的子节点字段名称
;  第二个参数是所有节点组成的list
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
  ;返回list，获取原始list
  console.log("raw",ztree.get_raw(tree));
  
  ;返回list，获取排序后的list，按层级和sortNum排序
  console.log("treelike",ztree.get_treelike(tree));
  
  ;返回tree，获取真正的tree，即树形的json
  console.log("full-tree",ztree.get_tree(tree));
  
  ;返回list，获取指定n个key的节点及其所有子孙节点，直到叶子节点，不重复
  console.log("descendant 1",ztree.pluck_descendant(tree,["1"]));
  console.log("descendant 1 5",ztree.pluck_descendant(tree,["1","5"]));
  
  ;返回list，获取指定n个key的节点及其所有父辈节点，直到根节点，不重复
  console.log("ancestors 5",ztree.pluck_ancestors(tree,["5"]));
  console.log("ancestors 5 7",ztree.pluck_ancestors(tree,["5","7"]));
  
  ;返回list，获取（指定n个key的节点及其所有子孙节点）以外的所有节点，即所有节点和descendant的差集
  console.log("survivors 2",ztree.pluck_survivors(tree,["2"]));
  console.log("survivors 2 4",ztree.pluck_survivors(tree,["2","4"]));
  
  ;返回list，获取指定1个key的节点及其所有兄弟节点，不含子孙节点
  console.log("siblings 100",ztree.pluck_siblings(tree,"100"));
  console.log("siblings 0",ztree.pluck_siblings(tree,"0"));
  console.log("siblings 3",ztree.pluck_siblings(tree,"3"));
  console.log("siblings 6",ztree.pluck_siblings(tree,"6"));
  
  ;返回长度为3的list，获取指定1个key的节点及其前后2个兄弟节点，[前一个兄弟, 自己, 下一个兄弟]，如果没有对应兄弟，则对应位置会是null
  console.log("prenext 6",ztree.pluck_prenext(tree,"6"));
  console.log("prenext 4",ztree.pluck_prenext(tree,"4"));
  console.log("prenext 0",ztree.pluck_prenext(tree,"0"));
  console.log("prenext 8",ztree.pluck_prenext(tree,"8"));
  
  ;返回object，获取指定1个key的节点
  console.log("find 6",ztree.find(tree,"6"));
  
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
    
    descendant 1 [
    {"key":"1","parentKey":"0","sortNum":1,"name":"b"},
    {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
    {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
    {"key":"4","parentKey":"1","sortNum":8,"name":"e"}]
    
    descendant 1 5 [
    {"key":"1","parentKey":"0","sortNum":1,"name":"b"},
    {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
    {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
    {"key":"4","parentKey":"1","sortNum":8,"name":"e"},
    {"key":"5","parentKey":"2","sortNum":5,"name":"f"},
    {"key":"6","parentKey":"5","sortNum":6,"name":"g"}]

    ancestors 5 [
    {"key":"0","parentKey":null,"sortNum":0,"name":"a"},
    {"key":"2","parentKey":"0","sortNum":2,"name":"c"},
    {"key":"5","parentKey":"2","sortNum":5,"name":"f"}]
    
    ancestors 5 7 [
    {"key":"0","parentKey":null,"sortNum":0,"name":"a"},
    {"key":"2","parentKey":"0","sortNum":2,"name":"c"},
    {"key":"5","parentKey":"2","sortNum":5,"name":"f"},
    {"key":"7","parentKey":"2","sortNum":7,"name":"h"}]
    
    survivors 2 [
    {"key":"0","parentKey":null,"sortNum":0,"name":"a"},
    {"key":"1","parentKey":"0","sortNum":1,"name":"b"},
    {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
    {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
    {"key":"4","parentKey":"1","sortNum":8,"name":"e"}]
    
    survivors 2 4 [
    {"key":"0","parentKey":null,"sortNum":0,"name":"a"},
    {"key":"1","parentKey":"0","sortNum":1,"name":"b"},
    {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
    {"key":"8","parentKey":"1","sortNum":3,"name":"i"}]
    
    siblings 100 []
    
    siblings 0 [
    {"key":"0","parentKey":null,"sortNum":0,"name":"a"}]
    
    siblings 3 [
    {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
    {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
    {"key":"4","parentKey":"1","sortNum":8,"name":"e"}]
    
    siblings 6 [
    {"key":"6","parentKey":"5","sortNum":6,"name":"g"}]
    
    prenext 6 [
    null,
    {"key":"6","parentKey":"5","sortNum":6,"name":"g"},
    null]
    
    prenext 4 [
    {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
    {"key":"4","parentKey":"1","sortNum":8,"name":"e"},
    null]
    
    prenext 0[
    null,
    {"key":"0","parentKey":null,"sortNum":0,"name":"a"},
    null]
    
    prenext 8 [
    {"key":"3","parentKey":"1","sortNum":3,"name":"d"},
    {"key":"8","parentKey":"1","sortNum":3,"name":"i"},
    {"key":"4","parentKey":"1","sortNum":8,"name":"e"}]
    
    find 6 
    {"key":"6","parentKey":"5","sortNum":6,"name":"g"}
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
    (.log js/console "descendant 1" (.stringify js/JSON (clj->js (ztree/pluck-descendant tree ["1"]))))
    (.log js/console "descendant 1 5" (.stringify js/JSON (clj->js (ztree/pluck-descendant tree ["1","5"]))))
    (.log js/console "ancestors 5" (.stringify js/JSON (clj->js (ztree/pluck-ancestors tree ["5"]))))
    (.log js/console "ancestors 5 7" (.stringify js/JSON (clj->js (ztree/pluck-ancestors tree ["5","7"]))))
    (.log js/console "survivors 2" (.stringify js/JSON (clj->js (ztree/pluck-survivors tree ["2"]))))
    (.log js/console "survivors 2 4" (.stringify js/JSON (clj->js (ztree/pluck-survivors tree ["2","4"]))))
    (.log js/console "siblings 100" (.stringify js/JSON (clj->js (ztree/pluck-siblings tree "100"))))
    (.log js/console "siblings 0" (.stringify js/JSON (clj->js (ztree/pluck-siblings tree "0"))))
    (.log js/console "siblings 3" (.stringify js/JSON (clj->js (ztree/pluck-siblings tree "3"))))
    (.log js/console "siblings 6" (.stringify js/JSON (clj->js (ztree/pluck-siblings tree "6"))))
    (.log js/console "prenext 6" (.stringify js/JSON (clj->js (ztree/pluck-prenext tree "6"))))
    (.log js/console "prenext 4" (.stringify js/JSON (clj->js (ztree/pluck-prenext tree "4"))))
    (.log js/console "prenext 0" (.stringify js/JSON (clj->js (ztree/pluck-prenext tree "0"))))
    (.log js/console "prenext 8" (.stringify js/JSON (clj->js (ztree/pluck-prenext tree "8"))))
    (.log js/console "find 6" (.stringify js/JSON (clj->js (ztree/find tree "6"))))
    ))
    
    输出同js
```

## 打包成依赖库：

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

发布：

```
lein new ztree

npm init //生成package.json，需修改一些配置才能发布成功

npm login //需要在npm官网注册账号

npm publish //发布

npm unpublish <package>@<version>

```


## License

Copyright © 2017 shann09

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
