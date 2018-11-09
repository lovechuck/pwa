菜单
家
订阅
 
服务工作者 - 实用指导（几个例子）
最近更新时间： 2018年6月16日 local_offer  PWA
在这篇文章中，我们将通过关注其中一个最重要的用例：应用程序下载和安装（包括应用程序版本控制）来进行实用的服务工作导览。

作为一个学习练习，我邀请您编写代码，并通过使其可下载和安装将您的应用程序转换为PWA！我们将对此存储库中提供的示例应用程序执行相同操作。

如果您之前尝试过学习服务工作者，您可能已经注意到服务工作者和服务工作者生命周期的许多功能乍一看似乎有点令人惊讶。

为什么我们需要一个单独的守护进程实例来拦截我们自己的应用程序的HTTP请求，在那里我们不能真正进行长时间运行的计算或访问DOM？

然而，服务工作者是渐进式Web应用程序的基石，它们是将所有其他PWA API绑定在一起的关键组件，并支持类似本机的功能，例如：

离线支持
应用程序下载，安装和版本控制
背景同步
通知
物理设备交互（Web蓝牙）
付款（通过付款请求API）
PWA真的起飞了吗？
拥有所有这些本机功能，PWA就在这里！以下是为什么现在是学习它们的最佳时间的一些原因：

我们已经拥有Chrome PWA支持，意味着超过50％的浏览器份额，包括移动设备
Apple已同意在Safari中实施Service Workers
Microsoft正在努力允许将PWA直接安装到Windows 10桌面，并在本机窗口中运行每个PWA
我们将在这篇文章中做些什么
然后让我们通过一个关键用例的A到Z实现来开始学习PWA：应用程序下载，安装和应用程序版本管理！

我们将从直接使用浏览器API的第一原则开始，并使用Chrome PWA开发工具显示每个步骤中的内容。

请注意，我们将构建此Service Worker 以用于学习目的，因为在生产中Service Worker 是由构建工具（如Angular CLI或WorkBox）自动生成的。

即使使用这些强大的工具，我们仍然需要知道服务工作者如何在幕后工作，以便能够：

选择合适的PWA工具
了解每个工具的范围
了解PWA工具文档
排除错误情况
设计完整的PWA解决方案
目录
在这篇文章中，我们将讨论以下主题：

什么是服务工作者？
简洁的应用程序下载，安装和版本控制
第1步 - 服务工作者注册
第2步 - 服务工作者Hello World安装阶段
缓存存储API
后台应用下载
服务工作者生命周期（默认为一致性）
第3步 - 服务工作者激活阶段
第4步 - 拦截HTTP请求
第5步 - 清除以前的应用程序版本
步骤6 - 使用缓存然后网络策略从缓存提供应用程序
自定义服务工作者生命周期
接管当前页面 clients.claim()
跳过等待阶段（以及可能导致的潜在问题）
手动更新服务工作者
针对破碎的服务工作者的内置浏览器保护
使用浏览器缓存和服务工作者的注意事项
结论
这篇文章是正在进行的Angular PWA系列的一部分，这是完整的系列：

服务工作者 - 实用指导（几个例子）
Angular App Shell - 提升应用程序启动性能
Angular Service Worker - 将您的应用程序转换为PWA的循序渐进指南
角度推送通知 - 循序渐进指南
让我们开始深入了解我们的Service Worker Fundamentals吧！

什么是服务工作者？
Service Worker就像后台守护程序进程，它位于我们的Web应用程序和网络之间，拦截应用程序发出的所有HTTP请求。

Service Worker无权访问DOM。实际上，相同的Service Worker实例在同一应用程序的多个选项卡之间共享，并且可以拦截来自所有这些选项卡的请求。

请注意，出于安全原因，Service Worker无法查看在同一浏览器中运行的其他Web应用程序发出的请求，并且只能通过HTTPS工作（在localhost上除外，用于开发目的）。

总结：Service Worker是一个网络代理，在浏览器内部运行！

服务人员概述
我们会定期从我们的网站下载服务工作者的代码，并且有一个完整的生命周期管理流程。

它的浏览器在任何时候都会决定服务工作者是否应该运行，这样可以节省资源，特别是在移动设备上。

因此，如果我们暂时没有做任何HTTP请求或没有收到任何通知，浏览器可能会关闭服务工作者。

如果我们确实触发了应由Service Worker处理的HTTP请求，则浏览器将再次激活它，以防它尚未运行。因此，看到服务工作者在开发工具中停止并不一定意味着某些东西被打破了。

服务工作者可以拦截我们为给定域和Url路径打开的所有浏览器选项卡发出的HTTP请求（该路径称为服务工作者范围）。

另一方面，它无法访问任何浏览器选项卡的DOM，但它可以访问浏览器API，例如Cache Storage API。

服务工作者用例：应用程序下载，安装和版本控制
您可能在想这一点，网络代理与应用程序下载和安装以及离线支持有什么关系？

该服务人员是有生命周期的安装网络代理，但它是由我们来用它来实现类似天然的PWA功能：服务工作者本身并不能提供这些功能。

那么让我们看看我们如何设计一个基于Service Worker的解决方案，该解决方案将实现后台下载和安装用例。

下载和安装设计细分
以下是我们即将实施的设计摘要：

我们将从服务器下载Service Worker脚本
我们将确保浏览器在应用程序引导时间内尽可能晚地在后台安装和激活服务工作者，以免破坏初始用户体验
在后台，服务工作者将下载整个Web应用程序（意思是HTML，CSS和Javascript），版本并保存以供日后使用
只有在用户下次访问该站点时，服务工作人员才会启动（稍后会详细介绍）
第二次用户访问该站点时，该应用程序将不会从网络下载HTML，CSS和Javascript - 服务工作者将提供它以后保留的缓存文件
这第二次，应用程序启动时会多快
即使网络已关闭，用户也至少会拥有一个正在运行的应用程序
这就是在浏览器中使用网络代理允许我们安装可安装的Web应用程序的方法！这与后退和刷新按钮完全兼容。

然后让我们开始实现这个设计：首先我们需要一个示例应用程序。

第1步 - 服务工作者注册
我们的出发点是一个简单的HTML，CSS和Javascript Bootstrap页面，它使用了一些非常常见的CSS和Javascript包。

我们将这个简单的页面转换为后台可下载和可安装的PWA，同样的设计适用于单个页面应用程序：毕竟它只是HTML，CSS和Javascript！

提醒：示例应用程序的代码可以在这里在Github上

将此标准网站转换为可下载的PWA的第一步是通过注册脚本添加服务工作者：


    <！ -常用的JS包- >
    < script  src = “ https://code.jquery.com/jquery-3.2.1.slim.min.js ” > </ script >
    < script  src = “ http://getbootstrap.com/dist/js/bootstrap.min.js ” > </ script >
    ...

    <！ -注册服务工作者- >
    < script  src = “ sw-register.js ” > </ script >

查看 由GitHub用❤托管的raw 01.html
注意脚本sw-register.js，它将触发我们的网络代理服务工作者的安装。我们来看看这个注册脚本：


如果（' serviceWorker ' 在 导航）{
    窗口。addEventListener（' load '，（）=> {
        导航员。serviceWorker。注册（' /sw.js '，{
            范围：' / '
        }）
        。然后（注册 => {
            控制台。log（“服务工作者注册完成...... ”）;
        }）;
    }）;
}

查看 由GitHub用❤托管的原始02.ts
让我们逐行打破注册过程，看看它意味着什么：

首先，我们通过serviceWorker在全局navigator对象中查找属性来检查浏览器是否支持Service Workers
如果浏览器不支持SW，那么一切都会工作，只是后台不会安装，所以我们回到正常的Web应用场景
什么时候应该注册服务工作者？
即使我们检测到浏览器确实支持Service Workers，我们仍然不会立即注册SW！在这种情况下，我们正在等待页面load事件。

该load事件仅在加载整个页面时触发，包括其链接的资源，如图像，CSS和Javascript，这可能需要很长时间。

为什么要延迟服务工作者的注册？
我们希望延迟服务工作者的注册有几个原因：我们希望避免在应用程序第一次加载时导致初始用户体验中断。

浏览器只能同时执行有限数量的HTTP请求，并且只有很多网络容量。服务工作者可能会也可能不会执行单独的网络请求，这些请求可能会干扰向用户显示初始内容所需的网络请求。

这意味着延迟服务工作者注册会阻止服务工作者降低初始用户体验。相反，服务工作者将等待应用程序启动，然后它将在后台安装。

请注意，对于单页面应用程序，我们可能希望进一步延迟注册，并等待load事件。

关键是要了解在服务工作者进行下载和安装的情况下，我们希望尽可能晚地注册它，以避免降低用户体验。

服务工作者和默认的一致性
延迟此类服务工作者注册的另一个原因是具有一致的应用程序行为。让我们记住，服务工作者经常会为整个应用程序服务！

所以我们想避免以下情况：

一些页面CSS和JS资源由服务工作者提供服务
而其他人来自网络
如果某个页面的初始请求来自网络，我们希望确保所有剩余的捆绑包也从网络加载，以保持一致性。

避免不一致的应用场景
在应用程序下载和安装的情况下，我们希望避免陷入在页面启动过程中激活服务工作者的情况。

这是因为根据时间条件，我们可能会意外地陷入一些难以重现的情况，即由于HTML / CSS / JS工件的不可预测的组合而导致页面被破坏，一些来自网络而另一些来自某种缓存，服务工作者正在使用。

在我们下次访问此页面时，Service Worker将处于活动状态，然后我们将从Service Worker而不是网络加载所有资源。

这意味着我们将再次拥有一组一致的捆绑包，所有捆绑包都来自缓存并且对应于给定版本的应用程序。

注册时会发生什么？
在上面的示例中，当load事件触发时，我们将调用register()并将该文件标识sw.js为Service Worker脚本。

然后浏览器将下载该sw.js文件，并通过创建此文件中包含的所有字节的快照来对其进行版本化。将来，即使一个数字发生变化，浏览器也会认为有一个全新版本的服务工作者。

什么是服务工人scope财产？
该scope属性决定了HTTP请求的集可以由服务人员截获，或者不是。在这种情况下，范围是'/'，这意味着我们的服务工作者将能够拦截此应用程序发出的所有 HTTP请求。

如果范围是相反的/api，那么服务工作者将无法拦截例如的请求/bundles/app.css，但它仍然能够拦截REST API请求，例如/api/courses。

多个服务工作者在同一页面？服务工作者ID
这意味着它可能有多个Service Worker在同一页面上运行，但在不同的范围内运行！

如果服务工作者具有唯一标识符，则它将是原始域和范围路径的组合。

这就是浏览器如何确定两个不同的脚本是否对应同一个Service Worker的两个不同版本（而不是基于保持相同的SW sw.js文件名）。

如果两个Service Worker脚本具有相同的作用域路径甚至是一个字节的差异，则浏览器将考虑它们是同一SW的两个版本并在后台安装最新版本。

我可以将Service Worker放在任何文件夹中吗？
sw.js文件的位置很重要：如果此文件放在文件夹中/service-worker/sw.js，那么它将无法拦截像/bundles/app.css或等的请求/api/courses。

相反，服务工作者可以拦截的最大HTTP请求范围是任何/service-worker以脚本所在的文件夹开头的请求！

有鉴于此，我们可以为不同的范围注册不同的服务工作者：一个服务工作者用于所有/bundles请求，另一个服务工作者用于所有/api请求。

我们可以看到，有很大的灵活性！现在，为了实现下载和安装，我们将使用根/范围并仅使用一个Service Worker。

第2步 - 服务工作者Hello World
当浏览器为给定范围识别新版本的Service Worker时，它将触发安装阶段，从而导致install生命周期事件的发生。

请注意，Service Worker规范未定义安装阶段的确切操作。这取决于我们通过聆听install事件来实现这一点sw.js。

安装完成后激活，然后网络拦截就可以使用了！基于此Hello World Service Worker sw.js示例，让我们准确了解安装和激活阶段的工作原理：


const VERSION =  ' v1 ' ;

自我。addEventListener（' install '，event  => {
    log（“ INSTALLING ”）;
    const installCompleted =  Promise。resolve（）
                        。然后（（）=>  log（“ INSTALLED ”））;

    事件。waitUntil（installCompleted）;
}）;

自我。addEventListener（' activate '，event  => {
    log（“ ACTIVATING ”）;
    const activationCompleted =  承诺。resolve（）
        。然后（（activationCompleted）=>  log（“ ACTIVATED ”））;

    事件。waitUntil（activationCompleted）;
}）;

//处理服务工作者安装
自我。addEventListener（' fetch '，event  => {
    日志（“ HTTP调用截获- ”  +  事件。请求。网址）;
    回归 事件。respondWith（取（事件。请求。URL））;
}）;


//每个日志记录行都将在服务工作者版本之前添加
功能日志（消息）{
    控制台。log（VERSION，message）;
}

查看 由GitHub用❤托管的原始03.ts
HTTP日志记录拦截器
这段代码实际上是一个简单的日志记录HTTP拦截器的实现，我们将进一步发展它以实现应用程序下载和安装！

现在，让我们分解这个最初的Hello World示例，看看这里发生了什么：

我们使用的引用self：这意味着代码运行的当前全局上下文，例如，window它将在应用程序级别运行
但是，在这种情况下，self指向Service Worker全局上下文
我们正在订阅install和activate事件，并将它们的出现记录到控制台
每个日志记录语句都附加了Service Worker的版本，这将有助于我们了解多个版本的工作原理
安装和激活步骤都传递了一个Promise waitUntil()，现在这只是为了展示我们将如何在这些阶段进行异步操作
如果传递的promise waitUntil()成功解析，则安装/激活阶段成功完成
另一方面，如果承诺被拒绝，则安装/激活阶段失败，并且不会触发下一阶段
我们也订阅了这个fetch活动。使用它，我们拦截应用程序发出的所有HTTP请求
fetch事件有一个名为的方法respondWith()，它也作为一个参数
我们传递它的承诺需要返回（解析时）对HTTP请求的响应
安装和激活阶段中的异步操作
我们可以看到，与几乎所有与PWA相关的API一样，这些生命周期阶段的Service Worker API是基于Promise的。在这些阶段，我们可以执行异步操作，例如从网络获取资源。

为了将阶段标记为已完成，我们返回一个Promise，在解析后将成功将阶段标记为已完成。在这种情况下，安装和激活阶段都会返回成功解决的Promise，因此应用程序现在可以开始拦截网络调用了。

使用fetch事件拦截HTTP请求
现在让我们仔细看看fetch事件的回调，它包含HTTP日志记录功能。

正如我们所看到的，这个fetch回调将返回使用的HTTP调用的实际响应respondWith()，并且可以通过将Promise传递给异步来异步计算响应respondWith()。

注意：应用程序代码将不知道此响应来自何处：如果来自网络或来自服务工作者

我们可以respondWith()从任何地方传递响应，例如：

我们可以将呼叫转发到网络并发回网络响应
或者我们可以从缓存存储中检索响应
我们甚至可以Response()手动构建对象
在这种情况下，我们正在做的是：

我们正在记录截获的请求的URL
然后我们使用Fetch API将HTTP请求转发到网络
fetch() 将返回一个Promise，如果已解决将传递网络响应，或在发生致命网络错误时失败
请注意，fetch()如果网络出现故障或者出现DNS错误等其他致命情况，则只会抛出错误。例如，HTTP状态代码500内部服务器错误不会导致获取承诺错误
然后我们传递fetch()将发出网络响应的承诺respondWith()
查看正在运行的Hello World Service Worker
传递给它的响应respondWith()然后将传递给应用程序！我们可以看到，此Service Worker充当日志记录代理。

从应用程序的角度来看，服务工作者提供的响应与服务工作者不存在时的调用无法区分，唯一的副作用是在控制台中进行日志记录。

然后让我们检查控制台输出：

v1 INSTALLING 
v1 INSTALLED
v1 ACTIVATING
v1 ACTIVATED
Service Worker registration completed ...
这是我们的服务工作者在Chrome开发工具（应用程序选项卡）中运行：

服务工作者v1

在编写此帖子的同时，最好将“重新加载时更新”选项设置为关闭，以便更好地了解服务工作者生命周期

这个初始日志记录示例是我们需要详细了解服务工作者生命周期的所有内容。

为什么服务工作者不立即活动？
您可能已经注意到一件事：虽然我们正在记录安装和激活事件，但是没有HTTP请求记录到控制台，这意味着fetch事件似乎不起作用！

就像fetch日志拦截器无法正常工作一样，即使服务工作者处于活动状态。

但是，如果我们打开另一个选项卡，或刷新相同的选项卡，我们拥有的是：

v1 HTTP call intercepted - getbootstrap.com/dist/css/bootstrap.min.css
v1 HTTP call intercepted - localhost:8080/carousel.css
v1 HTTP call intercepted - code.jquery.com/jquery-3.2.1.slim.min.js
v1 HTTP call intercepted - getbootstrap.com/js/vendor/popper.min.js
v1 HTTP call intercepted - getbootstrap.com/dist/js/bootstrap.min.js
 ... other intercepted CSS/Js bundles
v1 HTTP call intercepted - localhost:8080/sw-register.js
Service Worker registration completed ...
所以看起来服务工作者只有在重新加载页面后才开始拦截HTTP请求。这是我们第一次看到它时有点令人惊讶，但默认情况下会发生这种情况以确保一致性。

服务工作者生命周期和默认的一致性
我们在这里看到的服务工作者行为，虽然起初令人惊讶，但它实际上是一个经过深思熟虑的伟大功能。

在所有这些场景中：初始页面加载和Service Worker激活，打开新选项卡或刷新原始选项卡，所有场景都会发生一些事情：

页面的所有HTTP请求都是由服务工作者提供的，或者根本没有！这就是这里发生的事情：

我们第一次加载页面时，服务工作者没有提供任何请求
但是当第一次刷新发生时，或者我们打开了一个新选项卡时，所有请求都由服务工作者提供
这确保了一致性：页面的一个版本，服务工作者的一个版本。这避免了整个类的一些非常难以解决错误情况。

服务工作者如何与浏览器选项卡交互？
现在让我们模拟一些正常的用户行为。如果我们打开同一个应用程序的其他浏览器标签会怎样？

v1 HTTP call intercepted - getbootstrap.com/dist/css/bootstrap.min.css
 ... the same HTTP requests, all served by version 1
Service Worker registration completed ...
我们将看到此页面由完全相同的SW版本1提供服务！请注意，控制台日志记录在选项卡之间共享，这可能相当令人惊讶。

如果刷新申请了几次，然后再切换回你会看到所做的记录的HTTP请求另一个选项卡中的其他选项卡。

这实际上是预期的，因为我们有相同的服务工作者拦截来自所有选项卡的请求。

服务工作者版本控制在行动
为了进一步了解服务工作者生命周期，现在让我们看看如果我们修改Service Worker代码中的内容会发生什么。例如，让我们将版本号修改为v2。

请注意，我们不需要更改文件名sw.js来通知浏览器新版本的Service worker可用。

浏览器将看到两个版本都链接到范围/，如果两个版本之间甚至存在一个差异，则浏览器将安装新版本。

然后让我们尝试安装v2，仍然打开多个标签。如果我们将SW脚本的版本号更改为v2并打开另一个选项卡，我们在开发工具中看到的是：

服务工作者v1

我们可以看到，服务工作者的新版本没有立即应用，它处于某种等待状态！

如果我们看一下控制台，我们现在有：

v1 HTTP call intercepted - getbootstrap.com/dist/css/bootstrap.min.css
v1 HTTP call intercepted - localhost:8080/carousel.css
 ... the same requests as before still being intercepted by v1
Service Worker registration completed ...
v2 INSTALLING 
v2 INSTALLED
在这个日志中有几件非常有趣的事情：

版本v1 未再次安装，甚至未激活
看起来版本v1在整个刷新过程中保持活动状态，因为它一直在拦截HTTP请求
所有请求仍然被v1拦截
版本v2 已在后台安装，但未激活！
版本v2现在处于等待状态
这里有几个重要问题浮现在脑海中：

为什么新版本v2已安装但未激活？
一个原因是我们打开了多个标签，我们希望向用户展示一致的体验。如果用户打开了运行同一应用程序的不同版本的两个选项卡，那将会很困惑。

而且由于服务工作者拦截和修改HTTP请求，服务工作者的两个不同版本可能意味着应用程序本身的两个不同版本！

那么浏览器将如何处理在/作用域上运行的新版Service Service ？

浏览器将在v2的安装阶段继续执行任何安装操作，如下载包或脱机页面，但只要打开了多个仍在运行v1的选项卡，浏览器就不会激活 v2。

默认情况下，这种一致性是服务工作者生命周期的关键设计目标！

现在，在继续探索生命周期之前，请快速了解浏览器硬刷新和服务工作者。

服务人员和硬刷新
如果在尝试服务工作者时有些不清楚，尝试进行硬刷新（Ctrl + Shift + R）将无助于学习过程。

这是因为如果你点击硬刷新，整个服务工作者将被绕过，它将无法控制页面 - 这是标准的浏览器行为，不太可能改变。

Ctrl + Shift + R意味着绕过所有网络缓存，并且因为Service Worker经常用于缓存，所以它也会绕过它。

有了这个重要的注意事项，让我们继续深入了解服务工作者生命周期的工作原理，以及它如何实现应用程序下载和安装。

让我们理解为什么在这个阶段已经安装了v2 为什么v1仍在运行，以及为什么v2还没有活动。

为什么即使只打开一个标签，新的SW版本也不会变为活动状态？
我们确实刷新了运行v1的单个选项卡，但仍未激活v2：v2 在后台安装，但未激活。

这是因为，从浏览器的角度来看，当前页面保持活动状态，直到刷新完成，然后当我们至少从服务器接收到响应头时，页面才会被换出。

并且因为页面是在刷新过程的一部分期间保留的，所以确保一致性的唯一方法是在整个过程中始终保持活动状态。

之后，因为我们在刷新期间保持Service worker v1处于活动状态，所以默认情况下我们希望在刷新完成后保持它运行，这解释了为什么V1在页面刷新完成后仍处于活动状态。

那么如何激活新的Service Worker版本V2呢？
一种方法是使用skipWaitingDevTools中的选项，但不要这样做！让我们重现正常的用户体验：让我们关闭运行service worker v1的所有选项卡，然后打开一个新选项卡。

如果我们查看控制台输出，我们现在有：

v2 ACTIVATING
v2 ACTIVATED
v2 HTTP call intercepted - localhost:8080
v2 HTTP call intercepted - getbootstrap.com/dist/css/bootstrap.min.css
... the same list of requests, all intercepted by v2
我们可以看到，这次浏览器激活了之前在后台安装的Service Worker v2，v2拦截了该页面的所有网络请求，这意味着V2现在处于活动状态！

有了这个，我们现在已经很好地理解了服务工作者的生命周期，所以让我们总结一下。

服务工作者生命周期摘要
我们可以看到虽然乍一看有点棘手，但Service Service Lifecycle的工作方式很有意义。生命周期就是：

仅向用户显示应用程序的一个版本
不破坏用户体验
不延迟应用程序启动
默认情况下，避免页面与服务工作者之间的版本不匹配
最后一点对于我们即将审核的下载和安装用例尤为重要。

让我们记住，Service Workers的一个常见用例是缓存整个应用程序，意思是字面上的所有HTML，CSS和Javascript！

然后，服务工作者在哪里存储这些文件？

缓存存储API
在安装时，服务工作者将从网络中获取一起创建给定应用程序版本的所有捆绑包，然后将它们存储在称为缓存存储的浏览器缓存中。

与Service Worker API一样，Cache Storage也是基于Promise的，非常易于使用。然后，我们将使用此API并使用它来实现下载和安装用例的安装阶段。

第1步 - 实施后台应用程序下载
然后让我们开始调整我们的Hello world日志拦截器示例，并使用后台安装功能扩展它。

我们要做的第一件事是，我们将在安装阶段在后台下载所有Javascript和CSS文件，我们将把这些文件直接添加到缓存存储：


const VERSION =  ' v3 ' ;

自我。的addEventListener（'安装'，事件 =>  事件。最好推迟（installServiceWorker（）））;


async  function installServiceWorker（）{

    log（“ Service Worker安装已启动”）;

    const cache =  await  caches。open（ getCacheName（））;

    返回 缓存。addAll（[
        ' / '，
        ' carousel.css '，
        ' http://getbootstrap.com/dist/css/bootstrap.min.css '，
        ' https://code.jquery.com/jquery-3.2.1.slim.min.js '，
        ' http://getbootstrap.com/assets/js/vendor/popper.min.js '，
        ' http://getbootstrap.com/dist/js/bootstrap.min.js '，
        ' http://getbootstrap.com/assets/js/vendor/holder.min.js '
    ]）;
}

查看 由GitHub用❤托管的原始04.ts
在这个例子中，这里再次发生了很多事情，所以让我们一步一步地分解它：

我们要做的第一件事是，我们得到一个开放缓存的引用，使用caches.open()它返回一个Promise
我们将一个版本号附加到缓存名称，这意味着随着新版本的发布，将创建新的缓存
然后我们正在执行一系列HTTP请求来获取生成给定版本应用程序的所有文件
然后我们将所有这些文件直接添加到缓存存储中
缓存的关键是用于发出HTTP请求的Request对象
存储在缓存中的值是HTTP Response对象本身，我们可以直接向应用程序提供服务
该addAll()调用返回一个Promise，如果为加载每个文件所做的所有HTTP请求都有效，它将成功解析
检查缓存存储的内容
在我们的例子中，所有文件的下载都有效，这意味着安装阶段成功结束！现在让我们使用Chrome开发工具了解我们在缓存存储中存储的内容：

服务工作者v3

此面板位于Dev Tools中相同的Application选项卡上，位于名为Cache Storage的可折叠菜单下。

注意：如果打开菜单但找不到新的缓存内容，请右键单击“缓存存储”节点，然后单击“刷新”

我们可以看到，所有应用程序包都已在后台下载，应用程序已准备好从缓存中提供！

但在此之前，让我们继续并首先清除Cache Storage中所有以前版本的应用程序。

第2步 - 清除以前的应用程序版本
清除以前版本的应用程序的最佳时刻是服务工作者激活时间，因为这是我们可以确保用户不再在任何浏览器选项卡中使用以前的应用程序版本的唯一时刻。

这是我们如何在激活时清除以前的应用程序版本：


自我。addEventListener（' activate '，（）=>  activateSW（））;

异步 函数 activateSW（）{

    log（' Service Worker activated '）;

    const cacheKeys =  等待 缓存。keys（）;

    cacheKeys。forEach（cacheKey  => {
        if（cacheKey  ！==  getCacheName（））{
            缓存。delete（cacheKey）;
        }
    }）;
}

查看 由GitHub用❤托管的原始05.ts
我们可以看到，我们循环遍历缓存存储中可用的所有缓存名称，并删除与当前应用程序版本（即V3）不对应的所有缓存。

关于async/ awaitsyntax的注释
请注意，caches.keys()返回Promise，就像一般情况下它会发生在Cache Storage API调用中。

我们希望等待Promise解析，然后在下面的其余代码中使用该值，因此我们将继续应用await等待Promise解析的语法。

正如我们所看到的，这是使异步的基于Promise的代码看起来更具可读性和更接近同步代码的好方法，但这仅适用于使用async关键字注释的方法。

这种async / await语法已在很多浏览器中提供（请参阅此处获取支持），例如在Chrome中，您可以尝试这些示例而无需任何转换。

步骤3 - 使用缓存然后网络策略从缓存提供应用程序
实现应用程序下载和安装所需的最后一步是直接从Cache Storage提供应用程序包，并在必要时回退到网络：


自我。的addEventListener（'取'，事件 =>  事件。respondWith（cacheThenNetwork（事件）））;

异步 函数 cacheThenNetwork（event）{

    const cache =  await  caches。open（ getCacheName（））;

    const cachedResponse =  等待 缓存。匹配（事件。请求）;

    if（cachedResponse）{
        日志（'提供来自高速缓存：'  +  事件。请求。URL）;
        return  cachedResponse ;
    }

    常量 networkResponse =  等待 取（事件。请求）;

    日志（'主叫网络：'  +  事件。请求。URL）;

    return  networkResponse ;
}


查看 由GitHub用❤托管的原始06.ts
让我们分解这个例子，看看如何应用Cache Then Network策略：

我们在异步函数中拦截应用程序发出的所有HTTP调用
async函数将始终返回Promise respondWith()，显式地作为返回值，或者通过透明地将返回值包装在Promise中
在异步函数中，我们首先打开与当前应用程序版本对应的缓存
然后我们将查询缓存，以查看是否存在与应用程序发出的HTTP请求匹配的HTTP响应
调用match()也返回一个Promise，所以我们将在继续之前等待结果
如果找到匹配，这意味着应用程序发出的请求是在缓存中找到的，因此我们直接返回该HTTP响应 respondWith()
请注意，不需要从异步方法返回Promise，如果我们返回一个值，它将通过async / await机制隐式地包含在Promise中
如果没有找到匹配，我们将通过等待fetch()呼叫结果让请求进入网络
然后我们将记录转发到网络的请求，并将fetch()调用结果返回给应用程序
有了这个，应用程序用于加载缓存包的任何请求都将从缓存存储中提供，而其他请求（例如REST API调用）/api/courses仍将通过网络进行。

最后一步，我们有一个完整的下载和后台安装我们的Web应用程序的解决方案！所以让我们试一试。

部署新版本的应用程序
要查看正在运行的下载和安装机制，让我们在示例应用程序中打开一个新选项卡，并看到它现在正在运行Service Worker的V3版本，该版本实现了下载和安装功能。

注意：这是Service Worker 的完整版本v3

这是当前的控制台输出：

v3 Serving From Cache: bootstrap.min.css
v3 Serving From Cache: carousel.css
v3 Serving From Cache: jquery-3.2.1.slim.min.js
v3 Serving From Cache: popper.min.js
v3 Serving From Cache: bootstrap.min.js
...
正如我们所看到的，所有CSS和Javascript包都直接来自Cache Storage，而不是来自网络，正如预期的那样。如果我们有一个新版本的应用程序，现在会发生什么？

想象一下，我们对应用程序进行了大量修改，例如更改其设计或应用新主题。

如果每次直接从缓存中仍然提供v3版本，用户将如何获得新版本v4？

为了触发V4版本的安装，我们需要做的第一件事就是对Service Worker进行一些小改动，例如增加版本号：


const VERSION =  ' v4 ' ;
...。

查看 由GitHub用❤托管的原始07.ts
现在让我们关闭除一个以外的所有选项卡，然后刷新浏览器。以下是我们在控制台上的内容：

v3 Serving From Cache: bootstrap.min.css
v3 Serving From Cache: carousel.css
....
v4 Service Worker installation started 
正如我们所看到的，服务工作者（和应用程序）的V3仍然正常运行，正如预期的那样。这意味着应用程序版本由Service Worker v3提供，这意味着捆绑包都来自Cache命名app-cache-v3。

但我们也可以看到版本V4 已在后台安装。我们来看看Service Worker选项卡上的内容：

服务工作者v4

我们可以看到，版本V4正在等待激活。但是现在可以使用V4的捆绑包，它们可以对应于整个Web应用程序的完全不同的版本。

为了确认这一点，让我们看一下缓存存储的内容：

服务工作者v5

我们可以看到，缓存存储在此阶段包含两个版本的应用程序：

版本v3，仍然提供给用户
版本v4，已在后台下载，并且可以在所有版本v3选项卡关闭后立即使用
为了激活版本v4，让我们模拟一些正常的用户交互。用户最终将关闭运行v3版本的所有浏览器选项卡，然后稍后返回应用程序。

此时，浏览器将激活V4版本并从缓存中提供相应的文件：

v4 Service Worker activated
v4 Serving From Cache: bootstrap.min.css
v4 Serving From Cache: carousel.css
....
这样，整个生命周期就完成了，用户现在可以在浏览器中下载并安装新近更新的应用程序版本。

新版本的应用程序已在后台下载并安装，不会影响正常的用户体验。这实际上比原生移动设备更好！

自定义服务工作者生命周期行为
到目前为止我们所描述的是Service Worker生命周期的默认行为，这在Donwload和安装用例的上下文中很有意义。

现在让我们看看如何根据需要自定义生命周期，以更好地适应其他PWA用例。

请注意，正如我们将要看到的那样，尽管很有诱惑力，但仍不建议修改服务工作者生命周期的行为。

跳过等待阶段（以及可能导致的潜在问题）
例如，我们可以通过skipWaiting()在安装阶段结束时调用API来完全跳过服务工作者生命周期的等待阶段：


async  function installServiceWorker（）{

    log（“ Service Worker安装已启动”）;

    const cache =  await  caches。open（ getCacheName（））;

    等待 缓存。addAll（[
        ' / '，
        ' carousel.css '，
        ' http://getbootstrap.com/dist/css/bootstrap.min.css '，
        ' https://code.jquery.com/jquery-3.2.1.slim.min.js '，
        ' http://getbootstrap.com/assets/js/vendor/popper.min.js '，
        ' http://getbootstrap.com/dist/js/bootstrap.min.js '，
        ' http://getbootstrap.com/assets/js/vendor/holder.min.js '
    ]）;

    回归 自我。skipWaiting（）;
}

查看 由GitHub用❤托管的原始08.ts
在这个例子中，我们正在等待下载和安装文件，然后我们将调用self.skipWaiting()，它将返回一个Promise。

这将导致跳过生命周期的等待阶段，并使新版本的服务工作者立即变为活动状态。

这意味着如果用户打开新选项卡，则新版本将处于活动状态，这可能会导致选项卡之间的不一致。在大多数情况下，最好不要跳过等待阶段，并通过设计避免这些不一致的场景。

但是，这并不意味着通过使用skipWaiting()新版本的Service Worker可以立即拦截来自正在运行的选项卡的请求。

接管当前页面 clients.claim()
我们已经看到，例如，第一次加载带有Service Worker的页面时，Service Worker将被安装和激活，但它仍然无法拦截页面发出的网络请求。

我们必须刷新页面，以便让新的Service Worker开始拦截请求。

同样，这是为了保持一致性：如果页面的初始请求不是由服务工作者提供的，那么默认情况下，服务工作者也不会在启动后由该页面发出的任何HTTP请求。

但是我们可以通过让Service Worker 在激活时声明所有活动的应用程序选项卡来改变这种情况：


异步 函数 activateSW（）{

    log（' Service Worker activated '）;

    const cacheKeys =  等待 缓存。keys（）;

    cacheKeys。forEach（cacheKey  => {
        if（cacheKey  ！==  getCacheName（））{
             缓存。delete（cacheKey）;
        }
    }）;

    回归 自我。客户。索赔（）;
}

查看 由GitHub用❤托管的原始09.ts
调用claim()将允许Activated Service Worker立即开始从正在运行的页面（以及其他打开的选项卡）中拦截请求（包括Ajax），而无需等待重新加载。

这种服务工作者的早期激活带来了不一致的可能性：我们可能最终得到v4版服务的页面，以使Service Worker v5拦截其运行时HTTP请求。

但是对于某些用例，这种早期激活是我们需要的：想象一下在Scope /api上运行的第二个服务工作者在IndexedDB 上缓存应用程序数据：我们可能希望尽快激活它，以尽快缓存应用程序数据。

手动更新服务工作者
默认情况下，如果服务器上有新版本的Service Worker准备安装，浏览器将检查用户导航。

如果由于某种原因，我们有一个应用程序将长时间保持打开状态（如安装到用户主屏幕的PWA），我们可以手动检查是否有新版本的服务工作者使用注册对象是这样的：


导航员。serviceWorker。register（'/ sw-download-install.js '，{
    范围：' / '
}）
。然后（注册 => {

    控制台。log（“服务工作者注册完成...... ”）;

    //定期检查（每小时）是否有新版本的服务工作者
    setInterval（（）=> {

        注册。update（）;

    }，3600000）;

}）;

查看 由GitHub用❤托管的原始10.ts
如果服务器上有新版本的Service worker，则调用update()将触发新的后台安装。

这种定期检查通常不是必需的，因为浏览器已经非常频繁地对每个用户导航或其他事件（例如，如果收到推送通知）进行此检查。

我们想要检查是否有新版本的一个好方案是：如果我们运行的版本有错误怎么办？然后让我们谈谈如果应用程序出现问题会发生什么。

针对破碎的服务工作者的内置浏览器保护
正如您可能想象的那样，在用户计算机上缓存应用程序并绕过网络有点危险：如果用户意外下载的版本出现错误怎么办？

有一些针对此的内置浏览器保护。

例如，服务工作者永远不会拦截自己！

这sw.js意味着我们传递给的文件serviceWorker.register('sw.js')永远不会被fetch事件拦截。

但是，这不适用于Service Worker注册脚本sw-register.js，因此我们需要确保永远不会缓存它。

服务工作者和普通的浏览器缓存
Cache-Control由于其配置选项的混乱性，基于标头的标准浏览器缓存机制很容易被滥用。

为了避免这些问题，建议您熟悉一些常见的缓存最佳实践，因为这将有助于一般的任何应用程序，而不仅仅是PWA。

Cache-Control即使我们没有运行PWA，在为我们的应用程序设置标题时所犯的错误在生产中也会很麻烦，但使用服务工作者会使这些问题更加严重。

我们可能会遇到这样的情况：我们已经sw.js在标准浏览器缓存中缓存了Service Worker 文件，因为它提供了一个Cache-Control标题，使文件的生命周期很长。

假设sw.js服务的缓存生存期为一个月：

Cache-Control: max-age=2592000
浏览器确实会缓存标头，但由于该文件是服务工作者，它只会将其缓存最多24小时而不是1个月！

这是一个很好的预防措施，但是，在安装补丁之前，网站会被打破一整天。最简单和最安全的解决方案是永远不要缓存Service worker文件或其注册脚本。

避免缓存Service Worker文件
服务器可以通过将这些文件明确标记为立即过期来确保这一点：

Cache-Control: max-age=0
说到普通的浏览器缓存，CSS和JS包的缓存头怎么样？

有关使用浏览器缓存和服务工作者的注意事项
存储在缓存存储中的CSS / Js包将从网络加载，并且这些包可以或不可以使用Cache-Control标头提供，这意味着我们可能有两个缓存在运行，可能会相互干扰。

这可能导致故障情况，例如安装了新版本的Service Worker，但尝试加载新版本的JS捆绑文件，但不会更改文件名！

但是该文件缓存在普通的浏览器缓存中，并且古代版本偶然会被提供给服务工作者。

这意味着Service Worker的安装成功完成，但Cache Storage现在具有其中一个bundle的错误版本，这意味着应用程序安装已损坏。

那么我们如何避免遇到这些情况呢？最简单的方法是应用与非PWA应用程序相同的缓存策略：不同类型的文件需要不同的缓存策略。

Cache-Control 用于CSS / JS包
对于CSS和JS包，最简单的方法是在文件名后附加文件内容的哈希值或版本号，例如：bootstrap.v4.min.css。

然后对于这些文件，我们可以选择一个非常长的最大年龄，基本上声明它们是不可变的并永远缓存它们：

Cache-Control: max-age=31536000
如果文件的新版本可用，则文件名将更改（这可以由构建系统强制执行），并且将下载和缓存新版本。

这将避免支持Service Worker的浏览器和不支持Service Workers的浏览器的许多常见缓存问题。

从第三方域加载资源包
在此示例中，我们已从本地域下载了所有捆绑包。但是，如果我们想从Service worker中加载来自其他第三方域的CSS和JS包，比如从CDN加载，该怎么办？

这是可能的，但第三方域允许执行该跨源请求，就像任何其他CORS请求一样。

这可以通过使用此标头提供bundle文件来完成：

access-control-allow-origin: https://yourdomain.com
如果我们从像Amazon Cloudfront这样的CDN提供这些捆绑文件，并且我们希望这些文件可以通过来自任何域的交叉源请求加载，而不仅仅是https://yourdomain.com，我们可以使用此标头：

access-control-allow-origin: *
结论
正如我们所看到的，如果我们一起查看它们并在特定用例的上下文中而不是孤立地看，所有多个PWA功能和相关的PWA API都是最有意义的。

我们可以做的远远超过我们所涵盖的下载和安装用例，这只是一个例子，它恰好是理解为什么服务工作者生命周期按照原样设计的最佳起点。

Service Worker规范的核心理念是将这些网络代理功能放在开发人员手中，这样我们就可以实现许多不同的PWA用例和模式，而不是只提供一组预定义的离线模式（就像它是应用程序缓存的情况）。

我希望这篇文章有助于开始使用服务工作者，并且您喜欢它！

如果您想了解有关构建在Service Workers之上的Angular PWA功能的更多信息，请查看完整Angular PWA系列的其他帖子：

服务工作者 - 实用指导（几个例子）
Angular App Shell - 提升应用程序启动性能
Angular Service Worker - 将您的应用程序转换为PWA的循序渐进指南
为了在更多这样的帖子出来时收到通知，我邀请您订阅我们的时事通讯：

Angular大学
观看25％的Angular视频课程，获得及时的Angular新闻和PDF： 


Email*
 订阅并获得免费课程
相关链接
缓存最佳实践

服务工作者基础

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
Angular：host，：host-context，:: ng-deep - Angular View Encapsulation
在这篇文章中，我们将了解默认的Angular样式机制（Emulated Encapsulation）如何工作，以及...

角度安全 - 使用JSON Web令牌（JWT）进行身份验证：完整指南
这篇文章是在Angular应用程序中设计和实现基于JWT的身份验证的分步指南。目标…

Angular大学 ©2018与Ghost一起出版
分享到Twitter
分享到Google+
分享到Facebook
，股数59