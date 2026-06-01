<template>
  <div class="map-view">
    <!-- 有 API Key：百度地图 -->
    <template v-if="hasBaiduKey">
      <div v-if="!location" class="map-placeholder">
        <el-icon :size="28"><MapLocation /></el-icon>
        <span>暂无位置信息</span>
      </div>
      <div v-else class="map-container">
        <div ref="baiduMapRef" class="baidu-map"></div>
        <div class="map-loading-mask" v-if="mapLoading">
          <el-icon class="is-loading" :size="24"><Loading /></el-icon>
          <span>地图加载中...</span>
        </div>
        <div v-if="mapError" class="map-error-mask">
          <span>{{ mapError }}</span>
        </div>
        <!-- 定位按钮 -->
        <div v-if="markerPoint" class="map-locate-btn" @click="panToMarker" title="定位到活动地点">
          <el-icon :size="18"><Aim /></el-icon>
        </div>
        <!-- 距离提示 -->
        <div v-if="distanceText" class="map-distance-badge">
          <el-icon :size="14"><Position /></el-icon>
          <span>{{ distanceText }}</span>
        </div>
      </div>
    </template>

    <!-- 无 API Key：OpenStreetMap 降级 -->
    <template v-else>
      <div v-if="!hasCoords" class="map-placeholder">
        <el-icon :size="28"><MapLocation /></el-icon>
        <span>{{ location || '暂无位置信息' }}</span>
        <small v-if="location">无法定位此地址，可配置百度地图AK获得更精准定位</small>
      </div>
      <div v-else class="map-container">
        <iframe :src="osmUrl" width="100%" height="100%"
          frameborder="0" style="border:0; border-radius:14px;"
          allowfullscreen loading="lazy"
        ></iframe>
        <div class="pin-label">
          <el-icon :size="14"><MapLocation /></el-icon>
          <span>{{ location }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { MapLocation, Loading, Aim, Position } from '@element-plus/icons-vue'
import { BAIDU_MAP_AK } from '@/config/baidu-map'

const props = defineProps({
  location: { type: String, default: '' },
})

const baiduMapRef = ref(null)
const mapLoading = ref(true)
const mapError = ref('')
const markerPoint = ref(null)
const distanceText = ref('')
let mapInstance = null

const hasBaiduKey = computed(() => !!BAIDU_MAP_AK)

// ====== OpenStreetMap 降级方案 ======
const cityCoords = {
  '北京': [116.407, 39.904], '上海': [121.473, 31.230], '广州': [113.264, 23.129],
  '深圳': [114.058, 22.543], '杭州': [120.155, 30.274], '成都': [104.066, 30.573],
  '武汉': [114.305, 30.593], '南京': [118.797, 32.061], '重庆': [106.551, 29.563],
  '西安': [108.940, 34.260], '天津': [117.190, 39.125], '长沙': [112.939, 28.228],
  '青岛': [120.383, 36.067], '大连': [121.615, 38.914], '厦门': [118.089, 24.480],
  '苏州': [120.585, 31.299], '郑州': [113.625, 34.747], '济南': [117.001, 36.651],
}

const hasCoords = computed(() => {
  if (!props.location) return false
  for (const city of Object.keys(cityCoords)) {
    if (props.location.includes(city)) return true
  }
  return false
})

const osmCoords = computed(() => {
  if (!props.location) return [116.4, 39.9]
  for (const [city, c] of Object.entries(cityCoords)) {
    if (props.location.includes(city)) return c
  }
  return [116.4, 39.9]
})

const osmUrl = computed(() => {
  const [lng, lat] = osmCoords.value
  const d = 0.025
  return `https://www.openstreetmap.org/export/embed.html?bbox=${lng-d},${lat-d},${lng+d},${lat+d}&layer=mapnik&marker=${lat},${lng}`
})

// ====== 百度地图 ======
let scriptLoaded = false
let scriptLoading = false

function loadBaiduScript() {
  return new Promise((resolve) => {
    if (window.BMap) { scriptLoaded = true; resolve(); return }
    if (scriptLoading) {
      const check = setInterval(() => { if (window.BMap) { clearInterval(check); resolve() } }, 200)
      return
    }
    scriptLoading = true
    window.onBaiduMapLoaded = () => { scriptLoaded = true; resolve() }
    const script = document.createElement('script')
    script.src = `https://api.map.baidu.com/api?v=3.0&ak=${BAIDU_MAP_AK}&callback=onBaiduMapLoaded`
    script.onerror = () => { mapError.value = '百度地图加载失败，请检查网络或API Key'; resolve() }
    document.head.appendChild(script)
  })
}

// 尝试从地址中提取城市名
function extractCity(location) {
  for (const city of Object.keys(cityCoords)) {
    if (location.includes(city)) return city
  }
  return null
}

async function initBaiduMap() {
  if (!props.location || !baiduMapRef.value) return
  mapLoading.value = true
  mapError.value = ''

  try {
    await loadBaiduScript()
    if (!window.BMap) return
    if (mapInstance) { mapInstance.clearOverlays() }

    const BMap = window.BMap
    const map = new BMap.Map(baiduMapRef.value)
    map.enableScrollWheelZoom(true)
    mapInstance = map

    const loc = props.location.trim()
    const city = extractCity(loc)

    // 先用城市坐标表定位，保证地图一定显示
    if (city && cityCoords[city]) {
      map.centerAndZoom(new BMap.Point(cityCoords[city][0], cityCoords[city][1]), 13)
    } else {
      map.centerAndZoom('北京', 12)
    }

    // 百度地理编码：精确地址 → 坐标
    const geocoder = new BMap.Geocoder()
    const cityContext = city || '全国'

    geocoder.getPoint(loc, function (point) {
      if (point) {
        // 地理编码成功 — 移动到精确坐标
        markerPoint.value = point
        map.clearOverlays()
        map.centerAndZoom(point, 16)
        const marker = new BMap.Marker(point)
        map.addOverlay(marker)
        marker.setAnimation(BMAP_ANIMATION_DROP)
        const infoWindow = new BMap.InfoWindow(
          '<div style="padding:4px 8px;font-size:14px"><strong>📍 ' + loc + '</strong></div>',
          { width: 220, enableMessage: false }
        )
        marker.addEventListener('click', function () {
          this.openInfoWindow(infoWindow)
        })
        map.openInfoWindow(infoWindow, point)
        showUserLocation(point)
        mapLoading.value = false
      } else {
        // 地理编码失败 — 用城市坐标表作为后备
        let placed = false
        if (city && cityCoords[city]) {
          map.clearOverlays()
          const pt = new BMap.Point(cityCoords[city][0], cityCoords[city][1])
          markerPoint.value = pt
          map.centerAndZoom(pt, 14)
          const marker = new BMap.Marker(pt)
          map.addOverlay(marker)
          const infoWindow = new BMap.InfoWindow(
            '<div style="padding:4px 8px;font-size:14px"><strong>📍 ' + loc + '</strong><br/><small>（近似位置：' + city + '）</small></div>',
            { width: 220, enableMessage: false }
          )
          map.openInfoWindow(infoWindow, pt)
          showUserLocation(pt)
          placed = true
        }
        if (!placed) mapError.value = '无法在地图上定位"' + loc + '"，请填写更详细的地址'
        mapLoading.value = false
      }
    }, cityContext)
  } catch (e) {
    mapError.value = '地图初始化失败'
    mapLoading.value = false
  }
}

function panToMarker() {
  if (!mapInstance || !markerPoint.value) return
  mapInstance.centerAndZoom(markerPoint.value, 16)
}

// 获取用户当前位置，标注并计算距离
function showUserLocation(activityPoint) {
  if (!navigator.geolocation || !mapInstance || !window.BMap) return

  navigator.geolocation.getCurrentPosition(
    function (pos) {
      const BMap = window.BMap
      const userLng = pos.coords.longitude
      const userLat = pos.coords.latitude

      // GPS坐标(WGS84)转百度坐标，使用BMap.Convertor（需要加载convertor扩展）
      // 直接用GPS坐标也能大致定位，偏移量在中国约几十到几百米，可接受
      const userPt = new BMap.Point(userLng, userLat)

      // 用户位置标记（蓝色圆点）
      const userIcon = new BMap.Icon(
        'data:image/svg+xml,' + encodeURIComponent(
          '<svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24">' +
          '<circle cx="12" cy="12" r="10" fill="#409eff" opacity="0.3"/>' +
          '<circle cx="12" cy="12" r="5" fill="#409eff" stroke="#fff" stroke-width="2"/>' +
          '</svg>'
        ), new BMap.Size(24, 24)
      )
      const userMarker = new BMap.Marker(userPt, { icon: userIcon })
      mapInstance.addOverlay(userMarker)

      // 用户位置标签
      const userLabel = new BMap.Label('📍 我的位置', {
        position: userPt, offset: new BMap.Size(15, -30)
      })
      userLabel.setStyle({
        background: '#409eff', color: '#fff', padding: '2px 8px',
        borderRadius: '6px', fontSize: '12px', border: 'none', whiteSpace: 'nowrap'
      })
      mapInstance.addOverlay(userLabel)

      // 画连线
      const polyline = new BMap.Polyline([userPt, activityPoint], {
        strokeColor: '#409eff', strokeWeight: 2, strokeOpacity: 0.5,
        strokeStyle: 'dashed'
      })
      mapInstance.addOverlay(polyline)

      // 计算距离
      const dist = mapInstance.getDistance(userPt, activityPoint).toFixed(0)
      if (dist < 1000) {
        distanceText.value = '距你约 ' + dist + ' 米'
      } else {
        distanceText.value = '距你约 ' + (dist / 1000).toFixed(1) + ' 公里'
      }
    },
    function () { /* 用户拒绝定位，静默处理 */ },
    { enableHighAccuracy: true, timeout: 5000, maximumAge: 60000 }
  )
}

watch(() => props.location, async (val) => {
  if (val && hasBaiduKey.value) {
    await nextTick()
    initBaiduMap()
  }
})

onMounted(async () => {
  if (hasBaiduKey.value && props.location) {
    await nextTick()
    initBaiduMap()
  }
})

onUnmounted(() => {
  if (mapInstance) { mapInstance.clearOverlays(); mapInstance = null }
})
</script>

<style scoped>
.map-view {
  min-height: 200px; border-radius: 14px;
  overflow: hidden; background: #f5f6f8; position: relative;
}

.map-placeholder {
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 4px; height: 200px; color: #909399; font-size: 14px;
  .el-icon { color: #c8d6e5; }
  small { font-size: 12px; color: #b0b8c4; margin-top: 4px; max-width: 280px; text-align: center; }
}

.map-container { width: 100%; height: 280px; position: relative; }

.baidu-map { width: 100%; height: 100%; border-radius: 14px; }

.map-loading-mask, .map-error-mask {
  position: absolute; inset: 0;
  display: flex; flex-direction: column; align-items: center; justify-content: center;
  gap: 8px; background: rgba(245,246,248,0.85); border-radius: 14px;
  font-size: 13px; color: #909399; z-index: 2;
}
.map-error-mask { color: #e6a23c; }

/* 定位按钮 */
.map-locate-btn {
  position: absolute; top: 12px; right: 12px; z-index: 3;
  width: 36px; height: 36px; display: flex; align-items: center; justify-content: center;
  background: #fff; border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
  cursor: pointer; transition: all 0.25s;
  color: var(--primary-color);

  &:hover { background: var(--primary-color); color: #fff; box-shadow: 0 4px 14px rgba(64,158,255,0.3); }
  &:active { transform: scale(0.92); }
}

/* 距离徽章 */
.map-distance-badge {
  position: absolute; top: 12px; left: 12px; z-index: 3;
  display: inline-flex; align-items: center; gap: 6px;
  padding: 6px 14px; background: #fff; border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.12);
  font-size: 13px; font-weight: 600; color: var(--primary-color);
  .el-icon { color: var(--primary-color); }
}

.pin-label {
  position: absolute; bottom: 14px; left: 14px; z-index: 1;
  display: inline-flex; align-items: center; gap: 6px;
  padding: 8px 16px; background: #fff; border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  font-size: 13px; color: var(--text-primary); font-weight: 500;
  .el-icon { color: #e6a23c; }
}
</style>
