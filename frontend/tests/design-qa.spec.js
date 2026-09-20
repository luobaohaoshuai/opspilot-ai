import { expect, test } from '@playwright/test'

test('访客可以从首页进入 Vue 登录页', async ({ page }) => {
  await page.setViewportSize({ width: 1440, height: 1024 })
  await page.goto('/')

  await expect(page.getByRole('heading', { name: 'OpsPilot AI' })).toBeVisible()
  await page.getByRole('button', { name: '登录控制台' }).first().click()
  await expect(page).toHaveURL(/#\/login/)
  await expect(page.getByRole('heading', { name: '登录控制台' })).toBeVisible()

  await expect(page.getByRole('button', { name: '进入 OpsPilot' })).toBeVisible()
  await expect(page.getByText('默认演示账号已填好，点击即可进入 OpsPilot。')).toBeVisible()
})

test('移动端首页菜单可以正常打开', async ({ page }) => {
  await page.setViewportSize({ width: 390, height: 844 })
  await page.goto('/')

  await expect(page.getByRole('heading', { name: 'OpsPilot AI' })).toBeVisible()
  await page.getByRole('button', { name: '打开菜单' }).click()
  await expect(page.getByRole('button', { name: '查看接口' }).last()).toBeVisible()
  await expect(page.getByRole('button', { name: '登录控制台' }).last()).toBeVisible()
})
