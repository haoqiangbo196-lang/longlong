# 雅阅 · ElegantReader

一款精致、流畅的本地电子书阅读器 Android App。

基于 **Kotlin + Jetpack Compose** 构建，界面采用精致拟物质感设计，所有动画均为非线性缓动曲线，阅读体验丝滑自然。

---

## ✨ 核心功能

| 功能 | 说明 |
|------|------|
| 📚 本地书架 | 拟物书封卡片，封面渐变 + 书脊高光 + 立体阴影，显示阅读进度与字数 |
| 📥 书籍导入 | 系统文件选择器导入书籍，自动拷贝到应用私有目录 |
| 📄 TXT 支持 | 自动检测编码（UTF-8 / GBK / UTF-16），智能识别章节 |
| 📖 EPUB 支持 | 解压解析 OPF 元数据、目录 spine、章节 XHTML，自动转纯文本 |
| 🔍 章节识别 | 正则匹配「第X章/第X节/第X回/Chapter X/序言/楔子/尾声」等 |
| 📖 分页阅读 | StaticLayout 精确测量，中英文混排自动换行分页 |
| 🎨 排版调节 | 字号、行间距、段间距、页边距、字体（默认/衬线/无衬线/等宽） |
| 🌗 阅读主题 | 6 种纸张配色（羊皮纸/纯白/暖黄/护眼绿/夜间/暗黑） |
| 🔄 翻页模式 | 仿真/覆盖/滑动/上下滚动，支持点击翻页 + 手势 |
| 💾 进度记忆 | Room 数据库持久化阅读进度、章节、时长，重新打开自动续读 |
| ✨ 非线性动画 | 所有过渡用 spring / cubic-bezier 缓动曲线，丝滑不卡顿 |

---

## 🛠️ 技术栈

- **语言**：Kotlin 2.0.20
- **UI**：Jetpack Compose（Material 3）
- **数据库**：Room（KSP）
- **状态管理**：StateFlow + ViewModel
- **设置持久化**：DataStore Preferences
- **架构**：MVVM + Repository 模式
- **最低版本**：Android 8.0（API 26）
- **目标版本**：Android 14（API 34）

---

## 📦 项目结构

```
app/src/main/java/com/elegant/reader/
├── MainActivity.kt              # 入口 Activity
├── ReaderApplication.kt         # Application，持有数据库/仓库单例
├── data/                        # 数据层
│   ├── Book.kt                  # 书籍实体（Room）
│   ├── BookDao.kt               # DAO
│   ├── AppDatabase.kt           # 数据库
│   ├── BookRepository.kt        # 仓库
│   └── BookImporter.kt          # 导入服务
├── parse/                       # 解析层
│   ├── ParsedBook.kt            # 解析模型
│   ├── BookParser.kt            # 统一入口
│   ├── TxtParser.kt             # TXT 解析（编码检测+章节识别）
│   └── EpubParser.kt            # EPUB 解析（OPF+spine+XHTML）
├── reader/                      # 阅读器核心
│   ├── ReaderSettings.kt        # 排版设置/主题/字体枚举
│   ├── ReaderSettingsStore.kt   # 设置持久化（DataStore）
│   ├── Paginator.kt             # 文本分页引擎
│   ├── ReaderContentRepository.kt # 章节加载
│   └── ReaderViewModel.kt       # 阅读器状态管理
└── ui/                          # UI 层
    ├── AppNavigation.kt         # 导航
    ├── Modifiers.kt             # 通用修饰符
    ├── animation/Motion.kt      # 非线性动画缓动曲线
    ├── theme/                   # 设计系统（颜色/主题）
    ├── shelf/                   # 书架界面
    └── reader/                  # 阅读器界面 + 设置面板
```

---

## 🚀 构建与运行

### 前置要求

- **Android Studio**（建议最新稳定版，Hedgehog 及以上）
- **JDK 17**
- **Android SDK**（compileSdk 34）

### 步骤

1. 用 Android Studio 打开项目根目录（`build.gradle.kts` 所在目录）
2. 等待 Gradle 同步完成（会自动下载依赖）
3. 连接 Android 设备或启动模拟器
4. 点击 Run ▶ 或执行：

```bash
./gradlew :app:assembleDebug
```

生成的 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`。

### 安装到手机

```bash
./gradlew :app:installDebug
# 或手动：
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## 📖 使用说明

1. 打开 App 进入书架
2. 点击右下角「+」按钮，从文件选择器选择 `.txt` 或 `.epub` 文件
3. 点击书籍封面进入阅读
4. 阅读时：
   - 点击屏幕**左侧 1/3** → 上一页
   - 点击屏幕**右侧 1/3** → 下一页
   - 点击屏幕**中间** → 呼出/隐藏菜单
   - 菜单中点击右上角图标 → 打开设置面板
5. 设置面板可调节字号、行间距、段间距、字体、主题、翻页模式

---

## 🎯 设计亮点

- **非线性动画**：统一封装在 `Motion.kt`，使用 `CubicBezierEasing` 定义标准/强调/回弹/翻页等缓动曲线，杜绝生硬的线性过渡
- **拟物质感**：书封卡片用渐变 + 书脊高光 + 上下压暗模拟精装书立体感，阴影层次丰富
- **分页性能**：`StaticLayout` 一次性测量 + 字符偏移切分，避免逐字测量，保证大文件流畅
- **智能章节识别**：正则覆盖中文序号（一二三/123）、英文 Chapter、序言/楔子/尾声/番外等常见结构

---

## ⚠️ 已知限制

- 当前为**纯本地**应用，无云同步（架构已预留扩展点）
- EPUB 封面图片暂未渲染到书架（书架用渐变封面），后续可补
- 上下滚动模式（SCROLL）已枚举预留，翻页动画以仿真/覆盖/滑动为主

---

## 📝 License

个人学习/自用项目。
