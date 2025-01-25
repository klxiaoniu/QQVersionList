# QQ 版本列表实用工具 for Android

![QQ 版本列表实用工具 Banner](https://raw.githubusercontent.com/klxiaoniu/QQVersionList/master/QQVerToolBanner.png)

<div align="center">

[注意事项](#注意事项) | [简介](#简介) | [如何使用](#如何使用) | [获取更新](#获取更新) | [常见问题](#常见问题) | [其它](#其它)

[贡献成员](#贡献成员) | [开源相关](#开源相关) | [商业服务鸣谢](#商业服务鸣谢) | [星标趋势](#星标趋势) | [孪生项目](#孪生项目)

</div> 

<details>

<summary>关于近期多家国内 Android OEM 系统报毒 QQ 版本列表实用工具的声明</summary>

## 关于近期多家国内 Android OEM 系统报毒 QQ 版本列表实用工具的声明

近期我们发现多家国内 Android OEM 系统（包括但不限于小米 MIUI/HyperOS、荣耀 MagicOS 等）将 QQ 版本列表实用工具列为病毒，特在此发表声明：

### 1. 产品声明与保证

QQ 版本列表实用工具严格遵守中华人民共和国相关法律法规。我们确认，该应用程序的所有代码及其更新版本均未包含任何恶意代码或病毒性质的内容。

### 2. 开源许可证声明

QQ 版本列表实用工具的源代码依据经过开源促进会（[Open Source Initiative](https://opensource.org)）认证的 GNU Affero General Public License Version 3（AGPL v3）协议开放源代码，用户可以依照协议条款自由查看、审计和使用源代码。

### 3. 法律责任

若任何个人或组织因上述误报行为遭受损害，QQ 版本列表实用工具保留依法追究法律责任的权利。

特此声明！

附 VirusTotal 对 QQ 版本列表实用工具 v1.2.2-3b425fc 的扫描结果：https://www.virustotal.com/gui/file/865f30709812664937192d55c64568f7c8df3406f0dda3faf5534e64eef756c2

</details>

<span id="注意事项"></span>

## 注意事项：使用前须知

> [!WARNING]
> 请确保您在使用前充分审慎阅读了[用户协议](https://github.com/klxiaoniu/QQVersionList/blob/master/UserAgreement.md)。鉴于 QQ 测试版可能存在不可预知的稳定性问题，您在下载及使用该测试版本之前，必须明确并确保自身具备足够的风险识别和承受能力。根据相关条款，您使用本软件时应当已了解并同意，因下载或使用 QQ 测试版而可能产生的任何直接或间接损失、损害以及其他不利后果，均由您自行承担全部责任。

> [!WARNING]
> QQ 版本列表实用工具为[自由软件](https://www.gnu.org/philosophy/free-sw.html)，其第一方软件本体及源代码仅可在 GitHub 或相关官方平台上免费并自由获取。根据[用户协议](https://github.com/klxiaoniu/QQVersionList/blob/master/UserAgreement.md)，如果您从未经 GitHub 或相关官方平台获取本应用，QQ 版本列表实用工具无法保证该应用能够正常使用，并对因此给您造成的损失不予负责。

> [!WARNING]
> QQ 版本列表实用工具提供的所有服务及内容均旨在促进合法的学习交流活动，严禁用户将其用于任何非法、违规或侵犯他人权益的目的。敬请所有用户严格遵守相关法律法规，在使用本应用的过程中秉持合法、正当与诚信原则，切勿涉足任何违法用途。如有违反，相关法律责任将由行为人自负，同时，本应用亦保留采取一切必要措施的权利，包括但不限于暂停或终止服务，并追究其法律责任。

> [!WARNING]
> QQ 版本列表实用工具不面向中国大陆境内公众用户提供服务，因此不会在中国大陆境内进行移动互联网应用程序备案。根据[中华人民共和国《工业和信息化部关于开展移动互联网应用程序备案工作的通知》（工信部信管〔2023〕105号）](https://www.gov.cn/zhengce/zhengceku/202308/content_6897341.htm)，应用程序分发平台不得为未履行备案手续的应用程序提供分发服务。请任何未经 QQ 版本列表实用工具授权而私自上架 QQ 版本列表实用工具的在中华人民共和国境内运营的各软件下载站或应用程序分发平台立即全面下架 QQ 版本列表实用工具。因私自上架 QQ 版本列表实用工具而触犯相关法律法规或产生任何直接或间接损失、损害以及其他不利后果的，均由您自行承担全部责任。

## 简介

QQ 版本列表实用工具 for Android 是一个使用 Material 3 组件库构建，可用于查看 Android QQ 与 Android TIM 版本列表的 Android 软件。QQ 版本列表实用工具用户可以通过本应用了解到 Android QQ 与 Android TIM 版本更新的最新信息。

<span id="如何使用"></span>

## 如何使用？

### 版本列表

在进入 QQ 版本列表实用工具时，您首先会看到一系列显示“x.y.z”“xxx MB”的卡片，这些卡片组成的列表即为 Android QQ 的版本列表，卡片列表展示了已经或即将发布的 Android QQ 版本。版本信息来源：https://im.qq.com/rainbow/androidQQVersionList

从右向左滑动版本列表，可切换到 TIM 版本列表。TIM 版本信息来源：https://im.qq.com/rainbow/TIMDownload

通过点击卡片右侧箭头按钮，可展开卡片以查阅到更为详尽的信息。

默认情况下，长按卡片文字会弹出展示卡片原始 JSON 字符串的对话框，长按对话框文字可选择复制字符串内容。可在设置中关闭此功能。

### 应用包分析

在 QQ 版本列表实用工具首页，长按顶部“本机 QQ/TIM”卡片即可查看本机 QQ/TIM 的应用包分析。

也可将 QQ/TIM APK 文件通过系统分享至 QQ 版本列表实用工具以查看该 QQ/TIM APK 的应用包分析。

### 实验性功能

> [!IMPORTANT]
> QQ 版本列表实用工具可能以软件实验形式提供一些尚不稳定的服务，此类服务会明确标注“实验性”（或其的其它语言形式）。您使用此类服务即代表您已明确并确保自身具备足够的风险识别和承受能力。因使用此类实验性服务而可能产生的任何直接或间接损失、损害以及其它不利后果，QQ 版本列表实用工具不承担责任。

在 QQ 版本列表实用工具界面，点击底部锥形瓶按钮即可进入实验性功能对话框。

#### 从腾讯服务器配置拉取微信最新测试版下载直链

Android 微信测试版相关信息配置在[腾讯服务器配置文件](https://dldir1.qq.com/weixin/android/weixin_android_alpha_config.json)内。可使用 QQ 版本列表实用工具提供的“从腾讯服务器配置拉取微信最新测试版下载直链”功能尝试获取微信最新测试版下载直链。

> [!WARNING]
> 此功能并非每次请求都能成功获取到 Android 微信测试版下载直链，当无法获取下载直链时可能存在的情况是微信还未发布测试版或测试版已撤包。QQ 版本列表实用工具不对此功能及其任何后果作出任何可靠性保证。请明确并确保自身具备足够的风险识别和承受能力。

#### 从微信输入法测试通道获取微信输入法最新测试版下载直链

Android 微信输入法测试版下载直链可由[微信输入法公网测试通道](https://z.weixin.qq.com/android/download?channel=latest)重定向获取。可使用 QQ 版本列表实用工具提供的“从微信输入法测试通道获取微信输入法最新测试版下载直链”功能尝试获取微信输入法最新测试版下载直链。

#### 腾讯应用宝更新获取（实验性）

QQ、TIM、微信、企业微信、微信输入法使用腾讯应用宝（[腾讯应用开放平台](https://app.open.qq.com/)）分发软件最新安装包。可使用 QQ 版本列表实用工具提供的腾讯应用宝更新获取（实验性）获取 QQ、TIM、微信、企业微信、微信输入法最新腾讯应用宝上架版本安装包下载直链。

在 QQ 版本列表实用工具界面，点击底部锥形瓶按钮即可看到“腾讯应用宝更新获取（实验性）”选项，点击即可进入“腾讯应用宝更新获取（实验性）”对话框。之后按提示进行操作即可。

#### TDS 腾讯端服务 Shiply 容器与发布平台更新获取（实验性）

腾讯 QQ 使用 [TDS 腾讯端服务 Shiply 容器与发布平台](https://shiply.tds.qq.com/)，根据 QQ 号（uin）及其终端信息分发小范围灰度测试安装包。当您所使用的 Android QQ 收到官方升级弹窗提醒时，可使用 QQ 版本列表实用工具提供的 TDS 腾讯端服务 Shiply 容器与发布平台更新获取（实验性）尝试获取升级安装包下载直链。

在 QQ 版本列表实用工具界面，点击底部锥形瓶按钮即可看到“TDS 腾讯端服务 Shiply 容器与发布平台更新获取（实验性）”选项，点击即可进入“Shiply 平台更新获取（实验性）”对话框。

对话框含有两个输入框，分别是“uin”和“版本”。请在“uin”输入框填写收到官方升级弹窗提醒的 QQ 号，在“版本”输入框填写收到官方升级弹窗提醒时正在使用的 QQ 版本。

在“进阶配置”中可配置“appid（非必填）”等可选参数。

填写完毕后，点击“开始”，QQ 版本列表实用工具将会尝试根据填写参数构造内容并以此向 Shiply 平台发送 POST 请求。

如果返回内容包含 Android 应用安装包下载直链，QQ 版本列表实用工具将会自动对 Android 应用安装包下载直链进行识别和置顶推荐，点击推荐卡片可进行进一步操作（如下载、分享等），长按推荐卡片即可快捷复制直链。

> [!WARNING]
> 此功能并非每次请求都能成功获取到 Android QQ 应用安装包下载直链，无法获取 Android QQ 应用安装包下载直链则表示填写的参数不在 Shiply 平台认可与分发范围内。QQ 版本列表实用工具不对此功能及其任何后果作出任何可靠性保证。请明确并确保自身具备足够的风险识别和承受能力。

### 扫版（维护模式）

> [!WARNING]
> 由于 Android QQ 和 Android TIM 的较新版本越来越倾向于使用包含特征码的下载直链，使得通过枚举法获取到 Android QQ 和 Android TIM 的下载直链变得异常困难。基于此，QQ 版本列表实用工具的扫版功能进入维护模式。
> 
> #### 扫版进入维护模式意味着什么？
> 
> - 现行相关功能仍可继续使用；
> - 不再对相关功能进行积极开发，也不再接受相关新功能请求；
> - QQ 版本列表实用工具不再对相关功能及其任何后果作出任何可靠性保证。使用时请明确并确保自身具备足够的风险识别和承受能力。

在 Android QQ - 首页侧滑菜单 - 设置 - 关于QQ与帮助 中可得知，Android QQ 的版本号通常为 `x.y.z.nnnnn`。其中 `x.y.z` 在这里被称为“主版本号”，而 `nnnnn` 被称为“小版本号”。

在 QQ 版本列表实用工具界面，点击右下角放大镜图标浮动按钮即可进入“扫版 Extended”对话框。

对话框含有三个输入框，分别是“主版本号”、“扫版类型”和“小版本号”。“主版本号”已经预填入了版本列表显示的最新版本号，也可自行修改。

- 若选择扫正式版，无需填写小版本号，软件将尝试访问以下链接：
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>_64.apk`
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>_64_HB.apk`
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>_64_HB1.apk`
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>_64_HB2.apk`
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>_64_HB3.apk`
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>_64_BBPJ.apk`
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>_HB_64.apk`
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>_HB1_64.apk`
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>_HB2_64.apk`
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>_HB3_64.apk`
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>_BBPJ_64.apk`

- 若选择扫测试版，则需要填写起始小版本号：

  - 默认情况下，软件将尝试访问 `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_64.apk` ，若当次访问未果，默认情况下将按照设置逻辑自动递增小版本号后再次尝试访问，直到访问成功为止。

  - 在设置中打开扩展测试版扫版格式后，软件将尝试访问以下链接：
    <details>
    <summary>点击展开</summary>

    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_64.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_64_HB.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_64_HB1.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_64_HB2.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_64_HB3.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_HB_64.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_HB1_64.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_HB2_64.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_HB3_64.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_64_HD.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_64_HD1.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_64_HD2.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_64_HD3.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_HD_64.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_HD1_64.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_HD2_64.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_HD3_64.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_64_HD1HB.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_HD1HB_64.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号>_test.apk`
    </details>

    若当次访问未果，默认情况下将按照设置逻辑自动递增小版本号后再次尝试访问，直到访问成功为止。

  - 设置自定义扫版后缀后，可以扫描以下直链格式：

    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号><自定义后缀>.apk`
    - `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_<主版本号>.<小版本号><自定义后缀>.apk`

- 若选择 TIM 扫版，对应的直链为：
  - `https://downv6.qq.com/qqweb/QQ_1/android_apk/TIM_<主版本号>.<小版本号><自定义后缀>.apk`

  填入相应输入框内容后，软件将尝试访问上述链接。若当次访问未果，默认情况下将自动递增小版本号后再次尝试访问，直到访问成功为止。

> [!TIP]
> QQ 版本列表实用工具实验性支持了 Android 微信的扫版。若选择微信扫版，对话框将变更为四个输入框，分别是“主版本号”、“扫版类型”、“真实版本号”和“十六进制代码”，对应的直链为：
>
> - `http://dldir1v6.qq.com/weixin/android/weixin<主版本号>android<真实版本号>_<十六进制代码>_arm64.apk`
> - `http://dldir1v6.qq.com/weixin/android/weixin<主版本号>android<真实版本号>_<十六进制代码>_arm64_1.apk`
>
> 填入相应输入框内容后，软件将尝试访问上述链接。若当次访问未果，默认情况下将自动递增十六进制代码后再次尝试访问，直到访问成功为止。

访问成功后，软件会弹出成功对话框，对话框下方提供了一系列动作按钮，依次是“分享”、“下载”、“停止”、“跳过”和“复制”。

> [!WARNING]
> 微信扫版为 QQ 版本列表实用工具附带的实验性功能，可能存在不可预知的稳定性问题。请明确并确保自身具备足够的风险识别和承受能力。

#### Firebase 服务（实验性）

借助 Google Firebase 云消息传递，QQ 版本列表实用工具 1.4.1 版本实现了检测到版本列表更新后向用户推送系统通知的功能。

在 QQ 版本列表实用工具界面，点击底部锥形瓶按钮即可看到“初始化 Firebase 服务”选项，点击后即可初始化 Firebase 服务。

初始化 Firebase 服务后，将立即在本地由 Firebase SDK 生成注册令牌，并将标识符和配置数据上传到 Firebase 服务器。此操作一经启用即无法撤销。

初始化 Firebase 服务后，点击设置即可看到“版本列表更新时推送系统通知（通过 Firebase 云消息传递）”开关。打开后，QQ 版本列表实用工具首先将申请系统通知权限，之后将与 Google 服务器进行通信，上述步骤完成后即可订阅版本列表更新通知。

> [!IMPORTANT]
> Firebase 服务依赖于设备 Google Play 服务，设备缺失 Google Play 服务时将无法使用 Firebase 服务。

> [!IMPORTANT]
> 订阅版本列表更新通知需和 Google 服务器进行通信，请确保您的设备可以正常连接到 Google 服务器。

> [!WARNING]
> 在中国大陆发行的 Android 设备可能存在无法接收 Firebase 云消息传递的情况。

> [!WARNING]
> “通过 Firebase 云消息传递的版本列表更新提醒”为实验性功能，QQ 版本列表实用工具不对此功能的及时性和有效性做出任何可靠性保证。

<span id="获取更新"></span>

## 获取更新

<a href='https://github.com/klxiaoniu/QQVersionList/releases'><img src='https://raw.githubusercontent.com/klxiaoniu/QQVersionList/master/ReadmeAssets/GitHub-Badge.png' width="300" alt="Get it on GitHub"></a>

<a href='https://github.com/klxiaoniu/QQVersionList/blob/master/ReadmeAssets/Get-it-on-Obtainium.md'><img src='https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png' width="300" alt="Obtanium"></a>

## 常见问题

### QQ 版本列表实用工具能否实现“检测到新测试版本下载直链自动提醒您”的功能？

QQ 版本列表实用工具不能实现“检测到新测试版本下载直链自动提醒您”的功能，因为这需要自有服务器，并且需要自有服务器去长时间请求腾讯服务器，存在法律和技术风险。

### 设置 - 扫版直链格式设置 里的“其它”是什么？

#### “使用 QQ 8.9.58 测试版直链格式”

腾讯 QQ 团队曾在且目前仅在 QQ 8.9.58 测试 QQNT 技术架构时使用了 `https://downv6.qq.com/qqweb/QQ_1/android_apk/qq_<主版本号>.<小版本号>_64.apk` 直链格式。鉴于此，QQ 版本列表实用工具添加了支持此类非标准但实际存在的直链的选项。

勾选“使用 QQ 8.9.58 测试版直链格式”后，“正式版”“测试版”扫版格式将变更为 `https://downv6.qq.com/qqweb/QQ_1/android_apk/qq_<主版本号><自定义后缀>.apk` 或 `https://downv6.qq.com/qqweb/QQ_1/android_apk/qq_<主版本号>.<小版本号><自定义后缀>.apk`。

#### “扫版类型添加 QQ 9.0.8.14600 空格直链格式”

2023 年 12 月 22 日，腾讯 QQ 官方团队在上传 Android QQ 9.0.8.14600 版本时，不慎将常规链接格式 `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android_9.0.8.14600.64_apk` 错误配置为包含 URL 编码空格形式的链接地址 `https://downv6.qq.com/qqweb/QQ_1/android_apk/Android%209.0.8.14600%2064.apk` 。 鉴于此，QQ 版本列表实用工具增设了“空格扫版”扫版模式，该功能在原有的“测试版”扫版模式上将版本号中的 `.` 字符替换为 URL 编码的空格符 `%20`，以适应并支持此类非标准但实际存在的直链。

勾选“扫版类型添加 QQ 9.0.8.14600 空格直链格式”后，QQ 版本列表实用工具将会在扫版类型下拉菜单添加“空格扫版”选项，选中即可使用上述格式。

### 为什么默认添加了 QQ 测试版扫版小版本号必须为 5 的倍数这项限制？

基于对 Android QQ 长期以来的版本号发布规律进行深入观察和分析的结果，我们发现 Android QQ 小版本号更新通常遵循每增加一个有效版本即递增 5 的倍数这一特定模式。为了贴近这一潜在实际规范并确保 QQ 版本列表实用工具的快捷性，QQ 版本列表实用工具依据最佳实践原则，默认设置小版本号和扫版必须为 5 的倍数的限制规则。此限制并非强制，用户可随时进入设置解除此限制。

### 版本列表中已经有新的版本号了，为什么我使用枚举扫版却获取不到下载链接？

即使版本列表已出现了新的版本号，也并不意味着 QQ 团队已经完成了新版本（含测试版）安装包在腾讯公网服务器的部署和发布。一种可能的情况是，QQ 团队正在进行新版本的内部测试阶段或小范围灰度推送阶段，因而尚未对外提供广泛公网下载渠道。

### 为什么不提供 Android 微信的版本列表？

目前还没有找到来自官方的可靠且请求次数少而信息密度大的 Android 微信版本列表数据源，因此 QQ 版本列表实用工具无法提供 Android 微信的版本列表。如果您发现了可靠的 Android 微信版本列表数据源，欢迎提出 Issue(s) 或提交 PR。

### 什么是 QQNT 技术架构？

QQNT 技术架构是腾讯 QQ 客户端全新的跨平台技术架构体系。QQNT 技术架构将 QQ 客户端核心功能——如核心登录、消息系统、关系链、富媒体、长连接、数据库等——下沉至 QQNT 内核层，使用 C++ 抽象逻辑封装为原生库并提供多平台多语言一致性接口，以实现 QQ 客户端核心逻辑代码跨平台与程序高性能运作。

### 什么是 Kuikly？

Kuikly（Kotlin UI Kit）跨端开发框架，是 TDF 腾讯端框架（Tencent Device-oriented Framework）的一部分。Kuikly 通过自研 Kotlin MultiPlatform 逻辑与终端界面原生控件渲染映射协议层，并采用声明式与响应式设计，使采用 Kuikly 的 Kotlin 开发者能拥有原生高效的 Android 开发体验并构建具有原生性能的跨平台应用。Kuikly 更可依托于 [TDS 腾讯端服务 Shiply 容器与发布平台](https://shiply.tds.qq.com/)，实现按页颗粒度的完备客户端界面动态化能力。

## 其它

欢迎[帮助我们完成本地化翻译](https://crowdin.com/project/qqversionstool)！提交翻译则代表您同意您的译文将跟随 QQ 版本列表实用工具项目采用 [GNU Affero General Public License Version 3](/LICENSE) 开源许可。

[![Crowdin](https://badges.crowdin.net/qqversionstool/localized.svg)](https://crowdin.com/project/qqversionstool)

> [!IMPORTANT]
> QQ 版本列表实用工具始终坚守法律底线，秉持尊重与保护所有用户及第三方合法权益的原则。我们深切认识到任何可能存在的权益侵犯行为都会对权益方造成潜在影响，对此，我们表示由衷歉意，并承诺，一旦接到权益方的权益受到侵犯的通知，我们将立即依法启动核查程序，并在确认侵权事实后，迅速采取有效措施，以最大程度地消除不良影响，恢复并保障权益方的合法权益。敬请相关权益方在发现 QQ 版本列表实用工具存在任何侵权内容时，及时与我们取得联系，我们将竭诚为权益方提供必要的协助与支持。

> [!NOTE]
> “腾讯”“QQ”“腾讯 QQ”“腾讯 TIM”“微信”“WeChat”“Weixin”“腾讯微信”“企业微信”“WeCom”“微信输入法”“WeType”“应用宝”“腾讯应用宝”“腾讯企点”等是深圳市腾讯计算机系统有限公司和/或其关联公司的商标。本项目对“腾讯”“QQ”“腾讯 QQ”“腾讯 TIM”“微信”“WeChat”“Weixin”“腾讯微信”“企业微信”“WeCom”“微信输入法”“WeType”“应用宝”“腾讯应用宝”“腾讯企点”等的使用旨在注明和指向对应主体，并非表示对“腾讯”“QQ”“腾讯 QQ”“腾讯 TIM”“微信”“WeChat”“Weixin”“腾讯微信”“企业微信”“WeCom”“微信输入法”“WeType”“应用宝”“腾讯应用宝”“腾讯企点”等商标的注册和拥有。
>
> Android™ 是 Google LLC 的商标。
> 
> Unreal® 及其徽标是 Epic Games, Inc. 在美国及其他国家或地区的商标或注册商标。

## 贡献成员

<a href="https://github.com/klxiaoniu/QQVersionList/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=klxiaoniu/QQVersionList" alt="贡献成员"/>
</a>

## 开源相关

QQ 版本列表实用工具采用 [GNU Affero General Public License Version 3](https://github.com/klxiaoniu/QQVersionList/blob/master/LICENSE) 开源许可。

QQ 版本列表实用工具的诞生离不开以下开源项目，感谢以下开源项目的作者和贡献者：

- [Material Components for Android（Android Open Source Project）](https://github.com/material-components/material-components-android/)，Licensed under [Apache License Version 2.0](https://github.com/material-components/material-components-android/blob/master/LICENSE)
- [Android Jetpack（Android Open Source Project）](https://developer.android.com/jetpack)，Licensed under [Apache License Version 2.0](https://github.com/androidx/androidx/blob/androidx-main/LICENSE.txt)
- [Remix Icon（Remix Design）](https://remixicon.com/)，Licensed under [Apache License Version 2.0](https://remixicon.com/license)
- [OkHttp（Square）](https://square.github.io/okhttp/)，Licensed under [Apache License Version 2.0](https://github.com/square/okhttp/blob/master/LICENSE.txt)
- [Kotlin（JetBrains）](https://kotlinlang.org/)，Licensed under [Apache License Version 2.0](https://github.com/JetBrains/kotlin/blob/master/license%2FREADME.md)
- [Gson（Google）](https://github.com/google/gson/)，Licensed under [Apache License Version 2.0](https://github.com/google/gson/blob/master/LICENSE)
- [Coil](https://coil-kt.github.io/coil/)，Licensed under [Apache License Version 2.0](https://github.com/coil-kt/coil/blob/main/LICENSE.txt)
- [Eclipse Temurin™](https://adoptium.net/temurin/)，Licensed under GNU General Public License, version 2 with the Classpath Exception
- [Oracle JDK](https://www.oracle.com/java/technologies/downloads/)，Licensed under [Oracle No-Fee Terms and Conditions](https://www.java.com/freeuselicense)
- [JetBrains Runtime](https://github.com/JetBrains/JetBrainsRuntime)，Licensed under [GNU General Public License Version 2](https://github.com/JetBrains/JetBrainsRuntime/blob/main/LICENSE)
- [Kotlin Serialization](https://github.com/Kotlin/kotlinx.serialization)，Licensed under [Apache License Version 2.0](https://github.com/Kotlin/kotlinx.serialization/blob/master/LICENSE.txt)
- [Get QQ Update Link（owo233）](https://github.com/callng/GQUL)，Licensed under [The Unlicense](https://github.com/callng/GQUL/blob/master/LICENSE)
- [Paris（Airbnb）](https://github.com/airbnb/paris)，Licensed under [Apache License Version 2.0](https://github.com/airbnb/paris/blob/master/LICENSE)
- [Apache Maven™](https://maven.apache.org/)，Licensed under [Apache License Version 2.0](https://github.com/apache/maven/blob/master/LICENSE)
- [Gradle](https://gradle.org/)，Licensed under [Apache License Version 2.0](https://github.com/gradle/gradle/blob/master/LICENSE)
- [Material Symbols / Material Icons（Google）](https://fonts.google.com/icons)，Licensed under [Apache License Version 2.0](https://github.com/google/material-design-icons/blob/master/LICENSE)
- [Obtainium（Imran）](https://github.com/ImranR98/Obtainium)，Licensed under [GNU General Public License Version 3](https://github.com/ImranR98/Obtainium/blob/main/LICENSE.md)
- [Secrets Gradle Plugin for Android（Google）](https://github.com/google/secrets-gradle-plugin)，Licensed under [Apache License Version 2.0](https://github.com/google/secrets-gradle-plugin/blob/main/LICENSE)
- [Firebase Android Open Source Development（Google）](https://firebase.google.com/)，Licensed under [Apache License Version 2.0](https://github.com/firebase/firebase-android-sdk/blob/main/LICENSE)
- [AndroidFastScroll（张海）](https://github.com/zhanghai/AndroidFastScroll)，Licensed under [Apache License Version 2.0](https://github.com/zhanghai/AndroidFastScroll/blob/master/LICENSE)
- [Kotlin Coroutines on Android](https://github.com/Kotlin/kotlinx.coroutines)，Licensed under [Apache License Version 2.0](https://github.com/Kotlin/kotlinx.coroutines/blob/master/LICENSE.txt)
- [Google Play services Plugins](https://github.com/google/play-services-plugins)，Licensed under [Apache License Version 2.0](https://github.com/google/play-services-plugins/blob/main/LICENSE)
- [Apache Commons™](https://commons.apache.org/)，Licensed under [Apache License Version 2.0](https://www.apache.org/licenses/LICENSE-2.0)
- [腾讯 Kona 国密套件](https://github.com/Tencent/TencentKonaSMSuite)，Licensed under [GNU General Public License, version 2 with the Classpath Exception](https://github.com/Tencent/TencentKonaSMSuite/blob/master/LICENSE.txt)
- [斑朵 Boundo（Cliff Liu）](https://github.com/cliuff/boundo)，Licensed under [Apache License Version 2.0](https://github.com/cliuff/boundo/blob/master/LICENSE)
- [Smali/Baksmali（Google fork of JesusFreke's）](https://github.com/google/smali)
- [智谱开放平台大模型接口 Java SDK](https://github.com/MetaGLM/zhipuai-sdk-java-v4)，Licensed under [MIT License](https://github.com/MetaGLM/zhipuai-sdk-java-v4/blob/main/LICENSE)
- [Jackson Core & Databind（FasterXML）](https://github.com/FasterXML/jackson)，Licensed under Apache License Version 2.0
- [Java API for GitHub（Kohsuke Kawaguchi）](https://github.com/hub4j/github-api)，Licensed under [MIT License](https://github.com/hub4j/github-api/blob/main/LICENSE.txt)
<!-- 未来再用
- [Jsoup](https://jsoup.org/)，Licensed under [MIT License](https://jsoup.org/license)
-->

## 商业服务鸣谢

- 感谢 [Crowdin](https://crowdin.com/) 为本开源项目提供免费的[开源项目计划](https://crowdin.com/page/open-source-project-setup-request)。[Crowdin](https://crowdin.com/) 是面向团队和企业的 AI 驱动本地化软件。使用 600+ 个应用和集成自动翻译您的内容。

## 星标趋势

[![星标趋势](https://starchart.cc/klxiaoniu/QQVersionList.svg?variant=adaptive)](https://starchart.cc/klxiaoniu/QQVersionList)

## 孪生项目

[QQ 版本列表 Vigor for WeChat MiniProgram](https://github.com/ArcticFoxPro/QQVersionListTool-WeChatMiniProgram)，Licensed under [木兰公共许可证, 第2版](https://github.com/ArcticFoxPro/QQVersionListTool-WeChatMiniProgram/blob/main/LICENSE)

[![QQ 版本列表 Vigor Banner](https://raw.githubusercontent.com/ArcticFoxPro/QQVersionListTool-WeChatMiniProgram/main/QQVerLiteBanner.png)](https://github.com/ArcticFoxPro/QQVersionListTool-WeChatMiniProgram)
