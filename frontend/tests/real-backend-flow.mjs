import { chromium } from 'playwright'

const baseUrl = 'http://127.0.0.1:5173'
const deviceCode = `QA-${Date.now()}`

async function main() {
  const browser = await chromium.launch({ headless: true })
  const page = await browser.newPage({ viewport: { width: 1440, height: 1024 } })
  const result = {
    login: false,
    runModeOnline: false,
    deviceCreated: false,
    deviceUpdated: false,
    alarmResolved: false,
    knowledgeDeleteUi: false,
    cleanupDeviceRemoved: false,
  }

  try {
    await page.goto(`${baseUrl}/#/login`, { waitUntil: 'domcontentloaded' })
    await page.waitForTimeout(1200)

    await page.getByRole('button', { name: /进入 OpsPilot/ }).click()
    await page.waitForURL(/#\/console\/devices/, { timeout: 15000 })
    result.login = true

    const bodyText = (await page.textContent('body')) || ''
    result.runModeOnline = bodyText.includes('真实接口')

    await page.getByRole('button', { name: /新建设备/ }).click()
    await page.getByPlaceholder('设备编号，如 ESP32-004').fill(deviceCode)
    await page.getByPlaceholder('设备名称').fill('QA 联调设备')
    await page.getByPlaceholder('设备类型').fill('测试传感器')
    await page.getByPlaceholder('安装位置').fill('控制台联调区')
    await page.getByRole('button', { name: /新增设备/ }).click()
    await page.getByRole('button', { name: new RegExp(deviceCode) }).first().waitFor({ timeout: 10000 })
    result.deviceCreated = true

    await page.getByPlaceholder('设备名称').fill('QA 联调设备-已更新')
    await page.getByPlaceholder('安装位置').fill('控制台联调区-已更新')
    await page.getByRole('button', { name: /保存修改/ }).click()
    await page.getByText('QA 联调设备-已更新').first().waitFor({ timeout: 10000 })
    result.deviceUpdated = true

    await page.getByPlaceholder('温度 °C').fill('38.6')
    await page.getByPlaceholder('湿度 %').fill('61.4')
    await page.getByRole('button', { name: /提交上报/ }).click()
    await page.waitForTimeout(900)

    await page.getByRole('button', { name: '告警中心' }).click()
    await page.waitForURL(/#\/console\/alerts/, { timeout: 10000 })
    await page.getByText(deviceCode).first().waitFor({ timeout: 10000 })
    await page.getByRole('button', { name: /标记处理/ }).click()
    await page.waitForTimeout(800)
    result.alarmResolved = ((await page.textContent('body')) || '').includes('已处理')

    await page.getByRole('button', { name: 'RAG 问答' }).click()
    await page.waitForURL(/#\/console\/rag/, { timeout: 10000 })

    const deleteButtons = page.getByRole('button', { name: /^删除$/ })
    const beforeDeleteCount = await deleteButtons.count()
    if (beforeDeleteCount > 0) {
      await deleteButtons.first().click()
      await page.waitForTimeout(600)
      const afterDeleteCount = await deleteButtons.count()
      result.knowledgeDeleteUi = afterDeleteCount < beforeDeleteCount
    }

    await page.getByRole('button', { name: '设备监控' }).click()
    await page.waitForURL(/#\/console\/devices/, { timeout: 10000 })
    await page.getByRole('button', { name: new RegExp(deviceCode) }).click()
    await page.getByRole('button', { name: /删除设备/ }).click()
    await page.waitForTimeout(1000)
    result.cleanupDeviceRemoved = (await page.getByRole('button', { name: new RegExp(deviceCode) }).count()) === 0

    await page.screenshot({ path: 'qa-screenshots/real-backend-flow.png', fullPage: true })
    console.log(JSON.stringify(result, null, 2))
  } catch (error) {
    console.error(JSON.stringify({ ...result, error: error instanceof Error ? error.message : String(error) }, null, 2))
    process.exitCode = 1
  } finally {
    await browser.close()
  }
}

await main()
