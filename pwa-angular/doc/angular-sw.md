菜单
家
订阅
 
Angular Service Worker - 将您的应用程序转换为PWA的循序渐进指南
最近更新时间： 2018年6月16日 local_offer  PWA
借助Angular Service Worker和Angular CLI内置的PWA支持，现在可以比以往更简单地使我们的Web应用程序可下载和安装，就像本机移动应用程序一样。

在这篇文章中，我们将介绍如何配置Angular CLI构建管道，以生成可在生产模式下下载和安装的应用程序，就像本机应用程序一样。

我们还将为我们的PWA添加App Manifest，并使应用程序一键安装。

我邀请您一起编写代码，因为我们将使用Angular CLI从头开始构建应用程序，我们将逐步配置它以启用此功能，该功能目前仅适用于本机应用程序。

我们还将详细了解CLI正在执行的操作，以便您还可以根据需要将Service Worker添加到现有应用程序中。

在此过程中，我们将了解Angular Service Worker的设计及其工作原理，并了解它是如何以与其他构建时生成的服务工作者完全不同的方式工作的。

比当前的Native Mobile安装更好
您即将看到的服务工作者下载和安装体验都在后台进行，不会影响用户体验，实际上比我们用于版本升级的当前本机移动机制要好得多。

这种基于PWA的机制甚至对增量版本升级提供了隐式支持 - 例如，如果我们仅更改CSS，则只需要重新安装新的CSS，而不必再次安装整个应用程序！

此外，版本升级可以在后台以用户友好的方式透明地处理。用户将始终只在其打开的多个选项卡中看到该应用程序的一个版本，但我们也可以提示用户并询问他是否要立即升级版本。

性能优势和离线支持
在用户浏览器上安装我们所有的JavaScript和CSS束的性能优势使得应用程序的引导多快。多快了？根据应用的不同，这可能会快几倍到一个数量级。

通常，任何应用程序都将受益于此PWA下载和安装功能所带来的性能提升，这不是移动应用程序所独有的。

将完整的Web应用程序下载并安装在用户浏览器上也是启用应用程序脱机模式的第一步，但请注意，完整的脱机体验不仅需要下载和安装功能。

我们可以看到，这种基于PWA的新应用程序安装功能的多重优势是巨大的！让我们详细介绍这个非常棒的功能。

目录
在这篇文章中，我们将讨论以下主题：

步骤1 - 使用Angular CLI对Angular PWA应用程序进行脚手架
第2步 - 了解如何手动添加Angular PWA支持
第3步 - 了解Angular Service Worker运行时缓存机制
第4步 - 运行并了解PWA生产版本
步骤5 - 在生产模式下启动Angular PWA
步骤6 - 部署新的应用程序版本，了解版本管理
第7步 - 使用App Manifest进行一键安装
摘要
这篇文章是Angular PWA系列的一部分，这里是完整的系列：

服务工作者 - 实用指导（几个例子）
Angular App Shell - 提升应用程序启动性能
Angular Service Worker - 将您的应用程序转换为PWA的循序渐进指南
角度推送通知 - 循序渐进指南
现在，本部分将重点介绍CLI配置和Angular Service Worker，以了解应用程序下载和安装的特定用例。

所以，不用多说了，让我们开始将我们的Angular应用程序变成PWA吧！

第1步（共7步） - 使用Angular CLI对Angular PWA应用程序进行脚手架
通过几个命令，CLI将为我们提供一个启用了下载和安装的工作应用程序。创建Angular PWA的第一步是将Angular CLI升级到最新版本：

npm install -g @angular/cli@latest
如果您想尝试最新功能，也可以获得下一个即将推出的版本：

npm install -g @angular/cli@next
有了这个，我们现在可以构建一个Angular应用程序并添加Angular Service Worker支持：

ng new angular-pwa-app --service-worker
我们还可以使用以下命令将Angular Service Worker添加到现有应用程序：

ng add @angular/pwa --project <name of project as in angular.json>
第2步，共7步 - 了解如何手动添加Angular PWA支持
应用程序搭建的应用程序几乎与没有PWA支持的应用程序相同。如果您需要手动升级应用程序，请查看此标志包含的内容。

我们可以看到@angular/service-worker包已添加到package.json。此外，serviceWorkerCLI配置文件中的新标志设置为true angular.json：

  “ apps ”：[
    {
      “ root ”： “ src ”，
      “ outDir ”： “ dist ”，
      “资产”：[
        “资产”，
        “ favicon.ico ”
      ]
      “ index ”： “ index.html ”，
      “ main ”： “ main.ts ”，
      “ polyfills ”： “ polyfills.ts ”，
      “ test ”： “ test.ts ”，
      “ tsconfig ”： “ tsconfig.app.json ”，
      “ testTsconfig ”： “ tsconfig.spec.json ”，
      “ prefix ”： “ app ”，
      “风格”：[
        “ styles.css ”
      ]
      “脚本”：[]，
      “ environmentSource ”： “ environment / environment.ts ”，
      “环境”：{
        “ dev ”： “ environment / environment.ts ”，
        “ prod ”： “ environments / environment.prod.ts ”
      }，
      “ serviceWorker ”：是的
    }
  ]
查看原始03.json 由GitHub托管❤
什么是serviceWorker标志吗？
此标志将导致生成版本在输出dist文件夹中包含一些额外的文件：

Angular Service Worker文件 ngsw-worker.js
Angular Service Worker的运行时配置 ngsw.json
请注意，ngsw代表Angular Service Worker

我们将详细介绍这两个文件，现在让我们看看CLI为PWA支持添加了哪些内容。

怎么ServiceWorkerModule办？
CLI还在我们的应用程序根模块中包含了Service Worker模块：


@ NgModule（{
  声明：[
    AppComponent
  ]
  进口：[
    BrowserModule，
    AppRoutingModule，
    ServiceWorkerModule。寄存器（' /ngsw-worker.js '，{启用：环境。生产 }）
  ]
  提供者：[]，
  bootstrap：[ AppComponent ]
}）
导出 类 AppModule {}

查看 由GitHub用❤托管的原始04.ts
该模块提供了几种可注射服务：

SwUpdate 用于管理应用程序版本更新
SwPush 用于执行服务器Web推送通知
更重要的是，该模块ngsw-worker.js通过调用来在用户的浏览器中加载脚本，在浏览器中注册Angular Service Worker（如果Service Worker支持可用）navigator.serviceWorker.register()。

调用register()导致ngsw-worker.js文件在单独的HTTP请求中加载。有了这个，只有一件事可以将我们的Angular应用程序变成PWA。

构建配置文件 ngsw-config.json
CLI还添加了一个名为的新配置文件ngsw-config.json，用于配置Angular Service Worker运行时行为，生成的文件带有一些智能默认值。

根据您的应用程序，您甚至可能不需要编辑此文件！

这是文件的样子：


...
“ buildOptimizer ”：是的，
“ serviceWorker ”：是的，
“ ngswConfigPath ”： “ src / ngsw-config.json ”，
“ fileReplacements ”：[
  {
    “ replace ”： “ src / environments / environment.ts ”，
    “ with ”： “ src / environments / environment.prod.ts ”
  }
...
  
查看 由GitHub用❤托管的原始01.ts
这里有很多事情，所以让我们一步一步地分解它。此文件包含默认缓存行为或Angular Service Worker，它定位应用程序静态资产文件：index.htmlCSS和Javascript包。

第3步（共7步） - 了解Angular Service Worker运行时缓存机制
Angular Service Worker可以在浏览器缓存存储中缓存各种内容。

这是一种基于Javascript的键/值缓存机制，与标准浏览器Cache-Control机制无关，可以单独使用这两种机制。

assetGroups配置文件部分的目标是准确配置Angular Service Worker在缓存存储中缓存的HTTP请求，并且有两个缓存配置条目：

app为所有单页应用程序文件命名的一个条目（所有应用程序index.html，CSS和Javascript包以及favicon）

另一个名称assets，对于也在dist文件夹中提供的任何其他资产，例如图像，但不一定是每页运行所必需的

缓存静态文件是应用程序本身
根据应用板块中的文件是应用程序：一个页面是由它的组合，index.html再加上它的CSS和JS束。应用程序的每个页面都需要这些文件，并且不能延迟加载。

对于这些文件，我们希望尽可能早地和永久地缓存它们，这就是app缓存配置的作用。

这些app文件将由Service Worker主动下载并在后台安装，这就是安装模式的prefetch含义。

服务工作人员不会等待应用程序请求这些文件，而是提前下载并缓存它们，以便下次请求时可以为它们提供服务。

对于共同构成应用程序本身（index.html，CSS和Javascript包）的文件，这是一个很好的策略，因为我们已经知道我们将一直需要它们。

缓存其他辅助静态资产
另一方面，只有在请求它们时才会缓存资产文件（意味着安装模式是lazy），但是如果它们曾经被请求过一次，并且如果有新版本可用，那么它们将被提前下载（这是什么更新模式prefetch意味着）。

对于在单独的HTTP请求（例如图像）中下载的任何资产，这是一个很好的策略，因为根据用户访问的页面，可能并不总是需要它们。

但如果他们需要一次，那么我们可能也需要更新版本，所以我们不妨提前下载新版本。

这些是默认值，但我们可以根据自己的应用进行调整。app但是，在文件的特定情况下，我们不太可能想要使用其他策略。

毕竟，app缓存配置是我们正在寻找的下载和安装功能。也许我们在CLI生成的捆绑包之外使用其他文件？在这种情况下，我们希望调整我们的配置。

重要的是要记住，使用这些默认值，我们已经准备好了可下载和可安装的应用程序，所以让我们试试吧！

第4步，共7步 - 运行并了解PWA生产版本
让我们首先在应用程序中添加一些可视化内容，清楚地标识在用户浏览器中运行的给定版本。例如，我们可以使用以下内容替换app.component.html文件的内容：


< H1 > 版 V1  被 捉迷藏 ... < / H1 >

查看 由GitHub用❤托管的原始02.ts
现在让我们构建一个Hello world PWA应用程序。Angular Service Worker只能在生产模式下使用，所以让我们首先对我们的应用程序进行生产构建：

ng build --prod
这需要一点时间，但过了一段时间，我们将在dist文件夹中提供应用程序构建。

生产构建文件夹
让我们看看我们的构建文件夹中有什么，这里是生成的最多文件：

包含Angular Service Worker的Angular CLI dist文件夹

我们可以看到，构建配置文件中的serviceWorker标志angular.json导致Angular CLI包含一些额外的文件（以蓝色突出显示）。

什么是ngsw-worker.js文件？
该文件是在角服务人员本身。与所有服务工作者一样，它通过自己独立的HTTP请求获得，以便浏览器可以跟踪它是否已更改，并将其应用于服务工作者生命周期（在本文中详细介绍）。

它ServiceWorkerModule会通过调用间接触发加载此文件navigation.serviceWorker.register()。

请注意，Angular Service Worker文件ngsw-worker.js将始终与每个构建相同，因为它直接由CLI复制node_modules。

在升级到包含新版Angular Service Worker的新Angular版本之前，此文件将保持不变。

什么是ngsw.json文件？
这是Angular Service工作人员将使用的运行时配置文件。此文件基于该文件构建ngsw-config.json，并包含Angular Service Worker在运行时需要缓存哪些文件以及何时缓存所需的所有信息。

以下是ngsw.json运行时配置文件的外观：

{
  “ configVersion ”： 1，
  “ index ”： “/ index.html ”，
  “ assetGroups ”：[
    {
      “名字”： “ app ”，
      “ installMode ”： “ prefetch ”，
      “ updateMode ”： “ prefetch ”，
      “网址”：[
        “ /favicon.ico ”，
        “ /index.html ”，
        “ /inline.5646543f86fbfdc19b11.bundle.js ”，
        “ /main.3bb4e08c826e33bb0fca.bundle.js ”，
        “ /polyfills.55440df0c9305462dd41.bundle.js ”，
        “ /styles.1862c2c45c11dc3dbcf3.bundle.css ”
      ]
      “模式”：[]
    }，
    {
      “名称”： “资产”，
      “ installMode ”： “懒惰”，
      “ updateMode ”： “ prefetch ”，
      “网址”：[]，
      “模式”：[]
    }
  ]
  “ dataGroups ”：[]，
  “ hashTable ”：{
    “ /inline.5646543f86fbfdc19b11.bundle.js ”： “ 1028ce05cb8393bd53706064e3a8dc8f646c8039 ”，
    “ /main.3bb4e08c826e33bb0fca.bundle.js ”： “ ae15cc3875440d0185b46b4b73bfa731961872e0 ”，
    “ /polyfills.55440df0c9305462dd41.bundle.js ”： “ c3b13e2980f9515f4726fd2621440bd7068baa3b ”，
    “ /styles.1862c2c45c11dc3dbcf3.bundle.css ”： “ 3318b88e1200c77a5ff691c03ca5d5682a19b196 ”，
    “ /favicon.ico ”： “ 84161b857f5c547e3699ddfbffc6d8d737542e01 ”，
    “ /index.html ”： “ cfdca0ab1cec8379bbbf8ce4af2eaa295a3f3827 ”
  }
}
查看 由GitHub用❤托管的原始05.json
我们可以看到，此文件是文件的扩展版本ngsw-config.json，其中所有wilcard网址都已应用，并替换为与其匹配的任何文件的路径。

Angular Service Worker如何使用该ngsw.json文件？
Angular Service Worker将在安装模式下主动加载这些文件，或者在安装模式prefetch的情况下根据需要加载这些文件lazy，并且还将文件存储在缓存存储中。

当用户首次加载应用程序时，此加载将在后台进行。下次用户刷新页面时，Angular Service Worker将拦截HTTP请求，并将提供缓存文件，而不是从网络获取它们。

请注意，每个资产在哈希表中都有一个哈希条目。如果我们对其中列出的任何文件进行任何修改（即使它只有一个字符），我们将在以下Angular CLI构建中使用完全不同的哈希。

然后，Angular Service Worker将知道该文件在服务器上有可用的新版本，需要在适当的时候加载。

现在我们已经对所发生的一切进行了很好的概述，让我们看看这一点！

第5步 - 在生产模式下启动Angular PWA
然后让我们以生产模式启动应用程序，为了做到这一点，我们需要一个小型Web服务器。一个很好的选择http-server，所以让我们安装它：

npm install -g http-server
然后让我们进入该dist文件夹，并以生产模式启动应用程序：

cd dist
http-server -c-1 .
该-c-1选项将禁用服务器缓存，服务器通常将在端口上运行8080，为应用程序的生产版本提供服务。

请注意，如果你有口8080堵塞，应用程序可能运行在8081，8082等等，所使用的端口记录在启动时的控制台。

如果您在另一台服务器上本地运行REST API，例如在端口9000中，您还可以使用以下命令代理对它的任何REST API调用：

http-server -c-1 --proxy http://localhost:9000 . 
运行服务器后，让我们转到http://localhost:8080，然后使用Chrome开发工具查看我们运行的内容：

角度服务工作者

正如我们所看到的，我们现在已经运行了V1版本，并且我们已ngsw-worker.js按预期安装了带有源文件的Service Worker ！

Javascript和CSS包存储在哪里？
所有Javascript和CSS文件，甚至index.html所有文件都已在后台下载并安装在浏览器中供以后使用。

使用Chrome开发工具可以在缓存存储中找到这些文件：

角度服务工作者

Angular Service Worker将在您下次加载页面时开始提供应用程序文件。尝试点击刷新，您可能会注意到应用程序启动速度更快。

请注意，性能改进在生产中比在更加明显 localhost

使应用程序脱机
为了确认应用程序确实已经下载并安装到用户浏览器中，让我们做一个确凿的测试：让我们通过按Ctrl + C来关闭服务器。

现在让我们在关闭http-server进程后点击刷新：你可能会对应用程序仍然运行感到惊讶，我们得到完全相同的屏幕！

在控制台上，我们将找到以下消息：

An unknown error occurred when fetching the script.
ngsw-worker.js Failed to load resource: net::ERR_CONNECTION_REFUSED
它看起来像所有Javascript和CSS捆绑文件，使应用程序从网络以外的其他地方获取，因为应用程序仍在运行。

尝试从网络中获取的唯一文件是Service Worker文件本身，这是预期的（稍后会详细介绍）。

第6步（共7步） - 部署新的应用程序版本，了解版本管理
这是一个很棒的功能，但是缓存所有内容并不是有点危险，如果有错误并且我们想要发布新版本的代码怎么办？

假设我们对应用程序进行了一些小改动，例如在styles.css文件中编辑全局样式。在再次运行生产版本之前，让我们保留以前的版本ngsw.json，以便我们可以看到更改的内容。

现在让我们再次运行生成版本，并比较生成的ngsw.json文件：

角度服务工作者

正如我们所看到的，构建输出中唯一改变的是CSS包，除了index.html（正在加载新包的地方）之外，所有剩余文件都保持不变。

Angular Service Worker如何安装新的应用程序版本？
每次用户重新加载应用程序时，Angular Service Worker都会检查ngsw.json服务器上是否有新文件可用。

这是为了与标准Service Worker行为保持一致，并避免长时间运行应用程序的旧版本。陈旧版本可能包含错误甚至完全损坏，因此如果服务器上有新的应用程序版本，则必须经常检查。

在我们的示例中，ngsw.json将比较文件的先前版本和新版本，并将在后台下载并安装新的CSS捆绑包。

下次用户重新加载页面时，将显示新的应用程序版本！

通知用户新版本可用
对于用户可能已打开数小时的长时间运行的SPA应用程序，我们可能需要定期检查服务器上是否有新版本的应用程序并在后台安装。

为了检查新版本是否可用，我们可以使用该SwUpdate服务及其checkForUpdate()方法。

但一般情况下，checkForUpdate()不需要手动调用，因为Angular Service Worker将ngsw.json在每次完整的应用程序重新加载时查找新版本，以便与标准的Service Worker生命周期保持一致（请参阅此处的详细信息）。

我们可以要求通过使用availableObservable 来获得新版本的通知SWUpdate，然后通过对话框询问用户是否想要获得新版本：

@ 组件（{
  选择器：' app-root '，
  templateUrl：'。/ app.component.html '，
  styleUrls：[ '。/ app.component.css ' ]
}）
export  class  AppComponent   实现 OnInit {

    构造函数（private  swUpdate ： SwUpdate）{
    }

    ngOnInit（）{

        如果（此。swUpdate。的IsEnabled）{

            这个。swUpdate。可用。subscribe（（）=> {

                if（确认（“新版本可用。加载新版本？”））{

                    窗口。位置。reload（）;
                }
            }）;
        }        
    }
}

查看 由GitHub用❤托管的原始06.ts
让我们分解在服务器上部署新应用程序版本时，此代码会发生什么：

现在可以在服务器上使用新文件，例如新的CSS或Js包
ngsw.json服务器上有一个新文件，其中包含有关新应用程序版本的信息：要加载哪些文件，何时加载它们等。
但是当用户重新加载应用程序时，用户仍将看到旧的应用程序版本！

这是正常的，因为用户仍然在浏览器中运行服务工作者，仍然提供来自缓存存储的所有文件，并完全绕过网络。

但是，Angular Service Worker也会调用服务器以查看是否有新的ngsw.json，并ngsw.json在后台触发加载文件中提到的任何新文件。

一旦加载了新应用程序版本的所有文件，Angular Service Worker将发出该available事件，这意味着该应用程序的新版本可用。然后，用户将看到以下内容：

角度服务工作者

如果用户单击“确定”，则将重新加载完整的应用程序，并显示新版本。请注意，如果我们没有向用户显示此对话框，则用户仍会在下次重新加载时看到新版本。

角度服务工作者版本管理摘要
总而言之，以下是Angular Service Worker如何管理新的应用程序版本：

一个新的版本出现，和新的ngsw.json可用
部署新版本后首次应用程序重新加载 - Angular Service Worker检测到ngsw.json新文件并在后台加载任何新文件
新版本部署后的第二次重新加载 - 用户看到新版本
此行为将始终如一地工作，与用户打开的标签数量无关（与标准Service Worker生命周期不同）
有了这个，我们有一个可下载和安装的Angular PWA应用程序，内置版本管理！

我们现在缺少完整的一键式安装体验的最后一件事是要求用户将应用程序安装到其主屏幕。

第7步（共7步） - 使用App Manifest进行一键安装
现在让我们的应用程序一键安装，并注意这是可选的，这意味着我们可以在没有 App Manifest的情况下使用Angular Service Worker 。

另一方面，为了使App Manifest工作，我们需要在页面上运行Service Service！通过提供标准manifest.json文件，将要求用户将应用程序安装到主屏幕。

何时向用户显示“安装到主屏幕”按钮？
这有两个条件可供使用，其中之一是应用程序需要通过HTTPS运行并拥有服务工作者。

此外，只有在满足某些额外条件时才会显示安装到主屏幕的选项。

有一种不断发展的启发式方法将决定是否向用户显示“安装到主屏幕”按钮，或者通常与用户访问网站的次数，频率等有关。

示例应用程序清单文件
为了使这个功能起作用，我们需要首先创建一个manifest.json文件，然后我们将在我们的应用程序的根目录旁边放置index.html：

{
  “ dir ”： “ ltr ”，
  “ lang ”： “ en ”，
  “名字”： “ Angular PWA ”，
  “范围”： “ / ”，
  “显示”： “全屏”，
  “ start_url ”： “ http：// localhost：8080 / ”，
  “ short_name ”： “ Angular PWA ”，
  “ theme_color ”： “透明”，
  “ description ”： “ PWA应用示例”，
  “方向”： “任何”，
  “ background_color ”： “透明”，
  “ related_applications ”：[]，
  “ prefer_related_applications ”： false，
  “图标”：[
    {
      “ src ”： “/ favicon.ico ”，
      “尺寸”： “ 16x16 32x32 ”
    }，
    {
      “ src ”： “/ assets / android-icon-36x36.png ”，
      “尺寸”： “ 36x36 ”，
      “ type ”： “ image / png ”，
      “密度”： “ 0.75 ”
    }，
    {
      “ src ”： “/ assets / android-icon-48x48.png ”，
      “尺寸”： “ 48x48 ”，
      “ type ”： “ image.png ”，
      “密度”： “ 1.0 ”
    }，
    {
      “ src ”： “/ assets / installer -icon-72x72.png ”，
      “尺寸”： “ 72x72 ”，
      “ type ”： “ image / png ”，
      “密度”： “ 1.5 ”
    }，
    {
      “ src ”： “/ assets / installer -icon-96x96.png ”，
      “尺寸”： “ 96x96 ”，
      “ type ”： “ image / png ”，
      “密度”： “ 2.0 ”
    }，
    {
      “ src ”： “/ assets / android-icon-144x144.png ”，
      “尺寸”： “ 144x144 ”，
      “ type ”： “ image / png ”，
      “密度”： “ 3.0 ”
    }，
    {
      “ src ”： “/ assets / android-icon-192x192.png ”，
      “尺寸”： “ 192x192 ”，
      “ type ”： “ image / png ”，
      “密度”： “ 4.0 ”
    }
  ]
}
查看 由GitHub用❤托管的原始08.json
此文件定义主屏幕上安装的图标的外观，并且还定义了几个其他本机UI参数。

在页面加载时链接到App Manifest
一旦我们有了manifest.json，我们需要index.html使用页眉中的链接标记链接到我们的应用程序中：


<！doctype html>
< html  lang = “ en ” >
< 头 >
  < meta  charset = “ utf-8 ” >
  < title > NgPwa </ title >
  < base  href = “ / ” >

  < meta  name = “ viewport ”  content = “ width = device-width，initial-scale = 1 ” >
  < link  rel = “ icon ”  type = “ image / x-icon ”  href = “ favicon.ico ” >

    < link  rel = “ manifest ”  href = “ manifest.json ” >

</ head >
< body >
  < app-root > </ app-root >
</ body >
</ html >

查看GitHub 用❤托管的raw 10.html
设置CLI以包含App Manifest
为了在我们的生产版本中安装App Manifest文件，我们将配置CLI以将此文件与完整资产文件夹一起复制到dist文件夹。

我们可以在文件中配置angular.json：


  “ apps ”：[
    {
      “ root ”： “ src ”，
      “ outDir ”： “ dist ”，
      “资产”：[
        “资产”，
        “ manifest.json ”，
        “ favicon.ico ”
      ]
查看 由GitHub用❤托管的原始09.ts
有了这个，我们现在有一个manifest.json生产文件。但如果我们现在重新加载应用程序，很可能，什么都不会发生！

触发安装到主屏幕
我们的意思是，很可能没有“安装到主屏幕”按钮将显示给用户，这是因为尚未满足向用户显示该按钮的heurestic。

但我们可以使用“ 添加到主屏幕”选项在“清单”选项卡中使用Chrome开发工具触发按钮：

角度服务工作者

正如我们所看到的，在Mac上，按钮的外观仍然处于早期阶段，但这就是按钮在移动设备上的样子：

角度服务工作者

有了这个，我们现在可以为我们的应用程序提供完整的一键式下载和安装体验。

摘要
现在，使用Angular Service Worker和Angular CLI，类似于Native的应用程序下载和安装变得前所未有的简单！

此功能为任何应用程序（包括桌面）带来的性能优势都是巨大的，并且可以以渐进的方式逐步添加到标准应用程序中。

任何应用程序（移动或非移动）都可以从更快的启动时间中受益，并且可以通过Angular CLI提供的一些智能默认设置使该功能开箱即用。

我希望这篇文章有助于开始使用Angular Service Worker并且您喜欢它！如果您想了解有关Angular PWA其他功能的更多信息，请查看完整系列的其他帖子：

服务工作者 - 实用指导（几个例子）
Angular App Shell - 提升应用程序启动性能
Angular Service Worker - 将您的应用程序转换为PWA的循序渐进指南
如果您有任何问题或意见，请在下面的评论中告诉我，我会尽快回复您。要收到即将发布的帖子的通知，我邀请您订阅我们的新闻通讯：

Angular大学
观看25％的Angular视频课程，获得及时的Angular新闻和PDF： 


Email*
 订阅并获得免费课程
相关链接
服务工作者入门

Angular 5.1及更多版本现已推出 - 阅读5.1版本的所有内容

YouTube上提供的视频课程
看看Angular University Youtube频道，我们发布了大约25％到三分之一的视频教程，新视频一直在发布。

订阅获取新视频教程：


Angular上的其他帖子
还要看一下您可能感兴趣的其他热门帖子：

Angular入门 - 开发环境最佳实践使用Yarn，Angular CLI，设置IDE
为什么单页应用程序有哪些好处？什么是SPA？
Angular Smart Components vs Presentation Components: What's the Difference, When to Use Each and Why?
Angular Router - How To Build a Navigation Menu with Bootstrap 4 and Nested Routes
Angular Router - Extended Guided Tour, Avoid Common Pitfalls
Angular Components - The Fundamentals
How to build Angular apps using Observable Data Services - Pitfalls to avoid
Introduction to Angular Forms - Template Driven vs Model Driven
Angular ngFor - Learn all Features including trackBy, why is it not only for Arrays ?
Angular Universal In Practice - How to build SEO Friendly Single Page Apps with Angular
How does Angular Change Detection Really Work ?
Angular Material Data Table: A Complete Example (Server Pagination, Filtering, Sorting)
In this post, we are going to go through a complete example of how to use the Angular Material…

Angular App Shell - 提升应用程序启动性能
影响用户体验（特别是在移动设备上）的最重要因素之一是应用程序启动体验，并且感知...

Angular大学 ©2018与Ghost一起出版
分享到Twitter
分享到Google+
分享到Facebook
，股数61