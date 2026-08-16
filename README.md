# 附近POI搜索安卓应用

一个功能完整的Android应用程序，用于搜索附近的兴趣点（POI）。

## 功能特点

### 核心功能
- 📍 **定位功能**: 自动获取当前位置，搜索附近POI
- 🔍 **关键词搜索**: 支持按名称搜索地点
- 🏷️ **完整分类系统**: 支持20+大类、200+子分类的POI分类浏览

### 地图导航
- 🗺️ **地图展示**: Google Maps集成，直观展示所有POI位置
- 📍 **搜索范围圈**: 在地图上显示搜索范围圆圈
- 🎨 **距离颜色标记**: 
  - 🟢 绿色: < 500米
  - 🟡 黄色: 500-1500米
  - 🔴 红色: > 1500米
- 🔍 **缩放控制**: 根据搜索范围自动调整地图缩放级别

### 搜索范围
- 📏 **可调节范围**: 默认10公里，支持500米 - 50公里
- 🎯 **快速预设**: 500米、1公里、2公里、5公里、10公里、20公里、50公里
- 📊 **滑块调整**: 精确调整搜索范围

### 自定义定位
- 📍 **当前位置**: 使用GPS定位当前位置
- 📝 **自定义坐标**: 手动输入经纬度设定搜索中心
- 🏷️ **位置命名**: 为自定义位置添加名称

### 数据导出
- 📊 **Excel导出**: 将搜索结果导出为Excel文件(.xlsx)
- 📋 **完整信息**: 包含名称、类别、地址、电话、坐标、距离、评分
- 📤 **文件分享**: 支持通过邮件、微信等方式分享Excel文件
- 💾 **自动保存**: 文件保存到Documents/POI导出目录

### 其他功能
- 📞 **一键拨号**: 点击电话号码直接拨打
- 🎨 **分类图标**: 每个分类都有独特的图标标识
- 📱 **Material Design**: 采用Material Design 3设计风格

## 项目结构

```
app/src/main/java/com/example/poisearch/
├── MainActivity.java              # 主界面
├── model/
│   ├── POI.java                  # POI数据模型
│   └── POICategory.java          # POI分类模型（20+大类，200+子类）
├── service/
│   └── POISearchService.java     # POI搜索服务
├── adapter/
│   └── POIAdapter.java           # POI列表适配器
└── dialog/
    ├── CategoryDialog.java       # 分类选择对话框
    └── CategoryDetailDialog.java # 子分类详情对话框

app/src/main/res/
├── layout/
│   ├── activity_main.xml         # 主界面布局
│   ├── item_poi.xml              # POI列表项布局
│   ├── dialog_category.xml       # 分类选择对话框
│   ├── dialog_category_detail.xml # 子分类详情对话框
│   └── item_category_grid.xml    # 分类网格项
├── values/
│   ├── strings.xml               # 字符串资源
│   ├── colors.xml                # 颜色资源
│   └── themes.xml                # 主题样式
└── drawable/
    ├── circle_background.xml     # 圆形背景
    ├── dialog_background.xml     # 对话框背景
    ├── category_icon_background.xml # 分类图标背景
    ├── ic_launcher_foreground.xml # 应用图标
    └── ic_category_*.xml         # 各类分类图标（20个）
```

## 技术栈

- **开发语言**: Java
- **最低SDK**: Android 7.0 (API 24)
- **目标SDK**: Android 14 (API 34)
- **网络请求**: Volley
- **JSON解析**: Gson
- **定位服务**: Google Play Services Location
- **地图服务**: Google Play Services Maps
- **Excel导出**: Apache POI
- **UI组件**: Material Design 3

## 配置说明

### 1. 高德地图API配置（可选）

默认使用模拟数据。如需接入真实POI数据：

1. 前往 [高德地图开放平台](https://lbs.amap.com/) 注册并创建应用
2. 获取Web服务API Key
3. 修改 `POISearchService.java` 中的 `AMAP_KEY` 常量
4. 将 `USE_MOCK_DATA` 设置为 `false`

```java
private static final String AMAP_KEY = "你的高德地图API Key";
private static final boolean USE_MOCK_DATA = false;
```

### 2. Google Maps API配置

如需使用地图功能：

1. 前往 [Google Cloud Console](https://console.cloud.google.com/) 创建项目
2. 启用 Google Maps Android API
3. 创建API密钥
4. 在 `AndroidManifest.xml` 中替换 `YOUR_GOOGLE_MAPS_API_KEY`：

```xml
<meta-data
    android:name="com.google.android.geo.API_KEY"
    android:value="你的Google Maps API Key" />
```

### 3. 权限说明

应用需要以下权限：
- `INTERNET` - 网络访问
- `ACCESS_FINE_LOCATION` - 精确定位
- `ACCESS_COARSE_LOCATION` - 粗略定位
- `ACCESS_NETWORK_STATE` - 网络状态
- `WRITE_EXTERNAL_STORAGE` - 导出Excel文件（Android 9及以下）
- `READ_EXTERNAL_STORAGE` - 读取文件（Android 9及以下）

## 构建和运行

### 使用Android Studio

1. 打开项目文件夹
2. 等待Gradle同步完成
3. 连接设备或启动模拟器
4. 点击运行按钮

### 使用命令行

```bash
# 构建Debug版本
./gradlew assembleDebug

# 安装到设备
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 使用说明

### 基本搜索
1. 首次启动时会请求位置权限，请允许以获取附近POI
2. 在搜索框输入关键词，点击搜索按钮或按回车键
3. 点击"选择分类"按钮打开分类选择对话框
4. 选择主分类后，可以进一步选择子分类（如：餐饮 → 火锅店）

### 调整搜索范围
1. 点击"范围"按钮打开范围选择对话框
2. 使用滑块调整范围（0.5 - 50公里）
3. 或点击预设按钮快速选择
4. 默认范围为10公里

### 自定义搜索位置
1. 点击"位置"按钮打开位置设置对话框
2. 选择"使用当前位置"或"自定义位置"
3. 自定义位置时输入经纬度坐标
4. 可以为位置添加名称便于识别

### 地图导航
1. 搜索完成后，点击"地图导航"按钮
2. 在地图上查看所有POI位置
3. 不同颜色标记表示不同距离
4. 蓝色圆圈表示搜索范围
5. 点击标记查看POI详情

### 导出Excel
1. 搜索完成后，点击"导出Excel"按钮
2. 等待导出完成
3. 可选择直接分享文件
4. 文件保存在 Documents/POI导出/ 目录

### 其他操作
- 点击列表项查看详情
- 点击电话号码可直接拨打
- 点击地址可调用地图导航

## 模拟数据

应用默认使用模拟数据，包含45+个真实POI，覆盖以下分类：

| 分类 | 示例POI |
|------|---------|
| 🍽️ 餐饮 | 肯德基、麦当劳、星巴克、全聚德、海底捞、必胜客、喜茶、东来顺等 |
| 🛍️ 购物 | 万达广场、沃尔玛、7-Eleven、苏宁易购、ZARA、屈臣氏、新华书店等 |
| 🛎️ 生活服务 | 顺丰快递、洗衣店、家政服务、房屋中介等 |
| ⚽ 体育休闲 | 健身房、电影院、KTV、游泳馆、羽毛球馆、公园、酒吧等 |
| 🏥 医疗 | 协和医院、同仁堂药店、口腔医院、社区卫生服务中心等 |
| 🏨 酒店 | 如家、汉庭、希尔顿、7天等 |
| 🏞️ 景点 | 故宫、天坛公园、雍和宫等 |
| 🚗 交通 | 地铁站、公交站、加油站、停车场等 |
| 💰 金融 | 建设银行、工商银行、ATM、中信证券等 |
| 🎓 教育 | 北京大学、人大附中、新东方、国家图书馆等 |
| 🏢 企业 | 腾讯北京、百度大厦等 |
| 🏛️ 公共设施 | 公共厕所、充电桩等 |

所有POI都带有真实的地址、电话、评分和距离信息。

## 后续优化建议

- 接入真实地图API获取实时POI数据
- 添加地图展示界面
- 实现POI详情页面
- 添加收藏功能
- 支持历史记录
- 添加路线规划功能
- 支持离线地图

## 许可证

MIT License
