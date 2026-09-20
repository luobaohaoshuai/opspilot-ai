<template>
  <main v-if="guardedPage === 'home'" class="bg-[#04070d] text-white">
    <section class="relative h-screen w-full overflow-hidden">
      <video
        :class="[
          'absolute inset-x-0 top-[-2%] h-[104%] w-full object-cover object-[56%_39%] brightness-[1.36] saturate-[1.26] contrast-[1.14] [transform:translateZ(0)] transition-opacity duration-700 md:top-[-2.5%] md:h-[105%] md:object-[56%_39%]',
          videoReady ? 'opacity-[0.99]' : 'opacity-0',
        ]"
        autoplay
        muted
        playsinline
        preload="auto"
        :src="BG_VIDEO"
        @loadeddata="videoReady = true"
        @play="freezeLocked = false"
        @timeupdate="freezeVideo"
        @ended="freezeEnded"
      />

      <div class="absolute inset-0 bg-[#03070d]/5" />
      <div class="absolute inset-0 bg-hero-vignette" />
      <div class="hero-top-scrim absolute inset-x-0 top-0 h-44" />
      <div class="absolute inset-x-0 bottom-0 h-[48vh] bg-gradient-to-t from-[#04070d] via-[#04070d]/84 to-transparent" />
      <div class="absolute inset-y-0 left-0 w-[42vw] bg-gradient-to-r from-[#04070d]/45 to-transparent" />
      <div class="hero-title-scrim absolute inset-0" />

      <header class="absolute left-0 right-0 top-0 z-30 flex items-center justify-between px-5 py-5 sm:px-8">
        <button class="flex items-center gap-2 text-base font-medium text-white" @click="navigate('#/')">
          <Radar :size="20" :stroke-width="1.7" />
          <span>OpsPilot AI</span>
        </button>

        <div class="hidden items-center gap-3 md:flex">
          <button
            class="liquid-glass inline-flex h-12 items-center gap-3 rounded-full border border-cyan-200/15 bg-cyan-200/[0.06] px-5 text-base font-semibold text-white shadow-[0_18px_46px_rgba(34,211,238,0.12)] transition-colors duration-300 hover:bg-cyan-200/[0.1]"
            @click="navigate('#/console/agent')"
          >
            <Bot :size="19" :stroke-width="1.9" />
            <span>AI 助手</span>
            <ArrowRight :size="17" />
          </button>
          <button class="liquid-glass rounded-full px-4 py-2.5 text-sm font-medium text-white shadow-glass transition-colors duration-300 hover:bg-white/[0.05]" @click="navigate('#/console/api')">
            查看接口
          </button>
          <button class="rounded-full bg-white px-4 py-2.5 text-sm font-medium text-black transition-colors duration-300 hover:bg-white/90" @click="navigate('#/login')">
            登录控制台
          </button>
        </div>

        <button
          class="liquid-glass rounded-lg p-2 text-white shadow-glass md:hidden"
          :aria-expanded="menuOpen"
          :aria-label="menuOpen ? '关闭菜单' : '打开菜单'"
          @click="menuOpen = !menuOpen"
        >
          <X v-if="menuOpen" :size="18" />
          <Menu v-else :size="18" />
        </button>
      </header>

      <aside class="absolute left-5 top-[92px] z-30 hidden lg:block sm:left-8">
        <OpsVerticalNav :items="homeNavItems" active-path="#/" @navigate="navigate" />
      </aside>

      <div v-if="menuOpen" class="absolute left-4 right-4 top-[72px] z-40 flex flex-col gap-3 md:hidden">
        <button class="liquid-glass flex h-14 w-full items-center justify-between rounded-[1.75rem] px-5 text-left text-base font-semibold text-white shadow-glass transition-colors duration-300 hover:bg-white/[0.07]" @click="navigate('#/console/agent')">
          <span class="inline-flex items-center gap-2.5">
            <Bot :size="19" :stroke-width="1.9" />
            AI 助手
          </span>
          <ArrowRight :size="18" />
        </button>
        <OpsVerticalNav :items="homeNavItems" active-path="#/" class-name="w-full max-w-none" @navigate="navigate" />
        <div class="liquid-glass mt-1 flex gap-2 rounded-[1.65rem] border border-white/10 p-3">
          <button class="liquid-glass flex-1 rounded-full px-4 py-2.5 text-sm font-medium text-white transition-colors duration-300 hover:bg-white/[0.05]" @click="navigate('#/console/api')">
            查看接口
          </button>
          <button class="flex-1 rounded-full bg-white px-4 py-2.5 text-sm font-medium text-black transition-colors duration-300 hover:bg-white/90" @click="navigate('#/login')">
            登录控制台
          </button>
        </div>
      </div>

      <section class="absolute bottom-0 left-0 z-20 w-full max-w-3xl px-6 pb-10 sm:px-12 sm:pb-16">
        <div class="pointer-events-none absolute -bottom-16 -left-24 h-[420px] w-[720px] rounded-full bg-[#04070d]/75 blur-3xl" />
        <div class="relative z-10">
          <div class="mb-5 inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-medium uppercase tracking-[0.18em] text-white/75 backdrop-blur-sm">
            <Workflow :size="14" />
            企业 AIoT 运维智能体平台
          </div>
          <h1 class="mb-4 text-5xl font-medium leading-tight text-white sm:text-6xl lg:text-7xl">OpsPilot AI</h1>
          <p class="mb-6 max-w-xl text-sm leading-relaxed text-white/64 sm:text-[15px]">
            面向设备现场的 AI 运维助手：理解工程师的问题，连接设备遥测、告警、知识库和联网检索，把排障过程变成可执行、可复盘的闭环。
          </p>
          <div class="mb-7 flex max-w-xl flex-wrap gap-2.5">
            <button v-for="pill in homePills" :key="pill.label" class="liquid-glass whitespace-nowrap rounded-full px-3.5 py-2 text-[11px] font-medium text-white/80 transition-colors duration-300 hover:bg-white/[0.05]" @click="navigate(pill.path)">
              {{ pill.label }}
            </button>
          </div>
          <div class="flex flex-wrap items-center gap-3">
            <button class="rounded-full bg-white px-6 py-3 text-sm font-medium text-black transition-colors duration-300 hover:bg-white/90 sm:px-7 sm:text-base" @click="navigate('#/login')">
              登录控制台
            </button>
            <button class="liquid-glass rounded-full px-6 py-3 text-sm font-medium text-white transition-colors duration-300 hover:bg-white/[0.05] sm:px-7 sm:text-base" @click="scrollToOverview">
              <span class="inline-flex items-center gap-2">
                向下看运维链路
                <ArrowRight :size="16" />
              </span>
            </button>
          </div>
        </div>
      </section>

      <button class="absolute bottom-4 left-1/2 z-20 inline-flex -translate-x-1/2 items-center gap-2 rounded-full border border-white/10 bg-black/20 px-4 py-2 text-xs text-white/62 backdrop-blur-sm transition-colors hover:text-white" @click="scrollToOverview">
        向下滚动查看主内容
        <ChevronDown :size="14" />
      </button>
    </section>

    <section id="scroll-overview" class="relative overflow-hidden border-t border-white/10 bg-[#04070d]">
      <div class="absolute left-1/2 top-8 h-64 w-64 -translate-x-1/2 rounded-full bg-sky-500/10 blur-3xl" />
      <div class="absolute right-0 top-32 h-80 w-80 rounded-full bg-orange-500/10 blur-3xl" />
      <div class="relative mx-auto max-w-6xl px-6 pt-16 text-center sm:px-12">
        <p class="text-xs uppercase tracking-[0.28em] text-white/38">Agent Platform</p>
        <h2 class="mt-4 text-3xl font-semibold text-white sm:text-5xl">从一句问题，到一条可复盘的处理链路</h2>
        <p class="mx-auto mt-4 max-w-2xl text-sm leading-relaxed text-white/56 sm:text-base">
          OpsPilot AI 面向设备运维现场：先理解用户在问什么，再汇总设备状态、告警、知识库和联网证据，最后给出能执行、能确认、能留痕的建议。
        </p>
        <div class="mt-10 grid gap-3 text-left md:grid-cols-3">
          <div v-for="item in platformCards" :key="item.title" class="rounded-[24px] border border-white/10 bg-white/[0.035] p-5 backdrop-blur">
            <component :is="item.icon" class="h-5 w-5 text-orange-200" />
            <h3 class="mt-4 text-base font-semibold text-white">{{ item.title }}</h3>
            <p class="mt-2 text-sm leading-6 text-white/54">{{ item.detail }}</p>
          </div>
        </div>
      </div>
      <HeroScrollDemo />
    </section>

    <div id="footer-contact" class="border-t border-white/10">
      <FooterBlock />
    </div>
  </main>

  <main v-else-if="guardedPage === 'login'" :class="['relative min-h-screen overflow-hidden bg-[#03070d] text-white', isDissolving ? 'login-page--dissolving' : loginIntroReady ? 'login-page--ready' : 'login-page--intro']">
    <div class="absolute inset-0 bg-[#03070d]" />
    <div class="absolute inset-0 bg-[radial-gradient(42%_42%_at_50%_50%,rgba(54,164,255,0.22)_0%,rgba(4,7,13,0)_64%)]" />
    <div class="absolute inset-0 bg-[radial-gradient(30%_38%_at_72%_38%,rgba(255,132,54,0.18)_0%,rgba(4,7,13,0)_68%)]" />
    <div class="absolute inset-0 bg-[linear-gradient(180deg,rgba(3,7,13,0.38)_0%,rgba(3,7,13,0.12)_45%,rgba(3,7,13,0.72)_100%)]" />
    <div class="ops-login-grid absolute inset-0" />
    <div v-if="isDissolving" class="login-console-gradient fixed inset-0 z-[70] pointer-events-none" aria-hidden="true" />

    <div :class="['login-orb-stage absolute left-1/2 top-1/2 h-[min(82vw,620px)] w-[min(82vw,620px)] md:h-[min(46vw,560px)] md:w-[min(46vw,560px)]', isDissolving ? 'z-[85] login-orb-stage--scatter' : loginIntroReady ? 'z-[5] login-orb-stage--ready' : 'z-[25] login-orb-stage--intro']">
      <div class="absolute inset-6 rounded-full bg-cyan-300/12 blur-3xl" />
      <div class="absolute inset-0 rounded-full bg-[radial-gradient(circle_at_38%_30%,rgba(255,255,255,0.32),rgba(120,210,255,0.13)_28%,rgba(4,7,13,0)_64%)]" />
      <div :class="['login-particle-field', isDissolving ? 'login-particle-field--scatter' : '']" aria-hidden="true">
        <span
          v-for="(particle, index) in loginParticles"
          :key="`${particle[0]}-${particle[1]}-${index}`"
          :style="{
            '--particle-x': particle[0],
            '--particle-y': particle[1],
            '--particle-start-x': particle[2],
            '--particle-start-y': particle[3],
            '--particle-delay': particle[4],
          }"
        />
      </div>
      <div :class="['login-globe-shell relative z-10 h-full w-full', isDissolving ? 'login-globe-shell--scatter' : '']">
        <GlobePulse class-name="h-full w-full" />
      </div>
    </div>

    <header :class="['login-ui-reveal relative z-20 flex items-center justify-between px-5 py-5 sm:px-8', loginIntroReady && !loginLeaving ? 'login-ui-reveal--ready' : '', loginLeaving ? 'login-ui-reveal--exit' : '']">
      <button class="flex items-center gap-2 text-base font-semibold text-white" @click="navigate('#/')">
        <Radar :size="20" :stroke-width="1.7" />
        <span>OpsPilot AI</span>
      </button>
      <button class="liquid-glass rounded-full px-4 py-2.5 text-sm font-medium text-white shadow-glass transition-colors duration-300 hover:bg-white/[0.05]" @click="navigate('#/')">
        返回首页
      </button>
    </header>

    <section :class="['relative z-10 mx-auto grid min-h-[calc(100vh-88px)] w-full max-w-7xl items-center gap-8 px-5 pb-10 pt-4 sm:px-8 lg:grid-cols-[minmax(0,1.05fr)_460px]', loginIntroReady && !loginLeaving ? 'login-content-grid--ready' : '', loginLeaving ? 'login-content-grid--exit' : '']">
      <div :class="['login-copy-panel relative min-h-[520px]', loginIntroReady && !loginLeaving ? 'login-ui-reveal--ready' : '', loginLeaving ? 'login-ui-reveal--exit' : '']">
        <div class="relative z-10 max-w-2xl">
          <div class="mb-5 inline-flex items-center gap-2 rounded-full border border-white/10 bg-white/5 px-3 py-1.5 text-xs font-medium uppercase tracking-[0.18em] text-white/72 backdrop-blur-sm">
            <Sparkles :size="14" />
            AI Operations Sign In
          </div>
          <h1 class="max-w-3xl text-4xl font-medium leading-tight tracking-tight text-white sm:text-5xl lg:text-6xl">让运维地球先亮起，再进入控制台。</h1>
          <p class="mt-5 max-w-xl text-sm leading-relaxed text-white/64 sm:text-[15px]">
            登录页承接首页的玻璃球动势，用 Ops Globe 展示设备、告警、知识库和 Agent 的连接关系。后端未启动时会自动进入演示数据，演示链路不断档。
          </p>
        </div>

        <div class="absolute bottom-0 left-0 right-0 z-20 grid max-w-2xl gap-3 sm:grid-cols-3">
          <div v-for="item in loginStats" :key="item.label" class="liquid-glass rounded-2xl p-4">
            <component :is="item.icon" class="mb-3 text-cyan-100" :size="17" />
            <p class="text-xs text-white/48">{{ item.label }}</p>
            <strong class="mt-1 block text-xl font-semibold text-white">{{ item.value }}</strong>
          </div>
        </div>
      </div>

      <form :class="['login-form-panel liquid-glass relative z-20 overflow-hidden rounded-[2rem] border border-white/10 p-6 shadow-2xl sm:p-7', loginIntroReady && !loginLeaving ? 'login-ui-reveal--ready' : '', loginLeaving ? 'login-ui-reveal--exit' : '']" @submit.prevent="handleLoginSubmit">
        <div class="absolute -right-20 -top-24 h-56 w-56 rounded-full bg-cyan-300/14 blur-3xl" />
        <div class="absolute -bottom-24 left-6 h-52 w-52 rounded-full bg-orange-400/12 blur-3xl" />
        <div class="relative z-10">
          <div class="mb-7 flex items-start justify-between gap-4">
            <div>
              <p class="mb-2 text-xs font-medium uppercase tracking-[0.18em] text-white/45">Welcome Back</p>
              <h2 class="text-3xl font-semibold tracking-tight text-white">登录控制台</h2>
              <p class="mt-2 text-sm leading-relaxed text-white/55">默认演示账号已填好，点击即可进入 OpsPilot。</p>
            </div>
            <div class="rounded-2xl border border-white/10 bg-white/6 p-3 text-cyan-100">
              <ShieldCheck :size="22" />
            </div>
          </div>

          <div class="mb-5 rounded-2xl border border-white/10 bg-white/[0.045] p-3">
            <div class="flex items-center gap-2 text-xs font-medium text-white/70">
              <CircleCheck class="text-emerald-300" :size="15" />
              {{ modeLabel }}
            </div>
          </div>

          <div class="space-y-4">
            <label class="block">
              <span class="mb-2 flex items-center gap-2 text-xs font-medium text-white/55">
                <UserRound :size="14" />
                用户名
              </span>
              <input v-model="username" class="w-full rounded-2xl border border-white/10 bg-white/[0.055] px-4 py-3 text-sm text-white outline-none transition-colors placeholder:text-white/30 focus:border-cyan-200/50 focus:bg-white/[0.075]" placeholder="admin" autocomplete="username" />
            </label>
            <label class="block">
              <span class="mb-2 flex items-center gap-2 text-xs font-medium text-white/55">
                <LockKeyhole :size="14" />
                密码
              </span>
              <div class="flex items-center rounded-2xl border border-white/10 bg-white/[0.055] pr-3 transition-colors focus-within:border-cyan-200/50 focus-within:bg-white/[0.075]">
                <input v-model="password" class="min-w-0 flex-1 bg-transparent px-4 py-3 text-sm text-white outline-none placeholder:text-white/30" placeholder="请输入密码" :type="showPassword ? 'text' : 'password'" autocomplete="current-password" />
                <button type="button" class="rounded-full p-2 text-white/50 transition-colors hover:bg-white/10 hover:text-white" :aria-label="showPassword ? '隐藏密码' : '显示密码'" @click="showPassword = !showPassword">
                  <EyeOff v-if="showPassword" :size="16" />
                  <Eye v-else :size="16" />
                </button>
              </div>
            </label>
          </div>

          <p v-if="loginError" class="mt-4 text-sm text-amber-200">{{ loginError }}</p>
          <button class="mt-6 flex w-full items-center justify-center gap-2 rounded-full bg-white px-5 py-3.5 text-sm font-semibold text-black transition-colors duration-300 hover:bg-white/90 disabled:cursor-not-allowed disabled:opacity-70" :disabled="loginBusy" type="submit">
            {{ isDissolving ? '地球正在消散...' : loginBusy ? '正在进入...' : '进入 OpsPilot' }}
            <RefreshCw v-if="loginBusy" class="animate-spin" :size="17" />
            <LogIn v-else :size="17" />
          </button>
          <button class="liquid-glass mt-3 flex w-full items-center justify-center rounded-full px-5 py-3 text-sm font-medium text-white transition-colors duration-300 hover:bg-white/[0.05]" type="button" @click="navigate('#/')">
            先回首页看看球体转场
          </button>
        </div>
      </form>
    </section>
  </main>

  <main v-else-if="route.tab === 'agent'" :class="['ops-console-shell min-h-screen text-white', consoleRevealing ? 'ops-console-reveal' : '']">
    <ConsoleTopbar :run-mode-label="runModeLabel" :syncing="syncing" agent @navigate="navigate" @sync="syncProject" />
    <AgentStudio />
  </main>

  <main
    v-else
    :class="['ops-console-shell ops-dashboard-shell min-h-screen text-white', consoleAmbientEnabled ? 'ops-dashboard-shell--ambient' : 'ops-dashboard-shell--quiet', consoleRevealing ? 'ops-console-reveal' : '']"
    @pointermove="handleConsolePointerMove"
    @pointerleave="handleConsolePointerLeave"
  >
    <img class="ops-console-background-art" src="/hero-frozen-frame.png" alt="" aria-hidden="true" />
    <div :class="['ops-dashboard-frame', consoleSidebarCollapsed ? 'ops-dashboard-frame--collapsed' : '']">
      <aside class="ops-dashboard-sidebar">
        <div class="ops-dashboard-brand">
          <button class="ops-dashboard-brand__home" type="button" @click="navigate('#/')">
            <span class="ops-dashboard-brand__mark"><Cpu :size="17" /></span>
            <span class="ops-dashboard-brand__label">OpsPilot AI</span>
          </button>
          <button class="ops-icon-button" type="button" :aria-label="consoleSidebarCollapsed ? '展开侧栏' : '收起侧栏'" @click="consoleSidebarCollapsed = !consoleSidebarCollapsed">
            <Menu :size="16" />
          </button>
        </div>

        <div class="ops-dashboard-nav-scroll">
          <p class="ops-dashboard-nav-label">运维工作区</p>
          <OpsVerticalNav :items="consoleNavItems" :active-path="`#/console/${route.tab}`" :class-name="consoleSidebarCollapsed ? 'ops-vertical-nav--collapsed' : ''" @navigate="navigate" />
        </div>

        <div class="ops-dashboard-sidebar__footer">
          <div v-if="!consoleSidebarCollapsed" class="ops-sidebar-info-card">
            <div class="ops-sidebar-info-card__head">
              <span :class="['ops-status-dot', runMode === 'online' ? 'ops-status-dot--ok' : 'ops-status-dot--warn']" />
              <strong>{{ runModeLabel }}</strong>
            </div>
            <p>{{ notice }}</p>
            <button type="button" @click="syncProject">
              <RefreshCw :size="13" :class="syncing ? 'animate-spin' : ''" />
              {{ syncing ? '同步中' : '重新同步' }}
            </button>
          </div>
          <div class="ops-dashboard-user">
            <span class="ops-dashboard-user__avatar">{{ (sessionUser?.username || 'A').slice(0, 1).toUpperCase() }}</span>
            <span class="ops-dashboard-user__copy">
              <strong>{{ sessionUser?.username || 'admin' }}</strong>
              <small>{{ sessionUser?.role === 'ADMIN' ? '系统管理员' : '运维人员' }}</small>
            </span>
          </div>
        </div>
      </aside>

      <section class="ops-dashboard-main">
        <header class="ops-dashboard-pagebar">
          <div class="min-w-0">
            <p>控制台 / {{ activeTabLabel }}</p>
            <h1>{{ activeTabLabel }}</h1>
          </div>
          <div class="ops-dashboard-pagebar__actions">
            <span><span :class="['ops-status-dot', runMode === 'online' ? 'ops-status-dot--ok' : 'ops-status-dot--warn']" />{{ runModeLabel }}</span>
            <button
              :class="['ops-icon-button ops-ambient-toggle', consoleAmbientEnabled ? 'ops-ambient-toggle--active' : '']"
              type="button"
              :aria-label="consoleAmbientEnabled ? '关闭氛围效果' : '开启氛围效果'"
              :aria-pressed="consoleAmbientEnabled"
              @click="consoleAmbientEnabled = !consoleAmbientEnabled"
            ><Sparkles :size="15" /></button>
            <button class="ops-icon-button" type="button" aria-label="打开 AI 助手" @click="navigate('#/console/agent')"><Bot :size="16" /></button>
            <button class="ops-ghost-button px-3 py-2 text-xs" type="button" @click="syncProject"><RefreshCw :size="14" :class="syncing ? 'animate-spin' : ''" />刷新数据</button>
          </div>
        </header>

        <div class="ops-dashboard-content">
          <div class="ops-dashboard-kpis">
            <Metric label="设备总数" :value="String(devices.length)" :detail="`在线 ${onlineDeviceCount} 台`" />
            <Metric label="未处理告警" :value="String(totalOpenAlerts)" detail="高温与离线事件" />
            <Metric label="员工记录" :value="String(employees.length)" detail="基础数据管理" />
            <Metric label="当前模式" :value="runModeLabel" detail="JWT + Vite 代理" />
          </div>

          <KeepAlive :max="5">
            <component :is="activeConsolePanel" :key="route.tab" />
          </KeepAlive>
        </div>
      </section>
    </div>
  </main>
</template>

<script setup>
import { computed, defineComponent, h, nextTick, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import {
  Activity,
  AlertTriangle,
  ArrowRight,
  BellRing,
  Bot,
  Cable,
  ChevronDown,
  CircleCheck,
  Cpu,
  Database,
  Eye,
  EyeOff,
  FileText,
  HardDriveUpload,
  Home,
  KeyRound,
  LockKeyhole,
  LogIn,
  Mail,
  Menu,
  MessageSquare,
  Network,
  Plus,
  Radar,
  RefreshCw,
  Save,
  SendHorizonal,
  Server,
  ShieldCheck,
  Sparkles,
  Trash2,
  UserRound,
  UserCog,
  Users,
  Workflow,
  X,
} from 'lucide-vue-next'
import HeroScrollDemo from './components/vue/HeroScrollDemo.vue'
import GlobePulse from './components/vue/GlobePulse.vue'
import ResizableTelemetryTable from './components/vue/ResizableTelemetryTable.vue'
import TelemetryBarChart from './components/vue/TelemetryBarChart.vue'
import {
  askKnowledge,
  buildMockAgentAnswer,
  buildMockKnowledgeAnswer,
  buildMockKnowledgeSources,
  chatWithAgent,
  clearToken,
  cloneMockAlarms,
  cloneMockCommands,
  cloneMockConfigs,
  cloneMockDevices,
  cloneMockEmployees,
  cloneMockHistories,
  createDeviceCommand,
  createDevice,
  createEmployee,
  createUser,
  deleteDevice,
  deleteEmployee,
  deleteUser,
  deleteKnowledgeDocument,
  fetchDeviceAlarms,
  fetchDeviceCommands,
  fetchDeviceConfig,
  fetchDeviceHistory,
  fetchDevices,
  fetchEmployees,
  fetchKnowledgeDocuments,
  fetchUsers,
  getSessionUser,
  login,
  mockDevices,
  reportDeviceData,
  resolveAlarm,
  resetUserPassword,
  updateDeviceConfig,
  updateUserRole,
  uploadKnowledgeFile,
  updateDevice,
} from './lib/opspilot'

const BG_VIDEO =
  'https://d8j0ntlcm91z4.cloudfront.net/user_38xzZboKViGWJOttwIXH07lWA1P/hf_20260511_230229_7c9bc431-46cf-489a-948d-e8144d8eb5d4.mp4'
const HERO_FREEZE_OFFSET_SECONDS = 1.45
const LOGIN_EXIT_DURATION_MS = 420
const LOGIN_REVEAL_DELAY_MS = 420
const LOGIN_UI_EXIT_DELAY_MS = 160
const CONSOLE_REVEAL_DURATION_MS = 320
const mockKnowledgeDocuments = ['设备说明书-温湿度传感器.pdf', '高温告警排障手册.pdf']

const consolePanelClass = 'ops-panel p-4'
const consoleSubtlePanelClass = 'ops-panel-subtle p-3'
const consoleInputClass = 'ops-input'
const consolePrimaryButtonClass = 'ops-primary-button disabled:cursor-not-allowed disabled:opacity-60'
const consoleSecondaryButtonClass = 'ops-secondary-button disabled:cursor-not-allowed disabled:opacity-60'
const consoleGhostButtonClass = 'ops-ghost-button disabled:cursor-not-allowed disabled:opacity-60'
const consoleRowCardClass = 'ops-row-card p-3'
const consoleCodeChipClass = 'ops-code-chip px-2.5 py-1.5 text-xs'

const loginParticles = [
  ['-42%', '-30%', '-168%', '-118%', '0ms'],
  ['-31%', '-44%', '-114%', '-172%', '40ms'],
  ['-16%', '-51%', '-44%', '-190%', '80ms'],
  ['5%', '-54%', '52%', '-205%', '20ms'],
  ['24%', '-46%', '126%', '-164%', '90ms'],
  ['39%', '-31%', '188%', '-108%', '50ms'],
  ['47%', '-11%', '214%', '-18%', '130ms'],
  ['43%', '14%', '176%', '76%', '70ms'],
  ['32%', '35%', '134%', '172%', '150ms'],
  ['12%', '48%', '42%', '206%', '100ms'],
  ['-10%', '50%', '-58%', '220%', '180ms'],
  ['-29%', '39%', '-148%', '168%', '120ms'],
  ['-43%', '19%', '-210%', '88%', '210ms'],
  ['-49%', '-4%', '-224%', '-24%', '160ms'],
  ['-3%', '-34%', '18%', '-148%', '240ms'],
  ['18%', '-24%', '94%', '-108%', '200ms'],
  ['26%', '2%', '156%', '14%', '280ms'],
  ['12%', '25%', '86%', '130%', '230ms'],
  ['-11%', '28%', '-82%', '142%', '310ms'],
  ['-25%', '6%', '-148%', '34%', '260ms'],
  ['-19%', '-18%', '-104%', '-86%', '340ms'],
  ['3%', '2%', '18%', '16%', '300ms'],
  ['35%', '-2%', '214%', '-10%', '360ms'],
  ['-36%', '-12%', '-190%', '-58%', '330ms'],
]

const consoleTabs = [
  { key: 'devices', label: '设备监控', icon: Cpu },
  { key: 'alerts', label: '告警中心', icon: BellRing },
  { key: 'rag', label: 'RAG 问答', icon: FileText },
  { key: 'employees', label: '员工管理', icon: Users },
  { key: 'users', label: '用户管理', icon: UserCog, adminOnly: true },
  { key: 'api', label: '接口说明', icon: Database },
]
const consoleRouteTabs = ['devices', 'alerts', 'rag', 'agent', 'employees', 'users', 'api']
const deviceCommandTypes = ['DISPLAY_MESSAGE', 'SET_DISPLAY_MODE', 'SET_SAMPLE_INTERVAL', 'RUN_SELF_TEST', 'RELAY_ON', 'RELAY_OFF', 'RELAY_PULSE', 'REBOOT']
const commandDefaultPayloads = {
  DISPLAY_MESSAGE: '正在巡检，请检查散热',
  SET_DISPLAY_MODE: 'ALERT',
  SET_SAMPLE_INTERVAL: '10',
  RUN_SELF_TEST: '',
  RELAY_ON: 'FAN',
  RELAY_OFF: 'FAN',
  RELAY_PULSE: 'FAN',
  REBOOT: '',
}
const commandPayloadPlaceholders = {
  DISPLAY_MESSAGE: '屏幕文字',
  SET_DISPLAY_MODE: 'NORMAL / ALERT / MAINTENANCE',
  SET_SAMPLE_INTERVAL: '采样秒数，如 10',
  RUN_SELF_TEST: '可留空',
  RELAY_ON: 'FAN',
  RELAY_OFF: 'FAN',
  RELAY_PULSE: 'FAN，固件脉冲 500ms',
  REBOOT: '确认重启',
}

const quickAgentQuestions = [
  'ESP32-001 最近 6 小时温度是不是在升高？',
  '把 ESP32-001 高温阈值调到 30 度',
  '打开 ESP32-001 继电器风扇',
  '让 ESP32-001 屏幕显示正在巡检',
]

const homeNavItems = [
  { label: '首页', path: '#/', icon: Home },
  { label: '设备监控', path: '#/console/devices', icon: Cpu },
  { label: '告警中心', path: '#/console/alerts', icon: BellRing },
]
const homePills = [
  { label: '设备上报', path: '#/console/devices' },
  { label: '自动告警', path: '#/console/alerts' },
  { label: 'RAG 问答', path: '#/console/rag' },
  { label: 'Agent 排障', path: '#/console/agent' },
]
const platformCards = [
  { icon: Bot, title: '会追问的 Agent', detail: '问题指代不清时先确认对象，不把“温度低于 0 度”这种问题硬答成错误建议。' },
  { icon: FileText, title: '知识库优先', detail: '内部说明书、排障手册和历史处理记录优先成为答案依据，来源可以回看。' },
  { icon: Network, title: '联网兜底', detail: '知识库证据不足时再查公网资料，把外部来源作为补充而不是胡乱猜测。' },
]
const loginStats = [
  { label: '设备在线', value: '2 / 3', icon: Cpu },
  { label: '待处理告警', value: '3', icon: BellRing },
  { label: 'Agent 工具链', value: '4 steps', icon: Workflow },
]
const apiGroups = [
  { title: '认证与用户', icon: Server, endpoints: ['POST /auth/login', 'GET /users', 'POST /users', 'PUT /users/{id}/password', 'PUT /users/{id}/role', 'DELETE /users/{id}'] },
  {
    title: '设备与告警',
    icon: Cpu,
    endpoints: ['GET /device/list', 'POST /device', 'PUT /device/{id}', 'DELETE /device/{id}', 'POST /device/report', 'GET /device/{deviceCode}/data', 'GET /device/{deviceCode}/alarms', 'GET /device/{deviceCode}/config', 'PUT /device/{deviceCode}/config', 'POST /device/{deviceCode}/commands'],
  },
  { title: '知识库', icon: FileText, endpoints: ['POST /pdf/upload', 'GET /pdf/ask', 'GET /pdf/chat', 'GET /pdf/documents', 'DELETE /pdf/documents/{documentName}'] },
  { title: 'Agent 与员工', icon: Bot, endpoints: ['POST /agent/chat', 'GET /employee', 'POST /employee', 'DELETE /employee/{id}'] },
]

const route = reactive(getRoute())
const menuOpen = ref(false)
const videoReady = ref(false)
const freezeLocked = ref(false)
const isAuthenticated = ref(Boolean(localStorage.getItem('opspilot-token')))
const sessionUser = ref(getSessionUser())
const postLoginPath = ref('#/console/devices')
const runMode = ref('connecting')
const notice = ref('正在准备演示数据...')
const syncing = ref(false)
const username = ref('admin')
const password = ref('')
const showPassword = ref(false)
const loginError = ref('')
const submitting = ref(false)
const isDissolving = ref(false)
const loginIntroReady = ref(false)
const loginLeaving = ref(false)
const consoleRevealing = ref(false)
const consoleSidebarCollapsed = ref(false)
const consoleAmbientEnabled = ref(true)
const deviceWorkspaceTab = ref('overview')
let loginIntroTimer = 0
let consoleRevealTimer = 0
let consolePointerFrame = 0

const devices = ref(cloneMockDevices())
const deviceHistoryMap = ref(cloneMockHistories())
const alarmMap = ref(cloneMockAlarms())
const deviceConfigMap = ref(cloneMockConfigs())
const deviceCommandMap = ref(cloneMockCommands())
const employees = ref(cloneMockEmployees())
const users = ref([])
const selectedDeviceCode = ref(mockDevices[0].deviceCode)
const deviceFormMode = ref('edit')
const deviceForm = reactive(toDeviceForm(mockDevices[0]))
const deviceConfigForm = reactive(toDeviceConfigForm(cloneMockConfigs()[mockDevices[0].deviceCode]))
const deviceCommandForm = reactive({ commandType: 'DISPLAY_MESSAGE', payload: '正在巡检，请检查散热' })
const reportForm = reactive({ temperature: '36.8', humidity: '61.2' })
const savingDevice = ref(false)
const savingConfig = ref(false)
const sendingCommand = ref(false)
const reporting = ref(false)
const removingDeviceId = ref(null)
const resolvingAlarmId = ref(null)

const initialKnowledge = buildMockKnowledgeAnswer('设备温度过高时应该如何排查？')
const knowledgeQuestion = ref('设备温度过高时应该如何排查？')
const knowledgeAnswer = ref(initialKnowledge.answer)
const knowledgeSources = ref(initialKnowledge.sources)
const knowledgeDocuments = ref([...mockKnowledgeDocuments])
const knowledgeUploadNote = ref('当前展示演示知识库。后端启动后可以上传 PDF 并真实索引。')
const uploadingKnowledge = ref(false)
const askingKnowledge = ref(false)
const deletingKnowledgeDocumentName = ref(null)

const memoryId = ref('default')
const agentInput = ref('ESP32-001 当前状态怎么样？')
const agentSessions = ref([{ id: 'default', title: '默认会话', messages: [buildInitialAgentMessage()] }])
const chattingSessionIds = ref([])
const maintenanceRunning = ref(false)
const maintenanceSteps = ref([])
const maintenanceSummary = ref('选择设备后，点击按钮即可把状态、告警、知识库和建议串成一条排障链路。')

const employeeForm = reactive({ name: '', age: '', department: '' })
const savingEmployee = ref(false)
const removingEmployeeId = ref(null)

const userForm = reactive({ username: '', password: '', role: 'OPERATOR' })
const userPasswordDrafts = reactive({})
const savingUser = ref(false)
const userActionId = ref(null)

const guardedPage = computed(() => (route.page === 'console' && !isAuthenticated.value ? 'login' : route.page))
const modeLabel = computed(() => runMode.value === 'online' ? '真实接口已就绪' : runMode.value === 'offline' ? '演示模式可用' : '准备连接后端')
const loginBusy = computed(() => submitting.value || syncing.value || isDissolving.value)
const runModeLabel = computed(() => runMode.value === 'online' ? '真实接口' : runMode.value === 'offline' ? '演示数据' : '连接中')
const canManageUsers = computed(() => sessionUser.value?.role === 'ADMIN')
const selectedDevice = computed(() => devices.value.find((device) => device.deviceCode === selectedDeviceCode.value))
const selectedHistory = computed(() => deviceHistoryMap.value[selectedDeviceCode.value] ?? [])
const selectedAlarms = computed(() => alarmMap.value[selectedDeviceCode.value] ?? [])
const selectedConfig = computed(() => deviceConfigMap.value[selectedDeviceCode.value])
const selectedCommands = computed(() => deviceCommandMap.value[selectedDeviceCode.value] ?? [])
const latestTelemetry = computed(() => selectedHistory.value[0])
const totalOpenAlerts = computed(() => Object.values(alarmMap.value).flat().filter((alarm) => isUnresolved(alarm.status)).length)
const onlineDeviceCount = computed(() => devices.value.filter((device) => device.online).length)
const activeTabLabel = computed(() => consoleTabs.find((item) => item.key === route.tab)?.label ?? '设备监控')
const consoleNavItems = computed(() => consoleTabs
  .filter((item) => !item.adminOnly || canManageUsers.value)
  .map(({ key, label, icon }) => ({ label, icon, path: `#/console/${key}` })))
const activeAgentSession = computed(() => agentSessions.value.find((session) => session.id === memoryId.value) ?? agentSessions.value[0])
const agentMessages = computed(() => activeAgentSession.value?.messages ?? [])
const chatting = computed(() => chattingSessionIds.value.includes(memoryId.value))
const latestAssistantMessage = computed(() => [...agentMessages.value].reverse().find((message) => message.role === 'assistant'))
const latestUserMessage = computed(() => [...agentMessages.value].reverse().find((message) => message.role === 'user'))
const latestTrace = computed(() => latestAssistantMessage.value?.trace ?? [])
const latestSources = computed(() => latestAssistantMessage.value?.sources ?? [])
const activeSessionTitle = computed(() => activeAgentSession.value?.title ?? '默认会话')
const agentTasks = computed(() => [
  { label: '设备状态', icon: Cpu, prompt: `${selectedDeviceCode.value} 当前状态怎么样？` },
  { label: '告警闭环', icon: AlertTriangle, prompt: `${selectedDeviceCode.value} 有哪些未处理告警？请按优先级给出闭环建议。` },
  { label: 'RAG 排障', icon: FileText, prompt: `结合知识库，帮我诊断 ${selectedDeviceCode.value} 的高温问题。` },
  { label: '巡检计划', icon: Workflow, prompt: `为 ${selectedDeviceCode.value} 生成今天的巡检步骤，并标出需要人工确认的动作。` },
  { label: '变更建议', icon: ShieldCheck, prompt: `评估 ${selectedDeviceCode.value} 是否适合调整采样频率，并说明风险。` },
  { label: '接口说明', icon: Database, prompt: '说明当前系统里 Agent 可以调用哪些接口，以及每个接口适合什么场景。' },
])

function getRoute() {
  const hash = window.location.hash.replace(/^#\/?/, '')
  const [page, tab] = hash.split('/')
  if (page === 'console') return { page: 'console', tab: consoleRouteTabs.includes(tab) ? tab : 'devices' }
  if (page === 'login') return { page: 'login', tab: 'devices' }
  return { page: 'home', tab: 'devices' }
}

function navigate(path) {
  if (window.location.hash === path) {
    window.dispatchEvent(new HashChangeEvent('hashchange'))
    return
  }
  window.location.hash = path
}

function handleRouteChange() {
  const next = getRoute()
  if (route.page !== next.page) window.scrollTo({ top: 0, behavior: 'auto' })
  route.page = next.page
  route.tab = next.tab
  menuOpen.value = false
}

function scrollToOverview() {
  document.getElementById('scroll-overview')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function sleep(ms) {
  return new Promise((resolve) => window.setTimeout(resolve, ms))
}

function clearLoginIntroTimer() {
  if (!loginIntroTimer) return
  window.clearTimeout(loginIntroTimer)
  loginIntroTimer = 0
}

function clearConsoleRevealTimer() {
  if (!consoleRevealTimer) return
  window.clearTimeout(consoleRevealTimer)
  consoleRevealTimer = 0
}

function handleConsolePointerMove(event) {
  if (!consoleAmbientEnabled.value) return
  if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
  const node = event.currentTarget
  if (!(node instanceof HTMLElement)) return
  window.cancelAnimationFrame(consolePointerFrame)
  consolePointerFrame = window.requestAnimationFrame(() => {
    const rect = node.getBoundingClientRect()
    const x = ((event.clientX - rect.left) / rect.width) * 100
    const y = ((event.clientY - rect.top) / Math.max(rect.height, window.innerHeight)) * 100
    node.style.setProperty('--ops-console-pointer-x', `${Math.max(0, Math.min(100, x))}%`)
    node.style.setProperty('--ops-console-pointer-y', `${Math.max(0, Math.min(100, y))}%`)
    node.style.setProperty('--ops-console-shift-x', `${Math.max(-7, Math.min(7, (x - 50) * 0.14))}px`)
    node.style.setProperty('--ops-console-shift-y', `${Math.max(-5, Math.min(5, (y - 35) * 0.1))}px`)
  })
}

function handleConsolePointerLeave(event) {
  const node = event.currentTarget
  if (!(node instanceof HTMLElement)) return
  node.style.setProperty('--ops-console-pointer-x', '72%')
  node.style.setProperty('--ops-console-pointer-y', '12%')
  node.style.setProperty('--ops-console-shift-x', '0px')
  node.style.setProperty('--ops-console-shift-y', '0px')
}

function restartLoginIntro() {
  clearLoginIntroTimer()
  if (guardedPage.value !== 'login') return
  loginIntroReady.value = false
  loginLeaving.value = false
  isDissolving.value = false
  loginIntroTimer = window.setTimeout(() => {
    loginIntroReady.value = true
    loginIntroTimer = 0
  }, LOGIN_REVEAL_DELAY_MS)
}

function freezeVideo(event) {
  const video = event.currentTarget
  if (!Number.isFinite(video.duration) || video.duration <= 0 || freezeLocked.value) return
  const naturalFreezeAt = Math.max(0, video.duration - HERO_FREEZE_OFFSET_SECONDS)
  if (video.currentTime < naturalFreezeAt) return
  freezeLocked.value = true
  video.pause()
}

function freezeEnded(event) {
  freezeLocked.value = true
  event.currentTarget.pause()
}

function formatDateTime(value) {
  if (!value) return '暂无记录'
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return value
  return date.toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function isUnresolved(status) {
  return !status.includes('已处理') && !status.toLowerCase().includes('resolved')
}

function alarmTone(status) {
  if (status.includes('处理中')) return 'border-amber-400/25 bg-amber-500/12 text-amber-100'
  if (status.includes('已处理')) return 'border-emerald-400/25 bg-emerald-500/12 text-emerald-100'
  return 'border-rose-400/25 bg-rose-500/12 text-rose-100'
}

function commandTone(status) {
  if (status === 'DONE') return 'border-emerald-400/25 bg-emerald-500/12 text-emerald-100'
  if (status === 'FAILED') return 'border-rose-400/25 bg-rose-500/12 text-rose-100'
  if (status === 'SENT') return 'border-sky-400/25 bg-sky-500/12 text-sky-100'
  return 'border-amber-400/25 bg-amber-500/12 text-amber-100'
}

function toDeviceForm(device) {
  return {
    deviceCode: device?.deviceCode ?? '',
    name: device?.name ?? '',
    type: device?.type ?? '',
    location: device?.location ?? '',
  }
}

function toDeviceConfigForm(config) {
  return {
    temperatureThreshold: String(config?.temperatureThreshold ?? 35),
    humidityMinThreshold: String(config?.humidityMinThreshold ?? 20),
    humidityMaxThreshold: String(config?.humidityMaxThreshold ?? 80),
    sampleIntervalSeconds: String(config?.sampleIntervalSeconds ?? 10),
    inspectionIntervalMinutes: String(config?.inspectionIntervalMinutes ?? 30),
    displayMode: config?.displayMode ?? 'NORMAL',
  }
}

function assignReactive(target, source) {
  Object.keys(target).forEach((key) => {
    target[key] = source[key] ?? ''
  })
}

function buildConfigPayload() {
  return {
    temperatureThreshold: Number(deviceConfigForm.temperatureThreshold),
    humidityMinThreshold: Number(deviceConfigForm.humidityMinThreshold),
    humidityMaxThreshold: Number(deviceConfigForm.humidityMaxThreshold),
    sampleIntervalSeconds: Number(deviceConfigForm.sampleIntervalSeconds),
    inspectionIntervalMinutes: Number(deviceConfigForm.inspectionIntervalMinutes),
    displayMode: deviceConfigForm.displayMode.trim().toUpperCase(),
  }
}

function sortHistoryEntries(history) {
  return history
    .map((item) => ({ ...item, temperature: Number(item.temperature), humidity: Number(item.humidity) }))
    .sort((a, b) => +new Date(b.reportTime) - +new Date(a.reportTime))
}

function sortAlarmEntries(alarms) {
  return [...alarms].sort((a, b) => +new Date(b.createTime) - +new Date(a.createTime))
}

function buildKnowledgeNote(documents, isOnline) {
  if (!isOnline) return '当前展示演示知识库。后端启动后可以上传 PDF 并真实索引。'
  return documents.length ? `已接入真实知识库，当前收录 ${documents.length} 份文档。` : '已接入真实知识库，当前还没有上传任何 PDF 文档。'
}

function buildInitialAgentMessage() {
  return {
    id: 'assistant-initial',
    role: 'assistant',
    content: '你好，我是 OpsPilot Agent。你可以问我设备状态、告警历史，或者让我结合知识库给出排障建议。',
    trace: [],
    sources: buildMockKnowledgeSources('设备温度过高时应该如何排查？'),
  }
}

async function collectDeviceCollections(deviceList) {
  const nextHistoryMap = {}
  const nextAlarmMap = {}
  const nextConfigMap = {}
  const nextCommandMap = {}
  const telemetryResults = await Promise.all(deviceList.map(async (device) => {
    const [historyResult, alarmResult, configResult, commandResult] = await Promise.allSettled([
      fetchDeviceHistory(device.deviceCode),
      fetchDeviceAlarms(device.deviceCode),
      fetchDeviceConfig(device.deviceCode),
      fetchDeviceCommands(device.deviceCode),
    ])
    return {
      deviceCode: device.deviceCode,
      history: historyResult.status === 'fulfilled' ? sortHistoryEntries(historyResult.value) : [],
      alarms: alarmResult.status === 'fulfilled' ? sortAlarmEntries(alarmResult.value) : [],
      config: configResult.status === 'fulfilled' ? configResult.value : undefined,
      commands: commandResult.status === 'fulfilled' ? commandResult.value : [],
    }
  }))
  telemetryResults.forEach(({ deviceCode, history, alarms, config, commands }) => {
    nextHistoryMap[deviceCode] = history
    nextAlarmMap[deviceCode] = alarms
    if (config) nextConfigMap[deviceCode] = config
    nextCommandMap[deviceCode] = commands
  })
  return { nextHistoryMap, nextAlarmMap, nextConfigMap, nextCommandMap }
}

function applyMockState(customNotice) {
  clearToken()
  sessionUser.value = null
  runMode.value = 'offline'
  devices.value = cloneMockDevices()
  deviceHistoryMap.value = cloneMockHistories()
  alarmMap.value = cloneMockAlarms()
  deviceConfigMap.value = cloneMockConfigs()
  deviceCommandMap.value = cloneMockCommands()
  employees.value = cloneMockEmployees()
  users.value = []
  selectedDeviceCode.value = mockDevices[0]?.deviceCode ?? ''
  knowledgeDocuments.value = [...mockKnowledgeDocuments]
  knowledgeUploadNote.value = buildKnowledgeNote(mockKnowledgeDocuments, false)
  deviceFormMode.value = 'edit'
  notice.value = customNotice
}

async function refreshSingleDevice(deviceCode) {
  const [history, alarms, config, commands] = await Promise.all([
    fetchDeviceHistory(deviceCode),
    fetchDeviceAlarms(deviceCode),
    fetchDeviceConfig(deviceCode),
    fetchDeviceCommands(deviceCode),
  ])
  deviceHistoryMap.value = { ...deviceHistoryMap.value, [deviceCode]: sortHistoryEntries(history) }
  alarmMap.value = { ...alarmMap.value, [deviceCode]: sortAlarmEntries(alarms) }
  deviceConfigMap.value = { ...deviceConfigMap.value, [deviceCode]: config }
  deviceCommandMap.value = { ...deviceCommandMap.value, [deviceCode]: commands }
}

async function refreshDeviceCollection(preferredDeviceCode) {
  const safeDevices = await fetchDevices()
  const collections = await collectDeviceCollections(safeDevices)
  devices.value = safeDevices
  deviceHistoryMap.value = collections.nextHistoryMap
  alarmMap.value = collections.nextAlarmMap
  deviceConfigMap.value = collections.nextConfigMap
  deviceCommandMap.value = collections.nextCommandMap
  selectedDeviceCode.value = preferredDeviceCode && safeDevices.some((device) => device.deviceCode === preferredDeviceCode)
    ? preferredDeviceCode
    : safeDevices.some((device) => device.deviceCode === selectedDeviceCode.value)
      ? selectedDeviceCode.value
      : safeDevices[0]?.deviceCode ?? ''
}

async function hydrateProjectFromBackend() {
  sessionUser.value = getSessionUser()
  const [deviceList, employeeList, knowledgeDocumentList, userList] = await Promise.all([
      fetchDevices(),
      fetchEmployees().catch(() => []),
      fetchKnowledgeDocuments().catch(() => []),
      sessionUser.value?.role === 'ADMIN' ? fetchUsers().catch(() => []) : Promise.resolve([]),
  ])
  const collections = await collectDeviceCollections(deviceList)
  runMode.value = 'online'
  devices.value = deviceList
  employees.value = employeeList
  users.value = userList
  deviceHistoryMap.value = collections.nextHistoryMap
  alarmMap.value = collections.nextAlarmMap
  deviceConfigMap.value = collections.nextConfigMap
  deviceCommandMap.value = collections.nextCommandMap
  knowledgeDocuments.value = knowledgeDocumentList
  knowledgeUploadNote.value = buildKnowledgeNote(knowledgeDocumentList, true)
  selectedDeviceCode.value = deviceList.some((device) => device.deviceCode === selectedDeviceCode.value) ? selectedDeviceCode.value : deviceList[0]?.deviceCode ?? ''
  notice.value = '已连接本地后端，当前控制台正在使用真实接口数据。'
}

async function syncProject() {
  syncing.value = true
  notice.value = '正在连接本地 Spring Boot 后端...'
  try {
    if (!localStorage.getItem('opspilot-token')) {
      await login(username.value.trim(), password.value)
    }
    sessionUser.value = getSessionUser()
    await hydrateProjectFromBackend()
  } catch (error) {
    clearToken()
    sessionUser.value = null
    const message = error instanceof Error ? error.message : '后端连接失败。'
    if (runMode.value === 'offline') {
      notice.value = `演示数据保持不变；真实接口连接失败：${message}`
    } else {
      isAuthenticated.value = false
      loginError.value = message
      navigate('#/login')
    }
  } finally {
    syncing.value = false
  }
}

async function revealConsole() {
  loginLeaving.value = true
  await sleep(LOGIN_UI_EXIT_DELAY_MS)
  isDissolving.value = true
  await sleep(LOGIN_EXIT_DURATION_MS)
  isAuthenticated.value = true
  consoleRevealing.value = true
  clearConsoleRevealTimer()
  navigate(postLoginPath.value)
  consoleRevealTimer = window.setTimeout(() => {
    consoleRevealing.value = false
    isDissolving.value = false
    loginLeaving.value = false
    consoleRevealTimer = 0
  }, CONSOLE_REVEAL_DURATION_MS)
}

async function handleLoginSubmit() {
  if (!username.value.trim() || !password.value.trim()) {
    loginError.value = '请填写用户名和密码。'
    return
  }
  loginError.value = ''
  submitting.value = true
  syncing.value = true
  try {
    await login(username.value.trim(), password.value)
    sessionUser.value = getSessionUser()
    await hydrateProjectFromBackend()
    await revealConsole()
  } catch (error) {
    clearToken()
    sessionUser.value = null
    loginError.value = error instanceof Error ? error.message : '进入控制台失败，请稍后重试。'
    isDissolving.value = false
    loginLeaving.value = false
    loginIntroReady.value = true
  } finally {
    syncing.value = false
    submitting.value = false
  }
}

function handleSelectDevice(deviceCode) {
  deviceFormMode.value = 'edit'
  selectedDeviceCode.value = deviceCode
  deviceWorkspaceTab.value = 'overview'
}

function handleCreateDeviceMode() {
  deviceFormMode.value = 'create'
  assignReactive(deviceForm, toDeviceForm())
  deviceWorkspaceTab.value = 'settings'
}

async function handleDeviceSave() {
  const payload = {
    deviceCode: deviceForm.deviceCode.trim(),
    name: deviceForm.name.trim(),
    type: deviceForm.type.trim(),
    location: deviceForm.location.trim(),
    online: selectedDevice.value?.online,
    lastOnlineTime: selectedDevice.value?.lastOnlineTime ?? null,
  }
  if (!payload.deviceCode || !payload.name || !payload.type || !payload.location) {
    notice.value = '请完整填写设备编号、名称、类型和位置。'
    return
  }
  savingDevice.value = true
  try {
    if (deviceFormMode.value === 'create') {
      let provisionedToken = ''
      if (runMode.value === 'online') {
        const registration = await createDevice(payload)
        provisionedToken = registration.deviceToken
        await refreshDeviceCollection(payload.deviceCode)
      } else {
        const nextDevice = { id: Date.now(), ...payload, online: false, lastOnlineTime: null, deviceToken: `${payload.deviceCode.toLowerCase()}-token` }
        devices.value = [nextDevice, ...devices.value]
        deviceHistoryMap.value = { ...deviceHistoryMap.value, [nextDevice.deviceCode]: [] }
        alarmMap.value = { ...alarmMap.value, [nextDevice.deviceCode]: [] }
        deviceCommandMap.value = { ...deviceCommandMap.value, [nextDevice.deviceCode]: [] }
        deviceConfigMap.value = { ...deviceConfigMap.value, [nextDevice.deviceCode]: { id: Date.now() + 2, deviceId: nextDevice.id, temperatureThreshold: 35, humidityMinThreshold: 20, humidityMaxThreshold: 80, sampleIntervalSeconds: 10, inspectionIntervalMinutes: 30, displayMode: 'NORMAL', updateTime: new Date().toISOString() } }
        selectedDeviceCode.value = nextDevice.deviceCode
      }
      deviceFormMode.value = 'edit'
      notice.value = provisionedToken
        ? `已新增 ${payload.deviceCode}。请立即保存设备 Token（仅返回一次）：${provisionedToken}`
        : `演示模式已新增设备 ${payload.deviceCode}。`
      return
    }
    if (!selectedDevice.value) return
    if (runMode.value === 'online') {
      await updateDevice(selectedDevice.value.id, payload)
      await refreshDeviceCollection(payload.deviceCode)
    } else {
      devices.value = devices.value.map((device) => device.id === selectedDevice.value.id ? { ...device, ...payload } : device)
      selectedDeviceCode.value = payload.deviceCode
    }
    notice.value = `${runMode.value === 'online' ? '已通过真实接口更新设备' : '演示模式已更新设备'} ${payload.deviceCode}。`
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '保存设备失败。'
  } finally {
    savingDevice.value = false
  }
}

async function handleDeviceRemove() {
  if (!selectedDevice.value || deviceFormMode.value === 'create') return
  removingDeviceId.value = selectedDevice.value.id
  try {
    const removed = selectedDevice.value
    if (runMode.value === 'online') await deleteDevice(removed.id)
    devices.value = devices.value.filter((device) => device.id !== removed.id)
    selectedDeviceCode.value = devices.value[0]?.deviceCode ?? ''
    notice.value = `${runMode.value === 'online' ? '已通过真实接口删除设备' : '演示模式已删除设备'} ${removed.deviceCode}。`
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '删除设备失败。'
  } finally {
    removingDeviceId.value = null
  }
}

async function handleReportSubmit() {
  if (!selectedDevice.value) return
  const temperature = Number(reportForm.temperature)
  const humidity = Number(reportForm.humidity)
  if (Number.isNaN(temperature) || Number.isNaN(humidity)) {
    notice.value = '请输入有效的温度与湿度数值。'
    return
  }
  reporting.value = true
  try {
    if (runMode.value === 'online') {
      await reportDeviceData(selectedDevice.value.deviceCode, temperature, humidity)
      await refreshSingleDevice(selectedDevice.value.deviceCode)
      notice.value = `已向 ${selectedDevice.value.deviceCode} 写入真实上报，并刷新遥测与告警。`
    } else {
      const reportTime = new Date().toISOString()
      const nextData = { id: Date.now(), deviceId: selectedDevice.value.id, temperature, humidity, rssi: latestTelemetry.value?.rssi ?? -58, uptimeSeconds: (latestTelemetry.value?.uptimeSeconds ?? 0) + 10, firmwareVersion: latestTelemetry.value?.firmwareVersion ?? '1.0.0', reportTime }
      deviceHistoryMap.value = { ...deviceHistoryMap.value, [selectedDevice.value.deviceCode]: [nextData, ...(selectedHistory.value ?? [])] }
      if (temperature > (selectedConfig.value?.temperatureThreshold ?? 35)) {
        const nextAlarm = { id: Date.now() + 1, deviceId: selectedDevice.value.id, alarmType: '高温告警', alarmValue: `${temperature.toFixed(1)}°C`, status: '未处理', createTime: reportTime }
        alarmMap.value = { ...alarmMap.value, [selectedDevice.value.deviceCode]: [nextAlarm, ...(selectedAlarms.value ?? [])] }
      }
      notice.value = `演示模式已为 ${selectedDevice.value.deviceCode} 追加上报。`
    }
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '设备上报失败，请稍后重试。'
  } finally {
    reporting.value = false
  }
}

async function handleConfigSave() {
  if (!selectedDevice.value) return
  const payload = buildConfigPayload()
  savingConfig.value = true
  try {
    if (runMode.value === 'online') {
      const nextConfig = await updateDeviceConfig(selectedDevice.value.deviceCode, payload)
      await refreshSingleDevice(selectedDevice.value.deviceCode)
      deviceConfigMap.value = { ...deviceConfigMap.value, [selectedDevice.value.deviceCode]: nextConfig }
    } else {
      const now = new Date().toISOString()
      const nextConfig = { id: selectedConfig.value?.id ?? Date.now(), deviceId: selectedDevice.value.id, ...payload, updateTime: now }
      const generatedCommands = [
        { id: Date.now() + 10, deviceId: selectedDevice.value.id, commandType: 'SET_SAMPLE_INTERVAL', payload: String(payload.sampleIntervalSeconds), status: 'PENDING', issuedBy: 'admin', resultMessage: null, createTime: now, sentTime: null, doneTime: null },
        { id: Date.now() + 11, deviceId: selectedDevice.value.id, commandType: 'SET_DISPLAY_MODE', payload: payload.displayMode, status: 'PENDING', issuedBy: 'admin', resultMessage: null, createTime: now, sentTime: null, doneTime: null },
      ]
      deviceConfigMap.value = { ...deviceConfigMap.value, [selectedDevice.value.deviceCode]: nextConfig }
      deviceCommandMap.value = { ...deviceCommandMap.value, [selectedDevice.value.deviceCode]: [...generatedCommands, ...selectedCommands.value] }
    }
    notice.value = `已更新 ${selectedDevice.value.deviceCode} 配置，并同步命令队列。`
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '保存设备配置失败。'
  } finally {
    savingConfig.value = false
  }
}

function shouldUseDefaultPayload(currentPayload, previousCommandType) {
  const previousDefault = commandDefaultPayloads[previousCommandType] ?? ''
  return currentPayload.trim() === '' || currentPayload === previousDefault
}

function changeCommandType(commandType) {
  const previousType = deviceCommandForm.commandType
  const previousPayload = deviceCommandForm.payload
  deviceCommandForm.commandType = commandType
  deviceCommandForm.payload = shouldUseDefaultPayload(previousPayload, previousType) ? commandDefaultPayloads[commandType] ?? '' : previousPayload
}

async function handleCommandSend() {
  if (!selectedDevice.value) return
  const commandType = deviceCommandForm.commandType.trim().toUpperCase()
  const payload = deviceCommandForm.payload.trim()
  if (commandType === 'REBOOT' && !payload.includes('确认')) {
    notice.value = '重启属于高风险命令，请在参数里写入“确认重启”。'
    return
  }
  sendingCommand.value = true
  try {
    if (runMode.value === 'online') {
      await createDeviceCommand(selectedDevice.value.deviceCode, { commandType, payload })
      await refreshSingleDevice(selectedDevice.value.deviceCode)
    } else {
      const command = { id: Date.now(), deviceId: selectedDevice.value.id, commandType, payload, status: 'PENDING', issuedBy: 'admin', resultMessage: null, createTime: new Date().toISOString(), sentTime: null, doneTime: null }
      deviceCommandMap.value = { ...deviceCommandMap.value, [selectedDevice.value.deviceCode]: [command, ...selectedCommands.value] }
    }
    notice.value = `已向 ${selectedDevice.value.deviceCode} 下发硬件命令 ${commandType}。`
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '设备命令下发失败。'
  } finally {
    sendingCommand.value = false
  }
}

async function handleResolveAlarm(deviceCode, alarmId) {
  resolvingAlarmId.value = alarmId
  try {
    if (runMode.value === 'online') {
      await resolveAlarm(deviceCode, alarmId)
      await refreshSingleDevice(deviceCode)
    } else {
      alarmMap.value = { ...alarmMap.value, [deviceCode]: (alarmMap.value[deviceCode] ?? []).map((alarm) => alarm.id === alarmId ? { ...alarm, status: '已处理' } : alarm) }
    }
    notice.value = `已处理 ${deviceCode} 的告警。`
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '告警处理失败。'
  } finally {
    resolvingAlarmId.value = null
  }
}

async function handleKnowledgeQuestion() {
  const question = knowledgeQuestion.value.trim()
  if (!question) return
  askingKnowledge.value = true
  try {
    const result = runMode.value === 'online' ? await askKnowledge(question) : buildMockKnowledgeAnswer(question)
    knowledgeAnswer.value = result.answer
    knowledgeSources.value = result.sources
    notice.value = runMode.value === 'online' ? '已完成真实 RAG 查询。' : '当前展示演示模式 RAG 回答。'
  } catch (error) {
    const fallback = buildMockKnowledgeAnswer(question)
    knowledgeAnswer.value = fallback.answer
    knowledgeSources.value = fallback.sources
    notice.value = error instanceof Error ? error.message : 'RAG 查询失败，已回退到演示回答。'
  } finally {
    askingKnowledge.value = false
  }
}

async function handleKnowledgeUpload(event) {
  const file = event.target.files?.[0]
  if (!file) return
  uploadingKnowledge.value = true
  try {
    if (runMode.value === 'online') {
      await uploadKnowledgeFile(file)
      const documents = await fetchKnowledgeDocuments().catch(() => [])
      knowledgeDocuments.value = [file.name, ...documents.filter((name) => name !== file.name)]
    } else {
      knowledgeDocuments.value = knowledgeDocuments.value.includes(file.name) ? knowledgeDocuments.value : [file.name, ...knowledgeDocuments.value]
    }
    knowledgeUploadNote.value = runMode.value === 'online' ? `已接入真实知识库，最近上传：${file.name}` : `演示模式已模拟导入 ${file.name}。`
    notice.value = 'PDF 已上传，知识库索引结果已更新。'
  } catch (error) {
    knowledgeUploadNote.value = `上传失败：${error instanceof Error ? error.message : '未知错误'}`
    notice.value = 'PDF 上传失败，请检查后端与依赖服务。'
  } finally {
    uploadingKnowledge.value = false
    event.target.value = ''
  }
}

async function handleKnowledgeDocumentDelete(documentName) {
  deletingKnowledgeDocumentName.value = documentName
  try {
    if (runMode.value === 'online') await deleteKnowledgeDocument(documentName)
    knowledgeDocuments.value = knowledgeDocuments.value.filter((item) => item !== documentName)
    knowledgeUploadNote.value = buildKnowledgeNote(knowledgeDocuments.value, runMode.value === 'online')
    notice.value = `已删除知识库文档 ${documentName}。`
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '删除知识库文档失败。'
  } finally {
    deletingKnowledgeDocumentName.value = null
  }
}

function updateAgentSessionMessages(chatId, updater) {
  agentSessions.value = agentSessions.value.map((session) => session.id === chatId ? { ...session, messages: updater(session.messages) } : session)
}

function createAgentSession(seedDeviceCode = selectedDeviceCode.value) {
  const id = `chat-${Date.now()}-${Math.random().toString(36).slice(2, 8)}`
  agentSessions.value = [...agentSessions.value, { id, title: seedDeviceCode ? `${seedDeviceCode} 会话` : '新会话', messages: [buildInitialAgentMessage()] }]
  memoryId.value = id
  agentInput.value = seedDeviceCode ? `${seedDeviceCode} 当前状态怎么样？` : ''
}

function selectAgentSession(chatId) {
  memoryId.value = chatId
  agentInput.value = ''
}

function markAgentSessionChatting(chatId, nextChatting) {
  chattingSessionIds.value = nextChatting
    ? chattingSessionIds.value.includes(chatId) ? chattingSessionIds.value : [...chattingSessionIds.value, chatId]
    : chattingSessionIds.value.filter((id) => id !== chatId)
}

async function handleAgentSend(messageOverride) {
  const chatId = memoryId.value
  if (chattingSessionIds.value.includes(chatId)) return
  const content = (messageOverride ?? agentInput.value).trim()
  if (!content) return
  updateAgentSessionMessages(chatId, (previous) => [...previous, { id: `${Date.now()}-user`, role: 'user', content }])
  agentInput.value = ''
  markAgentSessionChatting(chatId, true)
  try {
    const reply = runMode.value === 'online'
      ? await chatWithAgent(content, chatId)
      : buildMockAgentAnswer(content, selectedDevice.value, latestTelemetry.value, selectedAlarms.value)
    updateAgentSessionMessages(chatId, (previous) => [...previous, { id: `${Date.now()}-assistant`, role: 'assistant', content: reply.answer, trace: reply.trace, sources: reply.sources }])
    notice.value = runMode.value === 'online' ? 'Agent 已完成真实工具链回答。' : '当前展示演示模式 Agent 回答。'
  } catch (error) {
    updateAgentSessionMessages(chatId, (previous) => [...previous, { id: `${Date.now()}-agent-error`, role: 'assistant', content: `真实 Agent 调用失败：${error instanceof Error ? error.message : 'Agent 调用失败。'}`, trace: [], sources: [] }])
    notice.value = error instanceof Error ? error.message : 'Agent 调用失败。'
  } finally {
    markAgentSessionChatting(chatId, false)
  }
}

async function handleRunMaintenance() {
  if (maintenanceRunning.value) return
  const deviceCode = selectedDevice.value?.deviceCode ?? selectedDeviceCode.value
  const steps = [
    { id: 'status', title: '查设备状态', detail: `${deviceCode} ${selectedDevice.value?.online ? '在线' : '离线'}，最新温度 ${latestTelemetry.value?.temperature ?? '--'}°C。`, status: 'pending' },
    { id: 'alarms', title: '查告警历史', detail: selectedAlarms.value.length ? `发现 ${selectedAlarms.value.length} 条告警，优先处理 ${selectedAlarms.value[0].alarmType}。` : '当前设备暂无未处理告警。', status: 'pending' },
    { id: 'trend', title: '回放遥测趋势', detail: `最近 ${selectedHistory.value.length} 条样本，判断温度是否持续上升。`, status: 'pending' },
    { id: 'command', title: '下发现场提示', detail: '高风险时向 ESP32 OLED 下发巡检提示，等待硬件回执。', status: 'pending' },
    { id: 'suggestion', title: '生成排查建议', detail: '合并状态、趋势、阈值、告警和知识库来源，输出可执行建议。', status: 'pending' },
  ]
  maintenanceRunning.value = true
  maintenanceSummary.value = '正在编排排障链路...'
  maintenanceSteps.value = steps
  for (const step of steps) {
    maintenanceSteps.value = maintenanceSteps.value.map((item) => item.id === step.id ? { ...item, status: 'running' } : item)
    await new Promise((resolve) => window.setTimeout(resolve, 260))
    maintenanceSteps.value = maintenanceSteps.value.map((item) => item.id === step.id ? { ...item, status: 'done' } : item)
  }
  let summary = selectedAlarms.value.length
    ? `${deviceCode} 建议先确认散热和传感器安装位置，再按告警记录完成处理闭环。`
    : `${deviceCode} 当前风险较低，建议保持遥测观察并记录本次巡检。`
  let answer = buildMockAgentAnswer('帮我排查高温告警并让屏幕显示正在巡检', selectedDevice.value, latestTelemetry.value, selectedAlarms.value)
  if (runMode.value === 'online') {
    try {
      answer = await chatWithAgent(`帮我诊断 ${deviceCode} 的硬件状态，检查趋势、阈值和告警，如果高温请给屏幕下发提示`, memoryId.value)
      summary = answer.answer
      await refreshSingleDevice(deviceCode).catch(() => undefined)
    } catch {
      summary = `${deviceCode} 真实 Agent 暂时不可用，已保留前端排障流程。`
    }
  }
  maintenanceSummary.value = summary
  updateAgentSessionMessages(memoryId.value, (previous) => [...previous, { id: `${Date.now()}-maintenance`, role: 'assistant', content: summary, trace: answer.trace, sources: answer.sources }])
  notice.value = '已生成一键排障流程，并把 trace 与来源同步到 Agent 对话。'
  maintenanceRunning.value = false
}

async function handleEmployeeSave() {
  const age = Number(employeeForm.age)
  if (!employeeForm.name.trim() || !employeeForm.department.trim() || Number.isNaN(age)) {
    notice.value = '请完整填写员工姓名、年龄和部门。'
    return
  }
  savingEmployee.value = true
  try {
    if (runMode.value === 'online') {
      await createEmployee({ name: employeeForm.name.trim(), age, department: employeeForm.department.trim() })
      employees.value = await fetchEmployees()
    } else {
      employees.value = [{ id: Date.now(), name: employeeForm.name.trim(), age, department: employeeForm.department.trim() }, ...employees.value]
    }
    employeeForm.name = ''
    employeeForm.age = ''
    employeeForm.department = ''
    notice.value = '已新增员工记录。'
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '新增员工失败。'
  } finally {
    savingEmployee.value = false
  }
}

async function handleEmployeeRemove(employeeId) {
  removingEmployeeId.value = employeeId
  try {
    if (runMode.value === 'online') await deleteEmployee(employeeId)
    employees.value = employees.value.filter((employee) => employee.id !== employeeId)
    notice.value = '已删除员工记录。'
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '删除员工失败。'
  } finally {
    removingEmployeeId.value = null
  }
}

async function refreshUsers() {
  if (!canManageUsers.value || runMode.value !== 'online') {
    users.value = []
    return
  }
  users.value = await fetchUsers()
}

async function handleUserCreate() {
  const payload = {
    username: userForm.username.trim(),
    password: userForm.password,
    role: userForm.role,
  }
  if (!payload.username || !payload.password) {
    notice.value = '请填写新账户的用户名和初始密码。'
    return
  }
  if (payload.password.length < 8) {
    notice.value = '初始密码至少需要 8 个字符。'
    return
  }
  savingUser.value = true
  try {
    await createUser(payload)
    await refreshUsers()
    userForm.username = ''
    userForm.password = ''
    userForm.role = 'OPERATOR'
    notice.value = `账户 ${payload.username} 创建成功。`
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '创建账户失败。'
  } finally {
    savingUser.value = false
  }
}

async function handleUserRoleUpdate(user, role) {
  if (user.role === role) return
  userActionId.value = `role-${user.id}`
  try {
    await updateUserRole(user.id, role)
    await refreshUsers()
    notice.value = `已将 ${user.username} 的角色修改为 ${role}。`
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '修改用户角色失败。'
  } finally {
    userActionId.value = null
  }
}

async function handleUserPasswordReset(user) {
  const nextPassword = String(userPasswordDrafts[user.id] ?? '')
  if (nextPassword.length < 8) {
    notice.value = '新密码至少需要 8 个字符。'
    return
  }
  userActionId.value = `password-${user.id}`
  try {
    await resetUserPassword(user.id, nextPassword)
    userPasswordDrafts[user.id] = ''
    notice.value = `已重置 ${user.username} 的密码。`
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '重置密码失败。'
  } finally {
    userActionId.value = null
  }
}

async function handleUserDelete(user) {
  if (!window.confirm(`确定删除账户 ${user.username} 吗？此操作无法撤销。`)) return
  userActionId.value = `delete-${user.id}`
  try {
    await deleteUser(user.id)
    await refreshUsers()
    notice.value = `账户 ${user.username} 已删除。`
  } catch (error) {
    notice.value = error instanceof Error ? error.message : '删除账户失败。'
  } finally {
    userActionId.value = null
  }
}

watch(selectedDevice, (device) => {
  if (deviceFormMode.value !== 'create') assignReactive(deviceForm, toDeviceForm(device))
})

watch(selectedConfig, (config) => {
  assignReactive(deviceConfigForm, toDeviceConfigForm(config))
})

watch(guardedPage, (page) => {
  if (page === 'login') {
    restartLoginIntro()
  } else {
    clearLoginIntroTimer()
  }
}, { immediate: true })

function enforceRouteGuard() {
  if (route.page === 'console' && !isAuthenticated.value) {
    postLoginPath.value = `#/console/${route.tab}`
    navigate('#/login')
    return
  }
  if (route.page === 'console' && route.tab === 'users' && !canManageUsers.value) {
    notice.value = '用户管理仅管理员可访问。'
    navigate('#/console/devices')
    return
  }
  if (route.page === 'console' && isAuthenticated.value && runMode.value === 'connecting') void syncProject()
}

watch([() => route.page, () => route.tab, isAuthenticated, runMode], enforceRouteGuard)

onMounted(() => {
  window.addEventListener('hashchange', handleRouteChange)
  enforceRouteGuard()
})

onBeforeUnmount(() => {
  window.removeEventListener('hashchange', handleRouteChange)
  window.cancelAnimationFrame(consolePointerFrame)
  clearLoginIntroTimer()
  clearConsoleRevealTimer()
})

const OpsVerticalNav = defineComponent({
  props: {
    items: { type: Array, required: true },
    activePath: { type: String, required: true },
    className: { type: String, default: '' },
  },
  emits: ['navigate'],
  setup(props, { emit }) {
    return () => h('nav', { class: ['ops-vertical-nav', props.className] }, props.items.map((item) =>
      h('button', {
        key: item.path,
        class: ['ops-vertical-nav__item', props.activePath === item.path ? 'ops-vertical-nav__item--active' : ''],
        onClick: () => emit('navigate', item.path),
      }, [
        h(item.icon, { size: 18, strokeWidth: 1.9, class: 'shrink-0' }),
        h('span', { class: 'truncate' }, item.label),
      ]),
    ))
  },
})

const Metric = defineComponent({
  props: { label: String, value: String, detail: String },
  setup(props) {
    return () => h('div', { class: 'ops-metric-card' }, [
      h('p', { class: 'text-xs text-white/45' }, props.label),
      h('div', { class: 'mt-2 flex items-end justify-between gap-3' }, [
        h('strong', { class: 'text-2xl font-medium' }, props.value),
        h('span', { class: 'text-xs text-white/40' }, props.detail),
      ]),
    ])
  },
})

const FooterBlock = defineComponent({
  setup() {
    const subscribed = ref(false)
    const feedback = ref('')
    const feedbackSent = ref(false)
    const quickLinks = [
      ['首页入口', '#/'],
      ['设备监控', '#/console/devices'],
      ['告警中心', '#/console/alerts'],
      ['知识库 RAG', '#/console/rag'],
      ['AI 助手', '#/console/agent'],
    ]
    const socialLinks = [
      ['产品动态', Sparkles],
      ['运维社区', Users],
      ['接口变更', Network],
      ['反馈通道', MessageSquare],
    ]

    function handleSubscribe(event) {
      event.preventDefault()
      subscribed.value = true
    }

    function handleFeedbackSubmit(event) {
      event.preventDefault()
      if (!feedback.value.trim()) return
      feedbackSent.value = true
      feedback.value = ''
    }

    return () => h('footer', { class: 'ops-footer relative overflow-hidden border-t border-white/10 bg-[#04070d] text-white' }, [
      h('div', { class: 'absolute inset-x-0 top-0 h-px bg-gradient-to-r from-transparent via-cyan-200/45 to-transparent' }),
      h('div', { class: 'absolute left-0 top-0 h-72 w-72 rounded-full bg-cyan-300/10 blur-3xl' }),
      h('div', { class: 'absolute right-[-8rem] bottom-[-8rem] h-80 w-80 rounded-full bg-orange-300/10 blur-3xl' }),
      h('div', { class: 'relative mx-auto max-w-7xl px-6 py-12 sm:px-8 lg:px-10' }, [
        h('div', { class: 'grid gap-10 md:grid-cols-2 lg:grid-cols-4' }, [
          h('div', { class: 'relative' }, [
            h('p', { class: 'text-xs font-semibold uppercase tracking-[0.28em] text-white/42' }, 'OpsPilot AI'),
            h('h2', { class: 'mt-3 text-3xl font-semibold tracking-tight text-white' }, '保持连接'),
            h('p', { class: 'mt-4 text-sm leading-7 text-white/56' }, '订阅产品更新、接口变更和 Agent 能力迭代，把重点功能第一时间同步给运维团队。'),
            h('form', { class: 'relative mt-6', onSubmit: handleSubscribe }, [
              h('input', { type: 'email', placeholder: '输入你的邮箱', class: 'h-12 w-full rounded-full border border-white/10 bg-white/[0.045] px-4 pr-12 text-sm text-white outline-none transition-colors placeholder:text-white/34 focus:border-cyan-200/45 focus:bg-white/[0.07]' }),
              h('button', { type: 'submit', class: 'absolute right-1.5 top-1.5 inline-flex h-9 w-9 items-center justify-center rounded-full bg-white text-black transition-transform hover:scale-105', 'aria-label': '订阅' }, [iconNode(SendHorizonal, 16)]),
            ]),
            subscribed.value ? h('p', { class: 'mt-3 text-xs text-cyan-100/80' }, '已登记订阅，后续可以继续扩展成真实接口。') : null,
          ]),

          h('div', [
            h('h3', { class: 'mb-4 text-lg font-semibold text-white' }, '快速入口'),
            h('nav', { class: 'grid gap-2 text-sm text-white/58' }, quickLinks.map(([label, href]) =>
              h('a', { key: href, href, class: 'transition-colors hover:text-white' }, label),
            )),
          ]),

          h('div', [
            h('h3', { class: 'mb-4 text-lg font-semibold text-white' }, '项目联系'),
            h('address', { class: 'grid gap-2 text-sm not-italic leading-6 text-white/56' }, [
              h('p', 'OpsPilot AI 设备运维 Agent'),
              h('p', 'Vue 3 + Spring Boot + RAG + Tool Trace'),
              h('p', '能力：设备、告警、知识库、联网兜底、Agent 闭环'),
              h('p', '定位：让工程师从一句问题进入完整排障流程'),
              h('a', { href: 'mailto:support@opspilot.ai', class: 'inline-flex items-center gap-2 text-cyan-100/86 transition-colors hover:text-white' }, [iconNode(Mail, 14), '投诉/反馈邮箱：support@opspilot.ai']),
            ]),
          ]),

          h('div', { class: 'relative' }, [
            h('h3', { class: 'mb-4 text-lg font-semibold text-white' }, '关注与反馈'),
            h('div', { class: 'mb-5 flex flex-wrap gap-3' }, socialLinks.map(([label, icon]) =>
              h('button', { key: label, type: 'button', class: 'inline-flex h-10 w-10 items-center justify-center rounded-full border border-white/10 bg-white/[0.035] text-white/68 transition-colors hover:bg-white/[0.08] hover:text-white', title: label, 'aria-label': label }, [iconNode(icon, 16)]),
            )),
            h('form', { class: 'grid gap-3', onSubmit: handleFeedbackSubmit }, [
              h('label', { for: 'footer-feedback', class: 'text-xs text-white/46' }, '给 OpsPilot AI 留一句反馈'),
              h('textarea', {
                id: 'footer-feedback',
                value: feedback.value,
                onInput: (event) => {
                  feedback.value = event.target.value
                  feedbackSent.value = false
                },
                placeholder: '比如：我希望 Agent 在温度异常时先确认设备，再给出现场处理步骤。',
                class: 'min-h-[96px] resize-none rounded-2xl border border-white/10 bg-white/[0.035] px-4 py-3 text-sm leading-6 text-white outline-none transition-colors placeholder:text-white/32 focus:border-cyan-200/45 focus:bg-white/[0.06]',
              }),
              h('button', { type: 'submit', class: 'inline-flex h-11 items-center justify-center rounded-full bg-white px-4 text-sm font-semibold text-black transition-colors hover:bg-white/90' }, '提交页脚反馈'),
              feedbackSent.value ? h('p', { class: 'text-xs text-cyan-100/78' }, '已记录这条前端反馈，用来支撑下一轮迭代。') : null,
            ]),
          ]),
        ]),
        h('div', { class: 'mt-12 flex flex-col items-center justify-between gap-4 border-t border-white/10 pt-8 text-center md:flex-row' }, [
          h('p', { class: 'text-sm text-white/42' }, '© 2026 OpsPilot AI. 面向设备运维现场的 AI Agent 工作台。'),
          h('nav', { class: 'flex flex-wrap justify-center gap-4 text-sm text-white/52' }, [
            h('a', { href: '#/console/api', class: 'transition-colors hover:text-white' }, '接口说明'),
            h('a', { href: '#/console/rag', class: 'transition-colors hover:text-white' }, '知识库来源'),
            h('a', { href: '#/console/agent', class: 'transition-colors hover:text-white' }, 'Agent Trace'),
          ]),
        ]),
      ]),
    ])
  },
})

const ConsoleTopbar = defineComponent({
  props: { runModeLabel: String, syncing: Boolean, agent: Boolean },
  emits: ['navigate', 'sync'],
  setup(props, { emit }) {
    return () => h('header', { class: 'ops-topbar sticky top-0 z-30 border-b border-white/10 px-5 py-3' }, [
      h('div', { class: 'mx-auto flex max-w-7xl items-center justify-between gap-4' }, [
        h('button', { class: 'flex items-center gap-2 text-sm font-semibold', onClick: () => emit('navigate', '#/') }, [h(Home, { size: 17 }), 'OpsPilot AI']),
        h('div', { class: 'flex items-center gap-2' }, [
          props.agent
            ? h('button', { class: 'ops-ghost-button px-3 py-2 text-xs', onClick: () => emit('navigate', '#/console/devices') }, [h(Cpu, { size: 14 }), '控制台'])
            : h('button', { class: 'inline-flex h-11 items-center gap-2.5 rounded-full border border-cyan-200/15 bg-cyan-200/[0.06] px-4 text-sm font-semibold text-white shadow-[0_18px_46px_rgba(34,211,238,0.1)] transition-colors hover:bg-cyan-200/[0.1]', onClick: () => emit('navigate', '#/console/agent') }, [h(Bot, { size: 17, strokeWidth: 1.9 }), h('span', { class: 'hidden sm:inline' }, '进入 AI 助手'), h(ArrowRight, { size: 15 })]),
          h('span', { class: 'hidden text-xs text-white/48 sm:inline' }, props.runModeLabel),
          h('button', { class: 'ops-ghost-button px-3 py-2 text-xs', onClick: () => emit('sync') }, [h(RefreshCw, { size: 14, class: props.syncing ? 'animate-spin' : '' }), '重新同步']),
        ]),
      ]),
    ])
  },
})

function iconNode(icon, size = 16, extraClass = '') {
  return h(icon, { size, class: extraClass })
}

function buttonNode(label, className, onClick, icon, disabled = false) {
  return h('button', { class: className, onClick, disabled }, icon ? [iconNode(icon, 14), label] : label)
}

function traceNodes(trace = []) {
  if (!trace.length) return []
  return [
    h('div', { class: 'mt-3 grid gap-2' }, trace.map((step, index) =>
      h('div', { key: `${step.toolName}-${index}`, class: 'rounded-2xl border border-white/10 bg-white/[0.035] p-3' }, [
        h('div', { class: 'flex items-center justify-between gap-2' }, [
          h('p', { class: 'text-sm font-medium' }, `${index + 1}. ${step.toolName}`),
          h('span', { class: 'text-xs text-white/38' }, step.durationMs ? `${step.durationMs}ms` : step.status ?? 'done'),
        ]),
        h('p', { class: 'mt-1 text-xs leading-relaxed text-white/48' }, step.result || '已完成调用。'),
      ]),
    )),
  ]
}

function sourceNodes(sources = [], compact = false) {
  if (!sources.length) return []
  return [
    h('div', { class: compact ? 'mt-3 grid gap-2' : 'grid gap-2' }, sources.map((source, index) =>
      h('div', { key: `${source.title}-${index}`, class: 'rounded-2xl border border-white/10 bg-black/20 p-3' }, [
        h('div', { class: 'flex items-center justify-between gap-3' }, [
          h('p', { class: 'text-sm font-medium text-white/76' }, source.title || `来源 ${index + 1}`),
          source.score ? h('span', { class: 'text-xs text-white/38' }, `${Math.round(Number(source.score) * 100)}%`) : null,
        ]),
        h('p', { class: 'mt-1 text-xs leading-relaxed text-white/48' }, source.snippet || '暂无片段。'),
        source.url ? h('a', { href: source.url, target: '_blank', rel: 'noreferrer', class: 'mt-2 inline-flex text-xs text-cyan-200 hover:text-cyan-100' }, '打开来源') : null,
      ]),
    )),
  ]
}

const OpsCommandDeck = defineComponent({
  setup() {
    const deckRef = ref(null)
    let pointerFrame = 0
    const signals = [
      { label: '设备遥测', icon: Radar },
      { label: '告警闭环', icon: BellRing },
      { label: 'RAG 溯源', icon: FileText },
      { label: 'Agent Trace', icon: Workflow },
    ]
    const pipeline = [
      { step: '01', label: '遥测异常', detail: '温湿度与在线状态进入判断', icon: Radar },
      { step: '02', label: '告警归因', detail: '高温/离线事件自动聚合', icon: BellRing },
      { step: '03', label: 'RAG 检索', detail: '拉取说明书与处理经验', icon: FileText },
      { step: '04', label: 'Agent 建议', detail: '生成可执行排障步骤', icon: Bot },
    ]

    function updatePointer(event) {
      const node = deckRef.value
      if (!node) return
      if (pointerFrame) window.cancelAnimationFrame(pointerFrame)
      pointerFrame = window.requestAnimationFrame(() => {
        const rect = node.getBoundingClientRect()
        const x = ((event.clientX - rect.left) / rect.width) * 100
        const y = ((event.clientY - rect.top) / rect.height) * 100
        node.style.setProperty('--ops-pointer-x', `${Math.max(0, Math.min(100, x))}%`)
        node.style.setProperty('--ops-pointer-y', `${Math.max(0, Math.min(100, y))}%`)
      })
    }

    function resetPointer() {
      const node = deckRef.value
      if (!node) return
      node.style.setProperty('--ops-pointer-x', '50%')
      node.style.setProperty('--ops-pointer-y', '50%')
    }

    onMounted(() => {
      const node = deckRef.value
      if (!node) return
      node.addEventListener('pointermove', updatePointer)
      node.addEventListener('pointerleave', resetPointer)
    })

    onBeforeUnmount(() => {
      const node = deckRef.value
      if (node) {
        node.removeEventListener('pointermove', updatePointer)
        node.removeEventListener('pointerleave', resetPointer)
      }
      if (pointerFrame) window.cancelAnimationFrame(pointerFrame)
    })

    return () => {
      const focusDevice = selectedDeviceCode.value || 'ESP32-001'
      const focusName = selectedDevice.value?.name ?? '温湿度传感器'
      const temperatureLabel = typeof latestTelemetry.value?.temperature === 'number' ? `${Number(latestTelemetry.value.temperature).toFixed(1)}°C` : '--'
      const hasOpenIncident = totalOpenAlerts.value > 0
      const incidentTitle = hasOpenIncident ? `${focusDevice} 高温风险待闭环` : `${focusDevice} 当前运行稳定`
      return h('section', { ref: deckRef, class: 'ops-command-deck' }, [
        h('div', { class: 'ops-command-grid', 'aria-hidden': 'true' }),
        h('div', { class: 'ops-command-beam', 'aria-hidden': 'true' }),
        h('div', { class: 'ops-command-glow', 'aria-hidden': 'true' }),
        h('div', { class: 'relative z-10 grid gap-8 xl:grid-cols-[1.08fr_0.92fr]' }, [
          h('div', { class: 'flex min-w-0 flex-col justify-between gap-7' }, [
            h('div', { class: 'min-w-0' }, [
              h('div', { class: 'flex flex-wrap items-center gap-3' }, [
                h('span', { class: 'ops-command-kicker' }, [iconNode(Sparkles, 14), 'AI 运维闭环']),
                h('span', { class: 'ops-command-subtle' }, `当前视角 · ${activeTabLabel.value}`),
                h('span', { class: 'ops-command-subtle' }, runModeLabel.value),
              ]),
              h('h1', { class: 'mt-6 max-w-3xl text-4xl font-semibold tracking-tight text-white sm:text-5xl xl:text-[3.7rem] xl:leading-[1.02]' }, '先盯住一个事件，再让 Agent 把排障链跑完。'),
              h('p', { class: 'mt-4 max-w-2xl text-sm leading-7 text-white/62 sm:text-base' }, '控制台的主角不是表格，而是一次正在处理的运维事件。设备遥测、告警历史、RAG 知识来源和 Agent trace 会被压成一条可复盘、可确认、可执行的闭环。'),
              h('div', { class: 'ops-incident-strip', 'aria-label': '当前 AI 运维事件闭环' }, [
                h('div', { class: 'ops-incident-strip__head' }, [
                  h('div', [h('span', { class: 'ops-incident-strip__eyebrow' }, 'Current Incident'), h('strong', incidentTitle)]),
                  h('span', { class: ['ops-severity-badge', hasOpenIncident ? 'ops-severity-badge--hot' : 'ops-severity-badge--ok'] }, hasOpenIncident ? 'P1 待处理' : '稳定巡检'),
                ]),
                h('div', { class: 'ops-incident-strip__meta' }, [h('span', focusName), h('span', temperatureLabel), h('span', runModeLabel.value)]),
                h('div', { class: 'ops-incident-pipeline' }, pipeline.map((item, index) =>
                  h('div', { key: item.step, class: ['ops-incident-step', index <= (hasOpenIncident ? 2 : 0) ? 'ops-incident-step--active' : ''] }, [
                    h('span', { class: 'ops-incident-step__index' }, item.step),
                    iconNode(item.icon, 15),
                    h('span', { class: 'ops-incident-step__label' }, item.label),
                    h('small', item.detail),
                  ]),
                )),
              ]),
              h('div', { class: 'mt-6 flex flex-wrap gap-2' }, signals.map((item) => h('span', { key: item.label, class: 'ops-command-chip' }, [iconNode(item.icon, 14), item.label]))),
              h('div', { class: 'mt-7 flex flex-wrap gap-3' }, [
                buttonNode('查看 Agent 排障', 'ops-primary-button', () => navigate('#/console/agent')),
                buttonNode('查看告警闭环', 'ops-secondary-button', () => navigate('#/console/alerts')),
                buttonNode('打开设备状态', 'ops-secondary-button', () => navigate('#/console/devices')),
                buttonNode(syncing.value ? '同步中' : '重新同步', 'ops-ghost-button', () => void syncProject(), RefreshCw),
              ]),
            ]),
            h('div', { class: 'grid gap-3 sm:grid-cols-3' }, [
              h('div', { class: 'ops-hero-stat' }, [h('span', { class: 'ops-hero-stat__label' }, '当前事件'), h('strong', { class: 'ops-hero-stat__value' }, hasOpenIncident ? totalOpenAlerts.value : 0), h('span', { class: 'ops-hero-stat__detail' }, hasOpenIncident ? '待处理告警进入排障链' : '暂无待处理事件')]),
              h('div', { class: 'ops-hero-stat' }, [h('span', { class: 'ops-hero-stat__label' }, '在线设备'), h('strong', { class: 'ops-hero-stat__value' }, [onlineDeviceCount.value, h('span', { class: 'text-lg text-white/42' }, ` / ${devices.value.length}`)]), h('span', { class: 'ops-hero-stat__detail' }, '支撑本次遥测判断')]),
              h('div', { class: 'ops-hero-stat' }, [h('span', { class: 'ops-hero-stat__label' }, '协作记录'), h('strong', { class: 'ops-hero-stat__value' }, employees.value.length), h('span', { class: 'ops-hero-stat__detail' }, '人员、值守与操作归属')]),
            ]),
          ]),
          h('div', { class: 'ops-command-canvas' }, [
            h('div', { class: 'flex flex-wrap items-center justify-between gap-3' }, [
              h('div', { class: 'flex items-center gap-2 text-xs uppercase tracking-[0.24em] text-white/34' }, [iconNode(Activity, 13), 'Incident Command Flow']),
              h('div', { class: 'ops-command-subtle' }, [iconNode(Cpu, 13), focusDevice]),
            ]),
            h('div', { class: 'mt-6 grid gap-5 lg:grid-cols-[minmax(0,1fr)_220px]' }, [
              h('div', { class: 'ops-core-stage' }, [
                h('div', { class: 'ops-core-ring ops-core-ring--outer', 'aria-hidden': 'true' }),
                h('div', { class: 'ops-core-ring ops-core-ring--mid', 'aria-hidden': 'true' }),
                h('div', { class: 'ops-core-ring ops-core-ring--inner', 'aria-hidden': 'true' }),
                h('div', { class: 'ops-core-orb' }),
                h('div', { class: 'ops-core-readout' }, [
                  h('span', { class: 'ops-core-readout__eyebrow' }, 'Current Incident'),
                  h('strong', { class: 'ops-core-readout__title' }, incidentTitle),
                  h('div', { class: 'mt-3 flex items-center justify-center gap-2 text-xs text-white/48' }, [iconNode(AlertTriangle, 13), `最新温度 ${temperatureLabel}`]),
                ]),
                h('div', { class: 'ops-floating-node ops-floating-node--top' }, [iconNode(Radar, 15), '遥测流入']),
                h('div', { class: 'ops-floating-node ops-floating-node--right' }, [iconNode(BellRing, 15), '告警判别']),
                h('div', { class: 'ops-floating-node ops-floating-node--bottom' }, [iconNode(Bot, 15), 'Agent 排障']),
              ]),
              h('div', { class: 'grid gap-3' }, [
                h('div', { class: 'ops-stack-card' }, [h('span', { class: 'ops-stack-card__label' }, '本次目标'), h('p', { class: 'ops-stack-card__title' }, hasOpenIncident ? `当前还有 ${totalOpenAlerts.value} 条告警需要处理，建议串联设备状态、历史告警、知识库来源和 Agent 建议。` : '当前没有待处理告警，可以继续保持遥测观察并记录本次巡检状态。')]),
                h('div', { class: 'ops-stack-card' }, [h('span', { class: 'ops-stack-card__label' }, '当前模式'), h('p', { class: 'ops-stack-card__title' }, runModeLabel.value)]),
                h('div', { class: 'ops-stack-card' }, [h('span', { class: 'ops-stack-card__label' }, '下一步操作'), h('div', { class: 'mt-2 flex flex-wrap gap-2' }, ['查状态', '看告警', '问 Agent'].map((text) => h('span', { class: 'ops-mini-chip' }, text)))]),
              ]),
            ]),
              h('div', { class: 'mt-5 grid gap-3 sm:grid-cols-3' }, [
                h('div', { class: 'ops-trace-card' }, [
                  h('span', { class: 'ops-trace-card__index' }, '01'),
                  h('p', { class: 'ops-trace-card__title' }, '发现异常'),
                  h('p', { class: 'ops-trace-card__detail' }, '聚合设备在线状态、温湿度和最新遥测。'),
                ]),
                h('div', { class: 'ops-trace-card' }, [
                  h('span', { class: 'ops-trace-card__index' }, '02'),
                  h('p', { class: 'ops-trace-card__title' }, '调用工具'),
                  h('p', { class: 'ops-trace-card__detail' }, '查询告警历史、知识库来源和设备上下文。'),
                ]),
                h('div', { class: 'ops-trace-card' }, [
                  h('span', { class: 'ops-trace-card__index' }, '03'),
                  h('p', { class: 'ops-trace-card__title' }, '输出建议'),
                  h('p', { class: 'ops-trace-card__detail' }, '生成可执行排障步骤，并进入处理闭环。'),
                ]),
              ]),
          ]),
        ]),
      ])
    }
  },
})

const DevicePanel = defineComponent({
  setup() {
    const workspaceTabs = [
      { key: 'overview', label: '运行概览' },
      { key: 'report', label: '设备上报' },
      { key: 'settings', label: '设备配置' },
      { key: 'commands', label: '命令队列' },
      { key: 'history', label: '遥测历史' },
    ]

    function workspaceTabButton(tab) {
      return h('button', {
        key: tab.key,
        type: 'button',
        class: ['ops-rounded-tab', deviceWorkspaceTab.value === tab.key ? 'ops-rounded-tab--active' : ''],
        'aria-selected': deviceWorkspaceTab.value === tab.key,
        onClick: () => { deviceWorkspaceTab.value = tab.key },
      }, tab.label)
    }

    function renderOverview() {
      return h('div', { class: 'ops-device-overview-grid' }, [
        h('section', { class: 'ops-dashboard-block ops-device-chart-block' }, [
          h('div', { class: 'ops-section-heading' }, [
            h('div', [h('h3', '温湿度趋势'), h('p', '最近 7 次遥测上报')]),
            h('span', { class: consoleCodeChipClass }, selectedConfig.value?.displayMode ?? 'NORMAL'),
          ]),
          h(TelemetryBarChart, { items: selectedHistory.value }),
        ]),
        h('aside', { class: 'ops-dashboard-block ops-device-live-panel' }, [
          h('div', { class: 'ops-section-heading' }, [h('div', [h('h3', '当前读数'), h('p', formatDateTime(latestTelemetry.value?.reportTime))])]),
          h('div', { class: 'ops-live-reading' }, [h('span', '温度'), h('strong', latestTelemetry.value?.temperature ?? '--'), h('small', '°C')]),
          h('div', { class: 'ops-live-reading' }, [h('span', '湿度'), h('strong', latestTelemetry.value?.humidity ?? '--'), h('small', '%')]),
          h('dl', { class: 'ops-device-meta-list' }, [
            ['设备类型', selectedDevice.value?.type ?? '--'],
            ['安装位置', selectedDevice.value?.location ?? '--'],
            ['高温阈值', `${selectedConfig.value?.temperatureThreshold ?? '--'}°C`],
            ['采样间隔', `${selectedConfig.value?.sampleIntervalSeconds ?? '--'} 秒`],
          ].map(([label, value]) => h('div', [h('dt', label), h('dd', value)]))),
        ]),
      ])
    }

    function renderReport() {
      return h('section', { class: 'ops-dashboard-block ops-focused-form' }, [
        h('div', { class: 'ops-section-heading' }, [h('div', [h('h3', '模拟设备上报'), h('p', '向当前设备写入一条温湿度遥测数据。')]), iconNode(Cable, 18)]),
        h('div', { class: 'ops-form-grid ops-form-grid--compact' }, [
          h('label', ['设备编号', h('input', { value: selectedDevice.value?.deviceCode ?? '', readOnly: true, class: consoleInputClass })]),
          h('label', ['温度 °C', h('input', { value: reportForm.temperature, onInput: (event) => { reportForm.temperature = event.target.value }, class: consoleInputClass, placeholder: '36.8' })]),
          h('label', ['湿度 %', h('input', { value: reportForm.humidity, onInput: (event) => { reportForm.humidity = event.target.value }, class: consoleInputClass, placeholder: '61.2' })]),
        ]),
        buttonNode(reporting.value ? '正在上报' : '提交上报', `${consolePrimaryButtonClass} mt-5`, () => void handleReportSubmit(), HardDriveUpload, !selectedDevice.value || reporting.value),
      ])
    }

    function renderSettings() {
      return h('div', { class: 'grid gap-px bg-white/[0.08] xl:grid-cols-2' }, [
        h('section', { class: 'ops-dashboard-block' }, [
          h('div', { class: 'ops-section-heading' }, [
            h('div', [h('h3', deviceFormMode.value === 'create' ? '注册设备' : '设备资料'), h('p', deviceFormMode.value === 'create' ? '将新设备纳入控制台。' : '维护当前设备的基础信息。')]),
            h('span', { class: consoleCodeChipClass }, deviceFormMode.value === 'create' ? 'CREATE' : 'UPDATE'),
          ]),
          h('div', { class: 'ops-form-grid' }, [
            ['设备编号', 'deviceCode', 'ESP32-004'],
            ['设备名称', 'name', '温湿度传感器'],
            ['设备类型', 'type', '传感器'],
            ['安装位置', 'location', '3号机房'],
          ].map(([label, key, placeholder]) => h('label', [label, h('input', { value: deviceForm[key], onInput: (event) => { deviceForm[key] = event.target.value }, class: consoleInputClass, placeholder })]))),
          h('div', { class: 'mt-5 flex flex-wrap gap-2' }, [
            buttonNode(savingDevice.value ? '保存中' : deviceFormMode.value === 'create' ? '新增设备' : '保存修改', consolePrimaryButtonClass, () => void handleDeviceSave(), Save, savingDevice.value),
            deviceFormMode.value === 'edit' && selectedDevice.value ? buttonNode(removingDeviceId.value === selectedDevice.value.id ? '删除中' : '删除设备', 'ops-danger-button', () => void handleDeviceRemove(), Trash2, removingDeviceId.value === selectedDevice.value.id) : null,
            deviceFormMode.value === 'create' && selectedDevice.value ? buttonNode('取消新增', consoleGhostButtonClass, () => handleSelectDevice(selectedDevice.value.deviceCode)) : null,
          ]),
        ]),
        h('section', { class: 'ops-dashboard-block' }, [
          h('div', { class: 'ops-section-heading' }, [h('div', [h('h3', '阈值与硬件策略'), h('p', '控制采样频率、告警阈值和屏幕模式。')]), iconNode(ShieldCheck, 18)]),
          h('div', { class: 'ops-form-grid ops-form-grid--two' }, [
            ['高温阈值 °C', 'temperatureThreshold'],
            ['采样间隔 秒', 'sampleIntervalSeconds'],
            ['湿度下限 %', 'humidityMinThreshold'],
            ['湿度上限 %', 'humidityMaxThreshold'],
            ['巡检间隔 分钟', 'inspectionIntervalMinutes'],
          ].map(([label, key]) => h('label', [label, h('input', { value: deviceConfigForm[key], onInput: (event) => { deviceConfigForm[key] = event.target.value }, class: consoleInputClass })])).concat([
            h('label', ['屏幕模式', h('select', { value: deviceConfigForm.displayMode, onChange: (event) => { deviceConfigForm.displayMode = event.target.value }, class: consoleInputClass }, ['NORMAL', 'ALERT', 'MAINTENANCE'].map((mode) => h('option', { value: mode }, mode)))]),
          ])),
          buttonNode(savingConfig.value ? '同步中' : '保存策略', `${consolePrimaryButtonClass} mt-5`, () => void handleConfigSave(), Save, !selectedDevice.value || savingConfig.value),
        ]),
      ])
    }

    function renderCommands() {
      return h('section', { class: 'ops-dashboard-block' }, [
        h('div', { class: 'ops-section-heading' }, [h('div', [h('h3', 'ESP32 命令队列'), h('p', '下发命令并追踪最近执行状态。')]), h('span', { class: consoleCodeChipClass }, `${selectedCommands.value.length} 条`)]),
        h('div', { class: 'ops-command-form' }, [
          h('select', { value: deviceCommandForm.commandType, onChange: (event) => changeCommandType(event.target.value), class: consoleInputClass }, deviceCommandTypes.map((type) => h('option', { value: type }, type))),
          h('input', { value: deviceCommandForm.payload, onInput: (event) => { deviceCommandForm.payload = event.target.value }, class: consoleInputClass, placeholder: commandPayloadPlaceholders[deviceCommandForm.commandType] ?? '命令参数' }),
          buttonNode(sendingCommand.value ? '下发中' : '下发命令', consolePrimaryButtonClass, () => void handleCommandSend(), SendHorizonal, !selectedDevice.value || sendingCommand.value),
        ]),
        h('div', { class: 'ops-activity-table mt-5' }, selectedCommands.value.length ? selectedCommands.value.slice(0, 8).map((command) =>
          h('div', { key: command.id, class: 'ops-activity-row' }, [
            h('span', { class: 'ops-activity-row__id' }, `#${command.id}`),
            h('strong', command.commandType),
            h('span', command.payload || '无参数'),
            h('span', command.issuedBy || 'system'),
            h('span', formatDateTime(command.createTime)),
            h('span', { class: ['ops-status-pill', commandTone(command.status)] }, command.status),
          ]),
        ) : h('p', { class: 'ops-empty-state' }, '暂无命令，Agent 或运维人员下发后会进入队列。')),
      ])
    }

    function renderHistory() {
      return h('section', { class: 'ops-dashboard-block' }, [
        h('div', { class: 'ops-section-heading' }, [h('div', [h('h3', '遥测历史'), h('p', '拖动表头分隔线可以调整列宽。')]), h('span', { class: consoleCodeChipClass }, `最新 ${latestTelemetry.value ? `${latestTelemetry.value.temperature}°C` : '暂无'}`)]),
        h(ResizableTelemetryTable, { items: selectedHistory.value }),
      ])
    }

    return () => h('div', { class: 'ops-device-page' }, [
      h('section', { class: ['ops-action-alert', totalOpenAlerts.value ? 'ops-action-alert--warning' : 'ops-action-alert--ok'] }, [
        h('div', { class: 'ops-action-alert__icon' }, [iconNode(totalOpenAlerts.value ? AlertTriangle : CircleCheck, 18)]),
        h('div', { class: 'min-w-0 flex-1' }, [
          h('h2', totalOpenAlerts.value ? `${totalOpenAlerts.value} 条告警需要处理` : '设备运行状态稳定'),
          h('p', totalOpenAlerts.value ? `${selectedDeviceCode.value} 当前存在高温或离线事件，建议先进入告警闭环。` : '当前没有未处理事件，可以继续观察遥测趋势。'),
        ]),
        totalOpenAlerts.value ? buttonNode('查看告警', consoleSecondaryButtonClass, () => navigate('#/console/alerts'), ArrowRight) : null,
      ]),
      h('section', { class: 'ops-device-workspace' }, [
        h('aside', { class: 'ops-device-master' }, [
          h('div', { class: 'ops-device-master__head' }, [h('div', [h('h2', '设备列表'), h('p', `${devices.value.length} 台设备`)]), buttonNode('新建', 'ops-ghost-button px-3 py-2 text-xs', handleCreateDeviceMode, Plus)]),
          h('div', { class: 'ops-device-master__list' }, devices.value.map((device) => {
            const openAlerts = (alarmMap.value[device.deviceCode] ?? []).filter((alarm) => isUnresolved(alarm.status)).length
            const latest = (deviceHistoryMap.value[device.deviceCode] ?? [])[0]
            return h('button', { key: device.deviceCode, type: 'button', class: ['ops-device-row', selectedDeviceCode.value === device.deviceCode ? 'ops-device-row--active' : ''], onClick: () => handleSelectDevice(device.deviceCode) }, [
              h('span', { class: ['ops-status-dot', device.online ? 'ops-status-dot--ok' : 'ops-status-dot--offline'] }),
              h('span', { class: 'min-w-0 flex-1' }, [h('strong', device.deviceCode), h('small', `${device.name} · ${device.location}`)]),
              h('span', { class: 'ops-device-row__telemetry' }, [h('strong', latest?.temperature != null ? `${latest.temperature}°` : '--'), openAlerts ? h('small', `${openAlerts} 告警`) : h('small', device.online ? '在线' : '离线')]),
            ])
          })),
        ]),
        h('div', { class: 'ops-device-detail' }, [
          h('header', { class: 'ops-device-detail__head' }, [
            h('div', { class: 'min-w-0' }, [h('div', { class: 'flex flex-wrap items-center gap-3' }, [h('h2', selectedDevice.value?.deviceCode ?? '未选择设备'), h('span', { class: ['ops-inline-status', selectedDevice.value?.online ? 'ops-inline-status--ok' : 'ops-inline-status--offline'] }, selectedDevice.value?.online ? '在线' : '离线')]), h('p', `${selectedDevice.value?.name ?? '暂无设备'} · ${selectedDevice.value?.location ?? '--'} · 最近在线 ${formatDateTime(selectedDevice.value?.lastOnlineTime)}`)]),
            h('div', { class: 'flex gap-2' }, [buttonNode('问 Agent', consoleGhostButtonClass, () => navigate('#/console/agent'), Bot), buttonNode('设备配置', consoleSecondaryButtonClass, () => { deviceWorkspaceTab.value = 'settings' }, ShieldCheck)]),
          ]),
          h('div', { class: 'ops-rounded-tabs', role: 'tablist' }, workspaceTabs.map(workspaceTabButton)),
          h('div', { class: 'ops-device-detail__body' }, [
            deviceWorkspaceTab.value === 'overview' ? renderOverview() : null,
            deviceWorkspaceTab.value === 'report' ? renderReport() : null,
            deviceWorkspaceTab.value === 'settings' ? renderSettings() : null,
            deviceWorkspaceTab.value === 'commands' ? renderCommands() : null,
            deviceWorkspaceTab.value === 'history' ? renderHistory() : null,
          ]),
        ]),
      ]),
    ])
  },
})

const AlertPanel = defineComponent({
  setup() {
    return () => h('div', { class: 'grid gap-5 xl:grid-cols-[320px_minmax(0,1fr)]' }, [
      h('section', { class: consolePanelClass }, [
        h('h2', { class: 'text-lg font-medium' }, '设备告警视图'),
        h('div', { class: 'mt-4 grid gap-2' }, devices.value.map((device) => h('button', { key: device.deviceCode, class: ['rounded-md border px-3 py-3 text-left transition-colors', selectedDeviceCode.value === device.deviceCode ? 'border-white/25 bg-white/[0.09]' : 'border-white/10 bg-transparent hover:bg-white/[0.05]'], onClick: () => handleSelectDevice(device.deviceCode) }, [
          h('div', { class: 'flex items-center justify-between gap-2' }, [h('span', { class: 'text-sm font-medium' }, device.deviceCode), h('span', { class: 'text-xs text-white/45' }, `${(alarmMap.value[device.deviceCode] ?? []).filter((alarm) => isUnresolved(alarm.status)).length} 条`)]),
          h('p', { class: 'mt-1 text-xs text-white/45' }, device.name),
        ]))),
      ]),
      h('section', { class: consolePanelClass }, [
        h('div', { class: 'flex items-center justify-between gap-3' }, [h('h2', { class: 'text-lg font-medium' }, `${selectedDeviceCode.value} 告警闭环`), h('span', { class: consoleCodeChipClass }, `${selectedAlarms.value.length} 条`)]),
        h('div', { class: 'mt-4 grid gap-2' }, selectedAlarms.value.length ? selectedAlarms.value.map((alarm) =>
          h('div', { key: alarm.id, class: consoleRowCardClass }, [
            h('div', { class: 'flex flex-wrap items-center justify-between gap-2' }, [
              h('div', [h('p', { class: 'text-sm font-medium' }, alarm.alarmType), h('p', { class: 'mt-1 text-xs text-white/45' }, `${alarm.alarmValue} · ${formatDateTime(alarm.createTime)}`)]),
              h('span', { class: ['rounded-full border px-2 py-0.5 text-[11px]', alarmTone(alarm.status)] }, alarm.status),
            ]),
            isUnresolved(alarm.status) ? h('button', { class: `${consoleSecondaryButtonClass} mt-3`, disabled: resolvingAlarmId.value === alarm.id, onClick: () => void handleResolveAlarm(selectedDeviceCode.value, alarm.id) }, resolvingAlarmId.value === alarm.id ? '处理中' : '标记处理') : null,
          ]),
        ) : h('p', { class: 'ops-panel-subtle rounded-2xl border border-dashed border-white/12 p-4 text-sm text-white/45' }, '当前设备暂无告警。')),
      ]),
    ])
  },
})

const RagPanel = defineComponent({
  setup() {
    return () => h('div', { class: 'grid gap-5 xl:grid-cols-[minmax(0,1fr)_360px]' }, [
      h('section', { class: consolePanelClass }, [
        h('div', { class: 'flex items-center justify-between gap-3' }, [h('h2', { class: 'text-lg font-medium' }, '知识库问答'), h('label', { class: consoleSecondaryButtonClass }, [iconNode(HardDriveUpload, 14), uploadingKnowledge.value ? '上传中' : '上传 PDF', h('input', { type: 'file', accept: 'application/pdf', class: 'hidden', disabled: uploadingKnowledge.value, onChange: handleKnowledgeUpload })])]),
        h('textarea', { value: knowledgeQuestion.value, onInput: (event) => { knowledgeQuestion.value = event.target.value }, class: `${consoleInputClass} mt-4 min-h-[120px] resize-none`, placeholder: '例如：设备温度过高时应该如何排查？' }),
        buttonNode(askingKnowledge.value ? '检索中' : '检索并回答', `${consolePrimaryButtonClass} mt-3`, () => void handleKnowledgeQuestion(), FileText, askingKnowledge.value),
        h('div', { class: 'ops-panel-subtle mt-4 rounded-2xl p-4 text-sm leading-7 text-white/72' }, knowledgeAnswer.value),
        h('div', { class: 'mt-4 grid gap-2' }, sourceNodes(knowledgeSources.value)),
      ]),
      h('aside', { class: consolePanelClass }, [
        h('h2', { class: 'text-lg font-medium' }, '知识库文档'),
        h('p', { class: 'mt-2 text-sm leading-relaxed text-white/52' }, knowledgeUploadNote.value),
        h('div', { class: 'mt-4 grid gap-2' }, knowledgeDocuments.value.map((documentName) => h('div', { key: documentName, class: `${consoleRowCardClass} flex items-center justify-between gap-3` }, [
          h('span', { class: 'min-w-0 truncate text-sm' }, documentName),
          h('button', { class: consoleGhostButtonClass, disabled: deletingKnowledgeDocumentName.value === documentName, onClick: () => void handleKnowledgeDocumentDelete(documentName) }, deletingKnowledgeDocumentName.value === documentName ? '删除中' : '删除'),
        ]))),
      ]),
    ])
  },
})

const EmployeesPanel = defineComponent({
  setup() {
    return () => h('div', { class: 'grid gap-5 xl:grid-cols-[320px_minmax(0,1fr)]' }, [
      h('section', { class: consolePanelClass }, [
        h('h2', { class: 'text-lg font-medium' }, '新增员工'),
        h('div', { class: 'mt-4 grid gap-2' }, [
          h('input', { value: employeeForm.name, onInput: (event) => { employeeForm.name = event.target.value }, class: consoleInputClass, placeholder: '姓名' }),
          h('input', { value: employeeForm.age, onInput: (event) => { employeeForm.age = event.target.value }, class: consoleInputClass, placeholder: '年龄' }),
          h('input', { value: employeeForm.department, onInput: (event) => { employeeForm.department = event.target.value }, class: consoleInputClass, placeholder: '部门' }),
          buttonNode(savingEmployee.value ? '保存中' : '新增员工', consolePrimaryButtonClass, () => void handleEmployeeSave(), null, savingEmployee.value),
        ]),
      ]),
      h('section', { class: consolePanelClass }, [
        h('h2', { class: 'text-lg font-medium' }, '员工列表'),
        h('div', { class: 'mt-4 grid gap-2' }, employees.value.map((employee) => h('div', { key: employee.id, class: `${consoleRowCardClass} flex items-center justify-between gap-3` }, [
          h('div', [h('p', { class: 'text-sm font-medium' }, employee.name), h('p', { class: 'mt-1 text-xs text-white/45' }, `${employee.department} · ${employee.age} 岁`)]),
          h('button', { class: consoleSecondaryButtonClass, disabled: removingEmployeeId.value === employee.id, onClick: () => void handleEmployeeRemove(employee.id) }, removingEmployeeId.value === employee.id ? '删除中' : '删除'),
        ]))),
      ]),
    ])
  },
})

const UsersPanel = defineComponent({
  setup() {
    return () => h('div', { class: 'grid gap-5 xl:grid-cols-[340px_minmax(0,1fr)]' }, [
      h('section', { class: consolePanelClass }, [
        h('div', { class: 'flex items-center gap-3' }, [
          h('div', { class: 'rounded-2xl border border-white/10 bg-white/[0.05] p-3 text-cyan-100' }, [iconNode(UserCog, 20)]),
          h('div', [
            h('h2', { class: 'text-lg font-medium' }, '创建账户'),
            h('p', { class: 'mt-1 text-xs text-white/45' }, '仅管理员可以新增系统登录账户。'),
          ]),
        ]),
        h('div', { class: 'mt-5 grid gap-3' }, [
          h('label', { class: 'grid gap-2' }, [
            h('span', { class: 'text-xs text-white/48' }, '用户名'),
            h('input', { value: userForm.username, onInput: (event) => { userForm.username = event.target.value }, class: consoleInputClass, placeholder: '例如 operator01', autocomplete: 'off' }),
          ]),
          h('label', { class: 'grid gap-2' }, [
            h('span', { class: 'text-xs text-white/48' }, '初始密码'),
            h('input', { value: userForm.password, onInput: (event) => { userForm.password = event.target.value }, class: consoleInputClass, type: 'password', placeholder: '至少 8 个字符', autocomplete: 'new-password' }),
          ]),
          h('label', { class: 'grid gap-2' }, [
            h('span', { class: 'text-xs text-white/48' }, '账户角色'),
            h('select', { value: userForm.role, onChange: (event) => { userForm.role = event.target.value }, class: consoleInputClass }, [
              h('option', { value: 'OPERATOR' }, 'OPERATOR · 普通操作员'),
              h('option', { value: 'ADMIN' }, 'ADMIN · 管理员'),
            ]),
          ]),
          buttonNode(savingUser.value ? '创建中' : '创建账户', consolePrimaryButtonClass, () => void handleUserCreate(), UserRound, savingUser.value),
        ]),
        h('div', { class: 'ops-panel-subtle mt-4 rounded-2xl p-4 text-xs leading-6 text-white/50' }, [
          h('p', { class: 'font-medium text-white/70' }, '安全说明'),
          h('p', { class: 'mt-1' }, '密码由后端使用 BCrypt 加密，页面和用户列表都不会读取数据库中的密码哈希。'),
        ]),
      ]),
      h('section', { class: consolePanelClass }, [
        h('div', { class: 'flex flex-wrap items-center justify-between gap-3' }, [
          h('div', [
            h('h2', { class: 'text-lg font-medium' }, '系统账户'),
            h('p', { class: 'mt-1 text-xs text-white/45' }, `当前登录：${sessionUser.value?.username ?? '--'} · ${sessionUser.value?.role ?? '--'}`),
          ]),
          h('div', { class: 'flex items-center gap-2' }, [
            h('span', { class: consoleCodeChipClass }, `${users.value.length} 个账户`),
            h('button', { class: consoleGhostButtonClass, onClick: () => void refreshUsers() }, [iconNode(RefreshCw, 14), '刷新']),
          ]),
        ]),
        users.value.length
          ? h('div', { class: 'mt-4 grid gap-3' }, users.value.map((user) => {
              const isCurrent = user.username === sessionUser.value?.username
              const roleBusy = userActionId.value === `role-${user.id}`
              const passwordBusy = userActionId.value === `password-${user.id}`
              const deleteBusy = userActionId.value === `delete-${user.id}`
              return h('article', { key: user.id, class: `${consoleRowCardClass} p-4` }, [
                h('div', { class: 'flex flex-wrap items-start justify-between gap-3' }, [
                  h('div', { class: 'flex items-center gap-3' }, [
                    h('div', { class: 'rounded-2xl border border-white/10 bg-white/[0.05] p-2.5 text-white/68' }, [iconNode(UserRound, 18)]),
                    h('div', [
                      h('div', { class: 'flex flex-wrap items-center gap-2' }, [
                        h('p', { class: 'text-sm font-medium' }, user.username),
                        isCurrent ? h('span', { class: 'rounded-full bg-cyan-300/10 px-2 py-0.5 text-[11px] text-cyan-100' }, '当前账户') : null,
                      ]),
                      h('p', { class: 'mt-1 text-xs text-white/38' }, `用户 ID · ${user.id}`),
                    ]),
                  ]),
                  h('span', { class: ['rounded-full border px-2.5 py-1 text-[11px]', user.role === 'ADMIN' ? 'border-amber-300/20 bg-amber-300/10 text-amber-100' : 'border-white/10 bg-white/[0.05] text-white/58'] }, user.role),
                ]),
                h('div', { class: 'mt-4 grid gap-3 lg:grid-cols-[190px_minmax(220px,1fr)_auto]' }, [
                  h('div', { class: 'flex gap-2' }, [
                    h('select', { value: user.role, class: `${consoleInputClass} min-w-0 flex-1`, disabled: isCurrent || roleBusy, title: isCurrent ? '不能在当前会话中修改自己的角色' : '修改角色', onChange: (event) => void handleUserRoleUpdate(user, event.target.value) }, [
                      h('option', { value: 'OPERATOR' }, 'OPERATOR'),
                      h('option', { value: 'ADMIN' }, 'ADMIN'),
                    ]),
                  ]),
                  h('div', { class: 'flex min-w-0 gap-2' }, [
                    h('input', { value: userPasswordDrafts[user.id] ?? '', onInput: (event) => { userPasswordDrafts[user.id] = event.target.value }, class: `${consoleInputClass} min-w-0 flex-1`, type: 'password', placeholder: '输入新密码（至少 8 位）', autocomplete: 'new-password' }),
                    h('button', { class: consoleSecondaryButtonClass, disabled: passwordBusy, onClick: () => void handleUserPasswordReset(user) }, [iconNode(KeyRound, 14), passwordBusy ? '处理中' : '重置']),
                  ]),
                  h('button', { class: consoleGhostButtonClass, disabled: isCurrent || deleteBusy, title: isCurrent ? '不能删除当前登录账户' : '删除账户', onClick: () => void handleUserDelete(user) }, [iconNode(Trash2, 14), deleteBusy ? '删除中' : '删除']),
                ]),
              ])
            }))
          : h('div', { class: 'ops-panel-subtle mt-4 rounded-2xl border border-dashed border-white/12 p-6 text-center text-sm text-white/45' }, '暂无可显示的系统账户，请确认后端已重启并重新同步。'),
      ]),
    ])
  },
})

const ApiPanel = defineComponent({
  setup() {
    return () => h('div', { class: 'grid gap-5 xl:grid-cols-2' }, apiGroups.map((group) => h('section', { key: group.title, class: consolePanelClass }, [
      h('div', { class: 'flex items-center gap-2' }, [iconNode(group.icon, 16), h('h2', { class: 'text-lg font-medium' }, group.title)]),
      h('div', { class: 'mt-4 grid gap-2' }, group.endpoints.map((endpoint) => h('code', { key: endpoint, class: 'ops-code-chip block rounded-xl px-3 py-2 text-xs' }, endpoint))),
    ])))
  },
})

const AgentStudio = defineComponent({
  setup() {
    const expansionLanes = [
      ['Observe', '设备状态、遥测趋势、告警上下文'],
      ['Reason', 'RAG 来源、历史 trace、排障步骤'],
      ['Act', '设备指令、配置变更、人工二次确认'],
      ['Record', '巡检报告、处理闭环、操作审计'],
    ]
    const defaultSteps = [
      { id: 'status', title: '查设备状态', detail: '等待启动。', status: 'pending' },
      { id: 'alarms', title: '查告警历史', detail: '等待启动。', status: 'pending' },
      { id: 'knowledge', title: '检索说明书', detail: '等待启动。', status: 'pending' },
      { id: 'suggestion', title: '生成建议', detail: '等待启动。', status: 'pending' },
    ]
    return () => h('section', { class: 'relative min-h-[calc(100vh-57px)] overflow-hidden px-5 py-6' }, [
      h('div', { class: 'agent-aurora-bg agent-aurora-bg--single-hemisphere', 'aria-hidden': 'true' }, [
        h('div', { class: 'agent-aurora-bg__stars' }),
        h('div', { class: 'agent-aurora-bg__curtain agent-aurora-bg__curtain--one' }),
        h('div', { class: 'agent-aurora-bg__curtain agent-aurora-bg__curtain--two' }),
        h('div', { class: 'agent-aurora-bg__curtain agent-aurora-bg__curtain--three' }),
        h('div', { class: 'agent-aurora-bg__hemisphere' }, [
          h('span', { class: 'agent-aurora-bg__latitude agent-aurora-bg__latitude--one' }),
          h('span', { class: 'agent-aurora-bg__latitude agent-aurora-bg__latitude--two' }),
          h('span', { class: 'agent-aurora-bg__latitude agent-aurora-bg__latitude--three' }),
          h('span', { class: 'agent-aurora-bg__meridian agent-aurora-bg__meridian--one' }),
          h('span', { class: 'agent-aurora-bg__meridian agent-aurora-bg__meridian--two' }),
        ]),
        h('div', { class: 'agent-aurora-bg__shade' }),
      ]),
      h('div', { class: 'relative mx-auto flex min-h-[calc(100vh-105px)] max-w-7xl flex-col' }, [
        h('div', { class: 'mb-5 flex flex-wrap items-center justify-between gap-3' }, [
          h('div', { class: 'flex flex-wrap items-center gap-2' }, [
            buttonNode('设备监控', 'ops-ghost-button px-3 py-2 text-xs', () => navigate('#/console/devices'), Cpu),
            buttonNode('告警中心', 'ops-ghost-button px-3 py-2 text-xs', () => navigate('#/console/alerts'), BellRing),
            buttonNode('知识库', 'ops-ghost-button px-3 py-2 text-xs', () => navigate('#/console/rag'), FileText),
          ]),
          h('div', { class: 'rounded-full border border-white/10 bg-black/24 px-3 py-1.5 text-xs text-white/50' }, `${runModeLabel.value} · ${selectedDeviceCode.value}`),
        ]),
        h('div', { class: 'grid flex-1 content-start gap-6' }, [
          h('div', { class: 'min-w-0' }, [
            h('div', { class: 'mx-auto max-w-4xl text-center xl:mx-0 xl:text-left' }, [
              h('div', { class: 'inline-flex items-center gap-2 rounded-full border border-white/12 bg-white/[0.045] px-3 py-1 text-[11px] font-medium uppercase tracking-[0.22em] text-white/64' }, [iconNode(Sparkles, 13), 'OpsPilot Agent Studio']),
              h('h1', { class: 'mt-5 text-4xl font-semibold leading-[1.05] text-white sm:text-5xl lg:text-6xl' }, '把运维问题交给 Agent 先跑一遍。'),
              h('p', { class: 'mt-4 max-w-2xl text-sm leading-7 text-white/58 xl:text-base' }, '先锁定设备，再查状态、告警、知识库和 trace，最后给出能执行、能复盘、能留痕的建议。'),
            ]),
            h('div', { class: 'mt-6 grid items-start gap-4 xl:grid-cols-[minmax(0,1fr)_360px]' }, [
              h('div', { class: 'min-w-0 space-y-4' }, [
                h('section', { class: 'flex min-h-[620px] flex-col rounded-[28px] border border-white/12 bg-black/44 p-5 shadow-[0_24px_90px_rgba(0,0,0,0.42)] backdrop-blur-xl' }, [
                  h('div', { class: 'flex flex-wrap items-start justify-between gap-3' }, [
                    h('div', { class: 'flex items-center gap-2' }, [iconNode(Bot, 17), h('div', [h('h2', { class: 'text-base font-medium text-white' }, 'Agent 对话'), h('p', { class: 'text-xs text-white/42' }, '消息会保留在当前聊天框里，向下滚动查看完整输出和追问。')])]),
                    h('div', { class: 'flex max-w-full flex-wrap items-center gap-2' }, [
                      ...agentSessions.value.map((session) => h('button', { key: session.id, class: ['rounded-full px-3 py-1.5 text-xs transition-colors', session.id === memoryId.value ? 'bg-white text-black' : 'border border-white/10 bg-white/[0.04] text-white/62 hover:bg-white/[0.1]'], onClick: () => selectAgentSession(session.id) }, session.title)),
                      h('button', { class: 'inline-flex items-center gap-1 rounded-full border border-white/10 bg-white/[0.05] px-3 py-1.5 text-xs text-white/72 transition-colors hover:bg-white/[0.12]', onClick: () => createAgentSession() }, [iconNode(Plus, 12), '新建']),
                    ]),
                  ]),
                  h('div', { class: 'mt-4 min-h-0 flex-1 rounded-2xl border border-white/10 bg-white/[0.035] p-4' }, [
                    h('div', { class: 'h-[440px] overflow-y-auto pr-2' }, [
                      h('div', { class: 'grid gap-3' }, agentMessages.value.map((message) => {
                        const isUser = message.role === 'user'
                        return h('div', { key: message.id, class: ['flex', isUser ? 'justify-end' : 'justify-start'] }, [
                          h('div', { class: ['max-w-[min(760px,92%)] rounded-2xl border px-4 py-3', isUser ? 'border-cyan-200/18 bg-cyan-200/[0.08]' : 'border-white/10 bg-black/30'] }, [
                            h('p', { class: 'mb-1 text-[11px] text-white/38' }, isUser ? '运维人员' : 'OpsPilot Agent'),
                            h('p', { class: 'whitespace-pre-wrap break-words text-[15px] leading-7 text-white/80' }, message.content),
                          ]),
                        ])
                      })),
                    ]),
                  ]),
                  h('p', { class: 'mt-3 text-xs text-white/38' }, `当前会话 ${agentMessages.value.length} 条消息。长回答不会撑开页面，直接在这里滚动查看。`),
                ]),
                h('div', { class: 'rounded-[24px] border border-white/12 bg-black/48 p-3 shadow-[0_24px_90px_rgba(0,0,0,0.42)] backdrop-blur-xl' }, [
                  h('textarea', { value: agentInput.value, onInput: (event) => { agentInput.value = event.target.value }, onKeydown: (event) => { if (event.key === 'Enter' && !event.shiftKey) { event.preventDefault(); void handleAgentSend() } }, class: 'min-h-[116px] w-full resize-none rounded-2xl border border-white/10 bg-white/[0.035] px-4 py-4 text-base leading-7 text-white outline-none transition-colors placeholder:text-white/34 focus:border-white/22', placeholder: '例如：帮我诊断 ESP32-001 的高温问题，并给出处理闭环。' }),
                  h('div', { class: 'mt-3 flex flex-wrap items-center justify-between gap-3' }, [
                    h('div', { class: 'flex flex-wrap items-center gap-2' }, [
                      h('button', { class: 'inline-flex h-9 items-center gap-2 rounded-full border border-white/10 px-3 text-xs text-white/62 transition-colors hover:bg-white/[0.06]', onClick: () => navigate('#/console/rag') }, [iconNode(FileText, 14), `知识库 ${knowledgeDocuments.value.length}`]),
                      h('div', { class: 'inline-flex h-9 items-center gap-2 rounded-full border border-white/10 px-3 text-xs text-white/50' }, [iconNode(ShieldCheck, 14), h('span', `短期记忆：${activeSessionTitle.value}`)]),
                    ]),
                    h('button', { class: 'inline-flex h-11 items-center justify-center gap-2 rounded-full bg-white px-5 text-sm font-semibold text-black transition-colors hover:bg-white/90 disabled:cursor-not-allowed disabled:opacity-60', onClick: () => void handleAgentSend(), disabled: chatting.value || !agentInput.value.trim() }, [chatting.value ? '思考中' : '发送', iconNode(SendHorizonal, 16)]),
                  ]),
                ]),
                h('div', { class: 'mt-5 flex flex-wrap justify-center gap-2 xl:justify-start' }, agentTasks.value.map((task) =>
                  h('button', { key: task.label, class: 'inline-flex items-center gap-2 rounded-full border border-white/10 bg-black/32 px-4 py-2 text-sm text-white/72 transition-colors hover:bg-white/[0.08] hover:text-white disabled:cursor-not-allowed disabled:opacity-50', disabled: chatting.value, onClick: () => void handleAgentSend(task.prompt) }, [iconNode(task.icon, 15), task.label]),
                )),
              ]),
              h('div', { class: 'grid gap-4' }, [
                h('section', { class: 'rounded-[26px] border border-white/12 bg-black/42 p-5 backdrop-blur-xl' }, [
                  h('div', { class: 'flex items-center justify-between gap-3' }, [h('div', { class: 'inline-flex items-center gap-2 text-[11px] font-medium uppercase tracking-[0.22em] text-white/42' }, [iconNode(ShieldCheck, 13), '运行上下文']), h('span', { class: 'rounded-full border border-white/10 bg-white/[0.05] px-3 py-1 text-xs text-white/62' }, selectedDeviceCode.value)]),
                  h('p', { class: 'mt-3 text-sm leading-6 text-white/58' }, `当前模式：${runModeLabel.value}。右上角不再留白，这里集中展示上下文、风险和当前请求。`),
                  h('div', { class: 'mt-4 grid grid-cols-2 gap-3' }, [
                    ['在线设备', `${onlineDeviceCount.value}/${devices.value.length}`],
                    ['待处理告警', totalOpenAlerts.value],
                    ['知识源', knowledgeDocuments.value.length],
                    ['Trace 步数', latestTrace.value.length],
                  ].map(([label, value]) => h('div', { class: 'rounded-2xl border border-white/10 bg-white/[0.035] p-3' }, [h('p', { class: 'text-[11px] uppercase tracking-[0.18em] text-white/36' }, label), h('p', { class: 'mt-2 text-2xl font-semibold text-white' }, value)]))),
                  h('div', { class: 'mt-4 rounded-2xl border border-white/10 bg-white/[0.035] p-4' }, [h('p', { class: 'text-[11px] font-medium uppercase tracking-[0.18em] text-white/38' }, '当前请求'), h('p', { class: 'mt-2 text-sm leading-6 text-white/76' }, latestUserMessage.value?.content ?? `${selectedDeviceCode.value} 当前状态怎么样？`)]),
                  h('div', { class: 'mt-3 rounded-2xl border border-white/10 bg-white/[0.03] p-4' }, [h('p', { class: 'text-[11px] font-medium uppercase tracking-[0.18em] text-white/38' }, '判断链路'), latestTrace.value.length ? traceNodes(latestTrace.value) : h('p', { class: 'mt-2 text-sm leading-6 text-white/46' }, '等待下一轮工具调用后显示 trace。')]),
                  h('div', { class: 'mt-3 rounded-2xl border border-white/10 bg-white/[0.03] p-4' }, [h('p', { class: 'text-[11px] font-medium uppercase tracking-[0.18em] text-white/38' }, '依据来源'), latestSources.value.length ? sourceNodes(latestSources.value, true) : h('p', { class: 'mt-2 text-sm leading-6 text-white/46' }, '等待知识库或联网检索命中后显示来源。')]),
                ]),
                h('section', { class: 'rounded-[24px] border border-white/12 bg-black/42 p-4 backdrop-blur-xl' }, [
                  h('div', { class: 'flex items-center gap-2' }, [iconNode(Workflow, 17), h('h2', { class: 'text-base font-medium' }, '排障流程')]),
                  h('p', { class: 'mt-2 text-sm leading-6 text-white/54' }, maintenanceSummary.value),
                  h('button', { class: 'mt-4 inline-flex w-full items-center justify-center gap-2 rounded-full bg-white px-4 py-2.5 text-sm font-semibold text-black transition-colors hover:bg-white/90 disabled:cursor-not-allowed disabled:opacity-60', disabled: maintenanceRunning.value, onClick: () => void handleRunMaintenance() }, [iconNode(Workflow, 15), maintenanceRunning.value ? '正在排障' : '启动一键排障']),
                  h('div', { class: 'mt-4 grid gap-2' }, (maintenanceSteps.value.length ? maintenanceSteps.value : defaultSteps).map((step, index) => h('div', { key: step.id, class: 'rounded-2xl border border-white/10 bg-white/[0.035] p-3' }, [
                    h('div', { class: 'flex items-center justify-between gap-2' }, [
                      h('p', { class: 'text-sm font-medium' }, `${index + 1}. ${step.title}`),
                      h('span', { class: ['rounded-full px-2 py-0.5 text-[11px]', step.status === 'done' ? 'bg-emerald-400/12 text-emerald-100' : step.status === 'running' ? 'bg-amber-400/12 text-amber-100' : 'bg-white/[0.06] text-white/38'] }, step.status === 'done' ? '完成' : step.status === 'running' ? '执行中' : '待执行'),
                    ]),
                    h('p', { class: 'mt-1 text-xs leading-relaxed text-white/48' }, step.detail),
                  ]))),
                ]),
              ]),
            ]),
          ]),
        ]),
        h('div', { class: 'relative mt-6 grid gap-3 border-t border-white/10 pt-5 md:grid-cols-4' }, expansionLanes.map(([title, detail]) => h('div', { key: title, class: 'rounded-2xl border border-white/10 bg-black/30 p-4 backdrop-blur' }, [h('p', { class: 'text-xs font-semibold uppercase tracking-[0.18em] text-white/42' }, title), h('p', { class: 'mt-2 text-sm leading-6 text-white/68' }, detail)]))),
        h('p', { class: 'mt-4 rounded-2xl border border-white/10 bg-black/28 px-4 py-3 text-xs leading-6 text-white/46' }, notice.value),
      ]),
    ])
  },
})

const activeConsolePanel = computed(() => ({
  devices: DevicePanel,
  alerts: AlertPanel,
  rag: RagPanel,
  employees: EmployeesPanel,
  users: UsersPanel,
  api: ApiPanel,
})[route.tab] ?? ApiPanel)
</script>
