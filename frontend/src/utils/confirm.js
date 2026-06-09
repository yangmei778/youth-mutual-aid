import { createVNode, render } from 'vue'
import ConfirmDialog from '@/components/ConfirmDialog.vue'

/**
 * 增强确认弹窗——自定义组件，完全可控样式
 * @param {string} message 提示文字
 * @param {string} title 标题
 * @param {string} type 'warning'|'danger'
 * @returns {Promise<boolean>} true=确认，false=取消
 */
export function showConfirm(message, title = '请确认', type = 'warning') {
  return new Promise((resolve) => {
    const container = document.createElement('div')
    document.body.appendChild(container)

    const vnode = createVNode(ConfirmDialog, {
      message, title, type,
      onResolve: (val) => {
        resolve(val)
        render(null, container)
        document.body.removeChild(container)
      }
    })

    render(vnode, container)
  })
}
