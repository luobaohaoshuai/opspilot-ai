<template>
  <div class="ops-chart-shell">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>

<script setup>
import { computed } from 'vue'
import {
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Legend,
  LinearScale,
  Tooltip,
} from 'chart.js'
import { Bar } from 'vue-chartjs'

ChartJS.register(CategoryScale, LinearScale, BarElement, Tooltip, Legend)

const props = defineProps({
  items: { type: Array, default: () => [] },
})

function formatLabel(value, index) {
  if (!value) return `记录 ${index + 1}`
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return `记录 ${index + 1}`
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

const normalizedItems = computed(() => [...props.items].slice(0, 7).reverse())

const chartData = computed(() => ({
  labels: normalizedItems.value.map((item, index) => formatLabel(item.reportTime, index)),
  datasets: [
    {
      label: '温度 °C',
      data: normalizedItems.value.map((item) => Number(item.temperature ?? 0)),
      backgroundColor: 'rgba(249, 115, 22, 0.78)',
      borderColor: 'rgba(251, 146, 60, 1)',
      borderWidth: 1,
      borderRadius: 5,
      maxBarThickness: 20,
    },
    {
      label: '湿度 %',
      data: normalizedItems.value.map((item) => Number(item.humidity ?? 0)),
      backgroundColor: 'rgba(45, 212, 191, 0.68)',
      borderColor: 'rgba(94, 234, 212, 1)',
      borderWidth: 1,
      borderRadius: 5,
      maxBarThickness: 20,
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  animation: { duration: 420 },
  interaction: { mode: 'index', intersect: false },
  plugins: {
    legend: {
      align: 'start',
      labels: {
        color: 'rgba(255,255,255,0.58)',
        boxWidth: 10,
        boxHeight: 10,
        padding: 18,
        font: { family: 'Inter, ui-sans-serif, system-ui', size: 11 },
      },
    },
    tooltip: {
      backgroundColor: '#111318',
      borderColor: 'rgba(255,255,255,0.12)',
      borderWidth: 1,
      titleColor: '#fff',
      bodyColor: 'rgba(255,255,255,0.7)',
      padding: 12,
    },
  },
  scales: {
    x: {
      grid: { display: false },
      border: { color: 'rgba(255,255,255,0.09)' },
      ticks: { color: 'rgba(255,255,255,0.38)', maxRotation: 0, autoSkip: true, maxTicksLimit: 7 },
    },
    y: {
      beginAtZero: true,
      suggestedMax: 100,
      grid: { color: 'rgba(255,255,255,0.065)', drawTicks: false },
      border: { display: false },
      ticks: { color: 'rgba(255,255,255,0.38)', padding: 8 },
    },
  },
}
</script>

<style scoped>
.ops-chart-shell {
  height: 290px;
  min-width: 0;
}

@media (max-width: 768px) {
  .ops-chart-shell {
    height: 250px;
  }
}
</style>
