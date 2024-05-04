# 获取更新——基于 [Obtainium](https://github.com/ImranR98/Obtainium) 方案

您可以使用开源软件 [Obtainium](https://github.com/ImranR98/Obtainium) 来接收 QQ 版本列表实用工具的未来更新。Obtainium 可以让您从一个 APP 内直接从其他 APP 的发布源获取安装包和更新应用程序，并在发布新版本时收到通知。

## 一键导入

若您的 Android 设备已安装 Obtainium，点击下方图片即可快速导入 QQ 版本列表实用工具的 Obtainium 配置。

<a href='http://apps.obtainium.imranr.dev/redirect.html?r=obtainium://app/%7B%22id%22:%22com.xiaoniu.qqversionlist%22,%22url%22:%22https://github.com/klxiaoniu/QQVersionList%22,%22author%22:%22%E5%BF%AB%E4%B9%90%E5%B0%8F%E7%89%9B%E3%80%81%E6%9C%89%E9%B2%AB%E9%9B%AA%E7%8B%90%22,%22name%22:%22QQ%20%E7%89%88%E6%9C%AC%E5%88%97%E8%A1%A8%E5%AE%9E%E7%94%A8%E5%B7%A5%E5%85%B7%22,%22preferredApkIndex%22:0,%22additionalSettings%22:%22%7B%5C%22includePrereleases%5C%22:false,%5C%22appName%5C%22:%5C%22QQ%20%E7%89%88%E6%9C%AC%E5%88%97%E8%A1%A8%E5%AE%9E%E7%94%A8%E5%B7%A5%E5%85%B7%5C%22,%5C%22versionExtractionRegEx%5C%22:%5C%22(?%3C=v)(.*)%5C%22,%5C%22matchGroupToUse%5C%22:%5C%22$0-Release%5C%22%7D%22%7D'><img src='https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png' width="300" alt="Obtanium"></a>

## 手动导入

下载安装并打开 Obtainium，点击底部导航栏“添加应用”，在“来源 URL”中输入：

```
https://github.com/klxiaoniu/QQVersionList/
```

然后在下方“提取版本号的正则表达式”内填入：

```
(?<=v)(.*)
```

“从上述匹配结果中引用的捕获组”填入：

```
$0-Release
```

填入完成后，点击“添加”按钮即可。