export interface Device {
  id: number
  deviceCode: string
  name: string
  type: string
  location: string
  online: boolean
  lastOnlineTime: string | null
  deviceToken?: string
}

export interface DevicePayload {
  deviceCode: string
  name: string
  type: string
  location: string
  online?: boolean
  lastOnlineTime?: string | null
  deviceToken?: string
}

export interface DeviceData {
  id: number
  deviceId: number
  temperature: number
  humidity: number
  rssi?: number | null
  uptimeSeconds?: number | null
  firmwareVersion?: string | null
  reportTime: string
}

export interface DeviceConfig {
  id: number
  deviceId: number
  temperatureThreshold: number
  humidityMinThreshold: number
  humidityMaxThreshold: number
  sampleIntervalSeconds: number
  inspectionIntervalMinutes: number
  displayMode: string
  updateTime: string | null
}

export interface DeviceConfigPayload {
  temperatureThreshold?: number
  humidityMinThreshold?: number
  humidityMaxThreshold?: number
  sampleIntervalSeconds?: number
  inspectionIntervalMinutes?: number
  displayMode?: string
}

export interface DeviceCommand {
  id: number
  deviceId: number
  commandType: string
  payload: string
  status: 'PENDING' | 'SENT' | 'DONE' | 'FAILED' | string
  issuedBy: string
  resultMessage: string | null
  createTime: string
  sentTime: string | null
  doneTime: string | null
}

export interface DeviceCommandPayload {
  commandType: string
  payload?: string
}

export interface DeviceRegistrationResponse {
  deviceCode: string
  deviceToken: string
}

export interface Alarm {
  id: number
  deviceId: number
  alarmType: string
  alarmValue: string
  status: string
  createTime: string
}

export interface Employee {
  id: number
  name: string
  age: number
  department: string
}

export type UserRole = 'ADMIN' | 'OPERATOR'

export interface UserAccount {
  id: number
  username: string
  role: UserRole
}

export interface CreateUserPayload {
  username: string
  password: string
  role: UserRole
}

export interface AuthSession {
  username: string
  role: UserRole
}

export interface KnowledgeSource {
  title: string
  snippet: string
  url?: string
  documentId?: string
  page?: number
  score?: number
}

export interface KnowledgeAnswer {
  answer: string
  sources: KnowledgeSource[]
}

export interface AgentTraceStep {
  toolName: string
  args: Record<string, unknown>
  result: string
  time: string
  durationMs?: number
  status?: 'success' | 'warning' | 'error'
}

export interface AgentChatResponse {
  answer: string
  trace: AgentTraceStep[]
  sources: KnowledgeSource[]
}

interface ResultPayload<T> {
  code: number
  message: string
  data: T
}

type RawKnowledgeAnswer =
  | string
  | Partial<KnowledgeAnswer> & {
      content?: string
      result?: string
      sourceDocuments?: KnowledgeSource[]
    }

type RawAgentChatResponse =
  | string
  | Partial<AgentChatResponse> & {
      content?: string
      result?: string
      toolTrace?: AgentTraceStep[]
      toolCalls?: AgentTraceStep[]
      sourceDocuments?: KnowledgeSource[]
    }

const API_BASE = import.meta.env.VITE_API_BASE_URL || '/api'
const TOKEN_KEY = 'opspilot-token'
const DEFAULT_REQUEST_TIMEOUT_MS = 6000
const AI_REQUEST_TIMEOUT_MS = 60000

interface ApiRequestInit extends RequestInit {
  timeoutMs?: number
}

export const mockDevices: Device[] = [
  {
    id: 1,
    deviceCode: 'ESP32-001',
    name: '温湿度传感器',
    type: '环境采集',
    location: '3号机房',
    online: true,
    lastOnlineTime: '2026-06-18T21:12:00',
    deviceToken: 'demo-device-token',
  },
  {
    id: 2,
    deviceCode: 'ESP32-002',
    name: '机柜环境节点',
    type: '边缘采集',
    location: 'A区配电间',
    online: true,
    lastOnlineTime: '2026-06-18T20:56:00',
    deviceToken: 'demo-device-token-002',
  },
  {
    id: 3,
    deviceCode: 'ESP32-003',
    name: '冷通道探针',
    type: '温湿巡检',
    location: 'B区冷通道',
    online: false,
    lastOnlineTime: '2026-06-18T18:41:00',
    deviceToken: 'demo-device-token-003',
  },
]

export const mockHistories: Record<string, DeviceData[]> = {
  'ESP32-001': [
    { id: 11, deviceId: 1, temperature: 36.8, humidity: 61.2, rssi: -58, uptimeSeconds: 7280, firmwareVersion: '1.0.0', reportTime: '2026-06-18T21:12:00' },
    { id: 12, deviceId: 1, temperature: 35.9, humidity: 60.1, rssi: -61, uptimeSeconds: 5800, firmwareVersion: '1.0.0', reportTime: '2026-06-18T20:48:00' },
    { id: 13, deviceId: 1, temperature: 34.7, humidity: 58.4, rssi: -63, uptimeSeconds: 3820, firmwareVersion: '1.0.0', reportTime: '2026-06-18T20:15:00' },
  ],
  'ESP32-002': [
    { id: 21, deviceId: 2, temperature: 28.5, humidity: 45.2, rssi: -52, uptimeSeconds: 6400, firmwareVersion: '1.0.0', reportTime: '2026-06-18T20:56:00' },
    { id: 22, deviceId: 2, temperature: 28.1, humidity: 44.8, rssi: -54, uptimeSeconds: 4300, firmwareVersion: '1.0.0', reportTime: '2026-06-18T20:21:00' },
  ],
  'ESP32-003': [
    { id: 31, deviceId: 3, temperature: 25.4, humidity: 41.3, rssi: -78, uptimeSeconds: 2200, firmwareVersion: '0.9.8', reportTime: '2026-06-18T18:41:00' },
  ],
}

export const mockConfigs: Record<string, DeviceConfig> = {
  'ESP32-001': {
    id: 1001,
    deviceId: 1,
    temperatureThreshold: 35,
    humidityMinThreshold: 20,
    humidityMaxThreshold: 80,
    sampleIntervalSeconds: 10,
    inspectionIntervalMinutes: 30,
    displayMode: 'ALERT',
    updateTime: '2026-06-18T21:10:00',
  },
  'ESP32-002': {
    id: 1002,
    deviceId: 2,
    temperatureThreshold: 35,
    humidityMinThreshold: 20,
    humidityMaxThreshold: 80,
    sampleIntervalSeconds: 15,
    inspectionIntervalMinutes: 30,
    displayMode: 'NORMAL',
    updateTime: '2026-06-18T20:40:00',
  },
  'ESP32-003': {
    id: 1003,
    deviceId: 3,
    temperatureThreshold: 34,
    humidityMinThreshold: 20,
    humidityMaxThreshold: 75,
    sampleIntervalSeconds: 30,
    inspectionIntervalMinutes: 15,
    displayMode: 'MAINTENANCE',
    updateTime: '2026-06-18T18:50:00',
  },
}

export const mockCommands: Record<string, DeviceCommand[]> = {
  'ESP32-001': [
    {
      id: 501,
      deviceId: 1,
      commandType: 'RELAY_ON',
      payload: 'FAN',
      status: 'SENT',
      issuedBy: 'agent',
      resultMessage: '等待 ESP32 ack',
      createTime: '2026-06-18T21:15:00',
      sentTime: '2026-06-18T21:15:02',
      doneTime: null,
    },
    {
      id: 502,
      deviceId: 1,
      commandType: 'DISPLAY_MESSAGE',
      payload: '高温巡检: 检查散热',
      status: 'PENDING',
      issuedBy: 'agent',
      resultMessage: null,
      createTime: '2026-06-18T21:14:00',
      sentTime: null,
      doneTime: null,
    },
    {
      id: 504,
      deviceId: 1,
      commandType: 'SET_SAMPLE_INTERVAL',
      payload: '10',
      status: 'DONE',
      issuedBy: 'admin',
      resultMessage: '采样间隔已更新',
      createTime: '2026-06-18T20:30:00',
      sentTime: '2026-06-18T20:30:08',
      doneTime: '2026-06-18T20:30:09',
    },
  ],
  'ESP32-002': [],
  'ESP32-003': [
    {
      id: 503,
      deviceId: 3,
      commandType: 'RUN_SELF_TEST',
      payload: '',
      status: 'FAILED',
      issuedBy: 'agent',
      resultMessage: 'WiFi RSSI 过低',
      createTime: '2026-06-18T19:00:00',
      sentTime: '2026-06-18T19:01:00',
      doneTime: '2026-06-18T19:01:05',
    },
  ],
}

export const mockAlarms: Record<string, Alarm[]> = {
  'ESP32-001': [
    {
      id: 101,
      deviceId: 1,
      alarmType: '高温告警',
      alarmValue: '36.8°C',
      status: '未处理',
      createTime: '2026-06-18T21:12:00',
    },
    {
      id: 102,
      deviceId: 1,
      alarmType: '高温预警',
      alarmValue: '35.9°C',
      status: '处理中',
      createTime: '2026-06-18T20:48:00',
    },
  ],
  'ESP32-002': [],
  'ESP32-003': [
    {
      id: 301,
      deviceId: 3,
      alarmType: '离线告警',
      alarmValue: '最近 2 小时无上报',
      status: '待确认',
      createTime: '2026-06-18T20:00:00',
    },
  ],
}

export const mockEmployees: Employee[] = [
  { id: 1, name: '张三', age: 20, department: '技术部' },
  { id: 2, name: '李四', age: 21, department: '研发部' },
  { id: 3, name: '王敏', age: 24, department: '值班运维' },
]

function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

function setToken(token: string) {
  localStorage.setItem(TOKEN_KEY, token)
}

export function getSessionUser(): AuthSession | null {
  const token = getToken()
  if (!token) return null

  try {
    const payloadPart = token.split('.')[1]
    if (!payloadPart) return null
    const normalized = payloadPart.replace(/-/g, '+').replace(/_/g, '/')
    const padded = normalized.padEnd(Math.ceil(normalized.length / 4) * 4, '=')
    const payload = JSON.parse(atob(padded)) as { sub?: string; role?: string }
    const role = payload.role?.toUpperCase()
    if (!payload.sub || (role !== 'ADMIN' && role !== 'OPERATOR')) return null
    return { username: payload.sub, role }
  } catch {
    return null
  }
}

export function clearToken() {
  localStorage.removeItem(TOKEN_KEY)
}

async function fetchWithTimeout(url: string, init: RequestInit, timeoutMs = DEFAULT_REQUEST_TIMEOUT_MS): Promise<Response> {
  const controller = new AbortController()
  const timeoutId = window.setTimeout(() => controller.abort(), timeoutMs)

  try {
    return await fetch(url, {
      ...init,
      signal: controller.signal,
    })
  } catch (error) {
    if (error instanceof DOMException && error.name === 'AbortError') {
      throw new Error('后端响应超时，请检查 Spring Boot 是否已启动。')
    }
    throw error
  } finally {
    window.clearTimeout(timeoutId)
  }
}

async function request<T>(path: string, init: ApiRequestInit = {}): Promise<T> {
  const { timeoutMs, ...fetchInit } = init
  const headers = new Headers(init.headers)
  const bodyIsFormData = fetchInit.body instanceof FormData

  if (!bodyIsFormData && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const token = getToken()
  if (token) {
    headers.set('Authorization', `Bearer ${token}`)
  }

  const response = await fetchWithTimeout(`${API_BASE}${path}`, {
    ...fetchInit,
    headers,
  }, timeoutMs)

  const text = await response.text()
  const payload = text ? (JSON.parse(text) as ResultPayload<T> | T) : null

  if (!response.ok) {
    if (typeof payload === 'object' && payload && 'message' in payload) {
      throw new Error(String(payload.message))
    }
    throw new Error(text || `HTTP ${response.status}`)
  }

  if (typeof payload === 'object' && payload && 'code' in payload) {
    if (payload.code !== 200) {
      throw new Error(payload.message || '请求失败')
    }
    return payload.data
  }

  return payload as T
}

function nowLabel() {
  return new Date().toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

function normalizeSources(sources: unknown): KnowledgeSource[] {
  return Array.isArray(sources)
    ? sources
        .filter((source): source is KnowledgeSource => Boolean(source && typeof source === 'object'))
        .map((source, index) => ({
          title: source.title || `知识片段 ${index + 1}`,
          snippet: source.snippet || '',
          url: source.url,
          documentId: source.documentId,
          page: source.page,
          score: source.score,
        }))
    : []
}

function normalizeTrace(trace: unknown): AgentTraceStep[] {
  return Array.isArray(trace)
    ? trace
        .filter((step): step is AgentTraceStep => Boolean(step && typeof step === 'object'))
        .map((step) => ({
          toolName: step.toolName || 'unknownTool',
          args: step.args || {},
          result: step.result || '后端返回了工具调用记录，但没有提供结果摘要。',
          time: step.time || nowLabel(),
          durationMs: step.durationMs,
          status: step.status || 'success',
        }))
    : []
}

function normalizeKnowledgeAnswer(payload: RawKnowledgeAnswer): KnowledgeAnswer {
  if (typeof payload === 'string') {
    return {
      answer: payload,
      sources: [],
    }
  }

  return {
    answer: payload.answer || payload.content || payload.result || '知识库暂未返回答案。',
    sources: normalizeSources(payload.sources || payload.sourceDocuments),
  }
}

function normalizeAgentResponse(payload: RawAgentChatResponse): AgentChatResponse {
  if (typeof payload === 'string') {
    return {
      answer: payload,
      trace: [],
      sources: [],
    }
  }

  return {
    answer: payload.answer || payload.content || payload.result || 'Agent 暂未返回答案。',
    trace: normalizeTrace(payload.trace || payload.toolTrace || payload.toolCalls),
    sources: normalizeSources(payload.sources || payload.sourceDocuments),
  }
}

export async function login(username: string, password: string) {
  const token = await request<string>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password }),
  })
  setToken(token)
  return token
}

export function fetchDevices() {
  return request<Device[]>('/device/list')
}

export function fetchDeviceHistory(deviceCode: string) {
  return request<DeviceData[]>(`/device/${deviceCode}/data`)
}

export function fetchDeviceAlarms(deviceCode: string) {
  return request<Alarm[]>(`/device/${deviceCode}/alarms`)
}

export function fetchDeviceConfig(deviceCode: string) {
  return request<DeviceConfig>(`/device/${deviceCode}/config`)
}

export function updateDeviceConfig(deviceCode: string, config: DeviceConfigPayload) {
  return request<DeviceConfig>(`/device/${deviceCode}/config`, {
    method: 'PUT',
    body: JSON.stringify(config),
  })
}

export function fetchDeviceCommands(deviceCode: string) {
  return request<DeviceCommand[]>(`/device/${deviceCode}/commands`)
}

export function createDeviceCommand(deviceCode: string, command: DeviceCommandPayload) {
  return request<DeviceCommand>(`/device/${deviceCode}/commands`, {
    method: 'POST',
    body: JSON.stringify(command),
  })
}

export function resolveAlarm(deviceCode: string, alarmId: number) {
  return request<string>(`/device/${deviceCode}/alarms/${alarmId}`, {
    method: 'PATCH',
  })
}

export function createDevice(device: DevicePayload) {
  return request<DeviceRegistrationResponse>('/device', {
    method: 'POST',
    body: JSON.stringify(device),
  })
}

export function updateDevice(id: number, device: DevicePayload) {
  return request<string>(`/device/${id}`, {
    method: 'PUT',
    body: JSON.stringify(device),
  })
}

export function deleteDevice(id: number) {
  return request<string>(`/device/${id}`, {
    method: 'DELETE',
  })
}

export function fetchEmployees() {
  return request<Employee[]>('/employee')
}

export function createEmployee(employee: Omit<Employee, 'id'>) {
  return request<string>('/employee', {
    method: 'POST',
    body: JSON.stringify(employee),
  })
}

export function deleteEmployee(id: number) {
  return request<string>(`/employee/${id}`, {
    method: 'DELETE',
  })
}

export function fetchUsers() {
  return request<UserAccount[]>('/users')
}

export function createUser(user: CreateUserPayload) {
  return request<UserAccount>('/users', {
    method: 'POST',
    body: JSON.stringify(user),
  })
}

export function resetUserPassword(id: number, password: string) {
  return request<string>(`/users/${id}/password`, {
    method: 'PUT',
    body: JSON.stringify({ password }),
  })
}

export function updateUserRole(id: number, role: UserRole) {
  return request<UserAccount>(`/users/${id}/role`, {
    method: 'PUT',
    body: JSON.stringify({ role }),
  })
}

export function deleteUser(id: number) {
  return request<string>(`/users/${id}`, {
    method: 'DELETE',
  })
}

export function reportDeviceData(deviceCode: string, temperature: number, humidity: number) {
  return request<string>('/device/report', {
    method: 'POST',
    body: JSON.stringify({
      deviceCode,
      temperature,
      humidity,
    }),
  })
}

export async function askKnowledge(question: string) {
  const payload = await request<RawKnowledgeAnswer>(`/pdf/chat?question=${encodeURIComponent(question)}`, {
    timeoutMs: AI_REQUEST_TIMEOUT_MS,
  })
  return normalizeKnowledgeAnswer(payload)
}

export function uploadKnowledgeFile(file: File) {
  const formData = new FormData()
  formData.append('file', file)

  return request<string>('/pdf/upload', {
    method: 'POST',
    body: formData,
  })
}

export function fetchKnowledgeDocuments() {
  return request<string[]>('/pdf/documents')
}

export function deleteKnowledgeDocument(documentName: string) {
  return request<string>(`/pdf/documents/${encodeURIComponent(documentName)}`, {
    method: 'DELETE',
  })
}

export async function chatWithAgent(message: string, memoryId: string) {
  const payload = await request<RawAgentChatResponse>(
    '/agent/chat',
    {
      method: 'POST',
      body: JSON.stringify({ message, memoryId }),
      timeoutMs: AI_REQUEST_TIMEOUT_MS,
    },
  )
  return normalizeAgentResponse(payload)
}

export function buildMockKnowledgeSources(question: string): KnowledgeSource[] {
  const isWiring = question.includes('接线') || question.includes('说明书')

  return [
    {
      title: isWiring ? '设备说明书 / DHT22 接线' : '排障手册 / 高温告警',
      snippet: isWiring
        ? '确认 3.3V 供电稳定后，将 DHT22 数据引脚接入 ESP32 GPIO4，并在上报前校验 payload 字段。'
        : '温度超过 35°C 时，优先检查机柜散热、传感器固定位置、冷通道风量与风道阻塞情况。',
      documentId: isWiring ? 'manual-dht22' : 'runbook-temperature',
      page: isWiring ? 2 : 3,
      score: isWiring ? 0.92 : 0.89,
    },
    {
      title: '历史告警知识片段',
      snippet: '连续两次高温上报应触发告警确认，并记录处理动作，便于后续审计与复盘。',
      documentId: 'alarm-policy',
      page: 5,
      score: 0.81,
    },
  ]
}

export function buildMockKnowledgeAnswer(question: string): KnowledgeAnswer {
  const sources = buildMockKnowledgeSources(question)

  if (question.includes('接线') || question.includes('说明书')) {
    return {
      answer: '根据演示知识库，设备接线应先确认供电，再连接 DHT22 数据引脚，最后校验上报报文格式。',
      sources,
    }
  }

  if (question.includes('温度') || question.includes('告警')) {
    return {
      answer: '根据演示知识库，温度超过 35°C 时应优先检查机柜散热、传感器固定位置与风道阻塞情况。',
      sources,
    }
  }

  return {
    answer: `演示模式回答：已收到问题“${question}”。当前会优先检索 PDF 片段，再根据来源生成答案并回传引用。`,
    sources,
  }
}

export function buildMockAgentAnswer(
  message: string,
  device: Device | undefined,
  latest: DeviceData | undefined,
  alarms: Alarm[] = [],
): AgentChatResponse {
  const deviceCode = device?.deviceCode ?? 'ESP32-001'
  const sources = buildMockKnowledgeSources(message)
  const openAlarm = alarms.find((alarm) => alarm.status !== '已处理')
  const relayIntent = message.includes('继电器') || message.includes('风扇') || message.includes('散热')
  const relayCommand = message.includes('关') || message.includes('关闭')
    ? 'RELAY_OFF'
    : message.includes('脉冲') || message.includes('点动')
      ? 'RELAY_PULSE'
      : 'RELAY_ON'
  const trace: AgentTraceStep[] = [
    {
      toolName: 'getDeviceStatus',
      args: { deviceCode },
      result: `${deviceCode} 当前${device?.online ? '在线' : '离线'}，最新温度 ${latest?.temperature ?? 36.8}°C，湿度 ${latest?.humidity ?? 61.2}%。`,
      time: nowLabel(),
      durationMs: 118,
      status: device?.online ? 'success' : 'warning',
    },
    {
      toolName: 'getAlarmHistory',
      args: { deviceCode, limit: 5 },
      result: openAlarm
        ? `发现 ${openAlarm.alarmType}：${openAlarm.alarmValue}，状态 ${openAlarm.status}。`
        : '最近没有未处理告警。',
      time: nowLabel(),
      durationMs: 156,
      status: openAlarm ? 'warning' : 'success',
    },
    {
      toolName: 'searchKnowledge',
      args: { query: message, topK: 2 },
      result: `命中 ${sources.length} 条知识片段，最高相关度 ${Math.round((sources[0]?.score ?? 0.9) * 100)}%。`,
      time: nowLabel(),
      durationMs: 244,
      status: 'success',
    },
  ]

  if (message.includes('阈值')) {
    trace.push({
      toolName: 'setAlertThreshold',
      args: { deviceCode, type: 'temperature', value: 35 },
      result: `${deviceCode} 高温阈值已更新，后续上报会按动态阈值生成告警。`,
      time: nowLabel(),
      durationMs: 132,
      status: 'success',
    })
  }

  if (message.includes('屏幕') || message.includes('自检') || message.includes('采样') || relayIntent) {
    const commandType = relayIntent
      ? relayCommand
      : message.includes('自检')
        ? 'RUN_SELF_TEST'
        : 'DISPLAY_MESSAGE'
    trace.push({
      toolName: 'issueDeviceCommand',
      args: { deviceCode, commandType, payload: relayIntent ? 'FAN' : undefined },
      result: `已创建 ${deviceCode} 设备命令，等待 ESP32 轮询执行。`,
      time: nowLabel(),
      durationMs: 149,
      status: 'success',
    })
  }

  if (relayIntent) {
    return {
      answer: `${deviceCode} 已创建 ${relayCommand} 命令，ESP32 轮询后会控制 GPIO26 继电器并回传执行结果。`,
      trace,
      sources,
    }
  }

  if (message.includes('状态')) {
    return {
      answer: `${deviceCode} 当前${device?.online ? '在线' : '离线'}，最新温度 ${latest?.temperature ?? 36.8}°C，湿度 ${latest?.humidity ?? 61.2}%。建议继续观察告警趋势。`,
      trace,
      sources,
    }
  }

  if (message.includes('告警') || message.includes('排查') || message.includes('温度')) {
    return {
      answer: `${deviceCode} 建议按四步闭环处理：先确认设备在线与最近上报，再核对告警历史，随后检索说明书和排障手册，最后记录处理动作。`,
      trace,
      sources,
    }
  }

  return {
    answer: `演示模式 Agent：已收到“${message}”。当前会结合设备工具、告警历史和知识库路由生成回答。`,
    trace,
    sources,
  }
}

export function cloneMockHistories() {
  return JSON.parse(JSON.stringify(mockHistories)) as Record<string, DeviceData[]>
}

export function cloneMockConfigs() {
  return JSON.parse(JSON.stringify(mockConfigs)) as Record<string, DeviceConfig>
}

export function cloneMockCommands() {
  return JSON.parse(JSON.stringify(mockCommands)) as Record<string, DeviceCommand[]>
}

export function cloneMockAlarms() {
  return JSON.parse(JSON.stringify(mockAlarms)) as Record<string, Alarm[]>
}

export function cloneMockDevices() {
  return JSON.parse(JSON.stringify(mockDevices)) as Device[]
}

export function cloneMockEmployees() {
  return JSON.parse(JSON.stringify(mockEmployees)) as Employee[]
}
