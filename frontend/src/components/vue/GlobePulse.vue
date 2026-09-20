<template>
  <div :class="['globe-pulse relative aspect-square select-none', className]">
    <canvas
      ref="canvasRef"
      class="h-full w-full rounded-full opacity-0 transition-opacity duration-1000"
      style="cursor: grab; touch-action: none"
      @pointerdown="handlePointerDown"
    />
    <div
      v-for="marker in markerList"
      :key="marker.id"
      class="globe-pulse__marker"
      :style="{
        positionAnchor: `--cobe-${marker.id}`,
        animationDelay: `${marker.delay}s`,
        opacity: `var(--cobe-visible-${marker.id}, 0)`,
        filter: `blur(calc((1 - var(--cobe-visible-${marker.id}, 0)) * 8px)) drop-shadow(0 0 12px rgba(52, 211, 255, 0.5))`,
      }"
    >
      <span />
      <span />
      <i />
    </div>
  </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import createGlobe from 'cobe'

const props = defineProps({
  markers: {
    type: Array,
    default: () => [
      { id: 'beijing', location: [39.9, 116.4], delay: 0, size: 0.035 },
      { id: 'shanghai', location: [31.23, 121.47], delay: 0.45, size: 0.028 },
      { id: 'shenzhen', location: [22.54, 114.06], delay: 0.9, size: 0.026 },
      { id: 'singapore', location: [1.35, 103.82], delay: 1.35, size: 0.024 },
    ],
  },
  className: {
    type: String,
    default: '',
  },
  speed: {
    type: Number,
    default: 0.0032,
  },
})

const markerList = computed(() => props.markers)
const canvasRef = ref(null)
const pointerInteracting = ref(null)
const dragOffset = { phi: 0, theta: 0 }
let phiOffset = 0
let thetaOffset = 0
let paused = false
let globe = null
let animationId = 0
let resizeObserver = null

function handlePointerDown(event) {
  pointerInteracting.value = { x: event.clientX, y: event.clientY }
  paused = true
  if (canvasRef.value) canvasRef.value.style.cursor = 'grabbing'
}

function handlePointerMove(event) {
  if (!pointerInteracting.value) return
  dragOffset.phi = (event.clientX - pointerInteracting.value.x) / 300
  dragOffset.theta = (event.clientY - pointerInteracting.value.y) / 900
}

function handlePointerUp() {
  if (pointerInteracting.value) {
    phiOffset += dragOffset.phi
    thetaOffset += dragOffset.theta
    dragOffset.phi = 0
    dragOffset.theta = 0
  }
  pointerInteracting.value = null
  paused = false
  if (canvasRef.value) canvasRef.value.style.cursor = 'grab'
}

function renderGlobe() {
  const canvas = canvasRef.value
  if (!canvas || globe) return
  const width = canvas.offsetWidth
  if (!width) return

  let phi = 0.45
  globe = createGlobe(canvas, {
    devicePixelRatio: Math.min(window.devicePixelRatio || 1, 2),
    width,
    height: width,
    phi,
    theta: 0.26,
    dark: 1,
    diffuse: 1.45,
    mapSamples: 18000,
    mapBrightness: 8.5,
    baseColor: [0.42, 0.52, 0.62],
    markerColor: [0.22, 0.86, 0.95],
    glowColor: [0.12, 0.22, 0.34],
    markerElevation: 0.01,
    opacity: 0.96,
    markers: markerList.value.map((marker) => ({
      id: marker.id,
      location: marker.location,
      size: marker.size ?? 0.026,
    })),
  })

  const animate = () => {
    if (!paused) phi += props.speed
    globe?.update({
      phi: phi + phiOffset + dragOffset.phi,
      theta: 0.26 + thetaOffset + dragOffset.theta,
    })
    animationId = requestAnimationFrame(animate)
  }

  animate()
  window.setTimeout(() => {
    if (canvas) canvas.style.opacity = '1'
  }, 120)
}

onMounted(() => {
  window.addEventListener('pointermove', handlePointerMove, { passive: true })
  window.addEventListener('pointerup', handlePointerUp, { passive: true })

  if (canvasRef.value?.offsetWidth > 0) {
    renderGlobe()
  } else if (canvasRef.value) {
    resizeObserver = new ResizeObserver((entries) => {
      if (entries[0]?.contentRect.width > 0) {
        resizeObserver?.disconnect()
        renderGlobe()
      }
    })
    resizeObserver.observe(canvasRef.value)
  }
})

onBeforeUnmount(() => {
  window.removeEventListener('pointermove', handlePointerMove)
  window.removeEventListener('pointerup', handlePointerUp)
  if (animationId) cancelAnimationFrame(animationId)
  resizeObserver?.disconnect()
  globe?.destroy()
})
</script>
