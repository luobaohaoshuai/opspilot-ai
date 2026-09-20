<template>
  <div class="ops-resizable-table-wrap">
    <table class="ops-resizable-table">
      <colgroup>
        <col v-for="(column, index) in columns" :key="column.key" :style="{ width: `${widths[index]}px` }" />
      </colgroup>
      <thead>
        <tr>
          <th v-for="(column, index) in columns" :key="column.key">
            <span>{{ column.label }}</span>
            <button
              v-if="index < columns.length - 1"
              class="ops-column-resizer"
              type="button"
              role="separator"
              :aria-label="`调整${column.label}列宽`"
              @pointerdown="startResize($event, index)"
              @keydown.left.prevent="resizeBy(index, -12)"
              @keydown.right.prevent="resizeBy(index, 12)"
            />
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="item in items" :key="item.id">
          <td>{{ formatDateTime(item.reportTime) }}</td>
          <td><strong>{{ item.temperature ?? '--' }}</strong><span class="ops-table-unit">°C</span></td>
          <td><strong>{{ item.humidity ?? '--' }}</strong><span class="ops-table-unit">%</span></td>
          <td>{{ item.rssi ?? '--' }} dBm</td>
          <td>{{ item.firmwareVersion ?? '--' }}</td>
        </tr>
        <tr v-if="!items.length">
          <td :colspan="columns.length" class="ops-table-empty">暂无遥测记录</td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { onBeforeUnmount, ref } from 'vue'

defineProps({
  items: { type: Array, default: () => [] },
})

const columns = [
  { key: 'time', label: '上报时间' },
  { key: 'temperature', label: '温度' },
  { key: 'humidity', label: '湿度' },
  { key: 'rssi', label: '信号强度' },
  { key: 'firmware', label: '固件版本' },
]

const widths = ref([160, 105, 105, 125, 130])
let activeResize = null

function formatDateTime(value) {
  if (!value) return '--'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return String(value)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

function resizeBy(index, delta) {
  const next = [...widths.value]
  next[index] = Math.max(112, next[index] + delta)
  widths.value = next
}

function handlePointerMove(event) {
  if (!activeResize) return
  const next = [...widths.value]
  next[activeResize.index] = Math.max(112, activeResize.startWidth + event.clientX - activeResize.startX)
  widths.value = next
}

function stopResize() {
  activeResize = null
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerup', stopResize)
}

function startResize(event, index) {
  activeResize = { index, startX: event.clientX, startWidth: widths.value[index] }
  window.addEventListener('pointermove', handlePointerMove)
  window.addEventListener('pointerup', stopResize, { once: true })
}

onBeforeUnmount(stopResize)
</script>

<style scoped>
.ops-resizable-table-wrap {
  width: 100%;
  overflow-x: auto;
  border: 1px solid rgba(255, 255, 255, 0.08);
}

.ops-resizable-table {
  width: 100%;
  min-width: 625px;
  table-layout: fixed;
  border-collapse: collapse;
  font-size: 0.82rem;
}

th,
td {
  border-bottom: 1px solid rgba(255, 255, 255, 0.075);
  padding: 0.85rem 1rem;
  text-align: left;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

th {
  position: relative;
  color: rgba(255, 255, 255, 0.4);
  font-size: 0.72rem;
  font-weight: 500;
}

td {
  color: rgba(255, 255, 255, 0.68);
}

tbody tr {
  transition: background-color 160ms ease;
}

tbody tr:hover {
  background: rgba(255, 255, 255, 0.025);
}

tbody tr:last-child td {
  border-bottom: 0;
}

.ops-column-resizer {
  position: absolute;
  top: 22%;
  right: -4px;
  z-index: 2;
  width: 8px;
  height: 56%;
  cursor: col-resize;
}

.ops-column-resizer::after {
  content: '';
  position: absolute;
  left: 3px;
  width: 1px;
  height: 100%;
  background: rgba(255, 255, 255, 0.12);
  transition: background-color 160ms ease;
}

.ops-column-resizer:hover::after,
.ops-column-resizer:focus-visible::after {
  background: rgba(45, 212, 191, 0.9);
}

.ops-table-unit {
  margin-left: 0.2rem;
  color: rgba(255, 255, 255, 0.38);
}

.ops-table-empty {
  padding: 2.5rem;
  text-align: center;
  color: rgba(255, 255, 255, 0.38);
}
</style>
