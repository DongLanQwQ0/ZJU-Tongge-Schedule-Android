# 同格 · 浙大课表撞课比对

> **与一个或一群有趣的人同行**

<p align="center">
  <img src="https://raw.githubusercontent.com/DongLanQwQ0/ZJU-Tongge-Schedule/main/public/img/mascot/pig-pair.gif" width="200" alt="同格吉祥物：两只凑在一起的小猪">
</p>

课表摊开、一句一句问过去，常常还是没凑上。
同格把这件事丢给电脑：**传课表 → 建个群 → 同学扫码进来 →
一眼看出你俩哪几节课在一起、教室离得多近。**

搭话还得你自己来，但不用再对着两张课表数格子了 ✨

---

## 怎么用

1. **注册**：昵称（全局唯一，≤10 字）+ 密码（≥6 位），过一道算术验证码。
2. **传课表**：首页虚线框选教务导出的 `.ics` / `.txt`。不知道怎么导出？首页有图文教程。
   **原始文件不上传**，在你浏览器里解析。
3. **建群**：拿到邀请链接 + 二维码，发给同学。也可以同时发多条链接，每条单独设有效期。
   每个新群自带一条**永不过期的默认链接**，随时能作废或换一条。
   创建时可选**临时群组**：从创建时算起 24 小时后自动解散，活动不会续期；邀请码随群失效，个人课表保留。
4. **看重合**：群组页每个人都直接显示和你的重合课时数，按「同教室 / 同楼栋 / 同区域 / 不同区域」分级。
5. **比对**：点某个人 → 双人网格图 → 「保存课表截图」导出 PNG（全学期）。
6. **备注**：给群里的人起一个只有你看得见的名字（像微信备注名）。
7. **群主还能**：改名、移人、设「拿到链接就能进 / 要我同意」、管成员能不能转发邀请链接、
   把群主转让给群里的人（要输一次密码）。
8. **账号**：点右上角 ⋯ →「账号」。昵称改不了（它是别人找到你的那个名字），
   可以**自己改密码**；注销也在这里最下面，输密码确认。注销后账号、课表、备注都没了。

首页还有个「本地快速比对」：选两份文件直接在浏览器里比，不发给服务器。

## Android 应用

**同格 · 撞课** 支持 Android 8.0 及以上，提供应用内扫码、课表文件选择、截图保存和跟随系统的明暗主题，需要联网。

Web 首页「本地快速比对」卡片中的 **下载 APK** 打开下载页面，可选择 GitHub 直链、GHFast、GH-Proxy 或 ghproxy.net 下载最新正式版，也可跳转至 Android 与 Web 项目。App 内隐藏此下载入口。

Android 页面资源内置在 APK，本地比对可离线使用；登录页及菜单中的「服务器设置」可修改 HTTPS 服务器及部署路径，重启后仍保留。临时群组需部署新版服务端；旧服务端上的该选项会禁用。

- [下载最新 APK](https://github.com/DongLanQwQ0/ZJU-Tongge-Schedule-Android/releases/latest/download/Tongge.apk)
- [Android 项目](https://github.com/DongLanQwQ0/ZJU-Tongge-Schedule-Android)
- [Web 项目](https://github.com/DongLanQwQ0/ZJU-Tongge-Schedule)

应用内「检查更新」检测 GitHub 正式 Release。后续 Release 提供固定名称 `Tongge.apk`，下载页面的 `latest/download/Tongge.apk` 链接会随新版自动更新。

---

## 图怎么看

格子里**上半是你，下半是 TA**：底色表示两人地点关系（同教室 / 同楼栋 / 同区域 / 不同区域），
边框表示这是谁的课（深色实线 = 你，白色实线 = TA）。

周次上写着两个数字，例如 `第 3 周 · 年 40 周`：教学周从课表里最早的日期算起（**单双周也按它**），
年周次是自然年的 ISO 周次。两个都给，是因为教务导出的课表里混着调休和考试，会把教学周编号整体带偏。

---

## 邀请链接

群是永久的，有寿命的只是邀请链接：每条约 **1 天 / 3 天 / 7 天 / 1 个月 / 永久**，可单独作废。
**过期只挡新人**，已经在群里的人不受影响；群主自己的码永不过期。

群主还能决定**要不要让成员帮忙转发邀请码**。关掉之后，成员那边就看不到码了，
只会看到一句「找群主要一条」——想让谁进来，由群主一个人发。
（注意这是「谁来发」的约定，不是安全闸：已经发出去的链接照样能用。）

---

## 谁能看到我的课表

- **原始 `.ics` 不会上传**，发过去的只有课程名、时间、地点这些内容。
- **只有同群的人看得到**：没人能搜到你，站点也没有「按昵称找人」这种入口。
- 你给别人的**备注只有你自己看得见**；密码不会被明文保存。

---

## 常见问题

**为什么要算一道算术题？** 挡批量注册的脚本。要是你看不见图片（比如在用读屏软件），跟我说一声。

**提示「已经过期」？** 找群主再要一条链接就行，你不会因此被踢出群。

**跟教务对不上？** 同格按你导出的课表里的真实日期显示，不做推算——重新导出一份最新的再传一次。

---

## 求个 Star

一个人、一台服务器慢慢磨出来的。觉得还行，或者只是喜欢页脚那两只小猪 🐷，
麻烦点一下 ⭐ **[github.com/DongLanQwQ0/ZJU-Tongge-Schedule](https://github.com/DongLanQwQ0/ZJU-Tongge-Schedule)**

有想加的功能、遇到 bug，欢迎开 Issue。

<sub>MIT License © 2026 NuanDong Shao · 想自己搭一套：<a href="https://github.com/DongLanQwQ0/ZJU-Tongge-Schedule/blob/main/docs/DEPLOY.md">部署文档</a></sub>

## 群成员列表

群成员列表按页加载，每页 20 人；列表排序会使用本地记录的查看次数，查看次数只随请求发送给服务端，不会保存到服务端。分页或重新加载时会重新计算当前页排序。

群组页上的群主转让、成员备注和移除操作只作用于当前页显示的成员。成员列表的短期缓存有效时，他人在别处做的改动可能不会立即显示。

首页还支持在自己加入的所有群组中搜索成员；同一成员会合并显示共同群组，点击后优先进入最近查看过的共同群组。

---

## Android 构建

需要现有 JDK、Android SDK 36 / Build Tools 36.0.0，项目使用 Gradle Wrapper 9.6.0 / AGP 9.4.1。

PowerShell 7：`./build-apk.ps1`；依赖已缓存时可用 `./build-apk.ps1 -Offline`。

其他平台：`./gradlew :app:assembleRelease :app:lintRelease`。

输出：`app/build/outputs/apk/release/app-release.apk`。PowerShell 脚本另生成 `dist/Tongge-<版本>.apk`。

本次 Release 关闭调试，但沿用之前交付 APK 的本机签名以支持覆盖升级。签名文件不入库；后续发布必须保留同一个签名文件，换电脑时不可直接用新生成的 debug.keystore 签名并作为升级包发布。当前本机设置 `ANDROID_USER_HOME=F:\Android\user-home` 使用现有签名文件。

## APK 内置页面与服务器设置

HTML、CSS、JavaScript、图片及课表纯函数模块全部随 APK 安装；只有登录、群组、课表同步等 API 请求访问服务器。登录页可直接进行本地快速比对，无需登录或连接服务器。页面资源的更新随 APK Release 发布。

登录页及右上角菜单的「服务器设置」可修改完整 HTTPS 地址（域名/IP、端口、部署路径），设置在重启和升级后保留。初始地址是 `https://111.228.3.50/tongge/`；例如迁至 `https://schedule.example.com:8443/app/`，保存此地址即可。服务器须继续提供同格的 `api/*` 接口与有效 HTTPS 证书。切换服务器会退出当前登录；登录凭据按完整服务器地址隔离，邀请二维码与分享链接也使用当前地址。

`EmbeddedWebsite` 使用 AndroidX `WebViewAssetLoader` 将 APK 资源映射到当前服务器同源的保留路径 `/__tongge_app__/`，API 指向配置的部署路径，因此不需要服务端额外开放 CORS。保留路径缺失资源返回本地 404，不从服务器加载网页。外部网页通过系统浏览器打开，不能使用应用内的扫一扫或服务器设置入口。

网页源代码仍由[原项目](https://github.com/DongLanQwQ0/ZJU-Tongge-Schedule)维护。更新网页后运行 `./sync-web-assets.ps1 -WebProject <原项目路径>`，再构建 APK。脚本只复制 `public/` 和浏览器所需的六个 `shared/` 模块；入库的资源快照允许本仓库独立构建。

默认服务器位于 `ServerAddress.DEFAULT`，更新仓库位于 `ReleaseUpdates.REPOSITORY`。迁移服务器仅需要修改用户的服务器设置；以后使用稳定域名迁移时，可保持此地址并调整 DNS。服务器变更不会影响 GitHub 新版检测。

## 发布约定

1. 同时递增 `app/build.gradle` 的 `versionCode` 与 `versionName`。
2. 构建、验证签名与 Android Lint，通过后提交代码并创建 `v<版本>` 标签。
3. 发布正式 GitHub Release，附上 `Tongge-<版本>.apk`、同一安装包的固定名称副本 `Tongge.apk` 和更新说明。

新版检测读取 `/repos/DongLanQwQ0/ZJU-Tongge-Schedule-Android/releases/latest`，按数字比较稳定版本 `vMAJOR.MINOR.PATCH`，忽略草稿/预发布。没有 APK、无 Release、网络失败及 GitHub 限流会在手动检查时说明，自动检查保持安静。

不包含 Node 后端、网页用户数据或本地签名密钥。
