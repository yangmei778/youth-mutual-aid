/**
 * 列表页通用逻辑 composable
 * 封装筛选、分页、加载状态，减少 List 页面间的重复代码
 *
 * @param {Function} fetchFn  - 接收 params 对象的 API 函数
 * @param {Object}   defaults - 默认筛选参数
 * @returns {{ list, total, loading, filters, pagination, loadList, handleSearch, resetFilters }}
 */
import { ref, reactive, onMounted } from 'vue'

export function useListPage(fetchFn, defaults = {}) {
  const list = ref([])
  const total = ref(0)
  const loading = ref(false)

  const filters = reactive({
    keyword: '',
    category: '',
    type: '',
    city: '',
    sortBy: '',
    exchangeType: '',
    status: '',
    ...defaults,
  })

  const pagination = reactive({
    page: 1,
    pageSize: 12,
  })

  /**
   * 加载列表数据
   * 自动合并 filters 和 pagination，清除空值参数
   */
  async function loadList(extraParams) {
    loading.value = true
    try {
      const params = {
        pageNum: pagination.page,
        pageSize: pagination.pageSize,
        ...filters,
        ...extraParams,
      }
      // 清除空值参数，避免传空字符串到后端
      Object.keys(params).forEach(k => {
        if (params[k] === '' || params[k] === null || params[k] === undefined) {
          delete params[k]
        }
      })
      const res = await fetchFn(params)
      list.value = res.data?.list || res.data?.records || []
      total.value = res.data?.total || 0
    } catch {
      list.value = []
      total.value = 0
    } finally {
      loading.value = false
    }
  }

  /** 搜索：重置页码后加载 */
  function handleSearch() {
    pagination.page = 1
    loadList()
  }

  /** 重置筛选条件 */
  function resetFilters() {
    Object.keys(filters).forEach(k => {
      filters[k] = ''
    })
    pagination.page = 1
    loadList()
  }

  /** 切换每页条数：重置页码后加载 */
  function handleSizeChange(size) {
    pagination.pageSize = size
    pagination.page = 1
    loadList()
  }

  /** 翻页：直接加载（页码已由 v-model 更新） */
  function handlePageChange() {
    loadList()
  }

  onMounted(() => loadList())

  return { list, total, loading, filters, pagination, loadList, handleSearch, resetFilters, handleSizeChange, handlePageChange }
}
