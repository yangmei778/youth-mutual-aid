/**
 * 项目公共常量
 */

/** 可选城市列表 */
export const CITIES = ['北京', '上海', '广州', '深圳', '杭州', '成都', '武汉', '南京', '重庆', '西安']

/** 技能分类 */
export const SKILL_CATEGORIES = ['编程', '语言', '音乐', '健身', '绘画', '摄影']

/** 物品分类 */
export const GOODS_CATEGORIES = ['数码', '书籍', '家居', '服饰', '运动', '食品']

/** 技能热门分类 */
export const HOT_SKILL_CATS = ['编程', '语言', '音乐', '健身', '绘画', '摄影']

/** 物品热门分类 */
export const HOT_GOODS_CATS = ['数码', '书籍', '家居', '服饰', '运动', '食品']

/** 活动热门类型 */
export const HOT_ACTIVITY_TYPES = [
  { value: 'meal', label: '🍜 拼饭' },
  { value: 'exhibition', label: '🎨 拼展' },
  { value: 'travel', label: '✈️ 拼旅行' },
  { value: 'other', label: '🤝 其他' },
]

/** 物品交换方式映射 */
export const EXCHANGE_TYPE_MAP = {
  sell: '出售',
  borrow: '借用',
  gift: '赠送',
  exchange: '交换',
}

/** 活动状态映射 */
export const ACTIVITY_STATUS_MAP = {
  open: '报名中',
  full: '已满员',
  ongoing: '进行中',
}

/** 活动状态 Tag 类型 */
export const ACTIVITY_STATUS_TAG = {
  open: 'success',
  full: 'warning',
  ongoing: 'primary',
}
