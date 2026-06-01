/**
 * 百度地图配置
 *
 * 获取 API Key（AK）步骤：
 * 1. 打开 https://lbsyun.baidu.com/
 * 2. 注册/登录百度账号
 * 3. 控制台 → 创建应用 → 应用类型选「浏览器端」
 * 4. 将生成的 AK 填入下方 BAIDU_MAP_AK
 */
export const BAIDU_MAP_AK = 'hiNhXqCu58IvZ9IBS1nnBS6tfZJ2GGbu' // ← 把 AK 填在这里，例如：'rGmXxxxxxxxxxxxxxxxxxxxxxxx'

/** 百度地图 JS API 地址 */
export const BAIDU_MAP_SCRIPT = 'https://api.map.baidu.com/api?v=3.0&ak=' + BAIDU_MAP_AK + '&callback=onBaiduMapLoaded'
