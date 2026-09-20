<template>
  <div class="flex flex-col overflow-hidden py-20 md:py-28">
    <div ref="containerRef" class="relative flex h-[72rem] items-center justify-center p-2 md:h-[94rem] md:p-20">
      <div class="relative w-full py-10 md:py-40" style="perspective: 850px">
        <div class="mx-auto max-w-5xl text-center" :style="{ transform: `translateY(${translate}px)` }">
          <p class="mb-4 text-sm uppercase tracking-[0.3em] text-white/45">Agent Showcase</p>
          <h2 class="text-4xl font-semibold text-white md:text-6xl">
            一个能查状态、追证据、给动作的
            <br />
            <span class="mt-1 inline-block bg-gradient-to-r from-white to-orange-300 bg-clip-text font-bold leading-none text-transparent">
              设备运维 Agent
            </span>
          </h2>
        </div>

        <div
          class="mx-auto -mt-12 h-[30rem] w-full max-w-5xl rounded-[30px] border-4 border-[#6C6C6C] bg-[#222222] p-2 shadow-2xl md:h-[40rem] md:p-6"
          :style="cardStyle"
        >
          <div class="h-full w-full overflow-hidden rounded-2xl bg-gray-100 dark:bg-zinc-900 md:rounded-2xl md:p-4">
            <div class="flex h-full flex-col bg-[radial-gradient(circle_at_top,_rgba(56,189,248,0.14),_transparent_36%),linear-gradient(180deg,_rgba(5,10,18,1)_0%,_rgba(7,12,20,1)_100%)] text-white">
              <div class="grid gap-3 border-b border-white/10 p-4 md:grid-cols-[1.2fr_0.8fr]">
                <div class="rounded-2xl border border-white/10 bg-white/[0.04] p-4">
                  <div class="mb-3 flex items-center gap-2 text-sm text-white/72">
                    <Bot class="h-4 w-4 text-orange-300" />
                    OpsPilot Agent 正在组织一次完整排障
                  </div>
                  <h3 class="text-2xl font-semibold">ESP32-001 高温告警：先看现场，再查证据，最后闭环。</h3>
                  <p class="mt-3 max-w-2xl text-sm leading-relaxed text-white/58">
                    它不是只给一句答案，而是把设备状态、告警历史、知识库证据和处理动作串成一条可复盘的链路。
                  </p>
                </div>

                <div class="grid gap-3 md:grid-cols-2">
                  <div v-for="item in stats" :key="item.label" class="rounded-2xl border border-white/10 bg-white/[0.04] p-4">
                    <div class="flex items-center gap-2 text-xs text-white/48">
                      <component :is="item.icon" class="h-4 w-4 text-sky-300" />
                      {{ item.label }}
                    </div>
                    <p class="mt-4 text-2xl font-semibold">{{ item.value }}</p>
                  </div>
                </div>
              </div>

              <div class="grid flex-1 gap-3 p-4 md:grid-cols-[1.1fr_0.9fr]">
                <div class="rounded-3xl border border-white/10 bg-white/[0.04] p-4">
                  <div class="mb-4 flex items-center justify-between">
                    <div>
                      <p class="text-sm font-medium text-white/80">Agent 工作链路</p>
                      <p class="mt-1 text-xs text-white/42">每一步都有工具、证据和结果，工程师能看懂，也能接着处理。</p>
                    </div>
                    <Database class="h-5 w-5 text-orange-300" />
                  </div>

                  <div class="grid gap-3">
                    <div v-for="(step, index) in workflow" :key="step.title" class="rounded-2xl border border-white/10 bg-black/20 p-4">
                      <div class="flex items-center justify-between gap-3">
                        <p class="text-sm font-medium">{{ index + 1 }}. {{ step.title }}</p>
                        <span class="text-xs text-white/38">{{ 118 + index * 46 }}ms</span>
                      </div>
                      <p class="mt-2 text-sm leading-relaxed text-white/56">{{ step.detail }}</p>
                    </div>
                  </div>
                </div>

                <div class="grid gap-3">
                  <div class="rounded-3xl border border-white/10 bg-white/[0.04] p-4">
                    <p class="text-sm font-medium text-white/80">证据来源片段</p>
                    <div class="mt-4 space-y-3">
                      <div class="rounded-2xl border border-white/10 bg-black/20 p-4">
                        <p class="text-xs text-sky-200">设备说明书 / P2</p>
                        <p class="mt-2 text-sm leading-relaxed text-white/58">DHT22 数据引脚接入 GPIO4，确认供电稳定后再开始温湿度上报。</p>
                      </div>
                      <div class="rounded-2xl border border-white/10 bg-black/20 p-4">
                        <p class="text-xs text-orange-200">排障手册 / P3</p>
                        <p class="mt-2 text-sm leading-relaxed text-white/58">高温超过 35°C 时优先排查机柜散热、冷通道风量与传感器安装位置。</p>
                      </div>
                    </div>
                  </div>

                  <div class="rounded-3xl border border-white/10 bg-gradient-to-br from-orange-400/18 to-transparent p-4">
                    <div class="flex items-center gap-2 text-sm font-medium text-white/80">
                      <Network class="h-4 w-4 text-orange-200" />
                      知识库缺失时继续找答案
                    </div>
                    <p class="mt-3 text-sm leading-relaxed text-white/56">
                      内部知识库不够时，Agent 会转向联网检索；问题含糊时，它先追问确认，不急着瞎答。
                    </p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { Activity, AlertTriangle, Bot, Database, FileText, Network, ShieldCheck } from 'lucide-vue-next'

const containerRef = ref(null)
const progress = ref(0)
const isMobile = ref(false)

const stats = [
  { icon: Activity, label: '设备上下文', value: '4 类' },
  { icon: AlertTriangle, label: '风险信号', value: '03' },
  { icon: FileText, label: '证据来源', value: '12' },
  { icon: ShieldCheck, label: '闭环动作', value: '5 步' },
]

const workflow = [
  { title: '识别对象', detail: '从当前对话和长期记忆里确认用户说的是哪台设备' },
  { title: '读取现场', detail: '拉取 ESP32-001 最新遥测、在线状态和告警队列' },
  { title: '检索证据', detail: '优先查知识库，缺证据时再走联网检索兜底' },
  { title: '生成闭环', detail: '输出处理步骤、风险提醒和需要人工确认的动作' },
]

const translate = computed(() => lerp(0, -78, mappedProgress.value))
const rotateX = computed(() => lerp(36, 0, mappedProgress.value))
const scale = computed(() => lerp(isMobile.value ? 0.7 : 1.05, isMobile.value ? 0.9 : 1, mappedProgress.value))
const mappedProgress = computed(() => clamp((progress.value - 0.28) / 0.4, 0, 1))
const cardStyle = computed(() => ({
  transform: `rotateX(${rotateX.value}deg) scale(${scale.value})`,
  transformOrigin: '50% 44%',
  transformStyle: 'preserve-3d',
  willChange: 'transform',
  boxShadow:
    '0 0 #0000004d, 0 9px 20px #0000004a, 0 37px 37px #00000042, 0 84px 50px #00000026, 0 149px 60px #0000000a, 0 233px 65px #00000003',
}))

function clamp(value, min, max) {
  return Math.min(max, Math.max(min, value))
}

function lerp(start, end, amount) {
  return start + (end - start) * amount
}

function updateScroll() {
  const node = containerRef.value
  if (!node) return
  const rect = node.getBoundingClientRect()
  const viewport = window.innerHeight || 1
  const start = viewport * 0.72
  const end = -rect.height + viewport * 0.18
  progress.value = clamp((start - rect.top) / (start - end), 0, 1)
}

function updateMobile() {
  isMobile.value = window.innerWidth <= 768
}

onMounted(() => {
  updateMobile()
  updateScroll()
  window.addEventListener('resize', updateMobile)
  window.addEventListener('resize', updateScroll)
  window.addEventListener('scroll', updateScroll, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', updateMobile)
  window.removeEventListener('resize', updateScroll)
  window.removeEventListener('scroll', updateScroll)
})
</script>
