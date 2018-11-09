菜单
家
订阅
 
Angular App Shell - 提升应用程序启动性能
最近更新时间： 2018年6月16日 local_offer  PWA
影响用户体验（尤其是移动设备）的最重要因素之一是应用程序启动体验和感知性能。事实上，研究表明，53％的移动用户放弃了加载时间超过3秒的网站！

对于所有应用程序而言，这都是正确的，而不仅仅是移动应用程序。任何应用程序都可以从更好的启动体验中受益，特别是如果我们能够开箱即用。

我们可以做的改善用户体验的一个方面是尽快向用户展示一些东西，减少第一次绘画的时间。

获得改善用户体验并向用户快速展示内容的最佳方法是使用App Shell！

什么是App Shell？
为了提高感知的启动性能，我们希望尽快向用户显示上面的折叠内容，这通常意味着显示菜单导航栏，页面的整体骨架，加载指示器和其他页面特定元素。

为此，我们将直接在我们加载index.html单页应用程序时从服务器返回的初始HTTP响应中包含这些元素的HTML和CSS 。

尽可能快地向用户显示的有限数量的简单HTML和样式的组合称为应用程序外壳。

在这篇文章中，我们将学习如何使用Angular CLI将App Shell添加到Angular应用程序！

注意：App Shell功能可独立于Service Worker的使用而提供，我们无需在生产中使用服务器端呈现的Angular Universal应用程序即可从App Shell中受益

目录
以下是我们将在这篇文章中做的事情：我们将从空白文件夹中从头开始构建一个Angular应用程序，我们将添加一个应用程序Shell，它将在构建时使用Angular CLI自动生成。

我们将了解正在发生的事情以及整个App Shell解决方案的工作原理。我们将在以下步骤中执行此操作：

第1步 - 使用Angular CLI对Angular PWA应用程序进行脚手架
第2步 - 检查index.html 之前包括App Shell
第3步 - 在 App Shell 之前分析应用程序启动
第4步 - 支持角度通用应用程序
步骤5 - 使用Angular CLI添加App Shell
第6步 - 在生产模式下生成App Shell
第7步 - 测量App Shell性能改进
这篇文章是正在进行的Angular PWA系列的一部分，这是完整的系列：

服务工作者 - 实用指导（几个例子）
Angular App Shell - 提升应用程序启动性能
Angular Service Worker - 将您的应用程序转换为PWA的循序渐进指南
角度推送通知 - 循序渐进指南
所以，不用多说了，让我们开始使用我们的Angular Application Shell导游！

第1步（共7步） - 使用Angular CLI支持Angular PWA应用程序
通过几个命令，CLI将为我们提供一个带有App Shell的工作应用程序。创建Angular应用程序的第一步是将Angular CLI升级到最新版本：

npm install -g @angular/cli@latest
如果您想尝试最新功能，还可以获得尚未发布的下一个即将推出的版本：

npm install -g @angular/cli@next
有了这个，我们现在可以构建一个Angular应用程序。App Shell必须能够设置Angular路由器，我们马上就会明白原因。

我们可以使用以下命令在新应用程序中包含路由器：

ng new my-app-shell --routing
这将创建一个以my-app-shell新的Angular应用程序命名的新文件夹，其中包括已设置的路由器。

第2步，共7步 - 检查index.html 之前包括App Shell
为了理解App Shell正在解决什么问题，让我们看一下在包含App Shell 之前应用程序是如何工作的。

让我们从生产模式中构建这个初始应用程序开始：

ng build --prod
现在我们在dist文件夹中有生产应用程序。如果我们看一下这个index.html文件，这就是我们所拥有的：

<！doctype html>
< html  lang = “ en ” >
< 头 >
    < meta  charset = “ utf-8 ” >
    < title > MyAppShell </ title >
    < base  href = “ / ” >
    < meta  name = “ viewport ”  content = “ width = device-width，initial-scale = 1 ” >
    < link  rel = “ icon ”  type = “ image / x-icon ”  href = “ favicon.ico ” >
    < link  href = “ styles.d41d8cd98f00b204e980.bundle.css ”  rel = “ stylesheet ” />
</ head >
< body >
< app-root > </ app-root >
< script  type = “ text / javascript ”  src = “ inline.7af73d884e232b8a88bd.bundle.js ” > </ script >
< script  type = “ text / javascript ”  src = “ polyfills.169c804fcec855447ce7.bundle.js ” > </ script >
< script  type = “ text / javascript ”  src = “ main.cd226be56c6c7ccae88d.bundle.js ” > </ script >
</ body >
</ html >
      
      
查看 由GitHub用❤托管的raw 01.html
我们可以看到，此页面是一个空白页面，仅包含以下内容：

应用程序样式
Javascript捆绑
这意味着当首次加载此页面时，用户将看不到任何内容。将有一个初始浏览器绘制，但它不是一个有意义的绘画：页面是空的！

所有内容都将通过Javascript添加到页面中，一切都是动态内容，没有静态内容。让我们通过启动应用程序并使用Chrome开发工具查看正在发生的事情来确认这一点。

第3步（共7步） - 在使用App Shell 之前分析应用程序启动
让我们以生产模式启动应用程序：

ng serve --prod 
然后我们将去localhost:4200测量页面启动性能：

让我们打开开发工具并选择性能选项卡
让我们选中“屏幕截图”复选框
在Performance选项卡中，让我们点击“Start Profiling and Reload Page”按钮
我们一看到页面上的内容就立即停止录制
现在让我们看一下分析结果：

没有app Shell的Angular应用程序

我们可以看到，浏览器正在渲染页面大约1000毫秒（渲染以紫色显示）。第一次尝试大约600毫秒，但问题是没有内容可以显示，所以页面保持空白。

这是Hello World应用程序的最佳案例场景，因为典型的SPA将在晚些时候呈现第一个结果！

让我们看看我们如何改进这一点。

如何改善页面启动时间？
改进事物的唯一方法是在机构中提供更多的HTML和CSS index.html。这是因为在页面加载的早期阶段，Angular尚未运行，事实上，Angular捆绑包仍在下载！

为此，我们希望至少采用app.component.ts主要应用程序组件HTML和CSS输出的内容，并将其移至index.html。这应该包括页面的主要骨架，包括导航系统。

但是如果我们查看组件的模板，我们会看到它有一个路由器插座：

< div  style = “ text-align：center ” >
  < h1 >
    欢迎 到 {{ 标题 }} ！
  < / h1 >
  < img  width = “ 300 ”  alt = “ Angular Logo ” >
< / div >
< H2 > 这里 有 一些 链接 ，以 帮助 您 开始：</ H2 >
< ul >
     ..   主 导航 菜单 中 的 应用 ...
< / ul >

< router - outlet > </ router - outlet >

  
查看 由GitHub用❤托管的原始02.ts
所以我们需要做的是预渲染这个组件，并获得App Shell的HTML和CSS输出，但是我们需要指定我们想要代替路由器插座的内容。

App Shell和Angular Universal之间有什么关系？
我们将使用Angular Universal 在构建时预渲染主要组件，并使用我们的预渲染输出index.html。

但是，代替路由器插座，我们可能希望放置比/家庭路线的完整内容更轻的东西，因为这可能包含太多的HTML和CSS。

相反，我们可能只想显示页面的加载指示符或简化版本而不是整个主页路径，而不是路由器插座。

最简单的方法是在我们的应用程序中创建一个辅助路径，例如在路径中/app-shell-path。然后我们需要预先渲染该路线的完整内容并将其包含在我们的中index.html，我们有我们的App Shell！

为了在Angular中进行预渲染，我们需要Angular Universal。然后让我们构建一个Angular Universal应用程序，它包含与客户端单页面应用程序相同的组件。

第4步（共7步） - 支持角度通用应用程序
我们可以通过运行以下Angular CLI命令为我们的应用程序添加预渲染功能：

ng generate universal ngu-app-shell --client-project <project name>
我们可以在angular.jsonCLI配置文件中找到客户端项目名称。让我们记住，现在CLI应用程序可以包含多个客户端项目，因此我们需要识别正确的项目。

这是命令输出：

CREATE src/main.server.ts (220 bytes)
CREATE src/app/app.server.module.ts (318 bytes)
CREATE src/tsconfig.server.json (245 bytes)
UPDATE package.json (1353 bytes)
UPDATE angular.json (3677 bytes)
UPDATE src/main.ts (430 bytes)
UPDATE src/app/app.module.ts (359 bytes)
added 3 packages and removed 3 packages in 10.619s
我们可以看到，此命令在Angular CLI angular.json配置文件中添加了一个新的构建配置条目，引入了一个名为的新应用程序ngu-app-shell。

我们如何使用Angular Universal应用程序？
这意味着现在我们可以使用renderModuleFactory预呈现我们的应用程序。预渲染可以以多种方式使用，例如：

我们可以在后端节点服务器（如Express ）中使用预呈现（请参阅说明），以便将服务器端完全呈现的路由直接提供给浏览器。
然后，Angular将自行引导并将该页面作为普通SPA接管
或者我们可以从命令行工具调用预呈现，并构建一个页面的纯HTML版本，然后我们从像Amazon Cloudfront这样的CDN上传和服务器
在我们的例子中，我们将通过预渲染App Shell的HTML和CSS，将预渲染用作命令行工具。

第5步（共7步） - 使用Angular CLI添加App Shell
我们可以使用以下命令将App Shell添加到我们的应用程序中：

ng generate app-shell my-loading-shell 
    --universal-project=ngu-app-shell 
    --route=app-shell-path 
    --client-project=<project name>
让我们分解这个命令来看看发生了什么：

我们正在使用ng generate并为其命名一个App Shell
我们正在通过--universal-project我们想要使用的Angular Universal应用程序进行配置，从可用的多个选项中进行预渲染angular.json
我们正在配置我们想要使用该--route选项完全预渲染的路由，因为我们的应用程序可以配置许多路由，并且/归属路由不一定是一个好的默认路由。
什么是ng generate app-shell命令吗？
让我们看一下命令输出：

CREATE src/app/app-shell/app-shell.component.css (0 bytes)
CREATE src/app/app-shell/app-shell.component.html (28 bytes)
CREATE src/app/app-shell/app-shell.component.spec.ts (643 bytes)
CREATE src/app/app-shell/app-shell.component.ts (280 bytes)
UPDATE angular.json (3940 bytes)
UPDATE src/app/app.module.ts (425 bytes)
UPDATE src/app/app.server.module.ts (599 bytes
我们可以看到，我们刚刚创建了一个名为的新组件app-shell！然后将该组件链接到/app-shell-path路径，但仅限于Angular Universal应用程序。

此/app-shell-path特殊路由只是用于生成App Shell的内部Angular CLI机制，应用程序用户将无法导航到此路由。在这种情况下，此路由仅是构建时辅助构造。

以下是仅在app.server.module.ts文件中添加的路由配置（而不是在主文件中app.module.ts）：


const routes： Routes  = [{path： ' app-shell-path '，component： AppShellComponent }];

@ NgModule（{
  进口：[
    AppModule，
    ServerModule，
    RouterModule。forRoot（路线），
  ]
  bootstrap：[ AppComponent ]，
  声明：[ AppShellComponent ]，
}）
导出 类 AppServerModule {}

查看 由GitHub用❤托管的原始06.ts
我们可以看到，/app-shell-path路由已链接到AppShellComponent，将添加该路径以代替router-outlet标记。这AppShellComponent是一个普通的支架角度组件，就像我们使用的任何组件一样ng generate。

我们可以对其进行编辑，以包含我们希望在App Shell正文中显示的内容。以下是使用加载指示符的示例：


@ 组件（{
    选择器：' app-app-shell '，
    模板：`      
      <img class =“loading-indicator”src =“loading.gif”>
  `，
    风格：[ `
      .loading-indicator {
          身高：300px;
          保证金：0自动;
      }
  ` ]
}）
export  class  AppShellComponent {
    
}

查看 由GitHub用❤托管的原始03.ts
除了配置App shell路由和组件外，我们在angular.json文件中还有一些新的配置：


“ app-shell ”：{
  “ builder ”： “ @ angular-devkit / build-angular：app-shell ”，
  “选项”：{
   “ browserTarget ”： “ my-app-name：build ”，
   “ serverTarget ”： “ my-app-name：server ”，
   “ route ”： “ app-shell-path ”          
  }
}

查看 由GitHub用❤托管的原始04.ts
正如我们所看到的，我们已经在生产Angular应用程序的构建配置中添加了一些配置：

app-shell-path使用名为Angular Universal的应用程序预渲染路径ngu-app-shell，并将其用作App Shell

所以一切都准备就绪，然后让我们构建我们的应用程序，看看它的运行情况并衡量性能改进。

第6步，共7步 - 在生产模式下生成App Shell
现在让我们运行app shell build！假设您的项目已命名app-shell-test，这是在angular.json文件顶部指定的值。

我们现在可以通过运行以下命令来构建App Shell：

ng run app-shell-test:app-shell 
这一次，文件夹中index.html生成的内容dist看起来有很大不同。我们来看一下：

<！DOCTYPE html>
< html  lang = “ en ” >
< 头 >
    < meta  charset = “ utf-8 ” >
    < title > MyAppShell </ title >
    < base  href = “ / ” >
    < meta  name = “ viewport ”  content = “ width = device-width，initial-scale = 1 ” >
    < link  rel = “ icon ”  type = “ image / x-icon ”  href = “ favicon.ico ” >
    < link  href = “ https://fonts.googleapis.com/icon?family=Material+Icons ”  rel = “ stylesheet ” >
    < link  href = “ styles.d41d8cd98f00b204e980.bundle.css ”  rel = “ stylesheet ” >
    < style  ng-transition = “ serverApp ” > </ style >
    < style  ng-transition = “ serverApp ” > .loading-indicator [ _ngcontent-c1 ] {
        身高： 300 像素 ;
        保证金： 0  自动 ;
    } </ style >
</ head >
< body >
< app-root  _nghost-c0 = “ ”  ng-version = “ 5.1.0 ” >
    < div  _ngcontent-c0 = “ ”  style = “ text-align：center ” >
        < h1  _ngcontent-c0 = “ ” >欢迎使用app！</ h1 >
        < img  _ngcontent-c0 = “ ”  alt = “ Angular Logo ”  src = “ .. ”  width = “ 300 ” >
    </ div >
    < h2  _ngcontent-c0 = “ ” >以下是一些帮助您入门的链接：</ h2 >
    < ul  _ngcontent-c0 = “ ” >
        < li  _ngcontent-c0 = “ ” >
            < H2  _ngcontent-C0 = “ ” > < 一个 _ngcontent-C0 = “ ”  HREF = “ https://angular.io/tutorial ” > Tourof英雄</ 一 > </ H2 >
        </ li >
        < li  _ngcontent-c0 = “ ” >
            < H2  _ngcontent-C0 = “ ” > < 一个 _ngcontent-C0 = “ ”  HREF = “ https://github.com/angular/angular-cli/wiki ” > CLI文档</ 一 > </ H2 >
        </ li >
        < li  _ngcontent-c0 = “ ” >
            < H2  _ngcontent-C0 = “ ” > < 一个 _ngcontent-C0 = “ ”  HREF = “ https://blog.angular.io/ ” >角博客</ 一 > </ H2 >
        </ li >
    </ ul >

    < router-outlet  _ngcontent-c0 = “ ” > </ router-outlet >
    
    < app-app-shell  _nghost-c1 = “ ” >
        < img  _ngcontent-c1 = “ ”  class = “ loading-indicator ”  src = “ loading.gif ” >
    </ app-app-shell >
</ app-root >
< script  type = “ text / javascript ”  src = “ inline.7f492b32ad91aff5b9d4.bundle.js ” > </ script >
< script  type = “ text / javascript ”  src = “ polyfills.169c804fcec855447ce7.bundle.js ” > </ script >
< script  type = “ text / javascript ”  src = “ main.4b438877429c33fe644e.bundle.js ” > </ script >
</ body >
</ html >

查看 由GitHub用❤托管的raw 07.html
我们可以看到，这不再是一个空白页面。它的样式AppShellComponent在页面中内嵌（像往常一样），导航菜单的HTML和加载指示器也出现在页面上。

那么这里发生了什么？Angular CLI已经获得了预呈现App shell路由的输出，并在index.html文件中添加了HTML输出。

所以看起来一切正常，我们准备好使用App Shell了！

第7步（共7步） - 测量使用App Shell获得的性能改进
现在让我们在生产模式下运行我们的应用程序并查看结果。我们可以通过运行来运行与生产接近的构建：

ng serve --prod
我们也可以执行以下操作，让我们进入目录并使用简单的HTTP服务器运行应用程序：

npm install -g http-server
cd dist
http-server -c-1 .
App Shell性能结果
在服务器运行的情况下，让我们前往localhost:8080并进行一些分析。让我们看一下用户可以看到app shell的时间：

没有app Shell的Angular应用程序

第一次涂漆的时间大大缩短了
正如我们所看到的，在这种特殊情况下，App Shell在大约660ms处可见，这代表了对完整SPA的第一次绘制的典型时间的巨大改进，这可能是几秒钟！

即使在这个Hello World示例的情况下，我们也有时间进行第一次绘制，这几乎是初始时间的一半，所以想象一下成熟SPA中的收益。

这可以通过以下几种方式进一步改进：

通过使用内联的Base 64映像作为加载指示器而不是外部映像，避免了加载映像所需的额外HTTP请求
通过移动甚至将某些样式从外部样式表复制到App Shell等。
每个应用程序都需要根据我们需要向用户显示的内容单独进行优化，而App Shell机制为我们提供了基础，可以实现我们正在寻找的超快速感知启动时间。

摘要
Angular CLI中的内置App Shell机制对于任何开箱即用的应用程序（不仅仅是移动设备）都是非常有益的性能改进。

从用户的角度来看，即使实际上应用程序仍在加载并从后端获取数据，大约半秒的第一次绘制时间感觉几乎是即时的。

第一次绘制的确切时间取决于每个应用程序，App Shell功能为我们提供了尽可能低的所需工具。

尽管此App shell机制通常与PWA相关联，但是不需要PWA从App Shell Angular CLI功能中受益，因为这两个渐进式改进可以单独配置。

我希望这篇文章有助于开始使用Angular App Shell并且您喜欢它！如果您想了解有关Angular PWA其他功能的更多信息，请查看完整系列的其他帖子：

服务工作者 - 实用指导（几个例子）
Angular App Shell - 提升应用程序启动性能
Angular Service Worker - 将您的应用程序转换为PWA的循序渐进指南
如果您有任何问题或意见，请在下面的评论中告诉我，我会尽快回复您。

要收到即将发布的帖子的通知，我邀请您订阅我们的新闻通讯：

Angular大学
观看25％的Angular视频课程，获得及时的Angular新闻和PDF： 


Email*
 订阅并获得免费课程
相关链接
Angular 5.1及更多版本现已推出 - 阅读5.1版本的所有内容

YouTube上提供的视频课程
看看Angular University Youtube频道，我们发布了大约25％到三分之一的视频教程，新视频一直在发布。

订阅获取新视频教程：


Angular上的其他帖子
还要看一下您可能感兴趣的其他热门帖子：

Angular入门 - 开发环境最佳实践使用Yarn，Angular CLI，设置IDE
为什么单页应用程序有哪些好处？什么是SPA？
角度智能组件与演示组件：有什么区别，何时使用以及为什么？
角度路由器 - 如何使用Bootstrap 4和嵌套路由构建导航菜单
角度路由器 - 扩展导览，避免常见陷阱
角度组件 - 基础知识
如何使用Observable Data Services构建Angular应用程序 - 要避免的陷阱
Angular Forms简介 - 模板驱动与模型驱动
Angular ngFor - 了解所有功能，包括trackBy，为什么它不仅适用于阵列？
角度通用实践 - 如何使用Angular构建SEO友好的单页应用程序
角度变化检测如何真正起作用？
Angular Service Worker - 将您的应用程序转换为PWA的循序渐进指南
通过Angular Service Worker和Angular CLI内置的PWA支持，现在比以往更简单了...

带有ngIf和异步管道的角度反应模板
ngIf模板语法在许多常见用例中非常有用，例如使用else子句......

Angular大学 ©2018与Ghost一起出版
分享到Twitter
分享到Google+
分享到Facebook
，股数69