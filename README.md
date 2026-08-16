# 附近 POI 搜索安卓应用（poi-search）

一个基于**高德地图（AMap）真实数据**的 Android 应用，用于按关键词 / 分类 / 范围搜索周边兴趣点（POI），并支持地图展示、网格化海量抓取、路线规划、离线地图、收藏、历史与 Excel 导出。

> ⚠️ 说明：高德 Web 服务 Key **硬编码**在 `app/src/main/java/com/example/poisearch/service/POISearchService.java`（`AMAP_KEY` 常量）。如需自行替换，直接改该常量即可，无需配置界面。

## 功能特点

### 核心搜索
- 🔍 **关键词搜索**：调用高德 `v3/place/text` 接口，按名称搜索
- 📍 **周边搜索**：调用高德 `v3/place/around` 接口，以某点为圆心搜索
- 🔲 **网格分片搜索**（`GridSearchService`）：将大范围切成 **800 米**小网格逐一搜索再合并去重，**突破高德单接口最多 200 条的限制**，适合大半径（最高 50km）的海量抓取
- 🏷️ **分类体系**（`POICategory`）：20+ 大类、200+ 子类，配独立分类图标

### 地图展示（`AMapActivity`）
- 🗺️ 高德地图（3D Map SDK）展示所有 POI
- 🎨 距离颜色标记：🟢 <500m / 🟡 500–1500m / 🔴 >1500m
- ⭕ 搜索范围圈 + 自动缩放到合适级别
- 📍 点击标记查看详情，支持自定义搜索中心（当前定位或手动经纬度）

### 路线规划（`RouteActivity`）✅
- 🚗 三种出行方式：**驾车 / 步行 / 骑行**（切换调用高德 `v3/direction`）
- 🧭 地图绘制路线 polyline + 起点/终点标记
- 📋 **分段导航步骤**列表（`RouteStep` + `RouteStepAdapter`）
- 📲 一键「打开高德 APP 导航」，未安装则降级到网页版高德
- 终点为所选 POI，起点为全局当前定位

### 离线地图（`OfflineMapActivity`）
- 📥 内置 **24 个热门城市**列表，下载/管理高德离线地图包

### 数据管理与导出
- ⭐ **收藏**（`FavoriteActivity` + `FavoriteManager`，SQLite）
- 🕘 **搜索历史**（`HistoryActivity` + `HistoryManager`，SQLite）
- 📊 **Excel 导出**（`ExcelExporter`，Apache POI）：名称 / 类别 / 地址 / 电话 / 坐标 / 距离 / 评分，可分享

### 其他
- 📞 列表/详情一键拨号
- 🎨 Material Design 风格界面
- 🔐 定位权限（精确定位 + 粗略定位）

## 项目结构

```
app/src/main/java/com/example/poisearch/
├── MainActivity.java              # 主界面（搜索框/分类/范围/位置/导出入口）
├── AMapActivity.java              # 地图展示
├── RouteActivity.java             # 路线规划（驾车/步行/骑行 + 分段步骤）
├── OfflineMapActivity.java        # 离线地图下载（24 城市）
├── POIDetailActivity.java         # POI 详情
├── FavoriteActivity.java          # 收藏列表
├── HistoryActivity.java           # 搜索历史
├── POIDataHolder.java             # 全局 POI / 当前位置持有
├── model/
│   ├── POI.java                   # POI 数据模型
│   ├── POICategory.java           # 分类模型
│   ├── RouteStep.java             # 路线分段步骤
│   └── SearchHistory.java         # 历史记录模型
├── service/
│   ├── POISearchService.java      # 关键词/周边搜索（高德 Web API）
│   └── GridSearchService.java     # 网格分片搜索（突破 200 条限制）
├── adapter/
│   ├── POIAdapter.java            # 搜索结果列表
│   ├── RouteStepAdapter.java      # 路线分段步骤
│   ├── HistoryAdapter.java        # 历史列表
│   ├── OfflineCityAdapter.java    # 离线地图城市
│   └── SimpleCityAdapter.java     # 通用城市列表
├── database/
│   ├── DatabaseHelper.java         # SQLite 辅助
│   ├── FavoriteManager.java        # 收藏管理
│   └── HistoryManager.java         # 历史管理
├── dialog/
│   ├── CategoryDialog.java         # 分类选择
│   ├── CategoryDetailDialog.java   # 子分类选择
│   ├── RadiusDialog.java           # 搜索范围（0.5–50km）
│   ├── LocationDialog.java         # 定位/自定义坐标
│   └── ExportSettingsDialog.java   # 导出设置
└── utils/
    └── ExcelExporter.java          # Excel 导出（Apache POI）

app/src/main/res/layout/
├── activity_main.xml / activity_amap.xml / activity_route.xml
├── activity_offline_map.xml / activity_favorite.xml / activity_history.xml
├── item_poi.xml / item_route_step.xml / item_history.xml
└── dialog_*.xml / item_category_*.xml
```

## 技术栈

- **语言**：Java
- **最低 SDK**：Android 8.0（API 26）
- **目标 / 编译 SDK**：Android 14（API 34）
- **地图**：高德 Android 3D Map SDK `com.amap.api:3dmap:10.0.600`
- **网络**：Volley（`com.android.volley:volley:1.2.1`）
- **JSON**：Gson（`com.google.code.gson:gson:2.10.1`）
- **Excel 导出**：Apache POI 5.2.3（`poi` + `poi-ooxml`）
- **UI**：AndroidX（AppCompat / RecyclerView / CardView / ConstraintLayout / CoordinatorLayout），Material Design

## 权限

- `INTERNET` — 访问高德网络服务
- `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` — 定位
- `ACCESS_NETWORK_STATE` — 网络状态
- `CALL_PHONE` — 一键拨号
- `WRITE_EXTERNAL_STORAGE` / `READ_EXTERNAL_STORAGE` — 导出 Excel（旧版 Android）

## 构建与运行

### 环境要求
- Android Studio（Hedgehog 及以上）或命令行 + JDK 17
- Android SDK Platform 34、Build-Tools 34.0.0
- 高德 3D Map SDK 需要 `AndroidManifest.xml` 中配置 `com.amap.api.v2.apikey` meta-data

### 命令行构建

```bash
# 构建 Release（默认未配置签名，可用 debug keystore 对齐签名后安装）
./gradlew assembleRelease

# 构建并安装 Debug
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

> ⚠️ `build.gradle` 当前 **release 未配置 signingConfig**。本地自测可用 debug keystore 对齐签名：
> ```bash
> $ANDROID_HOME/build-tools/<ver>/zipalign -p 4 \
>   app/build/outputs/apk/release/app-release-unsigned.apk aligned.apk
> $ANDROID_HOME/build-tools/<ver>/apksigner sign \
>   --ks ~/.android/debug.keystore --ks-key-alias androiddebugkey \
>   --ks-pass pass:android --key-pass pass:android --out poi-release.apk aligned.apk
> ```

### 替换高德 Key
编辑 `app/src/main/java/com/example/poisearch/service/POISearchService.java`：
```java
private static final String AMAP_KEY = "你的高德Web服务Key";
```
并在 `AndroidManifest.xml` 中同步地图 SDK 的 `com.amap.api.v2.apikey`。

## 使用说明

1. **首次启动**允许定位权限，首页会显示当前经纬度与地址
2. **搜索**：输入关键词或选分类 → 调范围（0.5–50km）→ 点搜索；大范围建议开启网格搜索以突破 200 条上限
3. **地图**：搜索后点「地图」查看 POI 分布与距离色标
4. **路线**：在 POI 详情/列表点「导航」→ 选择驾车/步行/骑行 → 看分段步骤 → 点「打开高德」唤起 APP 导航（未装降级网页版）
5. **离线地图**：下载常用城市离线包
6. **收藏 / 历史**：长按或列表操作收藏；历史自动记录
7. **导出 Excel**：导出搜索结果，可分享或存入 Documents

## 与原 README 的差异（已纠正）

- 旧版称「使用 Google Maps + 模拟数据」——**错误**。实际全程为**高德地图真实数据**，无模拟数据分支。
- 旧版「后续优化建议」中的地图/详情/收藏/历史/路线规划/离线地图，均已实现，不再属于待办。

## 许可证

MIT License
