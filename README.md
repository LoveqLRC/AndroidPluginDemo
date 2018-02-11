# AndroidPluginDemo
## 插件化的意义
1. 减少apk的大小，用到的功能才去下载，因为很多功能对于用户是用不到的。
2. 每个模块封装成不同的插件apk，不同模块又可以单独编译，提高开发效率

## 实现插件化有做哪些事
1.	加载插件的Activty，如何启动一个没有在主apk的AndroidManifest.xml配置的Activity
2.	加载插件的类
3.	加载插件的资源

### 加载插件Activity如何实现?
首先要熟悉一个Activty是如何启动的，只有熟悉这些才能想办法去解决，Activity启动流程很复杂，但大概的流程我们心中也有数
1.解析启动Activity
2.错误校验和启动模式
3.启动Activity 进入onPause，被启动Activity进入onCreate,onStart,onResume。

那么如何做呢？必须想办法跳过错误校验，使不在AndroidManifest.xml配置依旧能启动。那么就要要用到动态代理和反射技术，首先在主Apk中新建一个ProxyActvity，这时已经在AndroidManifest.xml配置了，这是一个壳。当执行错误校验的时候，我们用这个ProxyActvity壳包装插件Activty(对应上面第二步)，是它能通过错误校验，当通过校验后，我们去壳，还原插件Activty(对应上面第三步)，那么就能达到我们的目的了。

当然了插件化没有上面说的那么简单，可以参考[Android插件化系列第（一）篇---Hook技术之Activity的启动过程拦截](https://www.jianshu.com/p/69bfbda302df)
