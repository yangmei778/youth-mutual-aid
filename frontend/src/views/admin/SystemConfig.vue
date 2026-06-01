<template>
  <div class="system-config page-container">
    <h2 style="margin-bottom:20px">系统配置</h2>

    <!-- 技能分类 -->
    <el-card shadow="hover" class="config-card">
      <template #header><span>技能分类</span></template>
      <div class="tag-editor">
        <el-tag
          v-for="(cat, i) in skillCategories"
          :key="i"
          closable
          @close="removeCategory('skill', i)"
          class="config-tag"
        >{{ cat }}</el-tag>
        <el-input
          v-if="addingSkill"
          ref="skillInputRef"
          v-model="newSkillCat"
          size="small"
          style="width:100px"
          @keyup.enter="addCategory('skill')"
          @blur="addCategory('skill')"
        />
        <el-button v-else size="small" @click="startAdd('skill')">+ 添加</el-button>
      </div>
      <el-button type="primary" size="small" @click="saveConfig('skill_categories', skillCategories)" :loading="savingSkill">保存</el-button>
    </el-card>

    <!-- 物品分类 -->
    <el-card shadow="hover" class="config-card">
      <template #header><span>物品分类</span></template>
      <div class="tag-editor">
        <el-tag
          v-for="(cat, i) in goodsCategories"
          :key="i"
          closable
          @close="removeCategory('goods', i)"
          class="config-tag"
        >{{ cat }}</el-tag>
        <el-input
          v-if="addingGoods"
          ref="goodsInputRef"
          v-model="newGoodsCat"
          size="small"
          style="width:100px"
          @keyup.enter="addCategory('goods')"
          @blur="addCategory('goods')"
        />
        <el-button v-else size="small" @click="startAdd('goods')">+ 添加</el-button>
      </div>
      <el-button type="primary" size="small" @click="saveConfig('goods_categories', goodsCategories)" :loading="savingGoods">保存</el-button>
    </el-card>

    <!-- 活动类型 -->
    <el-card shadow="hover" class="config-card">
      <template #header><span>活动类型</span></template>
      <div class="tag-editor">
        <el-tag
          v-for="(t, i) in activityTypes"
          :key="i"
          closable
          @close="removeActivityType(i)"
          class="config-tag"
        >{{ t.label }}</el-tag>
        <template v-if="addingActivity">
          <el-input v-model="newActivityValue" size="small" placeholder="值" style="width:80px" />
          <el-input v-model="newActivityLabel" size="small" placeholder="标签" style="width:80px" @keyup.enter="addActivityType" />
        </template>
        <el-button v-else size="small" @click="addingActivity = true">+ 添加</el-button>
      </div>
      <el-button type="primary" size="small" @click="saveActivityTypes" :loading="savingActivity">保存</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { configApi } from '@/api'

// ── 技能分类 ──
const skillCategories = ref([])
const newSkillCat = ref('')
const addingSkill = ref(false)
const savingSkill = ref(false)

// ── 物品分类 ──
const goodsCategories = ref([])
const newGoodsCat = ref('')
const addingGoods = ref(false)
const savingGoods = ref(false)

// ── 活动类型 ──
const activityTypes = ref([])
const newActivityValue = ref('')
const newActivityLabel = ref('')
const addingActivity = ref(false)
const savingActivity = ref(false)

async function loadConfigs() {
  try {
    const res = await configApi.getAllConfig()
    const list = res.data?.data || res.data || []
    list.forEach(c => {
      try {
        const v = JSON.parse(c.configValue)
        if (c.configKey === 'skill_categories') skillCategories.value = v
        if (c.configKey === 'goods_categories') goodsCategories.value = v
        if (c.configKey === 'activity_types') activityTypes.value = v
      } catch { /* ignore parse error */ }
    })
  } catch { /* ignore */ }
}

function startAdd(type) {
  if (type === 'skill') { addingSkill.value = true; newSkillCat.value = '' }
  if (type === 'goods') { addingGoods.value = true; newGoodsCat.value = '' }
}

function addCategory(type) {
  if (type === 'skill' && newSkillCat.value.trim()) {
    skillCategories.value.push(newSkillCat.value.trim())
    newSkillCat.value = ''
    addingSkill.value = false
  }
  if (type === 'goods' && newGoodsCat.value.trim()) {
    goodsCategories.value.push(newGoodsCat.value.trim())
    newGoodsCat.value = ''
    addingGoods.value = false
  }
}

function removeCategory(type, i) {
  if (type === 'skill') skillCategories.value.splice(i, 1)
  if (type === 'goods') goodsCategories.value.splice(i, 1)
}

function addActivityType() {
  if (newActivityValue.value.trim() && newActivityLabel.value.trim()) {
    activityTypes.value.push({ value: newActivityValue.value.trim(), label: newActivityLabel.value.trim() })
    newActivityValue.value = ''
    newActivityLabel.value = ''
    addingActivity.value = false
  }
}

function removeActivityType(i) {
  activityTypes.value.splice(i, 1)
}

async function saveConfig(key, value) {
  if (key === 'skill_categories') savingSkill.value = true
  if (key === 'goods_categories') savingGoods.value = true
  try {
    await configApi.updateConfig(key, { configValue: JSON.stringify(value) })
    ElMessage.success('保存成功')
  } catch { /* ignore */ }
  finally {
    savingSkill.value = false
    savingGoods.value = false
  }
}

async function saveActivityTypes() {
  savingActivity.value = true
  try {
    await configApi.updateConfig('activity_types', { configValue: JSON.stringify(activityTypes.value) })
    ElMessage.success('保存成功')
  } catch { /* ignore */ }
  finally { savingActivity.value = false }
}

onMounted(() => loadConfigs())
</script>

<style scoped>
.config-card {
  margin-bottom: 16px;
}

.tag-editor {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}

.config-tag {
  font-size: 14px;
}
</style>
