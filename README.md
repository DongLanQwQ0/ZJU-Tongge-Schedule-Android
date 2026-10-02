# 同格 · 撞课

[下载最新 APK](https://github.com/DongLanQwQ0/ZJU-Tongge-Schedule-Android/releases/latest)

原生 Java WebView 应用，连接 [同格网站](https://111.228.3.50/tongge/)。支持 Android 8.0 及以上，需要联网。

- 状态栏、导航栏、屏幕缺口和软键盘安全边距。
- 页面和系统栏跟随系统明暗主题，切换时保留当前页面。
- 本地课表文件选择与 PNG 截图保存。
- 首页扫码图标打开原生 ZXing 扫描，只在使用时申请摄像头权限。
- 后台检查 GitHub 正式 Release；成功检查间隔 6 小时，失败重试间隔 15 分钟。更多菜单支持手动「检查更新」。
- 新版提示含版本和更新说明；点「下载 APK」交给浏览器下载，用户自行安装。应用后台时发现的新版会保存，在回到前台后提示。

## 构建

需要现有 JDK、Android SDK 36 / Build Tools 36.0.0，项目使用 Gradle Wrapper 9.6.0 / AGP 9.4.1。

PowerShell 7：`./build-apk.ps1`；依赖已缓存时可用 `./build-apk.ps1 -Offline`。

其他平台：`./gradlew :app:assembleRelease :app:lintRelease`。

输出：`app/build/outputs/apk/release/app-release.apk`。PowerShell 脚本另生成 `dist/Tongge-<版本>.apk`。

本次 Release 关闭调试，但沿用之前交付 APK 的本机签名以支持覆盖升级。签名文件不入库；后续发布必须保留同一个签名文件，换电脑时不可直接用新生成的 debug.keystore 签名并作为升级包发布。当前本机设置 `ANDROID_USER_HOME=F:\Android\user-home` 使用现有签名文件。

站点入口位于 `MainActivity.HOME`，更新仓库位于 `ReleaseUpdates.REPOSITORY`。

## 发布约定

1. 同时递增 `app/build.gradle` 的 `versionCode` 与 `versionName`。
2. 构建、验证签名与 Android Lint，通过后提交代码并创建 `v<版本>` 标签。
3. 发布正式 GitHub Release，附上 `Tongge-<版本>.apk` 和更新说明。

新版检测读取 `/repos/DongLanQwQ0/ZJU-Tongge-Schedule-Android/releases/latest`，按数字比较稳定版本 `vMAJOR.MINOR.PATCH`，忽略草稿/预发布。没有 APK、无 Release、网络失败及 GitHub 限流会在手动检查时说明，自动检查保持安静。

不包含 Node 后端、网页用户数据或本地签名密钥。
