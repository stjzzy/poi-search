package com.example.poisearch.model;

import com.example.poisearch.R;

import java.util.ArrayList;
import java.util.List;

public class POICategory {
    private String code;
    private String name;
    private String icon;
    private int iconResId;
    private List<POICategory> subCategories;

    public POICategory(String code, String name, String icon, int iconResId) {
        this.code = code;
        this.name = name;
        this.icon = icon;
        this.iconResId = iconResId;
        this.subCategories = new ArrayList<>();
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getIcon() { return icon; }
    public int getIconResId() { return iconResId; }
    public List<POICategory> getSubCategories() { return subCategories; }

    public void addSubCategory(POICategory sub) {
        subCategories.add(sub);
    }

    // 获取所有POI分类
    public static List<POICategory> getAllCategories() {
        List<POICategory> categories = new ArrayList<>();

        // 餐饮服务
        POICategory food = new POICategory("050000", "餐饮", "🍽️", R.drawable.ic_category_food);
        food.addSubCategory(new POICategory("050100", "中餐厅", "🥢", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("050200", "外国餐厅", "🍴", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("050300", "快餐厅", "🍔", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("050400", "休闲餐饮", "☕", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("050500", "咖啡厅", "☕", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("050600", "茶座", "🍵", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("050700", "甜品店", "🍰", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("050800", "面包店", "🥐", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("050900", "冷饮店", "🍦", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("051000", "小吃店", "🥟", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("051100", "火锅店", "🍲", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("051200", "烧烤店", "🍖", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("051300", "海鲜店", "🦐", R.drawable.ic_category_food));
        food.addSubCategory(new POICategory("051400", "素食店", "🥗", R.drawable.ic_category_food));
        categories.add(food);

        // 购物服务
        POICategory shopping = new POICategory("060000", "购物", "🛍️", R.drawable.ic_category_shopping);
        shopping.addSubCategory(new POICategory("060100", "综合商场", "🏬", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("060200", "超市", "🛒", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("060300", "便利店", "🏪", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("060400", "家电数码", "📱", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("060500", "家具建材", "🪑", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("060600", "服装鞋帽", "👔", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("060700", "箱包", "👜", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("060800", "钟表眼镜", "⌚", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("060900", "珠宝首饰", "💎", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("061000", "化妆品", "💄", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("061100", "母婴用品", "👶", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("061200", "烟酒专卖", "🍷", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("061300", "食品店", "🥫", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("061400", "书店", "📚", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("061500", "音像店", "📀", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("061600", "花店", "💐", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("061700", "宠物用品", "🐾", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("061800", "运动户外", "⚽", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("061900", "古玩字画", "🏺", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("062000", "自行车专卖", "🚲", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("062100", "礼品店", "🎁", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("062200", "文具店", "✏️", R.drawable.ic_category_shopping));
        shopping.addSubCategory(new POICategory("062300", "农贸市场", "🥬", R.drawable.ic_category_shopping));
        categories.add(shopping);

        // 生活服务
        POICategory lifeService = new POICategory("070000", "生活服务", "🛎️", R.drawable.ic_category_service);
        lifeService.addSubCategory(new POICategory("070100", "通讯服务", "📞", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("070200", "邮局", "📮", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("070300", "物流快递", "📦", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("070400", "洗衣店", "👕", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("070500", "家政服务", "🧹", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("070600", "摄影冲印", "📷", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("070700", "彩票销售", "🎰", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("070800", "报刊亭", "📰", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("070900", "自来水", "💧", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("071000", "电力", "⚡", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("071100", "燃气", "🔥", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("071200", "供热", "🌡️", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("071300", "维修点", "🔧", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("071400", "开锁", "🔑", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("071500", "殡葬", "⚰️", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("071600", "人才市场", "💼", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("071700", "搬家公司", "🚚", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("071800", "裁缝店", "🧵", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("071900", "配钥匙", "🗝️", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("072000", "管道疏通", "🚿", R.drawable.ic_category_service));
        lifeService.addSubCategory(new POICategory("072100", "房屋中介", "🏠", R.drawable.ic_category_service));
        categories.add(lifeService);

        // 体育休闲
        POICategory sports = new POICategory("080000", "体育休闲", "⚽", R.drawable.ic_category_sports);
        sports.addSubCategory(new POICategory("080100", "运动场馆", "🏟️", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("080200", "高尔夫", "⛳", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("080300", "游泳馆", "🏊", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("080400", "健身中心", "💪", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("080500", "瑜伽", "🧘", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("080600", "羽毛球", "🏸", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("080700", "乒乓球", "🏓", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("080800", "篮球场", "🏀", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("080900", "足球场", "⚽", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("081000", "网球场", "🎾", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("081100", "台球", "🎱", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("081200", "滑雪场", "⛷️", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("081300", "溜冰场", "⛸️", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("081400", "赛马场", "🏇", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("081500", "赛车", "🏎️", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("081600", "射击射箭", "🎯", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("081700", "公园", "🌳", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("081800", "游乐场", "🎡", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("081900", "垂钓园", "🎣", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("082000", "采摘园", "🍓", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("082100", "露营地", "⛺", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("082200", "温泉", "♨️", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("082300", "度假村", "🏖️", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("082400", "农家乐", "🌾", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("082500", "电影院", "🎬", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("082600", "KTV", "🎤", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("082700", "剧院", "🎭", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("082800", "歌舞厅", "💃", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("082900", "网吧", "💻", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("083000", "棋牌室", "🀄", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("083100", "桌游", "🎲", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("083200", "密室逃脱", "🔐", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("083300", "酒吧", "🍸", R.drawable.ic_category_sports));
        sports.addSubCategory(new POICategory("083400", "茶馆", "🍵", R.drawable.ic_category_sports));
        categories.add(sports);

        // 医疗保健
        POICategory medical = new POICategory("090000", "医疗", "🏥", R.drawable.ic_category_medical);
        medical.addSubCategory(new POICategory("090100", "综合医院", "🏥", R.drawable.ic_category_medical));
        medical.addSubCategory(new POICategory("090200", "专科医院", "🏥", R.drawable.ic_category_medical));
        medical.addSubCategory(new POICategory("090300", "诊所", "🩺", R.drawable.ic_category_medical));
        medical.addSubCategory(new POICategory("090400", "急救中心", "🚑", R.drawable.ic_category_medical));
        medical.addSubCategory(new POICategory("090500", "药房", "💊", R.drawable.ic_category_medical));
        medical.addSubCategory(new POICategory("090600", "疾控中心", "🦠", R.drawable.ic_category_medical));
        medical.addSubCategory(new POICategory("090700", "体检中心", "🩻", R.drawable.ic_category_medical));
        medical.addSubCategory(new POICategory("090800", "疗养院", "🏨", R.drawable.ic_category_medical));
        medical.addSubCategory(new POICategory("090900", "兽医", "🐕", R.drawable.ic_category_medical));
        categories.add(medical);

        // 住宿服务
        POICategory hotel = new POICategory("100000", "酒店", "🏨", R.drawable.ic_category_hotel);
        hotel.addSubCategory(new POICategory("100100", "星级酒店", "⭐", R.drawable.ic_category_hotel));
        hotel.addSubCategory(new POICategory("100200", "快捷酒店", "🛏️", R.drawable.ic_category_hotel));
        hotel.addSubCategory(new POICategory("100300", "经济型酒店", "💰", R.drawable.ic_category_hotel));
        hotel.addSubCategory(new POICategory("100400", "青年旅舍", "🎒", R.drawable.ic_category_hotel));
        hotel.addSubCategory(new POICategory("100500", "民宿", "🏡", R.drawable.ic_category_hotel));
        hotel.addSubCategory(new POICategory("100600", "招待所", "🛌", R.drawable.ic_category_hotel));
        hotel.addSubCategory(new POICategory("100700", "宾馆", "🏢", R.drawable.ic_category_hotel));
        categories.add(hotel);

        // 风景名胜
        POICategory scenic = new POICategory("110000", "景点", "🏞️", R.drawable.ic_category_scenic);
        scenic.addSubCategory(new POICategory("110100", "公园", "🌳", R.drawable.ic_category_scenic));
        scenic.addSubCategory(new POICategory("110200", "世界遗产", "🏛️", R.drawable.ic_category_scenic));
        scenic.addSubCategory(new POICategory("110300", "国家级景点", "🇨🇳", R.drawable.ic_category_scenic));
        scenic.addSubCategory(new POICategory("110400", "省级景点", "🗿", R.drawable.ic_category_scenic));
        scenic.addSubCategory(new POICategory("110500", "纪念馆", "🏛️", R.drawable.ic_category_scenic));
        scenic.addSubCategory(new POICategory("110600", "寺庙", "⛩️", R.drawable.ic_category_scenic));
        scenic.addSubCategory(new POICategory("110700", "教堂", "⛪", R.drawable.ic_category_scenic));
        scenic.addSubCategory(new POICategory("110800", "清真寺", "🕌", R.drawable.ic_category_scenic));
        scenic.addSubCategory(new POICategory("110900", "海滩", "🏖️", R.drawable.ic_category_scenic));
        scenic.addSubCategory(new POICategory("111000", "观景点", "👁️", R.drawable.ic_category_scenic));
        categories.add(scenic);

        // 商务住宅
        POICategory business = new POICategory("120000", "商务住宅", "🏢", R.drawable.ic_category_business);
        business.addSubCategory(new POICategory("120100", "产业园区", "🏭", R.drawable.ic_category_business));
        business.addSubCategory(new POICategory("120200", "写字楼", "🏢", R.drawable.ic_category_business));
        business.addSubCategory(new POICategory("120300", "住宅区", "🏘️", R.drawable.ic_category_business));
        business.addSubCategory(new POICategory("120400", "别墅", "🏰", R.drawable.ic_category_business));
        business.addSubCategory(new POICategory("120500", "商住两用", "🏠", R.drawable.ic_category_business));
        categories.add(business);

        // 政府机构
        POICategory government = new POICategory("130000", "政府", "🏛️", R.drawable.ic_category_government);
        government.addSubCategory(new POICategory("130100", "政府机关", "🏛️", R.drawable.ic_category_government));
        government.addSubCategory(new POICategory("130200", "行政单位", "📋", R.drawable.ic_category_government));
        government.addSubCategory(new POICategory("130300", "公检法", "⚖️", R.drawable.ic_category_government));
        government.addSubCategory(new POICategory("130400", "大使馆", "🌐", R.drawable.ic_category_government));
        government.addSubCategory(new POICategory("130500", "社会团体", "🤝", R.drawable.ic_category_government));
        categories.add(government);

        // 科教文化
        POICategory education = new POICategory("140000", "科教", "🎓", R.drawable.ic_category_education);
        education.addSubCategory(new POICategory("140100", "大学", "🎓", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("140200", "中学", "📚", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("140300", "小学", "✏️", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("140400", "幼儿园", "🧸", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("140500", "培训机构", "📖", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("140600", "驾校", "🚗", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("140700", "图书馆", "📚", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("140800", "博物馆", "🏛️", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("140900", "科技馆", "🔬", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("141000", "美术馆", "🎨", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("141100", "展览馆", "🏛️", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("141200", "天文馆", "🔭", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("141300", "档案馆", "📁", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("141400", "文化馆", "🎭", R.drawable.ic_category_education));
        education.addSubCategory(new POICategory("141500", "研究中心", "🔍", R.drawable.ic_category_education));
        categories.add(education);

        // 交通设施
        POICategory transport = new POICategory("150000", "交通", "🚗", R.drawable.ic_category_transport);
        transport.addSubCategory(new POICategory("150100", "机场", "✈️", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("150200", "火车站", "🚂", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("150300", "地铁站", "🚇", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("150400", "公交站", "🚌", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("150500", "长途汽车站", "🚌", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("150600", "港口码头", "⚓", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("150700", "停车场", "🅿️", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("150800", "加油站", "⛽", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("150900", "充电站", "🔌", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("151000", "加气站", "⛽", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("151100", "服务区", "🛣️", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("151200", "收费站", "💰", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("151300", "汽车租赁", "🚙", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("151400", "汽车维修", "🔧", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("151500", "洗车", "🚿", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("151600", "租车", "🚗", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("151700", "驾校", "🚗", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("151800", "自行车租赁", "🚲", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("151900", "轮渡", "⛴️", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("152000", "索道", "🚡", R.drawable.ic_category_transport));
        transport.addSubCategory(new POICategory("152100", "观光车", "🚃", R.drawable.ic_category_transport));
        categories.add(transport);

        // 金融保险
        POICategory finance = new POICategory("160000", "金融", "💰", R.drawable.ic_category_finance);
        finance.addSubCategory(new POICategory("160100", "银行", "🏦", R.drawable.ic_category_finance));
        finance.addSubCategory(new POICategory("160200", "ATM", "🏧", R.drawable.ic_category_finance));
        finance.addSubCategory(new POICategory("160300", "证券公司", "📈", R.drawable.ic_category_finance));
        finance.addSubCategory(new POICategory("160400", "保险公司", "🛡️", R.drawable.ic_category_finance));
        finance.addSubCategory(new POICategory("160500", "财务公司", "💼", R.drawable.ic_category_finance));
        finance.addSubCategory(new POICategory("160600", "典当行", "💎", R.drawable.ic_category_finance));
        categories.add(finance);

        // 公司企业
        POICategory company = new POICategory("170000", "企业", "🏢", R.drawable.ic_category_company);
        company.addSubCategory(new POICategory("170100", "公司", "🏢", R.drawable.ic_category_company));
        company.addSubCategory(new POICategory("170200", "工厂", "🏭", R.drawable.ic_category_company));
        company.addSubCategory(new POICategory("170300", "农林牧渔", "🌾", R.drawable.ic_category_company));
        company.addSubCategory(new POICategory("170400", "建筑公司", "🏗️", R.drawable.ic_category_company));
        categories.add(company);

        // 道路附属
        POICategory road = new POICategory("180000", "道路", "🛣️", R.drawable.ic_category_road);
        road.addSubCategory(new POICategory("180100", "地名", "📍", R.drawable.ic_category_road));
        road.addSubCategory(new POICategory("180200", "路口", "🚦", R.drawable.ic_category_road));
        road.addSubCategory(new POICategory("180300", "桥梁", "🌉", R.drawable.ic_category_road));
        road.addSubCategory(new POICategory("180400", "隧道", "🚇", R.drawable.ic_category_road));
        road.addSubCategory(new POICategory("180500", "环岛", "🔄", R.drawable.ic_category_road));
        categories.add(road);

        // 公共设施
        POICategory publicFacility = new POICategory("190000", "公共设施", "🏛️", R.drawable.ic_category_public);
        publicFacility.addSubCategory(new POICategory("190100", "公共厕所", "🚻", R.drawable.ic_category_public));
        publicFacility.addSubCategory(new POICategory("190200", "公用电话", "📞", R.drawable.ic_category_public));
        publicFacility.addSubCategory(new POICategory("190300", "紧急避难", "🆘", R.drawable.ic_category_public));
        publicFacility.addSubCategory(new POICategory("190400", "充电桩", "🔌", R.drawable.ic_category_public));
        categories.add(publicFacility);

        return categories;
    }

    // 根据代码获取分类名称
    public static String getCategoryNameByCode(String code) {
        for (POICategory cat : getAllCategories()) {
            if (cat.getCode().equals(code)) {
                return cat.getName();
            }
            for (POICategory sub : cat.getSubCategories()) {
                if (sub.getCode().equals(code)) {
                    return sub.getName();
                }
            }
        }
        return "";
    }

    // 根据代码获取图标资源
    public static int getIconResByCode(String code) {
        for (POICategory cat : getAllCategories()) {
            if (cat.getCode().equals(code)) {
                return cat.getIconResId();
            }
            for (POICategory sub : cat.getSubCategories()) {
                if (sub.getCode().equals(code)) {
                    return sub.getIconResId();
                }
            }
        }
        return R.drawable.ic_category_default;
    }
}
